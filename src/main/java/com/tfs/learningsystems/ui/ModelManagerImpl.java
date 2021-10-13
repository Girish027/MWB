package com.tfs.learningsystems.ui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.db.ModelDeploymentDetailsBO.Active;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.dao.ModelTestBatchDao;
import com.tfs.learningsystems.ui.dao.ProjectDao;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.ui.nlmodel.ModelJobExecutionService;
import com.tfs.learningsystems.ui.nlmodel.model.*;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState.Status;
import com.tfs.learningsystems.ui.nlmodel.model.dao.ModelDao;
import com.tfs.learningsystems.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kohsuke.github.GHRelease;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Slf4j
@Component
@Qualifier("modelManagerBean")
public class ModelManagerImpl implements ModelManager {


  @Autowired
  private ModelJobExecutionService modelJobService;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  @Qualifier("modelDaoBean")
  private ModelDao modelDao;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  private DataManagementManager dataManagementManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ConfigManager configManager;

  @Autowired
  @Qualifier("orionManagerBean")
  private OrionManager orionManager;

  @Inject
  @Qualifier("projectDaoBean")
  private ProjectDao projectDao;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Inject
  @Qualifier("modelTestBatchDaoBean")
  private ModelTestBatchDao modelTestBatchDao;

  @Inject
  private JsonConverter jsonConverter;

  @Autowired
  @Qualifier("deploy2APIManagerBean")
  private Deploy2APIManager deploy2APIManager;

  @Autowired
  @Qualifier("gitHubManagerBean")
  private GitHubManager gitHubManager;

  @Autowired
  @Qualifier("vectorizerManagerBean")
  private VectorizerManager vectorizerManager;

