/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;


/**
 * DateRange
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class DateRange {

  @NotNull
  private Long startDate = 0L;

  @NotNull
  private Long endDate = 0L;

  public DateRange startDate(Long startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Date in milliseconds since UTC epoch
   *
   * @return startDate
   **/
  @ApiModelProperty(value = "Date in milliseconds since UTC epoch")
  public Long getStartDate() {
    return startDate;
  }

  public void setStartDate(Long startDate) {
    this.startDate = startDate;
  }

  public DateRange endDate(Long endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Date in milliseconds since UTC epoch
   *
   * @return endDate
   **/
  @ApiModelProperty(value = "Date in milliseconds since UTC epoch")
  public Long getEndDate() {
    return endDate;
  }

  public void setEndDate(Long endDate) {
    this.endDate = endDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DateRange dateRange = (DateRange) o;
    return Objects.equals(this.startDate, dateRange.startDate) &&
        Objects.equals(this.endDate, dateRange.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startDate, endDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DateRange {\n");

    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
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

