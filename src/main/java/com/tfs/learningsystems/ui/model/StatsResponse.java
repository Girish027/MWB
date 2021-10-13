/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * StatsResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class StatsResponse {

  private Long intents = null;

  private SearchStats unique = null;

  private SearchStats all = null;

  public StatsResponse intents(Long intents) {
    this.intents = intents;
    return this;
  }

  /**
   * Total unique intents used to tag transcriptions for this project
   *
   * @return intents
   **/
  @ApiModelProperty(value = "Total unique intents used to tag transcriptions for this project")
  public Long getIntents() {
    return intents;
  }

  public void setIntents(Long intents) {
    this.intents = intents;
  }

  public StatsResponse unique(SearchStats unique) {
    this.unique = unique;
    return this;
  }

  /**
   * Get unique
   *
   * @return unique
   **/
  @ApiModelProperty(value = "")
  public SearchStats getUnique() {
    return unique;
  }

  public void setUnique(SearchStats unique) {
    this.unique = unique;
  }

  public StatsResponse all(SearchStats all) {
    this.all = all;
    return this;
  }

  /**
   * Get all
   *
   * @return all
   **/
  @ApiModelProperty(value = "")
  public SearchStats getAll() {
    return all;
  }

  public void setAll(SearchStats all) {
    this.all = all;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StatsResponse statsResponse = (StatsResponse) o;
    return Objects.equals(this.intents, statsResponse.intents) &&
        Objects.equals(this.unique, statsResponse.unique) &&
        Objects.equals(this.all, statsResponse.all);
  }

  @Override
  public int hashCode() {
    return Objects.hash(intents, unique, all);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatsResponse {\n");

    sb.append("    intents: ").append(toIndentedString(intents)).append("\n");
    sb.append("    unique: ").append(toIndentedString(unique)).append("\n");
    sb.append("    all: ").append(toIndentedString(all)).append("\n");
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

