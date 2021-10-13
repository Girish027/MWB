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
@Table(name = "model_deployment_details")
public class ModelDeploymentDetailsBO extends BusinessObject<ModelDeploymentDetailsBO, String> {

  public static final String DB_PREFIX = "mdd";

  public static final String FLD_MDD_ID = "id";

  public static final String FLD_CLIENT_ID = "clientId";

  public static final String FLD_GIT_HUB_TAG = "gitHubTag";

  public static final String FLD_ACTIVE = "isActive";

  public static final String FLD_STATUS = "status";

  public static final String FLD_DEPLOYMENT_ID = "deploymentJobId";

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "client_id")
  protected Integer clientId;

  @Column(name = "git_hub_tag")
  protected String gitHubTag;

  @Column(name = "deployment_job_id")
  protected String deploymentJobId;

  @Column(name = "deployed_by")
  protected String deployedBy;

  @Column(name = "deployed_start")
  protected Long deployedStart;

  @Column(name = "deployed_end")
  protected Long deployedEnd;

  @Column(name = "status")
  protected String status;

  @Column(name = "is_active")
  protected String isActive;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public void setStatus(ModelDeploymentDetailsBO.Status status) {
    this.status = status.value;
  }

  public void setIsActive(ModelDeploymentDetailsBO.Active isActive) {
    this.isActive = isActive.value;
  }


  public static enum Status {
    STARTED("STARTED"),
    TAGGED("TAGGED"),
    CREATED("CREATED"),
    ACTIVATED("ACTIVATED");

    private String value;

    Status(String value) {
      this.value = value;
    }

    public static Status lookup(String value) {
      for (Status deploymentStatus : Status.values()) {
        if (deploymentStatus.getValue().equalsIgnoreCase(value)) {
          return deploymentStatus;
        }
      }
      return null;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s", this.value);
    }
  }


  public static enum Active {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private String value;

    Active(String value) {
      this.value = value;
    }

    public static Active lookup(String value) {
      for (Active modelActiveStatus : Active.values()) {
        if (modelActiveStatus.getValue().equalsIgnoreCase(value)) {
          return modelActiveStatus;
        }
      }
      return null;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s", this.value);
    }
  }


}


