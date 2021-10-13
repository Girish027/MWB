package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.rest.ConfigsApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.tfs.learningsystems.util.CommonUtils;
import java.net.URI;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-10-11T14:48:26.319-04:00")
public class ConfigsApiServiceImpl extends ConfigsApiService {

  @Autowired
  private ConfigManager configManager;

  @Autowired
  private JsonConverter jsonConverter;

  @Override
  public Response addConfig(String name, String cid, String description, String projectId,
                            InputStream fileInputStream,
                            UriInfo uriInfo) throws NotFoundException {

    log.info("Adding model configuration - {} - {} ", projectId, name);
    try {
      fileInputStream.available();
    } catch (IOException e) {
      log.error("Failed adding model configuration - " + projectId + " - " + name, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig.setName(name);
    if (!StringUtils.isEmpty(cid)) {
      modelConfig.setCid(cid);
    }
    modelConfig.setDescription(description);
    modelConfig.setProjectId(Integer.parseInt(projectId));
    modelConfig.setUserId(currentUserEmail);

    ModelConfigBO modelConfigDetail =
        configManager.addModelConfig(modelConfig, fileInputStream);

    log.info("Added model configuration - {} - {} - {}", projectId, modelConfig.getId(), name);
    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + modelConfigDetail.getId()).build();
    return Response.created(locationURI)
        .entity(modelConfigDetail)
        .build();
  }

  @Override
  public Response getConfigById(final String configId) throws NotFoundException {
    log.info("Fetching model configuration - {}", configId);
    ModelConfigBO configDetail = configManager.getModelConfigById(configId);
    if (configDetail == null) {
      log.error("Failed to fetching model configuration - {}", configId);
      throw new javax.ws.rs.NotFoundException("Config with id  {" + configId + "} not found");
    }
    return Response.ok()
            .entity(configDetail)
            .build();

  }

  @Override
  public Response getConfigFilesById(final String configId) throws NotFoundException {
    try {
      log.info("Fetching model configuration file - {}", configId);
      ModelConfigBO configDetail = configManager.getModelConfigDataById(configId);
      if (configDetail == null) {
        log.info("Failed to find DB record for model configuration - {}", configId);
        throw new javax.ws.rs.NotFoundException("Config with id  {" + configId + "} not found");
      }
      File archiveFile = configManager.getModelConfigFile(configDetail);
      log.info("Reading model configuration file - {} - {}", configId, archiveFile.getName());

      FileInputStream fileInputStream = new FileInputStream(archiveFile);
      return Response.ok().header("Content-Length", archiveFile.length())
              .header("Content-Disposition", "attachment; filename=" + archiveFile.getName())
              .entity(fileInputStream).build();

    } catch (IOException e) {
      log.error("Failed reading model configuration file - " + configId, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                  ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }
  }

  @Override
  public Response listConfigs(Integer limit, Integer startIndex) throws NotFoundException {
    ModelConfigCollection configDetails = this.configManager.listConfigs(limit, startIndex);
    return Response.ok()
            .entity(configDetails)
            .build();
  }

  @Override
  public Response addConfig(final ModelConfigBO config,
                            final UriInfo uriInfo) {
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    config.setUserId(currentUserEmail);
    ModelConfigBO addedModelConfig = this.configManager.addModelConfig(config);

    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + addedModelConfig.getId()).build();
    log.info("Added model configuration by {} - {} ", currentUserEmail, locationURI.toString());
    return Response.created(locationURI)
            .entity(addedModelConfig)
            .build();
  }

  @Override
  public Response getConfigDataById(final String clientId, final String configId) {
    log.info("Fetching model configuration data - {}", configId);
    ModelConfigBO configDetail = configManager.getModelConfigDataById(clientId, configId);
    if (configDetail == null) {
      log.error("Failed in fetching configuration data - {}", configId);
      throw new javax.ws.rs.NotFoundException("Config with id  {" + configId + "} not found");
    }
    return Response.ok()
            .entity(configDetail)
            .build();
  }

  @Override
  public Response getWordClassFromConfig(final String clientId, final String configId, UriInfo uriInfo){
    ModelConfigBO model = configManager.getModelConfigById(configId);
    String configFile = model.getConfigFile();
    String wordclassFile = CommonUtils.getSpeedWorkProcessedMap(configFile);
    model.setWordClassesFile(wordclassFile);
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);

    ModelConfigBO modelConfig = new ModelConfigBO();
    modelConfig.setName(model.getName());
    modelConfig.setCid(model.getCid());
    modelConfig.setDescription(model.getDescription());
    modelConfig.setProjectId(model.getProjectId());
    modelConfig.setUserId(currentUserEmail);
    modelConfig.setConfigFile(wordclassFile);
    modelConfig.create();

    log.info("Added model configuration - {} - {} - {}", modelConfig.getId(), model.getName());
    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + modelConfig.getId()).build();
    return Response.created(locationURI)
            .entity(modelConfig)
            .build();
  }

  //
  // todo: check with Laurie about the 'jsonPatch' format
  //
  @Override
  public Response patchConfig(final String id, final PatchRequest jsonPatch) {
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    log.info("Patching model configuration data - {} - {}", id, currentUserEmail);
    ModelConfigBO originalConfig =
            this.configManager.getModelConfigDataById(id);

    if (originalConfig == null) {
      throw new javax.ws.rs.NotFoundException("Config with id  {" + id + "} not found");
    }

    ModelConfigBO patchedConfig = this.jsonConverter
            .patch(jsonPatch, originalConfig, ModelConfigBO.class);
    patchedConfig.setModifiedAt(System.currentTimeMillis());
    patchedConfig.setUserId(currentUserEmail);

    return Response.ok().entity(this.configManager.updateModelConfigById(id, patchedConfig))
            .build();
  }
}
