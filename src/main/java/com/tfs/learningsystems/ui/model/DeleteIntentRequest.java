/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * DeleteIntentRequest
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-11T23:16:26.062-08:00")
public class DeleteIntentRequest {

  @NotBlank
  private String username = null;

  @NotEmpty
  private List<String> transcriptionHashList = new ArrayList<String>();

  public DeleteIntentRequest username(String username) {
    this.username = username;
    return this;
  }

  /**
   * User who is making the change. (Temp)
   *
   * @return username
   **/
  @ApiModelProperty(required = true, value = "User who is making the change. (Temp)")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public DeleteIntentRequest transcriptionHashList(List<String> transcriptionHashList) {
    this.transcriptionHashList = transcriptionHashList;
    return this;
  }

  public DeleteIntentRequest addTranscriptionHashListItem(String transcriptionHashListItem) {
    this.transcriptionHashList.add(transcriptionHashListItem);
    return this;
  }

  /**
   * List of transcriptions to un-tag.
   *
   * @return transcriptionHashList
   **/
  @ApiModelProperty(required = true, value = "List of transcriptions to un-tag.")
  public List<String> getTranscriptionHashList() {
    return transcriptionHashList;
  }

  public void setTranscriptionHashList(List<String> transcriptionHashList) {
    this.transcriptionHashList = transcriptionHashList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeleteIntentRequest deleteIntentRequest = (DeleteIntentRequest) o;
    return Objects.equals(this.username, deleteIntentRequest.username) &&
        Objects.equals(this.transcriptionHashList, deleteIntentRequest.transcriptionHashList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, transcriptionHashList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeleteIntentRequest {\n");

    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    transcriptionHashList: ").append(toIndentedString(transcriptionHashList))
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

