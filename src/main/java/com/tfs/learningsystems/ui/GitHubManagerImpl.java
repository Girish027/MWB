package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.TFSProjectModel;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.GitHubUtils;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Qualifier("gitHubManagerBean")
public class GitHubManagerImpl implements GitHubManager {

  @Autowired
  private AppConfig appConfig;
  @Autowired
  private ConfigManager configManager;
  @Autowired
  @Qualifier("orionManagerBean")
  private OrionManager orionManager;

  /**
   * Method to create Models and configs files required for checking in github
   *
   * @param clientId client Id used for model / config loading
   * @param modelsToDeploy models which are deploy for this client
   * @param projectModelFiles model files loaded for a project. Each project has single model file
   * which will be deployed/ checked in in github . used in parent method.
   * @param projectConfigFiles config files loaded for a project. Each project has single model
   * config file which will be deployed/ checked in in github.  used in parent method models/configs
   * into git hun
   * @param projectModelsinResponse TFSProjectModels which will be returned as part of publish API
   * Response on successfule installation into Deplo2API
   */
  @Override
  public void createModelConfigs(String clientId, List<ModelBO> modelsToDeploy,
      Map<Integer, byte[]> projectModelFiles, Map<Integer, byte[]> projectConfigFiles,
      List<TFSProjectModel> projectModelsinResponse)
      throws IOException {

    ModelConfigBO configDetail;
    byte[] modelFileBytes;
    byte[] configFileBytes;
    TFSProjectModel tfsProjectModel;

    for (ModelBO modelElement : modelsToDeploy) {
      tfsProjectModel = new TFSProjectModel();
      modelFileBytes = orionManager.getBuiltModelByteStreamFromOrion(modelElement.getModelId());
      projectModelFiles.put(modelElement.getProjectId(), modelFileBytes);
      configDetail = configManager.getModelConfigDataById(modelElement.getConfigId().toString());
      configFileBytes = configDetail.getConfigFile().getBytes(StandardCharsets.UTF_8);
      projectConfigFiles.put(modelElement.getProjectId(), configFileBytes);
      tfsProjectModel.setModelUUID(modelElement.getModelId());
      tfsProjectModel.setModelId(modelElement.getId().toString());
      tfsProjectModel.setModelName(modelElement.getName());
      tfsProjectModel.setProjectId(modelElement.getProjectId().toString());
      projectModelsinResponse.add(tfsProjectModel);
    }
  }

  private GitHub gitHubConnect() throws IOException {
    return GitHub.connectToEnterpriseWithOAuth(appConfig.getGitHubBaseURL(), appConfig.getGitHubUserName(),
                    appConfig.getGitHubUserToken());

  }

  private GHOrganization fetchOrRetry(String gitHubBaseOrg, int retries) throws IOException {
    GHOrganization gitOrg = null;
    log.info("Going to fetch org details from github for base org: " + gitHubBaseOrg);
    long retryTimeout = appConfig.getGitHubRetryTimeout();
    try {
      GitHub github = gitHubConnect();
      gitOrg = github.getOrganization(gitHubBaseOrg);
    } catch (IOException e) {
      if(retries > 0) {
        log.error("Socket or SSL Exception while fetching organization. Sleeping " + retryTimeout / 1000 +
                " seconds before retrying... ; will try " + retries + " more time(s)", e.getMessage());
        try {
          Thread.sleep(retryTimeout);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw (IOException) new InterruptedIOException().initCause(e);
        }
        return fetchOrRetry(gitHubBaseOrg,retries - 1);
      }
      throw new IOException(e);
    }
    return gitOrg;
  }

  /**
   * To check and create Repsoitory if already not available.
   *
   * @param mwbItsClientMapBO ITSClientId and ITSAppId used for git hub repository / Deploy2API
   * module / Deploy2API package creation
   * @param clientId Client ID used for logging purposes in case of exceptions
   * @param validProjects List of valid project id and name map. project name will be used as model
   * file / config file  name
   * @param repositoryName Repository name for creating repository
   * @param modelFiles Map of model files agiant a project Id.
   * @param configFiles Map of config files against a project Id.
   * @param modelsSummary Model summary used as Tag summary which creating a tag on repository
   * @param tag Tag created after the check in of all model and config files
   */
  @Override
  public GHRelease checkOrCreateRepositories(MwbItsClientMapBO mwbItsClientMapBO, String clientId,
      Map<Integer, String> validProjects, Map<Integer, byte[]> modelFiles,
      Map<Integer, byte[]> configFiles, String gitHubBaseOrg, String repositoryName,
      StringBuilder modelsSummary,
      String tag, String userEmail) {

    try {
      GHRepository gitRepo = null;
      GHOrganization gitOrg = null;
      GHCreateRepositoryBuilder repoBuilder;
      boolean ifFreshRepository = false;
      int retries = appConfig.getGitHubRetryCount();

      gitOrg = fetchOrRetry(gitHubBaseOrg, retries);
      gitRepo = gitOrg.getRepository(repositoryName);
      ifFreshRepository = false;
      if (gitRepo == null) {
        ifFreshRepository = true;
        repoBuilder = gitOrg.createRepository(repositoryName);
        gitRepo = repoBuilder.autoInit(true).description(repositoryName).create();

      }
      return checkInModelsAndConfigs(gitRepo, ifFreshRepository, modelFiles, configFiles,
          validProjects, mwbItsClientMapBO, modelsSummary, tag, userEmail);

    } catch (
        IOException e) {
      String message =
          "Exception on authenticating with gihub-api while publishing a model in Modeling Workbench: " + e.getMessage();
      log.error("Exception on authenticating with gihub-api while publishing a model in Modeling Workbench for client {} ",
          clientId, e);
      throw new ApplicationException(message, e);
    }
  }

