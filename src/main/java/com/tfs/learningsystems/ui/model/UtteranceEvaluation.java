package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * UtteranceEvaluation
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
public class UtteranceEvaluation {

  private String utterance = null;

  private String recognitionScore = null;
  private String utteranceWithWordClass = null;


  private List<IntentScore> intents = new ArrayList<IntentScore>();

  private Object transformations = null;
  private Object entities = null;

  private String utteranceFileData = null;

  public UtteranceEvaluation utterance(String utterance) {
    this.utterance = utterance;
    return this;
  }

  /**
   * The sentence in test
   *
   * @return utterance
   **/
  @ApiModelProperty(required = true, value = "The sentence in test")
  public String getUtterance() {
    return utterance;
  }

  public void setUtterance(String utterance) {
    this.utterance = utterance;
  }

  public UtteranceEvaluation intents(List<IntentScore> intents) {
    this.intents = intents;
    return this;
  }

  public UtteranceEvaluation addIntentsItem(IntentScore intentsItem) {
    this.intents.add(intentsItem);
    return this;
  }

  @ApiModelProperty(required = false, value = "The sentence confidence score")
  public String getRecognitionScore() {
    return this.recognitionScore;
  }
  public void setRecognitionScore(String recognitionScore){
    this.recognitionScore = recognitionScore;
  }

  @ApiModelProperty(required = false, value = "The sentence  with word class")
  public String getUtteranceWithWordClass() {
    return this.utteranceWithWordClass;
  }
  public void setUtteranceWithWordClass(String utteranceWithWordClass){
    this.utteranceWithWordClass = utteranceWithWordClass;
  }


  /**
   * Get intents
   *
   * @return intents
   **/
  @ApiModelProperty(required = true, value = "")
  public List<IntentScore> getIntents() {
    return intents;
  }

  public void setIntents(List<IntentScore> intents) {
    this.intents = intents;
  }

  public UtteranceEvaluation transformations(Object transformations) {
    this.transformations = transformations;
    return this;
  }

  /**
   * return message from the underneath model
   *
   * @return transformations
   **/
  @ApiModelProperty(required = true, value = "return message from the underneath model")
  public Object getTransformations() {
    return transformations;
  }

  public void setTransformations(Object transformations) {
    this.transformations = transformations;
  }

  public UtteranceEvaluation entities(Object entities) {
    this.entities = entities;
    return this;
  }

  /**
   * return message from the underneath model
   *
   * @return entities
   **/
  @ApiModelProperty(required = true, value = "")
  public Object getEntities() {
    return entities;
  }

  public void setEntities(Object entities) {
    this.entities = entities;
  }

  /**
   * return message from the underneath model
   *
   * @return utteranceFileData
   **/
  @ApiModelProperty(required = true, value = "")
  public Object getUtteranceFileData() {
    return utteranceFileData;
  }

  public void setUtteranceFileData(String utteranceFileData) {
    this.utteranceFileData = utteranceFileData;
  }



  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UtteranceEvaluation utteranceEvaluation = (UtteranceEvaluation) o;
    return Objects.equals(this.utterance, utteranceEvaluation.utterance) &&
        Objects.equals(this.recognitionScore, utteranceEvaluation.recognitionScore) &&
        Objects.equals(this.utteranceWithWordClass, utteranceEvaluation.utteranceWithWordClass) &&
        Objects.equals(this.intents, utteranceEvaluation.intents) &&
        Objects.equals(this.transformations, utteranceEvaluation.transformations) &&
        Objects.equals(this.entities, utteranceEvaluation.entities) &&
        Objects.equals(this.utteranceFileData, utteranceEvaluation.utteranceFileData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(utterance, intents, transformations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UtteranceEvaluation {\n");

    sb.append("    utterance: ").append(toIndentedString(utterance)).append("\n");
    sb.append("    utteranceConfidence: ").append(toIndentedString(recognitionScore)).append("\n");
    sb.append("    utterancewithWordClass: ").append(toIndentedString(utteranceWithWordClass)).append("\n");
    sb.append("    intents: ").append(toIndentedString(intents)).append("\n");
    sb.append("    transformations: ").append(toIndentedString(transformations)).append("\n");
    sb.append("    entities: ").append(toIndentedString(entities)).append("\n");
    sb.append("    utteranceFileData: ").append(toIndentedString(utteranceFileData)).append("\n");
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

