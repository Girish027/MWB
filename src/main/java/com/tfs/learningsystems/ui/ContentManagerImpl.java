/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.helper.SearchHelper;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import com.tfs.learningsystems.ui.model.DocumentType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.Transcription;
import com.tfs.learningsystems.ui.model.TranscriptionCommentDocument;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.TranscriptionForAuditing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.search.taggingguide.model.ClassificationDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.ClassificationTransientDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.IDCreatorUtil;
import com.tfs.learningsystems.util.SecurityUtil;
import com.tfs.learningsystems.util.TextUtil;
import com.tfs.learningsystems.util.ErrorMessage;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Qualifier("contentManagerBean")
@Slf4j
public class ContentManagerImpl implements ContentManager {

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;
  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;
  @Autowired
  private SearchHelper elasticSearchHelper;
  @Autowired
  private AppConfig appConfig;
  @Autowired
  private JsonConverter jsonConverter;
  @Autowired
  @Qualifier("jsonObjectMapper")
  private ObjectMapper jsonObjectMapper;
  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;
  @Autowired
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;
  @Autowired
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;
  @Autowired
  private IDCreatorUtil idCreatorUtil;

  private static final String FOR_CLIENT = " for client ";
  private static final String PROJECT = " project ";
  private static final String COMMA_PROJECT = ", project ";
  private static final String COMMA_DATASET_ID = ", datasetIds ";
  private static final String DATASET_ID = "and datasetId";
  private static final String HASHES = "and hashes ";
  private static final String FOR_HASH_LIST = " for  hashList";
  private static final String FOR_TRANSCRIPTIONS = " for transcriptions ";
  private static final String CLIENT_ID = " client Id ";
  private static final String CLASSIFICATION_DOC = " get classification document ";

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param transcriptionHashList
   * @return
   */
  private QueryBuilder taggedDocumentsByTranscriptionHashListQuery(String clientId, String projectId,
                                                                   String datasetId,
                                                                   List<String> transcriptionHashList) {

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String tagLabel = elasticSearchProps.getTagLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    BoolQueryBuilder taggedDocumentBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders
                    .termsQuery(transcriptionHashLabel, transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId)).filter(
                    QueryBuilders.termQuery(documentTypeLabel,
                            DocumentType.INTENT_ADDED.type()))
            .must(QueryBuilders.existsQuery(tagLabel));

