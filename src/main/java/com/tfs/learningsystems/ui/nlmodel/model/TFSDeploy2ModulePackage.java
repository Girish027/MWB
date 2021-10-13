package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSDeploy2ModulePackage {



  private String name;
  private String module_id;
  private String date_created;
  private String user;
  private String comment;
  private String mark;
  private Deploy2PackageRepoParams repo_params;
  private Deploy2ModuleRepoLink link;


}
