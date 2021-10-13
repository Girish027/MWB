/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum DocumentType {
  ORIGINAL("original"),
  INTENT_ADDED("intent-added"),
  INTENT_DELETED("intent-deleted"),
  COMMENT_ADDED("comment-added");

  private final String type;

  DocumentType(String type) {
    this.type = type;
  }

  public String type() {
    return this.type;
  }

  public static DocumentType lookup(String value) {
    for (DocumentType type : DocumentType.values()) {
      if (type.type().equalsIgnoreCase(value)) {
        return type;
      }
    }
    return null;
  }
}
