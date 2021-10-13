package com.tfs.learningsystems.util;

import com.tfs.learningsystems.ui.search.query.model.CommentClause;
import com.tfs.learningsystems.ui.search.query.model.ManualTagClause;
import com.tfs.learningsystems.ui.search.query.model.QueryAggregator;
import com.tfs.learningsystems.ui.search.query.model.QueryClause;
import com.tfs.learningsystems.ui.search.query.model.SuggestedCategoryClause;
import com.tfs.learningsystems.ui.search.query.model.TFSQueryOperator;
import com.tfs.learningsystems.ui.search.query.model.TextStringForTaggingClause;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

@Slf4j
public class QueryParserUtil {

  public static final Pattern fieldValuePattern = Pattern.compile("^[\\w]+:.*");

  static final Analyzer analyzer = new WhitespaceAnalyzer();
  static final PrecedenceQueryParser luceneParser = new PrecedenceQueryParser(analyzer);

  static {
    luceneParser.setAllowLeadingWildcard(true);
    luceneParser.setDefaultOperator(Operator.AND);
  }

  public static TFSQueryOperator validateManualTagClauseList(
      List<ManualTagClause> manualTagClauseList,
      String textStringQuery) throws ParseException {
    TFSQueryOperator operator = TFSQueryOperator.AND;

    if (manualTagClauseList != null) {
      int count = 1;
      boolean manualTagRequired = false;
      boolean manualTagProhibited = false;
      boolean manualTagMoreThanOneTrue = true;
      for (ManualTagClause clause : manualTagClauseList) {
        manualTagRequired = manualTagRequired || clause.isRequired();
        manualTagProhibited = manualTagProhibited || clause.isProhibited();
        manualTagMoreThanOneTrue = manualTagMoreThanOneTrue && clause.isRequired();
        if (manualTagMoreThanOneTrue && count > 1) {
          throw new ParseException(new MessageImpl(
              "Invalid query. A single transcription cannot have multiple intents at this time."));
        }
        count++;
      }

      if (manualTagRequired && !manualTagProhibited && textStringQuery != null
          && !"".equals(textStringQuery)) {
        operator = TFSQueryOperator.AND;
      } else if (!manualTagRequired && !manualTagProhibited) {
        operator = TFSQueryOperator.OR;
      } else if (!manualTagRequired && manualTagProhibited) {
        operator = TFSQueryOperator.AND_NOT;
      } else if (manualTagRequired && manualTagProhibited
          && (textStringQuery == null || "".equals(textStringQuery))) {
        throw new ParseException(new MessageImpl("Invalid query. If you "
            + "want to search for one or more exact intents but not for few "
            + "others, just don't specify intents you don't need."));
      }
    }

    return operator;
  }

  public static boolean isTermsOnlyQuery(List<? extends QueryClause> clauseList) {
    return clauseList
        .stream()
        .allMatch(C -> (C.getQueryType() instanceof TermQuery));
  }

  public static String buildQuery(List<? extends QueryClause> clauses) {
    StringBuilder queryBuilder = new StringBuilder();
    for (QueryClause clause : clauses) {
      if (queryBuilder.length() > 0) {
        queryBuilder.append(" ");
      }
      Query query = clause.getQueryType();
      String text = clause.getText().trim();
      log.info("queryText: {}", text);
      Matcher matcher = fieldValuePattern.matcher(text);
      String queryText =
          (query instanceof PhraseQuery && !matcher.matches()) ? String.format("'%s'", text) : text;
      if (clause.isProhibited()) {
        queryBuilder.append(String.format("-%s", queryText));
      } else if (clause.isRequired()) {
        queryBuilder.append(queryText);
      } else {
        if (queryBuilder.length() > 0) {
          queryBuilder.append("OR ").append(queryText);
        } else {
          queryBuilder.append(queryText);
        }
      }
    }
    return queryBuilder.toString();
  }

  public static QueryClause getQueryClause(String field, String text, Query query)
      throws ParseException {
    QueryClause clause = null;

    if (field == null || text == null) {
      throw new ParseException(new MessageImpl("Invalid term - " + field + ":" + text));
    }

    if (CommentClause.SEARCH_FIELD.equalsIgnoreCase(field)) {
      clause = new CommentClause();
    } else if (ManualTagClause.SEARCH_FIELD.equalsIgnoreCase(field)) {
      clause = new ManualTagClause();
    } else if (SuggestedCategoryClause.SEARCH_FIELD.equalsIgnoreCase(field)
        || SuggestedCategoryClause.ELASTIC_SEARCH_FIELD.equalsIgnoreCase(field)) {
      String formatString = null;
      String[] textTokens = text.split(" ");
      if (textTokens.length > 1) {
        formatString = "%s:'%s'";
      } else {
        formatString = "%s:%s";
      }
      clause = new TextStringForTaggingClause();
      text = String.format(formatString, SuggestedCategoryClause.ELASTIC_SEARCH_FIELD, text.trim());
    } else {
      clause = new TextStringForTaggingClause();
    }

    if (clause != null) {
      clause.setText(text);
      clause.setQueryType(query);
    }

    return clause;
  }

  public static QueryClause parsePrefixQuery(PrefixQuery query) throws ParseException {
    Term term = query.getPrefix();
    return getQueryClause(term.field(), term.text() + "*", query);
  }

  public static QueryClause parseWildcardQuery(WildcardQuery query) throws ParseException {
    Term term = query.getTerm();
    return getQueryClause(term.field(), term.text(), query);
  }

