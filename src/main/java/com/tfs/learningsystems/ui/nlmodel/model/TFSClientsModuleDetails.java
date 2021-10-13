package com.tfs.learningsystems.ui.nlmodel.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize
@JsonIgnoreProperties
public class TFSClientsModuleDetails {


  @ApiModelProperty( value = "Standard clientId. To be used for navigation between tools", example = "tfsai")
  private String clientId;

  @ApiModelProperty( value = "Client display name to be shown in client picker and nav bar", example = "247 ai")
  private String clientDisplayName;

  private List<TFSAccountsModuleDetails> accounts;

  @ApiModelProperty( value = "ITS client id", example = "247ai")
  private String componentClientId;

  private List<TFSAppsModuleDetails> apps;

}
