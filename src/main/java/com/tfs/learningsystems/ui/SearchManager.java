/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.search.query.model.BoolQueryCondition;
import com.tfs.learningsystems.ui.search.query.model.ManualTagClause;
import com.tfs.learningsystems.ui.search.query.model.RuTagClause;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.elasticsearch.index.query.QueryBuilder;

public interface SearchManager {

  public long getUniqueTagCount(String clientId, String projectId);

  public long getTaggedTranscriptionsCount(String clientId, String projectId,
      List<String> datasetIds);

  public long getUntaggedTranscriptionsCount(String clientId, String projectId,
      List<String> datasetIds);

  public long getUniqueTaggedTranscriptionCount(String clientId, String projectId,
      List<String> datasetIds);

  public long getUniqueUntaggedTranscriptionCount(String clientId, String projectId,
      List<String> datasetIds);


  public StatsResponse getProjectDatasetStats(String clientId, String projectId,
      List<String> datasetIds);

  public List<String> getIntentsByPrefix(String clientId, String intentPrefix, String projectId);

  public List<TaggingGuideDocumentDetail> getTaggingGuideDocumentsForProject(String clientId,
      String projectId,
      List<String> sortByList);

  public Map<String, TaggingGuideDocumentDetail> getTaggingGuideDocumentStatsForProject(
      String clientId, String projectId);

  public Map<String, TaggingGuideDocumentDetail> getClassificationDocumentsForProject(
      String clientId, String projectId);

  public TranscriptionDocumentDetailCollection getFilteredTranscriptions(String clientId,
      String projectId, List<String> datasetIds, int startIndex, int limit,
      String queryOperator, List<String> sortByList,
      SearchRequest searchRequest, Locale locale) throws QueryNodeException;

  public TranscriptionDocumentDetailCollection getAllTranscriptions(String clientId,
      String projectId, List<String> datasetIds, String queryOperator,
      List<String> sortByList, Boolean hasIntent);

  public boolean isFieldExists(int clientId, String projectId, List<String> datasetIds,
      String fieldName);

  public Map<String, String> loadClassificationIds(String clientId, String projectId,
      List<String> lowCaseIntents,
      List<String> lowCaseRutag, Map<BoolQueryCondition, QueryBuilder> intentTagConditionToQueryMap,
      Map<BoolQueryCondition, QueryBuilder> ruTagconditionToQueryMap, boolean tagNegate,
      boolean ruTagNegate);

  public Map<String, String> loadIntentClassificationIds(String clientId, String projectId,
      List<ManualTagClause> manualTagClauseList,
      List<RuTagClause> ruTagClauseList,
      Map<BoolQueryCondition, QueryBuilder> intentConditionToQueryMap,
      Map<BoolQueryCondition, QueryBuilder> ruTagConditionToQueryMap, boolean tagNegate,
      boolean ruTagNegate);


  /*  public Map<String, String> loadIntentClassificationIds (String clientId, String projectId, List<ManualTagClause> manualTagClauseList,
    List<RuTagClause> lowCaseRutags, Map<BoolQueryCondition, String> intentConditionToQueryMap,
    Map<BoolQueryCondition, String> ruTagConditionToQueryMap, boolean tagOperationNegate, boolean ruTagOperationNegate, boolean ifOrQuery);


    public Map<String, String> loadClassificationIds (String clientId, String projectId,List<ManualTagClause> manualTagClauseList,
    List<RuTagClause> rutagClauseList, List<String> lowCaseIntents,
    List<String> lowCaseRutag,  Map<BoolQueryCondition, String> intentConditionToQueryMap,
    Map<BoolQueryCondition, String> ruTagConditionToQueryMap, boolean tagNegate, boolean ruTagNegate, boolean ifOrQuery);*/
}
