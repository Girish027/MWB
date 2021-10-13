/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * TaggingGuideColumnMappingSelection
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-10T01:29:39.020-07:00")
public class TaggingGuideColumnMappingSelection {

  private String id = null;

  private String userId = null;

  private String columnName = null;

  private String columnIndex = null;

  private String displayName = null;

  public TaggingGuideColumnMappingSelection id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The identifier for the column row
   *
   * @return id
   **/
  @ApiModelProperty(value = "The identifier for the column row")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TaggingGuideColumnMappingSelection userId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * The id of the user
   *
   * @return userId
   **/
  @ApiModelProperty(required = true, value = "The id of the user")
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public TaggingGuideColumnMappingSelection columnName(String columnName) {
    this.columnName = columnName;
    return this;
  }

  /**
   * The name of the column
   *
   * @return columnName
   **/
  @ApiModelProperty(required = true, value = "The name of the column")
  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public TaggingGuideColumnMappingSelection columnIndex(String columnIndex) {
    this.columnIndex = columnIndex;
    return this;
  }

  /**
   * Get columnIndex
   *
   * @return columnIndex
   **/
  @ApiModelProperty(required = true, value = "")
  public String getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(String columnIndex) {
    this.columnIndex = columnIndex;
  }

  public TaggingGuideColumnMappingSelection displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * The header name of the column in the uploaded guide
   *
   * @return displayName
   **/
  @ApiModelProperty(value = "The header name of the column in the uploaded guide")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaggingGuideColumnMappingSelection taggingGuideColumnMappingSelection = (TaggingGuideColumnMappingSelection) o;
    return Objects.equals(this.id, taggingGuideColumnMappingSelection.id) &&
        Objects.equals(this.userId, taggingGuideColumnMappingSelection.userId) &&
        Objects.equals(this.columnName, taggingGuideColumnMappingSelection.columnName) &&
        Objects.equals(this.columnIndex, taggingGuideColumnMappingSelection.columnIndex) &&
        Objects.equals(this.displayName, taggingGuideColumnMappingSelection.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, columnName, columnIndex, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaggingGuideColumnMappingSelection {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    columnName: ").append(toIndentedString(columnName)).append("\n");
    sb.append("    columnIndex: ").append(toIndentedString(columnIndex)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
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