  /**
   * Method to check in github models and config files
   *
   * @param gitRepo Git hub repository used to creating /updating files and creating tags
   * @param ifFreshRepository flag to specify if the repository in question was existing or just
   * created
   * @param modelFiles Project Id / model file map used to create and check-in model files
   * @param configFiles Project Id / model config file map used to create and check-in model files
   * @param validProjects Project Id / project file name map used to create and check-in model
   * files
   * @param mwbItsClientMapBO Database BO holding ITS client Id  / app id details used to update tag
   * summary while creating a tag
   * @param modelsSummary Existing models summary used to update tag summary while creating a tag
   * @param tag Tag name actually used for tag creation
   * @return GHRelease github release object containging details of the a release tag.
   */
  private GHRelease checkInModelsAndConfigs(GHRepository gitRepo, boolean ifFreshRepository,
      Map<Integer, byte[]> modelFiles, Map<Integer, byte[]> configFiles,
      Map<Integer, String> validProjects, MwbItsClientMapBO mwbItsClientMapBO,
      StringBuilder modelsSummary, String tag, String userEmail) throws IOException {

    String releaseTimeStamp = null;
    StringBuilder releaseDesc;
    String releaseSummary;

    if (ifFreshRepository) {
      createFreshModelFileContent(gitRepo, validProjects,
          modelFiles, configFiles);
    } else {
      updateModelFileContent(gitRepo, validProjects,
          modelFiles, configFiles);
    }

    releaseTimeStamp = GitHubUtils.getTagForCurrentDate();

    releaseSummary = createReleaseSummaryForRepository(mwbItsClientMapBO.getItsClientId(),
        mwbItsClientMapBO.getItsAccountId(), mwbItsClientMapBO.getItsAppId(), ifFreshRepository,
        modelsSummary.toString(), releaseTimeStamp, Constants.MASTER_BRANCH,
        userEmail);

    releaseDesc = new StringBuilder();

    releaseDesc.append(mwbItsClientMapBO.getItsClientId())
        .append(Constants.SPACE)
        .append(mwbItsClientMapBO.getItsAccountId())
        .append(Constants.SPACE)
        .append(mwbItsClientMapBO.getItsAppId())
        .append(Constants.SPACE)
        .append(releaseTimeStamp);

    // sample release summary cap1caat cap1caatac hrchat 20190520T232415

    return createGitHubReleaseAndTag(releaseDesc.toString(),
        gitRepo, tag, releaseSummary);
  }


  /**
   * Method to create fresh content on github Repository . No check required if a file with the name
   * exists previously
   *
   * @param ghRepository git hub repository handle giving access to check-in files
   * @param validProjects Name of model used for model file / config file
   * @param modelFiles Model files byte list used for model file creation
   * @param configFiles Config file byte list used for config file creation
   */
  private void createFreshModelFileContent(GHRepository ghRepository,
      Map<Integer, String> validProjects,
      Map<Integer, byte[]> modelFiles, Map<Integer, byte[]> configFiles)
      throws IOException {

    String modelTypeName;
    StringBuilder gitHubModelFile;
    StringBuilder configFileCheckInName;
    Integer projectId;

    for (Map.Entry<Integer, String> entry : validProjects.entrySet()) {
      modelTypeName = entry.getValue();
      projectId = entry.getKey();
      gitHubModelFile = new StringBuilder(
          StringUtils.replace(modelTypeName, Constants.SPACE, Constants.UNDER_SCORE))
          .append(Constants.MODEL_FILE_SUFFIX);

      ghRepository
          .createContent(modelFiles.get(projectId), modelTypeName, gitHubModelFile.toString()
          );

      configFileCheckInName = new StringBuilder(Constants.CONFIG_FILE_DIRECTORY)
          .append(Constants.FORWARD_SLASH)
          .append(StringUtils.replace(modelTypeName, Constants.SPACE, Constants.UNDER_SCORE))
          .append(Constants.CONFIG_FILE_SUFFIX);

      ghRepository.createContent(configFiles.get(projectId), modelTypeName,
          configFileCheckInName.toString());
    }
  }


