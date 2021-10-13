/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.tfs.learningsystems.util.CommonUtils;


/**
 * TranscriptionDocumentDetailTaggingGuideDocument
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-12T19:11:17.709-08:00")
public class TranscriptionDocumentDetail {

  private static final int N_CHARACTERS = 64;

  private String intent = null;
  private Map <Integer, String> intents = new HashMap <> ();
  private String taggedAt = null;
  private String taggedBy = null;
  private String comment = null;
  private String commentedBy = null;
  private String commentedAt = null;
  private String autoTagString = null;
  private Integer numTokens = null;
  private Set<Integer> datasetIds = new HashSet<>();
  private String datasetSource = null;
  private Integer autoTagCount = null;
  private Long documentCount = null;
  private String uuid = null;
  private String textStringOriginal = null;
  @NotBlank
  private String transcriptionHash = null;
  @NotBlank
  private String textStringForTagging = null;
  private String rutag = null;
  private String classificationId = null;
  private String fileName = null;

  public TranscriptionDocumentDetail intent(String intent) {
    this.intent = CommonUtils.sanitize(intent);
    return this;
  }

  /**
   * Get intent
   *
   * @return intent
   **/
  @ApiModelProperty(value = "")
  public String getIntent() {
    return CommonUtils.sanitize(intent);
  }

  public void setIntent(String intent) {
    this.intent = CommonUtils.sanitize(intent);
  }


  /**
   * Get classificationId
   *
   * @return classificationId
   **/
  @ApiModelProperty(value = "")
  public String getClassificationId() {

    return classificationId;
  }

  public void setClassificationId(String classificationId) {

    this.classificationId = classificationId;
  }

  public TranscriptionDocumentDetail intents(Map <Integer, String> intents) {
    this.intents = intents;
    return this;
  }

  /**
   * Get intent
   *
   * @return intent
   **/
  @ApiModelProperty(value = "")
  public Map <Integer, String> getIntents() {
    return intents;
  }

  public void setIntents(Map <Integer, String> intents) {
    this.intents = intents;
  }

  public TranscriptionDocumentDetail taggedAt(String taggedAt) {
    this.taggedAt = taggedAt;
    return this;
  }

  @JsonIgnore
  public String getSortableIntent() {
    if (intent == null || intent.isEmpty()) {
      return intent;
    } else {
      return intent.replace('-', ' ').replace('_', ' ');
    }
  }

  /**
   * Get taggedAt
   *
   * @return taggedAt
   **/
  @ApiModelProperty(value = "")
  public String getTaggedAt() {
    return taggedAt;
  }

  public void setTaggedAt(String taggedAt) {
    this.taggedAt = taggedAt;
  }

  public TranscriptionDocumentDetail taggedBy(String taggedBy) {
    this.taggedBy = taggedBy;
    return this;
  }

  /**
   * Get taggedBy
   *
   * @return taggedBy
   **/
  @ApiModelProperty(value = "")
  public String getTaggedBy() {
    return taggedBy;
  }

  public void setTaggedBy(String taggedBy) {
    this.taggedBy = taggedBy;
  }

  public TranscriptionDocumentDetail autoTagString(String autoTagString) {
    this.autoTagString = autoTagString;
    return this;
  }

  /**
   * Get datasetSource
   *
   * @return datasetSource
   **/
  @ApiModelProperty(value = "")
  public String getDatasetSource() {
    return datasetSource;
  }

  public void setDatasetSource(String datasetSource) {
    this.datasetSource = datasetSource;
  }

  public TranscriptionDocumentDetail datasetSource(String datasetSource) {
    this.datasetSource = datasetSource;
    return this;
  }

  /**
   * Get autoTagString
   *
   * @return autoTagString
   **/
  @ApiModelProperty(value = "")
  public String getAutoTagString() {
    return autoTagString;
  }

  public void setAutoTagString(String autoTagString) {
    this.autoTagString = autoTagString;
  }

  public TranscriptionDocumentDetail numTokens(Integer numTokens) {
    this.numTokens = numTokens;
    return this;
  }

  /**
   * Get numTokens
   *
   * @return numTokens
   **/
  @ApiModelProperty(value = "")
  public Integer getNumTokens() {
    return numTokens;
  }

  public void setNumTokens(Integer numTokens) {
    this.numTokens = numTokens;
  }

  public TranscriptionDocumentDetail datasetIds(Set<Integer> datasetIds) {
    this.datasetIds = datasetIds;
    return this;
  }

  /**
   * Get datasetIds
   *
   * @return datasetIds
   **/
  @ApiModelProperty(value = "")
  public Set <Integer> getDatasetIds() {
    return datasetIds;
  }

  public void setDatasetIds(Set<Integer> datasetIds) {
    this.datasetIds = datasetIds;
  }

  public TranscriptionDocumentDetail autoTagCount(Integer autoTagCount) {
    this.autoTagCount = autoTagCount;
    return this;
  }

  /**
   * Get autoTagCount
   *
   * @return autoTagCount
   **/
  @ApiModelProperty(value = "")
  public Integer getAutoTagCount() {
    return autoTagCount;
  }

  public void setAutoTagCount(Integer autoTagCount) {
    this.autoTagCount = autoTagCount;
  }

  public TranscriptionDocumentDetail documentCount(Long documentCount) {
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

  public TranscriptionDocumentDetail transcriptionHash(String transcriptionHash) {
    this.transcriptionHash = transcriptionHash;
    return this;
  }

  /**
   * Get transcriptionHash
   *
   * @return transcriptionHash
   **/
  @ApiModelProperty(required = true, value = "")
  public String getTranscriptionHash() {
    return transcriptionHash;
  }

  public void setTranscriptionHash(String transcriptionHash) {
    this.transcriptionHash = transcriptionHash;
  }

  public TranscriptionDocumentDetail textStringForTagging(String textStringForTagging) {
    this.textStringForTagging = textStringForTagging;
    return this;
  }

  /**
   * Get textStringForTagging
   *
   * @return textStringForTagging
   **/
  @ApiModelProperty(required = true, value = "")
  public String getTextStringForTagging() {
    return textStringForTagging;
  }

  public void setTextStringForTagging(String textStringForTagging) {
    this.textStringForTagging = textStringForTagging;
  }

  /**
   * @return the first n characters of the textStringForTagging for sorting
   */
  @JsonIgnore
  public String getSortableTextString() {

    return StringUtils.left(StringUtils.deleteWhitespace(this.textStringForTagging), N_CHARACTERS);
  }

  public TranscriptionDocumentDetail textStringOriginal(String textStringOriginal) {
    this.textStringOriginal = textStringOriginal;
    return this;
  }

  /**
   * Get textStringOriginal
   *
   * @return textStringOriginal
   **/
  @ApiModelProperty(required = true, value = "")
  public String getTextStringOriginal() {
    return textStringOriginal;
  }

  public void setTextStringOriginal(String originalTextString) {
    this.textStringOriginal = originalTextString;
  }

  public TranscriptionDocumentDetail uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * Get uuid
   *
   * @return uuid
   **/
  @ApiModelProperty(value = "")
  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public TranscriptionDocumentDetail comment(String comment) {
    this.comment = comment;
    return this;

  }

  /**
   * @return the comment
   */
  public String getComment() {
    return comment;
  }


  /**
   * @param comment the comment to set
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * @return the first n characters of the comment for sorting
   */
  @JsonIgnore
  public String getSortableComment() {

    return StringUtils.left(StringUtils.deleteWhitespace(this.comment), N_CHARACTERS);
  }

  public TranscriptionDocumentDetail commentedAt(String commentedAt) {
    this.commentedAt = commentedAt;
    return this;

  }

  /**
   * @return the commentedAt
   */
  public String getCommentedAt() {
    return commentedAt;
  }

  /**
   * @param commentedAt the commentedAt to set
   */
  public void setCommentedAt(String commentedAt) {
    this.commentedAt = commentedAt;
  }

  public TranscriptionDocumentDetail commentedBy(String commentedBy) {
    this.commentedBy = commentedBy;
    return this;

  }

  /**
   * @return the commentedBy
   */
  public String getCommentedBy() {
    return commentedBy;
  }

  /**
   * @param commentedBy the commentedBy to set
   */
  public void setCommentedBy(String commentedBy) {
    this.commentedBy = commentedBy;
  }


  /**
   * Get rutag
   *
   * @return rutag
   **/
  @ApiModelProperty(value = "")
  public String getRutag() {
    return CommonUtils.sanitize(rutag);
  }

  public void setRutag(String rutag) {
    this.rutag = CommonUtils.sanitize(rutag);
  }

  public TranscriptionDocumentDetail ruTag(String rutag) {
    this.rutag = CommonUtils.sanitize(rutag);
    return this;
  }


  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }


  public TranscriptionDocumentDetail fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TranscriptionDocumentDetail transcriptionDocumentDetail = (TranscriptionDocumentDetail) o;
    return Objects.equals(this.intent, transcriptionDocumentDetail.intent) &&
            Objects.equals(this.intents, transcriptionDocumentDetail.intents) &&
            Objects.equals(this.taggedAt, transcriptionDocumentDetail.taggedAt) &&
            Objects.equals(this.taggedBy, transcriptionDocumentDetail.taggedBy) &&
            Objects.equals(this.autoTagString, transcriptionDocumentDetail.autoTagString) &&
            Objects.equals(this.numTokens, transcriptionDocumentDetail.numTokens) &&
            Objects.equals(this.datasetIds, transcriptionDocumentDetail.datasetIds) &&
            Objects.equals(this.autoTagCount, transcriptionDocumentDetail.autoTagCount) &&
            Objects.equals(this.documentCount, transcriptionDocumentDetail.documentCount) &&
            Objects.equals(this.transcriptionHash, transcriptionDocumentDetail.transcriptionHash) &&
            Objects.equals(this.textStringOriginal, transcriptionDocumentDetail.textStringOriginal) &&
            Objects.equals(this.uuid, transcriptionDocumentDetail.uuid) &&
            Objects.equals(this.comment, transcriptionDocumentDetail.comment) &&
            Objects.equals(this.commentedAt, transcriptionDocumentDetail.commentedAt) &&
            Objects.equals(this.commentedBy, transcriptionDocumentDetail.commentedBy) &&
            Objects.equals(this.textStringForTagging, transcriptionDocumentDetail.textStringForTagging) &&
            Objects.equals(this.rutag, transcriptionDocumentDetail.rutag) &&
            Objects.equals(this.classificationId, transcriptionDocumentDetail.classificationId) &&
            Objects.equals(this.fileName, transcriptionDocumentDetail.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(intent, intents, taggedAt, taggedBy, autoTagString, numTokens, datasetIds,
            autoTagCount, documentCount, transcriptionHash, textStringForTagging, textStringOriginal,
            uuid, comment, commentedAt, commentedBy, rutag, classificationId, fileName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TranscriptionDocumentDetail {\n");

    sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
    sb.append("    intents: ").append(toIndentedString(intents)).append("\n");
    sb.append("    taggedAt: ").append(toIndentedString(taggedAt)).append("\n");
    sb.append("    taggedBy: ").append(toIndentedString(taggedBy)).append("\n");
    sb.append("    autoTagString: ").append(toIndentedString(autoTagString)).append("\n");
    sb.append("    numTokens: ").append(toIndentedString(numTokens)).append("\n");
    sb.append("    datasetIds: ").append(toIndentedString(datasetIds)).append("\n");
    sb.append("    autoTagCount: ").append(toIndentedString(autoTagCount)).append("\n");
    sb.append("    documentCount: ").append(toIndentedString(documentCount)).append("\n");
    sb.append("    transcriptionHash: ").append(toIndentedString(transcriptionHash)).append("\n");
    sb.append("    textStringForTagging: ").append(toIndentedString(textStringForTagging)).append("\n");
    sb.append("    originalTextString: ").append(toIndentedString(textStringOriginal)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    commentedAt: ").append(toIndentedString(commentedAt)).append("\n");
    sb.append("    commentedBy: ").append(toIndentedString(commentedBy)).append("\n");
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    ruTag: ").append(toIndentedString(rutag)).append("\n");
    sb.append("    classificationId: ").append(toIndentedString(classificationId)).append("\n");
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
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