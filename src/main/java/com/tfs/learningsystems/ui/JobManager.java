/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import java.util.List;
import java.util.Map;

public interface JobManager {

  public JobBO getJobById(String jobId);

  public JobBO getJobByProjectDataset(String projectId, String datasetId);

  public Map<String, List<TaskEventBO>> getUnfinishedJobs();

  public Map<String, List<TaskEventBO>> getFailedJobs();

  public TaskEventBO getLatestTaskEventByJobId(String jobId);

  public boolean updateTaskEvent(TaskEventBO taskEvent);

  public List<TaskEventBO> getTasksForJob(String jobId);

  public void deleteTasksForJobId(String jobId);

  public DatasetTaskStatusResponse getTransformStatusForDatasets(List<String> datasetIds);

  public ProjectTaskStatusResponse getTransformStatusForProjects(
      Map<String, List<String>> projectDatasets);
}
