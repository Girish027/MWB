/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.helper.SearchHelper;
import com.tfs.learningsystems.ui.model.DocumentType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.SearchRequestFilterAutoTagCountRange;
import com.tfs.learningsystems.ui.model.SearchRequestFilterWordCountRange;
import com.tfs.learningsystems.ui.model.SearchStats;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.search.query.model.BoolQueryCondition;
import com.tfs.learningsystems.ui.search.query.model.ManualTagClause;
import com.tfs.learningsystems.ui.search.query.model.QueryAggregator;
import com.tfs.learningsystems.ui.search.query.model.RuTagClause;
import com.tfs.learningsystems.ui.search.query.model.TFSQueryOperator;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.util.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Qualifier("searchManagerBean")
@Slf4j
public class SearchManagerImpl implements SearchManager {

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  @Qualifier("jsonObjectMapper")
  private ObjectMapper jsonObjectMapper;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;

  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private ContentManager contentManager;


  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  private static final String FOR_CLIENT_LABEL = " for Client ";
  private static final String PROJECT_LABEL = " project ";
  private static final String DATASET_ID_LABEL = " datasetId ";
  private static final String UNIQUE_TRANS_COUNT_LABEL = "unique_transcription_count";
  private static final String INTERSECTING_LABEL = "intersecting";
  private static final String UNKNOWN_SEARCH_QUERY_LABEL = "unknown_search_query";
  private static final String UNKNOWN_QUERY_TYPE_LABEL = "Unrecognized and unsupported query type";
  private static final String NULLABLE_LABEL = "[null]";
  private static final String INVALID_SEARCH_QUERY = "invalid_search_query";

  public BoolQueryBuilder getUntaggedBoolQueryBuilder(String projectId, List<String> datasetIds) {

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders
                    .termQuery(elasticSearchProps.getDocumentTypeLabel(), DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(elasticSearchProps.getProjectIdLabel(), projectId))
            .mustNot(QueryBuilders.existsQuery(elasticSearchProps.getClassificationIdLabel()));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder
              .filter(QueryBuilders.termsQuery(elasticSearchProps.getDatasetIdLabel(), datasetIds));
    }

