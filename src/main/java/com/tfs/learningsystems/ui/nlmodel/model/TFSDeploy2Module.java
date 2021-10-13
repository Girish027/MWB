package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSDeploy2Module {


  private Integer id;
  private String name;
  private String hidden;
  private String type;
  private String contacts;
  private String description;
  private Deploy2ModuleRepoParams repo_params;
  private Deploy2ModuleRepoFlags flags;
  private Deploy2ModuleRepoLink link;


}
