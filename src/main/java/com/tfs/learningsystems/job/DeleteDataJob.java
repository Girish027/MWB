/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.util.QuartzJobUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class DeleteDataJob implements Job {

  @Autowired
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  private AppConfig appConfig;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    int currentRetryCount = jobDataMap.getInt(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT);
    String clientId = jobDataMap.getString(QuartzJobUtil.KEY_CLIENT_ID);
    String projectId = jobDataMap.getString(QuartzJobUtil.KEY_PROJECT_ID);
    String datasetId = jobDataMap.getString(QuartzJobUtil.KEY_DATASET_ID);
    int maxRetryCount = jobDataMap.getInt(QuartzJobUtil.KEY_MAX_RETRY_COUNT);
    try {
      log.info("Executing delete data job");

      contentManager.deleteRecords(clientId, projectId, datasetId);

      log.info("Completed deleta data job");
    } catch (Exception e) {
      currentRetryCount++;
      String message =
          "Failed deleting the data for project " + projectId + " and dataset " + datasetId;
      log.error(message, e);
      JobExecutionException jobException = new JobExecutionException(message, e);
      jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, currentRetryCount);
      if (currentRetryCount < maxRetryCount) {
        try {
          log.info("Waiting for {} before refiring again",
              QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE);
          Thread.sleep(QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE);
        } catch (InterruptedException e1) {
          log.warn("Woke up from sleep before firing delete job for data for project " + projectId
              + " and dataset " + datasetId);
        }
        jobException.setRefireImmediately(true);
      }
      throw jobException;
    }

  }

}
