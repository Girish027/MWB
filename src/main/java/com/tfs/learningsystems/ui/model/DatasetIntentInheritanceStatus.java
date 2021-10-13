/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum DatasetIntentInheritanceStatus {
  PENDING("PENDING"),
  PROCESSING("PROCESSING"),
  COMPLETED("COMPLETED");

  private final String status;

  DatasetIntentInheritanceStatus(String status) {
    this.status = status;
  }

  public String status() {
    return this.status;
  }

  public static DatasetIntentInheritanceStatus lookup(String statusValue) {
    for (DatasetIntentInheritanceStatus status : DatasetIntentInheritanceStatus.values()) {
      if (status.status().equalsIgnoreCase(statusValue)) {
        return status;
      }
    }
    return null;
  }
}