  /**
   * Method to update an github Repository. No check required if a file with the name exists
   * previously
   *
   * @param ghRepository git hub repository handle giving access to check-in files
   * @param validProjects Project Id/ Name map of model used for model file / config file
   * @param modelFiles Model files byte list  used for model file creation
   * @param configFiles Config files byte list used for config file creation
   */
  private void updateModelFileContent(GHRepository ghRepository, Map<Integer, String> validProjects,
      Map<Integer, byte[]> modelFiles, Map<Integer, byte[]> configFiles) throws IOException {

    List<GHContent> ghRootContents = ghRepository.getDirectoryContent(Constants.ROOT_DIRECTORY);
    List<String> modelFileNames = ghRootContents.stream().map(GHContent::getName)
        .collect(Collectors.toList());
    GHContent fileContent;
    StringBuilder gitHubModelFile;
    String gitHubModelFileName;
    String modelTypeName;
    Iterator<GHContent> it = ghRootContents.iterator();
    List<String> configFileNames = null;
    List<GHContent> ghConfigContents = null;

    while (it.hasNext()) {
      GHContent element = it.next();
      if (element.isDirectory() && element.getName().equalsIgnoreCase(Constants.CONFIG_FILE_DIRECTORY)) {
        ghConfigContents = ghRepository
            .getDirectoryContent(Constants.CONFIG_DIRECTORY_PATH);
        configFileNames = ghConfigContents.stream().map(GHContent::getName)
            .collect(Collectors.toList());
        break;
      }
    }

    StringBuilder configFileCheckInName;
    String gitHubConfigFileName;
    Integer modelFileIndex;
    Integer configFileIndex = -1;
    Integer projectId;

    for (Map.Entry<Integer, String> entry : validProjects.entrySet()) {
      modelTypeName = entry.getValue();
      projectId = entry.getKey();
      gitHubModelFile = new StringBuilder(
          StringUtils.replace(modelTypeName, Constants.SPACE, Constants.UNDER_SCORE))
          .append(Constants.MODEL_FILE_SUFFIX);

      gitHubModelFileName = gitHubModelFile.toString();
      modelFileIndex = modelFileNames.indexOf(gitHubModelFileName);

      if (modelFileIndex >= 0) {
        fileContent = ghRootContents.get(modelFileIndex);
        if (fileContent != null) {
          fileContent.update(modelFiles.get(projectId), gitHubModelFileName);
        }
      } else {
        ghRepository
            .createContent(modelFiles.get(projectId), modelTypeName, gitHubModelFileName);
      }

      gitHubConfigFileName = StringUtils
          .replace(modelTypeName, Constants.SPACE, Constants.UNDER_SCORE);

      StringBuilder configFileNameWithSuffix = new StringBuilder(gitHubConfigFileName)
          .append(Constants.CONFIG_FILE_SUFFIX);

      configFileCheckInName = new StringBuilder(Constants.CONFIG_FILE_DIRECTORY)
          .append(Constants.FORWARD_SLASH)
          .append(gitHubConfigFileName)
          .append(Constants.CONFIG_FILE_SUFFIX);

      if (configFileNames != null) {
        configFileIndex = configFileNames.indexOf(configFileNameWithSuffix.toString().trim());
      }

      if (configFileIndex >= 0) {
        fileContent = ghConfigContents.get(configFileIndex);
        if (fileContent != null) {
          fileContent.update(configFiles.get(projectId), configFileCheckInName.toString());
        }
      } else {
        ghRepository.createContent(configFiles.get(projectId), modelTypeName,
            configFileCheckInName.toString());
      }
    }
  }

  /**
   * Method to createGit Hub Tag and Release
   *
   * @param releaseDesc Release description of tag creation
   * @param ghRepository github Repository handle used for tag creation
   * @param tag github Tag used in tag creation if available
   * @param releaseSummary github Release summary
   * @return GHRelease Git Hub Release tag object after github tag creation .
   */
  private GHRelease createGitHubReleaseAndTag(String releaseDesc, GHRepository ghRepository,
      String tag, String releaseSummary) throws IOException {

    GHReleaseBuilder releaseBuilder = ghRepository
        .createRelease(tag);

    releaseBuilder.prerelease(false);
    releaseBuilder.commitish(Constants.MASTER_BRANCH);
    releaseBuilder.name(releaseDesc);
    releaseBuilder.body(releaseSummary);
    releaseBuilder.draft(false);

    return releaseBuilder.create();
  }

  /**
   * Method used to create git hub release summary for tag creation
   *
   * @return ENtire release summary created.
   */
  private String createReleaseSummaryForRepository(String itsClientName, String itsAccountId,
      String itsAppId, boolean ifCreate, String modelsSummary, String timeStamp, String branch,
      String user) {

    StringBuilder releaseSummary = new StringBuilder();

    releaseSummary
        .append("Release for Client : ")
        .append(itsClientName)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append("App : ")
        .append(itsAppId)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append("Account : ")
        .append(itsAccountId)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append("Created by user : ")
        .append(user)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append(ifCreate ? " Created " : " Updated")
        .append(" at ")
        .append(timeStamp)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append("Models : ")
        .append(modelsSummary)
        .append(System.getProperty(Constants.LINE_SEPARATOR))
        .append("On branch : ")
        .append(branch);

    return releaseSummary.toString();
  }
}
