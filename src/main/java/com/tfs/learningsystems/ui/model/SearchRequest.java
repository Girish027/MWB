/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * SearchRequest
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class SearchRequest {

  private String query = "*";

  private SearchRequestFilter filter = null;

  public SearchRequest query(String query) {
    this.query = query;
    return this;
  }

  /**
   * Query string or keyword to search
   *
   * @return query
   **/
  @ApiModelProperty(value = "Query string or keyword to search")
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public SearchRequest filter(SearchRequestFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Get filter
   *
   * @return filter
   **/
  @ApiModelProperty(value = "")
  public SearchRequestFilter getFilter() {
    return filter;
  }

  public void setFilter(SearchRequestFilter filter) {
    this.filter = filter;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchRequest searchRequest = (SearchRequest) o;
    return Objects.equals(this.query, searchRequest.query) &&
        Objects.equals(this.filter, searchRequest.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, filter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchRequest {\n");

    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
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

