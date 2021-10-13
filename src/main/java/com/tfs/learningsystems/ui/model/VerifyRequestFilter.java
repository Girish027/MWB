package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * VerifyRequestFilter
 */
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-08-21T16:55:37.740-07:00")
public class VerifyRequestFilter {

  private Boolean onlyConflicts = false;
  private Boolean onlyTagged = true;

  private List<String> datasetIds = new ArrayList<>();

  public VerifyRequestFilter onlyConflicts(Boolean onlyConflicts) {
    this.onlyConflicts = onlyConflicts;
    return this;
  }

  /**
   * Return only transcriptions with conflic intent tags
   *
   * @return onlyConflicts
   **/
  @ApiModelProperty(
      value = "Return only transcriptions with conflicting intent tags")
  public Boolean getOnlyConflicts() {
    return onlyConflicts;
  }

  public void setOnlyConflicts(Boolean onlyConflicts) {
    this.onlyConflicts = onlyConflicts;
  }

  public VerifyRequestFilter onlyTagged(Boolean onlyTagged) {
    this.onlyTagged = onlyTagged;
    return this;
  }

  /**
   * Return only transcriptions with tags
   *
   * @return onlyTagged
   **/
  @ApiModelProperty(value = "Return only transcriptions with intent tags")
  public Boolean getOnlyTagged() {
    return onlyTagged;
  }

  public void setOnlyTagged(Boolean onlyTagged) {
    this.onlyTagged = onlyTagged;
  }

  public VerifyRequestFilter datasetIds(List<String> datasetIds) {
    this.datasetIds = datasetIds;
    return this;
  }

  public VerifyRequestFilter addDatasetIdsItem(String datasetIdsItem) {
    this.datasetIds.add(datasetIdsItem);
    return this;
  }

  /**
   * Get datasetIds
   *
   * @return datasetIds
   **/
  @ApiModelProperty(value = "")
  public List<String> getDatasetIds() {
    return datasetIds;
  }

  public void setDatasetIds(List<String> datasetIds) {
    this.datasetIds = datasetIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifyRequestFilter verifyRequestFilter = (VerifyRequestFilter) o;
    return Objects
        .equals(this.onlyConflicts, verifyRequestFilter.onlyConflicts)
        && Objects
        .equals(this.onlyTagged, verifyRequestFilter.onlyTagged)
        && Objects
        .equals(this.datasetIds, verifyRequestFilter.datasetIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(onlyConflicts, onlyTagged, datasetIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifyRequestFilter {\n");

    sb.append("    onlyConflicts: ").append(toIndentedString(onlyConflicts))
        .append("\n");
    sb.append("    onlyTagged: ").append(toIndentedString(onlyTagged))
        .append("\n");
    sb.append("    datasetIds: ").append(toIndentedString(datasetIds))
        .append("\n");
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

