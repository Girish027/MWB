package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * BatchTestInfo
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-09-24T09:49:35.343-07:00")
public class BatchTestInfo {

  private String testId = null;

  /**
   * The type of this test should be dataset
   */
  public enum TypeEnum {
    DATASETS("datasets");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  private TypeEnum type = null;

  /**
   * The status of this test
   */
  public enum StatusEnum {
    QUEUED("queued"),

    IN_PROGRESS("in_progress"),

    SUCCESS("success"),

    FAILED("failed");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  private StatusEnum status = null;

  private String message = null;

  private String requestPayload = null;

  private String createdAt = null;

  private String batchTestName = null;

  public BatchTestInfo testId(String testId) {
    this.testId = testId;
    return this;
  }

  /**
   * The id related to this test, only applicable with testing dataset
   *
   * @return testId
   **/
  @ApiModelProperty(required = true, value = "The id related to this test, only applicable with testing dataset")
  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public BatchTestInfo type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * The type of this test should be dataset
   *
   * @return type
   **/
  @ApiModelProperty(required = true, value = "The type of this test should be dataset")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public BatchTestInfo status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * The status of this test
   *
   * @return status
   **/
  @ApiModelProperty(required = true, value = "The status of this test")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public BatchTestInfo message(String message) {
    this.message = message;
    return this;
  }

  /**
   * The free-format message
   *
   * @return message
   **/
  @ApiModelProperty(required = true, value = "The free-format message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public BatchTestInfo requestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
    return this;
  }

  /**
   * Comma separate IDs of input datasets for the test
   *
   * @return requestPayload
   **/
  @ApiModelProperty(required = true, value = "Comma separate IDs of input datasets for the test")
  public String getRequestPayload() {
    return requestPayload;
  }

  public void setRequestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
  }

  public BatchTestInfo createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The time the batch test was created
   *
   * @return createdAt
   **/
  @ApiModelProperty(required = true, value = "The time the batch test was created")
  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public BatchTestInfo batchTestName(String batchTestName) {
    this.batchTestName = batchTestName;
    return this;
  }

  /**
   * the name of the batch test
   *
   * @return batchTestName
   **/
  @ApiModelProperty(required = true, value = "the name of the batch test")
  public String getBatchTestName() {
    return batchTestName;
  }

  public void setBatchTestName(String batchTestName) {
    this.batchTestName = batchTestName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatchTestInfo batchTestInfo = (BatchTestInfo) o;
    return Objects.equals(this.testId, batchTestInfo.testId) &&
        Objects.equals(this.type, batchTestInfo.type) &&
        Objects.equals(this.status, batchTestInfo.status) &&
        Objects.equals(this.message, batchTestInfo.message) &&
        Objects.equals(this.requestPayload, batchTestInfo.requestPayload) &&
        Objects.equals(this.createdAt, batchTestInfo.createdAt) &&
        Objects.equals(this.batchTestName, batchTestInfo.batchTestName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(testId, type, status, message, requestPayload, createdAt, batchTestName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatchTestInfo {\n");

    sb.append("    testId: ").append(toIndentedString(testId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    requestPayload: ").append(toIndentedString(requestPayload)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    batchTestName: ").append(toIndentedString(batchTestName)).append("\n");
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

