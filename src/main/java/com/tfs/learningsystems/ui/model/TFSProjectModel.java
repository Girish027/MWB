package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * ModelDetails
 */


@Data
@JsonSerialize
public class TFSProjectModel {

  @ApiModelProperty( value = "which project you want to publish", example = "353")
  private String projectId;

  @ApiModelProperty( value = "name of project", example = "root_intent")
  private String projectName;

  @ApiModelProperty(value = "which model within that project you want to publish", example = "12")
  private String modelId;

  @ApiModelProperty(value = "name of the model published", example = "root_intent")
  private String modelName;

  @ApiModelProperty(value = "model version", example = "211")
  private String modelVersion;

  @ApiModelProperty(value = "modelbuilder uuid", example = "78278724-1224244-364646")
  private String modelUUID;


}