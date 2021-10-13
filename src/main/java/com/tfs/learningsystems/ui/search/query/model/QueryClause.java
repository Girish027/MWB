/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.query.model;

import lombok.Data;
import org.apache.lucene.search.Query;

@Data
public abstract class QueryClause {

  private String text;
  private boolean required;
  private boolean prohibited;
  private Query queryType;
}
