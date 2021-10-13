package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonSerialize
@JsonIgnoreProperties
public class TFSComponentsModuleDetails {

    @ApiModelProperty( value = "ITS component id", example = "dialogmanager")
    private String componentId;

    @ApiModelProperty( value = "ITS client id", example = "247ai")
    private String componentClientId;

    @ApiModelProperty( value = "ITS account id", example = "247ai")
    private String componentAccountId;
}
