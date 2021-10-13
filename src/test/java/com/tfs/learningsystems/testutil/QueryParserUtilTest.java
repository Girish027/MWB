package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.tfs.learningsystems.ui.search.query.model.ManualTagClause;
import com.tfs.learningsystems.ui.search.query.model.QueryAggregator;
import com.tfs.learningsystems.ui.search.query.model.QueryClause;
import com.tfs.learningsystems.ui.search.query.model.TFSQueryOperator;
import com.tfs.learningsystems.util.QueryParserUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

public class QueryParserUtilTest {

  @Test
  public void testParseQueryString1() throws Exception {
    QueryAggregator expectedAggregator = new QueryAggregator();
    expectedAggregator.setTextStringForTaggingQuery("password -reset");
    expectedAggregator.setManualTagToOriginalDocumentsOperator(TFSQueryOperator.OR);
    //queryStrings.add("(manual_tag:reservation-* OR manual_tag:payment-change) AND auto_tag:make* reservation");
    //queryStrings.add("manual_tag:reservation-make reservation");
    //queryStrings.add("payment AND manual_tag:payment-change");
    //queryStrings.add("auto_tag:\"make reservation\" AND manual_tag:reservation-make reservation make");
    //queryStrings.add("auto_tag:\"make reservation\" OR auto_tag:\"change reservation\" reservation");
    //queryStrings.add("(suggested_category:\"make reservation\" OR suggested_category:\"change reservation\") AND (comment:\"reservation\" AND NOT comment:\"payment\") reservation -cancel");
    //queryStrings.add("suggested_category:\"make reservation\" OR suggested_category:\"change reservation\" reservation -cancel comment:\"reservation\" AND NOT comment:\"payment\"");
    //queryStrings.add("suggested_category:\"make reservation\" OR suggested_category:\"change reservation\" manual_tag:reservation-make AND NOT comment:\"reservation\" AND NOT comment:\"payment\"");
    //queryStrings.add("(name:\"Luke Skywalker\" AND father:unknown OR fname:Luke) or (name:yoda)");
    //queryStrings.add("password -reset OR manual_tag:password*");
    QueryAggregator receivedAggregator = QueryParserUtil
        .parseQueryString("password -reset OR granular_intent:password*");
    assertEquals(receivedAggregator.getManualTagToOriginalDocumentsOperator(), TFSQueryOperator.OR);
    assertEquals(receivedAggregator.getTextStringForTaggingQuery(), "password -reset");
    List<ManualTagClause> manualTagClauseList = receivedAggregator.getManualTagClauseList();
    assertEquals(manualTagClauseList.size(), 1);
    ManualTagClause clause = manualTagClauseList.get(0);
    assertEquals(clause.getText(), "password*");
    assertTrue(clause.getQueryType() instanceof PrefixQuery);
  }

  @Test
  public void testParseQuerySuggestedCategoryMultipleWords() throws Exception {
    QueryAggregator receivedAggregator = QueryParserUtil
        .parseQueryString("make reservation AND suggested_category:\"make reservation\"");
    assertNotNull(receivedAggregator);
    assertEquals(receivedAggregator.getTextStringForTaggingQuery(),
        "make reservation auto_tag:\'make reservation\'");
    assertEquals(receivedAggregator.getManualTagClauseList().size(), 0);
  }

  @Test
  public void testParseQueryStringWildcardQuery() throws Exception {
    QueryAggregator receivedAggregator = QueryParserUtil
        .parseQueryString("suggested_category:a* AND NOT granular_intent:*");
    assertNotNull(receivedAggregator);
    assertEquals(receivedAggregator.getTextStringForTaggingQuery(), "auto_tag:a*");
    assertEquals(receivedAggregator.getManualTagClauseList().size(), 1);
    ManualTagClause receivedTagClause = receivedAggregator.getManualTagClauseList().get(0);
    assertEquals(receivedTagClause.getText(), "*");
  }

