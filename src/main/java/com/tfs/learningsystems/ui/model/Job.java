/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import org.hibernate.validator.constraints.NotEmpty;

public class Job {

  @NotEmpty
  private String projectId = null;

  @NotEmpty
  private String datasetId = null;

  private String fileName = null;

  public Job projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * id of associated project
   *
   * @return projectId
   **/
  @ApiModelProperty(required = true, value = "id of associated project")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public Job datasetId(String datasetId) {
    this.datasetId = datasetId;
    return this;
  }

  /**
   * id of associated dataset
   *
   * @return datasetId
   **/
  @ApiModelProperty(required = true, value = "id of associated dataset")
  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public Job fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  /**
   * Name of file associated with job
   *
   * @return fileName
   **/
  @ApiModelProperty(required = true, value = "Name of file associated with job")
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Job job = (Job) o;
    return Objects.equals(this.projectId, job.projectId)
        && Objects.equals(this.datasetId, job.datasetId)
        && Objects.equals(this.fileName, job.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectId, datasetId, fileName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Job {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
    sb.append("    datasetId: ").append(toIndentedString(datasetId)).append("\n");
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
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

  /**
   * @return JSON string of Job object
   */
  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }
}
