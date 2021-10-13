package com.tfs.learningsystems.ui.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonSerialize
public class TFSModelTagDetails {


  @ApiModelProperty( value = "Name of the tag tagged / installed", example = "353")
  private String tagName;

  @ApiModelProperty( value = "Status of the Tag created", example = "CREATED")
  private String tagStatus;

  @ApiModelProperty( value = "Name of the package created", example = "clients--247.ai--applications--default--models--newTag")
  private String packageName;

  @ApiModelProperty( value = "List of Project Models tagged")
  private List<TFSProjectModel> projectModels;

}