  @Test
  public void testBuildQueryProhibitiveAnd() {
    List<ManualTagClause> clauses = new ArrayList<>();
    ManualTagClause clause1 = new ManualTagClause();
    clause1.setProhibited(true);
    clause1.setRequired(false);
    clause1.setText("payment-change");
    clauses.add(clause1);

    ManualTagClause clause2 = new ManualTagClause();
    clause2.setProhibited(false);
    clause2.setRequired(true);
    clause2.setText("reservation-make");
    clauses.add(clause2);

    assertEquals(QueryParserUtil.buildQuery(clauses), "-payment-change reservation-make");
  }

  @Test
  public void testBuildQueryAllOR() {
    List<ManualTagClause> clauses = new ArrayList<>();
    ManualTagClause clause1 = new ManualTagClause();
    clause1.setProhibited(false);
    clause1.setRequired(false);
    clause1.setText("payment-change");
    clauses.add(clause1);

    ManualTagClause clause2 = new ManualTagClause();
    clause2.setProhibited(false);
    clause2.setRequired(false);
    clause2.setText("reservation-make");
    clauses.add(clause2);

    assertEquals(QueryParserUtil.buildQuery(clauses), "payment-change OR reservation-make");
  }

  @Test
  public void testBuildQuerySingleOR() {
    List<ManualTagClause> clauses = new ArrayList<>();
    ManualTagClause clause1 = new ManualTagClause();
    clause1.setProhibited(false);
    clause1.setRequired(false);
    clause1.setText("payment-change");
    clauses.add(clause1);

    assertEquals(QueryParserUtil.buildQuery(clauses), "payment-change");
  }

  @Test
  public void testGetSuggestedCategoryQueryClause() {
    TermQuery query = new TermQuery(new Term("suggested_category", "make reservation"));
    try {
      QueryClause clause = QueryParserUtil.getQueryClause(query);
      assertNotNull(clause);
      assertTrue((clause.getQueryType() instanceof TermQuery));
      assertEquals(clause.getText(), "auto_tag:\'make reservation\'");
    } catch (ParseException e) {
      fail();
    }
  }

  @Test
  public void testIsTermsOnlyQuery() {
    List<ManualTagClause> list1 = new ArrayList<>();
    ManualTagClause c1 = new ManualTagClause();
    c1.setQueryType(new TermQuery(new Term("manual_tag", "res-make")));
    ManualTagClause c2 = new ManualTagClause();
    c2.setQueryType(new TermQuery(new Term("manual_tag", "pay-change")));
    list1.add(c1);
    list1.add(c2);
    assertTrue(QueryParserUtil.isTermsOnlyQuery(list1));

    List<ManualTagClause> list2 = new ArrayList<>();
    ManualTagClause c3 = new ManualTagClause();
    c3.setQueryType(new PrefixQuery(new Term("manual_tag", "res-make*")));
    ManualTagClause c4 = new ManualTagClause();
    c4.setQueryType(new TermQuery(new Term("manual_tag", "pay-change")));
    list2.add(c3);
    list2.add(c4);
    assertFalse(QueryParserUtil.isTermsOnlyQuery(list2));
  }

  @Test
  public void testPhrasedSearch() {
    try {
      QueryAggregator aggregator = QueryParserUtil
          .parseQueryString("\"where my credit card\" OR payment*");
      assertEquals(aggregator.getTextStringForTaggingQuery(),
          "\'where my credit card\' OR payment*");
    } catch (QueryNodeException e) {
      fail();
    }
  }

  @Test
  public void testTermRangeQuery() {
    try {
      QueryAggregator aggregator = QueryParserUtil
          .parseQueryString("reservation* AND numTokens:[4 TO 8]");
      assertEquals("reservation* numTokens[4 TO 8]", aggregator.getTextStringForTaggingQuery());
    } catch (QueryNodeException e) {
      fail();
    }
  }
}
