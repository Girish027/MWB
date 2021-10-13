/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.ui.model.DocumentType;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("elasticTestUtils")
public class ElasticSearchTestUtils {

  private long usageThreadCount = 0;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;


  @Data
  public static class ProjectDatasetSetupResult {

    String jobId;
    String clientId;
    String projectId;
    String datasetId;
    int clientIdInt;
    int projectIdInt;
    int datasetIdInt;
  }

  public void refreshAllIndices() {
    this.elasticSearchClient.admin().indices().prepareRefresh().get();
  }

  public void refreshIndices(String... indicesArray) {
    this.elasticSearchClient.admin().indices().prepareRefresh(indicesArray).get();
  }


  public void refreshNltoolsIndex() {
    String indexName = elasticSearchProps.getNltoolsIndexName();
    this.elasticSearchClient.admin().indices().prepareRefresh(indexName).get();
  }

  public void refreshClassificationIndex() {
    String indexName = elasticSearchProps.getClassificationIndexName();
    this.elasticSearchClient.admin().indices().prepareRefresh(indexName).get();
  }

  public List<TranscriptionDocumentForIndexing> getMakeReservationHash1Document(
      String jobId, int clientId, int projectId, int datasetId) {

    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(3);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("make reservation");
    doc1.setTextStringOriginal("[side_speech] make a reservation");
    doc1.setTextStringForTagging("make a reservation");
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(doc1.getTextStringForTagging()));
    documentList.add(doc1);
    return documentList;
  }


  public List<TranscriptionDocumentForIndexing> getMakeReservationHash1Documents(
      String jobId, int clientId, int projectId, int datasetId) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(3);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("make reservation");
    doc1.setTextStringOriginal("[side_speech] make a reservation");
    doc1.setTextStringForTagging("make a reservation");
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(doc1.getTextStringForTagging()));
    documentList.add(doc1);

    TranscriptionDocumentForIndexing doc2 = new TranscriptionDocumentForIndexing();
    doc2.setJobId(jobId);
    doc2.setVertical("ECOMMERCE");
    doc2.setClientId(clientId);
    doc2.setProjectId(projectId);
    doc2.setDatasetId(datasetId);
    doc2.setDataType("Chat/Text");
    doc2.setNumTokens(3);
    doc2.setAutoTagCount(1);
    doc2.setAutoTagString("make reservation");
    doc2.setTextStringOriginal("[speech_in_noise] make a reservation [noise]");
    doc2.setTextStringForTagging("make a reservation");
    doc2.setDocumentType(DocumentType.ORIGINAL.type());
    doc2.setTranscriptionHash(DigestUtils.sha1Hex(doc2.getTextStringForTagging()));
    documentList.add(doc2);

    TranscriptionDocumentForIndexing doc3 = new TranscriptionDocumentForIndexing();
    doc3.setJobId(jobId);
    doc3.setVertical("ECOMMERCE");
    doc3.setClientId(clientId);
    doc3.setProjectId(projectId);
    doc3.setDatasetId(datasetId);
    doc3.setDataType("Chat/Text");
    doc3.setNumTokens(3);
    doc3.setAutoTagCount(1);
    doc3.setAutoTagString("make reservation");
    doc3.setTextStringOriginal("[noise] make a reservation [faded_noise]");
    doc3.setTextStringForTagging("make a reservation");
    doc3.setDocumentType(DocumentType.ORIGINAL.type());
    doc3.setTranscriptionHash(DigestUtils.sha1Hex(doc3.getTextStringForTagging()));
    documentList.add(doc3);

    return documentList;
  }


  public List<TranscriptionDocumentForIndexing> getCancelReservationHash1Documents(
      String jobId, int clientId, int projectId, int datasetId) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(3);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("cancel reservation");
    doc1.setTextStringOriginal("[side_speech] cancel a reservation");
    doc1.setTextStringForTagging("cancel a reservation");
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(doc1.getTextStringForTagging()));
    documentList.add(doc1);

    TranscriptionDocumentForIndexing doc2 = new TranscriptionDocumentForIndexing();
    doc2.setJobId(jobId);
    doc2.setVertical("ECOMMERCE");
    doc2.setClientId(clientId);
    doc2.setProjectId(projectId);
    doc2.setDatasetId(datasetId);
    doc2.setDataType("Chat/Text");
    doc2.setNumTokens(3);
    doc2.setAutoTagCount(1);
    doc2.setAutoTagString("cancel reservation");
    doc2.setTextStringOriginal("[speech_in_noise] cancel my reservation [noise]");
    doc2.setTextStringForTagging("cancel my reservation");
    doc2.setDocumentType(DocumentType.ORIGINAL.type());
    doc2.setTranscriptionHash(DigestUtils.sha1Hex(doc2.getTextStringForTagging()));
    documentList.add(doc2);

    TranscriptionDocumentForIndexing doc3 = new TranscriptionDocumentForIndexing();
    doc3.setJobId(jobId);
    doc3.setVertical("ECOMMERCE");
    doc3.setClientId(clientId);
    doc3.setProjectId(projectId);
    doc3.setDatasetId(datasetId);
    doc3.setDataType("Chat/Text");
    doc3.setNumTokens(3);
    doc3.setAutoTagCount(1);
    doc3.setAutoTagString("cancel reservation");
    doc3.setTextStringOriginal("[noise] cancel the reservation [faded_noise]");
    doc3.setTextStringForTagging("cancel the reservation");
    doc3.setDocumentType(DocumentType.ORIGINAL.type());
    doc3.setTranscriptionHash(DigestUtils.sha1Hex(doc3.getTextStringForTagging()));
    documentList.add(doc3);

    return documentList;
  }

  public List<TranscriptionDocumentForIndexing> getMakeReservationHash2Documents(
      String jobId, int clientId, int projectId, int datasetId) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(6);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("make reservation");
    doc1.setTextStringOriginal("[side_speech] i'd like to make a reservation");
    doc1.setTextStringForTagging("make a uh reservation");
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(doc1.getTextStringForTagging()));
    documentList.add(doc1);

    return documentList;
  }

  public List<TranscriptionDocumentForIndexing> getMakeHashDocuments(
      String jobId, int clientId, int projectId, int datasetId, String textString) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(6);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("make reservation");
    doc1.setTextStringOriginal("[side_speech] " + textString);
    doc1.setTextStringForTagging(textString);
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(doc1.getTextStringForTagging()));
    documentList.add(doc1);

    return documentList;
  }

  public List<TranscriptionDocumentForIndexing> getPointsBalanceDocuments(
      String jobId, int clientId, int projectId, int datasetId) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    TranscriptionDocumentForIndexing doc2 = new TranscriptionDocumentForIndexing();
    doc2.setJobId(jobId);
    doc2.setVertical("ECOMMERCE");
    doc2.setClientId(clientId);
    doc2.setProjectId(projectId);
    doc2.setDatasetId(datasetId);
    doc2.setDataType("Chat/Text");
    doc2.setNumTokens(2);
    doc2.setAutoTagCount(3);
    doc2.setAutoTagString("balance choice point,choice points balance,points balance choice");
    doc2.setTextStringOriginal("[side_speech] points balance choice points balance");
    doc2.setTextStringForTagging("points balance choice points balance");
    doc2.setDocumentType(DocumentType.ORIGINAL.type());
    doc2.setTranscriptionHash(DigestUtils.sha1Hex(doc2.getTextStringForTagging()));
    documentList.add(doc2);

    return documentList;
  }


  public List<TranscriptionDocumentForIndexing> getAgentRequestDocuments(
      String jobId, int clientId, int projectId, int datasetId) {
    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    final String textString = "I'd like to talk to an agent";
    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical("ECOMMERCE");
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType("Chat/Text");
    doc1.setNumTokens(6);
    doc1.setAutoTagCount(1);
    doc1.setAutoTagString("request agent");
    doc1.setTextStringOriginal(textString);
    doc1.setTextStringForTagging(textString);
    doc1.setDocumentType(DocumentType.ORIGINAL.type());
    doc1.setTranscriptionHash(DigestUtils.sha1Hex(textString));
    documentList.add(doc1);

    return documentList;
  }


  public TranscriptionDocumentForIndexing makeDocument(String jobId, String vertical, int clientId,
      int projectId,
      int datasetId, String dataType, int numTokens,
      int autoTagCount, String autoTagStr, String textOriginal,
      String textTagging, String docType,
      String transcriptionHash) {

    TranscriptionDocumentForIndexing doc1 = new TranscriptionDocumentForIndexing();
    doc1.setJobId(jobId);
    doc1.setVertical(vertical);
    doc1.setClientId(clientId);
    doc1.setProjectId(projectId);
    doc1.setDatasetId(datasetId);
    doc1.setDataType(dataType);
    doc1.setNumTokens(numTokens);
    doc1.setAutoTagCount(autoTagCount);
    doc1.setAutoTagString(autoTagStr);
    doc1.setTextStringOriginal(textOriginal);
    doc1.setTextStringForTagging(textTagging);
    doc1.setDocumentType(docType);
    doc1.setTranscriptionHash(transcriptionHash);

    return doc1;

  }

}