/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * SearchStats
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class SearchStats {

  private Long total = null;

  private Long tagged = null;

  private Float percent = null;

  public SearchStats total(Long total) {
    this.total = total;
    return this;
  }

  /**
   * Number of transcriptions
   *
   * @return total
   **/
  @ApiModelProperty(value = "Number of transcriptions")
  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public SearchStats tagged(Long tagged) {
    this.tagged = tagged;
    return this;
  }

  /**
   * Number of tagged transcriptions
   *
   * @return tagged
   **/
  @ApiModelProperty(value = "Number of tagged transcriptions")
  public Long getTagged() {
    return tagged;
  }

  public void setTagged(Long tagged) {
    this.tagged = tagged;
  }

  public SearchStats percent(Float percent) {
    this.percent = percent;
    return this;
  }

  /**
   * Percentage of transcriptions tagged
   *
   * @return percent
   **/
  @ApiModelProperty(value = "Percentage of transcriptions tagged")
  public Float getPercent() {
    return percent;
  }

  public void setPercent(Float percent) {
    this.percent = percent;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchStats searchStats = (SearchStats) o;
    return Objects.equals(this.total, searchStats.total) &&
        Objects.equals(this.tagged, searchStats.tagged) &&
        Objects.equals(this.percent, searchStats.percent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, tagged, percent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchStats {\n");

    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    tagged: ").append(toIndentedString(tagged)).append("\n");
    sb.append("    percent: ").append(toIndentedString(percent)).append("\n");
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

