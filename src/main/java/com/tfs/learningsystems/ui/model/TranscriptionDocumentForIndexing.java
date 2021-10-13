/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
@JsonSerialize
public class TranscriptionDocumentForIndexing {

  @NotBlank
  private String jobId;

  @NotBlank
  private String vertical;

  @NotBlank
  private String dataType;

  @NotBlank
  private String filename;

  @NotBlank
  private String documentId;

  @NotBlank
  private String documentType;

  @NotBlank
  @JsonProperty("autoTagStr")
  private String autoTagString;

  @NotBlank
  private String transcriptionHash;

  @NotBlank
  private String textStringForTagging;

  @NotBlank
  @JsonProperty("originalTextString")
  private String textStringOriginal;

  @Min(value = 1)
  int clientId;

  @Min(value = 1)
  int projectId;

  @Min(value = 1)
  int datasetId;

  @JsonProperty("num_tokens")
  int numTokens;

  int autoTagCount;
}
