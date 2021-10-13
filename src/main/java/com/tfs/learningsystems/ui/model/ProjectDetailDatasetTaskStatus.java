/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * ProjectDetailDatasetTaskStatus
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-09T07:15:38.372-07:00")
public class ProjectDetailDatasetTaskStatus extends ProjectDetail {

  private List<DatasetTaskStatus> datasetTaskStatusList = new ArrayList<DatasetTaskStatus>();

  public ProjectDetailDatasetTaskStatus datasetTaskStatusList(
      List<DatasetTaskStatus> datasetTaskStatusList) {
    this.datasetTaskStatusList = datasetTaskStatusList;
    return this;
  }

  public ProjectDetailDatasetTaskStatus addDatasetTaskStatusListItem(
      DatasetTaskStatus datasetTaskStatusListItem) {
    this.datasetTaskStatusList.add(datasetTaskStatusListItem);
    return this;
  }

  /**
   * List of DatasetTaskStatus objects
   *
   * @return datasetTaskStatusList
   **/
  @ApiModelProperty(required = true, value = "List of DatasetTaskStatus objects")
  public List<DatasetTaskStatus> getDatasetTaskStatusList() {
    return datasetTaskStatusList;
  }

  public void setDatasetTaskStatusList(List<DatasetTaskStatus> datasetTaskStatusList) {
    this.datasetTaskStatusList = datasetTaskStatusList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectDetailDatasetTaskStatus projectDetailDatasetTaskStatus = (ProjectDetailDatasetTaskStatus) o;
    return Objects
        .equals(this.datasetTaskStatusList, projectDetailDatasetTaskStatus.datasetTaskStatusList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasetTaskStatusList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProjectDetailDatasetTaskStatus {\n");

    sb.append("    datasetTaskStatusList: ").append(toIndentedString(datasetTaskStatusList))
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

