package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class Deploy2ModuleRepoFlags {

  private Integer preserve_time;
  private Integer compress;
  private Integer touch;
  private Integer deep_package;
  private Integer auto_ena;
}
