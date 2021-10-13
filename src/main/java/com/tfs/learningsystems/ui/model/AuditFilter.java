/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.tfs.learningsystems.annotations.ValidDateRange;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * AuditFilter
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class AuditFilter {

  private String type = null;

  @ValidDateRange
  private DateRange dateRange = null;

  private String taggedBy = null;

  private String deletedBy = null;

  private String intent = null;

  public AuditFilter type(String type) {
    this.type = type;
    return this;
  }

  /**
   * tagged/untagged
   *
   * @return type
   **/
  @ApiModelProperty(value = "tagged/untagged")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AuditFilter dateRange(DateRange dateRange) {
    this.dateRange = dateRange;
    return this;
  }

  /**
   * Get dateRange
   *
   * @return dateRange
   **/
  @ApiModelProperty(value = "")
  public DateRange getDateRange() {
    return dateRange;
  }

  public void setDateRange(DateRange dateRange) {
    this.dateRange = dateRange;
  }

  public AuditFilter taggedBy(String taggedBy) {
    this.taggedBy = taggedBy;
    return this;
  }

  /**
   * User who tagged text strings
   *
   * @return taggedBy
   **/
  @ApiModelProperty(value = "User who tagged text strings")
  public String getTaggedBy() {
    return taggedBy;
  }

  public void setTaggedBy(String taggedBy) {
    this.taggedBy = taggedBy;
  }

  public AuditFilter deletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
    return this;
  }

  /**
   * User who deleted text strings
   *
   * @return deletedBy
   **/
  @ApiModelProperty(value = "User who deleted text strings")
  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  public AuditFilter intent(String intent) {
    this.intent = intent;
    return this;
  }

  /**
   * Intent in the tagged or untagged text strings
   *
   * @return intent
   **/
  @ApiModelProperty(value = "Intent in the tagged or untagged text strings")
  public String getIntent() {
    return intent;
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuditFilter auditFilter = (AuditFilter) o;
    return Objects.equals(this.type, auditFilter.type) &&
        Objects.equals(this.dateRange, auditFilter.dateRange) &&
        Objects.equals(this.taggedBy, auditFilter.taggedBy) &&
        Objects.equals(this.deletedBy, auditFilter.deletedBy) &&
        Objects.equals(this.intent, auditFilter.intent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, dateRange, taggedBy, deletedBy, intent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuditFilter {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    dateRange: ").append(toIndentedString(dateRange)).append("\n");
    sb.append("    taggedBy: ").append(toIndentedString(taggedBy)).append("\n");
    sb.append("    deletedBy: ").append(toIndentedString(deletedBy)).append("\n");
    sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
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

