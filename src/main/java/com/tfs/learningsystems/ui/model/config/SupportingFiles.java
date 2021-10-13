package com.tfs.learningsystems.ui.model.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    {"stopwords", "stemmingExceptions", "contractions", "wordClasses"})
public class SupportingFiles {

  @JsonProperty("stopwords")
  private String stopwords;
  @JsonProperty("stemmingExceptions")
  private String stemmingExceptions;
  @JsonProperty("contractions")
  private String contractions;
  @JsonProperty("wordClasses")
  private String wordClasses;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("stopwords")
  public String getStopwords() {
    return stopwords;
  }

  @JsonProperty("stopwords")
  public void setStopwords(String stopwords) {
    this.stopwords = stopwords;
  }

  @JsonProperty("stemmingExceptions")
  public String getStemmingExceptions() {
    return stemmingExceptions;
  }

  @JsonProperty("stemmingExceptions")
  public void setStemmingExceptions(String stemmingExceptions) {
    this.stemmingExceptions = stemmingExceptions;
  }

  @JsonProperty("contractions")
  public String getContractions() {
    return contractions;
  }

  @JsonProperty("contractions")
  public void setContractions(String contractions) {
    this.contractions = contractions;
  }

  @JsonProperty("wordClasses")
  public String getWordClasses() {
    return wordClasses;
  }

  @JsonProperty("wordClasses")
  public void setWordClasses(String wordClasses) {
    this.wordClasses = wordClasses;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("stopwords", stopwords)
        .append("stemmingExceptions", stemmingExceptions)
        .append("contractions", contractions)
        .append("wordClasses", wordClasses)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(contractions).append(wordClasses)
        .append(stemmingExceptions).append(additionalProperties)
        .append(stopwords).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof SupportingFiles) == false) {
      return false;
    }
    SupportingFiles rhs = ((SupportingFiles) other);
    return new EqualsBuilder().append(contractions, rhs.contractions)
        .append(wordClasses, rhs.wordClasses)
        .append(stemmingExceptions, rhs.stemmingExceptions)
        .append(additionalProperties, rhs.additionalProperties)
        .append(stopwords, rhs.stopwords).isEquals();
  }

}
