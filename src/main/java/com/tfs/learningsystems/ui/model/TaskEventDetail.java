/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class TaskEventDetail extends TaskEvent {

  private String id = null;

  private Long createdAt = null;

  private Long modifiedAt = null;

  private Integer percentComplete = 0;

  private Long recordsProcessed = 0l;

  private Long recordsImported = 0l;

  public TaskEventDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * TaskEvent id
   *
   * @return id
   **/
  @ApiModelProperty(required = true, value = "TaskEvent id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TaskEventDetail createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The UTC date time of the TaskEvent
   *
   * @return createdAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time for TaskEvent")
  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public TaskEventDetail modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * The UTC date time of the TaskEvent
   *
   * @return modifiedAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time for TaskEvent")
  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public TaskEventDetail percentComplete(Integer percentComplete) {
    this.percentComplete = percentComplete;
    return this;
  }

  /**
   * The percent that the job is complete
   *
   * @return percentComplete
   **/
  @ApiModelProperty(required = false, value = "The percent that the job is complete")
  public Integer getPercentComplete() {
    return percentComplete;
  }

  public void setPercentComplete(Integer percentComplete) {
    this.percentComplete = percentComplete;
  }

  public TaskEventDetail recordsProcessed(Long recordsProcessed) {
    this.recordsProcessed = recordsProcessed;
    return this;
  }

  /**
   * The number of records processed so far in this task
   *
   * @return recordsProcessed
   **/
  @ApiModelProperty(required = false, value = "The number of records processed so far in this task")
  public Long getRecordsProcessed() {
    return recordsProcessed;
  }

  public void setRecordsProcessed(Long recordsProcessed) {
    this.recordsProcessed = recordsProcessed;
  }

  public TaskEventDetail recordsImported(Long recordsImported) {
    this.recordsImported = recordsImported;
    return this;
  }

  /**
   * The number of records imported so far in this task
   *
   * @return recordsImported
   **/
  @ApiModelProperty(required = false, value = "The number of records imported so far in this task")
  public Long getRecordsImported() {
    return recordsImported;
  }

  public void setRecordsImported(Long recordsImported) {
    this.recordsImported = recordsImported;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskEventDetail taskEventDetail = (TaskEventDetail) o;
    return Objects.equals(this.id, taskEventDetail.id)
        && Objects.equals(this.createdAt, taskEventDetail.createdAt)
        && Objects.equals(this.modifiedAt, taskEventDetail.modifiedAt)
        && Objects.equals(this.percentComplete, taskEventDetail.percentComplete)
        && Objects.equals(this.recordsImported, taskEventDetail.recordsImported)
        && Objects.equals(this.recordsProcessed, taskEventDetail.recordsProcessed)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, modifiedAt, percentComplete, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaskEventDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    percentComplete: ").append(toIndentedString(percentComplete)).append("\n");
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
