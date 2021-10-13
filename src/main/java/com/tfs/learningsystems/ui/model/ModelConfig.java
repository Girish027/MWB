package com.tfs.learningsystems.ui.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import java.util.Objects;


/**
 * ModelConfig
 */
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-10-11T15:06:25.584-04:00")
public class ModelConfig {

  private String name = null;

  private String description = null;

  private String projectId = null;

  private File configJsonFile = null;

  private String user = null;

  public ModelConfig name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the config
   *
   * @return name
   **/
  @ApiModelProperty(required = true, value = "The name of the config")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ModelConfig description(String description) {
    this.description = description;
    return this;
  }

  /**
   * The discription of the config
   *
   * @return description
   **/
  @ApiModelProperty(required = true, value = "The discription of the config")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public ModelConfig projectId(String projectId) {
    this.projectId = projectId;
    return this;
  }

  /**
   * The projectId of the config
   *
   * @return projectId
   **/
  @ApiModelProperty(required = true, value = "The projectId of the config")
  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public ModelConfig configFile(File configJsonFile) {
    this.configJsonFile = configJsonFile;
    return this;
  }

  /**
   * The archived config and support files
   *
   * @return configJsonFile
   **/
  @ApiModelProperty(value = "The json config file")
  public File getConfigJsonFile() {
    return configJsonFile;
  }

  public void setConfigJsonFile(File configJsonFile) {
    this.configJsonFile = configJsonFile;
  }

  public ModelConfig user(String user) {
    this.user = user;
    return this;
  }

  /**
   * The username who saved the config
   *
   * @return user
   **/
  @ApiModelProperty(required = true,
      value = "The username who saved the config")
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelConfig modelConfig = (ModelConfig) o;
    return Objects.equals(this.name, modelConfig.name) && Objects
        .equals(this.description, modelConfig.description) && Objects
        .equals(this.projectId, modelConfig.projectId) && Objects
        .equals(this.configJsonFile, modelConfig.configJsonFile)
        && Objects.equals(this.user, modelConfig.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, projectId, configJsonFile, user);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelConfig {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description))
        .append("\n");
    sb.append("    projectId: ").append(toIndentedString(projectId))
        .append("\n");
    sb.append("    configJsonFile: ")
        .append(toIndentedString(configJsonFile)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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

