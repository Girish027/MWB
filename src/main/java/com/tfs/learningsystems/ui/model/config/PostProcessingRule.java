package com.tfs.learningsystems.ui.model.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    {"regex", "intent-match", "replacementIntent", "minConfidenceScore",
        "maxConfidenceScore"})
public class PostProcessingRule {

  @JsonProperty("regex")
  private String regex;
  //    Default config.json isn't valid for intent-match, one is string one is array
  @JsonProperty("intent-match")
  @JsonIgnore
  private List<String> intentMatch = new ArrayList<>();
  @JsonProperty("replacementIntent")
  private String replacementIntent;
  @JsonProperty("minConfidenceScore")
  private Integer minConfidenceScore;
  @JsonProperty("maxConfidenceScore")
  private Integer maxConfidenceScore;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("regex")
  public String getRegex() {
    return regex;
  }

  @JsonProperty("regex")
  public void setRegex(String regex) {
    this.regex = regex;
  }

  //    Default config.json isn't valid for intent-match, one is string one is array
  @JsonProperty("intent-match")
  @JsonIgnore
  public List<String> getIntentMatch() {
    return intentMatch;
  }

  @JsonProperty("intent-match")
  @JsonIgnore
  public void setIntentMatch(List<String> intentMatch) {
    this.intentMatch = intentMatch;
  }


  @JsonProperty("replacementIntent")
  public String getReplacementIntent() {
    return replacementIntent;
  }

  @JsonProperty("replacementIntent")
  public void setReplacementIntent(String replacementIntent) {
    this.replacementIntent = replacementIntent;
  }

  @JsonProperty("minConfidenceScore")
  public Integer getMinConfidenceScore() {
    return minConfidenceScore;
  }

  @JsonProperty("minConfidenceScore")
  public void setMinConfidenceScore(Integer minConfidenceScore) {
    this.minConfidenceScore = minConfidenceScore;
  }

  @JsonProperty("maxConfidenceScore")
  public Integer getMaxConfidenceScore() {
    return maxConfidenceScore;
  }

  @JsonProperty("maxConfidenceScore")
  public void setMaxConfidenceScore(Integer maxConfidenceScore) {
    this.maxConfidenceScore = maxConfidenceScore;
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
    return new ToStringBuilder(this).append("regex", regex)
        .append("intentMatch", intentMatch)
        .append("replacementIntent", replacementIntent)
        .append("minConfidenceScore", minConfidenceScore)
        .append("maxConfidenceScore", maxConfidenceScore)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(minConfidenceScore)
        .append(additionalProperties).append(intentMatch).append(regex)
        .append(replacementIntent).append(maxConfidenceScore)
        .toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof PostProcessingRule) == false) {
      return false;
    }
    PostProcessingRule rhs = ((PostProcessingRule) other);
    return new EqualsBuilder()
        .append(minConfidenceScore, rhs.minConfidenceScore)
        .append(additionalProperties, rhs.additionalProperties)
        .append(intentMatch, rhs.intentMatch).append(regex, rhs.regex)
        .append(replacementIntent, rhs.replacementIntent)
        .append(maxConfidenceScore, rhs.maxConfidenceScore).isEquals();
  }

}
