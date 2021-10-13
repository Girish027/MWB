/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
@JsonSerialize
public class ProjectModelRequest {

  @NotBlank
  @ApiModelProperty( value = "which project you want to publish", example = "root_intent")
  private String projectId;

  @NotBlank
  @ApiModelProperty(value = "which model within that project you want to publish", example = "12")
  private String modelId;


}