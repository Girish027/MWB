/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * TaggingGuideColumnMapping
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-08T18:54:19.161-07:00")
public class TaggingGuideColumnMappingModel {

  private String id = null;

  private String userId = null;

  private String columnId = null;

  private String projectId = null;

  private Integer columnIndex = null;

  private String displayName = null;

  public TaggingGuideColumnMappingModel id(String id) {
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

  public TaggingGuideColumnMappingModel userId(String userId) {
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

  public TaggingGuideColumnMappingModel columnId(String columnId) {
    this.columnId = columnId;
    return this;
  }

  /**
   * The id of the column
   *
   * @return columnId
   **/
  @ApiModelProperty(required = true, value = "The id of the column")
  public String getColumnId() {
    return columnId;
  }

  public void setColumnId(String columnId) {
    this.columnId = columnId;
  }

  public TaggingGuideColumnMappingModel projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * The identifier for the project
   *
   * @return id
   **/
  @ApiModelProperty(value = "The identifier for the project")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public TaggingGuideColumnMappingModel columnIndex(Integer columnIndex) {
    this.columnIndex = columnIndex;
    return this;
  }

  /**
   * Get columnIndex
   *
   * @return columnIndex
   **/
  @ApiModelProperty(required = true, value = "")
  public Integer getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(Integer columnIndex) {
    this.columnIndex = columnIndex;
  }

  public TaggingGuideColumnMappingModel displayName(String displayName) {
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
    TaggingGuideColumnMappingModel taggingGuideColumnMapping = (TaggingGuideColumnMappingModel) o;
    return Objects.equals(this.id, taggingGuideColumnMapping.id) &&
        Objects.equals(this.userId, taggingGuideColumnMapping.userId) &&
        Objects.equals(this.columnId, taggingGuideColumnMapping.columnId) &&
        Objects.equals(this.projectId, taggingGuideColumnMapping.projectId) &&
        Objects.equals(this.columnIndex, taggingGuideColumnMapping.columnIndex) &&
        Objects.equals(this.displayName, taggingGuideColumnMapping.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, columnId, projectId, columnIndex, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaggingGuideColumnMapping {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    columnId: ").append(toIndentedString(columnId)).append("\n");
    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
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

