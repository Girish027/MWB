/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import java.util.List;
import java.util.Map;

public interface TaskEventDao {

  public Map<String, List<TaskEventBO>> getUnfinishedTaskEvents();

  public Map<String, List<TaskEventBO>> getFailedTaskEvents();

  public DatasetTaskStatusResponse getLatestTaskEventStatusForDatasets(List<String> datasets);

  public ProjectTaskStatusResponse getLatestTaskEventStatusForProjects(
      Map<String, List<String>> project);
}
