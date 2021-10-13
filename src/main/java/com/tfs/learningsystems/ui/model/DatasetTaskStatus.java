/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * DatasetTaskStatus
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-09T07:15:38.372-07:00")
public class DatasetTaskStatus {

  @ApiModelProperty(required = true, value = "dataset id", example = "753")
  private String id = null;

  @ApiModelProperty(required = true, value = "dataset name", example = "20190607T1415")
  private String name = null;

  @ApiModelProperty(required = true, value = "last task ran for that dataset", example = "INDEX")
  private String task = null;

  @ApiModelProperty(required = true, value = "status of last task", example = "COMPLETED")
  private String status = null;

  public DatasetTaskStatus id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The dataset Id
   *
   * @return id
   **/
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DatasetTaskStatus name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The dataset Name
   *
   * @return name
   **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DatasetTaskStatus task(String task) {
    this.task = task;
    return this;
  }

  /**
   * The name of dataset transformation task
   *
   * @return task
   **/
  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public DatasetTaskStatus status(String status) {
    this.status = status;
    return this;
  }

  /**
   * The status of the dataset transformation task
   *
   * @return status
   **/
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatasetTaskStatus datasetTaskStatus = (DatasetTaskStatus) o;
    return Objects.equals(this.id, datasetTaskStatus.id) &&
        Objects.equals(this.name, datasetTaskStatus.name) &&
        Objects.equals(this.task, datasetTaskStatus.task) &&
        Objects.equals(this.status, datasetTaskStatus.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, task, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatasetTaskStatus {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    task: ").append(toIndentedString(task)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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