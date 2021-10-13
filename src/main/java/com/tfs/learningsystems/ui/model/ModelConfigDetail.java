package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;


/**
 * ModelConfigDetail
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-10-11T15:06:25.584-04:00")
public class ModelConfigDetail extends ModelConfig {

  private String id = null;

  private String configFile = null;

  private String legacyConfigFile = null;

  private String stopwordsFile = null;

  private String wordClassesFile = null;

  private String stemmingExceptionsFile = null;

  private String wordExpansionsFile = null;

  private String locationClassesFile = null;

  private Long createdAt = null;

  private Long modifiedAt = null;

  public ModelConfigDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The ID of this config
   *
   * @return id
   **/
  @ApiModelProperty(value = "The ID of this config")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ModelConfigDetail configFile(String configFile) {
    this.configFile = configFile;
    return this;
  }

  /**
   * The config file as a string
   *
   * @return configFile
   **/
  @ApiModelProperty(value = "The config file as a string")
  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }

  public ModelConfigDetail legacyConfigFile(String legacyConfigFile) {
    this.legacyConfigFile = legacyConfigFile;
    return this;
  }

  /**
   * The config file as a string
   *
   * @return legacyConfigFile
   **/
  @ApiModelProperty(value = "The config file as a string")
  public String getLegacyConfigFile() {
    return legacyConfigFile;
  }

  public void setLegacyConfigFile(String legacyConfigFile) {
    this.legacyConfigFile = legacyConfigFile;
  }

  public ModelConfigDetail stopwordsFile(String stopwordsFile) {
    this.stopwordsFile = stopwordsFile;
    return this;
  }

  /**
   * The stopwords file as a string
   *
   * @return stopwordsFile
   **/
  @ApiModelProperty(value = "The stopwords file as a string")
  public String getStopwordsFile() {
    return stopwordsFile;
  }

  public void setStopwordsFile(String stopwordsFile) {
    this.stopwordsFile = stopwordsFile;
  }

  public ModelConfigDetail wordClassesFile(String wordClassesFile) {
    this.wordClassesFile = wordClassesFile;
    return this;
  }

  /**
   * The word classes file as a string
   *
   * @return wordClassesFile
   **/
  @ApiModelProperty(value = "The word classes file as a string")
  public String getWordClassesFile() {
    return wordClassesFile;
  }

  public void setWordClassesFile(String wordClassesFile) {
    this.wordClassesFile = wordClassesFile;
  }

  public ModelConfigDetail stemmingExceptionsFile(String stemmingExceptionsFile) {
    this.stemmingExceptionsFile = stemmingExceptionsFile;
    return this;
  }

  /**
   * The stemming exceptions file as a string
   *
   * @return stemmingExceptionsFile
   **/
  @ApiModelProperty(value = "The stemming exceptions file as a string")
  public String getStemmingExceptionsFile() {
    return stemmingExceptionsFile;
  }

  public void setStemmingExceptionsFile(String stemmingExceptionsFile) {
    this.stemmingExceptionsFile = stemmingExceptionsFile;
  }

  public ModelConfigDetail wordExpansionsFile(String wordExpansionsFile) {
    this.wordExpansionsFile = wordExpansionsFile;
    return this;
  }

  /**
   * The word expansions file as a string
   *
   * @return wordExpansionsFile
   **/
  @ApiModelProperty(value = "The word expansions file as a string")
  public String getWordExpansionsFile() {
    return wordExpansionsFile;
  }

  public void setWordExpansionsFile(String wordExpansionsFile) {
    this.wordExpansionsFile = wordExpansionsFile;
  }

  public ModelConfigDetail locationClassesFile(String locationClassesFile) {
    this.locationClassesFile = locationClassesFile;
    return this;
  }

  /**
   * The location classes file as a string
   *
   * @return locationClassesFile
   **/
  @ApiModelProperty(value = "The location classes file as a string")
  public String getLocationClassesFile() {
    return locationClassesFile;
  }

  public void setLocationClassesFile(String locationClassesFile) {
    this.locationClassesFile = locationClassesFile;
  }

  public ModelConfigDetail createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The milliseconds since epoch representing the time the config was uploaded
   *
   * @return createdAt
   **/
  @ApiModelProperty(value = "The milliseconds since epoch representing the time the config was uploaded")
  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public ModelConfigDetail modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * The milliseconds since epoch representing the time the config metadata was modified
   *
   * @return modifiedAt
   **/
  @ApiModelProperty(value = "The milliseconds since epoch representing the time the config metadata was modified")
  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelConfigDetail modelConfigDetail = (ModelConfigDetail) o;
    return Objects.equals(this.id, modelConfigDetail.id) &&
        Objects.equals(this.configFile, modelConfigDetail.configFile) &&
        Objects.equals(this.stopwordsFile, modelConfigDetail.stopwordsFile) &&
        Objects.equals(this.wordClassesFile, modelConfigDetail.wordClassesFile) &&
        Objects.equals(this.stemmingExceptionsFile, modelConfigDetail.stemmingExceptionsFile) &&
        Objects.equals(this.wordExpansionsFile, modelConfigDetail.wordExpansionsFile) &&
        Objects.equals(this.locationClassesFile, modelConfigDetail.locationClassesFile) &&
        Objects.equals(this.createdAt, modelConfigDetail.createdAt) &&
        Objects.equals(this.modifiedAt, modelConfigDetail.modifiedAt) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, configFile, stopwordsFile, wordClassesFile, stemmingExceptionsFile,
        wordExpansionsFile, locationClassesFile, createdAt, modifiedAt, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelConfigDetail {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    configFile: ").append(toIndentedString(configFile)).append("\n");
    sb.append("    stopwordsFile: ").append(toIndentedString(stopwordsFile)).append("\n");
    sb.append("    wordClassesFile: ").append(toIndentedString(wordClassesFile)).append("\n");
    sb.append("    stemmingExceptionsFile: ").append(toIndentedString(stemmingExceptionsFile))
        .append("\n");
    sb.append("    wordExpansionsFile: ").append(toIndentedString(wordExpansionsFile)).append("\n");
    sb.append("    locationClassesFile: ").append(toIndentedString(locationClassesFile))
        .append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
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

