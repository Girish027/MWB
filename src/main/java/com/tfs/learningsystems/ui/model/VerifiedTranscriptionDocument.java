package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;


/**
 * VerifiedTranscriptionDocument
 */
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-08-21T16:55:37.740-07:00")
public class VerifiedTranscriptionDocument implements Cloneable {

  private static final int N_CHARACTERS = 64;

  private String intent = null;

  private String suggestedIntent = null;

  private Boolean intentConflict = false;

  private Set<String> intents = new HashSet<>();

  private Long documentCount = null;

  private String transcriptionHash = null;

  private String textStringForTagging = null;

  private String normalizedForm = null;

  private Integer normalizedFormGroup = null;

  public VerifiedTranscriptionDocument intent(String intent) {
    this.intent = intent;
    return this;
  }

  /**
   * Get intent
   *
   * @return intent
   **/
  @ApiModelProperty(value = "")
  public String getIntent() {
    return intent;
  }

  @JsonIgnore
  public String getSortableIntent() {
    if (intent == null || intent.isEmpty()) {
      return intent;
    } else {
      return intent.replace('-', ' ').replace('_', ' ');
    }
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }

  public VerifiedTranscriptionDocument suggestedIntent(
      String suggestedIntent) {
    this.suggestedIntent = suggestedIntent;
    return this;
  }

  /**
   * Get suggestedIntent
   *
   * @return suggestedIntent
   **/
  @ApiModelProperty(value = "")
  public String getSuggestedIntent() {
    return suggestedIntent;
  }

  public void setSuggestedIntent(String suggestedIntent) {
    this.suggestedIntent = suggestedIntent;
  }

  public VerifiedTranscriptionDocument intentConflict(
      Boolean intentConflict) {
    this.intentConflict = intentConflict;
    return this;
  }

  /**
   * Get intentConflict
   *
   * @return intentConflict
   **/
  @ApiModelProperty(value = "")
  public Boolean getIntentConflict() {
    return intentConflict;
  }

  public void setIntentConflict(Boolean intentConflict) {
    this.intentConflict = intentConflict;
  }

  public VerifiedTranscriptionDocument intents(Set<String> intents) {
    this.intents = intents;
    return this;
  }

  public VerifiedTranscriptionDocument addIntentsItem(String intentsItem) {
    this.intents.add(intentsItem);
    return this;
  }

  /**
   * Get intents
   *
   * @return intents
   **/
  @ApiModelProperty(value = "")
  public Set<String> getIntents() {
    return intents;
  }

  public void setIntents(Set<String> intents) {
    this.intents = intents;
  }

  public VerifiedTranscriptionDocument documentCount(Long documentCount) {
    this.documentCount = documentCount;
    return this;
  }

  /**
   * Get documentCount
   *
   * @return documentCount
   **/
  @ApiModelProperty(value = "")
  public Long getDocumentCount() {
    return documentCount;
  }

  public void setDocumentCount(Long documentCount) {
    this.documentCount = documentCount;
  }

  public VerifiedTranscriptionDocument transcriptionHash(
      String transcriptionHash) {
    this.transcriptionHash = transcriptionHash;
    return this;
  }

  /**
   * Get transcriptionHash
   *
   * @return transcriptionHash
   **/
  @ApiModelProperty(value = "")
  public String getTranscriptionHash() {
    return transcriptionHash;
  }

  public void setTranscriptionHash(String transcriptionHash) {
    this.transcriptionHash = transcriptionHash;
  }

  public VerifiedTranscriptionDocument textStringForTagging(
      String textStringForTagging) {
    this.textStringForTagging = textStringForTagging;
    return this;
  }

  /**
   * Get textStringForTagging
   *
   * @return textStringForTagging
   **/
  @ApiModelProperty(value = "")
  public String getTextStringForTagging() {
    return textStringForTagging;
  }

  /**
   * @return the first n characters of the textStringForTagging for sorting
   */
  @JsonIgnore
  public String getSortableTextString() {

    return StringUtils.left((this.textStringForTagging), N_CHARACTERS);
  }


  public void setTextStringForTagging(String textStringForTagging) {
    this.textStringForTagging = textStringForTagging;
  }

  public VerifiedTranscriptionDocument normalizedForm(String normalizedForm) {
    this.normalizedForm = normalizedForm;
    return this;
  }

  /**
   * Get normalizedForm
   *
   * @return normalizedForm
   **/
  @ApiModelProperty(value = "")
  public String getNormalizedForm() {
    return normalizedForm;
  }

  public void setNormalizedForm(String normalizedForm) {
    this.normalizedForm = normalizedForm;
  }

  public VerifiedTranscriptionDocument normalizedFormGroup(
      Integer normalizedFormGroup) {
    this.normalizedFormGroup = normalizedFormGroup;
    return this;
  }

  /**
   * Get normalizedFormGroup
   *
   * @return normalizedFormGroup
   **/
  @ApiModelProperty(value = "")
  public Integer getNormalizedFormGroup() {
    return normalizedFormGroup;
  }

  public void setNormalizedFormGroup(Integer normalizedFormGroup) {
    this.normalizedFormGroup = normalizedFormGroup;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifiedTranscriptionDocument verifiedTranscriptionDocument =
        (VerifiedTranscriptionDocument) o;
    return Objects.equals(this.intent, verifiedTranscriptionDocument.intent)
        && Objects.equals(this.suggestedIntent,
        verifiedTranscriptionDocument.suggestedIntent) && Objects
        .equals(this.intentConflict,
            verifiedTranscriptionDocument.intentConflict) && Objects
        .equals(this.intents, verifiedTranscriptionDocument.intents)
        && Objects.equals(this.documentCount,
        verifiedTranscriptionDocument.documentCount) && Objects
        .equals(this.transcriptionHash,
            verifiedTranscriptionDocument.transcriptionHash)
        && Objects.equals(this.textStringForTagging,
        verifiedTranscriptionDocument.textStringForTagging) && Objects
        .equals(this.normalizedForm,
            verifiedTranscriptionDocument.normalizedForm) && Objects
        .equals(this.normalizedFormGroup,
            verifiedTranscriptionDocument.normalizedFormGroup);
  }

  public VerifiedTranscriptionDocument clone() throws CloneNotSupportedException {
    return (VerifiedTranscriptionDocument) super.clone();
  }

  @Override
  public int hashCode() {
    return Objects.hash(intent, suggestedIntent, intentConflict, intents,
        documentCount, transcriptionHash, textStringForTagging,
        normalizedForm, normalizedFormGroup);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifiedTranscriptionDocument {\n");

    sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
    sb.append("    suggestedIntent: ")
        .append(toIndentedString(suggestedIntent)).append("\n");
    sb.append("    intentConflict: ")
        .append(toIndentedString(intentConflict)).append("\n");
    sb.append("    intents: ").append(toIndentedString(intents))
        .append("\n");
    sb.append("    documentCount: ").append(toIndentedString(documentCount))
        .append("\n");
    sb.append("    transcriptionHash: ")
        .append(toIndentedString(transcriptionHash)).append("\n");
    sb.append("    textStringForTagging: ")
        .append(toIndentedString(textStringForTagging)).append("\n");
    sb.append("    normalizedForm: ")
        .append(toIndentedString(normalizedForm)).append("\n");
    sb.append("    normalizedFormGroup: ")
        .append(toIndentedString(normalizedFormGroup)).append("\n");
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

