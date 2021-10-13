package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * BatchTestResultsResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-09-18T11:22:12.783-07:00")
public class BatchTestResultsResponse {

  private String projectId = null;

  private String modelId = null;

  private String modelName = null;

  private String modelVersion = null;

  private String modelDescription = null;

  private List<BatchTestInfo> batchTestInfo = new ArrayList<BatchTestInfo>();

  public BatchTestResultsResponse projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * The project id in test
   *
   * @return projectId
   **/
  @ApiModelProperty(required = true, value = "The project id in test")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public BatchTestResultsResponse modelId(String modelId) {
    this.modelId = modelId;
    return this;
  }

  /**
   * The model id in test
   *
   * @return modelId
   **/
  @ApiModelProperty(required = true, value = "The model id in test")
  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public BatchTestResultsResponse modelName(String modelName) {
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

  public BatchTestResultsResponse modelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
    return this;
  }

  /**
   * The version for the model
   *
   * @return modelVersion
   **/
  @ApiModelProperty(required = true, value = "The version for the model")
  public String getModelVersion() {
    return modelVersion;
  }

  public void setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
  }

  public BatchTestResultsResponse modelDescription(String modelDescription) {
    this.modelDescription = modelDescription;
    return this;
  }

  /**
   * The description of the model in test
   *
   * @return modelDescription
   **/
  @ApiModelProperty(value = "The description of the model in test")
  public String getModelDescription() {
    return modelDescription;
  }

  public void setModelDescription(String modelDescription) {
    this.modelDescription = modelDescription;
  }

  public BatchTestResultsResponse batchTestInfo(List<BatchTestInfo> batchTestInfo) {
    this.batchTestInfo = batchTestInfo;
    return this;
  }

  public BatchTestResultsResponse addBatchTestInfoItem(BatchTestInfo batchTestInfoItem) {
    this.batchTestInfo.add(batchTestInfoItem);
    return this;
  }

  /**
   * The information for the specific batch test
   *
   * @return batchTestInfo
   **/
  @ApiModelProperty(value = "The information for the specific batch test")
  public List<BatchTestInfo> getBatchTestInfo() {
    return batchTestInfo;
  }

  public void setBatchTestInfo(List<BatchTestInfo> batchTestInfo) {
    this.batchTestInfo = batchTestInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatchTestResultsResponse batchTestResultsResponse = (BatchTestResultsResponse) o;
    return Objects.equals(this.projectId, batchTestResultsResponse.projectId) &&
        Objects.equals(this.modelId, batchTestResultsResponse.modelId) &&
        Objects.equals(this.modelName, batchTestResultsResponse.modelName) &&
        Objects.equals(this.modelVersion, batchTestResultsResponse.modelVersion) &&
        Objects.equals(this.modelDescription, batchTestResultsResponse.modelDescription) &&
        Objects.equals(this.batchTestInfo, batchTestResultsResponse.batchTestInfo);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(projectId, modelId, modelName, modelVersion, modelDescription, batchTestInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatchTestResultsResponse {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
    sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
    sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
    sb.append("    modelVersion: ").append(toIndentedString(modelVersion)).append("\n");
    sb.append("    modelDescription: ").append(toIndentedString(modelDescription)).append("\n");
    sb.append("    batchTestInfo: ").append(toIndentedString(batchTestInfo)).append("\n");
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

