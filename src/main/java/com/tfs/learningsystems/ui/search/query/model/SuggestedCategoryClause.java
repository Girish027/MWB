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
public class SuggestedCategoryClause extends QueryClause {

  public static final String SEARCH_FIELD = "suggested_category";
  public static final String ELASTIC_SEARCH_FIELD = "auto_tag";

  @Override
  public String toString() {
    return String.format("%s::%s", SEARCH_FIELD, this.getText());
  }
}
