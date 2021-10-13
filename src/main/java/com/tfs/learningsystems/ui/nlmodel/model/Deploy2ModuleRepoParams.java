package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class Deploy2ModuleRepoParams {

  private String github_org;
  private String github_server;
  private String github_repo;


}
