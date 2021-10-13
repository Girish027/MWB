package com.tfs.learningsystems.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "model_job_queue")
public class ModelJobQueueBO extends BusinessObject<ModelJobQueueBO, String> {

  public static final String DB_PREFIX = "mjq";

  public static final String FLD_MODEL_ID = "modelId";

  public static enum Status {
    FAILED("FAILED"),
    QUEUED("QUEUED"),
    RUNNING("RUNNING"),
    COMPLETED("COMPLETED");

    private String value;

    Status(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s", this.value);
    }

    public static Status lookup(String value) {
      for (Status modelStatus : Status.values()) {
        if (modelStatus.getValue().equalsIgnoreCase(value)) {
          return modelStatus;
        }
      }
      return null;
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

  @Column(name = "model_id")
  protected String modelId;

  @Column(name = "token")
  protected String token;

  @Column(name = "status")
  protected String status;

  @Column(name = "started_at")
  protected Long startedAt;

  @Column(name = "ended_at")
  protected Long endedAt;

  @Column(name = "model_type")
  protected String modelType;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public void setStatus(Status s) {
    this.status = s.value;
  }
}
