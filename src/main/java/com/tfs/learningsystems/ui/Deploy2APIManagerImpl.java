package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.Deploy2ApiResponseHandler;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2Module;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2ModulePackage;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.GitHubUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHRelease;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Class to implement methods for Deploy2 API calls
 */
@Component
@Slf4j
@Qualifier("deploy2APIManagerBean")
public class Deploy2APIManagerImpl implements Deploy2APIManager {


  @Inject
  @Qualifier("apiCallManager")
  private APICallManager apiCallManager;

  @Autowired
  private AppConfig appConfig;

  private static final String FAILED_RETRIEVE_MODULE_FROM_DEPLOY2API =
          "Failed while retrieving module {} from content Deploy2API for Module ";

  /**
   * Method to get/handle response from Deploy2 API
   *
   * @param headers from Deploy2 API.
   * @param builder from UriComponentsBuilder
   * @param retries describes number of retry to be happened
   * @return response
   */
  private ResponseEntity<String> fetchOrRetry (HttpHeaders headers , UriComponentsBuilder builder, int retries) throws IOException {
    ResponseEntity<String> response = null;
    long retryTimeout = appConfig.getGitHubRetryTimeout();
    try{
      response = apiCallManager.restTemplateGetCall(headers, builder, new Deploy2ApiResponseHandler());
    } catch (Exception ex) {
      if(retries > 0) {
        try {
          Thread.sleep(retryTimeout);
        }catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new InternalServerErrorException(Response
                  .status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                          ErrorMessage.CONNECTION_ERROR)).build());
        }
        return fetchOrRetry(headers, builder, retries - 1);
      }
    }
    return response;
  }

  /**
   * Method to find Module from Deploy2 API
   *
   * @param moduleName Module name to serach for.
   * @return Lists of TFSDeploy2Module modules
   */
  @Override
  public List<TFSDeploy2Module> findModule(String moduleName) {

    StringBuilder errorMessage = new StringBuilder("Failed while finding Module with name ");
    errorMessage.append(moduleName).append(" in Deploy2API");
    List<TFSDeploy2Module> tfsDeploy2Modules = null;
    try {
      StringBuilder getModulesURL = new StringBuilder(appConfig.getDeploy2APIURl())
          .append(Constants.FORWARD_SLASH).append(Constants.DEPLOY2_MODULES_URI);

      // getModulesURL example value  http://stable.api.sv2.247-inc.net/v1/contentdeployment/modules

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getModulesURL.toString())
          .queryParam(Constants.DEPLOY2_NAME_PARAM, moduleName);

      HttpHeaders headers = apiCallManager.fillHeadersDetails();
      ResponseEntity<String> response = fetchOrRetry(headers, builder, appConfig.getGitHubRetryCount());

      if(response != null) {
        tfsDeploy2Modules = apiCallManager.reponseStatusCheck(response, TFSDeploy2Module.class, HttpStatus.OK,
                        Constants.DEPLOY2_RESPONSE_BODY_PARAM, errorMessage.toString(), appConfig.getDeploy2APIURl());
      }
    } catch (WebApplicationException wex) {
      log.error(FAILED_RETRIEVE_MODULE_FROM_DEPLOY2API,
          moduleName, wex);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());

    } catch (IOException ioe) {
      log.error(FAILED_RETRIEVE_MODULE_FROM_DEPLOY2API,
          moduleName, ioe);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());

    } catch (ClassNotFoundException cnfe) {
      log.error(FAILED_RETRIEVE_MODULE_FROM_DEPLOY2API,
          moduleName, cnfe);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }
    return tfsDeploy2Modules;
  }


  /**
   * Method to create Module in Deploy2 API.
   *
   * @param ifHiddenModule if Module is hidden module
   * @param moduleName name of the module created
   * @param moduleDesc Description of the module
   * @param repositoryName Repository name agiant which the moule is getting created
   * @param packagePreserveTime To preserve the time of time of package creation , impact is when
   * module gets modified
   * @param compress Compression flag
   * @param deepPackage If the time stamp preserve is on sub directories as well.
   * @param contactUser User contacted when module is created
   */
  @Override
  public Integer createModule(Boolean ifHiddenModule, String moduleName, String moduleDesc,
      String gitHubOrgName,
      String repositoryName, Boolean packagePreserveTime, Boolean compress, Boolean deepPackage,
      String contactUser) {

    ResponseEntity<String> response = null;
    StringBuilder errorMessage = new StringBuilder("Failed while creating Module ");
    errorMessage.append(moduleName).append(" in Deploy2API");
    Integer moduleId;

    try {

      HttpHeaders headers = apiCallManager.fillHeadersDetails();
      StringBuilder createModuleURL = new StringBuilder(appConfig.getDeploy2APIURl())
          .append(Constants.FORWARD_SLASH).append(Constants.DEPLOY2_MODULES_URI);

      // Sample create Module URL  http://stable.api.sv2.247-inc.net/v1/contentdeployment/modules

      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add(Constants.DEPLOY2_MODULES_HIDDEN, ifHiddenModule.toString());
      map.add(Constants.DEPLOY2_NAME_PARAM, moduleName);
      map.add(Constants.DEPLOY2_MODULE_CONTACTS, contactUser);
      map.add(Constants.DEPLOY2_MODULE_TYPE, Constants.DEPLOY2_MODULE_TYPE_GIT);
      map.add(Constants.DEPLOY2_DESC_PARAM, moduleDesc);
      map.add(Constants.DEPLOY2_GIT_HUB_ORG_PARAM, gitHubOrgName);
      map.add(Constants.DEPLOY2_GIT_HUB_SERVER_PARAM, appConfig.getGitHubServer());
      map.add(Constants.DEPLOY2_GIT_HUB_REPO_PARAM, repositoryName);
      map.add(Constants.DEPLOY2_MODULE_PRESEERVE_TIME_PARAM, packagePreserveTime.toString());
      map.add(Constants.DEPLOY2_MODULE_COMPRESS_PARAM, compress.toString());
      map.add(Constants.DEPLOY2_MODULE_DEEP_PACKAGE_PARAM, deepPackage.toString());

      response = apiCallManager.restTemplatePostCall(headers, createModuleURL.toString(), map,
          new Deploy2ApiResponseHandler());
      apiCallManager.checkHttpReturnStatus(response, HttpStatus.OK, errorMessage.toString(), createModuleURL.toString());
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, Object> responseBody = mapper.readValue(response.getBody(), HashMap.class);
      moduleId = (Integer) responseBody.get("id");

      return moduleId;
    } catch (WebApplicationException wex) {
      log.error("Failed while creating Module in Deploy2API for module {} ", moduleName, wex);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());

    } catch (Exception ex) {
      log.error("Failed while creating Module in Deploy2API for module {} ", moduleName, ex);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }
  }


  /**
   * Find a package againt a module
   *
   * @return a List of TFSDeploy2ModulePackage type found for the package name
   */
  @Override
  public List<TFSDeploy2ModulePackage> findModulePackages(Integer moduleId, String packageName) {

    StringBuilder errorMessage = new StringBuilder("Failed while finding Package ");
    errorMessage.append(packageName).append(" in Deploy2API ").append(" for ").append(moduleId);
    List<TFSDeploy2ModulePackage> tfsDeploy2ModulePackages = null;
    try {
      HttpHeaders headers = apiCallManager.createAuthHeaders(appConfig.getMwbServiceACUserName(),
          appConfig.getMwbServiceACUserPwd());

      StringBuilder getPackageURL = new StringBuilder(appConfig.getDeploy2APIURl())
          .append(Constants.FORWARD_SLASH)
          .append(Constants.DEPLOY2_MODULES_URI)
          .append(Constants.FORWARD_SLASH)
          .append(moduleId).append(Constants.FORWARD_SLASH)
          .append(Constants.DEPLOY2_PACKAGES_URI);

      // sample getPackageURL http://stable.api.sv2.247-inc.net/v1/contentdeployment/modules/4823/packages

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getPackageURL.toString())
          .queryParam(Constants.DEPLOY2_NAME_PARAM, packageName);

      ResponseEntity<String> response = apiCallManager.restTemplateGetCall(headers,
          builder, new Deploy2ApiResponseHandler());

      tfsDeploy2ModulePackages = apiCallManager.reponseStatusCheck(response, TFSDeploy2ModulePackage.class, HttpStatus.OK,
              Constants.DEPLOY2_RESPONSE_BODY_PARAM, errorMessage.toString(), getPackageURL.toString());

    } catch (WebApplicationException wex) {
      log.error("Failed while retrieving package {} from Deploy2 API for module {} !!",
          packageName, moduleId, wex);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());

    } catch (Exception ex) {
      log.error("Failed while retrieving package {} from Deploy2 API for module {} !!",
          packageName, moduleId, ex);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }
    return tfsDeploy2ModulePackages;
  }


  /**
   * Method to create package in Deploy2 APi
   *
   * @param moduleId Module Id of the module againt which the package is gettung created
   * @param packageName Package name
   * @param userEmail user who has triggered this action
   * @param gitHubTag gitHubTag which is used for this package creation
   * @param gitHubTagRefType gitHub reference type used for package creation, tags is what is
   * supported
   * @param tagDetailComments any comments on the tag
   * @param isGood if the package is good or obsolete
   */
  @Override
  public Boolean createPackage(String moduleId, String packageName, String userEmail,
      String gitHubTag, String gitHubTagRefType, String tagDetailComments, boolean isGood) {

    StringBuilder errorMessage = new StringBuilder("Failed while creating  package ");
    errorMessage.append(packageName).append(" in Deploy2API ").append(" for ").append(moduleId);
    ResponseEntity<String> response = null;

    try {

      HttpHeaders headers = apiCallManager.fillHeadersDetails();
      StringBuilder createPackageURL = new StringBuilder(appConfig.getDeploy2APIURl())
          .append(Constants.FORWARD_SLASH).append(Constants.DEPLOY2_PACKAGES_URI);

      // sample http://stable.api.sv2.247-inc.net/v1/contentdeployment/packages

      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

      map.add(Constants.DEPLOY2_MODULE_ID_PARAM, moduleId);
      map.add(Constants.DEPLOY2_NAME_PARAM, packageName);
      map.add(Constants.DEPLOY2_USER_PARAM, userEmail);
      map.add(Constants.DEPLOY2_COMMENT, tagDetailComments);
      map.add(Constants.DEPLOY2_PACKAGE_GIT_HUB_REF, gitHubTag);

      map.add(Constants.DEPLOY2_PACKAGE_GIT_HUB_REF_TYPE, gitHubTagRefType);

      if (isGood) {
        map.add(Constants.DEPLOY2_MARK, Constants.DEPLOY2_PACKAGE_GOOD);
      } else {
        map.add(Constants.DEPLOY2_MARK, Constants.DEPLOY2_PACKAGE_OBSOLETE);
      }

      response = apiCallManager.restTemplatePostCall(headers, createPackageURL.toString(), map,
          new Deploy2ApiResponseHandler());

      apiCallManager.checkHttpReturnStatus(response, HttpStatus.OK, errorMessage.toString(), createPackageURL.toString());

      return true;

    } catch (WebApplicationException wex) {

      log.error("Failed while creating package {} in Deploy2API for module {} !!", packageName,
          moduleId, wex);

      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());

    } catch (Exception ex) {

      log.error("Failed while creating package {} in Deploy2API for module {} !!", packageName,
          moduleId, ex);

      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }
  }

  /**
   * Method to find a create Package or Model Name
   *
   * @return package or Module name
   */
  @Override
  public String createPackageOrModuleName(MwbItsClientMapBO mwbItsClientMapBO, String creationType,
      String tag) {
    StringBuilder moduleOrPackage = new StringBuilder();

    String currentDelimiter = "";

    if (creationType.equals(Constants.DEPLOY2_MODULE_CREATION_TYPE) || creationType.equals(Constants.DEPLOY2_PACKAGE_CREATION_TYPE) ) {
      currentDelimiter = Constants.DEPLOY2_MODULE_NAME_DELIMITER;
    }

    moduleOrPackage.append(Constants.DEPLOY2_CLIENTS_STRING)
        .append(currentDelimiter)
        .append(mwbItsClientMapBO.getItsClientId())
        .append(currentDelimiter)
        .append(Constants.DEPLOY2_APPLICATION_STRING)
        .append(currentDelimiter)
        .append(mwbItsClientMapBO.getItsAppId())
        .append(currentDelimiter)
        .append(Constants.DEPLOY2_MODELS_STRING);

    // module sample clients--cap1caat--applications--hrchat--models

    // package sample clients/cap1caat/applications/hrchat/models

    if (creationType.equals(Constants.DEPLOY2_PACKAGE_CREATION_TYPE)) {
      moduleOrPackage.append(currentDelimiter);
      moduleOrPackage
          .append(StringUtils.isNotEmpty(tag) ? tag : GitHubUtils.getTagForCurrentDate());
    }
    return moduleOrPackage.toString();
  }


  /**
   * Load or Create conventional Module
   *
   * @param gitHubOrgName gitHubOrg name - used while creating a module if not already available
   * @param repositoryName Repository name  - used while creating a module if not already available
   * @param moduleName module Name used to look up or create Module
   * @return Module Id
   */
  @Override
  public Integer loadOrCreateConvensionModule(
      String gitHubOrgName, String repositoryName, String moduleName, String userEmail) {

    List<TFSDeploy2Module> modules = findModule(moduleName);

    if (modules != null && modules.size() > 1) {

      log.error(" Multiple Modules found with the name - : {}  ",
          moduleName);

      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("invalid_modules");
      error.setMessage("Cannot install on multiple modules");
      throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }

    Integer moduleId = null;
    if (modules == null || modules.isEmpty()) {
      moduleId = createModule(false, moduleName, moduleName, gitHubOrgName,
          repositoryName, true, false, true,
          userEmail);
    }

    if (moduleId == null && modules != null && !modules.isEmpty()) {
      moduleId = modules.get(0).getId();
    }

    return moduleId;
  }

  /**
   * Load or Create conventional Package
   *
   * @param mwbItsClientMapBO mwbItsClientMapBO used to create Package name .
   * @param ghRelease User to retrive tag which is used in package name creation
   * @param moduleName module name used to look up or create Module
   * @return packageName String
   */

  @Override
  public String loadOrCreateConvensionPackage(MwbItsClientMapBO mwbItsClientMapBO,
      GHRelease ghRelease, Integer moduleId, String moduleName) {

    String packageName = createPackageOrModuleName(mwbItsClientMapBO,
        Constants.DEPLOY2_PACKAGE_CREATION_TYPE,
        ghRelease.getTagName());

    List<TFSDeploy2ModulePackage> packages = findModulePackages(moduleId, packageName);

    if (packages != null && !packages.isEmpty()) {

      log.error(" Multiple Packages with name {} found on module - : {}  ",
          packageName, moduleName);

      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("invalid_package");
      error.setMessage("Cannot install on invalid Package");
      throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());

    }

    createPackage(moduleId.toString(), packageName, AuthUtil.getPrincipalFromSecurityContext(null),
        ghRelease.getTagName(), Constants.DEPLOY2_PACKAGE_GIT_HUB_REF_TYPE_TAG_VALUE,
        Constants.EMPTY_STRING, true);

    return packageName;
  }


  @Override
  public String extractPackageFromModuleAndCheck(TFSDeploy2Module tfsDeploy2Module,
      GHRelease gitHubRelease, String userEmail) {

    String packageName = null;
    StringBuilder packageNameBuf = new StringBuilder(StringUtils
        .replace(tfsDeploy2Module.getName(), Constants.DEPLOY2_MODULE_NAME_DELIMITER,
            Constants.DEPLOY2_MODULE_NAME_DELIMITER));

    packageNameBuf.append(Constants.DEPLOY2_MODULE_NAME_DELIMITER);
    packageNameBuf
        .append(StringUtils.isNotEmpty(gitHubRelease.getTagName()) ? gitHubRelease.getTagName()
            : GitHubUtils.getTagForCurrentDate());

    packageName = packageNameBuf.toString();
    List<TFSDeploy2ModulePackage> packages = findModulePackages(tfsDeploy2Module.getId(),
        packageName);

    if (packages == null || packages.isEmpty()) {
      createPackage(tfsDeploy2Module.getId().toString(), packageName, userEmail,
          gitHubRelease.getTagName(), Constants.DEPLOY2_PACKAGE_GIT_HUB_REF_TYPE_TAG_VALUE,
          Constants.EMPTY_STRING, true);

    } else if (packages.size() > 1) {
      log.error(" Multiple Packages with name {} found on module - : {}  ",
          packageName, tfsDeploy2Module.getName());

      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("invalid_packages");
      error.setMessage("Found multiple packages ro install");
      throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
    return packageName;
  }
}
