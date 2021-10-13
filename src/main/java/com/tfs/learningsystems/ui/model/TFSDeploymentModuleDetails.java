package com.tfs.learningsystems.ui.model;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonSerialize
public class TFSDeploymentModuleDetails {


  @ApiModelProperty( value = "Name of the module installed", example = "353")
  private String moduleName;

  private List<TFSModelTagDetails> modelTags;
}
