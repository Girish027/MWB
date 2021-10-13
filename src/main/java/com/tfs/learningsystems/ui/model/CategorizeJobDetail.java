/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import java.util.List;
import lombok.Data;

@Data
public class CategorizeJobDetail {

  private String csvFilePath;
  private String categorizeServicePath;
  private String datasetLocale;
  private List<String> columns;
  private String transcriptionColumn;
  private String originalTranscriptionColumn;
  private Boolean useModelForSuggestedCategory;
  private String projectId;
  private String clientId;

}
