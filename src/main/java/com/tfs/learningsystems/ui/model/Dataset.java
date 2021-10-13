/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.annotations.ValidDataType;
import io.swagger.annotations.ApiModelProperty;
import java.util.Calendar;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;


/**
 * Dataset
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-02T22:04:30.572+05:30")
public class Dataset {

  @NotBlank
  private String name = null;

  @NotNull
  @Pattern(regexp = "\\d+")
  private String clientId = null;

  private Long receivedAt = Calendar.getInstance().getTimeInMillis();

  @NotBlank
  @URL
  private String uri = null;

  @ValidDataType
  private DataType dataType = null;

  public String getProjectId() {

    return projectId;
  }

  public void setProjectId(String projectId) {

    this.projectId = projectId;
  }

  private String projectId = null;

  private String description = null;

  private String locale = null;

  public Dataset name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Dataset name
   *
   * @return name
   **/
  @ApiModelProperty(required = true, value = "Dataset name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Dataset clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * id of the client this dataset came from
   *
   * @return clientId
   **/
  @ApiModelProperty(required = true, value = "id of the client this dataset came from")
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Dataset receivedAt(Long receivedAt) {
    this.receivedAt = receivedAt;
    return this;
  }

  /**
   * The UTC date time for when the dataset was recieved
   *
   * @return receivedAt
   **/
  @ApiModelProperty(value = "The UTC date time for when the dataset was recieved")
  public Long getReceivedAt() {
    return receivedAt;
  }

  public void setReceivedAt(Long receivedAt) {
    this.receivedAt = receivedAt;
  }

  public Dataset uri(String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Uri within repository
   *
   * @return uri
   **/
  @ApiModelProperty(required = true, value = "Uri within repository")
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Dataset dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  /**
   * Get dataType
   *
   * @return dataType
   **/
  @ApiModelProperty(required = true, value = "")
  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public Dataset description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Short description of the Dataset
   *
   * @return description
   **/
  @ApiModelProperty(value = "Short description of the Dataset")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Dataset locale(String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * The BCP 47 code for dataset language.
   *
   * @return locale
   **/
  @ApiModelProperty(value = "The BCP 47 code for dataset language.")
  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dataset dataset = (Dataset) o;
    return Objects.equals(this.name, dataset.name) &&
        Objects.equals(this.clientId, dataset.clientId) &&
        Objects.equals(this.receivedAt, dataset.receivedAt) &&
        Objects.equals(this.uri, dataset.uri) &&
        Objects.equals(this.dataType, dataset.dataType) &&
        Objects.equals(this.description, dataset.description) &&
        Objects.equals(this.locale, dataset.locale);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, clientId, receivedAt, uri, dataType, description, locale);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Dataset {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    receivedAt: ").append(toIndentedString(receivedAt)).append("\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
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
   * @return JSON string of Project object
   */
  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }
}

