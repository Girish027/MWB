/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import org.hibernate.validator.constraints.NotBlank;

/**
 * An object that defines a request to transform the dataset
 **/

/**
 * An object that defines a request to transform the dataset
 */
@ApiModel(description = "An object that defines a request to transform the dataset")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-02T23:00:11.057+05:30")
public class TransformDatasetRequest {

  @NotBlank
  private String projectId;

  public TransformDatasetRequest projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * The ID of the project this dataset is associated with
   *
   * @return projectId
   **/
  @ApiModelProperty(required = true, value = "The ID of the project this dataset is associated with")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransformDatasetRequest transformDatasetRequest = (TransformDatasetRequest) o;
    return Objects.equals(this.projectId, transformDatasetRequest.projectId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransformDatasetRequest {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
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

