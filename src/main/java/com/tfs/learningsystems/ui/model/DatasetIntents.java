/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;


/**
 * DatasetIntents
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-02T22:04:30.572+05:30")
public class DatasetIntents {

  @NotBlank
  private String rutag = null;

  private String intent = null;

  public DatasetIntents rutag(String rutag) {
    this.rutag = rutag;
    return this;
  }

  /**
   * rutag of transcription
   *
   * @return rutag
   **/
  @ApiModelProperty(value = "")
  public String getRutag() {
    return rutag;
  }

  public void setRutag(String rutag) {
    this.rutag = rutag;
  }

  public DatasetIntents intent(String intent) {
    this.intent = intent;
    return this;
  }

  /**
   * intent of transcription
   *
   * @return intent
   **/
  @ApiModelProperty(value = "")
  public String getIntent() {
    return intent;
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatasetIntents datasetStatsResponse = (DatasetIntents) o;
    return Objects.equals(this.intent, datasetStatsResponse.intent) &&
            Objects.equals(this.rutag, datasetStatsResponse.rutag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(intent, rutag);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatasetIntents {\n");
    sb.append("    rutag: ").append(toIndentedString(rutag)).append("\n");
    sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

