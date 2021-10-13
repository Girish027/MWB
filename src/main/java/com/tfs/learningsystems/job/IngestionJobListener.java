/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.logging.AspectLogger;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.util.QuartzJobUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class IngestionJobListener implements JobListener {

  private static final String QUARTZ_JOB = "[QUARTZ LISTENER]";
  public static final String LISTENER_NAME = "ingestionJobListener";

  private JobManager jobManager;
  private String ingestionGroup;

  @Autowired
  @Qualifier("jobManagerBean")
  public void setJobManager(JobManager jobManager) {
    this.jobManager = jobManager;
  }

  public void setIngestionGroup(String ingestionGroup) {
    this.ingestionGroup = ingestionGroup;
  }

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    String jobName = context.getJobDetail().getKey().toString();
    AspectLogger.logMessage(QUARTZ_JOB, "jobToBeExecuted",
        "Job : " + jobName + " is going to start...");
    JobDetail jobDetail = context.getJobDetail();
    if (jobDetail.getKey().getGroup().equalsIgnoreCase(ingestionGroup)) {
      TaskEventBO task = (TaskEventBO) jobDetail.getJobDataMap()
          .getOrDefault(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, null);
      if (task != null) {
        task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
        task.setMessage("");
        task.setStatus(TaskEventBO.Status.STARTED);
        task.setErrorCode(TaskEventBO.ErrorCode.OK.toString());
        jobManager.updateTaskEvent(task);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void jobExecutionVetoed(JobExecutionContext context) {
    String jobName = context.getJobDetail().getKey().toString();
    AspectLogger.logMessage(QUARTZ_JOB, "jobExecutionVetoed",
        "Job : " + jobName + " was vetoed...");
    JobDetail jobDetail = context.getJobDetail();
    if (jobDetail.getKey().getGroup().equalsIgnoreCase(ingestionGroup)) {
      TaskEventBO task = (TaskEventBO) jobDetail.getJobDataMap()
          .getOrDefault(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, null);
      if (task != null) {
        long modifiedAt = Calendar.getInstance().getTimeInMillis();
        task.setModifiedAt(modifiedAt);
        task.setMessage("Task Cancelled");
        task.setStatus(TaskEventBO.Status.CANCELLED);
        task.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
        jobManager.updateTaskEvent(task);

        List<TaskEventBO> remainingTasks = (List<TaskEventBO>) jobDetail
            .getJobDataMap().getOrDefault(QuartzJobUtil.KEY_REMAINING_TASKS,
                new ArrayList<TaskEventDetail>());
        if (!remainingTasks.isEmpty()) {
          for (TaskEventBO remainingTask : remainingTasks) {
            remainingTask.setModifiedAt(modifiedAt);
            remainingTask.setMessage("Task Cancelled");
            remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
            remainingTask.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
            jobManager.updateTaskEvent(remainingTask);
          }
        }

      }
    }

  }

  @SuppressWarnings("unchecked")
  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    String jobName = context.getJobDetail().getKey().toString();
    AspectLogger.logMessage(QUARTZ_JOB, "jobWasExecuted",
        "Job : " + jobName + " is finished...");
    JobDetail jobDetail = context.getJobDetail();

    if (jobDetail.getKey().getGroup().equalsIgnoreCase(ingestionGroup)) {

      TaskEventBO task = (TaskEventBO) jobDetail.getJobDataMap()
          .getOrDefault(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, null);
      if (task != null) {
        long modifiedAt = Calendar.getInstance().getTimeInMillis();
        // Job Failed
        if (jobException != null) {

          Boolean cancelled = context.getResult() != null ? (Boolean) context.getResult() : false;
          if (cancelled) {
            AspectLogger.logMessage(QUARTZ_JOB, "jobWasExecuted",
                "Job : " + jobName + " cancelled...");
            List<TaskEventBO> remainingTasks = (List<TaskEventBO>) jobDetail
                .getJobDataMap().getOrDefault(QuartzJobUtil.KEY_REMAINING_TASKS,
                    new ArrayList<TaskEventDetail>());
            if (!remainingTasks.isEmpty()) {
              for (TaskEventBO remainingTask : remainingTasks) {
                remainingTask.setModifiedAt(modifiedAt);
                remainingTask.setMessage("Task Cancelled");
                remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
                remainingTask.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
                jobManager.updateTaskEvent(remainingTask);
              }
            }

            task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
            task.setMessage("Task Cancelled");
            task.setStatus(TaskEventBO.Status.CANCELLED);
            task.setErrorCode(task.getFailedErrorCodeForTask().toString());

            jobManager.updateTaskEvent(task);
          } else {
            AspectLogger.logMessage(QUARTZ_JOB, "jobWasExecuted",
                "Job : " + jobName + " failed...");
            List<TaskEventBO> remainingTasks = (List<TaskEventBO>) jobDetail
                .getJobDataMap().getOrDefault(QuartzJobUtil.KEY_REMAINING_TASKS,
                    new ArrayList<TaskEventDetail>());
            if (!remainingTasks.isEmpty()) {
              for (TaskEventBO remainingTask : remainingTasks) {
                remainingTask.setModifiedAt(modifiedAt);
                remainingTask.setMessage("");
                remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
                remainingTask.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
                jobManager.updateTaskEvent(remainingTask);
              }
            }

            task.setModifiedAt(Calendar.getInstance().getTimeInMillis());
            task.setMessage(jobException.getMessage());
            task.setStatus(TaskEventBO.Status.FAILED);
            task.setErrorCode(task.getFailedErrorCodeForTask().toString());

            jobManager.updateTaskEvent(task);

            AspectLogger.logException(jobException);
          }

        } else {
          // Job Completed
          task.setModifiedAt(modifiedAt);
          task.setMessage("");
          task.setStatus(TaskEventBO.Status.COMPLETED);
          task.setErrorCode(TaskEventBO.ErrorCode.OK.toString());
          jobManager.updateTaskEvent(task);
          List<TaskEventBO> remainingTasks = (List<TaskEventBO>) jobDetail
              .getJobDataMap().getOrDefault(QuartzJobUtil.KEY_REMAINING_TASKS,
                  new ArrayList<TaskEventDetail>());
          if (remainingTasks.isEmpty()) {
            AspectLogger.logMessage(QUARTZ_JOB, "jobWasExecuted",
                "All tasks completed");
          } else {
            TaskEventBO nextTask = remainingTasks.remove(0);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();

            if (nextTask.getTask().equals(TaskEventBO.TaskType.INDEX.toString())) {
              AspectLogger.logMessage(QUARTZ_JOB, "jobWasExecuted",
                  "Starting " + nextTask.getTask() + " task");
              jobDataMap.replace(QuartzJobUtil.KEY_CURRENT_TASK_DETAIL, nextTask);
              jobDataMap.replace(QuartzJobUtil.KEY_REMAINING_TASKS, remainingTasks);
              String name = String.format("%s_%s_%d", nextTask.getJobId(),
                  nextTask.getTask(),
                  nextTask.getCreatedAt());
              JobDetail nextJobDetail = JobBuilder.newJob(LogstashJob.class)
                  .withIdentity(new JobKey(name, jobDetail.getKey().getGroup()))
                  .setJobData(jobDataMap).storeDurably(true).build();

              String triggerName = String.format("%s_%s_%d_TRIGGER",
                  nextTask.getJobId(), nextTask.getTask(),
                  Calendar.getInstance().getTimeInMillis());
              Trigger trigger = TriggerBuilder.newTrigger()
                  .withIdentity(triggerName, jobDetail.getKey().getGroup())
                  .forJob(nextJobDetail).startNow().build();

              nextTask.setModifiedAt(Calendar.getInstance().getTimeInMillis());
              jobManager.updateTaskEvent(nextTask);

              try {
                context.getScheduler().scheduleJob(nextJobDetail, trigger);
              } catch (SchedulerException e) {
                log.error("Failed to schedule ingestion job ", e);
                if (!remainingTasks.isEmpty()) {
                  for (TaskEventBO remainingTask : remainingTasks) {
                    remainingTask.setModifiedAt(modifiedAt);
                    remainingTask.setMessage("");
                    remainingTask.setStatus(TaskEventBO.Status.CANCELLED);
                    remainingTask.setErrorCode(TaskEventBO.ErrorCode.CANCELLED.toString());
                    jobManager.updateTaskEvent(remainingTask);
                  }
                }

                nextTask.setModifiedAt(Calendar.getInstance().getTimeInMillis());
                nextTask.setMessage(e.getMessage());
                nextTask.setStatus(TaskEventBO.Status.FAILED);
                nextTask.setErrorCode(task.getFailedErrorCodeForTask().toString());

                jobManager.updateTaskEvent(nextTask);
              }
            }
          }
        }
      }
    }

  }
}
