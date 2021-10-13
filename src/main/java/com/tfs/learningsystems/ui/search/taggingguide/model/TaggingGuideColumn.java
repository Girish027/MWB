/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * TaggingGuideColumn
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-08T18:54:19.161-07:00")
public class TaggingGuideColumn {

  private String id = null;

  private String name = null;

  private Boolean required = null;

  private String displayName = null;


  public TaggingGuideColumn id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The identifier for the column row
   *
   * @return id
   **/
  @ApiModelProperty(value = "The identifier for the column row")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TaggingGuideColumn name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Column name
   *
   * @return name
   **/
  @ApiModelProperty(required = true, value = "Column name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TaggingGuideColumn required(Boolean required) {
    this.required = required;
    return this;
  }

  /**
   * States whether this is a required column
   *
   * @return required
   **/
  @ApiModelProperty(required = true, value = "States whether this is a required column")
  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public TaggingGuideColumn displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * The display name or the header name of the column
   *
   * @return displayName
   **/
  @ApiModelProperty(value = "The display name or the header name of the column")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaggingGuideColumn taggingGuideColumn = (TaggingGuideColumn) o;
    return Objects.equals(this.id, taggingGuideColumn.id) &&
        Objects.equals(this.name, taggingGuideColumn.name) &&
        Objects.equals(this.required, taggingGuideColumn.required) &&
        Objects.equals(this.displayName, taggingGuideColumn.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, required, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaggingGuideColumn {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
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
}

