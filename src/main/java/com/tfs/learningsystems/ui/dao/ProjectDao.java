/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.ProjectDetailDatasetTaskStatus;
import java.util.List;

public interface ProjectDao {

  public List<ProjectDetailDatasetTaskStatus> getProjects(int startIndex,
      int count, String filter, String sortBy,
      String sortOrder, boolean showDeleted, String filterClientId, List<Integer> projectIDs);

  public List<ProjectDetailDatasetTaskStatus> getProjectDetailsByProjectIDs(List<Integer> projectIdList);

  List<ProjectBO> getProjectsByProjectIDs(List<Integer> projectIdList);
}
