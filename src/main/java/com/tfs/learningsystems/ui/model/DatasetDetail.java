/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * DatasetDetail
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-02T22:04:30.572+05:30")
public class DatasetDetail extends Dataset {

  private String id = null;

  private Long createdAt = null;

  private String createdBy = null;

  private Long modifiedAt = null;

  private String modifiedBy = null;

  private Integer offset = null;

  private Long totalCount = null;

  private String transformationStatus;

  private String transformationTask;

  public DatasetDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * dataset id
   *
   * @return id
   **/
  @ApiModelProperty(required = true, value = "dataset id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DatasetDetail createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The UTC date time for when the dataset was created
   *
   * @return createdAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time for when the dataset was created")
  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public DatasetDetail createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * The user creating the dataset
   *
   * @return createdBy
   **/
  @ApiModelProperty(required = true, value = "The user creating the dataset")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public DatasetDetail modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * The UTC date time for when the dataset was modified
   *
   * @return modifiedAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time for when the dataset was modified")
  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public DatasetDetail modifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * The user modifying the dataset
   *
   * @return modifiedBy
   **/
  @ApiModelProperty(required = true, value = "The user modifying the dataset")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public DatasetDetail offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Get offset
   *
   * @return offset
   **/
  @ApiModelProperty(value = "")
  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public DatasetDetail totalCount(Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Get totalCount
   *
   * @return totalCount
   **/
  @ApiModelProperty(value = "")
  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public DatasetDetail transformationStatus(String transformationStatus) {
    this.transformationStatus = transformationStatus;
    return this;
  }

  public String getTransformationStatus() {
    return this.transformationStatus;
  }

  public void setTransformationStatus(String transformationStatus) {
    this.transformationStatus = transformationStatus;
  }

  public DatasetDetail transformationTask(String transformationTask) {
    this.transformationTask = transformationTask;
    return this;
  }

  public String getTransformationTask() {
    return this.transformationTask;
  }

  public void setTransformationTask(String transformationTask) {
    this.transformationTask = transformationTask;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatasetDetail datasetDetail = (DatasetDetail) o;
    return Objects.equals(this.id, datasetDetail.id) &&
        Objects.equals(this.createdAt, datasetDetail.createdAt) &&
        Objects.equals(this.createdBy, datasetDetail.createdBy) &&
        Objects.equals(this.modifiedAt, datasetDetail.modifiedAt) &&
        Objects.equals(this.modifiedBy, datasetDetail.modifiedBy) &&
        Objects.equals(this.offset, datasetDetail.offset) &&
        Objects.equals(this.totalCount, datasetDetail.totalCount) &&
        Objects.equals(this.transformationStatus, datasetDetail.transformationStatus) &&
        Objects.equals(this.transformationTask, datasetDetail.transformationTask) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, createdBy, modifiedAt, modifiedBy,
        offset, totalCount, transformationStatus, transformationTask, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatasetDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    modifiedBy: ").append(toIndentedString(modifiedBy)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    transformationStatus: ").append(toIndentedString(transformationStatus))
        .append("\n");
    sb.append("    transformationTask: ").append(toIndentedString(transformationTask)).append("\n");
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

