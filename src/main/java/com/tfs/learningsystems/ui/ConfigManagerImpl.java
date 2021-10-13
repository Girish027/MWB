package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.config.NLModelConfig;
import com.tfs.learningsystems.ui.model.config.SupportingFiles;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.CommonUtils;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Qualifier("configManagerBean")
@Slf4j
public class ConfigManagerImpl implements ConfigManager {

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  private AppConfig appConfig;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Override
  public void reloadDefaultConfig(final String configArchiveLocation, final String configName,
      final String language)
      throws IOException {

    ModelConfigBO modelConfigDetail = getModelConfigByName(configName);
    if (modelConfigDetail == null) {
      log.debug("Loading default config from archive file: {}", configArchiveLocation);
      modelConfigDetail = new ModelConfigBO();
      modelConfigDetail.setName("system_default_" + language);
      modelConfigDetail.setDescription("Default " + language + " Model Config");
      long currentTimeMillis = System.currentTimeMillis();
      modelConfigDetail.setCreatedAt(currentTimeMillis);
      modelConfigDetail.setModifiedAt(currentTimeMillis);
      modelConfigDetail.setProjectId(0);   // ?? why are we hard coding here
      modelConfigDetail.setUserId(AuthUtil.getPrincipalFromSecurityContext("reload_config"));

      ClassPathResource classPathResource =
          new ClassPathResource(configArchiveLocation);

      this.readConfigFile(modelConfigDetail, classPathResource.getInputStream());
      modelConfigDetail.create();

    }
  }

  @Override
  public void oldReloadDefaultConfig(final String configArchiveLocation, final String configName,
      final String language)
      throws IOException {

    ModelConfigBO modelConfigDetail = getModelConfigByName(configName);
    if (modelConfigDetail == null) {
      log.debug("Loading default config from archive file: {}", configArchiveLocation);
      modelConfigDetail = new ModelConfigBO();
      modelConfigDetail.setName(configName);
      modelConfigDetail.setDescription("Default " + language + " Model Config");
      long currentTimeMillis = System.currentTimeMillis();
      modelConfigDetail.setCreatedAt(currentTimeMillis);
      modelConfigDetail.setProjectId(0);
      modelConfigDetail.setUserId(AuthUtil.getPrincipalFromSecurityContext("old_reload_config"));
      // modelConfigDetail.setId("0");    // do we really need to do this?
      ClassPathResource classPathResource =
          new ClassPathResource(configArchiveLocation);
      this.oldReadArchiveFile(modelConfigDetail, classPathResource.getInputStream());
      modelConfigDetail.create();

    }
  }

  @Override
  public ModelConfigBO addModelConfig(final ModelConfigBO modelConfig,
      final InputStream fileInputStream) {

    long currentTimeMillis = System.currentTimeMillis();
    modelConfig.setCreatedAt(currentTimeMillis);
    modelConfig.setModifiedAt(currentTimeMillis);

    try {
      this.readConfigFile(modelConfig, fileInputStream);
    } catch (IOException e) {
      throw new BadRequestException("invalid_config_archive_file", e);
    }
    modelConfig.create();
    return modelConfig;
  }

  @Override
  public ModelConfigBO addModelConfig(final ModelConfigBO modelConfig) {

    long currentTimeMillis = System.currentTimeMillis();
    modelConfig.setCreatedAt(currentTimeMillis);
    modelConfig.setModifiedAt(currentTimeMillis);
    String projectId = modelConfig.getProjectId().toString();
    ProjectBO projectBO = new ProjectBO();
    projectBO = projectBO.findOne(projectId);

    ModelConfigBO systemDefault = new ModelConfigBO();
    systemDefault = systemDefault.findOne(ModelConfigBO.FLD_NAME, Constants.DEFAULT_EN_CONFIG_NAME);

    if (modelConfig.getConfigFile() == null) {
      modelConfig.setConfigFile(systemDefault.getConfigFile());
    }

    ClientBO clientBO = new ClientBO();
    clientBO = clientBO.findOne(projectBO.getClientId().toString());
    ActionContext.init(clientBO.getCid(), null, clientBO.getId(), clientBO.getName(),
              "mwb-team@247-inc.com", Arrays.asList("MWB_ROLE_TEST"));
    //modelConfig.setCid(clientBO.getCid());
    modelConfig.create();
    return (modelConfig);
  }

