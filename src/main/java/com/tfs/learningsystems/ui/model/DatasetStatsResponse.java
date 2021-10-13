/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * DatasetStatsResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class DatasetStatsResponse {

  private String uniqueRollupValue = null;

  private Boolean isDatasetFullyTagged = null;

  private Boolean isDatasetValid = true;

  private Set<DatasetIntents> datasetIntentsSet = null;

  public DatasetStatsResponse datasetIntentsSet(Set<DatasetIntents> datasetIntentsSet) {
    this.datasetIntentsSet = datasetIntentsSet;
    return this;
  }

  /**
   * Get isDatasetValid
   *
   * @return isDatasetValid
   **/
  @ApiModelProperty(value = "")
  public Set<DatasetIntents> getDatasetIntentsSet() {
    return datasetIntentsSet;
  }


  public void setDatasetIntentsSet(Set<DatasetIntents> datasetIntentsSet) {
    this.datasetIntentsSet = datasetIntentsSet;
  }

  public DatasetStatsResponse isDatasetValid(Boolean isDatasetValid) {
    this.isDatasetValid = isDatasetValid;
    return this;
  }

  /**
   * Get isDatasetValid
   *
   * @return isDatasetValid
   **/
  @ApiModelProperty(value = "")
  public Boolean getIsDatasetValid() {
    return isDatasetValid;
  }

  public void setIsDatasetValid(Boolean isDatasetValid) {
    this.isDatasetValid = isDatasetValid;
  }

  public DatasetStatsResponse isDatasetFullyTagged(Boolean isDatasetFullyTagged) {
    this.isDatasetFullyTagged = isDatasetFullyTagged;
    return this;
  }

  /**
   * Get isDatasetFullyTagged
   *
   * @return isDatasetFullyTagged
   **/
  @ApiModelProperty(value = "")
  public Boolean getIsDatasetFullyTagged() {
    return isDatasetFullyTagged;
  }

  public void setIsDatasetFullyTagged(Boolean isDatasetFullyTagged) {
    this.isDatasetFullyTagged = isDatasetFullyTagged;
  }


  public DatasetStatsResponse uniqueRollupValue(String uniqueRollupValue) {
    this.uniqueRollupValue = uniqueRollupValue;
    return this;
  }

  /**
   * Get uniqueRollupValue
   *
   * @return uniqueRollupValue
   **/
  @ApiModelProperty(value = "")
  public String getUniqueRollupValue() {
    return uniqueRollupValue;
  }

  public void setUniqueRollupValue(String uniqueRollupValue) {
    this.uniqueRollupValue = uniqueRollupValue;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatasetStatsResponse datasetStatsResponse = (DatasetStatsResponse) o;
    return Objects.equals(this.isDatasetFullyTagged, datasetStatsResponse.isDatasetFullyTagged) &&
            Objects.equals(this.isDatasetValid, datasetStatsResponse.isDatasetValid) &&
            Objects.equals(this.datasetIntentsSet, datasetStatsResponse.datasetIntentsSet) &&
            Objects.equals(this.uniqueRollupValue, datasetStatsResponse.uniqueRollupValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isDatasetFullyTagged, isDatasetValid, datasetIntentsSet, uniqueRollupValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatasetStatsResponse {\n");
    sb.append("    isDatasetValid: ").append(toIndentedString(isDatasetValid)).append("\n");
    sb.append("    datasetIntentsSet: ").append(toIndentedString(datasetIntentsSet)).append("\n");
    sb.append("    isDatasetFullyTagged: ").append(toIndentedString(isDatasetFullyTagged)).append("\n");
    sb.append("    uniqueRollupValue: ").append(toIndentedString(uniqueRollupValue)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

