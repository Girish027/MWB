/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * AddCommentRequest
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-03-23T19:38:16.183-04:00")
public class AddCommentRequest {

  @Length(max = 1000)
  private String comment = null;

  @NotBlank
  private String username = null;

  @NotEmpty
  private List<String> transcriptionHashList = new ArrayList<String>();

  public AddCommentRequest comment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * The comment to be added.
   *
   * @return comment
   **/
  @ApiModelProperty(value = "The comment to be added.")
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public AddCommentRequest username(String username) {
    this.username = username;
    return this;
  }

  /**
   * User who is making the change.
   *
   * @return username
   **/
  @ApiModelProperty(required = true, value = "User who is making the change.")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public AddCommentRequest transcriptionHashList(List<String> transcriptionHashList) {
    this.transcriptionHashList = transcriptionHashList;
    return this;
  }

  public AddCommentRequest addTranscriptionHashListItem(String transcriptionHashListItem) {
    this.transcriptionHashList.add(transcriptionHashListItem);
    return this;
  }

  /**
   * List of transcriptions to comment.
   *
   * @return transcriptionHashList
   **/
  @ApiModelProperty(required = true, value = "List of transcriptions to comment.")
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
    AddCommentRequest addCommentRequest = (AddCommentRequest) o;
    return Objects.equals(this.comment, addCommentRequest.comment) &&
        Objects.equals(this.username, addCommentRequest.username) &&
        Objects.equals(this.transcriptionHashList, addCommentRequest.transcriptionHashList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(comment, username, transcriptionHashList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AddCommentRequest {\n");

    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
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