  public String queueModelForBuilding(String clientId, String id, String modelType, String modelTechnology, String vectorizerVersion)
          throws ApplicationException {
    ModelBO modelBO = validationManager
            .validateAndGetModel(clientId, null, id, Constants.MODEL_DB_ID);

    if (!StringUtils.isEmpty(modelBO.getModelId()) && (modelBO.getModelType().equals(Constants.DIGITAL_MODEL) || modelBO.getModelType().equals(Constants.SPEECH_MODEL))) {
      TFSModelJobState job = modelDao.getModelJobStatus(modelBO.getModelId(), modelBO.getModelType());
      if (job != null && !TFSModelJobState.Status.FAILED.equals(job.getStatus())) {
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setErrorCode("invalid_model_job_state");
        error.setMessage("Cannot resubmit job for the same model unless failed");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    }

    String configId = (modelBO.getConfigId() == null) ? Constants.DEFAULT_ENGLISH_CONFIG_ID
            : Integer.toString(modelBO.getConfigId());
    ModelConfigBO configDetail = configManager.getModelConfigDataById(configId);
    if (configDetail == null) {
      if (appConfig.getDefModelConfig()) {
        configDetail = configManager.getModelConfigByName(Constants.DEFAULT_EN_CONFIG_NAME);
        modelBO.setConfigId(configDetail.getId());
      }
      if (configDetail == null) {
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setErrorCode("invalid_model_config");
        error.setMessage("Cannot submit job with invalid model config");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    }
    ModelConfigBO speechConfigDetail = null;
    if(modelBO.getSpeechConfigId()!=null){
      speechConfigDetail = configManager.getModelConfigDataById(Integer.toString(modelBO.getSpeechConfigId()));
      if (speechConfigDetail == null) {
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setErrorCode("invalid_model_config");
        error.setMessage("Cannot submit job with invalid model config");
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    }

    String token = UUID.randomUUID().toString();

    ModelJobQueueBO modelJob = new ModelJobQueueBO();
    long time = System.currentTimeMillis();

    modelJob.setModelId(id);
    modelJob.setToken(token);
    modelJob.setStartedAt(time);
    modelJob.setCid(ActionContext.getClientId());
    modelJob.setStatus(ModelJobQueueBO.Status.QUEUED);
    modelJob.setModelType(modelBO.getModelType());
    modelJob.create();

    final ModelBO originalModel = modelBO;
    String jobId = Integer.toString(modelJob.getId());

    TFSModelJob callableJob = new TFSModelJob();
    callableJob.setClientId(clientId);
    callableJob.setId(jobId);
    callableJob.setToken(token);
    callableJob.setProjectId(Integer.toString(modelBO.getProjectId()));
    callableJob.setConfigDetail(configDetail);
    callableJob.setDataManagementManager(dataManagementManager);
    callableJob.setAppConfig(appConfig);
    callableJob.setRestTemplate(restTemplate);
    callableJob.setOrionManager(orionManager);
    callableJob.setConfigManager(configManager);
    callableJob.setModelType(modelType);
    callableJob.setDatasetIds(modelBO.getDatasetIds());
    callableJob.setModelTechnology(modelTechnology);
    callableJob.setVectorizerVersion(vectorizerVersion);

    if(modelBO.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL)) {
      if(modelBO.getDigitalHostedUrl() != null && !modelBO.getDigitalHostedUrl().isEmpty()){
        callableJob.setDigitalHostedUrl(modelBO.getDigitalHostedUrl());
      }
      callableJob.setIsUnbundled(modelBO.getIsUnbundled());
      callableJob.setSpeechConfig(speechConfigDetail);
      callableJob.setModelId(modelBO.getModelId());
    }

    ListenableFuture<TFSModelJobResult> future = modelJobService.submit(callableJob);

    Futures.addCallback(future, new FutureCallback<TFSModelJobResult>() {

      @Override
      public void onSuccess(TFSModelJobResult jobResult) {

        try {
          modelJob.setStatus(ModelJobQueueBO.Status.RUNNING);
          modelJob.setModelId(jobResult.getMessage());
          modelJob.update();
          originalModel.setModelId(modelJob.getModelId());
          originalModel.update();
        } catch (Exception ex) {
          log.error("Exception updating status: ", ex);
        }
      }

      @Override
      public void onFailure(Throwable thrown) {

        modelJob.setStatus(ModelJobQueueBO.Status.FAILED);
        modelJob.update();
        log.error(String.format("Failure occurred while submitting job to Orion, " +
                "updating status with id: %s to FAILURE", jobId), thrown);
      }
    });
    return token;
  }

  @Override
  public List<TFSModel> listOfModelsForProject(String clientId, String projectId)
          throws ApplicationException {

    List<TFSModel> tfsModelList = modelDao.getModelsForProject(projectId);
    if (tfsModelList == null || tfsModelList.isEmpty()) {
      log.debug("Project '" + projectId + "' not found or no model built for current project");
    }
    return tfsModelList;
  }

  @Override
  public TFSModelJobState getModelStatus(String clientId, String modelId) {

    ModelBO model = new ModelBO();
    model = model.findOne(modelId);
    TFSModelJobState currentState = null;
    int modelBuildTimeout = Integer.parseInt(appConfig.getModelBuildTimeout());

    if(model != null) {
      if(model.getModelId() != null) {
        currentState = modelDao.getModelJobStatus(model.getModelId(), model.getModelType());
      } else {
        currentState = modelDao.getModelJobStatus(model.getId().toString(), model.getModelType());
        if(currentState != null && currentState.getStatus().equals(TFSModelJobState.Status.FAILED)) {
          log.error(ErrorMessage.ORION_MODEL_POST_ERROR);
          currentState.setStatusMessage(ErrorMessage.ORION_MODEL_POST_ERROR);
        }
        if(currentState != null && currentState.getStartedAt() < (System.currentTimeMillis() - ( modelBuildTimeout * 60 * 1000))) {
          log.error(ErrorMessage.ORION_MODEL_POST_ERROR_24);
          currentState.setStatus(Status.ERROR);
          currentState.setEndedAt(System.currentTimeMillis());
          currentState.setStatusMessage(ErrorMessage.ORION_MODEL_POST_ERROR_24);
          modelDao.updateModelJobStatusByModelId(model.getId().toString(), model.getModelType(), currentState);
        }
      }
    }

    if (currentState == null) {
      currentState = new TFSModelJobState();
      currentState.setStatus(Status.QUEUED);
      currentState.setModelId(modelId);
      return currentState;
    }

    if (!(currentState.getStatus().equals(TFSModelJobState.Status.COMPLETED)
            || currentState.getStatus().equals(TFSModelJobState.Status.FAILED)
            || currentState.getStatus().equals(Status.ERROR))) {

      currentState.setModelType(model.getModelType());
      // if modelId is null, it means orion didn't accepted the request
      try {
        TFSModelJobState orionStatus = model.getModelId() != null ?
                orionManager.getModelBuildingStatus(model.getModelId()) :
                null;
        if (orionStatus != null) {
          if (orionStatus.getStatus() == Status.COMPLETED && !(model.getModelType().equals(Constants.SPEECH_MODEL) || model.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL))) {
              this.getModelAccuracy(clientId, modelId, model);
            //this.getModelWeightedFScore(clientId, modelId, model);
          }
          if(currentState.getStartedAt() < (System.currentTimeMillis() - ( modelBuildTimeout * 60 * 1000))) {
            log.error(ErrorMessage.ORION_MODEL_POST_ERROR_24);
            orionStatus.setStatus(Status.ERROR);
            orionStatus.setEndedAt(System.currentTimeMillis());
            orionStatus.setStatusMessage(ErrorMessage.ORION_MODEL_POST_ERROR_24);
          }

          if(!(currentState.getStatus().equals(orionStatus.getStatus()))) {
            modelDao.updateModelJobStatusByModelId(model.getModelId(), model.getModelType(), orionStatus);
          }
          currentState.setStatus(orionStatus.getStatus());
          currentState.setStatusMessage(orionStatus.getStatusMessage());
          currentState.setEndedAt(orionStatus.getEndedAt());
          currentState.setModelUUID(orionStatus.getModelUUID());
        }
      } catch (Exception e) {
        log.error("Failed to set model state based on Orion status.", e);
        currentState.setStatus(TFSModelJobState.Status.FAILED);
        currentState.setEndedAt(System.currentTimeMillis());
        modelDao.updateModelJobStatusByModelId(model.getModelId(), model.getModelType(), currentState);
      }

    }
    return currentState;
  }

  private String getModelAccuracy(String clientId, String modelId, ModelBO model) {
    String modelAccuracy = model.getModelAccuracy();
    if(modelAccuracy == null) {
      File modelStatistics = this.getModelStatistics(clientId, modelId);
      Workbook workbook = null;
      try (FileInputStream fis = new FileInputStream(modelStatistics);){
        workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet != null) {
          Row firstRow = sheet.rowIterator().next();
          if (firstRow != null) {
            modelAccuracy = firstRow.getCell(1).getStringCellValue();
            model.setModelAccuracy(modelAccuracy);
            model.update();
          }
        }
      } catch (Exception e) {
        log.error("Exception while fetch model accuracy in getModelAccuracy.", e);
      }
    }
    return modelAccuracy;
  }

