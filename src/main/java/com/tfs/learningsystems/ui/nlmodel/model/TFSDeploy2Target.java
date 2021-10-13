package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSDeploy2Target {


  private Integer id;
  private String name;
  private String destination_path;
  private String destination_rsyncd_module;
  private String copy_method;
  private String ssh_user;
  private String handler_type;
  private Integer handler_port;
  private String create_full_path;
  private String ssh_key_id;
  private Deploy2ModuleRepoLink link;


}
