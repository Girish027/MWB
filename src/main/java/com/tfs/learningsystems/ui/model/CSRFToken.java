/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * The CSRF Token String
 */
@ApiModel(description = "The CSRF Token String")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-23T13:26:33.448-08:00")
public class CSRFToken {

  private String value;

  public CSRFToken value(String value) {
    this.value = value;
    return this;
  }

  /**
   * The value of the token
   *
   * @return value
   **/
  @ApiModelProperty(value = "The value of the token")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    if (o instanceof CSRFToken) {
      return this.value != null && this.value.equals(((CSRFToken) o).getValue());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CSRFToken {\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

