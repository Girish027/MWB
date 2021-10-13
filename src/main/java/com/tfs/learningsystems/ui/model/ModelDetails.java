package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * ModelDetails
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-09-18T11:22:12.783-07:00")
public class ModelDetails {

  private String projectId = null;

  private String modelId = null;

  private String modelName = null;

  private String clientId = null;

  public ModelDetails projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * The project id in test
   *
   * @return projectId
   **/
  @ApiModelProperty(required = true, value = "The project id")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public ModelDetails modelId(String modelId) {
    this.modelId = modelId;
    return this;
  }

  /**
   * The model id
   *
   * @return modelId
   **/
  @ApiModelProperty(required = true, value = "The model id")
  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public ModelDetails modelName(String modelName) {
    this.modelName = modelName;
    return this;
  }

  /**
   * The name of the model
   *
   * @return modelName
   **/
  @ApiModelProperty(required = true, value = "The name of the model")
  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public ModelDetails clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * The client Id
   *
   * @return clientId
   **/
  @ApiModelProperty(required = true, value = "The client Id")
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelDetails modelDetails = (ModelDetails) o;
    return Objects.equals(this.projectId, modelDetails.projectId) &&
            Objects.equals(this.modelId, modelDetails.modelId) &&
            Objects.equals(this.modelName, modelDetails.modelName) &&
            Objects.equals(this.clientId, modelDetails.clientId);
  }

  @Override
  public int hashCode() {
    return Objects
            .hash(projectId, modelId, modelName, clientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelDetails {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
    sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
    sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
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

