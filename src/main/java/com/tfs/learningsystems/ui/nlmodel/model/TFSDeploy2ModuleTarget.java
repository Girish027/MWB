package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSDeploy2ModuleTarget {


  private Integer target_id;
  private String module_id;
  private Deploy2ModuleRepoLink link;


}
