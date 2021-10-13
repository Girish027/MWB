/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.DatasetIntentInheritanceBO;
import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.job.LogstashJob;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.dao.DatasetIntentInheritanceDao;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.model.LogstashJobDetail;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TaskEvent;
import com.tfs.learningsystems.ui.model.DatasetIntents;
import com.tfs.learningsystems.ui.model.TransformDatasetRequest;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.DatasetStatsResponse;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import com.tfs.learningsystems.util.QuartzJobUtil;
import com.tfs.learningsystems.util.CommonUtils;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

/**
 * @author jkarpala
 */
@Component
@Qualifier("datasetManagerBean")
@Slf4j
public class DatasetManagerImpl implements DatasetManager {

  @Inject
  @Qualifier("datasetIntentInheritanceDao")
  private DatasetIntentInheritanceDao datasetIntentInheritanceDao;

  @Inject
  private JsonConverter jsonConverter;

  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Inject
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Autowired
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  private Scheduler scheduler;

  @Inject
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Inject
  @Qualifier("fileManagerBean")
  private FileManager fileManager;

  @Inject
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  @Qualifier("contentManagerBean")
  private DataManagementManager dataManagementManager;

  private static final String DATASET_NOT_FOUND = "dataset_not_found";

  /**
   * get a dataset specified by an id
   *
   * @return DatasetDetail
   */
  @Override
  public DatasetBO getDatasetById(String datasetId) {

    DatasetBO datasetBO = new DatasetBO();
    datasetBO = datasetBO.findOne(datasetId);
    return (datasetBO);
  }

  @Override
  public DatasetStatsResponse validDatasetForModelBuildingById(String clientId, String projectId, String datasetId) {

    int tagCount = 0;
    int untagCount = 0;
    String rutagString = null;
    boolean isValidRuCount = false;
    List<String> sortByList = new ArrayList<String>() {
      private static final long serialVersionUID = 1L;
      {
        add("count:asc");
      }
    };

    TranscriptionDocumentDetailCollection allTranscriptions = searchManager
            .getAllTranscriptions(clientId, projectId, Collections.singletonList(datasetId), null,
                    sortByList, false);

    DatasetStatsResponse stats = new DatasetStatsResponse();
    Set<DatasetIntents> datasetIntents = new HashSet<>();
    for (final TranscriptionDocumentDetail transcription : allTranscriptions
            .getTranscriptionList()) {
      if(transcription.getRutag() == null || transcription.getRutag().isEmpty()) {
        untagCount++;
      } else {
        CSVFileUtil.validateIntent(transcription.getRutag());
        if(!isValidRuCount){
          isValidRuCount = CSVFileUtil.validateRUCount(rutagString, transcription.getRutag());
          stats.setUniqueRollupValue(transcription.getRutag());
        }
        rutagString = transcription.getRutag();
        tagCount++;
        DatasetIntents datasetIntent = new DatasetIntents();
        datasetIntent.setRutag(rutagString);
        datasetIntent.setIntent(transcription.getIntent());
        datasetIntents.add(datasetIntent);
      }
      if(transcription.getIntent() != null && !transcription.getIntent().isEmpty()){
        CSVFileUtil.validateIntent(transcription.getIntent());
      }
    }


    if(!isValidRuCount) {
      stats.setIsDatasetValid(false);
    }

    Float percentage = ((float) tagCount / (tagCount + untagCount));
    stats.setIsDatasetFullyTagged(percentage == 1.0f);
    stats.setDatasetIntentsSet(datasetIntents);
    return stats;
  }

