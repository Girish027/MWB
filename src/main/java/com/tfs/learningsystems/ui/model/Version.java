/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * Version
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-07-04T11:21:16.376-04:00")
public class Version {

  private Long buildDate = null;

  private String revision = null;

  private String version = null;

  public Version buildDate(Long buildDate) {
    this.buildDate = buildDate;
    return this;
  }

  /**
   * Date of build in MS from epoch
   *
   * @return buildDate
   **/
  @ApiModelProperty(required = true, value = "Date of build in MS from epoch")
  public Long getBuildDate() {
    return buildDate;
  }

  public void setBuildDate(Long buildDate) {
    this.buildDate = buildDate;
  }

  public Version revision(String revision) {
    this.revision = revision;
    return this;
  }

  /**
   * Code Repository revision
   *
   * @return revision
   **/
  @ApiModelProperty(required = true, value = "Code Repository revision")
  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public Version version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version name
   *
   * @return version
   **/
  @ApiModelProperty(required = true, value = "Version name")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version version = (Version) o;
    return Objects.equals(this.buildDate, version.buildDate)
        && Objects.equals(this.revision, version.revision)
        && Objects.equals(this.version, version.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildDate, revision, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Version {\n");

    sb.append("    buildDate: ").append(toIndentedString(buildDate)).append("\n");
    sb.append("    revision: ").append(toIndentedString(revision)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

