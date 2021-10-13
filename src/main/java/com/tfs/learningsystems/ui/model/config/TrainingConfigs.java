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
@JsonPropertyOrder({"numOfEpochs", "validationSplit"})
public class TrainingConfigs {

  @JsonProperty("numOfEpochs")
  private Integer numOfEpochs;
  @JsonProperty("validationSplit")
  private Double validationSplit;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("numOfEpochs")
  public Integer getNumOfEpochs() {
    return numOfEpochs;
  }

  @JsonProperty("numOfEpochs")
  public void setNumOfEpochs(Integer numOfEpochs) {
    this.numOfEpochs = numOfEpochs;
  }

  @JsonProperty("validationSplit")
  public Double getValidationSplit() {
    return validationSplit;
  }

  @JsonProperty("validationSplit")
  public void setValidationSplit(Double validationSplit) {
    this.validationSplit = validationSplit;
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
    return new ToStringBuilder(this).append("numOfEpochs", numOfEpochs)
        .append("validationSplit", validationSplit)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(additionalProperties)
        .append(numOfEpochs).append(validationSplit).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof TrainingConfigs) == false) {
      return false;
    }
    TrainingConfigs rhs = ((TrainingConfigs) other);
    return new EqualsBuilder()
        .append(additionalProperties, rhs.additionalProperties)
        .append(numOfEpochs, rhs.numOfEpochs)
        .append(validationSplit, rhs.validationSplit).isEquals();
  }

}