  @Override
  public DatasetBO getDatasetById(String clientId, String projectId, String datasetId) {

    DatasetBO datasetBO = getDatasetById(datasetId);
    List<String> datasetIds = new ArrayList<>();
    datasetIds.add(datasetId);
    DatasetTaskStatusResponse datasetTaskStatusResponse =
            jobManager.getTransformStatusForDatasets(datasetIds);

    TaskEventBO taskEventBO = datasetTaskStatusResponse.get(datasetId);
    if (taskEventBO != null) {
      if (taskEventBO.getStatus().equals(TaskEvent.Status.COMPLETED.toString())
              && taskEventBO.getPercentComplete() != 100) {
        datasetBO.setTransformationStatus(TaskEvent.Status.QUEUED.toString());
      } else {
        datasetBO.setTransformationStatus(taskEventBO.getStatus());
      }
      datasetBO.setTransformationTask(taskEventBO.getTask());

    } else {
      datasetBO.setTransformationStatus(TaskEvent.Status.NULL.toString());
      datasetBO.setTransformationTask(null);
    }
    return (datasetBO);

  }

  @Override
  public Map<Integer, String> getDatasetSourceMapByDatasetIds(List<String> datasetIds) {
    validationManager.validateDatasetIds(datasetIds);
    Map<Integer, String> datasetSourceMap = new HashMap<>();
    for(String datasetId: datasetIds) {
      DatasetBO datasetBO = this.getDatasetById(datasetId);
      datasetSourceMap.put(datasetBO.getId(), datasetBO.getSource());
    }
    return datasetSourceMap;
  }

