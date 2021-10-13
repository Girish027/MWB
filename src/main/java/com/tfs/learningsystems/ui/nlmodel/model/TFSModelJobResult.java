package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSModelJobResult {

  private String message;
  private Throwable exception;
  private TFSModelJobState.Status status;
}
