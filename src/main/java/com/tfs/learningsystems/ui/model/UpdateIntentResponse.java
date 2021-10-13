/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * UpdateIntentResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-11T23:16:26.062-08:00")
public class UpdateIntentResponse {

  private Integer uniqueCount = null;

  private Integer totalCount = null;

  public UpdateIntentResponse uniqueCount(Integer uniqueCount) {
    this.uniqueCount = uniqueCount;
    return this;
  }

  /**
   * Number of unique transcriptions untagged.
   *
   * @return uniqueCount
   **/
  @ApiModelProperty(required = true, value = "Number of unique transcriptions.")
  public Integer getUniqueCount() {
    return uniqueCount;
  }

  public void setUniqueCount(Integer uniqueCount) {
    this.uniqueCount = uniqueCount;
  }

  public UpdateIntentResponse totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Total number of transcriptions untagged.
   *
   * @return totalCount
   **/
  @ApiModelProperty(required = true, value = "Total number of transcriptions.")
  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateIntentResponse updateIntentResponse = (UpdateIntentResponse) o;
    return Objects.equals(this.uniqueCount, updateIntentResponse.uniqueCount) &&
        Objects.equals(this.totalCount, updateIntentResponse.totalCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uniqueCount, totalCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateIntentResponse {\n");

    sb.append("    uniqueCount: ").append(toIndentedString(uniqueCount)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

