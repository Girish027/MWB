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
@Table(name = "model_deployment_map")
public class ModelDeploymentMapBO extends BusinessObject<ModelDeploymentMapBO, String> {

  public static final String DB_PREFIX = "mdm";

  public static final String FLD_MDD_ID = "id";

  public static final String FLD_DEPLOYMENT_ID = "deploymentId";

  public static final String FLD_GIT_HUB_TAG = "gitHubTag";

  public static final String FLD_PROJECT_ID = "projectId";

  public static final String FLD_MODEL_ID = "modelId";


  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "deployment_id")
  protected Integer deploymentId;

  @Column(name = "git_hub_tag")
  protected String gitHubTag;

  @Column(name = "project_id")
  protected Integer projectId;

  @Column(name = "model_id")
  protected String modelId;


  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }


}





