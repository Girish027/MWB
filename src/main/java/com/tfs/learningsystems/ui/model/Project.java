/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;


/**
 * Project
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-27T13:56:05.802-04:00")
public class Project {

  @NotBlank
  @ApiModelProperty(required = true, value = "project name", example = "root_intent")
  private String name = null;

  @ApiModelProperty(value = "project original name", example = "root_intent")
  private String originalName = null;

  @NotNull
  @Pattern(regexp = "\\d+")
  @ApiModelProperty(value = "client id to which this project belongs to", example = "159")
  private String clientId = null;

  @ApiModelProperty(value = "owner for this project", example = "ml_user")
  private String ownerId = null;

  @ApiModelProperty(hidden = true)
  private String groupId = null;

  @ApiModelProperty(required = true, value = "vertical for this project", allowableValues = "{FINANCIAL,INSURANCE, HEALTHCARE, RETAIL,TECHNOLOGY,TELCO,TRAVEL,UTILITIES,OTHER}", example = "TECHNOLOGY")
  private Vertical vertical = null;

  @ApiModelProperty(value = "project description", example = "model to predict journeys")
  private String description = null;


  @ApiModelProperty(required = true, value = "locale for this project", example = "en-US")
  private String locale = null;

  /**
   * Gets or Sets state
   */
  public enum StateEnum {
    ENABLED("ENABLED"),

    DISABLED("DISABLED");

    private String value;

    StateEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static StateEnum GetState(String val) {
      if (ENABLED.value.equals(val)) {
        return (ENABLED);
      } else {
        return (DISABLED);
      }
    }
  }

  private StateEnum state = StateEnum.ENABLED;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    NODE("NODE"),

    GLOBAL("GLOBAL");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum GetType(String val) {
      if (NODE.value.equalsIgnoreCase(val)) {
        return (NODE);
      } else {
        return (GLOBAL);
      }
    }
  }

  private TypeEnum type = TypeEnum.NODE;

  public Project name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Project original_name
   *
   * @return name
   **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Project originalName(String originalName) {
    this.originalName = originalName;
    return this;
  }

  /**
   * Project originalName
   *
   * @return originalName
   **/
  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public Project clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * id of the client this project belongs to
   *
   * @return clientId
   **/
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Project ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  /**
   * id of the user this project belongs to
   *
   * @return ownerId
   **/
  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public Project groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  /**
   * id of the group for this project
   *
   * @return groupId
   **/
  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public Project vertical(Vertical vertical) {
    this.vertical = vertical;
    return this;
  }

  /**
   * Get vertical
   *
   * @return vertical
   **/
  public Vertical getVertical() {
    return vertical;
  }

  public void setVertical(Vertical vertical) {
    this.vertical = vertical;
  }

  public Project description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Short description of the Project
   *
   * @return description
   **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Project locale(String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * The BCP 47 code for project language.
   *
   * @return locale
   **/
  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public Project state(StateEnum state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   *
   * @return state
   **/
  public StateEnum getState() {
    return state;
  }

  public void setState(StateEnum state) {
    this.state = state;
  }

  /**
   * Get type
   *
   * @return type
   **/
  public TypeEnum getType() { return type; }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Project project = (Project) o;
    return Objects.equals(this.name, project.name)
        && Objects.equals(this.clientId, project.clientId)
        && Objects.equals(this.ownerId, project.ownerId)
        && Objects.equals(this.groupId, project.groupId)
        && Objects.equals(this.vertical, project.vertical)
        && Objects.equals(this.description, project.description)
        && Objects.equals(this.locale, project.locale)
        && Objects.equals(this.state, project.state)
        && Objects.equals(this.type, project.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, clientId, ownerId, groupId, vertical, description, locale,
        state, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Project {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    vertical: ").append(toIndentedString(vertical)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
   * @return JSON string of Project object
   */
  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }
}
