/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model;

public enum TranscriptionOrderingField {
  COUNT("count"),
  CREATED_BY("createdBy"),
  CREATED_AT("createdAt"),
  DELETED_BY("deletedBy"),
  DELETED_AT("deletedAt"),
  MANUAL_TAG("manualTag"),
  SUGGESTED_TAG("suggestedTag"),
  UNIQUE_TEXT_STRING("uniqueTextString"),
  COMMENT("comment"),
  NORMALIZED_FORM("normalizedForm"),
  NORMALIZED_FORM_GROUP("normalizedFormGroup"),
  INTENT_CONFLICT("intentConflict"),
  SUGGESTED_INTENT("suggestedIntent");

  private String value;

  TranscriptionOrderingField(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.getValue();
  }

  public static TranscriptionOrderingField lookup(String value) {
    for (TranscriptionOrderingField field : TranscriptionOrderingField.values()) {
      if (field.getValue().equalsIgnoreCase(value)) {
        return field;
      }
    }
    return null;
  }
}
