/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.dao;

import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import java.util.List;

public interface DatasetIntentInheritanceDao {

  public DatasetIntentInheritance getInheritanceById(String id);

  public List<DatasetIntentInheritance> getInheritaceForDataset(String datasetId);

  public List<DatasetIntentInheritance> getInheritaceForProject(String projectId);

  public void updateStatus(String datasetId, DatasetIntentInheritanceStatus status);

  public DatasetIntentInheritance getLastPendingInheritaceForDataset(String datasetId);
}
