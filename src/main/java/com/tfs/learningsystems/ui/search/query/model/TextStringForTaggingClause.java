/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.query.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonSerialize
@EqualsAndHashCode(callSuper = true)
public class TextStringForTaggingClause extends QueryClause {

  public static final String SEARCH_FIELD = "textStringForTagging";

  @Override
  public String toString() {
    return String.format("%s::%s - %s, %s", SEARCH_FIELD, this.getText(),
        this.isRequired(), this.isProhibited());
  }
}
