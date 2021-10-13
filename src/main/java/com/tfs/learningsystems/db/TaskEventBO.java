package com.tfs.learningsystems.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'taskevents'. we will rename it to 'taskevent' (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "taskevents")
public class TaskEventBO extends BusinessObject<TaskEventBO, String> {

  public static final String DB_PREFIX = "tet";
  public static final String FLD_JOB_ID = "jobId";

  @JsonIgnore
  @Transient
  private Integer percentComplete = 0;

  public enum TaskType {
    CATEGORIZE("CATEGORIZE"),
    INDEX("INDEX");

    private String value;

    TaskType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

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

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();

  @Column(name = "dbid")
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "job_id")
  protected int jobId;

  @Column(name = "task")
  protected String task = TaskType.CATEGORIZE.value;

  @Column(name = "status")
  protected String status = Status.QUEUED.value;

  @Column(name = "error_code")
  protected String errorCode = ErrorCode.OK.value;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  @Column(name = "records_imported")
  protected Long recordsImported;

  @Column(name = "records_processed")
  protected Long recordsProcessed;

  @Column(name = "message")
  protected String message;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public void setTask(TaskType type) {
    this.task = type.value;
  }

  public void setStatus(Status s) {
    this.status = s.value;
  }

  @JsonIgnore
  public boolean isComplete() {
    return Objects.equals(this.status, Status.COMPLETED.value);
  }

  @JsonIgnore
  public boolean isCanceled() {
    return Objects.equals(this.status, Status.CANCELLED.value);
  }

  @JsonIgnore
  public void setErrorCode() {
    if (this.task.equals(TaskType.CATEGORIZE.value)) {
      this.errorCode = ErrorCode.CATFAILED.value;
    } else {
      this.errorCode = ErrorCode.ELKFAILED.value;
    }
  }

  @JsonIgnore
  public ErrorCode getFailedErrorCodeForTask() {
    return this.task.equals(TaskType.CATEGORIZE) ? ErrorCode.CATFAILED : ErrorCode.ELKFAILED;
  }

  /**
   * The percent that the job is complete
   *
   * @return percentComplete
   **/
  @JsonIgnore
  @ApiModelProperty(required = false, value = "The percent that the job is complete")
  public Integer getPercentComplete() {
    // consolidate from job manager to here
    percentComplete = getTask().equals(TaskType.CATEGORIZE.toString()) ? 0 : 50;
    switch (getStatus()) {
      case "QUEUED":
        percentComplete += 0;
        break;
      case "STARTED":
        percentComplete += 10;
        break;
      case "RUNNING":
        percentComplete += 25;
        break;
      case "COMPLETED":
        percentComplete += 50;
        break;
      default:
        percentComplete = 0;
    }

    return percentComplete;
  }

  @JsonIgnore
  public void setPercentComplete(Integer percentComplete) {
    this.percentComplete = percentComplete;
  }


}
