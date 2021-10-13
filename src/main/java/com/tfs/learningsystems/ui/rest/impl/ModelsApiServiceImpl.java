package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.*;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.ui.rest.ModelsApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;

import static com.tfs.learningsystems.util.Constants.TRAINING_OUTPUT_PURGE_TTL_IN_DAYS;

@Service
@Slf4j
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "20170814T23:59:55.88807:00")
public class ModelsApiServiceImpl extends ModelsApiService {

  @Autowired
  @Qualifier("modelManagerBean")
  private ModelManager modelManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  private JsonConverter jsonConverter;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Autowired
  @Qualifier("preferenceManagerBean")
  private PreferenceManager preferenceManager;

  @Autowired
  @Qualifier("vectorizerManagerBean")
  private VectorizerManager vectorizerManager;

  @Override
  public Response configureModel(String clientId, String projectId, ModelBO model,
                                 boolean trainNow, UriInfo uriInfo, String modelType, String modelTechnology, boolean toDefault)
      throws NotFoundException {
    try {
      validationManager.validateModelCreate(clientId, projectId, model);

      ProjectBO project = validationManager.validateClientAndProject(clientId, projectId);

      VectorizerBO vectorizerBO = vectorizerManager.getLatestVectorizerByTechnology(modelTechnology);

      String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      ModelBO modelBO = new ModelBO();
      int version = project.getModelVersion();
      version = version + 1;

      log.info("Configuring model version  {}  {}  {}", ActionContext.getClientId(),
          model.getId(), version);
      BeanUtils.copyProperties(modelBO, model);

      List<TFSModel> modelList = modelManager.listOfModelsForProject(clientId, projectId);

      if (modelBO.getConfigId() == null) {
        //Get last completed model config id
        // Sorting Model list based on version so we can pick up the latest version
        Collections.sort(modelList, new Comparator<TFSModel>() {
          public int compare(TFSModel o1, TFSModel o2) {
            return o2.getVersion() - o1.getVersion();
          }
        });
        // Try to go through the list of all successful built models and using the latest one
        for (TFSModel one : modelList) {
          TFSModelJobState jobState = modelManager.getModelStatus(clientId, one.getId());
          if (jobState.getStatus().getValue()
              .equalsIgnoreCase(TFSModelJobState.Status.COMPLETED.toString())) {
            // Setting the config id to last used successful  model id
            modelBO.setConfigId(Integer.valueOf(one.getConfigId()));
            break;
          }
        }
        /* If we cant find any successful model built for current project, use default config id */
        if (modelBO.getConfigId() == null) {
          ModelConfigBO defaultConfig = new ModelConfigBO();
          defaultConfig = defaultConfig
              .findOne(ModelConfigBO.FLD_NAME, Constants.DEFAULT_EN_CONFIG_NAME);
          modelBO.setConfigId(defaultConfig.getId());
        }
      }
      modelBO.setUserId(userEmail);
      modelBO.setVersion(version);
      modelBO.setProjectId(Integer.parseInt(projectId));
      modelBO.setVectorizer_type(vectorizerBO.getId());

      PreferencesBO preferencesBO = new PreferencesBO();
      PreferencesBO addedPreference = null;
      if(toDefault){
        try {
          preferencesBO = preferenceManager.getPreferenceByLevelTypeAndAttribute(clientId, Constants.PREFERENCE_MODEL_LEVEL, Constants.VECTORIZER_TYPE, projectId, false);
        } catch (InvalidRequestException e) {
          addedPreference = preferenceManager.addPreference(clientId, Constants.VECTORIZER_TYPE, projectId, vectorizerBO.getId(), Constants.PREFERENCE_MODEL_LEVEL, toDefault);
        }
        if (addedPreference == null) {
          PatchRequest patchRequest = new PatchRequest();
          PatchDocument patchDocumentForValue = new PatchDocument();
          patchDocumentForValue.setPath("/" + PreferencesBO.VALUE);
          patchDocumentForValue.setOp(PatchDocument.OpEnum.REPLACE);
          patchDocumentForValue.setValue(vectorizerBO.getId());
          patchRequest.add(patchDocumentForValue);
          preferenceManager.updatePreferences(clientId, preferencesBO.getId().toString(), patchRequest);
        }
      }

      if(modelBO.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL)
              && (modelBO.getDigitalHostedUrl() == null
              || modelBO.getDigitalHostedUrl().isEmpty())) {
        StringBuilder digitalUrl = new StringBuilder(appConfig.getOrionURL())
                .append(Constants.FORWARD_SLASH)
                .append(modelBO.getModelId())
                .append(Constants.FORWARD_SLASH)
                .append(Constants.DIGITAL_MODEL.toLowerCase());
        modelBO.setDigitalHostedUrl(digitalUrl.toString());
      }
      modelBO.create();

      if(modelBO.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL)) {
        ModelBO digitalModel = modelManager.getDigitalModelByModelId(modelBO.getModelId());
        if(digitalModel != null) {
          modelBO.setVectorizer_type(digitalModel.getVectorizer_type());
          modelBO.update();
          String speechModelId =  digitalModel.getSpeechModelId();
          if(speechModelId != null && !speechModelId.isEmpty()) {
            ModelBO speechModel = modelBO.findOne(speechModelId);
            if(speechModel != null){
              speechModel.delete();
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(ModelJobQueueBO.FLD_MODEL_ID, modelBO.getModelId());
            paramMap.put(Constants.MODEL_TYPE, Constants.DIGITAL_SPEECH_MODEL);

            ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
            modelJobQueueBO = modelJobQueueBO.findOne(paramMap);
            if(modelJobQueueBO != null){
              modelJobQueueBO.delete();
            }
          }
          digitalModel.setSpeechModelId(modelBO.getId().toString());
          digitalModel.update();
        }
      }

      if (trainNow) {
        modelManager.queueModelForBuilding(clientId, modelBO.getId().toString(), modelType, modelTechnology, vectorizerBO.getVersion());
      }
      modelBO.setStatus(TFSModelJobState.Status.QUEUED.getValue());

      UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
      URI locationURI = uriBuilder.path("" + modelBO.getId()).build();
      project.setModelVersion(version);
      project.update();
      return Response.created(locationURI).entity(modelBO).build();
    } catch (IllegalAccessException e) {
      log.error("Failed to config model ", e);
      throw new ServerErrorException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    } catch (InvocationTargetException e) {
      log.error("Failed to config model ", e);
      throw new ServerErrorException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }
  }

  @Override
  public Response downloadModel(String clientId, String projectId, String id)
      throws NotFoundException {
    try {
      File modelFile = modelManager.getModelFile(clientId, projectId, id);
      log.info("Downloading model  - {} - {} - {}", ActionContext.getClientId(), id,
          modelFile.getAbsolutePath());
      return Response.ok().entity(modelFile)
          .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
          .header("Content-Length", (int) modelFile.length())
          .header("Content-Disposition",
              String.format("attachment; filename=\"" + modelFile.getName() + "\""))
          .build();
    } catch (ApplicationException e) {
      log.error("Failed to download model ", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), e);
    } catch (Exception ex) {
      log.error("Failed to download model ", ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.MODEL_DOWNLOAD_MESSAGE))
          .build(), ex);
    }
  }

  @Override
  public Response downloadModelStats(String clientId, String projectId, String id)
          throws NotFoundException {
    try {
      File modelStatistics = modelManager.getModelStatistics(clientId, id);
      log.info("Downloading model statistics  {}  {}  {}", ActionContext.getClientId(), id,
              modelStatistics.getAbsolutePath());
      return Response.ok().entity(modelStatistics)
              .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
              .header("Content-Length", (int) modelStatistics.length())
              .header("Content-Disposition",
                      String.format("attachment; filename=\"" + modelStatistics.getName() + "\""))
              .build();
    } catch (ApplicationException e) {
      log.error("failed to download model stats", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.FILE_ACCESS_MESSAGE))
              .build(), e);
    } catch (WebApplicationException wex) {
      log.error("failed to download model stats", wex);
      throw wex;
    } catch (Exception ex) {
      log.error("failed to download model stats", ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.MODEL_DOWNLOAD_MESSAGE))
              .build(), ex);
    }
  }

  @Override
  public Response getModelBuildingStatus(String clientId, String projectId, String modelId,
      UriInfo uriInfo)
      throws NotFoundException {

    try {
      validationManager.validateAndGetModel(clientId, projectId, modelId, Constants.MODEL_DB_ID);
      TFSModelJobState response = modelManager.getModelStatus(clientId, modelId);
      if (response != null &&
          TFSModelJobState.Status.RUNNING != response.getStatus()) {
        // since the UI pull status constantly, logging RUNNING state will overwhelming logs
        log.info("Get model building status  {}  {}", modelId,
            response == null ? "" : response.getStatus());
      }
      return Response.ok().entity(response).build();
    } catch (ApplicationException e) {
      log.error("failed to get model statuss", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), e);
    }
  }


  @Override
  public Response downloadTrainingOutputs(final String clientId, String projectId,
      final String id) {
    try {
      ModelBO modelBO = validationManager.validateAndGetModel(clientId, projectId, id, Constants.MODEL_DB_ID);
      if (modelBO != null && modelBO.getCreatedAt() != null &&
              (Calendar.getInstance().getTimeInMillis() - modelBO.getCreatedAt() > TRAINING_OUTPUT_PURGE_TTL_IN_DAYS * 24 * 60 * 60 * 1000)) {
        log.error("Training output for modelId - {} -  was created {} days ago hence it is purged.", id, TRAINING_OUTPUT_PURGE_TTL_IN_DAYS);
        throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
                "training_output_purged",
                "Training output for modelId " + id + "  was created " + TRAINING_OUTPUT_PURGE_TTL_IN_DAYS + "days ago hence it is purged."));
      }
      File modelTrainingOutputs = modelManager.getModelTrainingOutputs(clientId, id);
      log.info("Downloading training outpus  {}  {}  {}", ActionContext.getClientId(), id,
          modelTrainingOutputs.getAbsolutePath());
      return Response.ok().entity(modelTrainingOutputs)
          .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
          .header("Content-Length", (int) modelTrainingOutputs.length())
          .header("Content-Disposition",
              String.format("attachment; filename=\"" + modelTrainingOutputs.getName() + "\""))
          .build();
    } catch (ApplicationException e) {
      log.error("failed to download training output", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build(), e);
    } catch (WebApplicationException wex) {
      log.error("failed to download training output", wex);
      throw wex;
    } catch (Exception ex) {
      log.error("failed to download training output", ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.MODEL_DOWNLOAD_MESSAGE))
          .build());
    }
  }

  @Override
  public Response deleteModelById(String clientId, String projectId, String modelId) {
    try {
      return modelManager.deleteModel(clientId, projectId, modelId);
    } catch (Exception e) {
      log.error("failed to delete model ", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              e.getMessage()))
          .build(), e);
    }
  }

  @Override
  public Response updateModelConfig(String clientId, String projectId, String id, PatchRequest jsonPatch)
          throws NotFoundException {
    ModelBO modifiedModel = modelManager
            .updateModel(clientId, projectId, id, jsonPatch);
    return Response.ok(modifiedModel).build();
  }

  @Override
  public Response updateCombinedModelConfig(String clientId, String projectId, String modelId, String digitalHostedUrl)
          throws NotFoundException {
    try {
      String combinedModel = modelManager
              .updateCombinedModel(clientId, projectId, modelId, digitalHostedUrl);
      return Response.ok(combinedModel).build();
    } catch (Exception e) {
      log.error("failed to combine model", e);
      throw new ServerErrorException(Response.status(Status.BAD_REQUEST)
              .entity(new Error(Status.BAD_REQUEST.getStatusCode(), null,
                      e.getMessage()))
              .build());
    }
  }

  @Override
  public Response queueModelForBuilding(String clientId, String id, UriInfo uriInfo, String modelType)
      throws NotFoundException {

    String token;

    ModelBO modelBO = new ModelBO();
    modelBO = modelBO.findOne(id);

    VectorizerBO vectorizer = vectorizerManager.getVectorizerById(modelBO.getVectorizer_type().toString());

    try {

      token = modelManager.queueModelForBuilding(clientId, id, modelType, vectorizer.getType(), vectorizer.getVersion());
      log.info("Queuing model for build  {}  {}  {}", ActionContext.getClientId(), id, token);

      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      URI locationURI = uriBuilder.path("" + token).build();
      return Response.accepted().location(locationURI).build();
    } catch (ApplicationException e) {
      log.error("failed to queue model for building", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }
  }

  @Override
  public Response getModelsForProject(String clientId, String projectId)
      throws NotFoundException {
    try {
      validationManager.validateClientAndProject(clientId, projectId);
      return Response.ok().entity(modelManager.listOfModelsForProject(clientId, projectId)).build();
    } catch (ApplicationException ex) {
      log.error("failed to list models for project  " + projectId, ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))

          .build());
    }
  }


  @Override
  public Response getModelById(String clientId, String projectId, String id)
      throws NotFoundException {
    try {
      validationManager.validateAndGetModel(clientId, projectId, id, Constants.MODEL_DB_ID);
      return Response.ok().entity(modelManager.getModel(clientId, projectId, id)).build();
    } catch (ApplicationException ex) {
      log.error("failed to list models for project  " + projectId, ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }
  }


  @Override
  public Response getTrainingDataForModel(String clientId, String projectId, String modelDbId)
      throws NotFoundException {
    try {
      validationManager.validateAndGetModel(clientId, projectId, modelDbId, Constants.MODEL_DB_ID);
      return Response.ok().entity(modelManager.getModelTrainingData(clientId, projectId, modelDbId))
          .build();
    } catch (ApplicationException ex) {
      log.error("failed to list models for project  " + projectId, ex);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE)).build());
    }
  }
}