  private String unzipTrainingOutputFile(ZipFile file, ModelBO model) {
    String modelWeightedFScore = null;
    Enumeration<? extends ZipEntry> entries = file.entries();
    //Iterate over entries
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.getName().startsWith(Constants.TRAINING_LOG_FILE_START_WITH)) {
        try(InputStream is = file.getInputStream(entry);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
          String line;
          while ((line = reader.readLine()) != null) {
            String[] array = line.split(Constants.MODEL_AVERAGE_WEIGHTED_F_SCORE);
            if (array.length > 1) {
              modelWeightedFScore = array[1].split(Constants.COLON)[0];
              model.setModelWeightedFScore(modelWeightedFScore);
              model.update();
              break;
            }
          }
        }catch(IOException e){
          log.error("Exception while reading training output zip file.", e);
        }
      }
    }
    return modelWeightedFScore;
  }

  private String getModelWeightedFScore(String clientId, String modelId, ModelBO model) {
    String modelWeightedFScore = model.getModelWeightedFScore();
    if(modelWeightedFScore == null) {
      File modelTrainingOutputs = this.getModelTrainingOutputs(clientId, modelId);
      //Open the file
      try(ZipFile file = new ZipFile(modelTrainingOutputs)) {
        //Get modelWeightedFScore
        modelWeightedFScore = this.unzipTrainingOutputFile(file, model);
      }
      catch(IOException e) {
        log.error("Failed to get the Model Weighted_F_Score:", e);
      }
    }
    return modelWeightedFScore;
  }

  private File getModalMetadata(Map<String, String> metadataMap){

    File file = null;
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(Constants.MODEL_ACCURACY);
    int rowNum = 0;
    for (Map.Entry<String,String> entry : metadataMap.entrySet()){
      Row row = sheet.createRow(rowNum++);
      row.createCell(0)
              .setCellValue(entry.getKey());
      row.createCell(1)
              .setCellValue(entry.getValue());
    }

    // Write the output to a file
    try {
      file = File.createTempFile(Constants.MODEL_STATS_PREFIX, Constants.XLSX_EXTENSION);
      file.deleteOnExit();
    } catch (IOException e) {
      log.error("Exception while creating Temp file for model accuracy download", e);
    } finally{
      if(file != null && file.exists()){
        file.deleteOnExit();
      }
    }

    if(file != null && file.exists()){
      try(FileOutputStream fileOut = new FileOutputStream(file);){
        workbook.write(fileOut);
      } catch (IOException e){
        log.error("Exception while writing fileOutputStream to xlsx file for model accuracy download", e);
      }
    }

    return file;
  }

  private String getDefaultGitHubRepositoryName(MwbItsClientMapBO mwbItsClientMapBO) {
    StringBuilder repositoryName = new StringBuilder(mwbItsClientMapBO.getItsClientId())
            .append(Constants.HYPHEN)
            .append(mwbItsClientMapBO.getItsAppId())
            .append(Constants.HYPHEN)
            .append(Constants.DEPLOY2_MODELS_STRING);
    return repositoryName.toString();
  }

  @Override
  public File getModelFile(String clientId, String projectId, String modelId)
          throws ApplicationException {

    ModelBO model = new ModelBO();
    model = model.findOne(modelId);
    validationManager
            .validateAndGetModel(clientId, projectId, model.getId().toString(), Constants.MODEL_DB_ID);
    TFSModelJobState currentState = modelDao.getModelJobStatus(model.getModelId(), model.getModelType());
    if (!currentState.getStatus().equals(TFSModelJobState.Status.COMPLETED)) {
      return null;
    }
    if(model.getModelType().equals(Constants.DIGITAL_SPEECH_MODEL)) {
      return orionManager.getBuiltModelFromOrion(model.getModelId());
    }
    if(model.getModelType().equals(Constants.SPEECH_MODEL)) {
      return orionManager.getSpeechModelFromOrion(model.getModelId());
    }
    return orionManager.getDigitalModelFromOrion(model.getModelId());
  }

  @Override
  public ModelBO getModel(String clientId, String projectId, String modelId)
          throws ApplicationException {

    ModelBO model = null;
    try {
      model = validationManager
              .validateAndGetModel(clientId, projectId, modelId, Constants.MODEL_DB_ID);
      TFSModelJobState currentState = this.getModelStatus(clientId, modelId);
      model.setStatus(currentState.getStatus().getValue());
      return model;
    } catch (Exception ex) {
      String message =
              "Exception while retrieving a model in Modeling Workbench: " + ex.getMessage();
      log.error("Exception while retrieving a model in Modeling Workbench ", ex);
      throw new ApplicationException(message, ex);
    }
  }

  @Override
  public ModelBO getDigitalModelByModelId(String modelId) {
    ModelBO modelBO = new ModelBO();
    try {

      Map<String, Object> conditions = new HashMap<>();
      conditions.put(ModelBO.FLD_MODEL_ID, modelId);
      conditions.put(Constants.MODEL_TYPE, Constants.DIGITAL_MODEL);
      modelBO = modelBO.findOne(conditions);

    } catch (Exception ex) {
      String message = "Exception while fetching digital model to create speech model: " + ex.getMessage();
      log.error("Exception while fetching digital model to create speech model: ", ex);
      throw new ApplicationException(message, ex);
    }

    return modelBO;
  }

  @Override
  public Response deleteModel(String clientId, String projectId, String modelId)
          throws ApplicationException {

    ModelBO model;
    try {
        validationManager.validateDeployableModel(clientId, projectId, modelId);

        validationManager.validatePreviewAndLiveModel(clientId, projectId, modelId);

        model = validationManager
              .validateAndGetModel(clientId, projectId, modelId, Constants.MODEL_DB_ID);

      String speechModelId = model.getSpeechModelId();
      if(speechModelId != null && !speechModelId.isEmpty()) {
        ModelBO speechModel = validationManager
                .validateAndGetModel(clientId, projectId, speechModelId, Constants.MODEL_DB_ID);
        if(speechModel != null) {
          speechModel.delete();
          Map<String, Object> paramMap = new HashMap<>();
          paramMap.put(ModelJobQueueBO.FLD_MODEL_ID, speechModel.getModelId());
          paramMap.put(Constants.MODEL_TYPE, Constants.DIGITAL_SPEECH_MODEL);

          ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
          modelJobQueueBO = modelJobQueueBO.findOne(paramMap);
          if(modelJobQueueBO != null){
            modelJobQueueBO.delete();
          }
        }
      }

      if (StringUtils.isNotEmpty(model.getModelId())) {

        ModelTestBatchBO mtbo = new ModelTestBatchBO();
        List<ModelTestBatchBO> batchTestResults;

        Map<String, Object> conditions = new HashMap<>();
        conditions.put(ModelTestBatchBO.FLD_PROJECT_ID, projectId);
        conditions.put(ModelTestBatchBO.FLD_MODEL_ID, model.getModelId());
        conditions.put(ModelTestBatchBO.FLD_CLIENT_ID, clientId);

        Sort sort = Sort.by(Sort.Direction.DESC, BusinessObject.CREATED_AT);
        batchTestResults = mtbo.page(conditions, 0, Integer.MAX_VALUE, sort);

        if (batchTestResults != null && !batchTestResults.isEmpty()) {
          modelTestBatchDao.deleteBatchesByIds(batchTestResults);
        }

        ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ModelJobQueueBO.FLD_MODEL_ID, model.getModelId());

        modelJobQueueBO = modelJobQueueBO.findOne(paramMap);
        modelJobQueueBO.delete();
      }

      model.delete();
    } catch (Exception ex) {
      String message = "Exception while deleting a model in Modeling Workbench: " + ex.getMessage();
      log.error("Exception while deleting a model in Modeling Workbench ", ex);
      throw new ApplicationException(message, ex);

    }

    ModelDetails modelDetails = new ModelDetails();
    modelDetails
            .projectId(projectId)
            .modelId(modelId)
            .modelName(model.getName())
            .clientId(clientId);

    try {
      if (StringUtils.isNotEmpty(model.getModelId())) {
        orionManager.deleteBuiltModelFromOrion(model.getModelId());
      }
    } catch (Exception ex) {
      log.error("Exception deleting model in Model Builder: , {} ", model.getModelId(), ex);

      return Response.ok().build();
    }

    return Response.ok().build();
  }

  @Override
  public ModelBO updateModel(String clientId, String projectId, String modelDBId, PatchRequest patchRequest) {
    ModelBO unmodifiedModel = validationManager.validateAndGetModel(clientId, projectId, modelDBId, Constants.MODEL_DB_ID);

    ModelBO modifiedModel = jsonConverter.patch(patchRequest, unmodifiedModel, ModelBO.class);
    modifiedModel.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
    modifiedModel.update();
    return (modifiedModel);
  }

  @Override
  public String updateCombinedModel(String clientId, String projectId, String modelDBId, String digitalHostedUrl) {
    try {
      ModelBO model = validationManager
              .validateAndGetModel(clientId, projectId, modelDBId, Constants.MODEL_DB_ID);
      Boolean isUnbundled = model.getIsUnbundled();
      TFSModelJobState currentJobState = modelDao.getModelJobStatus(model.getModelId(), Constants.SPEECH_MODEL);
      String[] urlTokens = digitalHostedUrl.split("/");
      String digitalModelUUID = urlTokens[(urlTokens.length - 2)];
      ModelBO digitalModel = getDigitalModelByModelId(digitalModelUUID);
      VectorizerBO vectorizer;
      if (digitalModel != null) {
        vectorizer = vectorizerManager.getVectorizerById(digitalModel.getVectorizer_type().toString());
      } else {
        vectorizer = vectorizerManager.getVectorizerByClientProject(clientId, projectId);
      }

      if (currentJobState != null && currentJobState.getStatus().toString().equals(Constants.COMPLETE_STATUS)) {
        String modelUUID = orionManager.patchModelToOrion(model.getModelId(), null, isUnbundled, digitalHostedUrl, null, "combine", vectorizer.getType(), vectorizer.getVersion());
        log.info(
                String.format("For project id: %s, Orion started building with UUID: %s",
                        projectId, modelUUID));
        String currentModelType = currentJobState.getModelType();
        currentJobState.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        currentJobState.setStatus(Status.RUNNING);
        modelDao.updateModelJobStatusModelTypeByModelIdModelType(model.getModelId(), currentJobState, currentModelType);
        model.setModelType(Constants.DIGITAL_SPEECH_MODEL);
        model.setVectorizer_type(vectorizer.getId());
        model.update();
        return modelUUID;
      } else {
        String message = "Cannot Combine model in Modeling Workbench: ";
        log.error("Exception while Combining model in Modeling Workbench model_id : {}", model.getModelId());
        Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(), null, message);
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }
    } catch (ApplicationException e) {
      String message = "Cannot Combine model in Modeling Workbench: ";
      log.error("Exception while Combining model in Modeling Workbench model id : {}", modelDBId);
      Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(), null, message);
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }
  }


  @Override
  public File getModelStatistics(final String clientId, final String modelId)
          throws ApplicationException {

    ModelBO model = new ModelBO();
    model = model.findOne(modelId);
    validationManager
            .validateAndGetModel(clientId, null, model.getId().toString(), Constants.MODEL_DB_ID);
    return orionManager.getBuildModelStatsFromOrion(model.getModelId());
  }

  @Override
  public File getModelTrainingOutputs(final String clientId, final String modelId)
          throws ApplicationException {

    ModelBO model = new ModelBO();
    model = model.findOne(modelId);
    validationManager
            .validateAndGetModel(clientId, null, model.getId().toString(), Constants.MODEL_DB_ID);
    return orionManager.getModelTrainingOutputsFromOrion(model.getModelId());
  }

  @Override
  public TFSDeploymentModuleDetails publishModel(String clientId,
                                                 List<ProjectModelRequest> projectModels, String tag, String userEmail)
          throws ApplicationException {

    List<TFSProjectModel> projectModelList = new ArrayList<>();

    TFSDeploymentModuleDetails tfsDeploymentModuleDetails = new TFSDeploymentModuleDetails();

    ArrayList<TFSModelTagDetails> responseTag = new ArrayList<>();

    TFSModelTagDetails tfsModelTagDetails = new TFSModelTagDetails();

    tag = StringUtils.isNotEmpty(tag) ? tag : GitHubUtils.getTagForCurrentDate();

    tfsModelTagDetails.setTagName(tag);

    TFSDeploy2Module tfsDeploy2Module;

    try {

      List<String> invalidModelIds = new ArrayList<>();
      List<ModelBO> modelsToDeploy = null;
      Map<Integer, String> validProjects = new HashMap<>();
      Map<Integer, Integer> validProjectModels = new HashMap<>();
      StringBuilder modelsSummary = new StringBuilder();

      if(projectModels == null || projectModels.isEmpty()) {
        log.error("Could not find project models details for client: {}  ", clientId);
        Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
                ErrorCode.PROJECT_MODELS_EMPTY,
                ErrorMessage.NULLABLE_OR_EMPTY_MODELS);
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }

      modelsToDeploy = validationManager
              .validateAndGetDeployableModelsAndProjects(clientId, projectModels, invalidModelIds,
                      validProjects, validProjectModels, modelsSummary, Constants.MODEL_DB_ID);

      if ((modelsToDeploy.isEmpty()) || !invalidModelIds.isEmpty()) {
        log.error(" Could not find deployable models for client: {}  ",clientId);
        Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
                ErrorCode.INVALID_MODULES,
                ErrorMessage.INVALID_MODELS);
        throw new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST).entity(error).build());
      }

      ClientBO clientBO = new ClientBO();
      clientBO = clientBO.findOne(clientId);
      tfsDeploy2Module = checkForPreExistingModule(clientBO);
      Deploy2ModuleRepoParams repoParams = null;

      if (tfsDeploy2Module != null) {
        repoParams = tfsDeploy2Module.getRepo_params();
      }

      Map<Integer, byte[]> modelFiles = new HashMap<>();

      Map<Integer, byte[]> configFiles = new HashMap<>();

      gitHubManager.createModelConfigs(clientId, modelsToDeploy, modelFiles, configFiles,
              projectModelList);

      MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put(BusinessObject.OBJ_ID, clientId);

      mwbItsClientMapBO = mwbItsClientMapBO.findOne(paramMap);

      String repositoryName = null;

      String gitHubOrg = null;

      boolean ifPredefined = false;
      if (repoParams == null) {
        repositoryName = getDefaultGitHubRepositoryName(mwbItsClientMapBO);
        gitHubOrg = appConfig.getGitHubOrg();
        ifPredefined = false;
      } else {
        repositoryName = repoParams.getGithub_repo();
        gitHubOrg = repoParams.getGithub_org();
        if(repositoryName == null || repositoryName.isEmpty() || gitHubOrg == null || gitHubOrg.isEmpty()) {
          log.error(" Could not find either repoitoryName: " + repositoryName + " or gitHubOrg: " +
                  gitHubOrg + " for client ", clientId);
          Error error = ErrorUtil.createError(Response.Status.BAD_REQUEST.getStatusCode(),
                  ErrorCode.INVALID_GITHUB_INFO_DEPLOY2,
                  ErrorMessage.INVALID_GITHUB_INFO_FROM_DEPLOY2);
          throw new BadRequestException(
                  Response.status(Response.Status.BAD_REQUEST).entity(error).build());
        }
        ifPredefined = true;
      }

      GHRelease ghRelease = gitHubManager
              .checkOrCreateRepositories(mwbItsClientMapBO, clientId, validProjects, modelFiles,
                      configFiles, gitHubOrg, repositoryName, modelsSummary, tag, userEmail);

      ModelDeploymentDetailsBO modelDeploymentDetailsBO = createEntriesInDeploymentTables(
              ghRelease, clientId, validProjects, validProjectModels, userEmail);

      String packageName = createThePackageAndUpdateClient(mwbItsClientMapBO, ghRelease, gitHubOrg,
              repositoryName, ifPredefined, tfsDeploy2Module, clientBO,
              tfsDeploymentModuleDetails);

      ModelDeploymentDetailsBO updatedDeploymentDetailsBO = updateEntriesInDeploymentTables(
              modelDeploymentDetailsBO);

      tfsModelTagDetails.setTagStatus(updatedDeploymentDetailsBO.getStatus());
      tfsModelTagDetails.setPackageName(packageName);

    } catch (Exception e) {

      String message =
              "Exception while publishing a model in Modeling Workbench  " + e.getMessage();
      log.error("Exception while publishing a model in Modeling Workbench  for client {} ",
              clientId, e);

      throw new ApplicationException(message, e);
    }

    tfsModelTagDetails.setProjectModels(projectModelList);
    responseTag.add(tfsModelTagDetails);
    tfsDeploymentModuleDetails.setModelTags(responseTag);

    return tfsDeploymentModuleDetails;

  }

  private TFSDeploy2Module checkForPreExistingModule(ClientBO clientBO) {

    List<TFSDeploy2Module> modules = null;
    if (clientBO != null && StringUtils.isNotEmpty(clientBO.getDeploymentModule())) {
      modules = deploy2APIManager.findModule(clientBO.getDeploymentModule());
      if (modules != null && !modules.isEmpty()) {
        for(TFSDeploy2Module module : modules) {
          if(module.getName().equals(clientBO.getDeploymentModule())) {
            return module;
          }
        }
      }
    }
    return null;
  }


  @Override
  public File getModelTrainingData(String clientId, String projectId, String modelId) throws ApplicationException{
    ModelBO model = null;
    try{
      model = validationManager.validateAndGetModel(clientId, projectId, modelId, Constants.MODEL_DB_ID);
      List<String> datasetIds = model.getDatasetIds();
      return dataManagementManager.export(clientId, projectId, datasetIds,
              "", Boolean.FALSE,
              Boolean.TRUE, Boolean.FALSE , null) ;
    } catch (Exception ex) {
      String message = "Exception while retrieving a model in Modeling Workbench: " + ex.getMessage();
      log.error("Exception while retrieving a model in Modeling Workbench ", ex);
      throw new ApplicationException(message, ex);
    }
  }



  private ModelDeploymentDetailsBO createEntriesInDeploymentTables(GHRelease gitHubRelease,
                                                                   String clientId, Map<Integer, String> validProjects,
                                                                   Map<Integer, Integer> validProjectModels, String userEmail) {

    ModelDeploymentDetailsBO modelDeploymentDetailsBO = null;

    modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();
    modelDeploymentDetailsBO.setClientId(Integer.parseInt(clientId));
    modelDeploymentDetailsBO.setDeployedStart(Calendar.getInstance().getTimeInMillis());
    modelDeploymentDetailsBO.setGitHubTag(gitHubRelease.getTagName());
    modelDeploymentDetailsBO.setDeployedBy(userEmail);
    modelDeploymentDetailsBO.setStatus(ModelDeploymentDetailsBO.Status.TAGGED);
    modelDeploymentDetailsBO.setIsActive(ModelDeploymentDetailsBO.Active.ACTIVE);
    modelDeploymentDetailsBO = modelDeploymentDetailsBO.create();

    ModelDeploymentMapBO modelDeploymentMapBO;

    for (Integer projectId : validProjects.keySet()) {

      modelDeploymentMapBO = new ModelDeploymentMapBO();
      modelDeploymentMapBO.setDeploymentId(modelDeploymentDetailsBO.getId());
      modelDeploymentMapBO.setGitHubTag(gitHubRelease.getTagName());
      modelDeploymentMapBO.setProjectId(projectId);
      modelDeploymentMapBO.setModelId(validProjectModels.get(projectId).toString());
      modelDeploymentMapBO.create();

    }

    return modelDeploymentDetailsBO;
  }

  private ModelDeploymentDetailsBO updateEntriesInDeploymentTables(
          ModelDeploymentDetailsBO modelDeploymentDetailsBO) {

    modelDeploymentDetailsBO.setDeployedStart(Calendar.getInstance().getTimeInMillis());

    modelDeploymentDetailsBO.setStatus(ModelDeploymentDetailsBO.Status.CREATED);

    modelDeploymentDetailsBO.update();

    return modelDeploymentDetailsBO;

  }


  private String createThePackageAndUpdateClient(MwbItsClientMapBO mwbItsClientMapBO,
                                                 GHRelease gitHubRelease,
                                                 String gitHubOrgName, String repositoryName, boolean ifPreDefined,
                                                 TFSDeploy2Module tfsDeploy2Module, ClientBO clientBO,
                                                 TFSDeploymentModuleDetails tfsDeploymentModuleDetails) {

    Integer moduleId = null;
    String packageName = null;
    String moduleName = null;

    String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);

    if (!ifPreDefined) {

      moduleName = deploy2APIManager
              .createPackageOrModuleName(mwbItsClientMapBO, Constants.DEPLOY2_MODULE_CREATION_TYPE, "");

      moduleId = deploy2APIManager.loadOrCreateConvensionModule(gitHubOrgName,
              repositoryName, moduleName, userEmail);

      packageName = deploy2APIManager
              .loadOrCreateConvensionPackage(mwbItsClientMapBO, gitHubRelease, moduleId,
                      moduleName);


    } else {

      moduleName = tfsDeploy2Module.getName();
      packageName = deploy2APIManager
              .extractPackageFromModuleAndCheck(tfsDeploy2Module, gitHubRelease, userEmail);
    }

    clientBO.setDeploymentModule(moduleName);
    clientBO.update();

    tfsDeploymentModuleDetails.setModuleName(moduleName);

    return packageName;
  }


  @Override
  public TFSDeploymentModuleDetails getClientTags(String clientId) {
    ModelDeploymentDetailsBO modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();

    ClientBO clientBO=new ClientBO();

    clientBO=clientBO.findOne(clientId);

    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

    Map<String, Object> mwbParamMap = new HashMap<>();
    mwbParamMap.put(BusinessObject.OBJ_ID, clientId);

    mwbItsClientMapBO = mwbItsClientMapBO.findOne(mwbParamMap);

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ModelDeploymentDetailsBO.FLD_CLIENT_ID, clientId);
    paramMap.put(ModelDeploymentDetailsBO.FLD_ACTIVE, Active.ACTIVE.getValue());
    paramMap.put(ModelDeploymentDetailsBO.FLD_STATUS,
            ModelDeploymentDetailsBO.Status.CREATED.getValue());

    List<ModelDeploymentDetailsBO> modelDeploymentDetailsBOs = modelDeploymentDetailsBO
            .list(paramMap, null);

    List<TFSModelTagDetails> tfsModelTagDetails = new ArrayList<>();

    for (ModelDeploymentDetailsBO modelDeploymentDetailsBOElem : modelDeploymentDetailsBOs) {
      tfsModelTagDetails
              .add(getClientTagDetails(modelDeploymentDetailsBOElem, mwbItsClientMapBO));
    }

    TFSDeploymentModuleDetails tfsDeploymentModuleDetails= new TFSDeploymentModuleDetails();

    tfsDeploymentModuleDetails.setModelTags(tfsModelTagDetails);

    tfsDeploymentModuleDetails.setModuleName(clientBO.getDeploymentModule());


    return tfsDeploymentModuleDetails;
  }

  @Override
  public TFSDeploymentModuleDetails getClientTagDetails(String clientId, String tagName) {

    ModelDeploymentDetailsBO modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();
    ClientBO clientBO=new ClientBO();

    clientBO=clientBO.findOne(clientId);

    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

    Map<String, Object> mwbParamMap = new HashMap<>();
    mwbParamMap.put(BusinessObject.OBJ_ID, clientId);

    mwbItsClientMapBO = mwbItsClientMapBO.findOne(mwbParamMap);

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ModelDeploymentDetailsBO.FLD_CLIENT_ID, clientId);
    paramMap.put(ModelDeploymentDetailsBO.FLD_GIT_HUB_TAG, tagName);
    paramMap.put(ModelDeploymentDetailsBO.FLD_ACTIVE, Active.ACTIVE.getValue());
    paramMap.put(ModelDeploymentDetailsBO.FLD_STATUS,
            ModelDeploymentDetailsBO.Status.CREATED.getValue());

    List<ModelDeploymentDetailsBO> modelDeploymentDetailsBOs = modelDeploymentDetailsBO
            .list(paramMap, null);

    if (modelDeploymentDetailsBOs == null || modelDeploymentDetailsBOs.isEmpty()) {
      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("Cannot_find_tag_details");
      error.setMessage("Unable to find relevant tag for the client");
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }

    ModelDeploymentDetailsBO modelDeploymentDetailsBORes = modelDeploymentDetailsBOs.get(0);

    TFSDeploymentModuleDetails tfsDeploymentModuleDetails= new TFSDeploymentModuleDetails();

    List<TFSModelTagDetails> tfsModelTagDetails = new ArrayList<>();

    tfsModelTagDetails.add(this.getClientTagDetails(modelDeploymentDetailsBORes, mwbItsClientMapBO));

    tfsDeploymentModuleDetails.setModelTags(tfsModelTagDetails);

    tfsDeploymentModuleDetails.setModuleName(clientBO.getDeploymentModule());

    return tfsDeploymentModuleDetails;

  }


  public TFSModelTagDetails getClientTagDetails(ModelDeploymentDetailsBO modelDeploymentDetailsBO,
                                                MwbItsClientMapBO mwbItsClientMapBO) {

    ModelDeploymentMapBO modelDeploymentMapBO = new ModelDeploymentMapBO();

    Map<String, Object> deploymentMapParamMap = new HashMap<>();
    deploymentMapParamMap
            .put(ModelDeploymentMapBO.FLD_GIT_HUB_TAG, modelDeploymentDetailsBO.getGitHubTag());

    List<ModelDeploymentMapBO> modelDeploymentMapBOs = modelDeploymentMapBO
            .list(deploymentMapParamMap, null);

    TFSModelTagDetails tfsModelTagDetails = new TFSModelTagDetails();

    tfsModelTagDetails.setTagName(modelDeploymentDetailsBO.getGitHubTag());

    tfsModelTagDetails.setTagStatus(modelDeploymentDetailsBO.getStatus());

    String packageName = deploy2APIManager.createPackageOrModuleName(mwbItsClientMapBO,
            Constants.DEPLOY2_PACKAGE_CREATION_TYPE, modelDeploymentDetailsBO.getGitHubTag());

    tfsModelTagDetails.setPackageName(packageName);

    List<TFSProjectModel> tfsProjectModels = new ArrayList<>();

    TFSProjectModel tfsProjectModel;

    Set<Integer> projectIDs = modelDeploymentMapBOs.stream().map(ModelDeploymentMapBO::getProjectId)
            .collect(Collectors.toSet());

    Set<String> modelIds = modelDeploymentMapBOs.stream().map(ModelDeploymentMapBO::getModelId)
            .collect(Collectors.toSet());

    List<ProjectBO> projectBos = projectDao.getProjectsByProjectIDs(new ArrayList<>(projectIDs));

    Map<Integer, String> projectIdNameMap = projectBos.stream().collect(
            Collectors.toMap(ProjectBO::getId, ProjectBO::getName)
    );

    List<TFSModel> modelBos = modelDao.getModelsForModelIds(new ArrayList<String>(modelIds));

    Map<String, TFSModel> modelIdBOMap =
            modelBos.stream().collect(Collectors.toMap(TFSModel::getId, tfsModel -> tfsModel));

    for (ModelDeploymentMapBO modelDeploymentMapBOElem : modelDeploymentMapBOs) {

      tfsProjectModel = new TFSProjectModel();

      tfsProjectModel.setModelId(modelDeploymentMapBOElem.getModelId());

      tfsProjectModel.setProjectId(modelDeploymentMapBOElem.getProjectId().toString());

      tfsProjectModel.setProjectName(projectIdNameMap.get(modelDeploymentMapBOElem.getProjectId()));

      tfsProjectModel.setModelVersion(
              modelIdBOMap.get(modelDeploymentMapBOElem.getModelId()).getVersion().toString());

      tfsProjectModels.add(tfsProjectModel);

    }

    tfsModelTagDetails.setProjectModels(tfsProjectModels);

    return tfsModelTagDetails;
  }

}