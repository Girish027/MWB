/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.file.model;

import lombok.Data;

@Data
public class FileColumnMappingModel {

  private String id;
  private String userId;
  private String columnId;
  private Integer columnIndex;
  private String displayName;
}

