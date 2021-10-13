package com.tfs.learningsystems.ui.search.taggingguide.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class DocumentDatasetObject {

  public DocumentDatasetObject(String id, String datasetId) {
    this.id = id;
    this.datasetId = datasetId;

  }

  private String id;
  private String datasetId;
}
