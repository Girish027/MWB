/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import java.io.File;
import java.util.List;
import java.util.Map;


public interface DataManagementManager {

  public File export(String clientId, String projectId, List<String> datasets, String queryOperator,
      Boolean onlyTagged, Boolean onlyWithRuTagged, Boolean modelBuilding, String modelType);

  public File exportUnique(String clientId, String projectId, List<String> datasets,
      String queryOperator, Boolean onlyTagged, Boolean onlyWithRuTagged);

  public File exportTaggingGuideForProject(String clientId, String projectId, String userId);

  public TaskEventBO transform(String clientId, String projectId, String datasetId, String userId,
      Boolean useModelForSuggestedCategory);

  public TaskEventBO status(String clientId, String projectId, String datasetId);

  public TaskEventBO retryTransform(String clientId, String projectId, String datasetId,
      String userId, Boolean useModelForSuggestedCategory);

  public TaskEventBO retryTransform(String jobId, String userId);

  public boolean retryTransform(String jobId, List<TaskEventBO> tasks);

  public void setSearchManager(SearchManager searchManager);

  public void deleteFailedTransformation(String clientId, String projectId, String datasetId,
      String currentUserId);

  public DatasetTaskStatusResponse transformStatusForDatasets(List<String> datasetIds);

  public TaskEventBO cancelTransformation(String clientId, String projectId, String datasetId);

  public ProjectTaskStatusResponse transformStatusForProjects(String clientId,
      Map<String, List<String>> projects);
}
