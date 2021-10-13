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
 * AddIntentRequest
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-11T23:16:26.062-08:00")
public class AddIntentRequest {

  @NotBlank
  private String intent = null;

  public String getRutag() {

    return rutag;
  }

  public void setRutag(String rutag) {

    this.rutag = rutag;
  }

  private String rutag = null;

  @NotBlank
  private String username = null;

  @NotEmpty
  private List<String> transcriptionHashList = new ArrayList<String>();

  public AddIntentRequest intent(String intent) {
    this.intent = intent;
    return this;
  }

  /**
   * The intent to be tagged.
   *
   * @return intent
   **/
  @ApiModelProperty(required = true, value = "The intent to be tagged.")
  public String getIntent() {
    return intent;
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }

  public AddIntentRequest username(String username) {
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

  public AddIntentRequest transcriptionHashList(List<String> transcriptionHashList) {
    this.transcriptionHashList = transcriptionHashList;
    return this;
  }

  public AddIntentRequest addTranscriptionHashListItem(String transcriptionHashListItem) {
    this.transcriptionHashList.add(transcriptionHashListItem);
    return this;
  }

  /**
   * List of transcriptions to tag.
   *
   * @return transcriptionHashList
   **/
  @ApiModelProperty(required = true, value = "List of transcriptions to tag.")
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
    AddIntentRequest addIntentRequest = (AddIntentRequest) o;
    return Objects.equals(this.intent, addIntentRequest.intent) &&
        Objects.equals(this.username, addIntentRequest.username) &&
        Objects.equals(this.rutag == null ? "" : this.rutag,
            addIntentRequest.rutag == null ? "" : addIntentRequest.rutag) &&
        Objects.equals(this.transcriptionHashList, addIntentRequest.transcriptionHashList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(intent, username, transcriptionHashList, rutag == null ? "" : rutag);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AddIntentRequest {\n");

    sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    rutag: ").append(toIndentedString(rutag == null ? "" : rutag)).append("\n");
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