  @Override
  public ModelConfigBO getModelConfigById(final String configId) {
    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig = modelConfig.findOne(configId);
    return (modelConfig);
  }

  @Override
  public ModelConfigBO getModelConfigById(final String clientId, final String configId) {

    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig = modelConfig.findOne(configId);

    if (modelConfig == null) {
      log.error("Failed to find the related  model config for configId  : {} ", configId
      );

      throw new NotFoundException(
          new Error(Response.Status.NOT_FOUND.getStatusCode(), "model_config_cid_combination_error",
              " ClientId:'" + clientId + " cId from request :'" + ActionContext.getClientId()
                  + " configId  :'" + configId + "'" + " combination not found "));

    }

    if (!modelConfig.getName().equalsIgnoreCase(Constants.DEFAULT_EN_CONFIG_NAME)
            && !modelConfig.getName().equalsIgnoreCase(Constants.OLD_DEFAULT_EN_CONFIG_NAME)
            && !modelConfig.getCid().equalsIgnoreCase(ActionContext.getClientId())) { // TODO - Remove condition when we have client level default configss
      log.error(
              "Failed to find the related  model config for configId  : {} and clientID : {} cid : {}",
              configId, clientId,
              ActionContext.getClientId());

      throw new NotFoundException(
              new Error(Response.Status.NOT_FOUND.getStatusCode(),
                      "model_config_cid_combination_error",
                      " ClientId:'" + clientId + " cId from request :'" + ActionContext.getClientId()
                              + " configId  :'" + configId + "'" + " combination not found "));

    }

    return (modelConfig);
  }

  @Override
  public ModelConfigBO getModelConfigByName(final String configName) {

    ModelConfigBO config = new ModelConfigBO();

    config = config.findOne(ModelConfigBO.FLD_NAME, configName);
    return (config);
  }

  @Override
  public ModelConfigCollection getModelConfigsByProject(String clientId, final String projectId) {

    // TODO Client_Isolation - add ClientId as part of this query

    validationManager.validateClientAndProject(clientId, projectId);

    ModelConfigBO configBO = new ModelConfigBO();
    ModelConfigCollection modelConfigsByProjectId = configBO.getModelConfigsByProjectId(projectId);

    // Add system default config to list
    // todo: get project's language specific default
    ModelConfigBO defaultConfig = new ModelConfigBO();
    defaultConfig = defaultConfig.findOne(ModelConfigBO.FLD_NAME, Constants.DEFAULT_EN_CONFIG_NAME);
    modelConfigsByProjectId.add(defaultConfig);
    return modelConfigsByProjectId;
  }


  @Override
  public ModelConfigBO getProjectsLatestModelConfig(final String projectId) {
    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig = modelConfig.getProjectsLatestConfig(projectId);

    // if project doesn't have a specific config, use system default
    // todo: get project's language specific default
    if (modelConfig == null) {
      modelConfig = new ModelConfigBO();
      modelConfig = modelConfig.findOne(ModelConfigBO.FLD_NAME, Constants.DEFAULT_EN_CONFIG_NAME);
    }
    return (modelConfig);
  }

  @Override
  public ModelConfigBO getModelConfigDataById(final String configId) {
    return this.getModelConfigById(configId);
  }

  @Override
  public ModelConfigBO getModelConfigDataById(final String clientId, final String configId) {
    return this.getModelConfigById(clientId, configId);
  }

  @Override
  public ModelConfigBO getModelConfigDataByName(final String configName) {
    return this.getModelConfigByName(configName);
  }

