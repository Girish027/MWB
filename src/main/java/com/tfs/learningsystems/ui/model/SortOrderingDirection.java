/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum SortOrderingDirection {
  a("a", "asc"),
  A("A", "asc"),
  asc("asc", "asc"),
  ASC("ASC", "asc"),
  d("d", "desc"),
  D("D", "desc"),
  desc("desc", "desc"),
  DESC("DESC", "desc");

  private String key;
  private String value;

  SortOrderingDirection(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s-->%s", this.key, this.value);
  }

  public static SortOrderingDirection lookup(String key) {
    for (SortOrderingDirection direction : SortOrderingDirection.values()) {
      if (direction.getKey().equalsIgnoreCase(key)) {
        return direction;
      }
    }
    return null;
  }
}