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
public class TranscriptionForAuditing {

  @NotBlank
  private String documentId;

  @NotBlank
  private String documentType;

  @NotBlank
  private String classificationId;

  @NotBlank
  private String taggedAt;

  @NotBlank
  private String taggedBy;

  @NotBlank
  private String deletedAt;

  @NotBlank
  private String deletedBy;

  @NotBlank
  private String transcriptionHash;

  @Min(value = 1)
  private int clientId;

  @Min(value = 1)
  private Integer projectId;

  @Min(value = 1)
  private Integer datasetId;

}
