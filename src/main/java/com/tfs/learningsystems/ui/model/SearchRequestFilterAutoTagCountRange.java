/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Range to specify minimum and maximum auto categorization counts
 **/

/**
 * Range to specify minimum and maximum auto categorization counts
 */
@ApiModel(description = "Range to specify minimum and maximum auto categorization counts")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class SearchRequestFilterAutoTagCountRange {

  @Min(value = 1)
  @NotNull
  private Integer min = 1;

  @Min(value = 0)
  @NotNull
  private Integer max = 0;

  public SearchRequestFilterAutoTagCountRange min(Integer min) {
    this.min = min;
    return this;
  }

  /**
   * Get min
   *
   * @return min
   **/
  @ApiModelProperty(value = "")
  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  public SearchRequestFilterAutoTagCountRange max(Integer max) {
    this.max = max;
    return this;
  }

  /**
   * Get max
   *
   * @return max
   **/
  @ApiModelProperty(value = "")
  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchRequestFilterAutoTagCountRange searchRequestFilterAutoTagCountRange = (SearchRequestFilterAutoTagCountRange) o;
    return Objects.equals(this.min, searchRequestFilterAutoTagCountRange.min) &&
        Objects.equals(this.max, searchRequestFilterAutoTagCountRange.max);
  }

  @Override
  public int hashCode() {
    return Objects.hash(min, max);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchRequestFilterAutoTagCountRange {\n");

    sb.append("    min: ").append(toIndentedString(min)).append("\n");
    sb.append("    max: ").append(toIndentedString(max)).append("\n");
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

