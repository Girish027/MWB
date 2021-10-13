/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.db.DatasetBO;
import java.util.List;

public interface ProjectDatasetDao {

  public Integer countDatasets(String projectId);

  public List<Integer> getDatasetIds(String projectId);

  public List<DatasetBO> getDatasets(String projectId, Integer startIndex, Integer limit,
      String filter, String sortBy, String sortOrder);

}
