/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import java.util.ArrayList;
import java.util.Objects;


/**
 * ProjectsDetail
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-27T13:56:05.802-04:00")
public class ProjectsDetail extends ArrayList<ProjectDetailDatasetTaskStatus> {

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProjectsDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

