/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.tfs.learningsystems.util.QuartzJobUtil;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class DeleteFileJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    int currentRetryCount = jobDataMap.getInt(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT);
    String filePath = jobDataMap.getString(QuartzJobUtil.KEY_SYSTEM_PATH);
    int maxRetryCount = jobDataMap.getInt(QuartzJobUtil.KEY_MAX_RETRY_COUNT);
    try {
      Files.delete(FileSystems.getDefault().getPath(filePath));
      log.info("Successfully deleted file " + filePath);
    } catch (IOException e) {
      currentRetryCount++;
      String message = "Failed deleting the file:" + filePath;
      log.error(message, e);
      JobExecutionException jobException = new JobExecutionException(message);
      jobDataMap.put(QuartzJobUtil.KEY_CURRENT_RERTY_COUNT, currentRetryCount);
      if (currentRetryCount < maxRetryCount) {
        try {
          log.info("Waiting for {} before refiring again",
              QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE);
          Thread.sleep(QuartzJobUtil.KEY_DEFAULT_WAIT_TIME_TO_REFIRE);
        } catch (InterruptedException e1) {
          log.warn("Woke up from sleep before firing delete job for file {} again", filePath);
        }
        jobException.setRefireImmediately(true);
      }
      throw jobException;
    }

  }

}
