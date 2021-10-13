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
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Component
public class SearchManagerDummyImpl implements SearchManager {

  @Override
  public long getUniqueTagCount(String clientId, String projectId) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getTaggedTranscriptionsCount(String clientId, String projectId,
      List<String> datasetIds) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getUntaggedTranscriptionsCount(String clientId, String projectId,
      List<String> datasetIds) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getUniqueTaggedTranscriptionCount(String clientId, String projectId,
      List<String> datasetIds) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getUniqueUntaggedTranscriptionCount(String clientId, String projectId,
      List<String> datasetIds) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<String> getIntentsByPrefix(String clientId, String intentPrefix, String projectId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranscriptionDocumentDetailCollection getFilteredTranscriptions(String clientId,
      String projectId, List<String> datasetIds, int startIndex, int limit,
      String queryOperator, List<String> sortByList, SearchRequest searchRequest, Locale locale) {
    return new TranscriptionDocumentDetailCollection();
  }

  @Override
  public TranscriptionDocumentDetailCollection getAllTranscriptions(String clientId,
      final String projectId,
      final List<String> datasetIds, final String queryOperator, List<String> sortByList,
      Boolean hasIntent) {
    return new TranscriptionDocumentDetailCollection();
  }

  @Override
  public StatsResponse getProjectDatasetStats(String clientId, String projectId,
      List<String> datasetIds) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isFieldExists(int clientId, String projectId, List<String> datasetIds,
      String fieldName) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Map<String, String> loadClassificationIds(String clientId, String projectId,
      List<String> lowCaseIntents,
      List<String> lowCaseRutag, Map<BoolQueryCondition, QueryBuilder> intentTagConditionToQueryMap,
      Map<BoolQueryCondition, QueryBuilder> ruTagconditionToQueryMap, boolean tagNegate,
      boolean ruTagNegate) {

    return null;
  }

  @Override
  public Map<String, String> loadIntentClassificationIds(String clientId, String projectId,
      List<ManualTagClause> manualTagClauseList,
      List<RuTagClause> lowCaseRutags,
      Map<BoolQueryCondition, QueryBuilder> intentConditionToQueryMap,
      Map<BoolQueryCondition, QueryBuilder> ruTagConditionToQueryMap, boolean tagOperationNegate,
      boolean ruTagOperationNegate) {
    return null;
  }


  @Override
  public List<TaggingGuideDocumentDetail> getTaggingGuideDocumentsForProject(String clientId,
      String projectId,
      List<String> sortByList) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, TaggingGuideDocumentDetail> getTaggingGuideDocumentStatsForProject(
      String clientId,
      String projectId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<String, TaggingGuideDocumentDetail> getClassificationDocumentsForProject(
      String clientId, String projectId) {
    // TODO Auto-generated method stub
    return null;
  }


}
