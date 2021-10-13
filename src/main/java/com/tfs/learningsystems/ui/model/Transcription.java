/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
@JsonSerialize
public class Transcription {

  private String classificationId;

  private String taggedBy;

  private String taggedAt;

  @NotBlank
  private String documentId;

  @NotBlank
  private String documentType;

  @NotBlank
  private String transcriptionHash;

  @Min(value = 1)
  int clientId;

  @Min(value = 1)
  int projectId;

  @Min(value = 1)
  int datasetId;
}