  @Override
  public File getModelConfigFile(final ModelConfigBO modelConfig)
      throws IOException {
    File archiveFile = null;
    try {
      CommonUtils.deleteFilesFromTempFolder(appConfig.getGitHubRetryCount());

      Path tempDirectory = Files.createTempDirectory("config");

      String configFile = modelConfig.getConfigFile();
      Path configFilePath = Files.createFile(tempDirectory.resolve("config.json"));
      Files.write(configFilePath, configFile.getBytes(StandardCharsets.UTF_8));

      archiveFile = configFilePath.toFile();
    } catch (IOException e) {
      throw new BadRequestException("invalid_config_archive_file - config json creation issue ", e);
    }
    return archiveFile;
  }


  @Override
  public ModelConfigBO updateModelConfigById(final String configId,
      final ModelConfigBO modelConfig) {
    modelConfig.update();
    return (modelConfig);
  }

  @Override
  public ModelConfigCollection listConfigs(final Integer limit,
      final Integer startIndex) {
    ModelConfigBO configBO = new ModelConfigBO();
    return (configBO.listConfigs(limit, startIndex));
  }

  @Override
  public Map<String, String> getWordExpansions(final ModelConfigBO modelConfig,
      boolean isReadJSONConfig) {
    Map<String, String> wordExpansions = new HashMap<>();

    String wordExpansionsFile = null;
    if (isReadJSONConfig) {
      wordExpansionsFile = modelConfig.getConfigFile();
    } else {
      wordExpansionsFile = modelConfig.getContractionsFile();
    }

    if (wordExpansionsFile == null || wordExpansionsFile.isEmpty()) {
      return wordExpansions;
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        new ByteArrayInputStream(wordExpansionsFile.getBytes(StandardCharsets.UTF_8))))) {
      String line = null;
      int lineNo = 0;
      while ((line = reader.readLine()) != null) {
        lineNo++;
        String[] words = line.split(",");
        if (isValidWordExpansionLine(words)) {
          wordExpansions.put(words[0], words[1]);
        } else {
          log.warn("invalid word expansion line:- {} :-line no is {}",
              line, lineNo);
        }
      }
    } catch (IOException e) {
      log.error("Failed to read wordExpansions file", e);
    }
    return wordExpansions;
  }

  @Override
  public Set<String> getStopwords(final ModelConfigBO modelConfig, boolean isReadJSONConfig) {
    Set<String> stopwords = new HashSet<>();

    String stopwordsFile = null;
    if (isReadJSONConfig) {
      stopwordsFile = modelConfig.getConfigFile();
    } else {
      stopwordsFile = modelConfig.getStopwordsFile();
    }

    if (stopwordsFile == null || stopwordsFile.isEmpty()) {
      return stopwords;
    }
    // Read Stopwords
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            new ByteArrayInputStream(stopwordsFile.getBytes(StandardCharsets.UTF_8))))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        stopwords.add(line.trim());
      }
    } catch (IOException e) {
      log.error("Failed to read stopwords file", e);
    }
    return stopwords;
  }

  @Override
  public Set<String> getStemmingExceptions(final ModelConfigBO modelConfig,
      boolean isReadJSONConfig) {
    Set<String> stemmingExceptions = new HashSet<>();

    String stemmingExceptionsFile = null;
    if (isReadJSONConfig) {
      stemmingExceptionsFile = modelConfig.getConfigFile();
    } else {
      stemmingExceptionsFile = modelConfig.getStemmingExceptionsFile();
    }

    if (stemmingExceptionsFile == null || stemmingExceptionsFile.isEmpty()) {
      return stemmingExceptions;
    }
    // Read Stemming Exceptions
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            new ByteArrayInputStream(stemmingExceptionsFile.getBytes(StandardCharsets.UTF_8))))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stemmingExceptions.add(line.trim());
      }
    } catch (IOException e) {
      log.error("Failed to read stemmingExceptions file", e);
    }
    return stemmingExceptions;
  }

  @Override
  public Map<String, String> getWordClasses(final ModelConfigBO modelConfig,
      boolean isReadJSONConfig) {
    Map<String, String> wordClasses = new HashMap<>();

    String wordClassesFile = null;
    if (isReadJSONConfig) {
      wordClassesFile = modelConfig.getConfigFile();
    } else {
      wordClassesFile = modelConfig.getWordClassesFile();
    }

    if (wordClassesFile == null || wordClassesFile.isEmpty()) {
      return wordClasses;
    }
    // Read Word Classes
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
            new ByteArrayInputStream(wordClassesFile.getBytes(StandardCharsets.UTF_8))))) {
      String line;
      String wrdclass = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.equals("")) {
          continue;
        }
        if (line.startsWith("_class_")) {
          wrdclass = line;
          continue;
        }
        wordClasses.put(line, wrdclass);
      }
    } catch (IOException e) {
      log.error("Failed to read wordClasses file", e);
    }
    return wordClasses;
  }

  private boolean isValidWordExpansionLine(String[] words) {
    return words.length == 2 && !words[1].trim().isEmpty() && !words[0]
        .trim().isEmpty();
  }

  private void readConfigFile(ModelConfigBO configDetail, InputStream inputStream)
      throws IOException {

    String configFile = null;
    if (inputStream != null) {
      configFile = IOUtils.toString(inputStream, Charset.defaultCharset());
    }
    configDetail.setConfigFile(configFile);

  }


  private void oldReadArchiveFile(ModelConfigBO configDetail, InputStream inputStream)
      throws IOException {

    int readFiles = 0;
    Map<String, String> additionalFiles = new HashMap<>();
    String configFile = null;
    try (ZipInputStream stream = new ZipInputStream(inputStream)) {
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {
        String name = entry.getName();
        log.debug("Reading from file: {}", name);

        // Come up with a better ignore list?
        if (entry.isDirectory()
                || name.contains(".DS_Store")
                || name.contains("__MACOSX")) {
          continue;
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, Charset.defaultCharset());

        if (name.endsWith("config.json")) {
          configFile = writer.toString();
          readFiles++;
        } else {
          int index = name.lastIndexOf('/') + 1;
          String fileName = name.substring(index);
          additionalFiles.put(fileName, writer.toString());
        }
      }
      if (readFiles < 1) {
        throw new IOException("invalid_config_archive_file - missing config.json");
      }
    }
    ObjectMapper objectMapper = new ObjectMapper();
    NLModelConfig
        nlModelConfig = objectMapper.readValue(configFile, NLModelConfig.class);
    configDetail.setConfigFile(configFile);
    if (Boolean.TRUE.equals(nlModelConfig.getUseLegacyConfigFile())) {
      configDetail.setLegacyConfigFile(additionalFiles.getOrDefault(
          nlModelConfig.getLegacyConfigFileName(), ""));
    }

    SupportingFiles supportingFiles =
        nlModelConfig.getSupportingFiles();
    String contractions = supportingFiles.getContractions();
    if (contractions != null) {
      if (!additionalFiles.containsKey(contractions)) {
        throw new IOException("invalid_config_archive_file - missing contractions file");
      }
      configDetail.setContractionsFile(additionalFiles.get(contractions));
    }
    String wordClasses = supportingFiles.getWordClasses();
    if (wordClasses != null) {
      if (!additionalFiles.containsKey(wordClasses)) {
        throw new IOException("invalid_config_archive_file - missing word classes file");
      }
      configDetail.setWordClassesFile(additionalFiles.get(wordClasses));
    }
    String stopwords = supportingFiles.getStopwords();
    if (stopwords != null) {
      if (!additionalFiles.containsKey(stopwords)) {
        throw new IOException("invalid_config_archive_file - missing stopwords file");
      }
      configDetail.setStopwordsFile(additionalFiles.get(stopwords));
    }
    String stemmingExceptions = supportingFiles.getStemmingExceptions();
    if (stemmingExceptions != null) {
      if (!additionalFiles.containsKey(stemmingExceptions)) {
        throw new IOException("invalid_config_archive_file - missing stemming exceptions file");
      }
      configDetail.setStemmingExceptionsFile(additionalFiles.get(stemmingExceptions));
    }
  }
}
