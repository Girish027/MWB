/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.db.TaskEventBO;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public class TaskEvent {

  @NotEmpty
  private String jobId = null;

  @NotEmpty
  private Task.Name task = null;

  public enum Status {
    NULL("NULL"), STARTED("STARTED"), QUEUED("QUEUED"), RUNNING("RUNNING"), COMPLETED(
        "COMPLETED"), CANCELLED("CANCELLED"), FAILED("FAILED");

    private String value;

    Status(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  @NotNull
  private Status status = Status.STARTED;

  public enum ErrorCode {
    OK("OK"), CANCELLED("CANCELLED"), ELKNOTFOUND("ELKNOTFOUND"), ELKFAILED(
        "ELKFAILED"), CATFAILED("CATFAILED");

    private String value;

    ErrorCode(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  @JsonIgnore
  private ErrorCode errorCode = null;

  private String message = null;

  public TaskEvent jobId(String jobId) {
    this.jobId = jobId;
    return this;
  }

  /**
   * JobId of associated job
   *
   * @return jobId
   **/
  @ApiModelProperty(required = true, value = "JobId of associated job")
  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public TaskEvent task(Task.Name task) {
    this.task = task;
    return this;
  }

  /**
   * Task type
   *
   * @return task
   **/
  @ApiModelProperty(required = true, value = "Task type")
  public Task.Name getTask() {
    return task;
  }

  public void setTask(Task.Name task) {
    this.task = task;
  }

  public TaskEvent status(Status status) {
    this.status = status;
    return this;
  }

  /**
   * Status of task event
   *
   * @return status
   **/
  @ApiModelProperty(required = true, value = "Status of task event")
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public TaskEvent errorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * Task event code
   *
   * @return errorCode
   **/
  @ApiModelProperty(required = true, value = "Task event code")
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public TaskEvent message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Task event message
   *
   * @return message
   **/
  @ApiModelProperty(required = true, value = "Task event message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @JsonIgnore
  public boolean isComplete() {
    return Objects.equals(this.status, TaskEvent.Status.COMPLETED);
  }

  @JsonIgnore
  public boolean isCanceled() {
    return Objects.equals(this.status, TaskEvent.Status.CANCELLED);
  }

  @JsonIgnore
  public ErrorCode getFailedErrorCodeForTask() {
    return this.task.equals(TaskEventBO.TaskType.CATEGORIZE) ? ErrorCode.CATFAILED
        : ErrorCode.ELKFAILED;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskEvent taskEvent = (TaskEvent) o;
    return Objects.equals(this.jobId, taskEvent.jobId)
        && Objects.equals(this.task, taskEvent.task)
        && Objects.equals(this.status, taskEvent.status)
        && Objects.equals(this.errorCode, taskEvent.errorCode)
        && Objects.equals(this.message, taskEvent.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jobId, task, status, errorCode, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaskEvent {\n");

    sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
    sb.append("    task: ").append(toIndentedString(task)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    code: ").append(toIndentedString(errorCode)).append("\n");
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

  /**
   * @return JSON string of TaskEvent object
   */
  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }
}
