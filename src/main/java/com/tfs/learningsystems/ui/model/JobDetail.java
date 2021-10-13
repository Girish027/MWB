/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class JobDetail extends Job {

  private String id = null;

  private Long createdAt = null;

  private String createdBy = null;

  private Long modifiedAt = null;

  private String modifiedBy = null;


  public JobDetail id(String id) {
    this.id = id;
    return this;
  }

  @ApiModelProperty(required = true, value = "Numeric string to identify the group uniquely")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public JobDetail createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The UTC date time of job creation
   *
   * @return createdAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time of job creation")
  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public JobDetail createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * User ID of job creator
   *
   * @return createdBy
   **/
  @ApiModelProperty(required = true, value = "User ID of job creator")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }


  public JobDetail modifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  /**
   * User ID of job modifier
   *
   * @return modifiedBy
   **/
  @ApiModelProperty(required = true, value = "User ID of job modifier")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public JobDetail modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * The UTC date time of the Job
   *
   * @return modifiedAt
   **/
  @ApiModelProperty(required = true, value = "The UTC date time for Job")
  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JobDetail jobDetail = (JobDetail) o;
    return Objects.equals(this.id, jobDetail.id)
        && Objects.equals(this.createdAt, jobDetail.createdAt)
        && Objects.equals(this.createdBy, jobDetail.createdBy)
        && Objects.equals(this.modifiedAt, jobDetail.modifiedAt)
        && Objects.equals(this.modifiedBy, jobDetail.modifiedBy)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, createdBy, modifiedAt, modifiedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    modifiedBy: ").append(toIndentedString(modifiedBy)).append("\n");
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
