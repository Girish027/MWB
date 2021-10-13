/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.opencsv.CSVWriter;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.config.IngestionPropertyConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.helper.FileUploadHelper;
import com.tfs.learningsystems.helper.JobHelper;
import com.tfs.learningsystems.job.CategorizeJob;
import com.tfs.learningsystems.job.LogstashJob;
import com.tfs.learningsystems.logging.AspectLogger;
import com.tfs.learningsystems.ui.model.CategorizeJobDetail;
import com.tfs.learningsystems.ui.model.DatasetDetailsModel;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.model.LogstashJobDetail;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.TaskEvent;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.elasticsearch.index.IndexNotFoundException;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Qualifier("contentManagerBean")
@Slf4j
public class DataManagementManagerImpl implements DataManagementManager {

  private static final String SYSTEM_USER = "0";

  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Autowired
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;

  @Autowired
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Autowired
  private IngestionPropertyConfig ingestionPropertyConfig;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Inject
  private FileManager fileManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private FileUploadHelper fileUploadHelper;

  @Autowired
  private JobHelper jobHelper;

  private void retryTransform(final JobBO job,
                              final List<TaskEventBO> tasks, final DatasetBO dataset,
                              final ProjectBO project, Boolean useModelForSuggestedCategory) {

    final String jobId = Integer.toString(job.getId());
    TaskEventBO unfinishedTask = null;
    Long startTime = Calendar.getInstance().getTimeInMillis();
    for (final TaskEventBO taskEventDetail : tasks) {
      if (!taskEventDetail.isComplete() && !taskEventDetail.isCanceled()) {
        unfinishedTask = taskEventDetail;
        break;
      }
    }

    // No failed task, check if only canceled
    if (unfinishedTask == null) {
      for (final TaskEventBO taskEventDetail : tasks) {
        if (taskEventDetail.isCanceled()) {
          unfinishedTask = taskEventDetail;
          break;
        }
      }
    }

    // no unfinished, failed or canceled tasks
    if (unfinishedTask == null) {
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "dataset_already_transformed",
              "Dataset '" + job.getDatasetId() + "' already transformed for project "
                      + job.getProjectId() + ", please contact engineering"));
    }
    final String jobName = String.format(this.ingestionPropertyConfig.getJobNameFormat(), jobId,
            unfinishedTask.getTask(), startTime);
    final String group = this.elasticSearchProps.getQuartzJobGroup();

    final String repositoryRoot = this.appConfig.getFileUploadRepositoryRoot();
    final FileSystem fileSystem = FileSystems.getDefault();
    String pathBuilder = "jobs/" + "client" + project.getClientId() +
            "/project" + project.getId() + "/job" + jobId;
    final Path jobPath = fileSystem.getPath(repositoryRoot, pathBuilder);

    boolean jobFolderExisted = true;
    // Check job folder
    try {
      if (Files.notExists(jobPath, LinkOption.NOFOLLOW_LINKS)) {
        this.jobHelper.buildJobPath(Integer.toString(project.getClientId()),
                Integer.toString(project.getId()), jobId);
        jobFolderExisted = false;

      }
    } catch (final IOException ioe) {
      log.error("failed to build job path - " + jobPath, ioe);
      throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, ioe);
    }

    final String fileName = job.getFileName();
    final int dotIndex = fileName.lastIndexOf('.');
    final String fileId = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    final FileEntryDetail fileEntry = this.fileManager.getFileById(fileId);
    final String username = AuthUtil.getPrincipalFromSecurityContext(null);

    // check if file is in job folder
    Path jobFilePath;
    boolean jobFileExisted;
    if (jobFolderExisted
            && Files.exists(jobPath.resolve(fileName), LinkOption.NOFOLLOW_LINKS)) {
      jobFilePath = jobPath.resolve(job.getFileName());
      jobFileExisted = true;
    } else {
      try {
        final Path userRoot = fileSystem.getPath(repositoryRoot, fileEntry.getUser());
        final Path filePath = userRoot.resolve(fileName);
        // copy dataset file into job path
        jobFilePath = Files.copy(filePath, jobPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);
        // create copy of original
        Files.copy(filePath, jobPath.resolve(fileId + ".original.csv"),
                StandardCopyOption.REPLACE_EXISTING);
        jobFileExisted = false;
      } catch (final IOException e) {
        log.error("failed to copy dataset file ", e);
        throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, e);
      }
    }

    if (unfinishedTask.getTask().equals(TaskEventBO.TaskType.CATEGORIZE.toString())
            || !jobFileExisted) {
      final int failedTaskIndex = tasks.indexOf(unfinishedTask);
      long modifiedAt = Calendar.getInstance().getTimeInMillis();
      for (TaskEventBO remainingTask : tasks) {
        remainingTask.setModifiedAt(modifiedAt);
        remainingTask.setMessage("");
        remainingTask.setStatus(TaskEventBO.Status.QUEUED);
        remainingTask.setErrorCode(TaskEventBO.ErrorCode.OK.toString());
        jobManager.updateTaskEvent(remainingTask);
      }

      tasks.remove(failedTaskIndex);
      final StringBuilder categorizerUrl = new StringBuilder();
      categorizerUrl.append(this.appConfig.getAutoCategorizerUrl()).append(jobId)
              .append("/file");
      List<String> columns = new ArrayList<>(Arrays.asList(
              this.appConfig.getAutoCategorizerColumns().split(",")));

      String transcriptionColumn =
              this.appConfig.getCsvNormalizedTranscriptionColumnName();

      String originalTranscriptionColumn =
              this.appConfig.getCsvOriginalTranscriptionColumnName();

      final CategorizeJobDetail categorizeJobDetail =
              this.jobHelper.buildCategorizeJobDetail(jobFilePath, categorizerUrl.toString(),
                      dataset.getLocale(), columns, transcriptionColumn, originalTranscriptionColumn,
                      useModelForSuggestedCategory, null, null);

      final Path confFileSystemPath =
              fileSystem.getPath(jobPath.toString(), fileId + ".conf");
      final LogstashJobDetail logstashJobDetail =
              this.jobHelper
                      .buildLogstashJobDetail(project, dataset, jobFilePath, confFileSystemPath, username);

      // create Task
      final JobDataMap jobDataMap = this.jobHelper.buildJobDataMap(categorizeJobDetail,
              logstashJobDetail, unfinishedTask, tasks);

      final JobDetail jobDetail =
              JobBuilder.newJob(CategorizeJob.class).withIdentity(new JobKey(jobName, group))
                      .setJobData(jobDataMap).storeDurably(true).build();

      final String triggerName = String.format(
              this.ingestionPropertyConfig.getTriggerNameFormat(), jobId,
              unfinishedTask.getTask(), Calendar.getInstance().getTimeInMillis());
      final Trigger categorizeTrigger = TriggerBuilder.newTrigger()
              .withIdentity(triggerName, group).forJob(jobDetail).startNow().build();

      if (!this.jobHelper.scheduleJob(jobDetail, categorizeTrigger,
              this.ingestionPropertyConfig.getMaxRetryCount(),
              this.ingestionPropertyConfig.getStartingRetryDelayMs())) {
        modifiedAt = Calendar.getInstance().getTimeInMillis();
        if (!tasks.isEmpty()) {
          for (TaskEventBO remainingTask : tasks) {
            remainingTask.setModifiedAt(modifiedAt);
            remainingTask.setMessage("");
            remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
            remainingTask.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
            jobManager.updateTaskEvent(remainingTask);
          }

        }
        unfinishedTask.setModifiedAt(Calendar.getInstance().getTimeInMillis());
        unfinishedTask.setMessage("Unable to schedule job");
        unfinishedTask.setStatus(TaskEventBO.Status.FAILED);
        unfinishedTask.setErrorCode(unfinishedTask.getFailedErrorCodeForTask().toString());

        this.jobManager.updateTaskEvent(unfinishedTask);
        throw new ServerErrorException(unfinishedTask.getMessage(),
                Status.SERVICE_UNAVAILABLE);
      }

    } else if (unfinishedTask.getTask().equals(TaskEventBO.TaskType.INDEX.toString())) {

      // make sure this project hasn't already been indexed
      final long untaggedTranscriptionsCount = this.searchManager
              .getUntaggedTranscriptionsCount(null, Integer.toString(project.getId()),
                      Collections.singletonList(Integer.toString(job
                              .getDatasetId())));
      final long taggedTranscriptionsCount = this.searchManager
              .getTaggedTranscriptionsCount(null, Integer.toString(project.getId()),
                      Collections.singletonList(Integer.toString(job
                              .getDatasetId())));
      final long totalCount = untaggedTranscriptionsCount + taggedTranscriptionsCount;
      if (totalCount > 0) {
        log.warn("Found {} indexed documents", totalCount);
        throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
                "dataset_already_indexed",
                "Dataset '" + job.getDatasetId() + "' already indexed for project "
                        + job.getProjectId() + ", please contact engineering"));
      } else {
        // TODO: clear index
      }

      tasks.clear();
      unfinishedTask.setCreatedAt(Calendar.getInstance().getTimeInMillis());
      unfinishedTask.setMessage("");
      unfinishedTask.setStatus(TaskEventBO.Status.QUEUED);
      unfinishedTask.setErrorCode(TaskEvent.ErrorCode.OK.toString());

      this.jobManager.updateTaskEvent(unfinishedTask);

      final Path confFileSystemPath =
              fileSystem.getPath(jobPath.toString(), fileId + ".conf");
      final LogstashJobDetail logstashJobDetail =
              this.jobHelper
                      .buildLogstashJobDetail(project, dataset, jobFilePath, confFileSystemPath, username);

      // create Task
      final JobDataMap jobDataMap =
              this.jobHelper.buildJobDataMap(null, logstashJobDetail, unfinishedTask, tasks);

      final JobDetail jobDetail =
              JobBuilder.newJob(LogstashJob.class).withIdentity(new JobKey(jobName, group))
                      .setJobData(jobDataMap).storeDurably(true).build();

      final String triggerName = String.format(
              this.ingestionPropertyConfig.getTriggerNameFormat(), jobId,
              unfinishedTask.getTask(), Calendar.getInstance().getTimeInMillis());
      final Trigger categorizeTrigger = TriggerBuilder.newTrigger()
              .withIdentity(triggerName, group).forJob(jobDetail).startNow().build();

      if (!this.jobHelper.scheduleJob(jobDetail, categorizeTrigger,
              this.ingestionPropertyConfig.getMaxRetryCount(),
              this.ingestionPropertyConfig.getStartingRetryDelayMs())) {

        unfinishedTask.setModifiedAt(Calendar.getInstance().getTimeInMillis());
        unfinishedTask.setMessage("Unable to schedule job");
        unfinishedTask.setStatus(TaskEventBO.Status.FAILED);

        this.jobManager.updateTaskEvent(unfinishedTask);
        throw new ServerErrorException(unfinishedTask.getMessage(),
                Status.SERVICE_UNAVAILABLE);
      }
    }

  }

  @Override
  public File export(final String clientId, String projectId, List<String> datasets,
                     String queryOperator, Boolean onlyTagged,
                     Boolean onlyWithRuTagged, Boolean forModelBuilding, String modelType) {

    try {

      ProjectBO project = validationManager.validateClientAndProject(clientId, projectId);

      List<String> sortByList = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
          add("count:asc");
        }
      };

      TranscriptionDocumentDetailCollection allTranscriptions = searchManager
              .getAllTranscriptions(clientId, projectId, datasets, queryOperator,
                      sortByList, onlyTagged);

      Path jobPath = this.jobHelper
              .buildJobPath(Integer.toString(project.getClientId()), projectId, null);

      String filepath;

      char seperator = ',';
      if (Boolean.TRUE.equals(forModelBuilding)) {
        filepath = jobPath.resolve("output").toString();
        seperator = '\t';
      } else {
        filepath = jobPath.resolve("output.csv").toString();
      }

      String transcriptionLabel = "transcription";

      String intentLabel;

      if (Boolean.TRUE.equals(forModelBuilding)) {
        intentLabel = elasticSearchProps.getTagLabel();
      } else {
        intentLabel = Constants.ROLL_UP_INTENT_LABEL;
      }

      WebApplicationException webApplicationException = null;
      try (final FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {

        // Write UTF-8 Signature
        fileOutputStream.write(ByteOrderMark.UTF_8.getBytes());
        try (final CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8),
                seperator)) {

          writer.writeNext(new String[]{intentLabel,
                  transcriptionLabel,
                  Constants.ORIGINAL_TRANSCRIPTION_LABEL,
                  Constants.GRANULAR_INTENT_LABEL,
                  Constants.FILENAME_LABEL,
                  Constants.TRANSCRIPTION_HASH_LABEL,
                  Constants.COMMENTS_LABEL,
                  Boolean.TRUE.equals(forModelBuilding) ? Constants.SOURCE_LABEL : ""});

          for (final TranscriptionDocumentDetail transcription : allTranscriptions
                  .getTranscriptionList()) {

            if (Boolean.FALSE.equals(onlyWithRuTagged) || !transcription.getRutag().equals("") || Constants.SPEECH_MODEL.equalsIgnoreCase(modelType)) {
              String text =
                      (transcription.getTextStringForTagging() == null
                              || transcription.getTextStringForTagging().isEmpty()) ?
                              null :
                              transcription.getTextStringForTagging();

              writer.writeNext(new String[]{
                      transcription.getRutag() == null ? "" : transcription.getRutag(),
                      text,
                      transcription.getTextStringOriginal(),
                      transcription.getIntent() == null ? "" : transcription.getIntent(),
                      StringUtils.isNotEmpty(transcription.getFileName()) ? transcription.getFileName()
                              : transcription.getUuid(),
                      transcription.getTranscriptionHash(),
                      transcription.getComment(),
                      Boolean.TRUE.equals(forModelBuilding) ? transcription.getDatasetSource() : ""
              });
            }
          }
        }
      } catch (Exception e) {
        log.error("failed to export transcript - " + filepath, e);
        webApplicationException = new ServerErrorException(
                Response.status(Status.SERVICE_UNAVAILABLE)
                        .entity(new Error(Status.INTERNAL_SERVER_ERROR
                                .getStatusCode(), null,
                                ErrorMessage.FILE_ACCESS_MESSAGE)).build(), e);
      }

      if (webApplicationException != null) {
        throw webApplicationException;
      }
      return new File(filepath);

    } catch (IndexNotFoundException e) {
      log.error("failed to export transcript, index not found - " + projectId, e);
      throw new ServiceUnavailableException(
              Response.status(Response.Status.SERVICE_UNAVAILABLE)
                      .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                              null, ErrorMessage.SEARCH_UNAVAILABLE))
                      .build(),

              e);
    } catch (IOException ioe) {
      log.error("failed to export transcript - " + projectId, ioe);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.FILE_ACCESS_MESSAGE))
              .build(), ioe);
    } catch (Exception e) {
      throw new ServiceUnavailableException(
              Response.status(Response.Status.SERVICE_UNAVAILABLE)
                      .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                              null, ErrorMessage.SEARCH_UNAVAILABLE))
                      .build(),

              e);
    }
  }

  @Override
  public File exportUnique(String clientId, String projectId, List<String> datasets,
                           String queryOperator, Boolean onlyTagged,
                           Boolean onlyWithRuTagged) {

    validationManager.validateClientAndProject(clientId, projectId);

    try {
      List<String> sortByList = new ArrayList<>();
      sortByList.add("count:desc");

      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter requestFilter = new SearchRequestFilter().tagged(onlyTagged)
              .untagged(false);
      searchRequest.setFilter(requestFilter);

      TranscriptionDocumentDetailCollection uniqueTranscriptions;
      try {
        uniqueTranscriptions = searchManager
                .getFilteredTranscriptions(clientId, projectId, datasets, 0, -1,
                        queryOperator, sortByList, searchRequest,
                        this.projectManager.getProjectLocale(clientId, projectId));
      } catch (QueryNodeException e1) {
        log.error("failed to export export unique transcript - " + projectId, e1);
        throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
                "invalid_search_query", e1.getLocalizedMessage()));
      }

      final ProjectBO project = this.projectManager.getProjectById(projectId);
      if (project == null) {
        throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
                "project_not_found", "ProjectId:'" + projectId + "' not found"));
      }

      Path jobPath = this.jobHelper
              .buildJobPath(Integer.toString(project.getClientId()), projectId, null);
      String filepath = jobPath.resolve("output-unique.csv").toString();


      WebApplicationException webApplicationException = null;
      try (final FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {
        // Write UTF-8 Signature
        fileOutputStream.write(ByteOrderMark.UTF_8.getBytes());
        try (final CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))) {
          creatUniqueCsv(onlyWithRuTagged, uniqueTranscriptions, writer);
        }

      } catch (Exception e) {
        log.error("failed to export export unique transcript - " + projectId + " - " + filepath, e);
        webApplicationException = new ServerErrorException(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new Error(
                                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                                ErrorMessage.FILE_ACCESS_MESSAGE))
                        .build(), e);
      }

      if (webApplicationException != null) {
        throw webApplicationException;
      }
      return new File(filepath);

    } catch (IndexNotFoundException e) {
      log.error("failed to export export unique transcript, index not found - " + projectId, e);
      throw new ServiceUnavailableException(
              Response.status(Response.Status.SERVICE_UNAVAILABLE)
                      .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                              null, ErrorMessage.SEARCH_UNAVAILABLE))
                      .build(), e);
    } catch (IOException ioe) {
      log.error("failed to export export unique transcript - " + projectId, ioe);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
              .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                      ErrorMessage.FILE_ACCESS_MESSAGE))
              .build(), ioe);
    }
  }

  @Override
  public File exportTaggingGuideForProject(String clientId, String projectId, String userEmail) {

    final ProjectBO project = this.validationManager.validateClientAndProject(clientId, projectId);
    try {
      String filePath = null;
      try {
        String fileName = String.format("%s_Tagging_Guide_%s.csv",
                URLEncoder.encode(project.getName(), "UTF-8"),
                System.currentTimeMillis());
        filePath = fileUploadHelper.getFileUploadPath(project, userEmail,
                "taggingguide", fileName, false);
      } catch (Exception e) {
        log.error("failed to export tagging guide - " + projectId, e);
        throw new ServerErrorException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Error(
                                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                                ErrorMessage.FILE_ACCESS_MESSAGE))
                        .build(), e);
      }

      WebApplicationException webApplicationException = null;
      try (final FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
        // Write UTF-8 Signature
        fileOutputStream.write(ByteOrderMark.UTF_8.getBytes());
        try (final CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))) {
          writer.writeNext(
                  new String[]{"Frequency", "% of Total", Constants.GRANULAR_INTENT_LABEL,
                          Constants.ROLL_UP_INTENT_LABEL, "Description", "Keywords",
                          "Examples", "Comments"});

          List<String> sortByList = new ArrayList<>();
          sortByList.add("asc:intent");
          List<TaggingGuideDocumentDetail> projectTags = searchManager
                  .getTaggingGuideDocumentsForProject(clientId, projectId,
                          sortByList);

          for (final TaggingGuideDocumentDetail tagRow : projectTags) {
            writer.writeNext(new String[]{"" + tagRow.getCount(),
                    "" + tagRow.getPercentage(), tagRow.getIntent(),
                    tagRow.getRutag(), tagRow.getDescription(),
                    tagRow.getKeywords(), tagRow.getExamples(),
                    tagRow.getComments()});
          }
        }
      } catch (Exception e) {
        log.error("failed to write tagging guide - " + projectId, e);
        webApplicationException = new ServerErrorException(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(new Error(
                                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
                                ErrorMessage.FILE_ACCESS_MESSAGE))
                        .build(), e);
      }

      if (webApplicationException != null) {
        throw webApplicationException;
      }
      return new File(filePath);

    } catch (IndexNotFoundException e) {
      log.error("failed to export tagging guide - " + projectId, e);
      throw new ServiceUnavailableException(
              Response.status(Response.Status.SERVICE_UNAVAILABLE)
                      .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                              null, ErrorMessage.SEARCH_UNAVAILABLE))
                      .build(), e);
    }
  }

  // As of 2018/06, we have mixed use TaskEventBO and JdbcTaskEventDao. If we set @Transactional,
  // it will create extra 'TaskEvent' in DB.
  @Override
  public TaskEventBO transform(final String clientId, final String projectId,
                               final String datasetId,
                               final String userEmail, Boolean useModelForSuggestedCategory) {

    final DatasetDetailsModel datasetDetailsModel = validationManager
            .validateClientProjectDataSet(clientId, projectId, datasetId);

    final DatasetBO dataset = datasetDetailsModel.getDatasetBO();

    final ProjectBO project = datasetDetailsModel.getProjectBO();

    //Default behavior
    if (useModelForSuggestedCategory == null) {
      useModelForSuggestedCategory = true;
    }
    final String uri = dataset.getUri();
    final String[] uriParts = uri.split("/");
    final String fileId = uriParts[uriParts.length - 1];
    final FileEntryDetail fileEntry = this.fileManager.getFileById(fileId);
    if (fileEntry == null) {
      throw new BadRequestException("Invalid file id: " + fileId + " and uri: " + uri);
    }

    final String username = AuthUtil.getPrincipalFromSecurityContext(null);
    final String fileName = fileEntry.getSystemName();

    final String repositoryRoot = this.appConfig.getFileUploadRepositoryRoot();
    final FileSystem fileSystem = FileSystems.getDefault();
    final Path userRoot = fileSystem.getPath(repositoryRoot, fileEntry.getUser());
    final Path filePath = fileSystem.getPath(userRoot.toString(), fileName);

    JobBO job = new JobBO();
    final long createdAt = Calendar.getInstance().getTimeInMillis();
    job.setDatasetId(Integer.parseInt(datasetId));
    job.setProjectId(Integer.parseInt(projectId));
    job.setFileName(fileName);
    job.setCreatedAt(createdAt);
    job.setCreatedBy(username);
    job.create();

    final String jobId = Integer.toString(job.getId());

    Path jobFilePath = null;
    Path jobRoot = null;

    // Create job folder to store files
    try {
      jobRoot = this.jobHelper
              .buildJobPath(Integer.toString(project.getClientId()), projectId, jobId);

      // copy dataset file into job path
      jobFilePath = Files.copy(filePath, jobRoot.resolve(filePath.getFileName()),
              StandardCopyOption.REPLACE_EXISTING);
      // create a copy of the original
      Files.copy(jobFilePath, jobRoot.resolve(fileId + ".original.csv"),
              StandardCopyOption.REPLACE_EXISTING);

    } catch (final IOException ioe) {
      log.error("failed to store files in job folders - " + projectId + " - " + filePath.toString(),
              ioe);
      throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, ioe);
    }

    Long recordsImported = 0l;
    List<String> columns = new ArrayList<>();
    try {
      CSVFileUtil.CSVFileInfo csvFileInfo =
              CSVFileUtil.validateAndGetColumns(jobFilePath.toString());
      columns.addAll(Arrays.asList(csvFileInfo.getColumns()));
      recordsImported = csvFileInfo.getValidRowCount();
    } catch (ApplicationException e) {
      log.warn("Failed to get number of records", e);
    }

    TaskEventBO task = new TaskEventBO();
    task.setRecordsImported(recordsImported);
    task.setJobId(Integer.parseInt(jobId));
    task.setTask(TaskEventBO.TaskType.CATEGORIZE);
    task.setStatus(TaskEventBO.Status.QUEUED);
    task.create();

    final List<TaskEventBO> remainingTasks = new ArrayList<>();

    TaskEventBO indexTask = new TaskEventBO();
    indexTask.setJobId(Integer.parseInt(jobId));
    indexTask.setTask(TaskEventBO.TaskType.INDEX);
    indexTask.setStatus(TaskEventBO.Status.QUEUED);
    indexTask.setRecordsImported(recordsImported);
    indexTask.create();

    remainingTasks.add(indexTask);

    String transcriptionColumn =
            this.appConfig.getCsvNormalizedTranscriptionColumnName();

    String originalTranscriptionColumn =
            this.appConfig.getCsvOriginalTranscriptionColumnName();

    final CategorizeJobDetail categorizeJobDetail =
            this.jobHelper.buildCategorizeJobDetail(jobFilePath, this.appConfig.getAutoCategorizerUrl() + jobId + "/file",
                    dataset.getLocale(), columns, transcriptionColumn, originalTranscriptionColumn,
                    useModelForSuggestedCategory, projectId, clientId);

    final Path confFileSystemPath = fileSystem.getPath(jobRoot.toString(), fileId + ".conf");
    final LogstashJobDetail logstashJobDetail =
            this.jobHelper.buildLogstashJobDetail(project, dataset, jobFilePath, confFileSystemPath, username);

    // create Task
    final JobDataMap jobDataMap = this.jobHelper
            .buildJobDataMap(categorizeJobDetail, logstashJobDetail, task, remainingTasks);

    final String jobName = String.format(this.ingestionPropertyConfig.getJobNameFormat(), jobId,
            task.getTask(), createdAt);
    final String group = this.elasticSearchProps.getQuartzJobGroup();
    final JobDetail jobDetail =
            JobBuilder.newJob(CategorizeJob.class).withIdentity(new JobKey(jobName, group))
                    .setJobData(jobDataMap).storeDurably(true).build();

    final String triggerName =
            String.format(this.ingestionPropertyConfig.getTriggerNameFormat(), jobId,
                    task.getTask(), createdAt);
    final Trigger categorizeTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerName, group).forJob(jobDetail).startNow().build();

    if (!this.jobHelper.scheduleJob(jobDetail, categorizeTrigger,
            this.ingestionPropertyConfig.getMaxRetryCount(),
            this.ingestionPropertyConfig.getStartingRetryDelayMs())) {
      if (!remainingTasks.isEmpty()) {
        for (final TaskEventBO remainingTask : remainingTasks) {
          remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
          remainingTask.setErrorCode();
          remainingTask.setMessage("");
          remainingTask.update();
        }
      }
      task.setStatus(TaskEventBO.Status.FAILED);
      task.setErrorCode();
      task.setMessage("Unable to schedule job");
      task.update();
      throw new ServerErrorException(task.getMessage(), Status.SERVICE_UNAVAILABLE);
    }

    return this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));

  }

  @Override
  public TaskEventBO status(final String clientId, final String projectId, final String datasetId) {

    if (!StringUtils.isEmpty(clientId)) {
      this.validationManager.validateClientProjectDataSet(clientId, projectId, datasetId);
    }

    TaskEventBO taskEventDetail = null;
    final JobBO job = this.jobManager.getJobByProjectDataset(projectId, datasetId);

    if (job != null) {
      taskEventDetail = this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));
    }

    return taskEventDetail;
  }

  @Override
  public TaskEventBO retryTransform(final String clientId, final String projectId,
                                    final String datasetId,
                                    final String userEmail, Boolean useModelForSuggestedCategory) {

    DatasetDetailsModel datasetDetailsModel = this.validationManager
            .validateClientProjectDataSet(clientId, projectId, datasetId);

    ProjectBO project = datasetDetailsModel.getProjectBO();

    DatasetBO dataset = datasetDetailsModel.getDatasetBO();

    if (dataset == null) {
      throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
              "dataset_not_found", "DatasetId:'" + datasetId + "' not found"));
    }

    //Default behavior
    if (useModelForSuggestedCategory == null) {
      useModelForSuggestedCategory = true;
    }
    final JobBO job = this.jobManager.getJobByProjectDataset(projectId, datasetId);
    if (job == null) {
      throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
              "transformation_job_not_found", "Transformation job for projectId:'" + projectId
              + "' datasetId:'" + datasetId + "' not found"));
    }

    final long modifiedAt = Calendar.getInstance().getTimeInMillis();
    job.setModifiedAt(modifiedAt);
    job.setModifiedBy(userEmail);
    job.update();

    final List<TaskEventBO> tasks = this.jobManager.getTasksForJob(Integer.toString(job.getId()));

    this.retryTransform(job, tasks, dataset, project, useModelForSuggestedCategory);
    return this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));
  }

  @Override
  public TaskEventBO retryTransform(final String jobId, final String userId) {

    final JobBO job = this.jobManager.getJobById(jobId);
    if (job == null) {
      throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
              "transformation_job_not_found", "Transformation job for id:'" + jobId + "'"));
    }

    final DatasetBO dataset = this.datasetManager
            .getDatasetById(Integer.toString(job.getDatasetId()));
    final ProjectBO project = this.projectManager
            .getProjectById(Integer.toString(job.getProjectId()));

    final long modifiedAt = Calendar.getInstance().getTimeInMillis();
    job.setModifiedAt(modifiedAt);
    job.setModifiedBy(userId);
    job.update();

    final List<TaskEventBO> tasks = this.jobManager.getTasksForJob(Integer.toString(job.getId()));
    this.retryTransform(job, tasks, dataset, project, Boolean.FALSE);

    return this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));
  }

  @Override
  public boolean retryTransform(final String jobId, final List<TaskEventBO> tasks) {

    try {
      final JobBO job = this.jobManager.getJobById(jobId);
      if (job == null || tasks.isEmpty()) {
        return false;
      }

      final DatasetBO dataset = this.datasetManager
              .getDatasetById(Integer.toString(job.getDatasetId()));
      final ProjectBO project = this.projectManager
              .getProjectById(Integer.toString(job.getProjectId()));

      final long modifiedAt = Calendar.getInstance().getTimeInMillis();
      job.setModifiedAt(modifiedAt);
      job.setModifiedBy(SYSTEM_USER);
      job.update();

      this.retryTransform(job, tasks, dataset, project, Boolean.FALSE);
    } catch (final Exception e) {
      AspectLogger.logException(e);
      return false;
    }
    return true;
  }

  @Override
  public void setSearchManager(final SearchManager searchManager) {
    this.searchManager = searchManager;
  }


  @Override
  @Transactional
  public void deleteFailedTransformation(String clientId, String projectId, String datasetId,
                                         String currentUserEmail) {

    DatasetDetailsModel datasetDetailsModel = this.validationManager
            .validateClientProjectDataSet(clientId, projectId, datasetId);
    ProjectBO project = datasetDetailsModel.getProjectBO();

    final JobBO job = this.jobManager.getJobByProjectDataset(projectId, datasetId);
    if (job == null) {
      throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
              "transformation_job_not_found", "Transformation job for projectId:'" + projectId
              + "' datasetId:'" + datasetId + "' not found"));
    }
    final String jobId = Integer.toString(job.getId());
    final List<TaskEventBO> tasks = this.jobManager.getTasksForJob(jobId);
    TaskEventBO unfinishedTask = null;
    for (final TaskEventBO taskEventDetail : tasks) {
      if (!taskEventDetail.isComplete() && !taskEventDetail.isCanceled()) {
        unfinishedTask = taskEventDetail;
        break;
      }
    }

    // No failed task, check if only canceled
    if (unfinishedTask == null) {
      for (final TaskEventBO taskEventDetail : tasks) {
        if (taskEventDetail.isCanceled()) {
          unfinishedTask = taskEventDetail;
          break;

        }
      }
    }

    // no unfinished, failed or canceled tasks
    if (unfinishedTask == null) {
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "dataset_already_transformed",
              "Dataset '" + job.getDatasetId() + "' already transformed for project "
                      + job.getProjectId() + ", please contact engineering"));
    }

    // make sure this project hasn't already been indexed
    final long untaggedTranscriptionsCount = this.searchManager
            .getUntaggedTranscriptionsCount(null, Integer.toString(project.getId()),
                    Collections.singletonList(Integer.toString(job.getDatasetId())));
    final long taggedTranscriptionsCount = this.searchManager
            .getTaggedTranscriptionsCount(null, Integer.toString(project.getId()),
                    Collections.singletonList(Integer.toString(job.getDatasetId())));
    final long totalCount = untaggedTranscriptionsCount + taggedTranscriptionsCount;
    if (totalCount > 0) {
      log.warn("Found {} indexed documents", totalCount);
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "dataset_already_indexed",
              "Dataset '" + job.getDatasetId() + "' already indexed for project "
                      + job.getProjectId() + ", please contact engineering"));
    }

    this.jobManager.deleteTasksForJobId(jobId);
    job.delete();

    try {
      Path buildJobPath = this.jobHelper
              .buildJobPath(Integer.toString(project.getClientId()), projectId, jobId);
      FileUtils.cleanDirectory(buildJobPath.toFile());
      Files.delete(buildJobPath);
    } catch (IOException e) {
      log.error("failed to delete failed job, index not found - " + projectId + " - " + jobId, e);
      throw new ServerErrorException(ErrorMessage.FILE_ACCESS_MESSAGE, Status.INTERNAL_SERVER_ERROR,
              e);
    }

  }

  @Override
  public DatasetTaskStatusResponse transformStatusForDatasets(List<String> datasetIds) {

    return this.jobManager.getTransformStatusForDatasets(datasetIds);
  }

  @Override
  public TaskEventBO cancelTransformation(final String clientId, String projectId,
                                          String datasetId) {

    this.validationManager.validateClientProjectDataSet(clientId, projectId, datasetId);

    boolean inturrupted = false;
    TaskEventBO taskEventDetail = null;
    TaskEventBO canceledTask = null;
    final JobBO job = this.jobManager.getJobByProjectDataset(projectId, datasetId);

    if (job != null) {
      taskEventDetail = this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));
      final String jobName = String
              .format(this.ingestionPropertyConfig.getJobNameFormat(), job.getId(),
                      taskEventDetail.getTask(), taskEventDetail.getCreatedAt());
      final String group = this.elasticSearchProps.getQuartzJobGroup();

      JobKey jobKey = new JobKey(jobName, group);
      try {
        inturrupted = this.scheduler.interrupt(jobKey);
        if (inturrupted) {
          canceledTask = this.jobManager.getLatestTaskEventByJobId(Integer.toString(job.getId()));
        }
      } catch (UnableToInterruptJobException e) {
        log.error("Failed to interrupt job", e);
      }
    }
    return canceledTask;
  }

  @Override
  public ProjectTaskStatusResponse transformStatusForProjects(String clientId,
                                                              Map<String, List<String>> projectDatasets) {

    for (String projectId : projectDatasets.keySet()) {
      this.validationManager.validateClientAndProject(clientId, projectId);
    }
    return this.jobManager.getTransformStatusForProjects(projectDatasets);
  }

  private void creatUniqueCsv(Boolean onlyWithRuTagged,
                              TranscriptionDocumentDetailCollection uniqueTranscriptions,
                              CSVWriter writer) {

    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String countLabel = "count";
    String transcriptionLabel = "transcription";
    String granularLabel = Constants.GRANULAR_INTENT_LABEL;
    String ruLabel = Constants.ROLL_UP_INTENT_LABEL;

    writer.writeNext(
            new String[]{ruLabel, transcriptionLabel,
                    transcriptionHashLabel, countLabel, granularLabel});

    for (final TranscriptionDocumentDetail transcription : uniqueTranscriptions
            .getTranscriptionList()) {

      if (Boolean.FALSE.equals(onlyWithRuTagged) || !transcription.getRutag().equals("")) {
        String text =
                (transcription.getTextStringForTagging() == null
                        || transcription.getTextStringForTagging().isEmpty()) ?
                        null :
                        transcription.getTextStringForTagging();

        writer.writeNext(new String[]{
                transcription.getRutag(), text,
                transcription.getTranscriptionHash(),
                transcription.getDocumentCount().toString(),
                transcription.getIntent() == null ? "" :
                        transcription.getIntent()});
      }
    }
  }
}
