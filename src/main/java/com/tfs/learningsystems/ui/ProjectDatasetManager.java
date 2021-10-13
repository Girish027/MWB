/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.DatasetBO;
import java.util.List;

/**
 * @author jkarpala
 */
public interface ProjectDatasetManager {

  /**
   * @return count of datasets mapped to project
   */
  public Long countDatasetsForProjectById(String clientId, String projectId);

  public List<Integer> listDatasetIdsByProjectId(String projectId);

  /**
   * @return list of datasets mapped to project by id
   */
  public List<DatasetBO> listDatasetsForProjectById(String projectId, Integer startIndex,
      Integer limit, String filter, String sortBy, String sortOrder);

  /**
   * remove all mappings for a dataset
   */
  public void removeDatasetProjectMapping(String datasetId);

  /**
   * remove dataset to project mapping
   */
  public void removeDatasetProjectMapping(String clientId, String datasetId, String projectId);

  /**
   * remove all mappings for a project
   */
  public void removeProjectDatasetMapping(String projectId);

  /**
   * validate the dataset for a project
   *
   * @return boolean stating if the dataset is valid for a project
   */
  public boolean isProjectDatasetValid(String projectId, String datasetId);
}
