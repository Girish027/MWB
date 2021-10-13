package com.tfs.learningsystems.db;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'datasets_projects'. we will renaem it to 'project_dataset' (without the s
 * )
 */
@Data
@IdClass(ProjectDatasetBO.ProjectDatasetId.class)
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "datasets_projects")
public class ProjectDatasetBO extends BusinessObject<ProjectDatasetBO, String> {

  public static final String DB_PREFIX = "pdm";
  public static final String FLD_CID = "cid";
  public static final String FLD_PROJECT_ID = "projectId";
  public static final String FLD_DATASET_ID = "datasetId";


  @Id
  @Column(name = "project_id")
  protected Integer projectId;

  @Id
  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();

  @Id
  @Column(name = "dataset_id")
  protected Integer datasetId;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public boolean hasId() {
    return (false);
  }

  @Override
  public boolean hasDbId() {
    return (false);
  }

  public static class ProjectDatasetId implements Serializable {

    protected Integer projectId;
    protected String cid = DbId.EMPTY.toString();
    protected Integer datasetId;

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ProjectDatasetId castedObj = (ProjectDatasetId) obj;
      return (castedObj.projectId == this.projectId &&
          castedObj.cid == this.cid &&
          castedObj.datasetId == this.datasetId);
    }

    @Override
    public int hashCode() {
      StringBuilder sb = new StringBuilder().append(this.projectId)
          .append("__")
          .append(this.cid)
          .append("__")
          .append(this.datasetId);
      return sb.toString().hashCode();
    }

  }
}
