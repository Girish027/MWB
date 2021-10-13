/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * ProjectDatasetInheritance
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-12T15:07:40.236-07:00")
public class ProjectDatasetInheritance {

  private String inheritingFromClientId = null;

  private String inheritingFromProjectId = null;

  private String inheritingFromDatasetId = null;

  private String inheritingIntoClientId = null;

  private String inheritingIntoProjectId = null;

  private String inheritingIntoDatasetId = null;

  public ProjectDatasetInheritance inheritingFromClientId(String inheritingFromClientId) {
    this.inheritingFromClientId = inheritingFromClientId;
    return this;
  }

  /**
   * ClientId from where to inherit
   *
   * @return inheritingFromClientId
   **/
  @ApiModelProperty(required = true, value = "ClientId from where to inherit")
  public String getInheritingFromClientId() {
    return inheritingFromClientId;
  }

  public void setInheritingFromClientId(String inheritingFromClientId) {
    this.inheritingFromClientId = inheritingFromClientId;
  }

  public ProjectDatasetInheritance inheritingFromProjectId(String inheritingFromProjectId) {
    this.inheritingFromProjectId = inheritingFromProjectId;
    return this;
  }

  /**
   * ProjectId from where to inherit
   *
   * @return inheritingFromProjectId
   **/
  @ApiModelProperty(required = true, value = "ProjectId from where to inherit")
  public String getInheritingFromProjectId() {
    return inheritingFromProjectId;
  }

  public void setInheritingFromProjectId(String inheritingFromProjectId) {
    this.inheritingFromProjectId = inheritingFromProjectId;
  }

  public ProjectDatasetInheritance inheritingFromDatasetId(String inheritingFromDatasetId) {
    this.inheritingFromDatasetId = inheritingFromDatasetId;
    return this;
  }

  /**
   * DatasetId from where to inherit
   *
   * @return inheritingFromDatasetId
   **/
  @ApiModelProperty(required = true, value = "DatasetId from where to inherit")
  public String getInheritingFromDatasetId() {
    return inheritingFromDatasetId;
  }

  public void setInheritingFromDatasetId(String inheritingFromDatasetId) {
    this.inheritingFromDatasetId = inheritingFromDatasetId;
  }

  public ProjectDatasetInheritance inheritingIntoClientId(String inheritingIntoClientId) {
    this.inheritingIntoClientId = inheritingIntoClientId;
    return this;
  }

  /**
   * ClientId into which inherit
   *
   * @return inheritingIntoClientId
   **/
  @ApiModelProperty(required = true, value = "ClientId into which inherit")
  public String getInheritingIntoClientId() {
    return inheritingIntoClientId;
  }

  public void setInheritingIntoClientId(String inheritingIntoClientId) {
    this.inheritingIntoClientId = inheritingIntoClientId;
  }

  public ProjectDatasetInheritance inheritingIntoProjectId(String inheritingIntoProjectId) {
    this.inheritingIntoProjectId = inheritingIntoProjectId;
    return this;
  }

  /**
   * ProjectId into which inherit
   *
   * @return inheritingIntoProjectId
   **/
  @ApiModelProperty(required = true, value = "ProjectId into which inherit")
  public String getInheritingIntoProjectId() {
    return inheritingIntoProjectId;
  }

  public void setInheritingIntoProjectId(String inheritingIntoProjectId) {
    this.inheritingIntoProjectId = inheritingIntoProjectId;
  }

  public ProjectDatasetInheritance inheritingIntoDatasetId(String inheritingIntoDatasetId) {
    this.inheritingIntoDatasetId = inheritingIntoDatasetId;
    return this;
  }

  /**
   * DatasetId into which inherit
   *
   * @return inheritingIntoDatasetId
   **/
  @ApiModelProperty(required = true, value = "DatasetId into which inherit")
  public String getInheritingIntoDatasetId() {
    return inheritingIntoDatasetId;
  }

  public void setInheritingIntoDatasetId(String inheritingIntoDatasetId) {
    this.inheritingIntoDatasetId = inheritingIntoDatasetId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectDatasetInheritance projectDatasetInheritance = (ProjectDatasetInheritance) o;
    return Objects
        .equals(this.inheritingFromClientId, projectDatasetInheritance.inheritingFromClientId) &&
        Objects
            .equals(this.inheritingFromProjectId, projectDatasetInheritance.inheritingFromProjectId)
        &&
        Objects
            .equals(this.inheritingFromDatasetId, projectDatasetInheritance.inheritingFromDatasetId)
        &&
        Objects
            .equals(this.inheritingIntoClientId, projectDatasetInheritance.inheritingIntoClientId)
        &&
        Objects
            .equals(this.inheritingIntoProjectId, projectDatasetInheritance.inheritingIntoProjectId)
        &&
        Objects.equals(this.inheritingIntoDatasetId,
            projectDatasetInheritance.inheritingIntoDatasetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inheritingFromClientId, inheritingFromProjectId,
        inheritingFromDatasetId, inheritingIntoClientId,
        inheritingIntoProjectId, inheritingIntoDatasetId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProjectDatasetInheritance {\n");

    sb.append("    inheritingFromClientId: ").append(toIndentedString(inheritingFromClientId))
        .append("\n");
    sb.append("    inheritingFromProjectId: ").append(toIndentedString(inheritingFromProjectId))
        .append("\n");
    sb.append("    inheritingFromDatasetId: ").append(toIndentedString(inheritingFromDatasetId))
        .append("\n");
    sb.append("    inheritingIntoClientId: ").append(toIndentedString(inheritingIntoClientId))
        .append("\n");
    sb.append("    inheritingIntoProjectId: ").append(toIndentedString(inheritingIntoProjectId))
        .append("\n");
    sb.append("    inheritingIntoDatasetId: ").append(toIndentedString(inheritingIntoDatasetId))
        .append("\n");
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

