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
public class TranscriptionCommentDocument {

  @NotBlank
  private String documentType;

  @NotBlank
  private String transcriptionHash;

  @NotBlank
  private String comment;

  @NotBlank
  private String commentedBy;

  @NotBlank
  private String commentedAt;

  @Min(value = 1)
  Integer projectId;

  @Min(value = 1)
  Integer datasetId;
}
