/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Data;

@Data
@JsonSerialize
public class DatasetIntentInheritance {

  private String id;
  private String datasetId;
  private String projectId;
  private String requestedBy;
  private long updatedAt;
  private long requestedAt;
  private long totalTagged;
  private long uniqueTagged;
  private long totalTaggedMulipleIntents;
  private long uniqueTaggedMulipleIntents;
  private List<String> inheritedFromDatasetIds;
  private DatasetIntentInheritanceStatus status;
}
