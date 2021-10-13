/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author girish.prajapati
**/

@Data
@JsonSerialize
public class TFSAggregatedMetrics {

  @ApiModelProperty(readOnly = true, value = "client id to which this metric belongs to", example = "11")
  private Integer clientId;
  @ApiModelProperty(required = true, value = "model_name", example = "model1")
  private String modelName;
  @ApiModelProperty(readOnly = true, value = "volume count", example = "100")
  private Integer volume;
  @ApiModelProperty(value = "escalation count", example = "60")
  private Integer escalation;
}