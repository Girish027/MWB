package com.tfs.learningsystems.db;

import java.lang.reflect.Field;
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
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "client")
public class ClientBO extends BusinessObject<ClientBO, String> {

  public static final String DB_PREFIX = "clt";

  public static final String FLD_STATE = "state";
  public static final String FLD_IS_VERTICAL = "isVertical";
  public static final String FLD_NAME = "name";
  public static final String FLD_DESCRIPTION = "description";
  public static final String FLD_ADDRESS = "address";
  public static final String FLD_DEPLOYMENT_MODULE = "deploymentModule";


  public enum State {
    ENABLED("ENABLED"),

    DISABLED("DISABLED");

    private String value;

    State(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  @Transient
  private Integer offset = null;

  @Transient
  private Long totalCount = null;

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();       // we will use this to replace the current ID

  @Column(name = "name")
  protected String name;

  @Column(name = "description")
  protected String description;

  @Column(name = "address")
  protected String address;

  @Column(name = "is_vertical")
  protected Boolean isVertical;

  @Column(name = "state")
  protected String state = State.ENABLED.value;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  @Column(name = "deployment_module")
  protected String deploymentModule;

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
      LOGGER.error("Exception while appending clause for CId : ", e);
    }
    return (null);
  }

  public boolean isDisabled() {
    return (State.DISABLED.value.equals(this.state));
  }

  public void setState(State state) {
    this.state = state.value;
  }
}
