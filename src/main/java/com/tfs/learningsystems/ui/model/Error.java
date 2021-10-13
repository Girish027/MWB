/*******************************************************************************
 * Copyright © [24]7 Customer, Inc.
 * All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * Error
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-12T17:44:35.590-04:00")
public class Error {

  private Integer code = null;

  private String errorCode = null;

  private String message = null;

  public Error() {
    // Purposefully empty
  }

  /**
   * @param code
   * @param errorCode
   * @param message
   */
  public Error(Integer code, String errorCode, String message) {
    super();
    this.code = code;
    this.errorCode = errorCode;
    this.message = message;
  }


  public Error code(Integer code) {
    this.code = code;
    return this;
  }

  /**
   * HTTP status code
   *
   * @return code
   **/
  @ApiModelProperty(required = true, value = "HTTP status code")
  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public Error errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * Error category
   *
   * @return errorCode
   **/
  @ApiModelProperty(required = true, value = "Error category")
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public Error message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Error message shown in api response
   *
   * @return message
   **/
  @ApiModelProperty(required = true, value = "Error message shown in api response")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.code, error.code)
        && Objects.equals(this.errorCode, error.errorCode)
        && Objects.equals(this.message, error.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, errorCode, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");

    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

  public static Error createError(int httpCode, String message) {

    Error error = new Error();
    error.setCode(httpCode);
    error.setMessage(message);
    return error;
  }

}
