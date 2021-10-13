package com.tfs.learningsystems.ui.model;

import java.util.Objects;
import javax.validation.constraints.NotNull;


/**
 * ModelConfig
 */

public class ModelDeploymentMap {


  @NotNull
  private Integer id;

  @NotNull
  private String gitHubTag;

  @NotNull
  private Integer projectId;

  @NotNull
  private String modelId;


  public ModelDeploymentMap id(Integer id) {
    this.id = id;
    return this;
  }


  public ModelDeploymentMap gitHubTag(String gitHubTag) {
    this.gitHubTag = gitHubTag;
    return this;
  }


  public ModelDeploymentMap projectId(Integer projectId) {
    this.projectId = projectId;
    return this;
  }


  public ModelDeploymentMap modelId(String modelId) {
    this.modelId = modelId;
    return this;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getGitHubTag() {
    return gitHubTag;
  }

  public void setGitHubTag(String gitHubTag) {
    this.gitHubTag = gitHubTag;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public void setProjectId(Integer projectId) {
    this.projectId = projectId;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelDeploymentMap modelConfig = (ModelDeploymentMap) o;
    return Objects.equals(this.id, modelConfig.id) && Objects
        .equals(this.gitHubTag, modelConfig.gitHubTag) && Objects
        .equals(this.projectId, modelConfig.projectId) && Objects
        .equals(this.modelId, modelConfig.modelId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, gitHubTag, projectId, modelId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelConfig {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    gitHubTag: ").append(toIndentedString(gitHubTag))
        .append("\n");
    sb.append("    projectId: ").append(toIndentedString(projectId))
        .append("\n");
    sb.append("    modelId: ")
        .append(toIndentedString(modelId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

