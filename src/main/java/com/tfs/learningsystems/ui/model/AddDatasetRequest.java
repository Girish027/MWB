/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tfs.learningsystems.db.DatasetBO;
import lombok.Data;

@Data
@JsonSerialize
public class AddDatasetRequest {

  private DatasetBO dataset;
  private String projectId;
  private boolean autoTagDataset;
}
