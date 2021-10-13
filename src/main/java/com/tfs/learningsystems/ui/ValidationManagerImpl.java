package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.ui.dao.MwbItsClientMapDao;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.DatasetField;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.ui.model.ProjectField;
import com.tfs.learningsystems.ui.model.GlobalModelName;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.DatasetDetailsModel;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.ui.nlmodel.model.dao.ModelDao;
import com.tfs.learningsystems.util.CommonUtils;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;


@Component
@Qualifier("validationManagerBean")
@Slf4j
public class ValidationManagerImpl implements ValidationManager {

  @Autowired
  @Qualifier("modelDaoBean")
  private ModelDao modelDao;

  @Autowired
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  @Qualifier("mwbItsDaoBean")
  private MwbItsClientMapDao mwbItsClientMapDao;


  @Override
  public ProjectBO validateProjectId(String projectId) {

    ProjectBO project = new ProjectBO();
    project = project.findOne(projectId);
    if (project == null) {
      Error error = ErrorUtil
              .notFoundError(Response.Status.NOT_FOUND.getStatusCode(), Constants.PROJECT_LABEL, projectId);
      throw new NotFoundException(error);
    }

    return project;
  }

  @Override
  public ClientBO validateClient(String clientId) {
    ClientBO client = clientManager.getClientById(clientId);

    if (client == null || client.getId() == null || client.isDisabled()) {
      log.error("Failed to find the related client while validating clientId - : {}  ", clientId);

      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "client_not_found",
                      ErrorMessage.CLIENT_NOT_FOUND));
    }
    return client;
  }

  @Override
  public boolean validateUser(String source) {

    Map<String, Object> detailsMap = CommonUtils.getUserAuthDetailsMap();
    Map<String, String> userGroups = CommonUtils.getUserGroups();

    Error error = new Error();
    error.setCode(Response.Status.FORBIDDEN.getStatusCode());
    error.setMessage(ErrorMessage.SOURCE_TYPE_ERROR);

    String userId = null;

    if(detailsMap != null && !detailsMap.isEmpty()) {
      userId = (String) detailsMap.get(Constants.USER_SUB);
    }

    if (userId == null && userGroups != null && userGroups.containsKey(Constants.MWB_ROLE_CLIENTADMIN)) {
      if(source.equals(DatasetBO.Source.A.getValue())){
        throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
      }
    } else if(CommonUtils.isUserExternalType()) {
      if(!source.equals(DatasetBO.Source.E.getValue())) {
        throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
      }
    } else if(source.equals(DatasetBO.Source.E.getValue())) {
      throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
    }
    return true;
  }


  @Override
  public ProjectBO validateProject(String clientId, String projectId) {

    ProjectBO project = new ProjectBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditions.put(ProjectBO.FLD_PROJECT_ID, projectId);
    conditions.put(ProjectBO.FLD_STATE, ProjectBO.State.ENABLED.toString());

    project = project.findOne(conditions);
    if (project == null || project.getId() == null) {

      log.error("Failed to find the related project for client - {} - {} ", projectId, clientId);

      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("model_not_found");
      error.setMessage("Model not found");
      throw new NotFoundException(error);
    }

    return project;
  }

  @Override
  public ProjectBO validateClientAndProject(String clientId, String projectId) {

    this.validateClient(clientId);
    return this.validateProject(clientId, projectId);

  }

  @Override
  public void validateGlobalProjectName(String projectType, String projectName) {
    GlobalModelName modelName = GlobalModelName.lookup(projectName);
    if(projectType.equals(ProjectBO.Type.GLOBAL.toString())) {
      if(modelName == null){
        log.error("Model name - {} is not allowed for global models", projectName);
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setMessage("Model name provided is not allowed for global models");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    } else {
      if(modelName != null) {
        log.error("Model name - {} is not allowed for node level models", projectName);
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setMessage("Global model names are not allowed for node level models");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    }
  }

  @Override
  public DatasetBO validateClientAndDataset(String clientId, String datasetId) {

    DatasetBO dataset = new DatasetBO();

    Map<String, Object> dataSetConditions = new HashMap<>();

    dataSetConditions.put(DatasetBO.FLD_CLIENT_ID, clientId);
    dataSetConditions.put(DatasetBO.FLD_DATASET_ID, datasetId);

    dataset = dataset.findOne(dataSetConditions);

    if (dataset == null || dataset.getId() == null) {

      log.error(Constants.DATASET_NOT_FOUND_FOR_CLIENT, clientId,
              datasetId);

      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode(Constants.DATASET_NOT_FOUND);
      error.setMessage("datasetId '" + datasetId + "' not found");
      throw new NotFoundException(error);
    }

    return dataset;
  }

  @Override
  public void validateDatasetIds(List<String> datasetIds) {
    if(datasetIds == null || datasetIds.isEmpty()) {
      log.error(ErrorMessage.DATASET_ID_MISSING);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "datasetId_not_found",
              ErrorMessage.DATASET_ID_MISSING));
    }
  }

  @Override
  public Boolean validateProjectDatasetEntry(String projectId, String datasetId, Boolean modelBuild) {
    // modelBuild parameter added
    ProjectBO project = this.validateProjectAndStart(projectId);

    DatasetBO dataset = this.validateClientAndDataset(project.getClientId().toString(), datasetId);

    if(CommonUtils.isUserExternalType() && !dataset.getSource().equals(DatasetBO.Source.E.getValue()) && !modelBuild) {
      Error error = new Error();
      error.setCode(Response.Status.FORBIDDEN.getStatusCode());
      error.setMessage(ErrorMessage.DATASET_VIEW_NOT_ALLOWED);
      throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
    }

    this.validateProjectDataset(projectId, datasetId);

    return true;
  }

  public void validateProjectDataset(String projectId, String datasetId) {

    if(Boolean.FALSE.equals(this.ifValidClientProjectDatasetEntry(projectId, datasetId))) {
      log.error("Failed to find the related  dataset for  project - {} , dataset - {} ", projectId,
              datasetId);
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode(Constants.DATASET_NOT_FOUND);
      error.setMessage(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED);
      throw new NotFoundException(error);
    }
  }

  public List<DatasetBO> validateProjectTransformedStatus(String projectId) {
    List<DatasetBO> datasetList = projectManager.hasTransformedDataset(projectId);
    if (datasetList.isEmpty()) {

      log.error("Failed to find the transformed dataset for model - {} : ", projectId);
      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("model_not_transformed");
      error.setMessage(ErrorMessage.PROJECT_NOT_TRANSFORMED);
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
    return datasetList;
  }

  @Override
  public DatasetDetailsModel validateClientProjectDataSet(String clientId, String projectId,
                                                          String datasetId) {

    ClientBO client = validateClient(clientId);

    ProjectBO project = this.validateProject(clientId, projectId);

    DatasetBO dataset = this.validateClientAndDataset(clientId, datasetId);

    this.validateProjectDataset(projectId, datasetId);

    DatasetDetailsModel datasetDetailsModel = new DatasetDetailsModel();
    datasetDetailsModel.setClientBO(client);
    datasetDetailsModel.setProjectBO(project);
    datasetDetailsModel.setDatasetBO(dataset);

    return datasetDetailsModel;


  }

  @Override
  public Boolean ifValidClientProjectDatasetEntry(String projectId, String datasetId) {

    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ProjectDatasetBO.FLD_PROJECT_ID, projectId);
    paramMap.put(ProjectDatasetBO.FLD_DATASET_ID, datasetId);
    projectDataset = projectDataset.findOne(paramMap);

    return projectDataset != null;

  }

  @Override
  public void ifProjectNameChangePatchRequest(String clientId, String projectType, PatchRequest patchRequest) {

    boolean isValid = false;
    String projectName = null;
    for (PatchDocument patchDocument : patchRequest) {
      if (patchDocument.getPath().indexOf(Constants.PROJECT_NAME, 1) > -1) {
        isValid = true;
        projectName = patchDocument.getValue().toString();
      }
    }
    if (isValid) {
      this.validateProjectNameExists(clientId, projectName);
      this.validateGlobalProjectName(projectType, projectName);
    }
  }

  @Override
  public void validatePatchCall(PatchRequest patchRequest, String flag) {

    boolean isTrue = false;
    for (PatchDocument patchDocument : patchRequest) {
      if(Constants.PROJECT_FIELDS.equals(flag)) {
        if (ProjectField.lookup(patchDocument.getPath()) == null) {
          isTrue = true;
        }
      } else {
        if (DatasetField.lookup(patchDocument.getPath()) == null) {
          isTrue = true;
        }
      }
      if(isTrue) {
        log.error("Failed to update. Operation not allowed.");

        Error error = new Error();
        error.setCode(Response.Status.FORBIDDEN.getStatusCode());
        error.setErrorCode("Not_allowed_to_update");
        error.setMessage("Not allowed to update '" + patchDocument.getPath());
        throw new ForbiddenException(
                Response.status(Response.Status.FORBIDDEN).entity(error).build());
      }
    }
  }

  @Override
  public void validateProjectNameExists(String clientId, String projectName) {

    ProjectBO projectOnName = new ProjectBO();
    ProjectBO projectOnOriginalName = new ProjectBO();
    Map<String, Object> conditionsOnName = new HashMap<>();
    Map<String, Object> conditionsOnOriginalName = new HashMap<>();
    conditionsOnName.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditionsOnName.put(ProjectBO.FLD_PROJECT_NAME, projectName);
    conditionsOnName.put(ProjectBO.FLD_STATE, ProjectBO.State.ENABLED.toString());
    conditionsOnOriginalName.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditionsOnOriginalName.put(ProjectBO.FLD_ORIGINAL_NAME, projectName);
    conditionsOnOriginalName.put(ProjectBO.FLD_STATE, ProjectBO.State.ENABLED.toString());
    projectOnName = projectOnName.findOne(conditionsOnName);
    projectOnOriginalName = projectOnOriginalName.findOne(conditionsOnOriginalName);
    if ((projectOnName != null && projectOnName.getId() != null) || (projectOnOriginalName != null && projectOnOriginalName.getId() != null)) {
      log.error("Project name - {} - already exist for clientId - {}.", projectName, clientId);
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "modelname_already_exists",
              "Model name '" + projectName + "' already exist"));
    }
  }

  @Override
  public ProjectBO validateProjectCreate(String clientId, ProjectBO project) {

    this.validateClient(clientId);
    if (project == null || project.getClientId() == null || !Integer.toString(project.getClientId())
            .equals(clientId)) {
      if (project == null) {
        log.error("Project details are null for client - : {} ,", clientId);
        throw new BadRequestException("Invalid  model details for client : " + clientId);

      } else {
        log.error("Project details provided are incorrect project client and clientId selected are - : {} , {} ",
                project.getClientId(), clientId);
        throw new BadRequestException("Invalid  model details for client : " + clientId + " and project client " + project
                        .getClientId());
      }
    }
    return project;
  }

  @Override
  public ProjectBO validateProjectAndStart(String projectId) {

    ProjectBO project = new ProjectBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ProjectBO.FLD_PROJECT_ID, projectId);
    project = project.findOne(conditions);

    if (project == null || project.getId() == null ) {
      log.error("Failed to find the related project  project - {} ", projectId);
      Error error = ErrorUtil
              .notFoundError(Response.Status.NOT_FOUND.getStatusCode(), Constants.PROJECT_LABEL, projectId);
      throw new NotFoundException(error);
    } else {
      if (project.getClientId() == null) {
        log.error(" Could not find client details on the project - : {}  ",
                project.getId());
        throw new BadRequestException("Bad request");
      }
      this.validateClient(project.getClientId().toString());
      if (project.getStartAt() == null || project.getStartAt() == 0) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        project.setStartAt(timeInMillis);
        project.update();
      }
    }
    return project;
  }

  @Override
  public ModelBO validateModelCreate(String clientId, String projectId, ModelBO model) {

    this.validateClientAndProject(clientId, projectId);
    if (model == null) {
      log.error("Failed to find the model for client  - {} ", clientId);
      throw new BadRequestException("Invalid  model version details for client : " + clientId);
    }
    if (model.getCid() == null) {
      log.error(" Could not find client details on the model - : {}  ", model.getId());
      throw new BadRequestException("Bad request");
    }
    return model;
  }


  @Override
  public ModelBO validateAndGetModel(String clientId, String projectId, String id, String typeOfId)
          throws NotFoundException {

    ModelBO modelBO = new ModelBO();

    if (typeOfId.equalsIgnoreCase("id")) {
      modelBO = modelBO.findOne(id);
      if (StringUtils.isNotEmpty(projectId) && StringUtils.isNotEmpty(clientId)) {
        if (modelBO != null && modelBO.getProjectId().toString().equals(projectId)) {
          this.validateClientAndProject(clientId, projectId);
        } else {
          log.error("Project Id {} details are incorrect for model Id - : {} ,", projectId,
                  id);
          Error error = ErrorUtil
                  .notFoundError(Response.Status.NOT_FOUND.getStatusCode(), "project", projectId);
          throw new NotFoundException(error);
        }
      }
    } else if (typeOfId.equalsIgnoreCase("modelId")) {

      Map<String, Object> conditions = new HashMap<>();
      //TODO: https://247inc.atlassian.net/browse/NT-2745
      //TODO: https://247inc.atlassian.net/browse/NT-2746
      conditions.put(ModelBO.FLD_MODEL_ID, id);
      if (StringUtils.isNotEmpty(projectId)) {
        conditions.put(ModelBO.FLD_PROJECT_ID, projectId);
      }
      modelBO = modelBO.findOne(conditions);

      if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(projectId)) {
        this.validateClientAndProject(clientId, projectId);
      }
    }

    if (modelBO == null) {
      Error error = ErrorUtil.notFoundError(Response.Status.NOT_FOUND.getStatusCode(), "model", id);
      throw new NotFoundException(error);
    }

    return modelBO;
  }

  @Override
  public List<ModelBO> validateAndGetDeployableModelsAndProjects(String clientId, List<ProjectModelRequest> projectModels, List<String> invalidModelIds,
                                                                 Map<Integer, String> validProjects, Map<Integer, Integer> validProjectModels,
                                                                 StringBuilder modelsSummary, String typeOfId) {

    List<ModelBO> models = new ArrayList<>();
    ModelBO modelBO;
    Map<String, Object> modelParamMap;
    ProjectBO projectBO;

    boolean ifNotFirstEntry = false;

    for (ProjectModelRequest projectModel : projectModels) {
      modelBO = new ModelBO();
      modelParamMap = new HashMap<>();
      TFSModelJobState modelCurrentState;
      if (typeOfId.equalsIgnoreCase(Constants.MODEL_DB_ID)) {
        modelParamMap.put(Constants.MODEL_DB_ID, projectModel.getModelId());
      } else if (typeOfId.equalsIgnoreCase(Constants.MODEL_UUID_ID)) {
        modelParamMap.put(ModelBO.FLD_MODEL_ID, projectModel.getModelId());
      } else if (typeOfId.equalsIgnoreCase(Constants.MODEL_VERSION)) {
        modelParamMap.put(ModelBO.FLD_MODEL_VERSION, projectModel.getModelId());
      }

      modelParamMap.put(ModelBO.FLD_PROJECT_ID, projectModel.getProjectId());
      modelBO = modelBO.findOne(modelParamMap);

      if (modelBO == null) {
        invalidModelIds.add(projectModel.getModelId());
        continue;
      } else {
        modelCurrentState = modelDao.getModelJobStatus(modelBO.getModelId(), modelBO.getModelType());
        if (!modelCurrentState.getStatus().equals(TFSModelJobState.Status.COMPLETED)) {
          if (typeOfId.equalsIgnoreCase(Constants.MODEL_DB_ID)) {
            invalidModelIds.add(modelBO.getId().toString());
          } else {
            invalidModelIds.add(modelBO.getModelId());
          }
          continue;
        }
      }

      projectBO = this.validateClientAndProject(clientId, projectModel.getProjectId());
      if (projectBO == null) {
        invalidModelIds.add(projectModel.getModelId());
        continue;
      } else {
        validProjects.put(projectBO.getId(), projectBO.getName());
        if (ifNotFirstEntry) {
          modelsSummary
                  .append(System.getProperty(Constants.LINE_SEPARATOR));
        } else {
          ifNotFirstEntry = true;
        }
        modelsSummary.append(Constants.SPACE).append(Constants.PROJECT_NAME).append(projectBO.getName())
                .append(Constants.SPACE).append(Constants.COMMA).append(Constants.SPACE)
                .append(ModelBO.FLD_MODEL_ID).append(Constants.SPACE).append(Constants.COLON)
                .append(Constants.SPACE).append(modelBO.getModelId()).append(Constants.SPACE)
                .append(Constants.COMMA).append(Constants.SPACE).append(ModelBO.FLD_ID)
                .append(Constants.SPACE).append(Constants.COLON).append(Constants.SPACE)
                .append(modelBO.getId()).append(Constants.SPACE).append(Constants.COMMA)
                .append(Constants.SPACE).append(ModelBO.FLD_MODEL_VERSION).append(Constants.SPACE)
                .append(Constants.COLON).append(Constants.SPACE).append(modelBO.getVersion())
                .append(Constants.SPACE);
        validProjectModels.put(projectBO.getId(), modelBO.getId());
        models.add(modelBO);
      }
    }
    return models;
  }

  @Override
  public Boolean validateClientCreate(Client client) {

    MwbItsClientMapBO mwbItsClientMapBO = mwbItsClientMapDao.getClientByClientAppAccount(client.getItsClientId(),
            client.getItsAppId(), client.getItsAccountId());
    if (mwbItsClientMapBO != null) {
      log.error(Constants.INVALID_CLIENT + client.getItsClientId() + "  app " + client.getItsAppId()
              + "  account " + client.getItsAccountId());
      throw new BadRequestException(Constants.INVALID_CLIENT + client.getItsClientId() + "  app " + client
              .getItsAppId() + "  account " + client.getItsAccountId());
    }
    return true;
  }

  @Override
  public void validateClientAppAccUpdate(String clientId, String accountId, String appId,
                                         MwbItsClientMapBO mwbItsClientMapBO) {

    MwbItsClientMapBO loadedMwbItsClientMapBO = new MwbItsClientMapBO();

    Map<String, Object> conditions = new HashMap<>();
    conditions.put(MwbItsClientMapBO.FLD_ITS_CLIENT_ID, mwbItsClientMapBO.getItsClientId());
    if (accountId != null) {
      conditions.put(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID, mwbItsClientMapBO.getItsAccountId());
    }
    if (appId != null) {
      conditions.put(MwbItsClientMapBO.FLD_ITS_APP_ID, mwbItsClientMapBO.getItsAccountId());
    }

    loadedMwbItsClientMapBO = loadedMwbItsClientMapBO.findOne(conditions);

    if (loadedMwbItsClientMapBO != null && loadedMwbItsClientMapBO.getId() != mwbItsClientMapBO
            .getId()) {
      log.error(
              Constants.INVALID_CLIENT + clientId + "  appId " + appId
                      + "  account " + accountId);

      throw new BadRequestException(
              Constants.INVALID_CLIENT + clientId + "  appId " + appId
                      + "  account " + accountId);
    }

  }

  @Override
  public Boolean validateRolesByClientId(String clientId) {
    boolean isUserValid = false;
    String userId = CommonUtils.getUserId();
    Map<String, String> userGroups = CommonUtils.getUserGroups();
    if (userId == null && userGroups != null && userGroups.containsKey(Constants.MWB_ROLE_CLIENTADMIN)) {
      return true;
    } else if(userId != null && clientId != null) {
      Map<String, ClientDetail> clients = clientManager.getClientDetailsMapByUserId(userId);
      MwbItsClientMapBO mwbItsClientMapBO = clientManager.getITSClientByClientId(clientId);
      String key = (mwbItsClientMapBO.getItsClientId() + "_" + mwbItsClientMapBO.getItsAppId()).toUpperCase();
      List<String> roles = new ArrayList<>();
      if(clients.get(key) != null) {
        roles =  clients.get(key).getRoles();
      }
      for(String role: roles) {
        isUserValid = isUserValid || role.equals(Constants.ADMIN_ROLE) || role.equals(Constants.OPERATOR_ROLE);
      }
    }
    return isUserValid;
  }

  @Override
  public void validateProjectIdForModel(String clientId, PatchRequest patchRequest, String projectId) {

    for (PatchDocument patchDocument: patchRequest) {
      if (patchDocument.getPath().indexOf(Constants.DEPLOYABLE_MODEL_ID, 1) > -1) {
        Integer modelId = (Integer) patchDocument.getValue();
        if (modelId == null) {
          log.error("Unmark for deploy - {} on projectId {} .", modelId, projectId);
          return;
        }

        ModelBO model = new ModelBO();
        Map<String, Object> conditions = new HashMap<>();
        conditions.put(Constants.DB_COLUMN_ID, modelId);
        model = model.findOne(conditions);
        if (model == null) {
          log.error("ModelId - {} - does not exist.", modelId);
          throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
                  "modelId_not_exist",
                  "ModelId " + modelId + " does not exist"));
        }
        if (!model.getProjectId().equals(Integer.valueOf(projectId))) {
          log.error("ModelId - {} -  is not associated with projectId - {}.", modelId, projectId);
          throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
                  "modelId_projectId_mismatch",
                  "ModelId " + modelId + " is not associated with projectId  " + projectId));
        }
        if(model.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL)) {
          log.error("Can not mark for deploy on speech model.");
          throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
                  "modelId_not_allowed_for_mark_for_deploy",
                  "Can not mark for deploy on speech model with modelId: " + modelId + " with projectId  " + projectId));
        }
        if(Boolean.FALSE.equals(this.validateRolesByClientId(clientId))) {
          log.error("User is not allowed to perform mark for deploy operation.");
          Error error = new Error();
          error.setCode(Response.Status.FORBIDDEN.getStatusCode());
          error.setMessage(ErrorMessage.USER_NOT_AUTHORISED);
          throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
        }
      }
    }
  }

  private void modelStatusValidation(PatchDocument patchDocument, String projectId) {
    String modelId = patchDocument.getValue() != null ? patchDocument.getValue().toString() : null;
    if (modelId == null) {
      return;
    }
    if(modelId.equals("")){
      log.error("ModelId - {} - does not exist.", modelId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "modelId_not_exist",
              "ModelId can not be empty"));
    }
    ModelBO model = new ModelBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(Constants.DB_COLUMN_ID, modelId);
    model = model.findOne(conditions);

    if (model == null || !model.getModelType().equals(Constants.DIGITAL_MODEL)) {
      log.error("Invalid digital modelId {} - provided.", modelId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "invalid_modelId_provided",
              "Invalid digital modelId" + modelId + " provided"));
    }

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ModelJobQueueBO.FLD_MODEL_ID, model.getModelId());
    paramMap.put(Constants.MODEL_TYPE, Constants.DIGITAL_MODEL);
    ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
    modelJobQueueBO = modelJobQueueBO.findOne(paramMap);

    if(modelJobQueueBO == null || !modelJobQueueBO.getStatus().equals(ModelJobQueueBO.Status.COMPLETED.toString())) {
      log.error("Invalid modelId {} - provided.", modelId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "invalid_modelId_provided",
              "Invalid modelId" + modelId + " provided"));
    }

    if (!model.getProjectId().equals(Integer.valueOf(projectId))) {
      log.error("ModelId - {} -  is not associated with projectId - {}.", modelId, projectId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "modelId_projectId_mismatch",
              "ModelId " + modelId + " is not associated with projectId  " + projectId));
    }
  }

  @Override
  public Boolean validateLiveAndPreviewModelId(String clientId, PatchRequest patchRequest, String projectId) {
    boolean isLiveModelIdUpdate = false;
    for (PatchDocument patchDocument: patchRequest) {
      if (patchDocument.getPath().indexOf(Constants.PREVIEW_MODEL_ID, 1) > -1
              || patchDocument.getPath().indexOf(Constants.LIVE_MODEL_ID, 1) > -1) {
        if (Boolean.TRUE.equals(CommonUtils.isCBUser())) {
          this.modelStatusValidation(patchDocument, projectId);
          if(patchDocument.getPath().indexOf(Constants.LIVE_MODEL_ID, 1) > -1) {
            isLiveModelIdUpdate = true;
          }
        } else {
          log.error(ErrorMessage.USER_NOT_AUTHORISED);
          Error error = new Error();
          error.setCode(Response.Status.FORBIDDEN.getStatusCode());
          error.setMessage(ErrorMessage.USER_NOT_AUTHORISED);
          throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN).entity(error).build());
        }
      }
    }
    return isLiveModelIdUpdate;
  }

  @Override
  public void validatePreviewAndLiveModel(String clientId, String projectId, String modelId){
    ProjectBO project = new ProjectBO();
    Map < String, Object > conditions = new HashMap < > ();
    conditions.put(Constants.DB_COLUMN_ID, projectId);
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    project = project.findOne(conditions);
    if (project == null) {
      String code = "projectid_not_found";
      String message = "projectId - " + projectId + " - for clientId -" + clientId + " - not found.";
      log.error(message);
      ErrorUtil.throwInvalidRequestException(code, message);
    } else if (project.getPreviewModelId() != null && project.getPreviewModelId().equals(modelId)) {
      String code = "model_preview_state";
      String message = "ModelId " + modelId + " is currently in preview state for projectId "+ projectId +  " and can not be deleted.";
      log.error(message);
      ErrorUtil.throwInvalidRequestException(code, message);
    } else if (project.getLiveModelId() != null && project.getLiveModelId().equals(modelId)) {
      String code = "model_live_state";
      String message = "ModelId " + modelId + " is currently in live state for projectId "+ projectId +  " and can not be deleted.";
      log.error(message);
      ErrorUtil.throwInvalidRequestException(code, message);
    }
  }

  @Override
  public void validateDeployableModel(String clientId, String projectId, String modelId) {
    ProjectBO project = new ProjectBO();
    Map < String, Object > conditions = new HashMap < > ();
    conditions.put(Constants.DB_COLUMN_ID, projectId);
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    project = project.findOne(conditions);
    if (project == null) {
      log.error("projectId - {} - for clientId - {} -  not found.",projectId,clientId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "projectid_not_found",
              "projectId - " + projectId + " - for clientId -" + clientId + " - not found."));
    }
    else if (project.getDeployableModelId() != null && Integer.valueOf(modelId).equals(project.getDeployableModelId())) {
      log.error("ModelId - {} -  is currently marked as deployable for projectId  - {} - and can not be deleted.", modelId, projectId);
      throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              "modelId_marked_deployable",
              "ModelId " + modelId + " is currently marked as deployable for projectId "+ projectId +  "and can not be deleted."));
    }
  }

  @Override
  public VectorizerBO validateVectorizer(String id) {
    // validate vectorizer record
    VectorizerBO vectorizerBO = new VectorizerBO();
    vectorizerBO = vectorizerBO.findOne(id);
    if (vectorizerBO == null || vectorizerBO.getId() == null) {
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "vectorizer_not_found",
                      ErrorMessage.VECTORIZER_NOT_FOUND));
    }

    return vectorizerBO;
  }

  public PreferencesBO validatePreference(String clientId, String id) {
    // validate preference record for the given client id and preference id
    PreferencesBO preferencesBO = new PreferencesBO();
    Map<String, Object> conditions = new HashMap<>();

    conditions.put(PreferencesBO.OBJ_ID, id);
    conditions.put(PreferencesBO.FLD_CLIENT_ID, clientId);
    // conditions.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);
    // Once we implement delete preferences we can uncomment above line
    preferencesBO = preferencesBO.findOne(conditions);
    if (preferencesBO == null || preferencesBO.getId() == null) {
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "preference_not_found",
                      ErrorMessage.PREFERENCE_NOT_FOUND));
    }

    // clientId check for preference
    if (preferencesBO != null && preferencesBO.getId() != null && String.valueOf(preferencesBO.getClient_id()).equals(clientId)) {
      this.validateClient(clientId);
    } else {
      log.error("Client Id details are not found and incorrect for preference Id ,", clientId,
              id);
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "client_not_found",
                      ErrorMessage.CLIENT_NOT_FOUND));
    }

    return preferencesBO;
  }

  public void validateLevelTypeAndAttribute(String level, String attribute, String type, String clientId) {

    // check if record already exists for the given data
    PreferencesBO preferencesBO = new PreferencesBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(PreferencesBO.FLD_LEVEL, level);
    conditions.put(PreferencesBO.FLD_ATTRIBUTE, attribute);
    conditions.put(PreferencesBO.FLD_TYPE, type);
    conditions.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);
    preferencesBO = preferencesBO.findOne(conditions);

    if (preferencesBO != null && preferencesBO.getId() != null ) {
      log.error("Preference for Client/Model already exists :", level, attribute);
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "Record already exists for level attribute and type",
              "Record already exists for level attribute and type"));
    }

    // check clientid attribute match for clientlevel preference

    if (level.equals(Constants.PREFERENCE_CLIENT_LEVEL)) {
      if (!clientId.equals(attribute)) {
        log.error("Failed to relate clientId and attribute : {}  ", attribute);

        throw new InvalidRequestException(
                new Error(Response.Status.BAD_REQUEST.getStatusCode(), "Failed to relate clientId and attribute",
                        "Failed to relate clientId and attribute"));
      }
      this.validateClient(attribute);
    }

    // check for model existence already
    if (level.equals(Constants.PREFERENCE_MODEL_LEVEL)) {
      ProjectBO project = projectManager.getProjectById(clientId, attribute);
      if ((project != null) && (project.getState().equals(PreferencesBO.STATUS_ENABLED))) {
        return;
      }
      log.error("Failed to find the related project : {}  ", attribute);

      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), "project_not_found",
                      ErrorMessage.PROJECT_NOT_FOUND));
    }
  }

  public void PreferencePatchRequest(String clientId, PreferencesBO preferencesBO, PatchRequest patchRequest) {

    String type = preferencesBO.getType();
    String value = preferencesBO.getValue().toString();

    // check to prevent existing preference level updation
    for (PatchDocument patchDocument : patchRequest) {
      if (patchDocument.getPath().indexOf(PreferencesBO.FLD_LEVEL, 1) > -1) {
        log.error("updating level is not allowed");
        throw new ForbiddenException("updating level is not allowed");
      }
      if (patchDocument.getPath().indexOf(PreferencesBO.FLD_CLIENT_ID, 1) > -1) {
        log.error("updating clientId is not allowed");
        throw new ForbiddenException("updating clientId is not allowed");
      }
      if (patchDocument.getPath().indexOf(PreferencesBO.FLD_ATTRIBUTE, 1) > -1) {
        log.error("updating attribute is not allowed");
        throw new ForbiddenException("updating attribute is not allowed");
      }
      if (patchDocument.getPath().indexOf(PreferencesBO.VALUE, 1) > -1) {
        value = patchDocument.getValue().toString();
      }
    }
    for (PatchDocument patchDocument : patchRequest) {
      if (patchDocument.getPath().indexOf(PreferencesBO.FLD_TYPE, 1) > -1) {
        type = patchDocument.getValue().toString();
        PreferencesBO preferencesBO1 = new PreferencesBO();
        Map<String, Object> conditions = new HashMap<>();
        conditions.put(PreferencesBO.FLD_LEVEL, preferencesBO.getLevel());
        conditions.put(PreferencesBO.FLD_ATTRIBUTE, preferencesBO.getAttribute());
        conditions.put(PreferencesBO.FLD_TYPE, type);
        conditions.put(PreferencesBO.FLD_STATUS, PreferencesBO.STATUS_ENABLED);
        preferencesBO1 = preferencesBO1.findOne(conditions);
        if ((preferencesBO1 != null) && (preferencesBO1.getId() != null)) {
          log.error("Preference for level, type, attribute already exists ");
          throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
                  "preference record already exists for level attribute and type",
                  "preference record already exists for level attribute and type"));
        }
      }
    }
    if (type.equals(Constants.VECTORIZER_TYPE)) {
      this.validateVectorizer(value);
    }
  }

  public Boolean validateLevel(String level, Boolean setDefault) {

    // check to use latest input values or the existing client level values
    if ((level.equals(Constants.PREFERENCE_MODEL_LEVEL) && setDefault) || (level.equals(Constants.PREFERENCE_CLIENT_LEVEL))) {
      return true;
    }
    return false;
  }

  public ModelDeploymentDetailsBO validateProjectAndModelId(String projectId) {
    ModelDeploymentDetailsBO modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();
    ProjectBO projectBO = new ProjectBO();
    Map<String, Object> params = new HashMap<>();
    params.put(ProjectBO.FLD_PROJECT_ID, projectId);
    projectBO = projectBO.findOne(params);
    if (projectBO.getLiveModelId() != null) {
      String modelId = projectBO.getLiveModelId();
      Map<String, Object> conditions = new HashMap<>();
      ModelDeploymentMapBO modelDeploymentMapBO = new ModelDeploymentMapBO();
      conditions.put(ModelDeploymentMapBO.FLD_PROJECT_ID, projectId);
      conditions.put(ModelDeploymentMapBO.FLD_MODEL_ID, modelId);
      List<ModelDeploymentMapBO> modelDeploymentMapList = modelDeploymentMapBO.list(conditions, null);
      if (modelDeploymentMapList == null || modelDeploymentMapList.size() == 0) {
        log.error("Model Deployment map details not found for project id {} and model id {}", projectId, modelId);
      } else {
        List<ModelDeploymentDetailsBO> deploymentDetailsArray = new ArrayList<ModelDeploymentDetailsBO>();
        for (ModelDeploymentMapBO deploymentMap : modelDeploymentMapList) {
          Map<String, Object> paramMap = new HashMap<>();
          paramMap.put(ModelDeploymentDetailsBO.FLD_MDD_ID, deploymentMap.getDeploymentId());
          ModelDeploymentDetailsBO details = modelDeploymentDetailsBO.findOne(paramMap);
          if (details != null) {
            deploymentDetailsArray.add(details);
          }
        }
        if (deploymentDetailsArray.size() == 0) {
          log.error("Model deployment details not found");
        } else {
          Comparator<ModelDeploymentDetailsBO> comparator = (c1, c2) -> (int) (c2.getDeployedStart() - c1.getDeployedStart());
          deploymentDetailsArray.sort(comparator);
          modelDeploymentDetailsBO = deploymentDetailsArray.get(0);
          modelDeploymentDetailsBO.setDeployedEnd(Calendar.getInstance().getTimeInMillis());
          modelDeploymentDetailsBO.update();
        }
      }
    }
    return modelDeploymentDetailsBO;
  }
}

