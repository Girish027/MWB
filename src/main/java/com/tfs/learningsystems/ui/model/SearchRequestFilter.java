/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.tfs.learningsystems.annotations.ValidDateRange;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;


/**
 * SearchRequestFilter
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class SearchRequestFilter {

  private Boolean tagged = false;

  private Boolean untagged = true;

  private SearchRequestFilterWordCountRange wordCountRange = null;

  private SearchRequestFilterAutoTagCountRange autoTagCountRange = null;

  private List<String> datasets = null;

  private Boolean hasComment = false;

  @ValidDateRange
  private DateRange dateRange = null;

  public SearchRequestFilter tagged(Boolean tagged) {
    this.tagged = tagged;
    return this;
  }

  /**
   * Search only tagged transcriptions
   *
   * @return tagged
   **/
  @ApiModelProperty(value = "Search only tagged transcriptions")
  public Boolean getTagged() {
    return tagged;
  }

  public void setTagged(Boolean tagged) {
    this.tagged = tagged;
  }

  public SearchRequestFilter untagged(Boolean untagged) {
    this.untagged = untagged;
    return this;
  }

  /**
   * Search only untagged transcriptions
   *
   * @return untagged
   **/
  @ApiModelProperty(value = "Search only untagged transcriptions")
  public Boolean getUntagged() {
    return untagged;
  }

  public void setUntagged(Boolean untagged) {
    this.untagged = untagged;
  }

  public SearchRequestFilter wordCountRange(SearchRequestFilterWordCountRange wordCountRange) {
    this.wordCountRange = wordCountRange;
    return this;
  }

  /**
   * Get wordCountRange
   *
   * @return wordCountRange
   **/
  @ApiModelProperty(value = "")
  public SearchRequestFilterWordCountRange getWordCountRange() {
    return wordCountRange;
  }

  public void setWordCountRange(SearchRequestFilterWordCountRange wordCountRange) {
    this.wordCountRange = wordCountRange;
  }

  public SearchRequestFilter autoTagCountRange(
      SearchRequestFilterAutoTagCountRange autoTagCountRange) {
    this.autoTagCountRange = autoTagCountRange;
    return this;
  }

  /**
   * Get autoTagCountRange
   *
   * @return autoTagCountRange
   **/
  @ApiModelProperty(value = "")
  public SearchRequestFilterAutoTagCountRange getAutoTagCountRange() {
    return autoTagCountRange;
  }

  public void setAutoTagCountRange(SearchRequestFilterAutoTagCountRange autoTagCountRange) {
    this.autoTagCountRange = autoTagCountRange;
  }

  public SearchRequestFilter dateRange(DateRange dateRange) {
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


  public SearchRequestFilter hasComment(Boolean hasComment) {
    this.hasComment = hasComment;
    return this;
  }

  /**
   * Get hasComment
   *
   * @return hasComment
   **/
  @ApiModelProperty(value = "")
  public Boolean getHasComment() {
    return hasComment;
  }

  public void setHasComment(Boolean hasComment) {
    this.hasComment = hasComment;
  }

  public SearchRequestFilter datasets(List<String> datasets) {
    this.datasets = datasets;
    return this;
  }

  /**
   * Search only specified datasets
   *
   * @return datasets
   **/
  @ApiModelProperty(value = "Search only specified datasets")
  public List<String> getDatasets() {
    return datasets;
  }

  public void setDatasets(List<String> datasets) {
    this.datasets = datasets;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchRequestFilter searchRequestFilter = (SearchRequestFilter) o;
    return Objects.equals(this.tagged, searchRequestFilter.tagged) &&
        Objects.equals(this.untagged, searchRequestFilter.untagged) &&
        Objects.equals(this.wordCountRange, searchRequestFilter.wordCountRange) &&
        Objects.equals(this.autoTagCountRange, searchRequestFilter.autoTagCountRange) &&
        Objects.equals(this.dateRange, searchRequestFilter.dateRange) &&
        Objects.equals(this.datasets, searchRequestFilter.datasets) &&
        Objects.equals(this.hasComment, searchRequestFilter.hasComment);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(tagged, untagged, wordCountRange, autoTagCountRange, dateRange, hasComment, datasets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchRequestFilter {\n");

    sb.append("    tagged: ").append(toIndentedString(tagged)).append("\n");
    sb.append("    untagged: ").append(toIndentedString(untagged)).append("\n");
    sb.append("    wordCountRange: ").append(toIndentedString(wordCountRange)).append("\n");
    sb.append("    autoTagCountRange: ").append(toIndentedString(autoTagCountRange)).append("\n");
    sb.append("    dateRange: ").append(toIndentedString(dateRange)).append("\n");
    sb.append("    hasComment: ").append(toIndentedString(hasComment)).append("\n");
    sb.append("    datasets: ").append(toIndentedString(datasets)).append("\n");
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

