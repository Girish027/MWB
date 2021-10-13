/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Client
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-21T13:18:07.066-04:00")
public class Client {



  private String cid = null;

  private String name = null;


  @NotEmpty
  private String itsClientId = null;

  @NotEmpty
  private String itsAppId = null;

  @NotEmpty
  private String itsAccountId = null;



  private String address = null;
  private String description = null;
  private Boolean isVertical = false;

  private String deploymentModule = null;

  @NotNull
  private String state = StateEnum.ENABLED.value;

  public Client itsAccountId(String itsAccountId) {
    this.setItsAccountId(itsAccountId);
    return this;
  }

  @ApiModelProperty(required = true, value = "its account id", example = "default")
  public String getItsAccountId() {
    return itsAccountId;
  }

  public void setItsAccountId(String itsAccountId) {
    this.itsAccountId = itsAccountId;
  }

  public Client name(String name) {
    this.setName(name);
    return this;
  }

  /**
   * Client name
   *
   * @return name
   **/
  @ApiModelProperty(required = true, value = "Client name", example = "247.ai")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }



  /**
   * Client address
   *
   * @return address
   **/
  @ApiModelProperty(value = "Client address", example = "121 fake st., CA 91231")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Client description(String description) {
    this.setDescription(description);
    return this;
  }

  /**
   * Short description for this client
   *
   * @return description
   **/
  @ApiModelProperty(value = "Short description for this client", example = "Supports Active Cards")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Client isVertical(Boolean isVertical) {
    this.setIsVertical(isVertical);
    return this;
  }

  /**
   * Is this client a vertical template?
   *
   * @return isVertical
   **/
  @ApiModelProperty(value = "Is this client a vertical archtype?", example = "false")
  public Boolean getIsVertical() {
    return isVertical;
  }

  public void setIsVertical(Boolean isVertical) {
    this.isVertical = isVertical;
  }

  public Client state(String state) {
    this.setState(state);
    return this;
  }


  @ApiModelProperty(value = "DB cid of this  this client", hidden = true)
  public String getCid() {
    return cid;
  }

  public void setCid(String cid) {
    this.cid = cid;
  }


  public Client cid(String cid) {
    this.setCid(cid);
    return this;
  }

  @ApiModelProperty(value = "ITS clientId of this  this client", example = "247.ai")
  public String getItsClientId() {
    return itsClientId;
  }

  public void setItsClientId(String itsClientId) {
    this.itsClientId = itsClientId;
  }


  public Client itsClientId(String itsClientId) {
    this.setItsClientId(itsClientId);
    return this;
  }

  @ApiModelProperty(value = "ITS appId of this  this client", example = "referencebot")
  public String getItsAppId() {
    return itsAppId;
  }

  public void setItsAppId(String itsAppId) {
    this.itsAppId = itsAppId;
  }

  public Client itsAppId(String itsAppId) {
    this.setItsAppId(itsAppId);
    return this;
  }



  public String getDeploymentModule() {
    return deploymentModule;
  }

  public void setDeploymentModule(String deploymentModule) {
    this.deploymentModule = deploymentModule;
  }

  public Client deploymentModule(String deploymentModule) {
    this.setDeploymentModule(deploymentModule);
    return this;
  }
  /**
   * Get state
   *
   * @return state
   **/
  @ApiModelProperty(required = true, value = "current state of the client", example = "ENABLED")
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Client client = (Client) o;
    return Objects.equals(this.name, client.name)
        && Objects.equals(this.address, client.address)
        && Objects.equals(this.description, client.description)
        && Objects.equals(this.isVertical, client.isVertical)
        && Objects.equals(this.cid, client.cid)
        && Objects.equals(this.itsAppId, client.itsAppId)
        && Objects.equals(this.itsClientId, client.itsClientId)
        && Objects.equals(this.state, client.state)
        && Objects.equals(this.deploymentModule, client.deploymentModule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, address, description, state, cid, itsAppId, itsClientId, deploymentModule);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Client {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    cid: ").append(toIndentedString(cid)).append("\n");
    sb.append("    itsAppId: ").append(toIndentedString(itsAppId)).append("\n");
    sb.append("    itsClientId: ").append(toIndentedString(itsClientId)).append("\n");
    sb.append("    deploymentModule: ").append(toIndentedString(deploymentModule)).append("\n");
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

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }

  public enum StateEnum {
    ENABLED("ENABLED"),

    DISABLED("DISABLED");

    private String value;

    StateEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}