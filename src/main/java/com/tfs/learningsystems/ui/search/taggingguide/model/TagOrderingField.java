/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

public enum TagOrderingField {

  TAG("tag"),
  INTENT("intent"),
  COUNT("count"),
  RUTAG("rutag"),
  COMMENTS("comments"),
  KEYWORDS("keywords"),
  EXAMPLES("examples"),
  FREQUENCY("frequency"),
  DESCRIPTION("description");

  private String value;

  TagOrderingField(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.getValue();
  }

  public static TagOrderingField lookup(String value) {
    for (TagOrderingField field : TagOrderingField.values()) {
      if (field.getValue().equalsIgnoreCase(value)) {
        return field;
      }
    }
    return null;
  }
}
