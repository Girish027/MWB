package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * repository link retrn as part of Deploy2 APIs
 */
@Data
@JsonSerialize
public class Deploy2ModuleRepoLink {

  private String rel;
  private String href;

}
