package com.tfs.learningsystems.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;


/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'projects'. we will rename it to 'project' (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "projects")
public class ProjectBO extends BusinessObject<ProjectBO, String> {

  public static final String DB_PREFIX = "prj";
  public static final String FLD_STATE = "state";
  public static final String FLD_CLIENT_ID = "clientId";
  public static final String FLD_PROJECT_ID = "id";
  public static final String FLD_PROJECT_NAME = "name";
  public static final String FLD_ORIGINAL_NAME = "originalName";
  public static final String PREVIEW_MODEL_ID = "previewModelId";
  public static final String LIVE_MODEL_ID = "liveModelId";

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

  public enum Type {
    NODE("NODE"),

    GLOBAL("GLOBAL");

    private String value;

    Type(String value) {
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
  @ApiModelProperty(readOnly = true, value = "internal id generated by MWB", example = "561")
  protected Integer id;

  @Column(name = "cid")
  @ApiModelProperty(readOnly = true, value = "internal client id generated for project", hidden = true)
  protected String cid = DbId.EMPTY.toString();

  @Column(name = "dbid")
  @ApiModelProperty(readOnly = true, value = "database id for the project", hidden = true)
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "group_id")
  @ApiModelProperty(hidden = true)
  protected int groupId;

  @Column(name = "client_id")
  @ApiModelProperty(value = "client id to which this project belongs to", example = "159")
  protected Integer clientId;

  @Column(name = "model_version")
  @ApiModelProperty(readOnly = true, value = "latest version for model", example = "12")
  protected Integer modelVersion = 0;

  @Column(name = "owner_id")
  @ApiModelProperty(value = "owner for this project", example = "ml_user")
  protected String ownerId;

  @Column(name = "name")
  @ApiModelProperty(required = true, value = "project name", example = "root_intent")
  protected String name;

  @Column(name = "original_name")
  @ApiModelProperty(value = "Original project name", example = "root_Intent")
  protected String originalName;

  @Column(name = "description")
  @ApiModelProperty(value = "project description", example = "model to predict journeys")
  protected String description;

  @Column(name = "start_at")
  @ApiModelProperty(hidden = true)
  protected Long startAt;

  @Column(name = "end_at")
  @ApiModelProperty(hidden = true)
  protected Long endAt;

  @Column(name = "vertical")
  @ApiModelProperty(required = true, value = "vertical for this project", allowableValues = "{FINANCIAL,INSURANCE, HEALTHCARE, RETAIL,TECHNOLOGY,TELCO,TRAVEL,UTILITIES,OTHER}", example = "TECHNOLOGY")
  protected String vertical;

  @Column(name = "locale")
  @ApiModelProperty(required = true, value = "locale for this project", example = "en-US")
  protected String locale;

  @Column(name = "state")
  @ApiModelProperty(readOnly = true, value = "current state of the project", example = "ENABLED")
  protected String state;

  @Column(name = "type")
  @ApiModelProperty(readOnly = true, value = "Type of project", example = "NODE")
  protected String type = Type.NODE.value;

  @Column(name = "created_at")
  @ApiModelProperty(readOnly = true, value = "Project creation time", example = "1551998097406")
  protected Long createdAt;

  @Column(name = "modified_at")
  @ApiModelProperty(readOnly = true, value = "Project last modified time", example = "1551998097406")
  protected Long modifiedAt;

  @Column(name = "modified_by")
  @ApiModelProperty(readOnly = true, value = "user id who modified the project", example = "ml_user")
  protected String modifiedBy;

  @JsonInclude(JsonInclude.Include.ALWAYS)
  @Column(name = "deployable_model_id")
  @ApiModelProperty(readOnly = true, value = "deployable model id", example = "12")
  protected Integer deployableModelId;

  @Column(name = "preview_model_id")
  @ApiModelProperty(value = "Preview Model id", example = "12")
  protected String previewModelId;

  @Column(name = "live_model_id")
  @ApiModelProperty(value = "Live Model id", example = "13")
  protected String liveModelId;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public boolean isDisabled() {
    return (State.DISABLED.value.equals(this.state));
  }

  public void setState(State state) {
    this.state = state.value;
  }

  public void setType(Type type) {
    this.type = type.value;
  }

}