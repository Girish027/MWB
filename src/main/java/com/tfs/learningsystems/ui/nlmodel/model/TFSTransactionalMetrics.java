/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
*******************************************************************************/
package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author girish.prajapati
**/

@Data
@JsonSerialize
public class TFSTransactionalMetrics {

  @ApiModelProperty(readOnly = true, value = "client id to which this metric belongs to", example = "11")
  private Integer clientId;
  @ApiModelProperty(required = true, value = "model_name", example = "model1")
  private String modelName;
  @ApiModelProperty(required = true, value = "node_name", example = "node1")
  private String nodeName;
  @ApiModelProperty(required = true, value = "metric date", example = "2021-07-01 00:00:00")
  private Timestamp metricDate;
  @ApiModelProperty(readOnly = true, value = "volume count", example = "100")
  private Integer volume;
  @ApiModelProperty(value = "escalation count", example = "60")
  private Integer escalation;
  @ApiModelProperty(value = "version value", example = "10")
  private Integer version;
  @ApiModelProperty(required = true, value = "created time", example = "1343805819061")
  private Long createdAt;
  @ApiModelProperty(required = true, value = "Last modified date", example = "1343805819061")
  private Long modifiedAt;
}