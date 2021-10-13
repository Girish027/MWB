package com.tfs.learningsystems.db;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thymeleaf.util.StringUtils;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "dataset_intent_inheritance")
public class DatasetIntentInheritanceBO extends BusinessObject<DatasetIntentInheritanceBO, String> {

  public static final String DB_PREFIX = "dii";
  public static final String FLD_PROJECT_ID = "projectId";

  public enum Status {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED");

    private final String value;

    Status(String status) {
      this.value = status;
    }
  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();       // we will use this to replace the current ID

  @Column(name = "dataset_id")
  protected int datasetId;

  @Column(name = "project_id")
  protected int projectId;

  @Column(name = "requested_at")
  protected long requestedAt;

  @Column(name = "requested_by")
  protected String requestedBy;

  @Column(name = "total_tagged")
  protected long totalTagged;

  @Column(name = "unique_tagged")
  protected long uniqueTagged;

  @Column(name = "total_tagged_multiple_intents")
  protected long totalTaggedMulipleIntents;

  @Column(name = "unique_tagged_multiple_intents")
  protected long uniqueTaggedMulipleIntents;

  @Column(name = "inherited_from_dataset_ids")
  protected String inheritedFromDatasetIds;

  @Column(name = "status")
  protected String status;

  @Column(name = "updated_at")
  protected long updatedAt;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public Field getDbIdField() {
    try {
      Field dbIdField = this.getClass().getDeclaredField(CLIENT_CID);
      return (dbIdField);
    } catch (Exception e) {
      LOGGER.error("Exception while getting DBId: ", e);
    }

    return (null);
  }

  public void setDatasetIds(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      inheritedFromDatasetIds = null;
    } else {
      inheritedFromDatasetIds = String.join(", ", ids);
      ;
    }
  }

  public List<String> getDatasetIds() {
    if (StringUtils.isEmpty(this.inheritedFromDatasetIds)) {
      return (new LinkedList<String>());
    }
    return (Arrays.asList(this.inheritedFromDatasetIds.split(",")));
  }


  public boolean isDisabled() {
    return (Status.COMPLETED.value.equals(this.status));
  }

  public void setStatus(Status s) {
    this.status = s.value;
  }
}
