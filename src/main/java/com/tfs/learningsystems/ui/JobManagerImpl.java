/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.config.IngestionPropertyConfig;
import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.dao.TaskEventDao;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@Qualifier("jobManagerBean")
@Slf4j

public class JobManagerImpl implements JobManager {

  @Inject
  @Qualifier("taskEventDaoBean")
  private TaskEventDao taskEventDao;

  @Autowired
  private IngestionPropertyConfig ingestionPropertyConfig;

  @Override
  public JobBO getJobById(String jobId) {
    JobBO job = new JobBO();
    job = job.findOne(jobId);
    return (job);
  }

  @Override
  public JobBO getJobByProjectDataset(String projectId, String datasetId) {
    JobBO job = new JobBO();
    job = job.getJobByProjectDataset(projectId, datasetId);
    return (job);
  }

  @Override
  public Map<String, List<TaskEventBO>> getUnfinishedJobs() {
    return this.taskEventDao.getUnfinishedTaskEvents();
  }

  @Override
  public Map<String, List<TaskEventBO>> getFailedJobs() {
    return this.taskEventDao.getFailedTaskEvents();
  }

  @Override
  public TaskEventBO getLatestTaskEventByJobId(String jobId) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(TaskEventBO.FLD_JOB_ID, jobId);
    Sort sort = Sort.by(Sort.Direction.DESC, new String[]{TaskEventBO.OBJ_ID});

    TaskEventBO eventBO = new TaskEventBO();
    eventBO = eventBO.findOne(paramMap, sort);
    return (eventBO);
  }

  @Override
  public List<TaskEventBO> getTasksForJob(String jobId) {
    TaskEventBO eventBO = new TaskEventBO();
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(TaskEventBO.FLD_JOB_ID, jobId);
    Sort sort = Sort.by(Sort.Direction.DESC, new String[]{TaskEventBO.OBJ_ID});

    return (eventBO.list(paramMap, sort));
  }

  @Override
  public boolean updateTaskEvent(TaskEventBO taskEvent) {
    return this.updateTask(taskEvent, ingestionPropertyConfig.getMaxRetryCount(),
        ingestionPropertyConfig.getStartingRetryDelayMs());
  }

  private boolean updateTask(final TaskEventBO task, int retriesRemaining, long retryDelay) {
    if (retriesRemaining > 0) {
      try {
        task.update();
        return true;
      } catch (Exception e) {
        log.error("unable to update task {}, trying {} more times", task.getTask(),
            retriesRemaining);
        log.error(e.getMessage(), e);
        try {
          Thread.sleep(retryDelay);
        } catch (InterruptedException ie) {
          log.error("Got interrupted while waiting to retry updating task");
        }
        return this.updateTask(task, --retriesRemaining, (retryDelay * 2));
      }
    } else {
      return false;
    }

  }

  @Override
  public void deleteTasksForJobId(String jobId) {
    TaskEventBO eventBO = new TaskEventBO();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(TaskEventBO.FLD_JOB_ID, jobId);
    Sort sort = Sort.by(Sort.Direction.DESC, new String[]{TaskEventBO.OBJ_ID});

    eventBO = eventBO.findOne(paramMap, sort);
    if (eventBO != null) {
      eventBO.delete();
    }

  }

  @Override
  public DatasetTaskStatusResponse getTransformStatusForDatasets(List<String> datasetIds) {
    return this.taskEventDao.getLatestTaskEventStatusForDatasets(datasetIds);
  }

  @Override
  public ProjectTaskStatusResponse getTransformStatusForProjects(
      Map<String, List<String>> projectDatasets) {
    return this.taskEventDao.getLatestTaskEventStatusForProjects(projectDatasets);
  }
}
