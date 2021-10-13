/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.DocumentType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.TaggedTranscriptionObject;
import com.tfs.learningsystems.ui.model.TranscriptionDocument;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.util.Constants;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class SearchHelper {

  @Autowired
  @Qualifier("jsonObjectMapper")
  private ObjectMapper jsonObjectMapper;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;

  /**
   * @param indexName
   * @param indexType
   * @param object
   * @return
   * @throws IOException
   */
  public IndexRequest buildIndexRequest(String indexName, String indexType, Object object)
      throws IOException {
    Map<String, Object> objectAsMap = new HashMap();
    try {
      BeanInfo info = Introspector.getBeanInfo(object.getClass());
      for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
        Method reader = pd.getReadMethod();
        if (reader != null) {
          objectAsMap.put(pd.getName(), reader.invoke(object));
        }
      }
    } catch (Exception e) {
      log.error("Failed to prepare the object map for index request -- " + jsonObjectMapper
          .writeValueAsString(object), e);
    }

    //IndexRequest indexRequest = new IndexRequest(indexName, indexType).source(jsonObjectMapper.writeValueAsBytes(object));
    IndexRequest indexRequest = new IndexRequest(indexName, indexType).source(objectAsMap);
    return indexRequest;
  }

  /**
   * @return
   */
  public BulkProcessor buildBulkProcessor() {

    BulkProcessor bulkProcessor = BulkProcessor
        .builder(elasticSearchClient, new BulkProcessor.Listener() {
          @Override
          public void beforeBulk(long executionId, BulkRequest request) {

            log.info("Executing total actions: {}", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

            if (response.hasFailures()) {
              log.error("Bulk response error: {}", response.buildFailureMessage());
            }
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

            log.error("Bulk operation failed with error", failure);
          }
        }).setBulkActions(elasticSearchProps.getTotalBulkProcessingActions())
        .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
        .setFlushInterval(
            TimeValue.timeValueSeconds(elasticSearchProps.getBulkProcessingFlushInterval()))
        .setConcurrentRequests(elasticSearchProps.getBulkProcessingConcurrentRequests())
        .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue
                    .timeValueMillis(elasticSearchProps.getBulkProcessingBackoffInitialDelay()),
                elasticSearchProps.getBulkProcessingBackoffTotalRetries())).build();

    return bulkProcessor;
  }

  /**
   * @param bulkRequests
   * @throws ApplicationException
   */
  public void executeBulkRequests(List<ActionRequest> bulkRequests, String functionality)
      throws ApplicationException {

    long scrollStartTime = System.currentTimeMillis();

    BulkProcessor bulkProcessor = this.buildBulkProcessor();
    if (bulkRequests != null && !bulkRequests.isEmpty()) {
      for (ActionRequest request : bulkRequests) {
        if (IndexRequest.class.isAssignableFrom(request.getClass())) {
          bulkProcessor.add((IndexRequest) request);
        } else if (DeleteRequest.class.isAssignableFrom(request.getClass())) {
          bulkProcessor.add((DeleteRequest) request);
        } else if (UpdateRequest.class.isAssignableFrom(request.getClass())) {
          bulkProcessor.add((UpdateRequest) request);
        } else {
          bulkProcessor.add((DocWriteRequest) request);
        }
      }
    }

    try {
      bulkProcessor
          .awaitClose(elasticSearchProps.getBulkProcessingAwaitCloseTime(), TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      String message = String
          .format("Interrupted while waiting for bulk processing to finish: %s", e.getMessage());
      log.error(message);
      throw new ApplicationException(message, e);
    }

    long scrollEndTime = System.currentTimeMillis();

    StringBuilder logStmt = new StringBuilder("  Execution of bulk request for functionality  ")
        .append(functionality).append(" took {}  milliseconds ");

    long timeTaken = (scrollEndTime - scrollStartTime);

    log.debug(logStmt.toString(), timeTaken);

  }

  /**
   * @param queryBuilder
   * @param startIndex
   * @param limit
   * @param fields
   * @return
   */
  public SearchResponse executeScrollRequest(QueryBuilder queryBuilder, int startIndex, int limit,
      String[] fields, String functionality) {

    return this.executeScrollRequest(queryBuilder, startIndex, limit, fields, null, functionality);
  }

  /**
   * @param queryBuilder
   * @param startIndex
   * @param limit
   * @param fields
   * @param aggregrationBuilder
   * @return
   */
  public SearchResponse executeScrollRequest(QueryBuilder queryBuilder, int startIndex, int limit,
      String[] fields,
      AbstractAggregationBuilder aggregrationBuilder, String functionality) {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();

    return this.executeScrollRequest(indexName, indexType, queryBuilder, startIndex, limit, fields,
        aggregrationBuilder, functionality);
  }

  /**
   * @param indexName
   * @param indexType
   * @param queryBuilder
   * @param startIndex
   * @param limit
   * @param fields
   * @param aggregrationBuilder
   * @return
   */
  public SearchResponse executeScrollRequest(String indexName, String indexType,
      QueryBuilder queryBuilder, int startIndex, int limit,
      String[] fields, AbstractAggregationBuilder aggregrationBuilder, String functionality) {

    int defaultPageSize = elasticSearchProps.getSearchPageSize();
    int scrollTimeValue = elasticSearchProps.getScrollTimeValue();
    int seachActionGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    int pageSize = limit <= defaultPageSize ? limit : defaultPageSize;

    SearchRequestBuilder searchRequestBuilder = elasticSearchClient.prepareSearch(indexName)
        .setFrom(startIndex)
        .setQuery(queryBuilder)
        .setScroll(new TimeValue(scrollTimeValue));

    if (pageSize > 0) {
      // with ES 6, when set to scroll, the size cannot be 0
      searchRequestBuilder = searchRequestBuilder.setSize(pageSize);
    }

    if (!StringUtils.isEmpty(indexType)) {
      searchRequestBuilder = searchRequestBuilder.setTypes(indexType);
    }

    if (fields != null) {
      if (fields.length == 0) {
        searchRequestBuilder.setFetchSource(false);
      } else {
        searchRequestBuilder.setFetchSource(fields, null);
      }
    }

    if (aggregrationBuilder != null) {
      searchRequestBuilder.addAggregation(aggregrationBuilder);
    }

    long scrollStartTime = System.currentTimeMillis();

    // hack for integration test cases .

    if (Thread.currentThread().getName().contains("scheduler")) {
      String threadName = Thread.currentThread().getName();
      threadName = "integration_test_thread_" + threadName.substring(threadName.length() - 2);
      Thread.currentThread().setName(threadName);
    }

    SearchResponse searchResponse = searchRequestBuilder.execute()
        .actionGet(seachActionGetTimeout, TimeUnit.MILLISECONDS);

    long scrollEndTime = System.currentTimeMillis();

    StringBuilder logStmt = new StringBuilder(" Scroll Request for the functionality to  ")
        .append(functionality).append(" with  Elastic Search query ")
        .append(queryBuilder).append(" and ").append(" aggragation query ")
        .append(aggregrationBuilder).append(" took {}  milliseconds ");

    long timeTaken = scrollEndTime - scrollStartTime;

    log.debug(logStmt.toString(), timeTaken);

    return searchResponse;
  }

  /**
   * @param indexName
   * @param indexType
   * @param jobId
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param documentType
   * @return
   */
  public long getTotalDocumentCount(String indexName, String indexType, String jobId, int clientId,
      int projectId, int datasetId, String
      documentType) {
    ArrayList<Integer> datasetIds = new ArrayList<>();
    if (datasetId != 0) {
      datasetIds.add(datasetId);
    }
    return getTotalDocumentCount(indexName, indexType, jobId, clientId, projectId, datasetIds,
        documentType);
  }


  /**
   * @param indexName
   * @param indexType
   * @param jobId
   * @param clientId
   * @param projectId
   * @param datasetIds
   * @param documentType
   * @return
   */
  public long getTotalDocumentCount(String indexName, String indexType, String jobId, int clientId,
      int projectId, ArrayList<Integer> datasetIds, String documentType) {

    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();

    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();

    String clientIdLabel = elasticSearchProps.getClientIdLabel();

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();

    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();

    String jobIdLabel = "jobId";

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .filter(QueryBuilders.termQuery(documentTypeLabel, documentType));

    if (!StringUtils.isEmpty(jobId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(jobIdLabel, jobId));
    }

    if (clientId != 0) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    if (projectId != 0) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }

    if (datasetIds != null && datasetIds.size() > 0) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));
    }

    FieldSortBuilder sortBuilder = SortBuilders.fieldSort(documentType.equalsIgnoreCase
        (DocumentType.ORIGINAL.type()) ? hashLabel : elasticSearchProps.getTaggedAtLabel())
        .order(SortOrder.DESC);

    AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.terms(datasetIdLabel)
        .field(datasetIdLabel).size(Integer.MAX_VALUE)
        .subAggregation(
            AggregationBuilders.terms(hashLabel).field(hashLabel).size(Integer.MAX_VALUE)
                .subAggregation(AggregationBuilders.topHits("top")
                    .sort(sortBuilder)
                    .fetchSource(
                        new String[]{classificationIdLabel, documentTypeLabel, datasetIdLabel},
                        null)
                    .size(1)));

    StringBuilder functionality = new StringBuilder(" get total document count ")
        .append(" from index ").append(indexName)
        .append(" for Client ").append(clientId).append(" project ").append(projectId)
        .append("and datasetId")
        .append(datasetIds).append(" for document type ").append(documentType);

    SearchResponse searchResponse = this
        .executeScrollRequest(indexName, indexType, queryBuilder, 0, 0, new String[]{},
            aggregationBuilder, functionality.toString());

    return searchResponse.getHits().getTotalHits();


  }

  /**
   * to get the number of  transcriptions for the dataset of the client project
   */
  public long getTotalNlToolsOriginalDocumentsCount(int clientId, int projectId, int datasetId) {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String documentType = DocumentType.ORIGINAL.type();

    return this.getTotalDocumentCount(indexName, indexType, null, clientId, projectId, datasetId,
        documentType);
  }

  /**
   * to get the number of  transcriptions for the dataset of the client project
   */
  public long getTotalNlToolsOriginalDocumentsCount(String clientId, String projectId,
      String datasetId) {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String documentType = DocumentType.ORIGINAL.type();

    Integer clientIdInt = new Integer(0);
    Integer projectIdInt = new Integer(0);
    Integer datasetIdInt = new Integer(0);
    if (!StringUtils.isEmpty(clientId)) {
      clientIdInt = Integer.parseInt(clientId);
    }
    if (!StringUtils.isEmpty(projectId)) {
      projectIdInt = Integer.parseInt(projectId);
    }

    if (!StringUtils.isEmpty(datasetId)) {
      datasetIdInt = Integer.parseInt(datasetId);
    }

    return this
        .getTotalDocumentCount(indexName, indexType, null, clientIdInt, projectIdInt, datasetIdInt,
            documentType);
  }


  /**
   * to get the number of  transcriptions for the dataset of the client project
   */
  public long getTotalNlToolsOriginalDocumentsCount(String clientId, String projectId,
      List<String> datasetIds) {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String documentType = DocumentType.ORIGINAL.type();

    Integer clientIdInt = new Integer(0);
    Integer projectIdInt = new Integer(0);
    ArrayList<Integer> datasetIdInts = new ArrayList();
    if (!StringUtils.isEmpty(clientId)) {
      clientIdInt = Integer.parseInt(clientId);
    }
    if (!StringUtils.isEmpty(projectId)) {
      projectIdInt = Integer.parseInt(projectId);
    }
    if (datasetIds != null && datasetIds.size() > 0) {
      for (String datasetId : datasetIds) {
        datasetIdInts.add(Integer.parseInt(datasetId));
      }
    }

    return this
        .getTotalDocumentCount(indexName, indexType, null, clientIdInt, projectIdInt, datasetIdInts,
            documentType);
  }

  /**
   * to get the number of  transcriptions for the dataset of the client project
   */
  public long getTotalNlToolsIntentAddedDocumentsCount(int clientId, int projectId, int datasetId) {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String documentType = DocumentType.INTENT_ADDED.type();

    return this.getTotalDocumentCount(indexName, indexType, null, clientId, projectId, datasetId,
        documentType);
  }

  /**
   * @param projectId
   * @param datasetIds
   * @param hashList
   * @return
   */
  public Map<String, List<TaggedTranscriptionObject>> getTaggedDocuments(int clientId,
      int projectId, List<Integer> datasetIds,
      Collection<String> hashList) {

    String classificationId = elasticSearchProps.getClassificationIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();

    String documentType = DocumentType.INTENT_ADDED.type();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .filter(QueryBuilders.termQuery(documentTypeLabel, documentType))
        .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
        .filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds))
        .filter(QueryBuilders.termsQuery(hashLabel, hashList));

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    queryBuilder = queryBuilder.must(QueryBuilders.existsQuery(classificationId));

    AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.terms(datasetIdLabel)
        .field(datasetIdLabel).size(Integer.MAX_VALUE).subAggregation(
            AggregationBuilders.terms(hashLabel).field(hashLabel).size(Integer.MAX_VALUE)
                .subAggregation(AggregationBuilders.topHits("top")
                    .fetchSource(new String[]{classificationId}, null).size(1)));

    StringBuilder functionality = new StringBuilder(" get tagged transcriptions ")
        .append(" for Client ").append(clientId).append(" project ").append(projectId)
        .append("and datasetId")
        .append(datasetIds).append(" for document type ").append(documentType);

    SearchResponse searchResponse = this
        .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregationBuilder,
            functionality.toString());

    Map<String, List<TaggedTranscriptionObject>> taggedTranscriptions = new HashMap<>();

    if (searchResponse != null) {
      Terms datasetTermBuckets = searchResponse.getAggregations().get(datasetIdLabel);
      for (Terms.Bucket datasetBucket : datasetTermBuckets.getBuckets()) {
        Terms hashTermBuckets = datasetBucket.getAggregations().get(hashLabel);
        for (Terms.Bucket hashBucket : hashTermBuckets.getBuckets()) {
          TaggedTranscriptionObject taggedBucket = new TaggedTranscriptionObject();
          String thisHash = hashBucket.getKeyAsString();
          long docCount = hashBucket.getDocCount();
          taggedBucket.setHash(thisHash);
          taggedBucket.setDocumentType(DocumentType.INTENT_ADDED.toString());
          taggedBucket.setDocCount(docCount);
          TopHits topHits = hashBucket.getAggregations().get("top");
          for (SearchHit hit : topHits.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            if (sourceMap != null && !sourceMap.isEmpty()) {
              if (sourceMap.containsKey(classificationId)) {
                String classificationIdinIndex = String.valueOf(sourceMap.get(classificationId));
                taggedBucket.setClassificationId(classificationIdinIndex);
              }
            }
          }
          List<TaggedTranscriptionObject> taggedBucketList = taggedTranscriptions.get(thisHash);
          if (taggedBucketList == null) {
            taggedBucketList = new ArrayList<TaggedTranscriptionObject>();
          }
          taggedBucketList.add(taggedBucket);
          taggedTranscriptions.put(thisHash, taggedBucketList);
        }
      }
    }
    log.debug("TranscriptionDocuments: {}", taggedTranscriptions);

    return taggedTranscriptions;
  }

  /**
   * @param projectId
   * @param datasetIds
   * @param hashList
   * @return
   */
  public Map<String, Map<String, List<TaggedTranscriptionObject>>> getTrascriptions(int clientId,
      int projectId, List<Integer> datasetIds, Collection<String> hashList) {

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String recordIdLabel = elasticSearchProps.getRecordIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();

    String originalDocumentType = DocumentType.ORIGINAL.type();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .mustNot(QueryBuilders.termQuery(documentTypeLabel, originalDocumentType))
        .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
        .filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds))
        .filter(QueryBuilders.termsQuery(hashLabel, hashList));

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.terms(datasetIdLabel)
        .field(datasetIdLabel).size(Integer.MAX_VALUE)
        .subAggregation(
            AggregationBuilders.terms(hashLabel).field(hashLabel).size(Integer.MAX_VALUE)
                .subAggregation(AggregationBuilders.topHits("top")
                    .sort(elasticSearchProps.getTaggedAtLabel(), SortOrder.DESC)
                    .fetchSource(new String[]{classificationIdLabel,
                        documentTypeLabel, datasetIdLabel, recordIdLabel}, null).size(1)));

    StringBuilder functionality = new StringBuilder(" get all transcriptions ")
        .append(" for Client ").append(clientId).append(" project ")
        .append(projectId).append(" datasetId ").append(datasetIds)
        .append(" for  hashList").append(hashList)
        .append(" excluding " + "document type ").append(originalDocumentType);

    SearchResponse searchResponse = this
        .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregationBuilder,
            functionality.toString());

    Map<String, Map<String, List<TaggedTranscriptionObject>>> allTranscriptions = new HashMap<>();

    if (searchResponse != null) {
      Terms datasetTermBuckets = searchResponse.getAggregations().get(datasetIdLabel);
      for (Terms.Bucket datasetBucket : datasetTermBuckets.getBuckets()) {
        Terms hashTermBuckets = datasetBucket.getAggregations().get(hashLabel);
        hashBucket:
        for (Terms.Bucket hashBucket : hashTermBuckets.getBuckets()) {
          TaggedTranscriptionObject taggedBucket = new TaggedTranscriptionObject();
          String thisHash = hashBucket.getKeyAsString();
          long docCount = hashBucket.getDocCount();
          taggedBucket.setHash(thisHash);
          taggedBucket.setDocCount(docCount);

          TopHits topHits = hashBucket.getAggregations().get("top");

          for (SearchHit hit : topHits.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            if (sourceMap != null && !sourceMap.isEmpty()) {
              if (sourceMap.containsKey(classificationIdLabel)) {

                String classificationIdStr = String.valueOf(sourceMap.get(classificationIdLabel));
                taggedBucket.setClassificationId(classificationIdStr);

              }
              if (sourceMap.containsKey(datasetIdLabel)) {
                taggedBucket.setDatasetId(String.valueOf(sourceMap.get(datasetIdLabel)));
              }
              if (sourceMap.containsKey(documentTypeLabel)) {
                taggedBucket.setDocumentType(String.valueOf(sourceMap.get(documentTypeLabel)));
              }
              if (sourceMap.containsKey(recordIdLabel)) {
                taggedBucket.setId(String.valueOf(sourceMap.get(recordIdLabel)));
              }
            }
            taggedBucket.setId(hit.getId());
          }

          Map<String, List<TaggedTranscriptionObject>> allHashCurrentDocs;

          String documentTypeLookUpKey = null;

          if (taggedBucket == null || taggedBucket.getDocumentType() == null) {

            log.warn("Document type not found");

            continue hashBucket;

          }

          if (taggedBucket.getDocumentType() != null && taggedBucket.getDocumentType()
              .equalsIgnoreCase(DocumentType.INTENT_ADDED.type())) {
            documentTypeLookUpKey = Constants.TAGGED_KEY;
          } else if (taggedBucket.getDocumentType() != null && taggedBucket.getDocumentType()
              .equalsIgnoreCase(DocumentType.INTENT_DELETED
                  .type())) {
            documentTypeLookUpKey = Constants.UNTAGGED_KEY;
          }

          allHashCurrentDocs = allTranscriptions.get(documentTypeLookUpKey);

          if (allHashCurrentDocs == null) {

            allHashCurrentDocs = new HashMap<String, List<TaggedTranscriptionObject>>();
          }
          List<TaggedTranscriptionObject> currentTranscriptions = allHashCurrentDocs.get(thisHash);

          if (currentTranscriptions == null) {
            currentTranscriptions = new ArrayList<TaggedTranscriptionObject>();
          }
          currentTranscriptions.add(taggedBucket);

          allHashCurrentDocs.put(thisHash, currentTranscriptions);

          allTranscriptions.put(documentTypeLookUpKey, allHashCurrentDocs);

        }
      }
    }
    log.debug("TranscriptionDocuments: {}", allTranscriptions);

    return allTranscriptions;
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetIds
   * @return
   */
  public Map<String, TranscriptionDocument> getTaggedDocumentsForProjectDataset(int clientId,
      int projectId, List<Integer> datasetIds) {

    Map<String, TranscriptionDocument> documentMap = new HashMap<>();
    String inheritingIntentField = elasticSearchProps.getImportedIntentLabel();

    if (inheritingIntentField == null || "".equals(inheritingIntentField)) {
      log.warn("Requested to inherit tags from client: {}, project: {}, dataset: {} " +
          "but the field to inherit from is not specified. Ignoring inheritance...");
      return documentMap;
    }

    String tagLabel = elasticSearchProps.getTagLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String hashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String inheritedIntentLabel = elasticSearchProps.getImportedIntentLabel();

    String originalDocumentType = DocumentType.ORIGINAL.type();
    String intentAddedDocumentType = DocumentType.INTENT_ADDED.type();

    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    String field = null;
    String documentType = null;
    if (inheritedIntentLabel.equals(inheritingIntentField)) {
      field = inheritedIntentLabel;
      documentType = originalDocumentType;
    } else if (tagLabel.equals(inheritingIntentField)) {
      field = tagLabel;
      documentType = intentAddedDocumentType;
    } else {
      throw new InvalidRequestException(
          new Error(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_search_query",
              "Invalid inheriting intent field"));
    }

    log.debug("Inheriting tags from the field:{} client: {}, project: {}, dataset: {}", field,
        clientId, projectId,
        String.join(",",
            datasetIds.stream().map(ID -> String.format("%s", ID)).collect(Collectors.toList())));

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .filter(QueryBuilders.termQuery(documentTypeLabel, documentType))
        .filter(QueryBuilders.termQuery(clientIdLabel, clientId))
        .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
        .filter(QueryBuilders.termsQuery(datasetIdLabel, datasetIds));

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    queryBuilder = queryBuilder.must(QueryBuilders.existsQuery(field));

    SearchResponse searchResponse = this
        .executeScrollRequest(queryBuilder, 0, 50, new String[]{field, hashLabel,}, null);

    if (searchResponse != null) {
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          TranscriptionDocument doc = new TranscriptionDocument();
          String hash = hit.getSourceAsMap().get(hashLabel).toString();
          doc.setIntent(hit.getSourceAsMap().get(field).toString());
          doc.setTranscriptionHash(hash);
          documentMap.put(hash, doc);
        }

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
            .setScroll(new TimeValue(searchGetTimeout))
            .execute().actionGet();
      } while (searchResponse.getHits().getHits().length != 0);
    }

    return documentMap;
  }

}
