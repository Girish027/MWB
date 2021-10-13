/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.taggingguide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonSerialize
@EqualsAndHashCode(callSuper = true)
public class TaggingGuideDocumentDetail extends TaggingGuideDocument {

  private long count;
  private float percentage;

  @JsonIgnore
  public String getSortableIntent() {
    String intent = this.getIntent();
    if (intent == null || intent.isEmpty()) {
      return intent;
    } else {
      return intent.replace('-', ' ').replace('_', ' ');
    }
  }
}
