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
@JsonPropertyOrder({"version", "name", "description", "useLegacyConfigFile",
    "legacyConfigFileName", "dataCleaning",
    "supportingFiles", "transformations",
    "postProcessingRules", "trainingConfigs"})
public class NLModelConfig {

  @JsonProperty("version")
  private String version;
  @JsonProperty("name")
  private String name;
  @JsonProperty("description")
  private String description;
  @JsonProperty("useLegacyConfigFile")
  private Boolean useLegacyConfigFile = false;
  @JsonProperty("legacyConfigFileName")
  private String legacyConfigFileName;
  @JsonProperty("dataCleaning")
  private DataCleaning dataCleaning;
  @JsonProperty("supportingFiles")
  private SupportingFiles supportingFiles;
  @JsonProperty("transformations")
  private Transformations transformations;
  @JsonProperty("postProcessingRules")
  private List<PostProcessingRule> postProcessingRules = null;
  @JsonProperty("trainingConfigs")
  private TrainingConfigs trainingConfigs;
  @JsonIgnore
  private Map<String, Object> additionalProperties =
      new HashMap<String, Object>();

  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("useLegacyConfigFile")
  public Boolean getUseLegacyConfigFile() {
    return useLegacyConfigFile;
  }

  @JsonProperty("useLegacyConfigFile")
  public void setUseLegacyConfigFile(Boolean useLegacyConfigFile) {
    this.useLegacyConfigFile = useLegacyConfigFile;
  }

  @JsonProperty("legacyConfigFileName")
  public String getLegacyConfigFileName() {
    return legacyConfigFileName;
  }

  @JsonProperty("legacyConfigFileName")
  public void setLegacyConfigFileName(String legacyConfigFileName) {
    this.legacyConfigFileName = legacyConfigFileName;
  }

  @JsonProperty("dataCleaning")
  public DataCleaning getDataCleaning() {
    return dataCleaning;
  }

  @JsonProperty("dataCleaning")
  public void setDataCleaning(DataCleaning dataCleaning) {
    this.dataCleaning = dataCleaning;
  }

  @JsonProperty("supportingFiles")
  public SupportingFiles getSupportingFiles() {
    return supportingFiles;
  }

  @JsonProperty("supportingFiles")
  public void setSupportingFiles(SupportingFiles supportingFiles) {
    this.supportingFiles = supportingFiles;
  }

  @JsonProperty("transformations")
  public Transformations getTransformations() {
    return transformations;
  }

  @JsonProperty("transformations")
  public void setTransformations(Transformations transformations) {
    this.transformations = transformations;
  }

  @JsonProperty("postProcessingRules")
  public List<PostProcessingRule> getPostProcessingRules() {
    return postProcessingRules;
  }

  @JsonProperty("postProcessingRules")
  public void setPostProcessingRules(
      List<PostProcessingRule> postProcessingRules) {
    this.postProcessingRules = postProcessingRules;
  }

  @JsonProperty("trainingConfigs")
  public TrainingConfigs getTrainingConfigs() {
    return trainingConfigs;
  }

  @JsonProperty("trainingConfigs")
  public void setTrainingConfigs(TrainingConfigs trainingConfigs) {
    this.trainingConfigs = trainingConfigs;
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
    return new ToStringBuilder(this).append("version", version)
        .append("name", name).append("description", description)
        .append("useLegacyConfigFile", useLegacyConfigFile)
        .append("legacyConfigFileName", legacyConfigFileName)
        .append("dataCleaning", dataCleaning)
        .append("supportingFiles", supportingFiles)
        .append("transformations", transformations)
        .append("postProcessingRules", postProcessingRules)
        .append("trainingConfigs", trainingConfigs)
        .append("additionalProperties", additionalProperties)
        .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(postProcessingRules)
        .append(useLegacyConfigFile).append(additionalProperties)
        .append(trainingConfigs).append(description).append(name)
        .append(supportingFiles).append(transformations)
        .append(dataCleaning).append(legacyConfigFileName)
        .append(version).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof NLModelConfig) == false) {
      return false;
    }
    NLModelConfig rhs = ((NLModelConfig) other);
    return new EqualsBuilder()
        .append(postProcessingRules, rhs.postProcessingRules)
        .append(useLegacyConfigFile, rhs.useLegacyConfigFile)
        .append(additionalProperties, rhs.additionalProperties)
        .append(trainingConfigs, rhs.trainingConfigs)
        .append(description, rhs.description).append(name, rhs.name)
        .append(supportingFiles, rhs.supportingFiles)
        .append(transformations, rhs.transformations)
        .append(dataCleaning, rhs.dataCleaning)
        .append(legacyConfigFileName, rhs.legacyConfigFileName)
        .append(version, rhs.version).isEquals();
  }

}
