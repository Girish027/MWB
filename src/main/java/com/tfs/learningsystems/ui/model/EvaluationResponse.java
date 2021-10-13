package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * EvaluationResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
public class EvaluationResponse {

  private String projectId = null;

  private String modelId = null;

  private String testId = null;

  /**
   * The type of this test
   */
  public enum TypeEnum {
    UTTERANCES("utterances"),

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
    QUEUED("QUEUED"),    // change the value to captal cases, save the trouble of conversion

    IN_PROGRESS("IN_PROGRESS"),

    SUCCESS("SUCCESS"),

    FAILED("FAILED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  private StatusEnum status = StatusEnum.FAILED;

  private String message = null;

  private List<UtteranceEvaluation> evaluations = new ArrayList<UtteranceEvaluation>();

  public EvaluationResponse projectId(String projectId) {
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

  public EvaluationResponse modelId(String modelId) {
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

  public EvaluationResponse testId(String testId) {
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

  public EvaluationResponse type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * The type of this test
   *
   * @return type
   **/
  @ApiModelProperty(required = true, value = "The type of this test")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public EvaluationResponse status(StatusEnum status) {
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

  public EvaluationResponse message(String message) {
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

  public EvaluationResponse evaluations(List<UtteranceEvaluation> evaluations) {
    this.evaluations = evaluations;
    return this;
  }

  public EvaluationResponse addEvaluationsItem(UtteranceEvaluation evaluationsItem) {
    this.evaluations.add(evaluationsItem);
    return this;
  }

  /**
   * The evaluation for each sentence sent by the caller
   *
   * @return evaluations
   **/
  @ApiModelProperty(value = "The evaluation for each sentence sent by the caller")
  public List<UtteranceEvaluation> getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(List<UtteranceEvaluation> evaluations) {
    this.evaluations = evaluations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EvaluationResponse evaluationResponse = (EvaluationResponse) o;
    return Objects.equals(this.projectId, evaluationResponse.projectId) &&
        Objects.equals(this.modelId, evaluationResponse.modelId) &&
        Objects.equals(this.testId, evaluationResponse.testId) &&
        Objects.equals(this.type, evaluationResponse.type) &&
        Objects.equals(this.status, evaluationResponse.status) &&
        Objects.equals(this.message, evaluationResponse.message) &&
        Objects.equals(this.evaluations, evaluationResponse.evaluations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectId, modelId, testId, type, message, evaluations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EvaluationResponse {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
    sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
    sb.append("    testId: ").append(toIndentedString(testId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    evaluations: ").append(toIndentedString(evaluations)).append("\n");
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

