/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.helper;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.config.IngestionPropertyConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.job.DeleteDataJob;
import com.tfs.learningsystems.ui.model.CategorizeJobDetail;
import com.tfs.learningsystems.ui.model.LogstashJobDetail;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.util.QuartzJobUtil;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobHelper {

  @Autowired
  private IngestionPropertyConfig ingestionPropertyConfig;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private Scheduler scheduler;

  public JobDetailFactoryBean createJobDetail(Class<?> jobClass) {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass((Class<? extends Job>) jobClass);
    // job has to be durable to be stored in DB:
    factoryBean.setDurability(true);
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT,
        ingestionPropertyConfig.getStartingRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT,
        ingestionPropertyConfig.getMaxRetryCount());
    factoryBean.setJobDataMap(jobDataMap);
    return factoryBean;
  }

  public Trigger createTrigger(JobDetail jobDetail, Boolean delay) {
    TriggerBuilder<Trigger> trigger = TriggerBuilder.newTrigger()
        .withIdentity(jobDetail.getKey().getName()).forJob(jobDetail);
    if (delay) {
      Calendar instance = Calendar.getInstance();
      Long delayedStart = instance.getTimeInMillis() + ingestionPropertyConfig.getStartingDelayMs();
      instance.setTimeInMillis(delayedStart);
      trigger.startAt(instance.getTime());
    } else {
      trigger.startNow();
    }

    return trigger.build();
  }

  public JobDetail createDeleteDataJobDetail(String projectId, String datasetId) {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT,
        ingestionPropertyConfig.getStartingRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT,
        ingestionPropertyConfig.getMaxRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_PROJECT_ID, projectId);
    jobDataMap.put(QuartzJobUtil.KEY_DATASET_ID, datasetId);
    String name = DeleteDataJob.class.getName() + "_" + System.currentTimeMillis();
    JobDetail detail = JobBuilder.newJob(DeleteDataJob.class).setJobData(jobDataMap)
        .storeDurably(true).withIdentity(name).build();

    return detail;
  }


  public LogstashJobDetail buildLogstashJobDetail(final ProjectBO project,
      final DatasetBO dataset, final Path jobFilePath, final Path confFileSystemPath,
      final String username) {
    final int elasticSearchApiPort = this.elasticSearchProps.getApiPort();
    final List<String> elasticSearchHostUrls = this.elasticSearchProps.getHosts().stream()
        .map(host -> String.format("%s:%d", host, elasticSearchApiPort))
        .collect(Collectors.toList());
    final LogstashJobDetail logstashJobDetail = new LogstashJobDetail();
    logstashJobDetail.setUsername(username);
    logstashJobDetail.setProjectId(project.getId());
    logstashJobDetail.setVertical(project.getVertical());
    logstashJobDetail.setDataType(dataset.getDataType().toString());
    logstashJobDetail.setClientId(Integer.parseInt(dataset.getClientId()));
    logstashJobDetail.setDatasetId(dataset.getId());
    logstashJobDetail.setCsvFilePath(jobFilePath.toString());
    logstashJobDetail.setConfFilePath(confFileSystemPath.toString());

    logstashJobDetail.setOriginalTranscriptionColumnName(
        this.appConfig.getCsvOriginalTranscriptionColumnName());
    logstashJobDetail.setNormalizedTranscriptionColumnName(
        this.appConfig.getCsvNormalizedTranscriptionColumnName());
    logstashJobDetail.setTranscriptionEntityColumnName(
        this.appConfig.getCsvTranscriptionEntityColumnName());

    logstashJobDetail.setLogstashCheckExecTimeout(this.appConfig.getCheckLogstashExecTimeout());
    logstashJobDetail.setLogstashExecTimeout(this.appConfig.getLogstashExecTimeout());
    logstashJobDetail.setElasticSearchHosts(elasticSearchHostUrls);
    logstashJobDetail.setElasticSearchIndexName(this.elasticSearchProps.getNltoolsIndexAlias());
    logstashJobDetail
        .setElasticSearchIndexType(this.elasticSearchProps.getDefaultDocumentIndexType());

    return logstashJobDetail;
  }

  public CategorizeJobDetail buildCategorizeJobDetail(final Path jobFilePath,
      final String categorizerUrl, final String locale, final List<String> columns,
      final String transcriptionColumn, final String originalTranscriptionColumn,
      final Boolean useModelForSuggestedCategory,
      final String projectId, String clientId) {
    final CategorizeJobDetail categorizeJobDetail = new CategorizeJobDetail();
    categorizeJobDetail.setCsvFilePath(jobFilePath.toString());
    categorizeJobDetail.setCategorizeServicePath(categorizerUrl);
    categorizeJobDetail.setDatasetLocale(locale);
    categorizeJobDetail.setColumns(columns);
    categorizeJobDetail.setTranscriptionColumn(transcriptionColumn);
    categorizeJobDetail.setOriginalTranscriptionColumn(originalTranscriptionColumn);
    categorizeJobDetail.setUseModelForSuggestedCategory(useModelForSuggestedCategory);
    categorizeJobDetail.setProjectId(projectId);
    categorizeJobDetail.setClientId(clientId);
    return categorizeJobDetail;
  }

  public JobDataMap buildJobDataMap(final CategorizeJobDetail categorizeJobDetail,
      final LogstashJobDetail logstashJobDetail, final TaskEventBO task,
      final List<TaskEventBO> remainingTasks) {
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT,
        this.ingestionPropertyConfig.getStartingRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT,
        this.ingestionPropertyConfig.getMaxRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZE_JOB_DETAIL, categorizeJobDetail);
    jobDataMap.put(QuartzJobUtil.KEY_LOGSTASH_JOB_DETAIL, logstashJobDetail);
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, task);
    jobDataMap.put(QuartzJobUtil.KEY_REMAINING_TASKS, remainingTasks);
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_READ_TIMEOUT,
        this.appConfig.getCategorizerReadTimeout());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_CONNECT_TIMEOUT,
        this.appConfig.getCategorizerConnectTimeout());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_CONNECTION_REQUEST_TIMEOUT,
        this.appConfig.getCategorizerConnectionRequestTimeout());

    return jobDataMap;
  }

  public JobDataMap buildJobDataMap(final CategorizeJobDetail categorizeJobDetail,
      final LogstashJobDetail logstashJobDetail, final TaskEventDetail task,
      final List<TaskEventDetail> remainingTasks) {
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT,
        this.ingestionPropertyConfig.getStartingRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_MAX_RETRY_COUNT,
        this.ingestionPropertyConfig.getMaxRetryCount());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZE_JOB_DETAIL, categorizeJobDetail);
    jobDataMap.put(QuartzJobUtil.KEY_LOGSTASH_JOB_DETAIL, logstashJobDetail);
    jobDataMap.put(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, task);
    jobDataMap.put(QuartzJobUtil.KEY_REMAINING_TASKS, remainingTasks);
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_READ_TIMEOUT,
        this.appConfig.getCategorizerReadTimeout());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_CONNECT_TIMEOUT,
        this.appConfig.getCategorizerConnectTimeout());
    jobDataMap.put(QuartzJobUtil.KEY_CATEGORIZER_CONNECTION_REQUEST_TIMEOUT,
        this.appConfig.getCategorizerConnectionRequestTimeout());

    return jobDataMap;
  }

  public boolean scheduleJob(final JobDetail jobDetail, final Trigger trigger) {
    return this.scheduleJob(jobDetail, trigger, this.ingestionPropertyConfig.getMaxRetryCount(),
        this.ingestionPropertyConfig.getStartingRetryDelayMs());
  }

  public boolean scheduleJob(final JobDetail jobDetail, final Trigger trigger,
      int retriesRemaining, final long retryDelay) {
    try {
      this.scheduler.scheduleJob(jobDetail, trigger);
    } catch (final SchedulerException e) {
      if (retriesRemaining > 0) {
        log.error(e.getMessage(), e);
        log.error(String.format("unable to schedule job %s with trigger %s, trying %s more times",
            jobDetail.getKey(), trigger.getKey(), retriesRemaining));
        try {
          Thread.sleep(retryDelay);
        } catch (final InterruptedException ie) {
          log.error("Got interrupted while waiting to retry sheduling job");
        }
        return this.scheduleJob(jobDetail, trigger, --retriesRemaining, (retryDelay * 2));
      } else {
        return false;
      }
    }
    return true;
  }

  public Path buildJobPath(final String clientId, final String projectId, final String jobId)
      throws IOException {

    final String repositoryRoot = this.appConfig.getFileUploadRepositoryRoot();
    final FileSystem fileSystem = FileSystems.getDefault();
    final Path jobPath;

    if (jobId != null) {

      jobPath = fileSystem
          .getPath(repositoryRoot, "jobs", "client" + clientId,
              "project" + projectId, "job" + jobId);
    } else {

      jobPath = fileSystem
          .getPath(repositoryRoot, "jobs", "client" + clientId,
              "project" + projectId);
    }
    if (Files.notExists(jobPath, LinkOption.NOFOLLOW_LINKS)) {
      if(System.getProperty("os.name").contains("Windows")){
        Files.createDirectories(jobPath);
      }else {
        final Set<PosixFilePermission> permissions =
                PosixFilePermissions.fromString("rwxr-xr-x");
        final FileAttribute<Set<PosixFilePermission>> fileAttributes =
                PosixFilePermissions.asFileAttribute(permissions);
        Files.createDirectories(jobPath, fileAttributes);
      }
    }

    return jobPath;
  }
}
