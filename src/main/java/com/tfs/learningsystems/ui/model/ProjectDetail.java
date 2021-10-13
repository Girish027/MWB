/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * ProjectDetail
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-27T13:56:05.802-04:00")
public class ProjectDetail extends Project {


  private String id = null;

  private Long createdAt = null;

  private Long modifiedAt = null;

  private String modifiedBy = null;

  private Long startAt = null;

  private Long endAt = null;

  private Integer offset = null;

  private Long totalCount = null;

  private Integer modelCount = null;

  private Integer deployableModelId =0;

  private String previewModelId = null;

  private String liveModelId = null;

  public ProjectDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * project id
   *
   * @return id
   **/
  @ApiModelProperty(required = true, value = "project id", example = "561")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ProjectDetail createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The UTC date time for project creation
   *
   * @return createdAt
   **/
  @ApiModelProperty(readOnly = true, value = "Project creation time", example = "1551998097406")
  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public ProjectDetail modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * Modification time for any attribute update
   *
   * @return modifiedAt
   **/
  @ApiModelProperty(readOnly = true, value = "Project last modified time", example = "1551998097406")
  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public ProjectDetail modifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * User ID of last user to make a change
   *
   * @return modifiedBy
   **/
  @ApiModelProperty(readOnly = true, value = "user id who modified the project", example = "ml_user")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public ProjectDetail previewModelId(String previewModelId) {
    this.previewModelId = previewModelId;
    return this;
  }

  /**
   * To get value of previewModelId
   *
   * @return previewModelId
   **/
  @ApiModelProperty(readOnly = true, value = "preview model id", example = "12")
  public String getPreviewModelId() {
    return previewModelId;
  }

  public void setPreviewModelId(String previewModelId) {
    this.previewModelId = previewModelId;
  }

  /**
   * To get value of liveModelId
   *
   * @return liveModelId
   **/
  @ApiModelProperty(readOnly = true, value = "live model id", example = "45")
  public String getLiveModelId() {
    return liveModelId;
  }

  public void setLiveModelId(String liveModelId) {
    this.liveModelId = liveModelId;
  }


  public ProjectDetail startAt(Long startAt) {
    this.startAt = startAt;
    return this;
  }

  /**
   * The UTC date time for project start date
   *
   * @return startAt
   **/
  @ApiModelProperty(readOnly = true, value = "project start time",  example = "1551998097406")
  public Long getStartAt() {
    return startAt;
  }

  public void setStartAt(Long startAt) {
    this.startAt = startAt;
  }

  public ProjectDetail endAt(Long endAt) {
    this.endAt = endAt;
    return this;
  }

  /**
   * The UTC date time for project end date
   *
   * @return endAt
   **/
  @ApiModelProperty(readOnly = true, value = "project end time", example = "1551998097406")
  public Long getEndAt() {
    return endAt;
  }

  public void setEndAt(Long endAt) {
    this.endAt = endAt;
  }

  public ProjectDetail modelCount(Integer modelCount) {
    this.modelCount = modelCount;
    return this;
  }

  /**
   * Get modelCount
   *
   * @return modelCount
   **/
  @ApiModelProperty(value = "")
  public Integer getModelCount() {
    return modelCount;
  }

  public void setModelCount(Integer modelCount) {
    this.modelCount = modelCount;
  }

  public ProjectDetail offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Get offset
   *
   * @return offset
   **/
  @ApiModelProperty(hidden = true, value = "")
  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public ProjectDetail totalCount(Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Get totalCount
   *
   * @return totalCount
   **/
  @ApiModelProperty(hidden = true, value = "")
  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public Integer getDeployableModelId() {
    return deployableModelId;
  }

  public void setDeployableModelId(Integer deployableModelId) {
    this.deployableModelId = deployableModelId;
  }

  public ProjectDetail deployableModelId(Integer deployableModelId) {
    this.deployableModelId = deployableModelId;
    return this;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectDetail projectDetail = (ProjectDetail) o;
    return Objects.equals(this.id, projectDetail.id)
        && Objects.equals(this.createdAt, projectDetail.createdAt)
        && Objects.equals(this.modifiedAt, projectDetail.modifiedAt)
        && Objects.equals(this.modifiedBy, projectDetail.modifiedBy)
        && Objects.equals(this.startAt, projectDetail.startAt)
        && Objects.equals(this.endAt, projectDetail.endAt)
        && Objects.equals(this.offset, projectDetail.offset)
        && Objects.equals(this.modelCount, projectDetail.modelCount)
        && Objects.equals(this.totalCount, projectDetail.totalCount)
        && Objects.equals(this.deployableModelId, projectDetail.deployableModelId)&& super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, modifiedAt, modifiedBy, startAt, endAt, offset, totalCount, modelCount,
            super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProjectDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    modifiedBy: ").append(toIndentedString(modifiedBy)).append("\n");
    sb.append("    startAt: ").append(toIndentedString(startAt)).append("\n");
    sb.append("    endAt: ").append(toIndentedString(endAt)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    modelCount: ").append(toIndentedString(modelCount)).append("\n");
    sb.append("    deployableModelId: ").append(toIndentedString(deployableModelId)).append("\n");
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
