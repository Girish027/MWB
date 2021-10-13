/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.ClassificationDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.elasticsearch.action.ActionRequest;

public interface ContentManager {

  ClassificationDocument loadClassificationById(String clientId, String projectId,
      String classificationRecordId);

  @Data
  @JsonSerialize
  public static class SeedIntentDocumentsStats {

    private int totalSeeded;
    private int totalTagged;
    private int uniqueTagged;
    private int totalTaggedMultipleIntents;
    private int uniqueTaggedMultipleIntents;
  }

  /**
   * create intent documents for the transcriptions
   */
  public SeedIntentDocumentsStats seedIntentDocuments(int clientId, int projectId, int datasetId,
      String username)
      throws ApplicationException;

  /**
   * To index new Transcriptions
   */
  public void indexNewTranscriptions(String clientId, String projectId, String datasetId,
      List<TranscriptionDocumentForIndexing> transcriptions,
      String username) throws ApplicationException;


  /**
   * to add intents for transcriptions into current datasets
   */
  public UpdateIntentResponse addIntentByTranscriptionHashList(String clientId, String projectId,
      String datasetId, String intent, String rutag, String username,
      List<String> transcriptionHashList) throws ApplicationException;


  /**
   * populate bulk request withs intent documents for the transcription list on multiple datasets
   */
  public UpdateIntentResponse addIntentsByTranscriptionHashList(String clientId, String projectId,
      Map<String, List<String>> classificationWithTranscriptions,
      List<Integer> otherDatasetIds, List<ActionRequest> bulkRequests, String username);


  /**
   * update intents for transcriptions on multiple datasets
   */
  public UpdateIntentResponse updateIntentsByTranscriptionHashList(String clientId,
      String projectId, Map<String, List<String>> classificationWithTranscriptions, String username,
      List<Integer> otherDatasetIds, String currentDatasetId) throws ApplicationException;


  /**
   * update intents for transcriptions on single dataset
   */
  public UpdateIntentResponse updateIntentByTranscriptionHashList(String clientId, String projectId,
      String datasetId, String newIntent, String rutag, String username,
      List<String> transcriptionHashList) throws ApplicationException;


  /**
   * delete intents from tagging guide for the project
   */
  public void deleteProjectIntentsFromGuide(String clientId, String projectId)
      throws ApplicationException;


  /**
   * remove intents (created intent_deleted documents ) from transcription
   */
  public UpdateIntentResponse deleteIntentByTranscriptionHashList(String clientId, String projectId,
      String datasetId, String username,
      List<String> transcriptionHashList) throws ApplicationException;


  /**
   * add comments on transcriptions (create comment _added document)
   */
  public UpdateIntentResponse addCommentByTranscriptionHash(String clientId, String projectId,
      String datasetId, String comment, String username,
      List<String> transcriptionHashList) throws ApplicationException;


  /**
   * delete comments on transcriptions (create comment_deleted document)
   */
  public UpdateIntentResponse deleteCommentByTranscriptionHash(String clientId, String projectId,
      String datasetId, String username,
      List<String> transcriptionHashList)
      throws ApplicationException;


  /**
   * create new tagging guide document
   */
  public ClassificationDocument addNewIntent(String clientId, String projectId,
      TaggingGuideDocument classificationDocument) throws ApplicationException;


  /**
   * Modify intents by Id
   */
  public ClassificationDocument patchIntent(String clientId, String projectId,
      String classificationId, PatchRequest intentJsonPatch,
      String username) throws ApplicationException;


  /**
   * Modify intents by Id
   */
  public ClassificationDocument patchIntent(String clientId, String projectId, String intent,
      String rutag, PatchRequest intentJsonPatch,
      String username) throws ApplicationException;

  public ClassificationDocument patchIntentById(String clientId, String projectId,
      String classificationId,
      PatchRequest intentJsonPatch, String username) throws ApplicationException;

  /**
   * delete intent for the project
   */
  public void deleteIntentById(String clientId, String projectId, String classificationId,
      String userName) throws ApplicationException;

  /**
   * delete records from projects dataset
   */
  public void deleteRecords(String clientId, String projectId, String datasetId)
      throws ApplicationException;


  /**
   * load project intents
   */
  public Set<String> getProjectIntents(String clientId, String indexType);


  /**
   *
   * @param clientId
   * @param projectId
   * @param newIntents
   * @param danglingIntents
   * @throws ApplicationException
   */
  public void indexClassification(String clientId,
      String projectId, List<ClassificationDocument> newIntents,
      List<String> danglingIntents) throws ApplicationException;

  /**
   * delete intents for project
   */
  public void deleteIntentsByIntentListForProject(String clientId, String projectId,
      String userName,
      List<String> classificationIds) throws ApplicationException;

  /**
   * retrive rutag from tagging guide
   * @param clientId
   * @param intent
   * @param projectId
   * @return
   */
  // public String getRuTagforIntentFromTaggingGuide (String clientId, String projectId, String intent);


  /**
   * check if intent exists in tagging guide
   */
  public Boolean intentExistsInTheGuide(String clientId, String projectId, String classificationId);

  /**
   *
   * @param clientId
   * @param projectId
   * @param classification
   * @param intent
   * @return
   */
  public String loadClassificationId(String clientId, String projectId, String classification,
      String intent);

  /**
   *
   * @param clientId
   * @param projectId
   * @param classificationId
   * @return
   */
  public ClassificationDocument loadClassification(String clientId, String projectId,
      String classificationId);

  /**
   *
   * @param clientId
   * @param projectId
   * @return
   */
  public List<ClassificationDocument> getClassificationsForProject(String clientId,
      String projectId);


}
