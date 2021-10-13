package com.tfs.learningsystems.ui.model.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"regex-removal"})
public class DataCleaning {

  @JsonProperty("regex-removal")
  private List<String> regexRemoval = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("regex-removal")
  public List<String> getRegexRemoval() {
    return regexRemoval;
  }

  @JsonProperty("regex-removal")
  public void setRegexRemoval(List<String> regexRemoval) {
    this.regexRemoval = regexRemoval;
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
    return new ToStringBuilder(this).append("regexRemoval", regexRemoval)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(additionalProperties)
        .append(regexRemoval).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof DataCleaning) == false) {
      return false;
    }
    DataCleaning rhs = ((DataCleaning) other);
    return new EqualsBuilder()
        .append(additionalProperties, rhs.additionalProperties)
        .append(regexRemoval, rhs.regexRemoval).isEquals();
  }

}
