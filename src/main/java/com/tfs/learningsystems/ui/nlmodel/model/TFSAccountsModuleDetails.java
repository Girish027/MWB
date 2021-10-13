package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonSerialize
@JsonIgnoreProperties
public class TFSAccountsModuleDetails {

    @ApiModelProperty( value = "Standard accountId. To be used for navigation between tools", example = "tfsai")
    private String accountId;

    @ApiModelProperty( value = "Account display name to be shown in client picker and nav bar", example = "247 ai")
    private String accountDisplayName;

    @ApiModelProperty( value = "Package code", example = "full")
    private String packageCode;

    private List<TFSProductsModuleDetails> products;

}
