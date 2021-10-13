package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * VerifiedTranscriptionsResponse
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-21T16:55:37.740-07:00")
public class VerifiedTranscriptionsResponse {

  private Integer startIndex = null;

  private Integer limit = null;

  private Long total = null;

  private List<VerifiedTranscriptionDocument> transcriptionList = new ArrayList<VerifiedTranscriptionDocument>();

  public VerifiedTranscriptionsResponse startIndex(Integer startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  /**
   * The startIndex sent in the request
   *
   * @return startIndex
   **/
  @ApiModelProperty(value = "The startIndex sent in the request")
  public Integer getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
  }

  public VerifiedTranscriptionsResponse limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  /**
   * The limit sent in the request
   *
   * @return limit
   **/
  @ApiModelProperty(value = "The limit sent in the request")
  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public VerifiedTranscriptionsResponse total(Long total) {
    this.total = total;
    return this;
  }

  /**
   * Total unique transcriptions that fall into the search criteria
   *
   * @return total
   **/
  @ApiModelProperty(value = "Total unique transcriptions that fall into the search criteria")
  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public VerifiedTranscriptionsResponse transcriptionList(
      List<VerifiedTranscriptionDocument> transcriptionList) {
    this.transcriptionList = transcriptionList;
    return this;
  }

  public VerifiedTranscriptionsResponse addTranscriptionListItem(
      VerifiedTranscriptionDocument transcriptionListItem) {
    this.transcriptionList.add(transcriptionListItem);
    return this;
  }

  /**
   * Get transcriptionList
   *
   * @return transcriptionList
   **/
  @ApiModelProperty(value = "")
  public List<VerifiedTranscriptionDocument> getTranscriptionList() {
    return transcriptionList;
  }

  public void setTranscriptionList(List<VerifiedTranscriptionDocument> transcriptionList) {
    this.transcriptionList = transcriptionList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifiedTranscriptionsResponse verifiedTranscriptionsResponse = (VerifiedTranscriptionsResponse) o;
    return Objects.equals(this.startIndex, verifiedTranscriptionsResponse.startIndex) &&
        Objects.equals(this.limit, verifiedTranscriptionsResponse.limit) &&
        Objects.equals(this.total, verifiedTranscriptionsResponse.total) &&
        Objects.equals(this.transcriptionList, verifiedTranscriptionsResponse.transcriptionList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startIndex, limit, total, transcriptionList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifiedTranscriptionsResponse {\n");

    sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    transcriptionList: ").append(toIndentedString(transcriptionList)).append("\n");
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

