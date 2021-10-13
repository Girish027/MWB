/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.DatasetIntentInheritanceBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetStatsResponse;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TransformDatasetRequest;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author jkarpala
 */
public interface DatasetManager {

  /**
   * get a dataset specified by an id
   *
   * @return DatasetBO
   */
  public DatasetBO getDatasetById(String datasetId);

  /**
   * get a dataset specified by client id, project id and dataset id
   *
   * @return DatasetBO
   */
  public DatasetBO getDatasetById(String clientId, String projectId, String datasetId);
  // TODO remove once clientId is added to all APIs

  /**
   * get a valid dataset which can be build specified by client id, project id and dataset id
   *
   * @return DatasetBO
   */
  public DatasetStatsResponse validDatasetForModelBuildingById(String clientId, String projectId, String datasetId);
  // TODO remove once clientId is added to all APIs

  /**
   * post a new dataset
   *
   * @return created dataset details object
   */
  public DatasetBO postDataset(String clientId, String projectId, String name, String source,
                               InputStream fileInputStream, boolean ignoreFirstRow,
                               FileColumnMappingSelectionList columnMappings, String dataType,
                               String description, String currentUserId);


  /**
   * create a new dataset
   *
   * @return created dataset details object
   */
  public DatasetBO addDataset(AddDatasetRequest addDatasetRequest, String currentUserId);

  /**
   * Delete dataset based on id
   */
  public void deleteDataset(String clientId, String datasetId);

  /**
   * Delete dataset based on id
   */
  public void deleteDataset(String datasetId);

  /**
   * update dataset
   *
   * @return updated dataset details
   */
  public DatasetBO updateDataset(String datasetId, PatchRequest patchRequest, String currentUserId);


  /**
   * get paged list of data sets
   *
   * @return paged list of data sets
   */
  public List<DatasetBO> getDatasets(int startIndex, int count, String filter,
                                     String filterClientId);

  /**
   * count all datasets in datasource
   *
   * @return datasets in datasource
   */
  public long countDatasets();


  /**
   * Index the given dataset into Elasticsearch
   */
  public DatasetBO transformDataset(DatasetBO dataset,
                                    TransformDatasetRequest transformDatasetRequest) throws ApplicationException;

  /**
   * Get the Locale for the dataset. returns default locale if none specified in the dataset
   *
   * @return Locale
   */
  public Locale getDatasetLocale(String datasetId);

  /**
   * set the dataset intent inheritance for the specified dataset and project
   *
   * @param datasetId dataset that should inherit tags
   * @param projectId project the dataset is associated with
   * @param currentUserId user that initiated action
   */
  public void addDatasetIntentInheritance(String datasetId, String projectId, String currentUserId);

  /**
   * get last pending inheritance for dataset
   */
  public DatasetIntentInheritance getLastPendingInheritaceForDataset(String datasetId);

  /**
   * Update intent Inheritance
   */
  public DatasetIntentInheritanceBO updateIntentInheritance(DatasetIntentInheritance inheritance);

  /**
   * update status on intent inheritance
   */
  public void updateIntentInheritanceStatus(String id, DatasetIntentInheritanceStatus status);

  Map<Integer, String> getDatasetSourceMapByDatasetIds(List<String> datasetIds);
}
