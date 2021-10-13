package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize
@JsonIgnoreProperties
public class TFSProductsModuleDetails {

    @ApiModelProperty( value = "Ums product id", example = "botframework")
    private String productId;

    @ApiModelProperty( value = "Role of the user for product and client", example = "developer")
    private List<String> roles;

    private List<TFSComponentsModuleDetails> components;
}
