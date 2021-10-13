/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import com.tfs.learningsystems.logging.AspectLogger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class IngestionSchedulerListener implements SchedulerListener {

  private static final String QUARTZ_SCHEDULER = "[QUARTZ SCHEDULER]";

  @Override
  public void jobScheduled(Trigger trigger) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobScheduled",
        "Job " + trigger.getKey().toString() + " sheduled");
  }

  @Override
  public void jobUnscheduled(TriggerKey triggerKey) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobUnscheduled",
        "Job " + triggerKey.toString() + " unsheduled");
  }

  @Override
  public void triggerFinalized(Trigger trigger) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "triggerFinalized",
        "Trigger " + trigger.getKey().toString() + " finalized");
  }

  @Override
  public void triggerPaused(TriggerKey triggerKey) {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "jobUnscheduled", "Job " + triggerKey.toString() + " paused");

  }

  @Override
  public void triggersPaused(String triggerGroup) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobUnscheduled",
        "Trigger Group " + triggerGroup.toString() + " paused");

  }

  @Override
  public void triggerResumed(TriggerKey triggerKey) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobUnscheduled",
        "Job " + triggerKey.toString() + " resumed");

  }

  @Override
  public void triggersResumed(String triggerGroup) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobUnscheduled",
        "Trigger Group " + triggerGroup.toString() + " resumed");
  }

  @Override
  public void jobAdded(JobDetail jobDetail) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobAdded",
        "Job " + jobDetail.getKey().toString() + " added");
  }

  @Override
  public void jobDeleted(JobKey jobKey) {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "jobDeleted", "Job " + jobKey.toString() + " deleted");
  }

  @Override
  public void jobPaused(JobKey jobKey) {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "jobPaused", "Job " + jobKey.toString() + " paused");
  }

  @Override
  public void jobsPaused(String jobGroup) {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "jobsPaused", "Job " + jobGroup.toString() + " paused");
  }

  @Override
  public void jobResumed(JobKey jobKey) {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "jobResumed", "Job " + jobKey.toString() + " resumed");
  }

  @Override
  public void jobsResumed(String jobGroup) {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "jobsResumed", "Job " + jobGroup.toString() + " resumed");
  }

  @Override
  public void schedulerError(String msg, SchedulerException cause) {
    AspectLogger.logException(cause);

  }

  @Override
  public void schedulerInStandbyMode() {
    AspectLogger
        .logMessage(QUARTZ_SCHEDULER, "schedulerInStandbyMode", "Scheduler In Standby Mode");
  }

  @Override
  public void schedulerStarted() {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "schedulerStarted", "scheduler Started");

  }

  @Override
  public void schedulerStarting() {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "schedulerStarting", "scheduler Starting");
  }

  @Override
  public void schedulerShutdown() {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "schedulerShutdown", "scheduler Shutdown");
  }

  @Override
  public void schedulerShuttingdown() {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "schedulerShuttingdown", "scheduler Shuttingdown");

  }

  @Override
  public void schedulingDataCleared() {
    AspectLogger.logMessage(QUARTZ_SCHEDULER, "schedulingDataCleared", "scheduling Data Cleared");

  }

}
