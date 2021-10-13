package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSModelJobBase {

  private String id;
  private String token;
  private String modelId;
}