    return queryBuilder;
  }

  public BoolQueryBuilder getTaggedBoolQueryBuilder(String projectId,
                                                    List<String> datasetIds) {

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders
                    .termQuery(elasticSearchProps.getDocumentTypeLabel(), DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(elasticSearchProps.getProjectIdLabel(), projectId))
            .must(QueryBuilders.existsQuery(elasticSearchProps.getClassificationIdLabel()));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder
              .filter(QueryBuilders.termsQuery(elasticSearchProps.getDatasetIdLabel(), datasetIds));
    }

    return queryBuilder;
  }

  public BoolQueryBuilder getTotalOriginalBoolQueryBuilder(String projectId,
                                                           List<String> datasetIds) {

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders
                    .termQuery(elasticSearchProps.getDocumentTypeLabel(), DocumentType.ORIGINAL.type()))
            .filter(QueryBuilders.termQuery(elasticSearchProps.getProjectIdLabel(), projectId));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder
              .filter(QueryBuilders.termsQuery(elasticSearchProps.getDatasetIdLabel(), datasetIds));
    }

    return queryBuilder;
  }

  @Override
  public long getUniqueTagCount(String clientId, String projectId) {

    validationManager.validateProjectId(projectId);

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

    if (!StringUtils.isEmpty(projectId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(elasticSearchProps.getProjectIdLabel(), projectId));
    }

    String indexName = elasticSearchProps.getClassificationIndexAlias();

    String functionality = " get unique tag count " +
            FOR_CLIENT_LABEL +
            clientId +
            PROJECT_LABEL;
    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(indexName, null, queryBuilder, 0, 0,
                    new String[]{elasticSearchProps.getClassificationIdLabel(),
                            elasticSearchProps.getProjectIdLabel()}, null,
                    functionality);

    return searchResponse.getHits().getTotalHits();
  }

  @Override
  public long getTaggedTranscriptionsCount(String clientId, String projectId,
                                           List<String> datasetIds) {

    validationManager.validateProjectId(projectId);

    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    BoolQueryBuilder queryBuilder = this.getTaggedBoolQueryBuilder(projectId, datasetIds);

    StringBuilder functionality = new StringBuilder(" get tagged transcriptions count  ")
            .append(FOR_CLIENT_LABEL)
            .append(clientId)
            .append(PROJECT_LABEL)
            .append(projectId)
            .append(DATASET_ID_LABEL)
            .append(datasetIds);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, elasticSearchProps.getSearchPageSize(),
                    new String[]{transcriptionHashLabel}, functionality.toString());

    BoolQueryBuilder intersectingBuilder = this
            .buildTranscriptionHashAggregationQuery( null, null, projectId, datasetIds,
                    "AND", null, null, false);

    Map<String, TranscriptionDocumentDetail> intersectingOrigDocsMap = new HashMap<>(this.getOriginalDocumentsForCount(
            intersectingBuilder));

    Set<String> taggedHashes = new HashSet<>();

    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    long scrollStartTime;
    long scrollEndTime;

    if (searchResponse != null) {
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          taggedHashes.add(hit.getSourceAsMap().get(transcriptionHashLabel).toString());
        }

        scrollStartTime = System.currentTimeMillis();
        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();
        scrollEndTime = System.currentTimeMillis();

        logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString());

      } while (searchResponse.getHits().getHits().length != 0);
    }

    List<TranscriptionDocumentDetail> taggedOrigDocs = intersectingOrigDocsMap
            .entrySet().stream()
            .filter(e -> taggedHashes.contains(e.getKey().toLowerCase()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

    return taggedOrigDocs.stream().filter(e -> e.getDocumentCount() > 0)
            .mapToLong(TranscriptionDocumentDetail::getDocumentCount).sum();
  }

  @Override
  public long getUntaggedTranscriptionsCount(String clientId, String projectId,
                                             List<String> datasetIds) {

    validationManager.validateProjectId(projectId);
    BoolQueryBuilder queryBuilder = this.getUntaggedBoolQueryBuilder(projectId, datasetIds);
    String functionality = " get untagged transcriptions count " +
            FOR_CLIENT_LABEL + clientId +
            PROJECT_LABEL + projectId +
            DATASET_ID_LABEL + datasetIds;
    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, functionality);

    return searchResponse.getHits().getTotalHits();
  }

  @Override
  public long getUniqueTaggedTranscriptionCount(String clientId, String projectId,
                                                List<String> datasetIds) {

    BoolQueryBuilder queryBuilder = this.getTaggedBoolQueryBuilder(projectId, datasetIds);

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .cardinality(UNIQUE_TRANS_COUNT_LABEL)
            .field(elasticSearchProps.getTranscriptionHashLabel())
            .precisionThreshold(
                    elasticSearchProps.getCardinalityPrecisionThreshold());

    String functionality = " get unique tagged transcriptions " +
            FOR_CLIENT_LABEL +
            clientId +
            PROJECT_LABEL +
            projectId +
            DATASET_ID_LABEL +
            datasetIds;

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregrationBuilder,
                    functionality);
    Cardinality cardinality = searchResponse.getAggregations().get(UNIQUE_TRANS_COUNT_LABEL);
    return cardinality.getValue();
  }

  @Override
  public long getUniqueUntaggedTranscriptionCount(String clientId, String projectId,
                                                  List<String> datasetIds) {

    validationManager.validateProjectId(projectId);
    BoolQueryBuilder queryBuilder = this.getUntaggedBoolQueryBuilder(projectId, datasetIds);
    getTotalOriginalBoolQueryBuilder(projectId, datasetIds);
    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .cardinality(UNIQUE_TRANS_COUNT_LABEL)
            .field(elasticSearchProps.getTranscriptionHashLabel())
            .precisionThreshold(
                    elasticSearchProps.getCardinalityPrecisionThreshold());

    String functionality = " get unique untagged transcriptions " +
            FOR_CLIENT_LABEL +
            clientId +
            PROJECT_LABEL +
            projectId +
            DATASET_ID_LABEL +
            datasetIds;
    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregrationBuilder,
                    functionality);

    Cardinality cardinality = searchResponse.getAggregations().get(UNIQUE_TRANS_COUNT_LABEL);
    return cardinality.getValue();
  }

  @Override
  public StatsResponse getProjectDatasetStats(String clientId, String projectId,
                                              List<String> datasetIds) {

    long intents = this.getUniqueTagCount(clientId, projectId);
    long tagged = this.getTaggedTranscriptionsCount(clientId, projectId, datasetIds);
    long allStringsTotal = searchHelper
            .getTotalNlToolsOriginalDocumentsCount(clientId, projectId, datasetIds);

    long uniqueTaggedTranscriptionCount = this
            .getUniqueTaggedTranscriptionCount(clientId, projectId, datasetIds);

    long uniqueStringsTotal = getUniqueOriginalTranscriptionCount(clientId, projectId, datasetIds);

    StatsResponse stats = new StatsResponse();
    stats.setIntents(intents);

    SearchStats allStringStats = new SearchStats();
    allStringStats.setTagged(tagged);
    allStringStats.setTotal(allStringsTotal);
    float allStringsPercent = 0;
    if (allStringsTotal > 0) {
      allStringsPercent = ((float) tagged / allStringsTotal) * 100;
    }
    allStringStats.setPercent(allStringsPercent);
    stats.setAll(allStringStats);

    SearchStats uniqueStringsStats = new SearchStats();
    uniqueStringsStats.setTagged(uniqueTaggedTranscriptionCount);
    uniqueStringsStats.setTotal(uniqueStringsTotal);
    float uniqueStringsPercent = 0;
    if (uniqueStringsTotal > 0) {
      uniqueStringsPercent = ((float) uniqueTaggedTranscriptionCount / uniqueStringsTotal) * 100;
    }
    uniqueStringsStats.setPercent(uniqueStringsPercent);
    stats.setUnique(uniqueStringsStats);

    return stats;
  }

  @Override
  public List<String> getIntentsByPrefix(String clientId, String intentPrefix, String projectId) {

    String intentLabel = elasticSearchProps.getGranularIntentLabel();
    String indexName = elasticSearchProps.getClassificationIndexAlias();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    List<String> matchedIntents = new ArrayList<>();

    if (intentPrefix != null) {
      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
              .must(QueryBuilders.matchPhrasePrefixQuery(intentLabel, intentPrefix
                      .toLowerCase()));

      StringBuilder functionality = new StringBuilder(" get intents by Prefix from classification ")
              .append(FOR_CLIENT_LABEL)
              .append(clientId)
              .append(PROJECT_LABEL);

      SearchResponse searchResponse = searchHelper
              .executeScrollRequest(indexName, projectId, queryBuilder, 0, 50,
                      new String[]{intentLabel},
                      null, functionality.toString());

      long scrollEndTime = 0;
      long scrollStartTime = 0;
      if (searchResponse != null) {
        do {
          for (SearchHit hit : searchResponse.getHits().getHits()) {
            matchedIntents.add(CommonUtils.sanitize(hit.getSourceAsMap().get(intentLabel).toString()));
          }

          scrollStartTime = System.currentTimeMillis();
          searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                  .setScroll(new TimeValue(searchGetTimeout))
                  .execute()
                  .actionGet();
          scrollEndTime = System.currentTimeMillis();

          logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
                  queryBuilder.toString());

        } while (searchResponse.getHits().getHits().length != 0);
      }

      Collections.sort(matchedIntents);
    }
    return matchedIntents;
  }

  @Override
  public List<TaggingGuideDocumentDetail> getTaggingGuideDocumentsForProject(String clientId,
                                                                             String projectId,
                                                                             List<String> sortByList) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationLabel = elasticSearchProps.getClassificationDataLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();

    List<TaggingGuideDocumentDetail> guideDocs = new ArrayList<>();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.matchAllQuery());

    if (!StringUtils.isEmpty(projectId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }

    queryBuilder = queryBuilder.must(QueryBuilders.existsQuery(classificationIdLabel));

    StringBuilder functionality = new StringBuilder(" get tagging guide documents from new index  ")
            .append(FOR_CLIENT_LABEL)
            .append(clientId)
            .append(PROJECT_LABEL)
            .append(projectId);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(indexName, projectId, queryBuilder, 0,
                    elasticSearchProps.getSearchPageSize(),
                    new String[]{classificationIdLabel, granularIntentLabel,
                            classificationLabel, commentsLabel, examplesLabel,
                            keywordsLabel, descriptionLabel}, null,
                    functionality.toString());

    long scrollStartTime;
    long scrollEndTime;

    while (searchResponse != null && searchResponse.getHits().getHits().length != 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        updateTaggingGuideDoc(hit, guideDocs);
      }
      scrollStartTime = System.currentTimeMillis();
      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(elasticSearchProps.getSearchActionGetTimeout()))
              .execute()
              .actionGet();

      scrollEndTime = System.currentTimeMillis();

      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }

    Map<String, TaggingGuideDocumentDetail> docMapWithStats = this
            .getTaggingGuideDocumentStatsForProject(clientId, projectId);

    for (TaggingGuideDocumentDetail doc : guideDocs) {
      String classificationId = doc.getClassificationId();
      TaggingGuideDocumentDetail docWithStats = docMapWithStats.get(classificationId);
      if (docWithStats != null) {
        doc.setCount(docWithStats.getCount());
        doc.setPercentage(docWithStats.getPercentage());
      }
    }

    sortByList.add("intent:asc");
    return SortUtil.sortTaggingGuideDocuments(guideDocs, sortByList, Locale.ENGLISH);
  }

  private void updateTaggingGuideDoc(SearchHit hit, List<TaggingGuideDocumentDetail> guideDocs) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationLabel = elasticSearchProps.getClassificationDataLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();

    String docId = hit.getId();
    TaggingGuideDocumentDetail doc = new TaggingGuideDocumentDetail();

    doc.setId(docId);

    if (hit.getSourceAsMap().containsKey(classificationIdLabel)) {
      doc.setClassificationId(
              hit.getSourceAsMap().get(classificationIdLabel) != null ? hit.getSourceAsMap()
                      .get(classificationIdLabel).toString() : null);
    }

    if (hit.getSourceAsMap().containsKey(granularIntentLabel)) {
      doc.setIntent(
              hit.getSourceAsMap().get(granularIntentLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
                      .get(granularIntentLabel).toString()) : null);
    }

    if (hit.getSourceAsMap().containsKey(classificationLabel)) {
      doc.setRutag(
              hit.getSourceAsMap().get(classificationLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
                      .get(classificationLabel).toString()) : null);
    }

    if (hit.getSourceAsMap().containsKey(commentsLabel)) {
      doc.setComments(hit.getSourceAsMap().get(commentsLabel) != null ? hit.getSourceAsMap()
              .get(commentsLabel).toString() : null);
    }

    if (hit.getSourceAsMap().containsKey(examplesLabel)) {
      doc.setExamples(hit.getSourceAsMap().get(examplesLabel) != null ? hit.getSourceAsMap()
              .get(examplesLabel).toString() : null);
    }

    if (hit.getSourceAsMap().containsKey(descriptionLabel)) {
      doc.setDescription(hit.getSourceAsMap().get(descriptionLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
                      .get(descriptionLabel).toString()) : null);
    }

    if (hit.getSourceAsMap().containsKey(keywordsLabel)) {
      doc.setKeywords(hit.getSourceAsMap().get(keywordsLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
              .get(keywordsLabel).toString()) : null);
    }
    guideDocs.add(doc);
  }

  @Override
  public Map<String, TaggingGuideDocumentDetail> getTaggingGuideDocumentStatsForProject(
          String clientId, String projectId) {

    Map<String, TaggingGuideDocumentDetail> documentMap = new LinkedHashMap<>();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()));

    queryBuilder = queryBuilder.must(QueryBuilders.existsQuery(classificationIdLabel));

    if (!StringUtils.isEmpty(projectId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }

    StringBuilder functionality = new StringBuilder(" get tagging guide document stats ")
            .append(FOR_CLIENT_LABEL).append(clientId)
            .append(PROJECT_LABEL);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0,
                    elasticSearchProps.getSearchPageSize(), new String[]{hashLabel, classificationIdLabel},
                    null, functionality.toString());

    Map<String, String> taggedHashes = new HashMap<>();

    long scrollStartTime;
    long scrollEndTime;

    while (searchResponse != null && searchResponse.getHits().getHits().length != 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        taggedHashes.put(hit.getSourceAsMap().get(hashLabel).toString(),
                        hit.getSourceAsMap().get(classificationIdLabel).toString());
      }

      scrollStartTime = System.currentTimeMillis();
      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(searchGetTimeout))
              .execute()
              .actionGet();
      scrollEndTime = System.currentTimeMillis();

      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }

    BoolQueryBuilder intersectingBuilder = this
            .buildTranscriptionHashAggregationQuery(null, null, projectId, null,
                    "AND", null, null, false);

    Map<String, TranscriptionDocumentDetail> intersectingOrigDocsMap = new HashMap<>(this.getOriginalDocumentsForCount(
            intersectingBuilder));

    List<TranscriptionDocumentDetail> taggedOrigDocs = intersectingOrigDocsMap.entrySet()
            .stream()
            .filter(e -> taggedHashes.containsKey(e.getKey().toLowerCase()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

    long totalcount = taggedOrigDocs.stream().filter(e -> e.getDocumentCount() > 0)
            .mapToLong(TranscriptionDocumentDetail::getDocumentCount).sum();

    HashMap<String, Long> classificationIdCount = new HashMap<>();

    String classificationId;

    TranscriptionDocumentDetail transcriptionDocumentDetail;
    long docCount;

    long classificationDocCount;
    for (Entry<String,String> entry : taggedHashes.entrySet()) {
      docCount = 0L;
      transcriptionDocumentDetail = intersectingOrigDocsMap.get(entry.getKey());

      if (transcriptionDocumentDetail != null
              && transcriptionDocumentDetail.getDocumentCount() != null) {
        docCount = transcriptionDocumentDetail.getDocumentCount();
      }

      classificationId = entry.getValue();

      if (classificationIdCount.get(classificationId) != null) {
        classificationDocCount = classificationIdCount.get(classificationId);
        classificationDocCount = classificationDocCount + docCount;
        classificationIdCount.put(classificationId, classificationDocCount);

      } else {
        classificationIdCount.put(classificationId, docCount);
      }
    }

    long classificationCount;
    for (Entry<String,Long> entry : classificationIdCount.entrySet()) {

      String classificationIdKey = entry.getKey();
      TaggingGuideDocumentDetail doc = new TaggingGuideDocumentDetail();
      classificationCount = entry.getValue();
      doc.setCount(classificationCount);
      doc.setClassificationId(classificationIdKey);
      doc.setPercentage((float)(classificationCount * 100) / totalcount);
      documentMap.put(classificationIdKey, doc);
    }

    return documentMap;
  }

  @Override
  public Map<String, TaggingGuideDocumentDetail> getClassificationDocumentsForProject(
          String clientId, String projectId) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationLabel = elasticSearchProps.getClassificationDataLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();

    validationManager.validateProjectId(projectId);

    Map<String, TaggingGuideDocumentDetail> classificationDocs = new HashMap<>();
    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.matchAllQuery());

    if (!StringUtils.isEmpty(projectId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .terms(classificationIdLabel)
            .field(classificationIdLabel)
            .size(Integer.MAX_VALUE)
            .subAggregation(
                    AggregationBuilders.topHits("top")
                            .fetchSource(
                                    new String[]{granularIntentLabel,
                                            classificationLabel, classificationIdLabel,
                                            commentsLabel,
                                            examplesLabel, keywordsLabel,
                                            descriptionLabel}, null)
                            .size(1));

    String functionality = " get classification documents " +
            FOR_CLIENT_LABEL + clientId +
            PROJECT_LABEL + projectId;

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(indexName, projectId, queryBuilder, 0, Integer.MAX_VALUE, new String[]{},
                    aggregrationBuilder,
                    functionality);

    if (searchResponse != null) {
      Terms agg = searchResponse.getAggregations().get(classificationIdLabel);
      for (Terms.Bucket entry : agg.getBuckets()) {
        // We ask for top_hits for each bucket
        TopHits topHits = entry.getAggregations().get("top");
        for (SearchHit hit : topHits.getHits().getHits()) {
          TaggingGuideDocumentDetail doc = new TaggingGuideDocumentDetail();

          Map<String, Object> sourceMap = hit.getSourceAsMap();
          String classificationId = (String) sourceMap.get(classificationIdLabel);
          doc.setId(hit.getId());
          doc.setIntent((String) sourceMap.get(granularIntentLabel));
          doc.setRutag((String) sourceMap.get(classificationLabel));
          doc.setComments((String) sourceMap.get(commentsLabel));
          doc.setExamples((String) sourceMap.get(examplesLabel));
          doc.setKeywords((String) sourceMap.get(keywordsLabel));
          doc.setDescription((String) sourceMap.get(descriptionLabel));
          doc.setClassificationId(classificationId);
          classificationDocs.put(classificationId, doc);

        }
      }
    }
    return classificationDocs;
  }

  @Override
  public TranscriptionDocumentDetailCollection getFilteredTranscriptions(String clientId,
                                                                         String projectId, List<String> datasetIds,
                                                                         int startIndex, int limit, String queryOperator,
                                                                         List<String> sortByList, SearchRequest searchRequest,
                                                                         Locale locale) throws QueryNodeException {

    validationManager.validateProjectId(projectId);

    SearchRequestFilter requestFilter = searchRequest.getFilter();
    if (requestFilter == null) {
      requestFilter = new SearchRequestFilter();
    }

    TranscriptionDocumentDetailCollection transcriptionCollection = new TranscriptionDocumentDetailCollection();

    transcriptionCollection.setLimit(limit);
    transcriptionCollection.setStartIndex(startIndex);
    transcriptionCollection.setTotal(0L);

    String mainSearchQuery = searchRequest.getQuery();

    QueryAggregator queryAggregator = QueryParserUtil.parseQueryString(mainSearchQuery);
    TFSQueryOperator combiningOperatorFromQuery = queryAggregator
            .getManualTagToOriginalDocumentsOperator();

    log.info("QueryAggregator: {}", queryAggregator);

    if (Boolean.FALSE.equals(requestFilter.getTagged()) && Boolean.TRUE.equals(requestFilter.getUntagged())
            && queryAggregator.getManualTagClauseList() != null && !queryAggregator.getManualTagClauseList().isEmpty()) {
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), INVALID_SEARCH_QUERY,
                      "Cannot search for granular_intent with untagged filter"));
    }

    Map<String, TranscriptionDocumentDetail> commentAwareDocumentsMap;
    boolean hasComment = requestFilter.getHasComment() != null && requestFilter.getHasComment();

    if (hasComment) {
      commentAwareDocumentsMap = this.getCommentAwareTranscriptions(projectId, datasetIds,
              queryAggregator.getCommentQuery(), Operator.AND);
    } else {
      if (queryAggregator.getCommentQuery() != null && !"".equals(queryAggregator.getCommentQuery())) {
        throw new InvalidRequestException(
                new Error(Response.Status.BAD_REQUEST.getStatusCode(), INVALID_SEARCH_QUERY,
                        "Cannot search for comment without has comment filter"));
      }
      commentAwareDocumentsMap = new HashMap<>();
    }

    boolean tagged = requestFilter.getTagged();
    boolean untagged = requestFilter.getUntagged();

    Map<String, Map<String, TranscriptionDocumentDetail>> intentAwareDocumentsMap = this.getIntentAwareTranscriptions(
            queryAggregator, tagged, untagged, hasComment, clientId, projectId, datasetIds, commentAwareDocumentsMap);

    if (!intentAwareDocumentsMap.isEmpty()) {
      String queryTxt = queryAggregator.getTextStringForTaggingQuery();
      Query query = queryAggregator.getQuery();

      Map<String, TranscriptionDocumentDetail> documentsMap = this.getTranscriptionHashAggregation(queryTxt, query, combiningOperatorFromQuery,
              queryOperator, projectId, datasetIds, requestFilter.getWordCountRange(), requestFilter.getAutoTagCountRange(), intentAwareDocumentsMap);

      int totalDocuments = documentsMap.size();
      transcriptionCollection.setTotal((long) totalDocuments);

      if (!documentsMap.isEmpty() && startIndex < documentsMap.size()) {
        if (!hasComment) {
          this.getTranscriptionComments(projectId, datasetIds, documentsMap);
        }

        List<TranscriptionDocumentDetail> documentList = new ArrayList<>(documentsMap.values());
        List<TranscriptionDocumentDetail> sortedDocumentList = SortUtil.sortTranscriptions(documentList, sortByList, locale);

        int endIndex = startIndex + limit;
        endIndex = ((totalDocuments < endIndex) || (limit == -1)) ? totalDocuments : endIndex;
        List<TranscriptionDocumentDetail> documentSubList = sortedDocumentList
                .subList(startIndex, endIndex);

        transcriptionCollection.setTranscriptionList(documentSubList);
      }
    }
    return transcriptionCollection;
  }

  @Override
  public TranscriptionDocumentDetailCollection getAllTranscriptions(final String clientId,
                                                                    final String projectId,
                                                                    final List<String> datasetIds, String queryOperator, List<String> sortByList,
                                                                    Boolean hasIntent) {

    validationManager.validateProjectId(projectId);

    String transcriptionLabel = elasticSearchProps.getTranscriptionLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String transcriptionOriginalLabel = elasticSearchProps.getTranscriptionOriginalLabel();
    String intentLabel = elasticSearchProps.getTagLabel();
    String ruTagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String uuidLabel = elasticSearchProps.getUuidLabel();
    String filenameLabel = elasticSearchProps.getFilenameLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String autoTagCountLabel = elasticSearchProps.getAutoTagCountLabel();
    String autoTagStringLabel = elasticSearchProps.getAutoTagStringLabel();

    BoolQueryBuilder queryBuilder = this
            .buildTranscriptionHashAggregationQuery(null, null, projectId,
                    datasetIds, queryOperator, null, null, false);

    StringBuilder functionality = new StringBuilder(" get all transcriptions ")
            .append(FOR_CLIENT_LABEL)
            .append(clientId).append(PROJECT_LABEL).append(projectId)
            .append(DATASET_ID_LABEL).append(datasetIds);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, elasticSearchProps.getSearchPageSize(),
                    new String[]{uuidLabel,filenameLabel, transcriptionLabel, transcriptionHashLabel,
                            transcriptionOriginalLabel, autoTagCountLabel, autoTagStringLabel,
                            intentLabel, ruTagLabel, datasetIdLabel}, functionality.toString());

    MultiValueMap<String, TranscriptionDocumentDetail> exportMap = new LinkedMultiValueMap<>();

    long scrollEndTime = 0;
    long scrollStartTime = 0;
    while (searchResponse != null && searchResponse.getHits().getHits().length != 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        updateTranscription(hit, exportMap);
      }
      scrollStartTime = System.currentTimeMillis();
      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(elasticSearchProps.getSearchActionGetTimeout()))
              .execute()
              .actionGet();

      scrollEndTime = System.currentTimeMillis();
      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }

    Map<String, TranscriptionDocumentDetail> intentJoinMap = new HashMap<>();
    Map<String, TranscriptionDocumentDetail> documentsMap = new HashMap<>();

    TranscriptionDocumentDetailCollection collection = new TranscriptionDocumentDetailCollection();
    if (!exportMap.isEmpty()) {

      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter requestFilter = new SearchRequestFilter().tagged(false).untagged(false);
      searchRequest.setFilter(requestFilter);

      String mainSearchQuery = searchRequest.getQuery();

      QueryAggregator queryAggregator = new QueryAggregator();
      try {
        queryAggregator = QueryParserUtil.parseQueryString(mainSearchQuery);
      } catch (QueryNodeException e) {
        log.warn("Failed to get parse query string - {}", mainSearchQuery);
      }

      Map<String, TranscriptionDocumentDetail> commentAwareDocumentsMap = this.getCommentAwareTranscriptions(projectId, datasetIds,
              queryAggregator.getCommentQuery(), Operator.AND);

      Map<String, Map<String, TranscriptionDocumentDetail>> intentAwareDocumentsMap = this.getIntentAwareTranscriptions(new QueryAggregator(),
              false, false, false,
              clientId, projectId, datasetIds,
              new HashMap<String, TranscriptionDocumentDetail>());

      Map<String, TranscriptionDocumentDetail> intentMap = intentAwareDocumentsMap
              .get(INTERSECTING_LABEL);

      Map<String, TranscriptionDocumentDetail> intersectingOrigDocsMap = new HashMap<>(this.getOriginalDocuments(queryBuilder));

      this.joinOrigAndIntentDocs(intersectingOrigDocsMap, intentMap, intentJoinMap);
      this.joinOrigAndCommentDocs(intentJoinMap, commentAwareDocumentsMap, documentsMap);

      if (Boolean.TRUE.equals(hasIntent)) {
        exportMap.keySet().retainAll(intentMap.keySet());
      }

      for (Entry<String, TranscriptionDocumentDetail> entry : documentsMap.entrySet()) {
        String key = entry.getKey();
        TranscriptionDocumentDetail detail = entry.getValue();
        String intent = detail.getIntent();
        String taggedAt = detail.getTaggedAt();
        String taggedBy = detail.getTaggedBy();
        String ruTag = detail.getRutag();
        String comment = detail.getComment();
        String datasetSource = detail.getDatasetSource();
        for (TranscriptionDocumentDetail transcriptionDocumentDetail : Objects.requireNonNull(exportMap.get(key))) {
          transcriptionDocumentDetail.setIntent(intent);
          transcriptionDocumentDetail.setTaggedAt(taggedAt);
          transcriptionDocumentDetail.setTaggedBy(taggedBy);
          transcriptionDocumentDetail.setRutag(ruTag);
          transcriptionDocumentDetail.setComment(comment);
          transcriptionDocumentDetail.setDatasetSource(datasetSource);
          collection.addTranscriptionListItem(transcriptionDocumentDetail);
        }
      }
    }
    collection.setStartIndex(0);
    collection.setTotal(Integer.toUnsignedLong(collection.getTranscriptionList().size()));
    collection.setLimit(collection.getTranscriptionList().size());
    return collection;
  }

  private void updateTranscription(SearchHit hit, MultiValueMap<String, TranscriptionDocumentDetail> exportMap) {

    String transcriptionLabel = elasticSearchProps.getTranscriptionLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String transcriptionOriginalLabel = elasticSearchProps.getTranscriptionOriginalLabel();
    String ruTagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String uuidLabel = elasticSearchProps.getUuidLabel();
    String filenameLabel = elasticSearchProps.getFilenameLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String autoTagCountLabel = elasticSearchProps.getAutoTagCountLabel();
    String autoTagStringLabel = elasticSearchProps.getAutoTagStringLabel();

    String docId = hit.getId();
    TranscriptionDocumentDetail doc = new TranscriptionDocumentDetail();
    if (hit.getSourceAsMap().containsKey(uuidLabel) && hit.getSourceAsMap().get(uuidLabel)!=null) {
      doc.setUuid(hit.getSourceAsMap().get(uuidLabel).toString());
    } else {
      doc.setUuid(docId);
    }
    if (hit.getSourceAsMap().containsKey(transcriptionOriginalLabel) && hit.getSourceAsMap().get(transcriptionOriginalLabel)!=null) {
      doc.setTextStringOriginal(
              hit.getSourceAsMap().get(transcriptionOriginalLabel).toString());
    } else {
      doc.setTextStringOriginal(NULLABLE_LABEL);
    }
    if (hit.getSourceAsMap().containsKey(transcriptionLabel) && hit.getSourceAsMap().get(transcriptionLabel)!=null) {
      doc.setTextStringForTagging(hit.getSourceAsMap().get(transcriptionLabel).toString());
    } else {
      doc.setTextStringForTagging(NULLABLE_LABEL);
    }
    if (hit.getSourceAsMap().containsKey(autoTagStringLabel) && hit.getSourceAsMap().get(autoTagStringLabel)!=null) {
      doc.setAutoTagString(hit.getSourceAsMap().get(autoTagStringLabel).toString());
    } else {
      doc.setAutoTagString(NULLABLE_LABEL);
    }
    if (hit.getSourceAsMap().containsKey(filenameLabel) && hit.getSourceAsMap().get(filenameLabel)!=null) {
      doc.setFileName(hit.getSourceAsMap().get(filenameLabel).toString());
    } else {
      doc.setFileName(null);
    }
    if (hit.getSourceAsMap().containsKey(ruTagLabel) && hit.getSourceAsMap().get(ruTagLabel)!=null) {
      doc.setRutag(hit.getSourceAsMap().get(ruTagLabel).toString());
    } else {
      doc.setRutag(null);
    }

    int numAutoTags = 0;
    try {
      numAutoTags = Integer.parseInt(hit.getSourceAsMap().get(autoTagCountLabel).toString());
    } catch (Exception e) {
      log.warn("Failed to get number of auto tags");
    }
    doc.setAutoTagCount(numAutoTags);
    doc.getDatasetIds().add(Integer.valueOf(hit.getSourceAsMap().get(datasetIdLabel).toString()));
    doc.setDocumentCount(1L);
    String hash = hit.getSourceAsMap().get(transcriptionHashLabel).toString();
    doc.setTranscriptionHash(hash);
    exportMap.add(hash, doc);
  }

  @Override
  public boolean isFieldExists(int clientId, String projectId, List<String> datasetIds,
                               String fieldName) {

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String documentType = DocumentType.ORIGINAL.type();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, documentType))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .must(QueryBuilders.existsQuery(fieldName));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, null);

    if (searchResponse != null) {
      return searchResponse.getHits().getTotalHits() > 0;
    }
    return false;
  }

  @Override
  public Map<String, String> loadIntentClassificationIds(String clientId, String projectId,
                                                         List<ManualTagClause> manualTagClauseList,
                                                         List<RuTagClause> lowCaseRutags,
                                                         Map<BoolQueryCondition, QueryBuilder> intentConditionToQueryMap,
                                                         Map<BoolQueryCondition, QueryBuilder> ruTagConditionToQueryMap, boolean tagOperationNegate,
                                                         boolean ruTagOperationNegate) {

    Map<String, String> intentclassificationMap;

    List<String> intentsList = new ArrayList<>();
    if (manualTagClauseList != null && !manualTagClauseList.isEmpty()) {
      for (ManualTagClause clause : manualTagClauseList) {

        intentsList.add(clause.getText().toLowerCase());

      }
    }

    List<String> rutags = new ArrayList<>();
    if (lowCaseRutags != null && !lowCaseRutags.isEmpty()) {
      for (RuTagClause ruTagClause : lowCaseRutags) {

        rutags.add(CommonUtils.sanitize(ruTagClause.getText().toLowerCase()));
      }
    }

    intentclassificationMap = this
            .loadClassificationIds(clientId, projectId, intentsList, rutags, intentConditionToQueryMap,
                    ruTagConditionToQueryMap, tagOperationNegate, ruTagOperationNegate);

    return intentclassificationMap;

  }

  @Override
  public Map<String, String> loadClassificationIds(String clientId, String projectId, List<String> lowCaseIntents, List<String> lowCaseRutags,
                                                   Map<BoolQueryCondition, QueryBuilder> intentTagConditionToQueryMap,
                                                   Map<BoolQueryCondition, QueryBuilder> ruTagconditionToQueryMap,
                                                   boolean tagOperationNegate, boolean ruTagOperationNegate) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    Map<String, String> intentClassificationIdMap = new HashMap<>();

    if ((lowCaseIntents == null || lowCaseIntents.isEmpty()) && (lowCaseRutags == null || lowCaseRutags.isEmpty())) {
      return null;
    }

    String[] fields = {classificationIdLabel, granularIntentLabel, classificationDataLabel};
    StringBuilder functionality = new StringBuilder(" get intent classification document maps ")
            .append(" for client ").append(clientId).append(PROJECT_LABEL).append(projectId);

    if (lowCaseIntents != null && !lowCaseIntents.isEmpty()) {
      queryBuilder = buildConditionQuery(queryBuilder, tagOperationNegate, lowCaseIntents, granularIntentLabel,
              intentTagConditionToQueryMap);
      functionality.append("and for Intent ").append(Arrays.toString(lowCaseIntents.toArray()));
    }

    if (lowCaseRutags != null && !lowCaseRutags.isEmpty()) {
      queryBuilder = buildConditionQuery(queryBuilder, ruTagOperationNegate, lowCaseRutags, classificationDataLabel,
              ruTagconditionToQueryMap);
      functionality.append("and for rutags ").append(Arrays.toString(lowCaseRutags.toArray()));
    }

    SearchResponse searchResponse = searchHelper.executeScrollRequest(indexName, projectId, queryBuilder,
            0, 1, fields, null, functionality.toString());

    long scrollEndTime = 0;
    long scrollStartTime = 0;

    while (searchResponse != null && searchResponse.getHits().getHits().length != 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        if (hit.getSourceAsMap().get(classificationIdLabel) != null) {
          updateIntentClassificationIdMap(hit, intentClassificationIdMap);
        }
      }

      scrollStartTime = System.currentTimeMillis();
      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(elasticSearchProps.getSearchActionGetTimeout()))
              .execute()
              .actionGet();

      scrollEndTime = System.currentTimeMillis();
      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }
    return intentClassificationIdMap;
  }

  private void updateIntentClassificationIdMap(SearchHit hit, Map<String, String> intentClassificationIdMap) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String classificationId = null;
    String granularIntent = null;
    String classificationData = null;

    if (hit.getSourceAsMap().containsKey(classificationIdLabel)) {
      classificationId = hit.getSourceAsMap().get(classificationIdLabel) != null ? hit.getSourceAsMap()
              .get(classificationIdLabel).toString() : null;
    }

    if (hit.getSourceAsMap().containsKey(granularIntentLabel)) {
      granularIntent = hit.getSourceAsMap().get(granularIntentLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
              .get(granularIntentLabel).toString()) : null;
    }

    if (hit.getSourceAsMap().containsKey(classificationDataLabel)) {
      classificationData = hit.getSourceAsMap().get(classificationDataLabel) != null ? CommonUtils.sanitize(hit.getSourceAsMap()
              .get(classificationDataLabel).toString()) : null;
    }

    if (classificationId != null) {
      if (granularIntent != null && !StringUtils.isEmpty(granularIntent)) {
        intentClassificationIdMap.put(granularIntent, classificationId);
      } else {
        intentClassificationIdMap.put(Constants.INTENT_RU_DELIMITER + classificationData, classificationId);
      }
    }
  }

  private BoolQueryBuilder buildConditionQuery(BoolQueryBuilder queryBuilder, boolean tagOperationNegate,
                                                        List<String> lowCaseIntents, String label,
                                                        Map<BoolQueryCondition, QueryBuilder> tagConditionToQueryMap) {

    if (tagConditionToQueryMap != null && tagConditionToQueryMap.size() > 0) {
      for (Map.Entry<BoolQueryCondition, QueryBuilder> entry : tagConditionToQueryMap.entrySet()) {
        BoolQueryCondition thisCondition = entry.getKey();
        QueryBuilder subQueryBuilder = entry.getValue();
        if (BoolQueryCondition.MUST.equals(thisCondition)) {
          if (tagOperationNegate) {
            queryBuilder.mustNot(subQueryBuilder);
          } else {
            queryBuilder.must(subQueryBuilder);
          }
        } else if (BoolQueryCondition.MUST_NOT.equals(thisCondition)) {
          if (tagOperationNegate) {
            queryBuilder.must(subQueryBuilder);
          } else {
            queryBuilder.mustNot(subQueryBuilder);
          }
        }
      }
    } else {
      queryBuilder = queryBuilder.filter(QueryBuilders.termsQuery(label, lowCaseIntents));
    }

    return queryBuilder;
  }

  public long getUniqueOriginalTranscriptionCount(String clientId, String projectId,
                                                  List<String> datasetIds) {

    validationManager.validateProjectId(projectId);

    BoolQueryBuilder queryBuilder = this.getTotalOriginalBoolQueryBuilder(projectId, datasetIds);

    getTotalOriginalBoolQueryBuilder(projectId, datasetIds);

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .cardinality(UNIQUE_TRANS_COUNT_LABEL)
            .field(elasticSearchProps.getTranscriptionHashLabel())
            .precisionThreshold(
                    elasticSearchProps.getCardinalityPrecisionThreshold());

    String functionality = " get unique original transcriptions " +
            FOR_CLIENT_LABEL + clientId + PROJECT_LABEL +
            projectId + DATASET_ID_LABEL + datasetIds;
    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregrationBuilder,
                    functionality);
    Cardinality cardinality = searchResponse.getAggregations().get(UNIQUE_TRANS_COUNT_LABEL);
    return cardinality.getValue();
  }

  public BoolQueryBuilder addRangeQuery(int min, int max, String fieldLabel,
                                        BoolQueryBuilder queryBuilder) {

    if (min > 0 && max > 0 && min == max) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(fieldLabel, min));
    } else if (min >= 1 && max == 0) {
      queryBuilder = queryBuilder.filter(QueryBuilders.rangeQuery(fieldLabel).gte(min));
    } else if (min <= 1 && max > 1) {
      queryBuilder = queryBuilder.filter(QueryBuilders.rangeQuery(fieldLabel).lte(max));
    } else if (min > 0 && max > 0 && max > min) {
      queryBuilder = queryBuilder.filter(QueryBuilders.rangeQuery(fieldLabel).gte(min).lte(max));
    }

    return queryBuilder;
  }

  public AbstractAggregationBuilder getIntentAwareAggreationBuilder(String classificationLabel,
                                                                    String hashLabel, String taggedAtLabel,
                                                                    String taggedByLabel, String datasetIdLabel, String orderByfield, String orderByCondition) {
    SortOrder order;
    if(StringUtils.isEmpty(orderByCondition)) {
      order = SortOrder.ASC;
    } else {
      order = orderByCondition.equalsIgnoreCase(Constants.DECENDING) ? SortOrder.DESC : SortOrder.ASC;
    }

    return AggregationBuilders
            .terms(datasetIdLabel)
            .field(datasetIdLabel)
            .size(Integer.MAX_VALUE)
            .subAggregation(
                    AggregationBuilders
                            .terms(hashLabel)
                            .field(hashLabel)
                            .size(Integer.MAX_VALUE)
                            .subAggregation(
                                    AggregationBuilders
                                            .topHits("top")
                                            .sort(StringUtils
                                                            .isEmpty(orderByfield) ? datasetIdLabel : orderByfield,
                                                    order)
                                            .fetchSource(
                                                    new String[]{classificationLabel, hashLabel, taggedAtLabel,
                                                            taggedByLabel,
                                                            datasetIdLabel}, null)
                                            .size(1)));
  }

  public BoolQueryBuilder buildTranscriptionHashAggregationQuery(
          String queryTxt, Query query, String projectId, List<String> datasetIds,
          String queryOperator, SearchRequestFilterWordCountRange wordCountRange,
          SearchRequestFilterAutoTagCountRange autoTagCountRange, boolean negate) {

    String tokensLabel = elasticSearchProps.getNumTokensLabel();
    String textLabel = elasticSearchProps.getTranscriptionLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String autoTagCountLabel = elasticSearchProps.getAutoTagCountLabel();

    if (queryTxt == null || "".equals(queryTxt.trim())) {
      queryTxt = "*";
    }

    Operator thisQueryOperator = Operator.OR;
    if (queryOperator != null) {
      try {
        thisQueryOperator = Operator.valueOf(queryOperator);
      } catch (IllegalArgumentException iae) {
        log.warn("Invalid query operator {}. Using default operator OR", queryOperator);
      }
    }

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.ORIGINAL.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    QueryBuilder termQueryBuilder = null;
    if (query instanceof TermRangeQuery) {
      TermRangeQuery trq = (TermRangeQuery) query;
      String lower = new String(trq.getLowerTerm().bytes).trim();
      String upper = new String(trq.getUpperTerm().bytes).trim();
      termQueryBuilder = QueryBuilders.rangeQuery(trq.getField()).from(lower).to(upper)
              .includeLower(true).includeUpper(true);
    } else {
      termQueryBuilder = QueryBuilders.queryStringQuery(queryTxt).defaultField(textLabel)
              .analyzeWildcard(true)
              .defaultOperator(thisQueryOperator);
    }

    if (negate) {
      queryBuilder.mustNot(termQueryBuilder);
    } else {
      queryBuilder.must(termQueryBuilder);
    }

    if (wordCountRange != null) {
      queryBuilder = this
              .addRangeQuery(wordCountRange.getMin(), wordCountRange.getMax(), tokensLabel,
                      queryBuilder);
    }

    if (autoTagCountRange != null) {
      queryBuilder = this
              .addRangeQuery(autoTagCountRange.getMin(), autoTagCountRange.getMax(), autoTagCountLabel,
                      queryBuilder);
    }
    return queryBuilder;
  }


  public Map<String, TranscriptionDocumentDetail> getOriginalDocumentsForCount(QueryBuilder queryBuilder) {

    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    Map<String, TranscriptionDocumentDetail> documentsMap = new HashMap<>();

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .terms(datasetIdLabel)
            .field(datasetIdLabel)
            .size(Integer.MAX_VALUE)
            .subAggregation(
                    AggregationBuilders
                            .terms(hashLabel)
                            .field(hashLabel)
                            .size(Integer.MAX_VALUE)
                            .subAggregation(
                                    AggregationBuilders
                                            .topHits("top")
                                            .fetchSource(
                                                    new String[]{hashLabel, datasetIdLabel
                                                    }, null)
                                            .size(1)));

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregrationBuilder,
                    " get original transcriptions_1 ");

    if (searchResponse != null) {
      Terms datasetTermBuckets = searchResponse.getAggregations().get(datasetIdLabel);
      for (Terms.Bucket datasetBucket : datasetTermBuckets.getBuckets()) {
        Terms hashTermBuckets = datasetBucket.getAggregations().get(hashLabel);
        log.info("Total transcription hash buckets: {}", hashTermBuckets.getBuckets().size());
        for (Terms.Bucket hashBucket : hashTermBuckets.getBuckets()) {
          String thisHash = hashBucket.getKeyAsString();
          long docCount = hashBucket.getDocCount();

          TopHits topHits = hashBucket.getAggregations().get("top");
          for (SearchHit hit : topHits.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            if (sourceMap != null) {
              TranscriptionDocumentDetail document = documentsMap.get(thisHash);
              if (document == null) {
                document = new TranscriptionDocumentDetail();
                document.setDocumentCount(docCount);
                document.setTranscriptionHash(thisHash);
              } else {
                document.setDocumentCount(docCount + document.getDocumentCount());
              }
              document.getDatasetIds()
                      .add(Integer.valueOf(String.valueOf(sourceMap.get(datasetIdLabel))));
              documentsMap.put(thisHash, document);
            }
          }
        }
      }
    }
    return documentsMap;
  }

  public Map<String, TranscriptionDocumentDetail> getOriginalDocuments(QueryBuilder queryBuilder) {

    String intentLabel = elasticSearchProps.getTagLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String tokensLabel = elasticSearchProps.getNumTokensLabel();
    String textLabel = elasticSearchProps.getTranscriptionLabel();
    String autoTagCountLabel = elasticSearchProps.getAutoTagCountLabel();
    String autoTagStringLabel = elasticSearchProps.getAutoTagStringLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String ruTagStringLabel = elasticSearchProps.getTaggingGuideRUTagLabel();

    Map<String, TranscriptionDocumentDetail> documentsMap = new HashMap<>();

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .terms(datasetIdLabel)
            .field(datasetIdLabel)
            .size(Integer.MAX_VALUE)
            .subAggregation(
                    AggregationBuilders
                            .terms(hashLabel)
                            .field(hashLabel)
                            .size(Integer.MAX_VALUE)
                            .subAggregation(
                                    AggregationBuilders
                                            .topHits("top")
                                            .fetchSource(
                                                    new String[]{hashLabel, textLabel, tokensLabel, projectIdLabel,
                                                            autoTagCountLabel,
                                                            autoTagStringLabel, datasetIdLabel,
                                                            intentLabel, ruTagStringLabel}, null)
                                            .size(1)));

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregrationBuilder,
                    " get original transcriptions ");

    if (searchResponse != null) {
      Terms datasetTermBuckets = searchResponse.getAggregations().get(datasetIdLabel);
      for (Terms.Bucket datasetBucket : datasetTermBuckets.getBuckets()) {
        Terms hashTermBuckets = datasetBucket.getAggregations().get(hashLabel);
        log.info("Total transcription hash buckets: {}", hashTermBuckets.getBuckets().size());
        for (Terms.Bucket hashBucket : hashTermBuckets.getBuckets()) {
          String thisHash = hashBucket.getKeyAsString();
          long docCount = hashBucket.getDocCount();
          TopHits topHits = hashBucket.getAggregations().get("top");
          for (SearchHit hit : topHits.getHits().getHits()) {
            updateOriginalDoc(hit, thisHash, docCount, documentsMap);
          }
        }
      }
    }
    return documentsMap;
  }

  private void updateOriginalDoc(SearchHit hit, String thisHash,
                                 long docCount, Map<String, TranscriptionDocumentDetail> documentsMap) {

    String intentLabel = elasticSearchProps.getTagLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String tokensLabel = elasticSearchProps.getNumTokensLabel();
    String textLabel = elasticSearchProps.getTranscriptionLabel();
    String autoTagCountLabel = elasticSearchProps.getAutoTagCountLabel();
    String autoTagStringLabel = elasticSearchProps.getAutoTagStringLabel();
    String ruTagStringLabel = elasticSearchProps.getTaggingGuideRUTagLabel();

    Map<String, Object> sourceMap = hit.getSourceAsMap();
    if (sourceMap != null) {
      String textStringForTagging = (String) sourceMap.get(textLabel);
      int numTokens = 0;
      int numAutoTags = 0;
      try {
        Object numTokensObject = sourceMap.get(tokensLabel);
        if (numTokensObject != null) {
          numTokens = (Integer) numTokensObject;
        } else {
          numTokens = textStringForTagging != null ? textStringForTagging.split(" ").length : 0;
        }
        numAutoTags = (Integer) sourceMap.get(autoTagCountLabel);
      } catch (Exception cce) {
        log.warn("Invalid numTokens or numAutoTags, ignored");
      }
      TranscriptionDocumentDetail document = documentsMap.get(thisHash);
      if (document == null) {
        document = new TranscriptionDocumentDetail();
        document.setDocumentCount(docCount);
        document.setTranscriptionHash(thisHash);
        document.setTextStringForTagging((String) sourceMap.get(textLabel));
        document.setNumTokens(numTokens);
        document.setAutoTagCount(numAutoTags);
        document.setAutoTagString((String) sourceMap.get(autoTagStringLabel));
        document.setRutag((String) sourceMap.get(ruTagStringLabel));
        document.setIntent((String) sourceMap.get(intentLabel));
      } else {
        document.setDocumentCount(docCount + document.getDocumentCount());
      }
      document.getDatasetIds()
              .add(Integer.valueOf(String.valueOf(sourceMap.get(datasetIdLabel))));
      documentsMap.put(thisHash, document);
    }
  }

  public void joinOrigAndIntentDocs(Map<String, TranscriptionDocumentDetail> origDocs,
                                    Map<String, TranscriptionDocumentDetail> intentDocs,
                                    Map<String, TranscriptionDocumentDetail> finalDocs) {

    for (Map.Entry<String, TranscriptionDocumentDetail> entry : origDocs.entrySet()) {
      String hash = entry.getKey();
      TranscriptionDocumentDetail origDoc = entry.getValue();
      TranscriptionDocumentDetail intentDoc = intentDocs.get(hash);
      if (intentDoc != null) {
        origDoc.setIntent(intentDoc.getIntent());
        origDoc.setRutag(intentDoc.getRutag());
        origDoc.setClassificationId(intentDoc.getClassificationId());
        origDoc.setTaggedBy(intentDoc.getTaggedBy());
        origDoc.setTaggedAt(intentDoc.getTaggedAt());
        origDoc.setComment(intentDoc.getComment());
        origDoc.setCommentedBy(intentDoc.getCommentedBy());
        origDoc.setCommentedAt(intentDoc.getCommentedAt());
        origDoc.getIntents().putAll(intentDoc.getIntents());
        origDoc.setDatasetSource(intentDoc.getDatasetSource());

        if (intentDoc.getRutag() != null && !intentDoc.getRutag().isEmpty()) {
          origDoc.setRutag(intentDoc.getRutag());
        } else if (origDoc.getRutag() == null) {
          origDoc.setRutag("");
        }
        finalDocs.put(hash, origDoc);
      }
    }
  }

  public void joinOrigAndCommentDocs(Map<String, TranscriptionDocumentDetail> origDocs,
                                     Map<String, TranscriptionDocumentDetail> commentDocs,
                                     Map<String, TranscriptionDocumentDetail> finalDocs) {

    for (Map.Entry<String, TranscriptionDocumentDetail> entry : origDocs.entrySet()) {
      String hash = entry.getKey();
      TranscriptionDocumentDetail origDoc = entry.getValue();
      TranscriptionDocumentDetail commentDoc = commentDocs.get(hash);
      if (commentDoc != null) {
        origDoc.setComment(commentDoc.getComment());
        origDoc.setCommentedBy(commentDoc.getCommentedBy());
        origDoc.setCommentedAt(commentDoc.getCommentedAt());
      }
      finalDocs.put(hash, origDoc);
    }
  }

  public Map<String, TranscriptionDocumentDetail> getTranscriptionHashAggregation(
          String queryTxt, Query query, TFSQueryOperator manualTagToOrigDocOperator, String queryOperator,
          String projectId, List<String> datasetIds, SearchRequestFilterWordCountRange wordCountRange,
          SearchRequestFilterAutoTagCountRange autoTagCountRange,
          Map<String, Map<String, TranscriptionDocumentDetail>> intentAwareDocumentsMap) {

    Map<String, TranscriptionDocumentDetail> documentsMap = new HashMap<>();
    Map<String, TranscriptionDocumentDetail> differingOrigDocsMap = new HashMap<>();
    Map<String, TranscriptionDocumentDetail> intersectingIntentAwareDocs = intentAwareDocumentsMap
            .get(INTERSECTING_LABEL);
    Map<String, TranscriptionDocumentDetail> differingIntentAwareDocs = intentAwareDocumentsMap
            .get("differing");

    BoolQueryBuilder intersectingBuilder = this
            .buildTranscriptionHashAggregationQuery(queryTxt, query, projectId,
                    datasetIds, queryOperator, wordCountRange, autoTagCountRange, false);

    Map<String, TranscriptionDocumentDetail> intersectingOrigDocsMap = new HashMap<>(this.getOriginalDocuments(intersectingBuilder));

    this.joinOrigAndIntentDocs(intersectingOrigDocsMap, intersectingIntentAwareDocs, documentsMap);

    if (query != null && !"".equals(query.toString()) && TFSQueryOperator.OR.equals(manualTagToOrigDocOperator)
            && differingIntentAwareDocs != null) {

      BoolQueryBuilder differingBuilder = this
              .buildTranscriptionHashAggregationQuery(queryTxt, query, projectId,
                      datasetIds, queryOperator, wordCountRange, autoTagCountRange, true);

      differingOrigDocsMap.putAll(this.getOriginalDocuments(differingBuilder));

      this.joinOrigAndIntentDocs(differingOrigDocsMap, intersectingIntentAwareDocs, documentsMap);
      this.joinOrigAndIntentDocs(intersectingOrigDocsMap, differingIntentAwareDocs, documentsMap);
    }

    return documentsMap;
  }

  public Map<BoolQueryCondition, QueryBuilder> booleanQueryBuilderOnIntentLabel(
          List<ManualTagClause> manualTagClauseList) {

    String granularTagLabel = elasticSearchProps.getGranularIntentLabel();

    QueryBuilder queryBuilder;
    EnumMap<BoolQueryCondition, QueryBuilder> map = new EnumMap<>(BoolQueryCondition.class);

    if (manualTagClauseList != null && !manualTagClauseList.isEmpty()
            && !QueryParserUtil.isTermsOnlyQuery(manualTagClauseList)) {
      BoolQueryCondition condition = BoolQueryCondition.MUST;
      if (manualTagClauseList.size() == 1) {
        ManualTagClause manualTagClause = manualTagClauseList.get(0);
        Query query = manualTagClause.getQueryType();
        String queryText = manualTagClause.getText();
        if (manualTagClause.isProhibited()) {
          condition = BoolQueryCondition.MUST_NOT;
        }
        if (query instanceof PrefixQuery) {
          queryBuilder = QueryBuilders.prefixQuery(granularTagLabel,
                  org.apache.commons.lang.StringUtils.strip(queryText, "*"));
        } else if (query instanceof WildcardQuery) {
          queryBuilder = QueryBuilders.wildcardQuery(granularTagLabel, queryText);
        } else if (query instanceof PhraseQuery) {
          queryBuilder = QueryBuilders.matchQuery(granularTagLabel, queryText);
        } else {
          throw new InvalidRequestException(
                  new Error(Response.Status.BAD_REQUEST.getStatusCode(), UNKNOWN_SEARCH_QUERY_LABEL,
                          UNKNOWN_QUERY_TYPE_LABEL));
        }
      } else {
        String queryText = QueryParserUtil.buildQuery(manualTagClauseList);
        queryBuilder = QueryBuilders.queryStringQuery(queryText)
                .defaultField(granularTagLabel)
                .allowLeadingWildcard(false)
                .analyzeWildcard(true);
      }

      map.put(condition, queryBuilder);
    }
    return map;
  }

  public Map<BoolQueryCondition, QueryBuilder> booleanQueryBuilderOnRuTagLabel(
          List<RuTagClause> ruTagClauseList) {

    String classificationDataLabelLabel = elasticSearchProps.getClassificationDataLabel();

    QueryBuilder queryBuilder;
    EnumMap<BoolQueryCondition, QueryBuilder> map = new EnumMap<>(BoolQueryCondition.class);

    if (ruTagClauseList != null && !ruTagClauseList.isEmpty()
            && !QueryParserUtil.isTermsOnlyQuery(ruTagClauseList)) {
      BoolQueryCondition condition = BoolQueryCondition.MUST;
      if (ruTagClauseList.size() == 1) {
        RuTagClause ruTagClause = ruTagClauseList.get(0);
        Query query = ruTagClause.getQueryType();
        String queryText = ruTagClause.getText();
        if (ruTagClause.isProhibited()) {
          condition = BoolQueryCondition.MUST_NOT;
        }
        if (query instanceof PrefixQuery) {
          queryBuilder = QueryBuilders.prefixQuery(classificationDataLabelLabel,
                  org.apache.commons.lang.StringUtils.strip(queryText, "*"));
        } else if (query instanceof WildcardQuery) {
          queryBuilder = QueryBuilders.wildcardQuery(classificationDataLabelLabel, queryText);
        } else if (query instanceof PhraseQuery) {
          queryBuilder = QueryBuilders.matchQuery(classificationDataLabelLabel, queryText);
        } else {
          throw new InvalidRequestException(
                  new Error(Response.Status.BAD_REQUEST.getStatusCode(), UNKNOWN_SEARCH_QUERY_LABEL,
                          UNKNOWN_QUERY_TYPE_LABEL));
        }
      } else {
        String queryText = QueryParserUtil.buildQuery(ruTagClauseList);
        queryBuilder = QueryBuilders.queryStringQuery(queryText)
                .defaultField(classificationDataLabelLabel)
                .allowLeadingWildcard(false)
                .analyzeWildcard(true);
      }

      map.put(condition, queryBuilder);
    }
    return map;
  }


  public Map<BoolQueryCondition, QueryBuilder> buildIntentAwareSpecificQuery(String tagLabel,
                                                                             List<ManualTagClause> manualTagClauseList, List<RuTagClause> rutagClauseList,
                                                                             Map<String, String> intentClassificationIdList, boolean ifOrIncludeQuery) {

    EnumMap<BoolQueryCondition, QueryBuilder> map = new EnumMap<>(BoolQueryCondition.class);

    List<String> intentInclusionList = new ArrayList<>();
    List<String> intentExclusionList = new ArrayList<>();

    if (manualTagClauseList != null && !manualTagClauseList.isEmpty()) {
      for (ManualTagClause clause : manualTagClauseList) {
        if (clause.isRequired()) {
          intentInclusionList = createClassificationIdList(intentClassificationIdList);
        } else if (clause.isProhibited()) {
          intentExclusionList = createClassificationIdList(intentClassificationIdList);
        } else if (ifOrIncludeQuery) {
          intentInclusionList = createClassificationIdList(intentClassificationIdList);
        } else {
          intentExclusionList = createClassificationIdList(intentClassificationIdList);
        }
      }
    }

    if (rutagClauseList != null && !rutagClauseList.isEmpty()) {
      for (RuTagClause ruTagClause : rutagClauseList) {
        if (ruTagClause.isRequired()) {
          intentInclusionList = createClassificationIdList(ruTagClause, intentClassificationIdList);
        } else if (ruTagClause.isProhibited()) {
          intentExclusionList = createClassificationIdList(ruTagClause, intentClassificationIdList);
        } else if (ifOrIncludeQuery) {
          intentInclusionList = createClassificationIdList(ruTagClause, intentClassificationIdList);
        } else {
          intentExclusionList = createClassificationIdList(ruTagClause, intentClassificationIdList);
        }
      }
    }

    if (!intentInclusionList.isEmpty()) {
      map.put(BoolQueryCondition.MUST, QueryBuilders.termsQuery(tagLabel, intentInclusionList));
    }
    if (!intentExclusionList.isEmpty()) {
      map.put(BoolQueryCondition.MUST_NOT, QueryBuilders.termsQuery(tagLabel, intentExclusionList));
    }

    return map;
  }

  private List<String> createClassificationIdList(
          Map<String, String> intentClassificationIdList) {

    final Set<String> matchingKeys = new HashSet<>();
    for (String key : intentClassificationIdList.keySet()) {
      matchingKeys.add(StringUtils.strip(key, "*").toLowerCase());
    }

    return intentClassificationIdList.entrySet()
            .stream()
            .filter(e -> matchingKeys.contains(e.getKey().toLowerCase()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
  }

  private List<String> createClassificationIdList(RuTagClause clause,
                                                  Map<String, String> intentClassificationIdList) {

    final List<String> matchingKeys = new ArrayList<>();
    for (String key : intentClassificationIdList.keySet()) {
      if (key.matches("^.*" + clause.getText().replace("*", ".*") + ".*$")) {
        matchingKeys.add(key.toLowerCase());
      }
    }

    return intentClassificationIdList.entrySet()
            .stream()
            .filter(e -> matchingKeys.contains(e.getKey().toLowerCase()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
  }

  public BoolQueryBuilder buildIntentAwareBasicQuery(String projectId, List<String> datasetIds,
                                                     boolean hasComment, Map<String, TranscriptionDocumentDetail> documentsMap) {

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    int maxClauseCount = elasticSearchProps.getMaxClauseCount();

    BoolQueryBuilder intentAddedDocumentsQueryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      intentAddedDocumentsQueryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    if (hasComment && (documentsMap.size() < maxClauseCount)) {
      intentAddedDocumentsQueryBuilder
              .filter(QueryBuilders.termsQuery(hashLabel, documentsMap.keySet()));
    }

    intentAddedDocumentsQueryBuilder.must(QueryBuilders.existsQuery(classificationIdLabel));
    return intentAddedDocumentsQueryBuilder;
  }

  public BoolQueryBuilder buildIntentAwareBasicQueryForUntagged(String projectId, List<String> datasetIds,
                                                                boolean hasComment, Map<String, TranscriptionDocumentDetail> documentsMap) {

    int maxClauseCount = elasticSearchProps.getMaxClauseCount();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();

    BoolQueryBuilder intentAddedDocumentsQueryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      intentAddedDocumentsQueryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }
    if (hasComment && (documentsMap.size() < maxClauseCount)) {
      intentAddedDocumentsQueryBuilder
              .filter(QueryBuilders.termsQuery(hashLabel, documentsMap.keySet()));
    }
    return intentAddedDocumentsQueryBuilder;
  }

  public BoolQueryBuilder buildUntaggedIntentAwareDocumentsQuery(String projectId,
                                                                 List<String> datasetIds, boolean hasComment,
                                                                 Map<String, TranscriptionDocumentDetail> documentsMap) {
    String tagLabel = elasticSearchProps.getTagLabel();
    BoolQueryBuilder queryBuilder = this
            .buildIntentAwareUntaggedBasicQuery(
                    projectId,
                    datasetIds, hasComment, documentsMap);
    queryBuilder.mustNot(QueryBuilders.existsQuery(tagLabel));
    return queryBuilder;
  }

  public BoolQueryBuilder buildIntentAwareUntaggedBasicQuery(String projectId, List<String> datasetIds,
                                                             boolean hasComment, Map<String, TranscriptionDocumentDetail> documentsMap) {

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    int maxClauseCount = elasticSearchProps.getMaxClauseCount();

    BoolQueryBuilder intentAddedDocumentsQueryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      intentAddedDocumentsQueryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    if (hasComment && (documentsMap.size() < maxClauseCount)) {
      intentAddedDocumentsQueryBuilder
              .filter(QueryBuilders.termsQuery(hashLabel, documentsMap.keySet()));
    }
    intentAddedDocumentsQueryBuilder.mustNot(QueryBuilders.existsQuery(classificationIdLabel));
    return intentAddedDocumentsQueryBuilder;
  }

  public BoolQueryBuilder buildDefaultTaggedIntentAwareDocumentsQuery(String projectId,
                                                                      List<String> datasetIds, boolean hasComment,
                                                                      Map<String, TranscriptionDocumentDetail> documentsMap) {
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    BoolQueryBuilder queryBuilder = this
            .buildIntentAwareBasicQuery(projectId,
                    datasetIds, hasComment, documentsMap);
    queryBuilder.must(QueryBuilders.existsQuery(classificationIdLabel));
    return queryBuilder;
  }

  public BoolQueryBuilder buildIntentAwareDocumentsQuery(String projectId, List<String>
          datasetIds, boolean tagged, boolean hasComment, Map<String, TranscriptionDocumentDetail> documentsMap,
                                                         Map<BoolQueryCondition, QueryBuilder> conditionToQueryMap, boolean negate) {

    BoolQueryBuilder queryBuilder;
    if (tagged) {
      queryBuilder = this
              .buildDefaultTaggedIntentAwareDocumentsQuery(projectId, datasetIds, hasComment, documentsMap);
    } else {
      queryBuilder = this
              .buildIntentAwareBasicQuery(projectId, datasetIds, hasComment, documentsMap);
    }

    for (Map.Entry<BoolQueryCondition, QueryBuilder> entry : conditionToQueryMap.entrySet()) {
      BoolQueryCondition thisCondition = entry.getKey();
      QueryBuilder subQueryBuilder = entry.getValue();

      if (BoolQueryCondition.MUST.equals(thisCondition)) {
        if (negate) {
          queryBuilder.mustNot(subQueryBuilder);
        } else {
          queryBuilder.must(subQueryBuilder);
        }
      } else if (BoolQueryCondition.MUST_NOT.equals(thisCondition)) {
        if (negate) {
          queryBuilder.must(subQueryBuilder);
        } else {
          queryBuilder.mustNot(subQueryBuilder);
        }
      }
    }

    return queryBuilder;
  }

  public Map<String, TranscriptionDocumentDetail> getIntentAwareTranscriptionsByQuery(
          String clientId, String projectId, List<String> datasetIds,
          QueryBuilder queryBuilder, boolean hasComment,
          Map<String, TranscriptionDocumentDetail> documentsMap) {

    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();

    Map<String, TranscriptionDocumentDetail> finalDocumentsMap = new HashMap<>();

    AbstractAggregationBuilder aggregationBuilder = this
            .getIntentAwareAggreationBuilder(classificationIdLabel, hashLabel,
                    taggedAtLabel,
                    taggedByLabel, datasetIdLabel,
                    taggedAtLabel,
                    Constants.DECENDING);

    String functionality = " get intent aware transcriptions " +
            FOR_CLIENT_LABEL + clientId +
            PROJECT_LABEL;
    SearchResponse transcriptionQueryResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, Integer.MAX_VALUE, new String[]{},
                    aggregationBuilder,
                    functionality);

    Map<String, TaggingGuideDocumentDetail> classifications = this.getClassificationDocumentsForProject(clientId, projectId);
    Map<Integer, String> datasetSourceMap = datasetManager.getDatasetSourceMapByDatasetIds(datasetIds);
    if (transcriptionQueryResponse != null) {
      Terms datasetTermBuckets = transcriptionQueryResponse.getAggregations().get(datasetIdLabel);
      for (Terms.Bucket datasetBucket : datasetTermBuckets.getBuckets()) {
        Terms agg = datasetBucket.getAggregations().get(hashLabel);
        for (Terms.Bucket hashBucket : agg.getBuckets()) {
          String thisHash = hashBucket.getKeyAsString();
          TopHits topHits = hashBucket.getAggregations().get("top");
          for (SearchHit hit : topHits.getHits().getHits()) {
            updateIntentTrascription(hit, thisHash, classifications, datasetSourceMap, finalDocumentsMap);
          }
        }
      }
    }

    if (hasComment) {
      finalDocumentsMap.keySet().retainAll(documentsMap.keySet());
    }

    return finalDocumentsMap;
  }

  private void updateIntentTrascription(SearchHit hit, String thisHash, Map<String, TaggingGuideDocumentDetail> classifications,
                                        Map<Integer, String> datasetSourceMap, Map<String, TranscriptionDocumentDetail> finalDocumentsMap) {

    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();

    TranscriptionDocumentDetail newDoc = new TranscriptionDocumentDetail();
    Map<String, Object> source = hit.getSourceAsMap();
    String datasetIdStr = source.get(datasetIdLabel).toString();
    Integer datasetId = Integer.valueOf(datasetIdStr);
    String datasetSource = datasetManager.getDatasetById(datasetIdStr) != null ?
            datasetManager.getDatasetById(datasetIdStr).getSource() : null;
    if(datasetSource == null) {
      datasetSource = DatasetBO.Source.I.getValue();
    }
    String classificationId = (String) source.get(classificationIdLabel);
    TaggingGuideDocumentDetail classification = null;

    if (classificationId != null) {
      classification = classifications.get(classificationId);
    }
    if (classification != null) {
      newDoc.setIntent(classification.getIntent());
      newDoc.setTaggedAt((String) source.get(taggedAtLabel));
      newDoc.setTaggedBy((String) source.get(taggedByLabel));
      newDoc.getIntents().put(datasetId, classification.getIntent());
      Set<Integer> datasets = new HashSet<>();
      datasets.add(datasetId);
      newDoc.setDatasetIds(datasets);
      newDoc.setDatasetSource(datasetSource);
      newDoc.setRutag(classification.getRutag());
      newDoc.setClassificationId(classificationId);
    } else {
      newDoc.getIntents().put(datasetId, null);
    }
    if(finalDocumentsMap.containsKey(thisHash)) {
      TranscriptionDocumentDetail oldDoc = finalDocumentsMap.get(thisHash);
      if(!oldDoc.getDatasetIds().isEmpty() && !newDoc.getDatasetIds().isEmpty()) {
        Integer oldDatasetId1 = oldDoc.getDatasetIds().iterator().next();
        newDoc = CSVFileUtil.resolveConflict(oldDoc, newDoc, datasetSourceMap.get(oldDatasetId1),
                datasetSourceMap.get(datasetId));
      }
    }
    finalDocumentsMap.put(thisHash, newDoc);
  }

  public Map<String, Map<String, TranscriptionDocumentDetail>> getIntentAwareTranscriptions(
          QueryAggregator queryAggregator, boolean tagged, boolean untagged, boolean hasComment, String clientId,
          String projectId, List<String> datasetIds, Map<String, TranscriptionDocumentDetail> documentsMap) {

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();

    BoolQueryBuilder intersectingDocsQueryBuilder;
    BoolQueryBuilder differingDocsQueryBuilder = null;
    Map<String, Map<String, TranscriptionDocumentDetail>> finalDocumentsMap = new HashMap<>();

    if (!tagged && untagged) { // Untagged case
      intersectingDocsQueryBuilder = this.buildUntaggedIntentAwareDocumentsQuery(projectId, datasetIds,
              hasComment, documentsMap);

    } else if (!tagged || !untagged) { // Untagged and All text strings case
      String textStringQuery = queryAggregator.getTextStringForTaggingQuery();
      List<ManualTagClause> manualTagClauseList = queryAggregator.getManualTagClauseList();

      List<RuTagClause> ruTagClauseList = queryAggregator.getRuTagClauseList();
      TFSQueryOperator manualToOrigDocOperator = queryAggregator.getManualTagToOriginalDocumentsOperator();
      TFSQueryOperator ruTagToOrigDocOperator = queryAggregator.getRuTagTagToOriginalDocumentsOperator();

      if ((manualTagClauseList == null || manualTagClauseList.isEmpty())
              && (ruTagClauseList == null || ruTagClauseList.isEmpty())) {

        if (tagged) {
          intersectingDocsQueryBuilder = this.buildDefaultTaggedIntentAwareDocumentsQuery(projectId, datasetIds,
                  hasComment, documentsMap);
        } else {
          intersectingDocsQueryBuilder = this.buildIntentAwareBasicQueryForUntagged(projectId, datasetIds,
                  hasComment, documentsMap);
        }
      } else {
        Map<BoolQueryCondition, QueryBuilder> tagLabelBooleanMap = booleanQueryBuilderOnIntentLabel(
                manualTagClauseList);
        Map<BoolQueryCondition, QueryBuilder> ruTagBooleanMap = booleanQueryBuilderOnRuTagLabel(
                ruTagClauseList);

        boolean tagNegate = false;
        boolean ruTagNegate = false;
        boolean ifOrQuery = false;

        if (textStringQuery != null && !"".equals(textStringQuery) && TFSQueryOperator.OR
                .equals(manualToOrigDocOperator)) {
          tagNegate = true;
        }

        if (textStringQuery != null && !"".equals(textStringQuery) && TFSQueryOperator.OR
                .equals(ruTagToOrigDocOperator)) {
          ruTagNegate = true;
        }

        if (tagNegate || ruTagNegate) {
          ifOrQuery = true;
        }

        Map<String, String> classificationTagList = this.loadIntentClassificationIds(clientId, projectId, manualTagClauseList,
                ruTagClauseList, tagLabelBooleanMap, ruTagBooleanMap, false, false);

        if (!ifOrQuery && ((manualTagClauseList != null && !manualTagClauseList.isEmpty())
                || (ruTagClauseList != null && !ruTagClauseList.isEmpty()))
                && (classificationTagList == null || classificationTagList.isEmpty())) {
          return finalDocumentsMap;
        }

        Map<BoolQueryCondition, QueryBuilder> queryMap = this.buildIntentAwareSpecificQuery(classificationIdLabel,
                manualTagClauseList, ruTagClauseList, classificationTagList, ifOrQuery);

        intersectingDocsQueryBuilder = this.buildIntentAwareDocumentsQuery(projectId, datasetIds, tagged, hasComment,
                documentsMap, queryMap, false);

        if (ifOrQuery) {
          classificationTagList = this.loadIntentClassificationIds(clientId, projectId, manualTagClauseList,
                  ruTagClauseList, tagLabelBooleanMap, ruTagBooleanMap, tagNegate, ruTagNegate);

          queryMap = this.buildIntentAwareSpecificQuery(classificationIdLabel, manualTagClauseList,
                  ruTagClauseList, classificationTagList, false);

          differingDocsQueryBuilder = this.buildIntentAwareDocumentsQuery(projectId, datasetIds, tagged, hasComment,
                  documentsMap, queryMap, true);
        }
      }
    } else { // tagged and untagged cannot happen together
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), INVALID_SEARCH_QUERY,
                      "Invalid request. There can be no transcription that is both tagged and untagged"));
    }
    if (intersectingDocsQueryBuilder != null) {
      Map<String, TranscriptionDocumentDetail> intersectingDocumentMap = new HashMap<>(this.getIntentAwareTranscriptionsByQuery(clientId, projectId,
              datasetIds, intersectingDocsQueryBuilder, hasComment, documentsMap));
      finalDocumentsMap.put(INTERSECTING_LABEL, intersectingDocumentMap);
    }

    if (differingDocsQueryBuilder != null) {
      Map<String, TranscriptionDocumentDetail> differingDocumentMap = new HashMap<>(this.getIntentAwareTranscriptionsByQuery(clientId, projectId,
              datasetIds, differingDocsQueryBuilder, hasComment, documentsMap));
      finalDocumentsMap.put("differing", differingDocumentMap);
    }
    return finalDocumentsMap;
  }

  public Map<String, TranscriptionDocumentDetail> getCommentAwareTranscriptions(String projectId, List<String> datasetIds,
                                                                                String commentQuery, Operator defaultOperator) {

    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String commentLabel = elasticSearchProps.getCommentLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String commentedByLabel = elasticSearchProps.getCommentedByLabel();

    final Map<String, TranscriptionDocumentDetail> commentAwareDocumentsMap = new HashMap<>();

    if (commentQuery == null || "".equals(commentQuery)) {
      commentQuery = "*";
    }

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.COMMENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId)).must(
                    QueryBuilders.queryStringQuery(commentQuery).defaultField(commentLabel)
                            .analyzeWildcard(true).defaultOperator(defaultOperator));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    StringBuilder functionality = new StringBuilder(" get comment aware transcriptions ")
            .append(PROJECT_LABEL).append(projectId)
            .append(DATASET_ID_LABEL).append(datasetIds);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, elasticSearchProps.getSearchPageSize(),
                    new String[]{hashLabel, commentLabel, commentedAtLabel,
                            commentedByLabel, datasetIdLabel}, functionality.toString());
    long scrollStartTime = 0;
    long scrollEndTime = 0;
    while (searchResponse.getHits().getHits().length > 0) {

      for (SearchHit hit : searchResponse.getHits().getHits()) {
        TranscriptionDocumentDetail doc = new TranscriptionDocumentDetail();
        String hash = hit.getSourceAsMap().get(hashLabel).toString();
        doc.setTranscriptionHash(hash);
        commentAwareDocumentsMap.put(hash, this.updateCommentDoc(doc, hit));
      }

      scrollStartTime = System.currentTimeMillis();

      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(elasticSearchProps.getSearchActionGetTimeout()))
              .execute()
              .actionGet();

      scrollEndTime = System.currentTimeMillis();

      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }
    return commentAwareDocumentsMap;
  }

  private TranscriptionDocumentDetail updateCommentDoc(TranscriptionDocumentDetail doc, SearchHit hit) {

    String commentLabel = elasticSearchProps.getCommentLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String commentedByLabel = elasticSearchProps.getCommentedByLabel();

    Map<String, Object> hitSourceMap = hit.getSourceAsMap();
    if (hitSourceMap.containsKey(commentLabel)) {
      doc.setComment(
              hitSourceMap.get(commentLabel) != null ? hitSourceMap
                      .get(commentLabel).toString() : null);
    }
    if (hitSourceMap.containsKey(commentedAtLabel)) {
      doc.setCommentedAt(
              hitSourceMap.get(commentedAtLabel) != null ? hitSourceMap
                      .get(commentedAtLabel).toString() : null);
    }
    if (hitSourceMap.containsKey(commentedByLabel)) {
      doc.setCommentedBy(
              hitSourceMap.get(commentedByLabel) != null ? hitSourceMap
                      .get(commentedByLabel).toString() : null);
    }
    return doc;
  }

  /**
   * Search on transcription hashes modifying the documents to add comment field values
   */
  private void getTranscriptionComments(String projectId,
                                        List<String> datasetIds, Map<String,
          TranscriptionDocumentDetail> documents) {

    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String commentLabel = elasticSearchProps.getCommentLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String commentedByLabel = elasticSearchProps.getCommentedByLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.COMMENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .must(QueryBuilders.existsQuery(commentLabel));

    if (datasetIds != null && !datasetIds.isEmpty()) {
      queryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    StringBuilder functionality = new StringBuilder(" get all transcription comment documents ")
            .append(PROJECT_LABEL)
            .append(projectId)
            .append(DATASET_ID_LABEL)
            .append(datasetIds);

    SearchResponse searchResponse = searchHelper
            .executeScrollRequest(queryBuilder, 0, elasticSearchProps.getSearchPageSize(),
                    new String[]{transcriptionHashLabel, commentLabel, commentedAtLabel,
                            commentedByLabel, datasetIdLabel}, functionality.toString());

    long scrollStartTime = 0;
    long scrollEndTime = 0;

    while (searchResponse.getHits().getHits().length > 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        String hash = hit.getSourceAsMap().get(transcriptionHashLabel).toString();
        TranscriptionDocumentDetail doc = documents.get(hash);
        if (doc != null) {
          this.updateCommentDoc(doc, hit);
        }
      }
      scrollStartTime = System.currentTimeMillis();
      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(elasticSearchProps.getSearchActionGetTimeout()))
              .execute()
              .actionGet();
      scrollEndTime = System.currentTimeMillis();

      logTimeForPrepareScrolls(scrollStartTime, scrollEndTime, functionality.toString(),
              queryBuilder.toString());
    }
  }

  private void logTimeForPrepareScrolls(long scrollStartTime, long scrollEndTime,
                                        String functionality, String queryBuilder) {

    long logTime = scrollEndTime - scrollStartTime;
    String logString = "  Scroll Refresh Request for the functionality to " +
            functionality + " with Elastic Search query " + queryBuilder +
            " and aggragation query " + null +
            " took {} milliseconds.";
    log.debug(logString, logTime);
  }
}