/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. 
 * All Rights Reserved. 
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;


/**
 * Error
 */
public class ValidationError extends Error {


  private List<ValidationError.FieldError> errors = null;

  public ValidationError errors(List<ValidationError.FieldError> errors) {
    this.errors = errors;
    return this;
  }

  /**
   * list of field errors shown in api response
   *
   * @return errors
   **/
  @ApiModelProperty(required = true, value = "list of field errors shown in api response")
  public List<ValidationError.FieldError> getErrors() {
    return errors;
  }

  public void setErrors(List<ValidationError.FieldError> errors) {
    this.errors = errors;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationError error = (ValidationError) o;
    return Objects.equals(this.errors, error.errors)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errors, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidationError {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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
   * @author jkarpala
   */
  public static class FieldError {

    private String field;
    private String error;

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    @Override
    public boolean equals(java.lang.Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FieldError error = (FieldError) o;
      return Objects.equals(this.error, error.error)
          && Objects.equals(this.field, error.field);
    }

    @Override
    public int hashCode() {
      return Objects.hash(error, field);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class FieldError {\n");
      sb.append("    field: ").append(toIndentedString(field)).append("\n");
      sb.append("    error: ").append(toIndentedString(error)).append("\n");
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
}