  public static QueryClause parsePhraseQuery(PhraseQuery query) throws ParseException {
    String field = query.getTerms()[0].field();
    StringBuilder phraseBuilder = new StringBuilder();
    for (Term term : query.getTerms()) {
      phraseBuilder.append(term.text());
      phraseBuilder.append(" ");
    }
    return getQueryClause(field, phraseBuilder.toString(), query);
  }

  public static QueryClause parseTermQuery(TermQuery query) throws ParseException {
    Term term = query.getTerm();
    return getQueryClause(term.field(), term.text(), query);
  }

  public static QueryClause parseTermRangeQuery(TermRangeQuery query) throws ParseException {
    String lowerTermText = new String(query.getLowerTerm().bytes).trim();
    String higherTermText = new String(query.getUpperTerm().bytes).trim();
    String field = query.getField();
    String text = String.format("%s[%s TO %s]", field, lowerTermText, higherTermText);
    return getQueryClause(field, text, query);
  }

  public static QueryClause getQueryClause(Query query) throws ParseException {
    if (query instanceof TermQuery) {
      return parseTermQuery((TermQuery) query);
    } else if (query instanceof PhraseQuery) {
      return parsePhraseQuery((PhraseQuery) query);
    } else if (query instanceof PrefixQuery) {
      return parsePrefixQuery((PrefixQuery) query);
    } else if (query instanceof WildcardQuery) {
      return parseWildcardQuery((WildcardQuery) query);
    } else if (query instanceof TermRangeQuery) {
      return parseTermRangeQuery((TermRangeQuery) query);
    } else {
      throw new ParseException();
    }
  }

  public static void bucketAndAnaylze(
      Query query,
      boolean required,
      boolean prohibited,
      List<CommentClause> commentClauseList,
      List<ManualTagClause> manualTagClauseList,
      List<TextStringForTaggingClause> textStringForTaggingClauseList) throws ParseException {
    QueryClause clause = getQueryClause(query);
    clause.setProhibited(prohibited);
    clause.setRequired(required);
    if (clause instanceof CommentClause) {
      commentClauseList.add((CommentClause) clause);
    } else if (clause instanceof ManualTagClause) {
      manualTagClauseList.add((ManualTagClause) clause);
    } else if (clause instanceof TextStringForTaggingClause) {
      textStringForTaggingClauseList.add((TextStringForTaggingClause) clause);
    }
  }

  public static QueryAggregator parseQueryString(String queryString) throws QueryNodeException {
    QueryAggregator queryAggregator = new QueryAggregator();

    if (queryString != null && !"".equals(queryString)) {
      int level = 0;
      Query query = luceneParser.parse(queryString, "textStringForTagging");
      Deque<Query> queryStack = new ArrayDeque<>();
      List<CommentClause> commentClauseList = new ArrayList<>();
      List<ManualTagClause> manualTagClauseList = new ArrayList<>();
      List<TextStringForTaggingClause> textStringForTaggingClauseList = new ArrayList<>();

      if (query instanceof BooleanQuery) {
        queryStack.push(query);
      } else {
        bucketAndAnaylze(query, true, false, commentClauseList, manualTagClauseList,
            textStringForTaggingClauseList);
      }

      TFSQueryOperator topOperator = TFSQueryOperator.AND;
      while (!queryStack.isEmpty()) {
        Query currentQuery = queryStack.pop();
        assert currentQuery instanceof BooleanQuery;
        BooleanQuery booleanQuery = (BooleanQuery) currentQuery;
        for (BooleanClause clause : booleanQuery.clauses()) {
          Query subQuery = clause.getQuery();
          if (subQuery instanceof BooleanQuery) {
            queryStack.push(subQuery);
            if (level == 0) {
              if (clause.isRequired() && !clause.isProhibited()) {
                topOperator = TFSQueryOperator.AND;
              } else if (!clause.isRequired()) {
                topOperator = TFSQueryOperator.OR;
              }
            }
          } else {
            bucketAndAnaylze(subQuery, clause.isRequired(), clause.isProhibited(),
                commentClauseList, manualTagClauseList, textStringForTaggingClauseList);
          }
        }
        level++;
      }

      String textStringQuery = buildQuery(textStringForTaggingClauseList);
      TFSQueryOperator manualTagClauseOperator =
          validateManualTagClauseList(manualTagClauseList, textStringQuery);

      queryAggregator.setCommentQuery(buildQuery(commentClauseList));
      queryAggregator.setManualTagClauseList(manualTagClauseList);
      queryAggregator.setTextStringForTaggingQuery(textStringQuery);
      queryAggregator.setQuery(query);

      TFSQueryOperator intentToOrigDocsOperator;
      if ((TFSQueryOperator.AND.equals(manualTagClauseOperator)
          && TFSQueryOperator.AND.equals(topOperator))
          || (TFSQueryOperator.AND_NOT.equals(manualTagClauseOperator)
          && TFSQueryOperator.AND_NOT.equals(topOperator))
          || (TFSQueryOperator.AND_NOT.equals(manualTagClauseOperator)
          && TFSQueryOperator.AND.equals(topOperator))
          || (TFSQueryOperator.AND.equals(manualTagClauseOperator)
          && TFSQueryOperator.AND_NOT.equals(topOperator))) {
        intentToOrigDocsOperator = TFSQueryOperator.AND;
      } else {
        intentToOrigDocsOperator = TFSQueryOperator.OR;
      }

      queryAggregator.setManualTagToOriginalDocumentsOperator(intentToOrigDocsOperator);
    }

    return queryAggregator;
  }
}
