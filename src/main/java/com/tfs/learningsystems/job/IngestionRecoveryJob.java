/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.DataManagementManager;
import com.tfs.learningsystems.ui.JobManager;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class IngestionRecoveryJob implements Job {

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  private DataManagementManager dataManagementManager;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    String jobId = null;
    try {
      log.info("Attempting to recover unfinished jobs");
      Map<String, List<TaskEventBO>> unfinishedJobs = jobManager.getUnfinishedJobs();
      if (unfinishedJobs.isEmpty()) {
        log.info("No Jobs to recover");
      } else {
        for (Entry<String, List<TaskEventBO>> entry : unfinishedJobs.entrySet()) {
          jobId = entry.getKey();
          log.info("Recovering Job " + jobId);
          if (!dataManagementManager.retryTransform(entry.getKey(), entry.getValue())) {
            log.warn("Failed to recover Job {}", jobId);
          }
        }
      }

      // Removing retrying failed jobs on startup, only recovering unfinished jobs.

    } catch (Exception e) {
      log.warn("Failed to recover Job {} - cause : {}", jobId, e.getMessage());
      throw new JobExecutionException(e, false);
    }

  }

}
