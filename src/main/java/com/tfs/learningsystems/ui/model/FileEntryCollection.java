/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * FileEntryCollection
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-22T16:11:15.902-08:00")
public class FileEntryCollection {

  private Integer offset = null;

  private Integer totalCount = null;

  private List<FileEntryDetail> resources = new ArrayList<FileEntryDetail>();

  public FileEntryCollection offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  /**
   * The offset from where the list files is returned
   *
   * @return offset
   **/
  @ApiModelProperty(required = true, value = "The offset from where the list files is returned")
  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public FileEntryCollection totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * The total count of files returned
   *
   * @return totalCount
   **/
  @ApiModelProperty(required = true, value = "The total count of files returned")
  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public FileEntryCollection resources(List<FileEntryDetail> resources) {
    this.resources = resources;
    return this;
  }

  public FileEntryCollection addResourcesItem(FileEntryDetail resourcesItem) {
    this.resources.add(resourcesItem);
    return this;
  }

  /**
   * Get resources
   *
   * @return resources
   **/
  @ApiModelProperty(required = true, value = "")
  public List<FileEntryDetail> getResources() {
    return resources;
  }

  public void setResources(List<FileEntryDetail> resources) {
    this.resources = resources;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileEntryCollection fileEntryCollection = (FileEntryCollection) o;
    return Objects.equals(this.offset, fileEntryCollection.offset) &&
        Objects.equals(this.totalCount, fileEntryCollection.totalCount) &&
        Objects.equals(this.resources, fileEntryCollection.resources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, totalCount, resources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileEntryCollection {\n");

    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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

