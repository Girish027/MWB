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
@JsonPropertyOrder({"regex", "replaceText"})
public class RegexWordClassSubstitution {

  @JsonProperty("regex")
  private String regex;
  @JsonProperty("replaceText")
  private String replaceText;
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

  @JsonProperty("replaceText")
  public String getReplaceText() {
    return replaceText;
  }

  @JsonProperty("replaceText")
  public void setReplaceText(String replaceText) {
    this.replaceText = replaceText;
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
        .append("replaceText", replaceText)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(additionalProperties).append(regex)
        .append(replaceText).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof RegexWordClassSubstitution) == false) {
      return false;
    }
    RegexWordClassSubstitution rhs = ((RegexWordClassSubstitution) other);
    return new EqualsBuilder()
        .append(additionalProperties, rhs.additionalProperties)
        .append(regex, rhs.regex).append(replaceText, rhs.replaceText)
        .isEquals();
  }

}