    if (!StringUtils.isEmpty(datasetId)) {
      taggedDocumentBuilder
              .filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));
    }
    if (!StringUtils.isEmpty(projectId)) {
      taggedDocumentBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }
    if (!StringUtils.isEmpty(clientId)) {
      taggedDocumentBuilder
              .filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    log.debug(" validateIntentAdditionBuilder in taggedDocumentsByTranscriptionHashListQuery--> "
            + taggedDocumentBuilder.toString());

    return taggedDocumentBuilder;
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param transcriptionHashList
   */
  private void validateIntentAddition(String clientId, String projectId, String datasetId,
                                      List<String> transcriptionHashList) {

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();

    QueryBuilder intentAdditionValidator = this
            .taggedDocumentsByTranscriptionHashListQuery(clientId, projectId, datasetId,
                    transcriptionHashList);

    long scrollStartTime = System.currentTimeMillis();

    SearchResponse response = elasticSearchClient
            .prepareSearch(documentIndexName)
            .setQuery(intentAdditionValidator)
            .setFetchSource(false)
            .get();

    StringBuilder functionality = new StringBuilder(" to validate intent addition  ")
            .append(FOR_CLIENT)
            .append(clientId)
            .append(PROJECT)
            .append(projectId)
            .append(DATASET_ID)
            .append(datasetId)
            .append(FOR_TRANSCRIPTIONS)
            .append(transcriptionHashList);

    long scrollEndTime = System.currentTimeMillis();

    logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
            intentAdditionValidator.toString(), null);

    if (response.getHits().getHits().length > 0) {
      String message = "The selected transcriptions are already tagged with an intent";
      throw new AlreadyExistsException(message);
    }
  }

  @Override
  public ClassificationDocument loadClassificationById(String clientId, String projectId,
                                                       String classificationRecordId) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String recordIdLabel = elasticSearchProps.getRecordIdLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (!StringUtils.isEmpty(classificationRecordId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(recordIdLabel, classificationRecordId));
    }
    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    String classificationId;

    String[] fields = {classificationIdLabel, granularIntentLabel, classificationDataLabel,
            recordIdLabel, projectIdLabel,
            clientIdLabel};

    StringBuilder functionality = new StringBuilder(CLASSIFICATION_DOC)
            .append(" for classificationRecordId ")
            .append(classificationRecordId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, null, queryBuilder, 0, 1, fields, null,
                    functionality.toString());

    long scrollStartTime = 0;
    long scrollEndTime = 0;
    ClassificationDocument classificationDocument = null;
    if (searchResponse != null) {
      classificationDocument = new ClassificationDocument();
      do {

        for (SearchHit hit : searchResponse.getHits().getHits()) {
          classificationId = hit.getSourceAsMap().get(classificationIdLabel).toString();
          classificationDocument.setClassificationId(classificationId);

          classificationDocument
                  .setGranularIntent(hit.getSourceAsMap().get(granularIntentLabel) == null ? null
                          : hit.getSourceAsMap().get(granularIntentLabel).toString());

          classificationDocument
                  .setClassification(hit.getSourceAsMap().get(classificationDataLabel) == null ? null
                          : hit.getSourceAsMap().get(classificationDataLabel).toString());

          classificationDocument.setProjectId(
                  hit.getSourceAsMap().get(projectIdLabel) == null ? null
                          : hit.getSourceAsMap().get(projectIdLabel).toString());

          classificationDocument.setClientId(
                  hit.getSourceAsMap().get(clientIdLabel) == null ? null
                          : hit.getSourceAsMap().get(clientIdLabel).toString());

          classificationDocument
                  .setId(hit.getSourceAsMap().get(recordIdLabel) == null ? null
                          : hit.getSourceAsMap().get(recordIdLabel).toString());

          classificationDocument.setExamples(
                  hit.getSourceAsMap().get(examplesLabel) == null ? null
                          : hit.getSourceAsMap().get(examplesLabel).toString());

          classificationDocument.setDescription(
                  hit.getSourceAsMap().get(descriptionLabel) == null ? null
                          : hit.getSourceAsMap().get(descriptionLabel).toString());

          classificationDocument.setKeywords(
                  hit.getSourceAsMap().get(keywordsLabel) == null ? null
                          : hit.getSourceAsMap().get(keywordsLabel).toString());

          classificationDocument.setComments(
                  hit.getSourceAsMap().get(commentedAtLabel) == null ? null
                          : hit.getSourceAsMap().get(commentedAtLabel).toString());
        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(" to load classification while updating ")
                .append(" for classificationRecordId ")
                .append(classificationRecordId);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    return classificationDocument;
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param username
   * @return
   * @throws ApplicationException
   */
  @Override
  public SeedIntentDocumentsStats seedIntentDocuments(int clientId, int projectId, int datasetId,
                                                      String username) throws ApplicationException {

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String intentLabel = elasticSearchProps.getImportedIntentLabel();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String rutagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    String projectIdStr = String.valueOf(projectId);
    String clientIdStr = String.valueOf(clientId);
    String datasetIdStr = String.valueOf(datasetId);

    validationManager.validateProjectId(projectIdStr);

    SeedIntentDocumentsStats seedStats = new SeedIntentDocumentsStats();

    List<Integer> allDatasetIds = projectDatasetManager.listDatasetIdsByProjectId(projectIdStr);

    DatasetIntentInheritance datasetInheritance = this.datasetManager
            .getLastPendingInheritaceForDataset(datasetIdStr);

    if (datasetInheritance != null) {
      this.datasetManager.updateIntentInheritanceStatus(datasetInheritance.getId(),
              DatasetIntentInheritanceStatus.PROCESSING);
    }

    // get all the data from current data set original record.
    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(
            QueryBuilders
                    .termQuery(documentTypeLabel, DocumentType.ORIGINAL.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    log.debug("queryBuilder--->", queryBuilder.toString());

    StringBuilder functionality = new StringBuilder(
            " load original transcriptions while seeding intent added documents ")
            .append(CLIENT_ID).append(clientId)
            .append(COMMA_PROJECT).append(projectId)
            .append(COMMA_DATASET_ID).append(datasetId);

    SearchResponse originalTransciptionsResponse = elasticSearchHelper
            .executeScrollRequest(queryBuilder, 0, pageSize,
                    new String[]{transcriptionHashLabel, intentLabel, rutagLabel},
                    functionality.toString());

    List<ActionRequest> bulkRequests = new ArrayList<>();

    final ConcurrentMap<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    final ConcurrentMap<String, AtomicInteger> multiIntentcounterMap = new ConcurrentHashMap<>();

    String taggedAtTime = String
            .format("%d", Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());

    int totalSeeded = 0;

    // while we find any original datatype records in current dataset
    if (originalTransciptionsResponse != null) {

      Map<String, String> intentAndRuClassificationIdMap = new HashMap<>(); // map to store list of classification is for combination of intent and rutag
      Map<String, ClassificationDocument> newClassifications = new HashMap<>(); // map to store list of only new classifications
      Map<String, List<String>> classificationHashes = new HashMap<>();
      Set<String> currentCheckedHashes = new HashSet<>();
      HashMap<String, Transcription> currentDataClassificationRecords = new HashMap<>();
      SearchHit[] originalTranscriptions;

      do {
        //  current original transcriptions
        originalTranscriptions = originalTransciptionsResponse.getHits().getHits();

        // adding tagged documents we are dealing with currently added to all documents
        String classificationID = null;
        long scrollEndTime = 0;
        long scrollStartTime = 0;

        for (SearchHit currentTranscription : originalTranscriptions) {
          classificationID = null;
          totalSeeded += 1;  // incrementing seeded doc count
          String docId = currentTranscription.getId(); //record Id
          String hash = currentTranscription.getSourceAsMap().get(transcriptionHashLabel)
                  .toString(); // current record hash

          if (currentCheckedHashes.contains(hash)) {
            // nothing to do if hash already deal with
            continue;
          }

          // add to current hash if not found already
          currentCheckedHashes.add(hash);

          String intentValue = null;
          String ruTagValue = null;
          String importedIntent = null; // inherited Intent
          String inheritedRutag = null; // inherited rutag

          if (currentTranscription.getSourceAsMap().containsKey(intentLabel)) {
            // granular intent
            intentValue = currentTranscription.getSourceAsMap().get(intentLabel).toString();
          }
          if (currentTranscription.getSourceAsMap().containsKey(rutagLabel)) {
            // ru intent
            ruTagValue = currentTranscription.getSourceAsMap().get(rutagLabel).toString();
          }

          // if either granular or rutag found
          if ((intentValue != null && !StringUtils.isEmpty(intentValue)) ||
                  (ruTagValue != null && !StringUtils.isEmpty(ruTagValue))) {

            if (currentTranscription.getSourceAsMap().get(intentLabel) != null) {
              importedIntent = currentTranscription.getSourceAsMap().get(intentLabel).toString();
            } // inherited Intent

            if (currentTranscription.getSourceAsMap().get(rutagLabel) != null) {
              inheritedRutag = currentTranscription.getSourceAsMap().get(rutagLabel).toString();
            } // inherited rutag

            classificationID = checkOrCreateClassification(clientIdStr, projectIdStr,
                    intentAndRuClassificationIdMap,
                    newClassifications, inheritedRutag,
                    importedIntent); // classificationId from current Record

          }

          // creation of actual transciption which will be inserted into elastic search.
          Transcription doc = new Transcription();
          if (!StringUtils.isEmpty(classificationID)) {
            doc.setClassificationId(classificationID);
            List<String> hashes = classificationHashes.get(classificationID);
            if (hashes == null) {
              hashes = new ArrayList<>();
            }
            hashes.add(hash);

            classificationHashes.put(classificationID, hashes);
          }

          doc.setTaggedBy(username);
          doc.setTaggedAt(taggedAtTime);
          doc.setDocumentId(docId);
          doc.setClientId(clientId);
          doc.setProjectId(projectId);
          doc.setDatasetId(datasetId);
          doc.setDocumentType(DocumentType.INTENT_ADDED.type());
          doc.setTranscriptionHash(hash);

          currentDataClassificationRecords.put(hash, doc);

        }

        scrollStartTime = System.currentTimeMillis();

        originalTransciptionsResponse = elasticSearchClient
                .prepareSearchScroll(originalTransciptionsResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        functionality = new StringBuilder(
                " iterating through original documents while seed intent added docuemnts   ")
                .append(FOR_CLIENT).append(clientId).append(PROJECT).append(projectId);

        scrollEndTime = System.currentTimeMillis();

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (originalTransciptionsResponse.getHits().getHits().length != 0);

      for (Map.Entry<String,Transcription> entry : currentDataClassificationRecords.entrySet()) {
        Transcription doc = entry.getValue();
        try {
          bulkRequests
                  .add(elasticSearchHelper.buildIndexRequest(documentIndexName, indexType, doc));
        } catch (IOException ioe) {
          String message = String.format("Error creating request: %s", ioe.getMessage());
          log.error(message);
          throw new ApplicationException(message, ioe);
        }
      }

      functionality = new StringBuilder(" to seed intent_added documents ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(" and  current dataset ").append(datasetId);

      elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

      if (!newClassifications.isEmpty()) {

        List<ClassificationDocument> tags = new ArrayList<>();
        ClassificationDocument tagRow;
        for (Map.Entry<String, ClassificationDocument> entry : newClassifications.entrySet()) {
          ClassificationDocument classificationItem = entry.getValue();
          tagRow = this.scrubClassificationDocument(classificationItem, datasetIdStr,
                  classificationHashes.get(entry.getKey()));
          tags.add(tagRow);
        }

        if (!tags.isEmpty()) {
          this.indexClassification(clientIdStr, projectIdStr, tags, new ArrayList<>());
        }
      }

      bulkRequests = new ArrayList<>();

      if (bulkRequests != null && !bulkRequests.isEmpty()) {
        functionality = new StringBuilder(" to seed intent_added documents into previous datasets ")
                .append(FOR_CLIENT).append(clientId)
                .append(PROJECT).append(projectId);
        elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());
      }

      log.debug("Tagged documents stats: {}, {}", counterMap, multiIntentcounterMap);

      int uniqueTagged = counterMap.keySet().size();
      int totalTagged = counterMap.values().stream().mapToInt(AtomicInteger::intValue).sum();
      int uniqueTaggedMultipleIntents = multiIntentcounterMap.keySet().size();
      int totalTaggedMultipleIntents = multiIntentcounterMap.values().stream()
              .mapToInt(AtomicInteger::intValue).sum();

      if (datasetInheritance != null) {
        datasetInheritance.setInheritedFromDatasetIds(
                allDatasetIds.stream().map(String::valueOf).collect(Collectors.toList()));
        datasetInheritance.setStatus(DatasetIntentInheritanceStatus.COMPLETED);
        datasetInheritance.setTotalTagged(totalTagged);
        datasetInheritance.setUniqueTagged(uniqueTagged);
        datasetInheritance.setTotalTaggedMulipleIntents(totalTaggedMultipleIntents);
        datasetInheritance.setUniqueTaggedMulipleIntents(uniqueTaggedMultipleIntents);
        datasetManager.updateIntentInheritance(datasetInheritance);
      }

      seedStats.setTotalSeeded(totalSeeded);
      seedStats.setTotalTagged(totalTagged);
      seedStats.setUniqueTagged(uniqueTagged);
      seedStats.setTotalTaggedMultipleIntents(totalTaggedMultipleIntents);
      seedStats.setUniqueTaggedMultipleIntents(uniqueTaggedMultipleIntents);
    }

    return seedStats;
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param transcriptions
   * @param username
   * @throws ApplicationException
   */
  @Override
  public void indexNewTranscriptions(String clientId, String projectId, String datasetId,
                                     List<TranscriptionDocumentForIndexing> transcriptions, String username)
          throws ApplicationException {

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();

    int clientIdInt = Integer.parseInt(clientId);
    int projectIdInt = Integer.parseInt(projectId);
    int datasetIdInt = Integer.parseInt(datasetId);

    log.debug("indexName in indexNewTranscriptions-->", documentIndexName);

    try {
      long documentsBefore = elasticSearchHelper
              .getTotalNlToolsOriginalDocumentsCount(clientIdInt, projectIdInt, datasetIdInt);
      long expectedDocumentsAfter = documentsBefore + transcriptions.size();

      List<ActionRequest> bulkRequests = new ArrayList<>();
      for (TranscriptionDocumentForIndexing doc : transcriptions) {
        bulkRequests.add(elasticSearchHelper.buildIndexRequest(documentIndexName, indexType, doc));
      }

      StringBuilder functionality = new StringBuilder(" to index new transcriptions ")
              .append(" from index ").append(documentIndexName)
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(DATASET_ID).append(datasetId)
              .append(FOR_TRANSCRIPTIONS).append(transcriptions);

      elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

      long documentsAfter = elasticSearchHelper
              .getTotalNlToolsOriginalDocumentsCount(clientIdInt, projectIdInt, datasetIdInt);
      while (documentsAfter < expectedDocumentsAfter) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          log.error("Interrupted while sleeping before checking for total original documents again",
                  e);
          Thread.currentThread().interrupt();
        }
        documentsAfter = elasticSearchHelper
                .getTotalNlToolsOriginalDocumentsCount(clientIdInt, projectIdInt, datasetIdInt);
      }

      this.seedIntentDocuments(clientIdInt, projectIdInt, datasetIdInt, username);

    } catch (IOException ioe) {
      String message = "Failed indexing new original transcriptions";
      log.error(message, ioe);
      throw new ApplicationException(message);
    }
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param intent
   * @param username
   * @param transcriptionHashList
   * @return
   * @throws ApplicationException
   */
  @Override
  public UpdateIntentResponse addIntentByTranscriptionHashList(String clientId, String projectId,
                                                               String datasetId, String intent,
                                                               String rutag, String username, List<String> transcriptionHashList)
          throws ApplicationException {

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    if (!TextUtil.isIntentValid(intent)) {
      String message = String
              .format("Invalid intent. Intent %s should be of goal-topic format", intent);
      throw new BadRequestException(message);
    }

    int uniqueCount = transcriptionHashList.size();
    log.debug(String
            .format("[ELASTICSEARCH] Tagging %d unique entries with intent '%s'", uniqueCount, intent));

    this.validateIntentAddition(clientId, projectId, datasetId, transcriptionHashList);

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String taggedDate = String.valueOf(cal.getTimeInMillis());

    BoolQueryBuilder intentAddedDocQueryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .filter(QueryBuilders.termQuery(documentTypeLabel,
                    DocumentType.INTENT_ADDED.type()));

    if (datasetId != null) {
      intentAddedDocQueryBuilder.filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));
    }

    StringBuilder functionality = new StringBuilder(
            " load transciptions to add intent for transcriptions   ").append(FOR_CLIENT)
            .append(clientId)
            .append(COMMA_PROJECT)
            .append(projectId)
            .append(COMMA_DATASET_ID)
            .append(datasetId)
            .append(HASHES)
            .append(
                    transcriptionHashList)
            .append(" {} ");

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(intentAddedDocQueryBuilder, 0, pageSize,
                    new String[]{}, functionality.toString());
    int taggedCount = 0;

    HashMap<String, String> intentAndRuClassificationMap = new HashMap<>();

    Map<String, ClassificationDocument> newClassifications = new HashMap<>();

    String classificationID = checkOrCreateClassification(clientId, projectId,
            intentAndRuClassificationMap, newClassifications,
            rutag, intent);
    long scrollStartTime = 0;
    long scrollEndTime = 0;
    do {
      List<ActionRequest> bulkRequests = new ArrayList<>();
      for (SearchHit hit : searchResponse.getHits().getHits()) {

        log.debug("searchHit: {}", hit.toString());
        UpdateRequest request = this.buildUpdateRequest(documentIndexName, indexType, hit.getId(),
                classificationID != null ? classificationID : null, username, taggedDate);
        bulkRequests.add(request);

      }

      functionality = new StringBuilder(" to add intents to the transcriptions  ")
              .append(FOR_CLIENT).append(clientId)
              .append(COMMA_PROJECT).append(projectId)
              .append(COMMA_DATASET_ID).append(datasetId)
              .append(HASHES).append(transcriptionHashList)
              .append(" with classificationID ").append(classificationID);

      elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

      taggedCount += bulkRequests.size();

      scrollStartTime = System.currentTimeMillis();

      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(searchGetTimeout))
              .execute()
              .actionGet();

      functionality = new StringBuilder(" iterating through  documents while updating intents   ")
              .append(FOR_CLIENT)
              .append(clientId)
              .append(PROJECT)
              .append(projectId);

      scrollEndTime = System.currentTimeMillis();

      logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
              intentAddedDocQueryBuilder.toString(), null);

    } while (searchResponse.getHits().getHits().length != 0);

    if (!newClassifications.isEmpty()) {

      List<ClassificationDocument> tags = new ArrayList<>();
      ClassificationDocument tagRow;
      for (Map.Entry<String, ClassificationDocument> entry : newClassifications.entrySet()) {
        ClassificationDocument classificationItem = entry.getValue();
        tagRow = this
                .scrubClassificationDocument(classificationItem, datasetId, transcriptionHashList);
        tags.add(tagRow);
      }

      if (!tags.isEmpty()) {
        this.indexClassification(clientId, projectId, tags, new ArrayList<>());
      }
    }

    log.debug(String
            .format("[ELASTICSEARCH] Tagged %d total entries with intent '%s'", taggedCount, intent));

    return new UpdateIntentResponse().uniqueCount(uniqueCount).totalCount(taggedCount);
  }

  /**
   * @param clientId
   * @param projectId
   * @param classificationWithTranscriptions
   * @param otherDatasetIds
   * @param bulkRequests
   * @param username
   * @return
   */
  @Override
  public UpdateIntentResponse addIntentsByTranscriptionHashList(String clientId, String projectId,
                                                                Map<String, List<String>> classificationWithTranscriptions,
                                                                List<Integer> otherDatasetIds, List<ActionRequest> bulkRequests,
                                                                String username) {

    log.debug("For adding intents to transcriptions.");
    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String taggedDate = String.valueOf(cal.getTimeInMillis());

    BoolQueryBuilder intentAddedDocQueryBuilder;

    if (classificationWithTranscriptions != null) {

      for (Map.Entry<String, List<String>> entry : classificationWithTranscriptions.entrySet()) {
        intentAddedDocQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termsQuery(transcriptionHashLabel,
                        entry.getValue()))
                .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
                .filter(QueryBuilders.termQuery(documentTypeLabel,
                        DocumentType.INTENT_ADDED.type()));

        if (otherDatasetIds != null && !otherDatasetIds.isEmpty()) {
          intentAddedDocQueryBuilder
                  .filter(QueryBuilders.termsQuery(datasetIdLabel, otherDatasetIds));
        }

        log.debug("intentAddedDocQueryBuilder in addIntentsByTranscriptionHashList ---> ",
                intentAddedDocQueryBuilder.toString());

        StringBuilder functionality = new StringBuilder(
                " load intent_added transcriptions while updating intents  ")
                .append(FOR_CLIENT)
                .append(clientId)
                .append(COMMA_PROJECT)
                .append(projectId)
                .append(FOR_TRANSCRIPTIONS)
                .append(entry.getValue());

        SearchResponse searchResponse = elasticSearchHelper
                .executeScrollRequest(intentAddedDocQueryBuilder, 0,
                        pageSize,
                        new String[]{}, functionality.toString());

        long execStartTime = 0;
        long execEndTime = 0;
        do {

          for (SearchHit hit : searchResponse.getHits().getHits()) {
            log.debug("searchHit: {}", hit.toString());
            UpdateRequest request = this
                    .buildUpdateRequest(documentIndexName, indexType, hit.getId(), entry.getKey(),
                            username,
                            taggedDate);

            bulkRequests.add(request);

          }

          execStartTime = System.currentTimeMillis();

          searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                  .setScroll(new TimeValue(searchGetTimeout))
                  .execute()
                  .actionGet();

          execEndTime = System.currentTimeMillis();

          functionality = new StringBuilder(
                  " load intent_added transcriptions while updating intents ")
                  .append(FOR_CLIENT).append(clientId).append(PROJECT).append(projectId)
                  .append(FOR_HASH_LIST)
                  .append(entry.getValue());

          logTimeForESRequest(execStartTime, execEndTime, functionality.toString(),
                  intentAddedDocQueryBuilder.toString(), null);

        } while (searchResponse.getHits().getHits().length != 0);
      }
    }

    return new UpdateIntentResponse().uniqueCount(bulkRequests.size())
            .totalCount(bulkRequests.size());
  }

  /**
   * Method used to update instants of multiple transcriptions list for multiple intents. This is
   * called as part of Seed documents method to update intents of old tagged transcriptions
   */
  @Override
  public UpdateIntentResponse updateIntentsByTranscriptionHashList(String clientId,
                                                                   String projectId,
                                                                   Map<String, List<String>> classificationWithTranscriptions,
                                                                   String username, List<Integer> otherDatasetIds, String currentDatasetId)
          throws ApplicationException {

    log.debug("Checking and updating any existing previous transcriptions with current transcription intents");

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();
    String documentIdLabel = elasticSearchProps.getTaggedDocumentIdLabel();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String tagLabel = elasticSearchProps.getTagLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String updatedAt = String.valueOf(cal.getTimeInMillis());

    List<String> transcriptionHashList;

    String intentDeletedLabel = "intent-deleted";

    List<ActionRequest> bulkRequests = new ArrayList<>();

    int updatedCount = 0;

    int uniqueCount = 0;

    Map<String, List<String>> newClassificationWithTranscriptions = new HashMap<>();

    StringBuilder functionality;

    if (!classificationWithTranscriptions.isEmpty()) {
      validationManager.validateProjectId(projectId);

      if (classificationWithTranscriptions != null && !classificationWithTranscriptions.isEmpty()) {

        classificationLoop:
        for (Map.Entry<String, List<String>> entry : classificationWithTranscriptions.entrySet()) {

          String newClassification = entry.getKey();
          if (!TextUtil.isIntentValid(newClassification)) {

            continue classificationLoop;
          }
          transcriptionHashList = entry.getValue();

          uniqueCount = uniqueCount + transcriptionHashList.size();

          // creating query to search Elastic for transcriptions in the project
          BoolQueryBuilder intentUpdateDocQueryBuilder = QueryBuilders.boolQuery()
                  .filter(
                          QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList))
                  .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
                  .filter(QueryBuilders.termQuery(
                          documentTypeLabel,
                          DocumentType.INTENT_ADDED.type()));

          if (otherDatasetIds != null && !otherDatasetIds.isEmpty()) {
            intentUpdateDocQueryBuilder
                    .filter(QueryBuilders.termsQuery(datasetIdLabel, otherDatasetIds));
          }

          functionality = new StringBuilder(" load transcriptions to update intents")
                  .append(FOR_CLIENT).append(clientId)
                  .append(", " + "project ").append(projectId)
                  .append(COMMA_DATASET_ID).append(otherDatasetIds)
                  .append(HASHES).append(transcriptionHashList);

          SearchResponse searchResponse = elasticSearchHelper
                  .executeScrollRequest(intentUpdateDocQueryBuilder, 0,
                          pageSize,
                          new String[]{tagLabel, taggedAtLabel, taggedByLabel, clientIdLabel,
                                  documentIdLabel, transcriptionHashLabel, datasetIdLabel}, functionality.toString());

          if (searchResponse.getHits().getHits().length < 1) {
            return new UpdateIntentResponse().uniqueCount(0).totalCount(0);
          }
          long execStartTime = 0;
          long execEndTime = 0;

          do {
            for (SearchHit hit : searchResponse.getHits().getHits()) {
              createAndAddUpdateReqToBulkRequests(bulkRequests, newClassificationWithTranscriptions,
                      hit, projectId, newClassification, username, updatedAt, intentDeletedLabel);
            }

            updatedCount += searchResponse.getHits().getHits().length;
            execStartTime = System.currentTimeMillis();
            searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(searchGetTimeout))
                    .execute()
                    .actionGet();

            execEndTime = System.currentTimeMillis();
            functionality = new StringBuilder(" load transcriptions to update intent ")
                    .append(FOR_CLIENT).append(clientId).append(PROJECT)
                    .append(projectId).append(FOR_HASH_LIST).append(transcriptionHashList)
                    .append("with new classification  ").append(newClassification);
            logTimeForESRequest(execStartTime, execEndTime, functionality.toString(),
                    intentUpdateDocQueryBuilder.toString(),
                    null);

          } while (searchResponse.getHits().getHits().length != 0);
        }

      }

      addIntentsByTranscriptionHashList(clientId, projectId, newClassificationWithTranscriptions,
              otherDatasetIds, bulkRequests,
              username);

      functionality = new StringBuilder(" to add intents to transcriptions  ")
              .append(FOR_CLIENT).append(clientId)
              .append(COMMA_PROJECT).append(projectId)
              .append(", for other DatasetIds ").append(otherDatasetIds);

      elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

    }

    return new UpdateIntentResponse().uniqueCount(uniqueCount).totalCount(updatedCount);

  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param newIntent
   * @param newRutag
   * @param username
   * @param transcriptionHashList
   * @return
   * @throws ApplicationException
   */
  @Override
  public UpdateIntentResponse updateIntentByTranscriptionHashList(String clientId, String projectId,
                                                                  String datasetId, String newIntent,
                                                                  String newRutag, String username, List<String> transcriptionHashList)
          throws ApplicationException {

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();
    String documentIdLabel = elasticSearchProps.getTaggedDocumentIdLabel();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    // validating project
    validationManager.validateProjectId(projectId);

    int uniqueCount = transcriptionHashList.size();
    log.debug("[ELASTICSEARCH] Untagging " + uniqueCount + " unique entries");

    ClassificationDocument classificationDocument = null;
    // creating query to search Elastic for transcriptions in the project
    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId)).filter(
                    QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()));
    if (datasetId != null) {
      queryBuilder.filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));
    }
    if (clientId != null) {
      queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    log.debug("intentUpdateDocQueryBuilder in updateIntentByTranscriptionHashList ---> ",
            queryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" update intents for the transcriptions ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append(" datasetId ").append(datasetId)
            .append(FOR_HASH_LIST).append(transcriptionHashList)
            .append("with new Intent ").append(newIntent)
            .append("and  new rutag ").append(newRutag);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(queryBuilder, 0, pageSize,
                    new String[]{classificationIdLabel, taggedAtLabel,
                            taggedByLabel, clientIdLabel, documentIdLabel, transcriptionHashLabel,
                            datasetIdLabel, documentTypeLabel}, functionality.toString());

    long nltoolsTxnIntentAddedDocCount = searchResponse.getHits().getHits().length;

    int updatedCount = 0;

    if (nltoolsTxnIntentAddedDocCount < 1) {
      return new UpdateIntentResponse().uniqueCount(0).totalCount(0);
    }

    boolean ifNewClassification = false;

    String classificationID = checkIfIntentAndRutagExists(clientId, projectId, newIntent, newRutag);

    if (StringUtils.isEmpty(classificationID)) {

      classificationDocument = this
              .createClassificationBase(clientId, projectId, newRutag, newIntent);

      classificationID = classificationDocument.getClassificationId();
      ifNewClassification = true;
    }

    List<ActionRequest> bulkRequests = new ArrayList<>();

    long execStartTime;
    long execEndTime;

    Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    String addRecordUpdateTime = String.valueOf(cal1.getTimeInMillis() + 1);

    do {

      for (SearchHit hit : searchResponse.getHits().getHits()) {

        if (hit.getSourceAsMap().containsKey(documentTypeLabel) &&
                hit.getSourceAsMap().get(documentTypeLabel).toString()
                        .equalsIgnoreCase(DocumentType.INTENT_ADDED.type())) {

          UpdateRequest addRequest = this
                  .buildUpdateRequest(documentIndexName, indexType, hit.getId(), classificationID, username,
                          addRecordUpdateTime);

          bulkRequests.add(addRequest);
          String datasetIdStr = null;

          if (hit.getSourceAsMap().containsKey(datasetIdLabel)) {
            datasetIdStr = hit.getSourceAsMap().get(datasetIdLabel).toString();
          }
          TranscriptionForAuditing auditTranscription = this.getAuditDocument(projectId,
                  datasetIdStr, username, addRecordUpdateTime, hit, false);

          IndexRequest indexRequest = null;
          try {
            indexRequest = elasticSearchHelper
                    .buildIndexRequest(documentIndexName, indexType,
                            auditTranscription);
          } catch (IOException e) {

            String message = String
                    .format("Error updating intent on transcription Documents: %s", e.getMessage());
            log.error(message);
            throw new ApplicationException(message, e);
          }
          bulkRequests.add(indexRequest);
        }

      }

      updatedCount += searchResponse.getHits().getHits().length;
      execStartTime = System.currentTimeMillis();

      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(searchGetTimeout))
              .execute()
              .actionGet();

      execEndTime = System.currentTimeMillis();

      functionality = new StringBuilder(" update intents for the transcriptions ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(" datasetId ").append(datasetId)
              .append(FOR_HASH_LIST).append(transcriptionHashList)
              .append("with new Intent ").append(newIntent)
              .append("and  new rutag ").append(newRutag);

      logTimeForESRequest(execStartTime, execEndTime, functionality.toString(),
              queryBuilder.toString(), null);

    } while (searchResponse.getHits().getHits().length != 0);

    functionality = new StringBuilder(" to update intents on transcriptions  ")
            .append(FOR_CLIENT).append(clientId)
            .append(COMMA_PROJECT).append(projectId)
            .append(", dataset ").append(datasetId)
            .append(", rutag ").append(newRutag)
            .append(", intent ").append(newIntent);

    elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

    if (ifNewClassification) {

      List<ClassificationDocument> tags = new ArrayList<>();
      ClassificationDocument tagRow;

      tagRow = scrubClassificationDocument(classificationDocument, datasetId,
              transcriptionHashList);

      tags.add(tagRow);
      if (!tags.isEmpty()) {
        this.indexClassification(clientId, projectId, tags, new ArrayList<String>());
      }
    }

    return new UpdateIntentResponse().uniqueCount(uniqueCount).totalCount(updatedCount);
  }

  /**
   * @param clientId
   * @param projectId
   * @throws ApplicationException
   */
  @Override
  public void deleteProjectIntentsFromGuide(String clientId, String projectId)
          throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    validationManager.validateProjectId(projectId);

    String indexType = projectId;
    QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

    List<ActionRequest> bulkRequests = new ArrayList<>();

    StringBuilder functionality = new StringBuilder(
            " to load intents while deleting  project intents  ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 100, new String[]{}, null,
                    functionality.toString());

    long scrollStartTime = 0;
    long scrollEndTime = 0;
    if (searchResponse != null) {
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          bulkRequests.add(new DeleteRequest(indexName, indexType, hit.getId()));
        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(" to load intents while deleting  project intents  ")
                .append(FOR_CLIENT).append(clientId)
                .append(PROJECT).append(projectId);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    functionality = new StringBuilder(" to delete project intents from guide  ")
            .append(FOR_CLIENT).append(clientId)
            .append(COMMA_PROJECT).append(projectId);

    elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @param username
   * @param transcriptionHashList
   * @return
   * @throws ApplicationException
   */
  @Override
  public UpdateIntentResponse deleteIntentByTranscriptionHashList(String clientId, String projectId,
                                                                  String datasetId, String username,
                                                                  List<String> transcriptionHashList) throws ApplicationException {

    log.debug("while deleting intent on transcriptions");
    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();
    String documentIdLabel = elasticSearchProps.getTaggedDocumentIdLabel();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    validationManager.validateProjectId(projectId);

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String deletedAt = String.valueOf(cal.getTimeInMillis());

    int uniqueCount = transcriptionHashList.size();
    log.debug("[ELASTICSEARCH] Untagging " + uniqueCount + " unique entries");

    BoolQueryBuilder intentDeleteDocQueryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId)).filter(
                    QueryBuilders.termQuery(documentTypeLabel,
                            DocumentType.INTENT_ADDED.type()));

    log.debug("intentDeleteDocQueryBuilder in deleteIntentByTranscriptionHashList --->  ",
            intentDeleteDocQueryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" delete intents for transcriptions ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append(DATASET_ID).append(datasetId)
            .append(" for transcriptionHashList ").append(transcriptionHashList);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(intentDeleteDocQueryBuilder, 0,
                    pageSize,
                    new String[]{classificationIdLabel, taggedAtLabel, taggedByLabel, clientIdLabel,
                            documentIdLabel, transcriptionHashLabel, datasetIdLabel}, functionality.toString());

    if (searchResponse.getHits().getHits().length < 1) {
      return new UpdateIntentResponse().uniqueCount(0).totalCount(0);
    }

    int untaggedCount = 0;

    long execStartTime = 0;

    long execEndTime = 0;
    do {
      List<ActionRequest> bulkRequests = new ArrayList<>();

      for (SearchHit hit : searchResponse.getHits().getHits()) {
        try {
          if (hit.getSourceAsMap().containsKey(classificationIdLabel)) {

            bulkRequests.add(this.buildUnTaggingUpdateRequest(documentIndexName, indexType, hit.getId()));

            TranscriptionForAuditing auditTranscription = this.getAuditDocument(projectId,
                    hit.getSourceAsMap().get(datasetIdLabel).toString(), username, deletedAt, hit, true);

            bulkRequests.add(
                    elasticSearchHelper.buildIndexRequest(documentIndexName, indexType, auditTranscription));
          }
        } catch (IOException ioe) {
          String message = String.format("Error creating request: %s", ioe.getMessage());
          log.error(message);
          throw new ApplicationException(message, ioe);
        }
      }

      functionality = new StringBuilder(" to delete intents for transcriptions  ")
              .append(FOR_CLIENT).append(clientId)
              .append(COMMA_PROJECT).append(projectId)
              .append(COMMA_DATASET_ID).append(datasetId)
              .append(HASHES).append(transcriptionHashList);

      elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

      untaggedCount += searchResponse.getHits().getHits().length;

      execStartTime = System.currentTimeMillis();

      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(searchGetTimeout))
              .execute()
              .actionGet();

      execEndTime = System.currentTimeMillis();

      functionality = new StringBuilder(" to delete intents for transcriptions  ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(FOR_HASH_LIST).append(transcriptionHashList);

      logTimeForESRequest(execStartTime, execEndTime, functionality.toString(),
              intentDeleteDocQueryBuilder.toString(), null);

    } while (searchResponse.getHits().getHits().length != 0);
    log.debug("[ELASTICSEARCH] Untagged " + untaggedCount + " total entries");
    return new UpdateIntentResponse().uniqueCount(uniqueCount).totalCount(untaggedCount);
  }

  /**
   * add comments on transcriptions (create comment _added document)
   */
  @Override
  public UpdateIntentResponse addCommentByTranscriptionHash(String clientId, String projectId,
                                                            String datasetId, String comment,
                                                            String username, List<String> transcriptionHashList)
          throws ApplicationException {

    log.debug(String.format("[ELASTICSEARCH] adding comment to '%s'", transcriptionHashList));

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String commentLabel = elasticSearchProps.getCommentLabel();
    int pageSize = elasticSearchProps.getSearchPageSize();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    int commentedCount = 0;
    int newCommentCount = 0;
    long scrollStartTime = 0;
    long scrollEndTime = 0;
    List<ActionRequest> bulkRequests = new ArrayList<>();

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String commentedDate = String.valueOf(cal.getTimeInMillis());


    BoolQueryBuilder commentAddedDocQueryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .filter(QueryBuilders.termQuery(documentTypeLabel,
                            DocumentType.COMMENT_ADDED.type()))
            .must(QueryBuilders.existsQuery(commentLabel));

    if (datasetId != null) {
      commentAddedDocQueryBuilder.filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));

    }

    log.debug("commentAddedDocQueryBuilder in addCommentByTranscriptionHash ---> ",
            commentAddedDocQueryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" add comment for transcription ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(commentAddedDocQueryBuilder, 0,
                    pageSize,
                    new String[]{transcriptionHashLabel, datasetIdLabel}, functionality.toString());


    if (searchResponse.getHits().getTotalHits() > 0) {
      Set<String> hashes = new HashSet<>();
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          UpdateRequest request = this
                  .buildCommentUpdateRequest(documentIndexName, indexType, hit.getId(), comment, username,
                          commentedDate);
          bulkRequests.add(request);
          hashes.add(hit.getSourceAsMap().get(transcriptionHashLabel).toString());
          commentedCount++;
        }

        scrollStartTime = System.currentTimeMillis();
        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(" to add comments by transcriptions   ")
                .append(FOR_CLIENT).append(clientId)
                .append(PROJECT).append(projectId);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                commentAddedDocQueryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);

      transcriptionHashList.removeAll(hashes);
    }

    // create new comment-added documents
    if (!transcriptionHashList.isEmpty()) {
      for (String transcriptionHash : transcriptionHashList) {

        if (datasetId == null) {
          // if no dataset id given add comment to all of the projects datasets
          for (DatasetBO datasetDetail : validationManager.validateProjectTransformedStatus(projectId)) {
            IndexRequest indexRequest = this.buildCommentIndexRequest(projectId,
                    Integer.toString(datasetDetail.getId()),
                    transcriptionHash, comment, username, commentedDate);

            bulkRequests.add(indexRequest);
            newCommentCount++;
          }

        } else {
          IndexRequest indexRequest = this
                  .buildCommentIndexRequest(projectId, datasetId, transcriptionHash, comment,
                          username,
                          commentedDate);

          bulkRequests.add(indexRequest);
          newCommentCount++;
        }
      }
    }
    functionality = new StringBuilder(" to add comments by transcriptions  ")
            .append(FOR_CLIENT).append(clientId)
            .append(COMMA_PROJECT).append(projectId)
            .append(", datasetId ").append(datasetId)
            .append(", transcriptionHashList ").append(transcriptionHashList);

    elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

    int total = commentedCount + newCommentCount;
    log.debug(String
            .format("[ELASTICSEARCH] Updated %d total entries with comment '%s'", total, comment));

    return new UpdateIntentResponse().uniqueCount(newCommentCount).totalCount(total);
  }

  /**
   * @param projectId
   * @param datasetId
   * @param username
   * @param transcriptionHashList
   * @return
   * @throws ApplicationException
   */
  @Override
  public UpdateIntentResponse deleteCommentByTranscriptionHash(String clientId, String projectId,
                                                               String datasetId, String username,
                                                               List<String> transcriptionHashList) throws ApplicationException {

    log.debug(String.format("[ELASTICSEARCH] removing comments from '%s'", transcriptionHashList));

    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();
    int pageSize = elasticSearchProps.getSearchPageSize();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String commentLabel = elasticSearchProps.getCommentLabel();

    BoolQueryBuilder commentDeleteDocQueryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel,
                    transcriptionHashList))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
            .filter(
                    QueryBuilders.termQuery(documentTypeLabel,
                            DocumentType.COMMENT_ADDED.type()))
            .must(QueryBuilders.existsQuery(commentLabel));

    if (datasetId != null) {
      commentDeleteDocQueryBuilder.filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));
    }

    log.debug("commentDeleteDocQueryBuilder in deleteCommentByTranscriptionHash ---> ",
            commentDeleteDocQueryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" delete comment for transcription ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(commentDeleteDocQueryBuilder, 0,
                    pageSize,
                    new String[]{transcriptionHashLabel}, functionality.toString());

    int commentedCount = 0;

    long scrollEndTime = 0;

    long scrollStartTime = 0;

    List<ActionRequest> bulkRequests = new ArrayList<>();
    while (searchResponse.getHits().getHits().length > 0) {
      for (SearchHit hit : searchResponse.getHits().getHits()) {
        DeleteRequest request = new DeleteRequest(documentIndexName, indexType, hit.getId());
        bulkRequests.add(request);
        commentedCount++;
      }

      scrollStartTime = System.currentTimeMillis();

      searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
              .setScroll(new TimeValue(searchGetTimeout))
              .execute()
              .actionGet();

      scrollEndTime = System.currentTimeMillis();

      functionality = new StringBuilder(" to delete comments by transcriptions   ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId);

      logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
              commentDeleteDocQueryBuilder.toString(), null);

    }

    functionality = new StringBuilder(" to delete comments by transcriptions  ")
            .append(FOR_CLIENT).append(clientId)
            .append(COMMA_PROJECT).append(projectId)
            .append(", datasetId ").append(datasetId)
            .append(", transcriptionHashList ").append(transcriptionHashList);

    elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

    log.debug(String.format("[ELASTICSEARCH] Removed %d total entries comments", commentedCount));

    return new UpdateIntentResponse().uniqueCount(commentedCount).totalCount(commentedCount);
  }

  @Override
  public ClassificationDocument addNewIntent(String clientId, String projectId,
                                             TaggingGuideDocument taggingGuideDocument)
          throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String indexType = projectId;
    String intent = taggingGuideDocument.getIntent();

    if (!TextUtil.isIntentValid(intent)) {
      throw new InvalidRequestException(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), null, "Invalid intent"));
    }

    String classificationId = this
            .loadClassificationId(clientId, projectId, taggingGuideDocument.getRutag(),
                    taggingGuideDocument.getIntent());

    if (!StringUtils.isEmpty(classificationId)) {
      log.error("Intent {} already exists");
      throw new AlreadyExistsException(
              new Error(Response.Status.CONFLICT.getStatusCode(), null, "Intent already exist"));
    }
    ClassificationDocument classificationDocument;
    try {
      String documentId = SecurityUtil.toSHA1(String.format("%s_%s", projectId, intent));

      classificationDocument = this
              .createClassificationBase(clientId, projectId, taggingGuideDocument.getRutag(),
                      taggingGuideDocument.getIntent());

      classificationDocument.setId(documentId);

      IndexResponse indexResponse = elasticSearchClient
              .prepareIndex(indexName, indexType, documentId)
              .setSource(jsonObjectMapper.writeValueAsBytes(classificationDocument),
                      XContentType.JSON)
              .get();
      if (indexResponse.status() == RestStatus.CREATED) {
        return classificationDocument;
      }
    } catch (IOException ioe) {
      String message = String.format("Error creating request: %s", ioe.getMessage());
      log.error(message);
      throw new ApplicationException(message, ioe);
    }

    throw new ApplicationException(
            "Failed creating the intent document " + classificationDocument);
  }

  @Override
  public ClassificationDocument patchIntent(String clientId, String projectId,
                                            String classificationId, PatchRequest intentJsonPatch,
                                            String username) throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String tagLabel = elasticSearchProps.getTagLabel();
    String rutagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String indexType = projectId;

    String[] fieldsArray = new String[]{projectIdLabel, clientIdLabel, granularIntentLabel,
            classificationDataLabel,
            classificationIdLabel,
            commentsLabel, examplesLabel, keywordsLabel, descriptionLabel};

    ClassificationDocument classificationRecord = this
            .loadClassification(clientId, projectId, classificationId);

    GetResponse response = elasticSearchClient
            .prepareGet(indexName, indexType, classificationRecord.getId())
            .setStoredFields(fieldsArray)
            .get();

    if (!response.isExists()) {
      log.error("Intent with ID {} not found", classificationId);
      throw new NotFoundException(
              new Error(Response.Status.NOT_FOUND.getStatusCode(), null, ErrorMessage.INTENT_NOT_FOUND));
    }

    ClassificationTransientDocument docFromSystem = new ClassificationTransientDocument();
    PropertyAccessor docAccessor = PropertyAccessorFactory.forBeanPropertyAccess(docFromSystem);

    for (int i = 0; i < fieldsArray.length; i++) {
      String field = fieldsArray[i];
      GetField getField = response.getField(field);
      if (getField != null) {
        Object objectValue = getField.getValue();
        if (field.equals(granularIntentLabel)) {
          docAccessor.setPropertyValue(tagLabel, objectValue);
          continue;
        }
        if (field.equals(classificationDataLabel)) {
          docAccessor.setPropertyValue(rutagLabel, objectValue);
          continue;
        }
        if (objectValue != null) {
          docAccessor.setPropertyValue(field, objectValue);
        }
      }
    }

    docFromSystem.setId(response.getId());

    ClassificationTransientDocument docAfterUpdate = jsonConverter
            .patch(intentJsonPatch, docFromSystem, ClassificationTransientDocument.class);

    ClassificationDocument newClassificationDoc = new ClassificationDocument();

    BeanUtils.copyProperties(docAfterUpdate, newClassificationDoc);

    newClassificationDoc.setClassification(docAfterUpdate.getRutag());

    newClassificationDoc.setGranularIntent(docAfterUpdate.getIntent());

    try {
      IndexRequest indexRequest = elasticSearchHelper
              .buildIndexRequest(indexName, indexType, newClassificationDoc);

      long execStartTime = System.currentTimeMillis();
      elasticSearchClient
              .prepareUpdate(indexName, indexType, classificationRecord.getId())
              .setDoc(jsonObjectMapper.writeValueAsBytes(newClassificationDoc), XContentType.JSON)
              .setUpsert(indexRequest)
              .get();

      long execEndTime = System.currentTimeMillis();

      StringBuilder functionality = new StringBuilder(" to patch intent ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(" and classificationId ").append(classificationId);

      logTimeForESRequest(execStartTime, execEndTime, functionality.toString(), null, null);

    } catch (Exception e) {
      String message = String.format("Error updating intent document: %s", e.getMessage());
      log.error(message, e);
      throw new ApplicationException(message, e);
    }

    return newClassificationDoc;

  }

  @Override
  public ClassificationDocument patchIntent(String clientId, String projectId, String intent,
                                            String rutag, PatchRequest intentJsonPatch,
                                            String username) throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String tagLabel = elasticSearchProps.getTagLabel();
    String rutagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String indexType = projectId;

    String[] fieldsArray = new String[]{projectIdLabel, clientIdLabel, granularIntentLabel,
            classificationDataLabel,
            classificationIdLabel,
            commentsLabel, examplesLabel, keywordsLabel, descriptionLabel};

    ClassificationDocument classificationRecord = this
            .loadClassificationFromIntent(clientId, projectId, rutag, intent);

    GetResponse response = elasticSearchClient
            .prepareGet(indexName, indexType, classificationRecord.getId())
            .setStoredFields(fieldsArray)
            .get();

    if (!response.isExists()) {
      log.error(
              String.format("Intent with ID %s not found for rutag and intent %s", rutag, intent));
      throw new NotFoundException(
              new Error(Response.Status.NOT_FOUND.getStatusCode(), null, ErrorMessage.INTENT_NOT_FOUND));
    }

    ClassificationTransientDocument docFromSystem = new ClassificationTransientDocument();
    PropertyAccessor docAccessor = PropertyAccessorFactory.forBeanPropertyAccess(docFromSystem);

    for (int i = 0; i < fieldsArray.length; i++) {
      String field = fieldsArray[i];
      GetField getField = response.getField(field);
      if (getField != null) {
        Object objectValue = getField.getValue();
        if (field.equals(granularIntentLabel)) {
          docAccessor.setPropertyValue(tagLabel, objectValue);
          continue;
        }
        if (field.equals(classificationDataLabel)) {
          docAccessor.setPropertyValue(rutagLabel, objectValue);
          continue;
        }
        if (objectValue != null) {
          docAccessor.setPropertyValue(field, objectValue);
        }
      }
    }

    docFromSystem.setId(response.getId());

    ClassificationTransientDocument docAfterUpdate = jsonConverter
            .patch(intentJsonPatch, docFromSystem, ClassificationTransientDocument.class);

    ClassificationDocument newClassificationDoc = new ClassificationDocument();

    BeanUtils.copyProperties(docAfterUpdate, newClassificationDoc);

    newClassificationDoc.setClassification(docAfterUpdate.getRutag());

    newClassificationDoc.setGranularIntent(docAfterUpdate.getIntent());

    try {
      IndexRequest indexRequest = elasticSearchHelper
              .buildIndexRequest(indexName, indexType, newClassificationDoc);

      long execStartTime = System.currentTimeMillis();
      elasticSearchClient
              .prepareUpdate(indexName, indexType, classificationRecord.getId())
              .setDoc(jsonObjectMapper.writeValueAsBytes(newClassificationDoc), XContentType.JSON)
              .setUpsert(indexRequest)
              .get();

      long execEndTime = System.currentTimeMillis();

      StringBuilder functionality = new StringBuilder(" to patch intent ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(" , intent ").append(intent)
              .append(" , rutag ").append(rutag);

      logTimeForESRequest(execStartTime, execEndTime, functionality.toString(), null, null);

    } catch (Exception e) {
      String message = String.format("Error updating intent document: %s", e.getMessage());
      log.error(message, e);
      throw new ApplicationException(message, e);
    }

    return newClassificationDoc;
  }

  /**
   * @param clientId
   * @param projectId
   * @param classificationRecordId
   * @param intentJsonPatch
   * @param username
   * @return
   * @throws ApplicationException
   */
  @Override
  public ClassificationDocument patchIntentById(String clientId, String projectId,
                                                String classificationRecordId,
                                                PatchRequest intentJsonPatch, String username) throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String tagLabel = elasticSearchProps.getTagLabel();
    String rutagLabel = elasticSearchProps.getTaggingGuideRUTagLabel();
    String indexType = projectId;
    Long execStartTime = System.currentTimeMillis();
    GetResponse response = elasticSearchClient
            .prepareGet(indexName, indexType, classificationRecordId)
            .get();
    Long execEndTime = System.currentTimeMillis();

    StringBuilder functionality = new StringBuilder(" load classifications  using prepareGet ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId);

    logTimeForESRequest(execStartTime, execEndTime, functionality.toString(), null, null);

    if (!response.isExists()) {
      log.error("Intent with ID {} not found", classificationRecordId);
      throw new NotFoundException(
              new Error(Response.Status.NOT_FOUND.getStatusCode(), null, ErrorMessage.INTENT_NOT_FOUND));
    }

    ClassificationTransientDocument docFromSystem = new ClassificationTransientDocument();
    PropertyAccessor docAccessor = PropertyAccessorFactory.forBeanPropertyAccess(docFromSystem);

    Map<String, Object> responseMap = response.getSourceAsMap();
    responseMap.put(tagLabel, responseMap.get(granularIntentLabel));
    responseMap.remove(granularIntentLabel);
    responseMap.put(rutagLabel, responseMap.get(classificationDataLabel));
    responseMap.remove(classificationDataLabel);
    responseMap.remove("class");
    docAccessor.setPropertyValues(responseMap);

    docFromSystem.setId(response.getId());

    ClassificationTransientDocument docAfterPatch = jsonConverter
            .patch(intentJsonPatch, docFromSystem, ClassificationTransientDocument.class);

    ClassificationDocument newClassificationDoc = new ClassificationDocument();

    BeanUtils.copyProperties(docAfterPatch, newClassificationDoc);

    newClassificationDoc.setClassification(docAfterPatch.getRutag());

    newClassificationDoc.setGranularIntent(docAfterPatch.getIntent());

    try {

      String classificationId = this
              .loadClassificationId(clientId, projectId, docAfterPatch.getRutag(),
                      docAfterPatch.getIntent());
      if (classificationId != null && !classificationId
              .equalsIgnoreCase(docAfterPatch.getClassificationId())) {
        throw new ApplicationException("Duplicate intent names are not allowed ");
      }

      IndexRequest indexRequest = elasticSearchHelper
              .buildIndexRequest(indexName, indexType, newClassificationDoc);

      log.debug("indexRequest in patchIntentById ---> ", indexRequest.toString());

      elasticSearchClient
              .prepareUpdate(indexName, indexType, classificationRecordId)
              .setDoc(jsonObjectMapper.writeValueAsBytes(newClassificationDoc), XContentType.JSON)
              .setUpsert(indexRequest)
              .get();

    } catch (Exception e) {
      String message = String.format("Error updating intent document: %s", e.getMessage());
      log.error(message, e);
      throw new ApplicationException(message, e);
    }

    return newClassificationDoc;
  }

  /**
   * @param clientId
   * @param projectId
   * @param classificationId
   * @param userName
   * @throws ApplicationException
   */
  @Override
  public void deleteIntentById(String clientId, String projectId, String classificationId,
                               String userName) throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String indexType = projectId;

    GetResponse getResponse = elasticSearchClient.prepareGet(indexName, indexType, classificationId)
            .setStoredFields(granularIntentLabel, classificationIdLabel).get();

    if (!getResponse.isExists()) {
      throw new NotFoundException(
              new Error(Response.Status.NOT_FOUND.getStatusCode(), null, ErrorMessage.INTENT_NOT_FOUND));
    }

    GetField tagField = getResponse.getField(classificationIdLabel);
    if (tagField != null) {
      Object objectValue = tagField.getValue();
      if (objectValue != null) {
        String loadedLlassificationId = (String) objectValue;
        List<String> classificationIds = new ArrayList<>();
        classificationIds.add(loadedLlassificationId);
        this.deleteIntentsByIntentListForProject(clientId, projectId, userName, classificationIds);
      }
    }

    DeleteResponse deleteResponse = elasticSearchClient
            .prepareDelete(indexName, indexType, classificationId)
            .setVersion(getResponse.getVersion()).get();

    if (deleteResponse.status() == RestStatus.NOT_FOUND) {
      throw new ApplicationException(
              "Failed deleting the intent document with ID " + classificationId);
    }
  }

  /**
   * @param clientId
   * @param projectId
   * @param datasetId
   * @throws ApplicationException
   */
  @Override
  public void deleteRecords(String clientId, String projectId, String datasetId)
          throws ApplicationException {

    //
    // https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-delete-by-query.html
    //
    String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();

    BulkByScrollResponse response =
            DeleteByQueryAction.INSTANCE.newRequestBuilder(elasticSearchClient)
                    .filter(QueryBuilders.boolQuery()
                            .filter(QueryBuilders.termQuery(projectIdLabel, projectId))
                            .filter(QueryBuilders.termQuery(datasetIdLabel, datasetId)))
                    .source(documentIndexName)
                    .get();

    long deleted = response.getDeleted();
    long totalTimeElapsed = response.getTook().getMillis();
    log.info(
            "Delete Complete: {} records deleted in {} ms", deleted, totalTimeElapsed);
  }

  /**
   * @param clientId
   * @param indexType
   * @return
   */
  @Override
  public Set<String> getProjectIntents(String clientId, String indexType) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    Set<String> intents = new HashSet<>();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.matchAllQuery());

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termsQuery(clientIdLabel, clientId));
    }

    log.debug("queryBuilder in getProjectIntents ---> ", queryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" get client intents  ")
            .append(FOR_CLIENT).append(clientId)
            .append(" for project ").append(indexType);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 50,
                    new String[]{granularIntentLabel, classificationDataLabel
                    }, null,
                    functionality.toString());
    long scrollStartTime = 0;
    long scrollEndTime = 0;

    if (searchResponse != null) {
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          intents.add(hit.getSourceAsMap().get(granularIntentLabel).toString());
        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(" to load projects intents while deleting ")
                .append(FOR_CLIENT).append(clientId)
                .append(" for project ").append(indexType);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    return intents;
  }

  /**
   * method which created intentbguide documents
   */

  @Override
  public void indexClassification(String clientId, String projectId,
                                  List<ClassificationDocument> newClassifications,
                                  List<String> danglingIntents) throws ApplicationException {

    String indexName = elasticSearchProps.getClassificationIndexAlias();

    String indexType = projectId;
    List<ActionRequest> bulkRequests = new ArrayList<>();
    String documentId = null;
    for (ClassificationDocument tagRow : newClassifications) {

      documentId = SecurityUtil
              .toSHA1(String.format("%s_%s", projectId, tagRow.getClassificationId()));
      try {
        tagRow.setId(documentId);
        IndexRequest indexRequest = this.elasticSearchHelper
                .buildIndexRequest(indexName, indexType, tagRow);
        UpdateRequest updateRequest = new UpdateRequest(indexName, indexType, documentId)
                .doc(indexRequest).upsert(indexRequest);
        log.info("Update request are: " + updateRequest);
        bulkRequests.add(updateRequest);
      } catch (IOException ioe) {
        String message = "Error creating index request";
        log.error(message, ioe);
        throw new ApplicationException(message, ioe);
      }
    }

    for (String intent : danglingIntents) {
      String danglingId = SecurityUtil.toSHA1(String.format("%s_%s", projectId, intent));
      DeleteRequest deleteRequest = new DeleteRequest(indexName, indexType, danglingId);
      bulkRequests.add(deleteRequest);
    }

    StringBuilder functionality = new StringBuilder(
            " to delete dangling intents while indexing classification ")
            .append(FOR_CLIENT).append(clientId)
            .append(COMMA_PROJECT).append(projectId)
            .append(" for intents").append(danglingIntents);

    elasticSearchHelper.executeBulkRequests(bulkRequests, functionality.toString());

  }

  /**
   * @param clientId
   * @param projectId
   * @param userName
   * @param classificationIds
   * @throws ApplicationException
   */
  @Override
  public void deleteIntentsByIntentListForProject(String clientId, String projectId,
                                                  String userName, List<String> classificationIds)
          throws ApplicationException {

    log.debug("deleteIntentsByIntentListForProject ---> ", classificationIds.toString());

    Map<String, List<String>> datasetToHashMap = this
            .getTranscriptionHashListForProjectByIntents(clientId, projectId,
                    classificationIds);

    for (Map.Entry<String, List<String>> entry : datasetToHashMap.entrySet()) {
      String datasetId = entry.getKey();
      List<String> hashList = entry.getValue();

      //TODO remove
      log.debug("Untagging following hashes {} for projectId {} and datasetId {}");
      log.info(String.format("Untagging following hashes %s for projectId %s and datasetId %s",
              String.join(",", hashList), projectId, datasetId));
      this.deleteIntentByTranscriptionHashList(clientId, projectId, datasetId, userName, hashList);
    }
  }

  /**
   * @param clientId
   * @param projectId
   * @param classificationId
   * @return
   */
  @Override
  public Boolean intentExistsInTheGuide(String clientId, String projectId,
                                        String classificationId) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String tagLabel = elasticSearchProps.getTagLabel();
    boolean exists = false;

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(tagLabel, classificationId));
    String indexType = projectId;

    StringBuilder functionality = new StringBuilder(" check intent exits in guide ")
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append(" classificationId ").append(classificationId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 1, new String[]{},
                    null,
                    functionality.toString());

    if (searchResponse != null) {
      exists = searchResponse.getHits().getTotalHits() > 0;
    }
    return exists;
  }

  @Override
  public String loadClassificationId(String clientId, String projectId, String classification,
                                     String intent) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (!StringUtils.isEmpty(intent)) {
      queryBuilder = queryBuilder.filter(QueryBuilders
              .termQuery(granularIntentLabel, intent.toLowerCase()));
    } else {
      queryBuilder = queryBuilder.filter(
              QueryBuilders.termQuery(classificationDataLabel,
                      classification.toLowerCase()))
              .must(QueryBuilders.existsQuery(classificationDataLabel))
              .mustNot(QueryBuilders.existsQuery(granularIntentLabel));
    }

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    String indexType = projectId;
    String[] fields = {classificationIdLabel,
            granularIntentLabel,
            classificationDataLabel};
    String classificationId = null;

    StringBuilder functionality = new StringBuilder(CLASSIFICATION_DOC)
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append("and classificationId").append(classificationId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 1, fields, null,
                    functionality.toString());

    long scrollEndTime = 0;

    long scrollStartTime = 0;

    if (searchResponse != null) {

      do {

        for (SearchHit hit : searchResponse.getHits().getHits()) {

          if (hit.getSourceAsMap().get(classificationIdLabel) != null) {
            classificationId = hit.getSourceAsMap()
                    .get(classificationIdLabel).toString();
          }

        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    return classificationId;
  }

  @Override
  public ClassificationDocument loadClassification(String clientId, String projectId,
                                                   String classificationId) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String recordIdLabel = elasticSearchProps.getRecordIdLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (!StringUtils.isEmpty(classificationId)) {
      queryBuilder = queryBuilder.filter(
              QueryBuilders.termQuery(classificationIdLabel, classificationId));
    }
    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    String indexType = projectId;
    String[] fields = {classificationIdLabel, granularIntentLabel, classificationDataLabel,
            recordIdLabel};

    StringBuilder functionality = new StringBuilder(CLASSIFICATION_DOC)
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append("and classificationId").append(classificationId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 1, fields, null,
                    functionality.toString());

    long scrollStartTime = 0;
    long scrollEndTime = 0;
    ClassificationDocument classificationDocument = null;
    if (searchResponse != null) {
      classificationDocument = new ClassificationDocument();
      do {

        for (SearchHit hit : searchResponse.getHits().getHits()) {
          classificationId = hit.getSourceAsMap().get(classificationIdLabel).toString();
          classificationDocument.setClassificationId(classificationId);

          classificationDocument
                  .setGranularIntent(hit.getSourceAsMap().get(granularIntentLabel) == null ? null
                          : hit.getSourceAsMap().get(granularIntentLabel).toString());

          classificationDocument
                  .setClassification(hit.getSourceAsMap().get(classificationDataLabel) == null ? null
                          : hit.getSourceAsMap().get(classificationDataLabel).toString());

          classificationDocument.setProjectId(projectId);

          classificationDocument.setClientId(clientId);

          classificationDocument
                  .setId(hit.getSourceAsMap().get(recordIdLabel) == null ? null
                          : hit.getSourceAsMap().get(recordIdLabel).toString());

          classificationDocument.setExamples(
                  hit.getSourceAsMap().get(examplesLabel) == null ? null
                          : hit.getSourceAsMap().get(examplesLabel).toString());

          classificationDocument.setDescription(
                  hit.getSourceAsMap().get(descriptionLabel) == null ? null
                          : hit.getSourceAsMap().get(descriptionLabel).toString());

          classificationDocument.setKeywords(
                  hit.getSourceAsMap().get(keywordsLabel) == null ? null
                          : hit.getSourceAsMap().get(keywordsLabel).toString());

          classificationDocument.setComments(
                  hit.getSourceAsMap().get(commentedAtLabel) == null ? null
                          : hit.getSourceAsMap().get(commentedAtLabel).toString());

        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(" to load classification while updating   ")
                .append(FOR_CLIENT).append(clientId);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    return classificationDocument;
  }

  public UpdateRequest buildUpdateRequest(String indexName, String indexType, String docId,
                                          String classificationid, String username,
                                          String taggedDate) {

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();

    List<String> scriptElements = new ArrayList<>();

    String scriptBaseStringFormat = "ctx._source.%s = '%s';";

    scriptElements.add(String
            .format(scriptBaseStringFormat, classificationIdLabel,
                    classificationid));

    scriptElements.add(
            String.format(scriptBaseStringFormat, taggedByLabel, username));

    scriptElements.add(
            String.format(scriptBaseStringFormat, taggedAtLabel, taggedDate));

    String code = String.join(" ", scriptElements);

    Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, code,
            new HashMap<>());
    log.debug("indexName in buildTaggingUpdateRequest--->", indexName);

    return new UpdateRequest(indexName, indexType, docId).script(script);

  }

  /**
   * @param indexName
   * @param indexType
   * @param docId
   * @return
   */
  public UpdateRequest buildUnTaggingUpdateRequest(String indexName, String indexType,
                                                   String docId) {

    log.debug(
            "Build a untag update request for index document " + docId + " on index " + indexName);

    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();

    Long untagDocReqStartTime = System.currentTimeMillis();
    String scriptBaseStringFormat = "ctx._source.remove('%s');";

    List<String> scriptElements = new ArrayList<>();

    scriptElements
            .add(String.format(scriptBaseStringFormat, classificationIdLabel));

    log.debug("indexName  in buildUnTaggingUpdateRequest ---> ", indexName);

    String code = String.join(" ", scriptElements);

    Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, code,
            new HashMap<>());
    UpdateRequest updateRequest = new UpdateRequest(indexName, indexType, docId).script(script);

    Long untagDocReqEndTime = System.currentTimeMillis();

    log.info("untag update request building took " + (untagDocReqEndTime - untagDocReqStartTime)
            + " milliseconds.");
    return updateRequest;
  }

  /**
   * @param projectId
   * @param datasetId
   * @param username
   * @param taggedAtTime
   * @param hit
   * @return
   */
  private TranscriptionForAuditing getAuditDocument(String projectId, String datasetId,
                                                    String username, String taggedAtTime,
                                                    SearchHit hit, boolean isDelete) {


    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String taggedByLabel = elasticSearchProps.getTaggedByLabel();
    String documentIdLabel = elasticSearchProps.getTaggedDocumentIdLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();

    int clientId = 0;

    if (hit.getSourceAsMap().containsKey(clientIdLabel)
            && hit.getSourceAsMap().get(clientIdLabel) != null) {
      clientId = Integer.parseInt(hit.getSourceAsMap().get(clientIdLabel).toString());
    }
    String classificationId = null;
    if (hit.getSourceAsMap().containsKey(classificationIdLabel)
            && hit.getSourceAsMap().get(classificationIdLabel) != null) {
      classificationId = hit.getSourceAsMap().get(classificationIdLabel).toString();
    }

    String taggedAt = null;
    if (hit.getSourceAsMap().containsKey(taggedAtLabel)
            && hit.getSourceAsMap().get(taggedAtLabel) != null) {
      taggedAt = hit.getSourceAsMap().get(taggedAtLabel).toString();
    }

    Long recUpdateTime = Long.getLong(taggedAtTime);

    String taggedBy = null;

    if (hit.getSourceAsMap().containsKey(taggedByLabel)
            && hit.getSourceAsMap().get(taggedAtLabel) != null) {
      taggedBy = hit.getSourceAsMap().get(taggedAtLabel).toString();
    }

    if (StringUtils.isEmpty(taggedBy)) {
      taggedBy = username;
    }

    String transcriptionHash =
            hit.getSourceAsMap().get(transcriptionHashLabel) != null ? hit.getSourceAsMap().get(transcriptionHashLabel).toString()
                    : null;
    String originalDocId = hit.getSourceAsMap().get(documentIdLabel) != null ? hit.getSourceAsMap()
            .get(documentIdLabel).toString() : null;

    TranscriptionForAuditing auditTranscription = new TranscriptionForAuditing();

    log.debug("auditTranscription in getAuditDocument --->  ", auditTranscription.toString());

    Long taggedLong;
    if (!StringUtils.isEmpty(taggedAt)) {

      taggedLong = Long.parseLong(taggedAt);
      if (isDelete) {
        taggedLong = taggedLong + 1;
      } else {
        taggedLong = taggedLong - 1;
      }
    } else {
      if (isDelete) {
        if (recUpdateTime != null) {
          taggedLong = recUpdateTime.longValue() + 100;
        } else {
          taggedLong = System.currentTimeMillis() + 60000;
        }
      } else {
        if (recUpdateTime != null) {
          taggedLong = recUpdateTime.longValue() - 100;
        } else {
          taggedLong = System.currentTimeMillis() - 60000;
        }
      }


    }
    auditTranscription.setClassificationId(classificationId);
    auditTranscription.setTaggedAt(taggedLong.toString());
    auditTranscription.setTaggedBy(taggedBy);
    auditTranscription.setDeletedAt(taggedAtTime);
    auditTranscription.setDeletedBy(username);
    auditTranscription.setClientId(clientId);
    auditTranscription.setDatasetId(datasetId == null ? null : Integer.valueOf(datasetId));
    auditTranscription.setProjectId(Integer.valueOf(projectId));
    auditTranscription.setDocumentId(originalDocId);
    auditTranscription.setTranscriptionHash(transcriptionHash);
    auditTranscription.setDocumentType(DocumentType.INTENT_DELETED.type());

    return auditTranscription;
  }

  /**
   * @param bulkRequests
   * @param newClassificationTranscriptions
   * @param hit
   * @param projectId
   * @param newClassificationId
   * @param username
   * @param updatedAt
   * @param intentDeletedLabel
   * @throws ApplicationException
   */
  private void createAndAddUpdateReqToBulkRequests(List<ActionRequest> bulkRequests,
                                                   Map<String, List<String>> newClassificationTranscriptions, SearchHit hit,
                                                   String projectId, String newClassificationId, String username,
                                                   String updatedAt, String intentDeletedLabel) throws ApplicationException {

    try {
      String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
      String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
      String indexType = elasticSearchProps.getDefaultDocumentIndexType();
      String documentIndexName = elasticSearchProps.getNltoolsIndexAlias();

      UpdateRequest addRequest = this.buildUpdateRequest(documentIndexName, indexType, hit.getId(),
              newClassificationId, username, updatedAt);

      bulkRequests.add(addRequest);

      if (hit.getSourceAsMap().containsKey(classificationIdLabel)) {
        TranscriptionForAuditing auditTranscription = this.getAuditDocument(projectId,
                hit.getSourceAsMap().get(datasetIdLabel).toString(), username, updatedAt, hit, false);

        IndexRequest indexRequest = elasticSearchHelper
                .buildIndexRequest(documentIndexName, indexType,
                        auditTranscription);

        bulkRequests.add(indexRequest);
      } else if (intentDeletedLabel != null && !intentDeletedLabel.isEmpty() && hit.getFields()
              .containsKey(intentDeletedLabel)) {
        List<String> transcriptions;
        if (newClassificationTranscriptions.get(newClassificationId) != null) {
          transcriptions = newClassificationTranscriptions.get(newClassificationId);

        } else {
          transcriptions = new ArrayList<>();
        }
        transcriptions.add(hit.getSourceAsMap().toString());
        newClassificationTranscriptions.put(newClassificationId, transcriptions);
      }

    } catch (IOException ioe) {
      String message = String.format("Error creating request: %s", ioe.getMessage());
      log.error(message);
      throw new ApplicationException(message, ioe);
    }

  }

  /**
   * @param clientId
   * @param projectId
   * @param classificationIds
   * @return
   */
  public Map<String, List<String>> getTranscriptionHashListForProjectByIntents(String clientId,
                                                                               String projectId,
                                                                               List<String> classificationIds) {

    Map<String, List<String>> datasetToHashMap = new HashMap<>();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    int termsQueryChunkSize = elasticSearchProps.getTermsQueryChunkSize();
    int endIndex = 0;
    int startIndex = 0;
    int mainListSize = classificationIds.size();
    int projectIdInt = Integer.parseInt(projectId);

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders.terms(transcriptionHashLabel)
            .field(transcriptionHashLabel).size(Integer.MAX_VALUE).subAggregation(
                    AggregationBuilders.topHits("top")
                            .fetchSource(new String[]{transcriptionHashLabel, datasetIdLabel}, null));

    do {
      endIndex = ((endIndex + termsQueryChunkSize) > mainListSize) ? mainListSize
              : (endIndex + termsQueryChunkSize);
      List<String> subCLassificationList = classificationIds.subList(startIndex, endIndex);
      QueryBuilder queryBuilder = QueryBuilders.boolQuery()
              .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()))
              .filter(QueryBuilders.termQuery(projectIdLabel, projectIdInt))
              .filter(QueryBuilders.termsQuery(classificationIdLabel, subCLassificationList));

      log.debug("queryBuilder in getTranscriptionHashListForProjectByIntents ---> ",
              queryBuilder.toString());

      StringBuilder functionality = new StringBuilder(
              " get transcriptions forgive classification Ids ")
              .append(FOR_CLIENT).append(clientId)
              .append(PROJECT).append(projectId)
              .append(" classificationIds ").append(classificationIds);

      SearchResponse searchResponse = elasticSearchHelper
              .executeScrollRequest(queryBuilder, 0, 0, new String[]{transcriptionHashLabel, datasetIdLabel},
                      aggregrationBuilder, functionality.toString());

      if (searchResponse != null) {
        Terms hashTermBuckets = searchResponse.getAggregations().get(transcriptionHashLabel);
        for (Terms.Bucket entry : hashTermBuckets.getBuckets()) {
          TopHits topHits = entry.getAggregations().get("top");
          for (SearchHit hit : topHits.getHits().getHits()) {
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            if (sourceMap != null) {
              int datasetIdInt = (Integer) sourceMap.get(datasetIdLabel);
              String datasetId = "" + datasetIdInt;
              String trascriptionHash = (String) sourceMap.get(transcriptionHashLabel);
              List<String> hashList = datasetToHashMap.get(datasetId);
              if (hashList == null) {
                hashList = new ArrayList<>();
              }
              hashList.add(trascriptionHash);
              datasetToHashMap.put(datasetId, hashList);
            }
          }
        }
      }

      startIndex = endIndex;
    } while (endIndex < mainListSize);

    return datasetToHashMap;
  }

  /**
   * Method to get Latest intent for transcription in a project.
   */

  public String getIntent(String projectId, String trancscription) {

    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String tagLabel = elasticSearchProps.getTagLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String taggedAtLabel = elasticSearchProps.getTaggedAtLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String intent = null;

    int projectIdInt = Integer.parseInt(projectId);

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders.terms(transcriptionHashLabel)
            .field(transcriptionHashLabel).size(Integer.MAX_VALUE).subAggregation(
                    AggregationBuilders.topHits("top")
                            .fetchSource(new String[]{transcriptionHashLabel, tagLabel, datasetIdLabel, taggedAtLabel},
                                    null));

    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.INTENT_ADDED.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, projectIdInt))
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, trancscription));

    log.debug("queryBuilder in getIntent ---> ", queryBuilder.toString());

    StringBuilder functionality = new StringBuilder(" get intent ").append(PROJECT)
            .append(projectId);

    SearchResponse searchResponse = elasticSearchHelper.executeScrollRequest(queryBuilder, 0, 0,
            new String[]{transcriptionHashLabel, datasetIdLabel},
            aggregrationBuilder, functionality.toString());

    BigInteger lastUpdated = BigInteger.valueOf(0);
    BigInteger currentTaggedTime;
    if (searchResponse != null) {
      Terms hashTermBuckets = searchResponse.getAggregations().get(transcriptionHashLabel);
      for (Terms.Bucket entry : hashTermBuckets.getBuckets()) {
        TopHits topHits = entry.getAggregations().get("top");
        for (SearchHit hit : topHits.getHits().getHits()) {
          Map<String, Object> sourceMap = hit.getSourceAsMap();
          if (sourceMap != null) {
            currentTaggedTime = new BigInteger((String) sourceMap.get(taggedAtLabel));
            if (currentTaggedTime != null && lastUpdated.compareTo(currentTaggedTime) < 0) {
              lastUpdated = currentTaggedTime;
              intent = (String) sourceMap.get(tagLabel);
            }
          }
        }
      }
    }
    return intent;
  }

  private ClassificationDocument loadClassificationFromIntent(String clientId, String projectId,
                                                              String classification, String intent) {

    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String recordIdLabel = elasticSearchProps.getRecordIdLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    int searchGetTimeout = elasticSearchProps.getSearchActionGetTimeout();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(projectIdLabel, projectId));

    if (!StringUtils.isEmpty(intent)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(granularIntentLabel, intent));
    } else {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(granularIntentLabel, null));
      queryBuilder = queryBuilder.filter(
              QueryBuilders.termQuery(classificationDataLabel, classification));
    }

    if (!StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder
              .filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    String indexType = projectId;

    String[] fields = {classificationIdLabel, granularIntentLabel, classificationDataLabel};

    StringBuilder functionality = new StringBuilder(CLASSIFICATION_DOC)
            .append(FOR_CLIENT).append(clientId)
            .append(PROJECT).append(projectId)
            .append("and intent").append(intent)
            .append("and classification/rutag").append(classification);

    String classificationId = null;

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 1, fields, null,
                    functionality.toString());

    long scrollEndTime = 0;
    long scrollStartTime = 0;

    ClassificationDocument classificationDocument = null;
    if (searchResponse != null) {
      classificationDocument = new ClassificationDocument();
      do {
        for (SearchHit hit : searchResponse.getHits().getHits()) {
          classificationId = hit.getSourceAsMap().get(classificationIdLabel).toString();
          classificationDocument.setClassificationId(classificationId);

          classificationDocument
                  .setGranularIntent(hit.getSourceAsMap().get(granularIntentLabel) == null ? null
                          : hit.getSourceAsMap().get(granularIntentLabel).toString());

          classificationDocument
                  .setClassification(hit.getSourceAsMap().get(classificationDataLabel) == null ? null
                          : hit.getSourceAsMap().get(classificationDataLabel).toString());

          classificationDocument.setProjectId(projectId);

          classificationDocument.setClientId(clientId);

          classificationDocument
                  .setId(hit.getSourceAsMap().get(recordIdLabel) == null ? null
                          : hit.getSourceAsMap().get(recordIdLabel).toString());

          classificationDocument.setExamples(
                  hit.getSourceAsMap().get(examplesLabel) == null ? null
                          : hit.getSourceAsMap().get(examplesLabel).toString());

          classificationDocument.setDescription(
                  hit.getSourceAsMap().get(descriptionLabel) == null ? null
                          : hit.getSourceAsMap().get(descriptionLabel).toString());

          classificationDocument.setKeywords(
                  hit.getSourceAsMap().get(keywordsLabel) == null ? null
                          : hit.getSourceAsMap().get(keywordsLabel).toString());

          classificationDocument.setComments(
                  hit.getSourceAsMap().get(commentedAtLabel) == null ? null
                          : hit.getSourceAsMap().get(commentedAtLabel).toString());
        }

        scrollStartTime = System.currentTimeMillis();

        searchResponse = elasticSearchClient.prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(new TimeValue(searchGetTimeout))
                .execute()
                .actionGet();

        scrollEndTime = System.currentTimeMillis();

        functionality = new StringBuilder(
                " to load classification Classification based on intent  ")
                .append(FOR_CLIENT).append(clientId)
                .append(PROJECT).append(projectId)
                .append("and intent").append(intent)
                .append("and classification/rutag").append(classification);

        logTimeForESRequest(scrollStartTime, scrollEndTime, functionality.toString(),
                queryBuilder.toString(), null);

      } while (searchResponse.getHits().getHits().length != 0);
    }

    return classificationDocument;
  }

  private ClassificationDocument createClassificationBase(String clientId, String projectId,
                                                          String classification, String intent) {

    ClassificationDocument classificationDocument = new ClassificationDocument();
    String uniqueID = idCreatorUtil.createIntentRutagId(intent, classification, 25);

    classificationDocument.setClassificationId(uniqueID);

    if (!StringUtils.isEmpty(classification)) {
      classificationDocument.setClassification(classification);
    }
    if (!StringUtils.isEmpty(intent)) {
      classificationDocument.setGranularIntent(intent);
    }
    if (!StringUtils.isEmpty(clientId)) {
      classificationDocument.setClientId(clientId);
    }

    classificationDocument.setProjectId(projectId);

    return classificationDocument;
  }

  private ClassificationDocument scrubClassificationDocument(ClassificationDocument classification,
                                                             String datasetId,
                                                             List<String> transcriptionHashList) {

    String textLabel = elasticSearchProps.getTranscriptionLabel();
    String transcriptionHashLabel = elasticSearchProps.getTranscriptionHashLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String datasetIdLabel = elasticSearchProps.getDatasetIdLabel();
    String documentTypeLabel = elasticSearchProps.getDocumentTypeLabel();
    String autoTagStringLabel = elasticSearchProps.getAutoTagStringLabel();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery(documentTypeLabel, DocumentType.ORIGINAL.type()))
            .filter(QueryBuilders.termQuery(projectIdLabel, classification.getProjectId()))
            .filter(QueryBuilders.termsQuery(transcriptionHashLabel, transcriptionHashList));
    if (datasetId != null) {
      queryBuilder.filter(QueryBuilders.termQuery(datasetIdLabel, datasetId));
    }

    if (classification.getClientId() != null) {
      queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, classification.getClientId()));
    }

    AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.terms(transcriptionHashLabel)
            .field(transcriptionHashLabel)
            .size(
                    1) // In the future maybe set this higher to get the top X transcriptions
            .subAggregation(AggregationBuilders
                    .topHits("top")
                    .fetchSource(new String[]{textLabel,
                            autoTagStringLabel}, null)
                    .size(1));

    StringBuilder functionality = new StringBuilder(
            " loading original transcriptions while generating intent guide document examples ")
            .append(FOR_CLIENT).append(classification.getClientId()).append(PROJECT)
            .append(classification.getProjectId());

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(queryBuilder, 0, 0, new String[]{}, aggregationBuilder,
                    functionality.toString());

    Set<String> keywords = new HashSet<>();
    Set<String> examples = new HashSet<>();

    if (searchResponse != null) {
      Terms hashTermBuckets = searchResponse.getAggregations().get(transcriptionHashLabel);
      for (Terms.Bucket entry : hashTermBuckets.getBuckets()) {
        TopHits topHits = entry.getAggregations().get("top");
        for (SearchHit hit : topHits.getHits().getHits()) {
          Map<String, Object> sourceMap = hit.getSourceAsMap();
          if (sourceMap != null && !sourceMap.isEmpty()) {
            if (sourceMap.containsKey(textLabel)) {
              String valueOf = String.valueOf(sourceMap.get(textLabel));
              examples.add(valueOf);
            }
            if (sourceMap.containsKey(autoTagStringLabel)) {
              String valueOf = String.valueOf(sourceMap.get(autoTagStringLabel));
              Collections.addAll(keywords, valueOf.split(","));
            }
          }
        }
      }
    }

    String keywordsString = keywords.isEmpty() ? null
            : keywords.stream().map(String::trim).collect(Collectors.joining("\n"));
    String examplesString = examples.isEmpty() ? null
            : examples.stream().map(String::trim).collect(Collectors.joining("\n"));
    classification.setKeywords(keywordsString);
    classification.setExamples(examplesString);

    return classification;

  }

  /**
   * @param projectId
   * @param datasetId
   * @param transcriptionHash
   * @param comment
   * @param username
   * @param commentedDate
   * @return
   * @throws ApplicationException
   */
  private IndexRequest buildCommentIndexRequest(String projectId, String datasetId,
                                                String transcriptionHash,
                                                String comment, String username, String commentedDate) throws ApplicationException {

    String indexName = elasticSearchProps.getNltoolsIndexAlias();
    String indexType = elasticSearchProps.getDefaultDocumentIndexType();

    IndexRequest request;

    TranscriptionCommentDocument doc = new TranscriptionCommentDocument();
    doc.setProjectId(Integer.valueOf(projectId));
    doc.setDatasetId(datasetId == null ? null : Integer.valueOf(datasetId));
    doc.setDocumentType(DocumentType.COMMENT_ADDED.type());
    doc.setTranscriptionHash(transcriptionHash);
    doc.setComment(comment);
    doc.setCommentedAt(commentedDate);
    doc.setCommentedBy(username);

    try {
      request = elasticSearchHelper.buildIndexRequest(indexName, indexType, doc);
    } catch (IOException ioe) {
      String message = String.format("Error creating request: %s", ioe.getMessage());
      log.error(message);
      throw new ApplicationException(message, ioe);
    }

    return request;
  }

  /**
   * @param indexName
   * @param indexType
   * @param docId
   * @param comment
   * @param username
   * @param commentedDate
   * @return
   */
  private UpdateRequest buildCommentUpdateRequest(String indexName, String indexType, String docId,
                                                  String comment, String username,
                                                  String commentedDate) {

    log.debug("indexName in buildCommentUpdateRequest ---> ", indexName);

    String commentLabel = elasticSearchProps.getCommentLabel();
    String commentedByLabel = elasticSearchProps.getCommentedByLabel();
    String commentedAtLabel = elasticSearchProps.getCommentedAtLabel();

    List<String> scriptElements = new ArrayList<>();

    String scriptBaseStringFormat = "ctx._source.%s = '%s';";

    scriptElements
            .add(String.format(scriptBaseStringFormat, commentLabel, comment));
    scriptElements.add(
            String.format(scriptBaseStringFormat, commentedByLabel, username));
    scriptElements.add(String
            .format(scriptBaseStringFormat, commentedAtLabel, commentedDate));

    String code = String.join(" ", scriptElements);
    Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, code,
            new HashMap<>());

    return new UpdateRequest(indexName, indexType, docId).script(script);
  }

  /**
   * @return
   */
  private String checkIfIntentAndRutagExists(String clientIdStr, String projectIdStr,
                                             String importedIntent, String inheritedRutag) {

    if (!TextUtil.isIntentValid(importedIntent)) {
      return null;
    }

    if (StringUtils.isEmpty(importedIntent) && StringUtils.isEmpty(inheritedRutag)) {
      return null;
    }

    String classificationId = null;

    classificationId = this
            .loadClassificationId(clientIdStr, projectIdStr, inheritedRutag, importedIntent);

    return classificationId;
  }

  /**
   * @return
   */
  private String checkOrCreateClassification(String clientIdStr, String projectIdStr,
                                             Map<String, String> intentAndRuClassificationMap,
                                             Map<String, ClassificationDocument> newClassifications, String ruTag, String intent) {

    if (StringUtils.isEmpty(intent) && StringUtils.isEmpty(ruTag)) {
      return null;
    }

    if (!StringUtils.isEmpty(intent) && !TextUtil.isIntentValid(intent)) {
      return null; // TODO revisit
    }

    StringBuilder tagKey = new StringBuilder();

    if (!StringUtils.isEmpty(intent)) {
      // to identify that this is intent key
      tagKey.append(intent);
      tagKey.append(Constants.INTENT_RU_DELIMITER);
    }

    if (StringUtils.isEmpty(intent) && !StringUtils.isEmpty(ruTag)) {
      // to identify this is rutag key
      tagKey.append(Constants.INTENT_RU_DELIMITER);
      tagKey.append(ruTag);
    }

    String classificationId = null;
    classificationId = intentAndRuClassificationMap.get(tagKey.toString());  // check in current Map

    boolean ifLoadedOrCreated = false;

    if (classificationId == null) { // if not found in map check in db & load
      ifLoadedOrCreated = true;
      classificationId = this
              .loadClassificationId(clientIdStr, projectIdStr, ruTag, intent); // check in db and load
    }

    if (classificationId == null) { // not found in db as well create a new Record

      ClassificationDocument classificationDocument = this
              .createClassificationBase(clientIdStr, projectIdStr, ruTag, intent);
      classificationId = classificationDocument.getClassificationId();
      newClassifications.put(classificationId, classificationDocument);
    }

    if (!StringUtils.isEmpty(classificationId) && ifLoadedOrCreated) {
      intentAndRuClassificationMap.put(tagKey.toString(), classificationId);
    }

    ClassificationDocument previousClassificationDocument = newClassifications
            .get(classificationId);

    if (previousClassificationDocument != null) {
      // if rutag had changed for intent.. update of rutag happens here..
      previousClassificationDocument.setGranularIntent(intent);
      previousClassificationDocument.setClassification(ruTag);
    }

    return classificationId;
  }

  private void logTimeForESRequest(long scrollStartTime, long scrollEndTime, String functionality,
                                   String queryBuilder,
                                   String aggregrationBuilder) {

    StringBuilder logString = new StringBuilder(
            "  Scroll Refresh Request for the functionality to ")
            .append(functionality).append(" with  Elastic Search query ").append(queryBuilder)
            .append(" and aggragation query ").append(aggregrationBuilder)
            .append("  took  {} milliseconds.");
    long logTime = scrollEndTime - scrollStartTime;

    log.debug(logString.toString(), logTime);
  }

  /**
   * @param clientId
   * @param projectId
   * @return
   */
  public List<ClassificationDocument> getClassificationsForProject(String clientId,
                                                                   String projectId) {

    String granularIntentLabel = elasticSearchProps.getGranularIntentLabel();
    String indexName = elasticSearchProps.getClassificationIndexAlias();
    String classificationDataLabel = elasticSearchProps.getClassificationDataLabel();
    String classificationIdLabel = elasticSearchProps.getClassificationIdLabel();
    String commentsLabel = elasticSearchProps.getTaggingGuideCommentsLabel();
    String keywordsLabel = elasticSearchProps.getTaggingGuideKeywordsLabel();
    String examplesLabel = elasticSearchProps.getTaggingGuideExamplesLabel();
    String descriptionLabel = elasticSearchProps.getTaggingGuideDescriptionLabel();
    String projectIdLabel = elasticSearchProps.getProjectIdLabel();
    String clientIdLabel = elasticSearchProps.getClientIdLabel();
    String indexType = projectId;

    List<ClassificationDocument> guideDocs = new ArrayList<>();

    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.matchAllQuery());

    if (!org.apache.commons.lang.StringUtils.isEmpty(clientId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(clientIdLabel, clientId));
    }

    if (!org.apache.commons.lang.StringUtils.isEmpty(projectId)) {
      queryBuilder = queryBuilder.filter(QueryBuilders.termQuery(projectIdLabel, projectId));
    }

    AbstractAggregationBuilder aggregrationBuilder = AggregationBuilders
            .terms(classificationIdLabel).field(granularIntentLabel).size(Integer.MAX_VALUE)
            .subAggregation(
                    AggregationBuilders.topHits("top").fetchSource(
                            new String[]{classificationIdLabel, granularIntentLabel,
                                    classificationDataLabel,
                                    commentsLabel, examplesLabel, keywordsLabel, descriptionLabel},
                            null).size(1));

    StringBuilder functionality = new StringBuilder(" get tagging guide documents from new index  ")
            .append(FOR_CLIENT)
            .append(clientId)
            .append(PROJECT)
            .append(projectId);

    SearchResponse searchResponse = elasticSearchHelper
            .executeScrollRequest(indexName, indexType, queryBuilder, 0, 0,
                    new String[]{classificationIdLabel, classificationDataLabel, granularIntentLabel},
                    aggregrationBuilder,
                    functionality.toString());

    if (searchResponse != null) {
      Terms agg = searchResponse.getAggregations().get(classificationIdLabel);
      for (Terms.Bucket entry : agg.getBuckets()) {
        // We ask for top_hits for each bucket
        TopHits topHits = entry.getAggregations().get("top");
        for (SearchHit hit : topHits.getHits().getHits()) {
          ClassificationDocument doc = new ClassificationDocument();

          Map<String, Object> sourceMap = hit.getSourceAsMap();
          doc.setId(hit.getId());
          doc.setGranularIntent((String) sourceMap.get(granularIntentLabel));
          doc.setClassification((String) sourceMap.get(classificationDataLabel));
          doc.setClassificationId((String) sourceMap.get(classificationIdLabel));
          doc.setComments((String) sourceMap.get(commentsLabel));
          doc.setExamples((String) sourceMap.get(examplesLabel));
          doc.setKeywords((String) sourceMap.get(keywordsLabel));
          doc.setDescription((String) sourceMap.get(descriptionLabel));
          guideDocs.add(doc);
        }
      }
    }
    return guideDocs;
  }
}
