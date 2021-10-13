/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.search.query.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Data;
import org.apache.lucene.search.Query;

@Data
@JsonSerialize
public class QueryAggregator {

  private String commentQuery;
  private List<ManualTagClause> manualTagClauseList;
  private List<RuTagClause> ruTagClauseList;
  private String textStringForTaggingQuery;
  private Query query;
  private TFSQueryOperator manualTagToOriginalDocumentsOperator;
  private TFSQueryOperator ruTagTagToOriginalDocumentsOperator;
}
