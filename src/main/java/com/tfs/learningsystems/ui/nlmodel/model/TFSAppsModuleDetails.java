package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonSerialize
@JsonIgnoreProperties
public class TFSAppsModuleDetails {

    @ApiModelProperty( value = "ITS app id", example = "dialogmanager")
    private String appId;

    @ApiModelProperty( value = "App display name to be shown in client picker and nav bar", example = "aisha")
    private String appDisplayName;

    @ApiModelProperty( value = "ITS account id", example = "247ai")
    private String accountId;

}
