package com.tfs.learningsystems.ui.nlmodel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize
public class TFSModelJobState {

  private String id;
  private String token; // Job id from Model Builder Service
  private String modelUUID; // UUID for model generated by Model Builder Service
  private String modelId; // ID from the database
  private Status status;
  private String statusMessage;
  private String modelType;
  private long startedAt;
  private long endedAt;

  public static enum Status {
    FAILED("FAILED"),
    ERROR("ERROR"),
    QUEUED("QUEUED"),
    RUNNING("RUNNING"),
    COMPLETED("COMPLETED");

    private String value;

    Status(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s", this.value);
    }

    public static Status lookup(String value) {
      for (Status modelStatus : Status.values()) {
        if (modelStatus.getValue().equalsIgnoreCase(value)) {
          return modelStatus;
        }
      }
      return null;
    }
  }
}
