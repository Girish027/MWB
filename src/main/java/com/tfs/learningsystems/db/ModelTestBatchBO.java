package com.tfs.learningsystems.db;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "model_test_batch")
public class ModelTestBatchBO extends BusinessObject<ModelTestBatchBO, String> {

  public static final String DB_PREFIX = "mtb";

  public static final String FLD_PROJECT_ID = "projectId";

  public static final String FLD_CLIENT_ID = "clientId";

  public static final String FLD_MODEL_ID = "modelId";

  @Id
  @Column(name = "id")
  protected String id;

  @Column(name = "project_id")
  protected Long projectId;

  @Column(name = "status")
  protected String status;

  @Column(name = "model_id")
  protected String modelId;

  @Column(name = "request_payload")
  protected String requestPayload;

  @Column(name = "result_file")
  protected String result_file;

  @Column(name = "client_id")
  protected Long clientId;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  @Column(name = "created_by")
  protected String createdBy;

  @Column(name = "modified_by")
  protected String modifiedBy;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public boolean hasDbId() {
    return (false);
  }
}
