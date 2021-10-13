package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class Deploy2PackageRepoParams {

  private String github_ref;
  private String github_reftype;

}