  /**
   * create a new dataset
   */
  @Override
  public DatasetBO postDataset(String clientId, String projectId, String name, String source,
                               InputStream fileInputStream,
                               boolean ignoreFirstRow, FileColumnMappingSelectionList columnMappings, String dataType,
                               String description, String currentUserEmail) {

    validationManager.validateClient(clientId);
    validationManager.validateUser(source);
    // we don't need to set createdAt and modifiedAt. They are taken care of by the BusinessObject

    FileStagedImportResponse importResponse = null;

    try {
      importResponse = fileManager.importFile(fileInputStream,
              currentUserEmail, null);
    } catch (BadRequestException e) {
      log.error("Failed to import file", e);
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                      e.getMessage()));
    } catch (Exception e) {
      log.error("Failed to import file", e);
      throw new InvalidRequestException(
              new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.IMPORTING_FILE_ERROR));
    }

    FileBO fileBO = fileManager.generateUserSelectedColumnsFile(currentUserEmail,
            importResponse.getToken(), ignoreFirstRow, columnMappings);

    DatasetBO dataset = new DatasetBO();
    dataset.setClientId(clientId);
    dataset.setName(name);
    dataset.setDataType(dataType);
    dataset.setUri(Constants.FILE_UPLOAD_BASE_URI + fileBO.getFileId());
    dataset.setLocale("en-US");
    dataset.setDescription(description);
    dataset.setProjectId(projectId);
    dataset.setCreatedBy(currentUserEmail);
    dataset.setModifiedBy(currentUserEmail);
    dataset.setSource(DatasetBO.Source.valueOf(source));
    dataset.create();

    int id = dataset.getId();

    projectManager
            .addDatasetProjectMapping(clientId, projectId, String.valueOf(id), currentUserEmail);

    TaskEventBO taskEventBO = dataManagementManager
            .transform(clientId, projectId, String.valueOf(id), currentUserEmail, Boolean.FALSE);

    DatasetIntentInheritance inheritance = this
            .getLastPendingInheritaceForDataset(Integer.toString(id));
    if (inheritance == null) {
      DatasetIntentInheritanceBO dii = new DatasetIntentInheritanceBO();
      dii.setDatasetId(id);
      long timeMillis = System.currentTimeMillis();
      dii.setRequestedAt(timeMillis);
      dii.setRequestedBy(currentUserEmail);
      dii.setProjectId(Integer.parseInt(projectId));
      dii.setStatus(DatasetIntentInheritanceBO.Status.PENDING);
      dii.create();
    }

    if (taskEventBO != null) {
      if (taskEventBO.getStatus().equals(TaskEvent.Status.COMPLETED.toString())
              && taskEventBO.getPercentComplete() != 100) {
        dataset.setTransformationStatus(TaskEvent.Status.QUEUED.toString());
      } else {
        dataset.setTransformationStatus(taskEventBO.getStatus());
      }
      dataset.setTransformationTask(taskEventBO.getTask());

    } else {
      dataset.setTransformationStatus(TaskEvent.Status.NULL.toString());
      dataset.setTransformationTask(null);
    }

    return dataset;
  }

  /**
   * create a new dataset
   */
  @Override
  public DatasetBO addDataset(AddDatasetRequest addDatasetRequest, String currentUserEmail) {

    DatasetBO dataset = addDatasetRequest.getDataset();
    boolean autoTagDataset = addDatasetRequest.isAutoTagDataset();

    validationManager.validateClient(dataset.getClientId());
    validationManager.validateUser(dataset.getSource());

    // we don't need to set createdAt and modifiedAt. They are taken care of by the BusinessObject
    dataset.setCreatedBy(currentUserEmail);
    dataset.setModifiedBy(currentUserEmail);

    dataset.create();

    int id = dataset.getId();

    DatasetIntentInheritance inheritance = this
            .getLastPendingInheritaceForDataset(Integer.toString(id));
    if (inheritance == null && autoTagDataset) {
      DatasetIntentInheritanceBO dii = new DatasetIntentInheritanceBO();
      dii.setDatasetId(id);
      long timeMillis = System.currentTimeMillis();
      dii.setRequestedAt(timeMillis);
      dii.setRequestedBy(currentUserEmail);
      dii.setProjectId(Integer.parseInt(addDatasetRequest.getProjectId()));
      dii.setStatus(DatasetIntentInheritanceBO.Status.PENDING);
      dii.create();
    }

    return dataset;
  }

  /**
   * Delete dataset based on id
   */
  @Override
  public void deleteDataset(String clientId, String datasetId) {

    DatasetBO dataset = new DatasetBO();

    Map<String, Object> dataSetConditions = new HashMap<>();

    dataSetConditions.put(DatasetBO.FLD_CLIENT_ID, clientId);
    dataSetConditions.put(DatasetBO.FLD_DATASET_ID, datasetId);

    dataset = dataset.findOne(dataSetConditions);

    if (dataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode(DATASET_NOT_FOUND);
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new NotFoundException(error);
    } else {
      this.projectDatasetManager.removeDatasetProjectMapping(datasetId);
      dataset.delete();
    }

  }

  /**
   * Delete dataset based on id
   */
  @Override
  public void deleteDataset(String datasetId) {

    // TODO Client_Isolation

    DatasetBO dataset = new DatasetBO();
    dataset = dataset.findOne(datasetId);
    if (dataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode(DATASET_NOT_FOUND);
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new NotFoundException(error);
    } else {
      dataset.delete();
      this.projectDatasetManager.removeDatasetProjectMapping(datasetId);
    }

  }

  /**
   * update dataset
   *
   * @return updated dataset details
   */
  @Override
  public DatasetBO updateDataset(String datasetId, PatchRequest patchRequest,
                                 String currentUserEmail) {

    DatasetBO unmodifiedDataset = new DatasetBO();
    unmodifiedDataset = unmodifiedDataset.findOne(datasetId);
    if (unmodifiedDataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode(DATASET_NOT_FOUND);
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new NotFoundException(error);
    }

    DatasetBO modifiedDataset =
            jsonConverter.patch(patchRequest, unmodifiedDataset, DatasetBO.class);

    modifiedDataset.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    modifiedDataset.setModifiedBy(currentUserEmail);
    modifiedDataset.update();

    return (modifiedDataset);
  }

  /**
   * get paged list of data sets
   *
   * @return paged list of data sets
   */
  @Override
  public List<DatasetBO> getDatasets(int startIndex, int count, String filter,
                                     String filterClientId) {

    DatasetBO datasetBO = new DatasetBO();
    Map<String, Object> param = new HashMap<>();
    if (!StringUtils.isEmpty(filterClientId)) {
      param.put(DatasetBO.FLD_CLIENT_ID, filterClientId);
    }
    return (datasetBO.page(param, startIndex, count, null));
  }

  /**
   * count all datasets in datasource
   *
   * @return datasets in datasource
   */
  @Override
  public long countDatasets() {

    DatasetBO datasetBO = new DatasetBO();
    return (datasetBO.count(new HashMap<String, Object>()));
  }

  /**
   * Index the given dataset into Elasticsearch
   */
  @Override
  public DatasetBO transformDataset(DatasetBO datasetDetail,
                                    TransformDatasetRequest transformDatasetRequest) throws ApplicationException {

    String projectId = transformDatasetRequest.getProjectId();
    ProjectBO projectDetail = this.projectManager.getProjectById(null, projectId);
    if (projectDetail == null) {
      log.error("Transform dataset - project not found - {}", projectId);
      throw new BadRequestException("Invalid projectId: " + projectId);
    }

    String datasetId = Integer.toString(datasetDetail.getId());
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);

    int elasticSearchApiPort = elasticSearchProps.getApiPort();
    List<String> elasticSearchHostUrls = elasticSearchProps.getHosts()
            .stream()
            .map(host -> String.format("%s:%d", host, elasticSearchApiPort))
            .collect(Collectors.toList());

    String uri = datasetDetail.getUri();
    String[] uriParts = uri.split("/");
    String fileId = uriParts[uriParts.length - 1];
    FileEntryDetail fileEntry = fileManager.getFileById(fileId);
    if (fileEntry == null) {
      log.error("Transform dataset - invalid file id - {} - {} ", fileId, uri);
      throw new BadRequestException("Invalid file id: " + fileId + " and uri: " + uri);
    }

    final String username = AuthUtil.getPrincipalFromSecurityContext(null);
    String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
    FileSystem fileSystem = FileSystems.getDefault();

    Path userRoot = fileSystem.getPath(repositoryRoot, username);
    String userRootString = userRoot.toString();
    Path fileSystemPath = fileSystem.getPath(userRootString, fileEntry.getSystemName());
    Path confFileSystemPath = fileSystem.getPath(userRootString, fileId + ".conf");

    CSVFileUtil.CSVFileInfo csvFileInfo = CSVFileUtil
            .validateAndGetColumns(fileSystemPath.toString());
    log.info("Transform dataset - csv file - {} ", fileSystemPath.toString());
    String[] columns = csvFileInfo.getColumns();

    LogstashJobDetail logstashJobDetail = new LogstashJobDetail();
    logstashJobDetail.setUsername(username);
    logstashJobDetail.setProjectId(Integer.parseInt(projectId));
    logstashJobDetail.setVertical(projectDetail.getVertical());
    logstashJobDetail.setDataType(datasetDetail.getDataType());
    logstashJobDetail.setClientId(Integer.parseInt(datasetDetail.getClientId()));
    logstashJobDetail.setDatasetId(datasetDetail.getId());
    logstashJobDetail.setColumns(columns);
    logstashJobDetail.setCsvFilePath(fileSystemPath.toString());
    logstashJobDetail.setConfFilePath(confFileSystemPath.toString());
    logstashJobDetail.setFirstColumnName(columns[0]);
    logstashJobDetail
            .setOriginalTranscriptionColumnName(appConfig.getCsvOriginalTranscriptionColumnName());
    logstashJobDetail
            .setNormalizedTranscriptionColumnName(appConfig.getCsvNormalizedTranscriptionColumnName());
    logstashJobDetail
            .setTranscriptionEntityColumnName(appConfig.getCsvTranscriptionEntityColumnName());

    logstashJobDetail.setLogstashCheckExecTimeout(appConfig.getCheckLogstashExecTimeout());
    logstashJobDetail.setLogstashExecTimeout(appConfig.getLogstashExecTimeout());
    logstashJobDetail.setElasticSearchHosts(elasticSearchHostUrls);
    logstashJobDetail.setElasticSearchIndexName(elasticSearchProps.getNltoolsIndexAlias());
    logstashJobDetail.setElasticSearchIndexType(elasticSearchProps.getDefaultDocumentIndexType());

    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, 0);
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT, 3);
    jobDataMap.put(QuartzJobUtil.KEY_LOGSTASH_JOB_DETAIL, logstashJobDetail);

    String jobName = String.format("Job_%d", Calendar.getInstance().getTimeInMillis());
    String jobGroup = elasticSearchProps.getQuartzJobGroup();
    JobDetail jobDetail = JobBuilder
            .newJob(LogstashJob.class)
            .withIdentity(new JobKey(jobName, jobGroup))
            .setJobData(jobDataMap)
            .storeDurably(true).build();

    log.info("Transform dataset - schedule job - {} ", datasetId);

    Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).startNow().build();

    try {
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      log.error("Could not schedule a quartz job for ingestion - " + datasetId, e);
      throw new ApplicationException("Could not schedule a quartz job for ingestion", e);
    }

    return datasetDetail;
  }

  /**
   * Get the Locale for the dataset. returns default locale if none specified in the dataset
   *
   * @return Locale
   */
  @Override
  public Locale getDatasetLocale(String datasetId) {

    DatasetBO dataset = this.getDatasetById(datasetId);
    if (dataset == null) {
      return Locale.getDefault();
    }
    String datasetLocale = dataset.getLocale();
    if (datasetLocale == null) {
      return Locale.getDefault();
    }

    return Locale.forLanguageTag(datasetLocale);
  }

  /**
   * set the dataset intent inheritance for the specified dataset and project
   *
   * @param datasetId dataset that should inherit tags
   * @param projectId project the dataset is associated with
   * @param currentUserEmail user that initiated action
   */
  @Override
  public void addDatasetIntentInheritance(final String datasetId,
                                          final String projectId, final String currentUserEmail) {

    DatasetIntentInheritance inheritance = this.getLastPendingInheritaceForDataset(datasetId);
    if (inheritance == null || !projectId.equals(inheritance.getProjectId())) {
      DatasetIntentInheritanceBO dii = new DatasetIntentInheritanceBO();
      dii.setDatasetId(Integer.parseInt(datasetId));
      long timeMillis = System.currentTimeMillis();
      dii.setRequestedAt(timeMillis);
      dii.setRequestedBy(currentUserEmail);
      dii.setProjectId(Integer.parseInt(projectId));
      dii.setStatus(DatasetIntentInheritanceBO.Status.PENDING);
      dii.create();
    }
  }

  /**
   * get last pending inheritance for dataset
   */
  @Override
  public DatasetIntentInheritance getLastPendingInheritaceForDataset(String datasetId) {

    return this.datasetIntentInheritanceDao.getLastPendingInheritaceForDataset(datasetId);
  }

  /**
   * Update intent Inheritance
   */
  @Override
  public DatasetIntentInheritanceBO updateIntentInheritance(DatasetIntentInheritance inheritance) {

    if (inheritance == null || StringUtils.isEmpty(inheritance.getId())) {
      return (null);
    }
    DatasetIntentInheritanceBO dii = new DatasetIntentInheritanceBO();
    dii = dii.findOne(inheritance.getId());
    dii.setDatasetIds(inheritance.getInheritedFromDatasetIds());
    dii.setStatus(DatasetIntentInheritanceBO.Status.COMPLETED);
    dii.setTotalTagged(inheritance.getTotalTagged());
    dii.setUniqueTagged(inheritance.getUniqueTagged());
    dii.setTotalTaggedMulipleIntents(inheritance.getTotalTaggedMulipleIntents());
    dii.setUniqueTaggedMulipleIntents(inheritance.getUniqueTaggedMulipleIntents());
    dii.update();

    return (dii);
  }

  /**
   * update status on intent inheritance
   */
  @Override
  public void updateIntentInheritanceStatus(String id, DatasetIntentInheritanceStatus status) {

    this.datasetIntentInheritanceDao.updateStatus(id, status);
  }
}