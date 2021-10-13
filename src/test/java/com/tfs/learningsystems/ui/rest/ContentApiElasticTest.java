/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.testutil.DataCreatorUtils;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritance;
import com.tfs.learningsystems.ui.model.DatasetIntentInheritanceStatus;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.rest.ElasticSearchTestUtils.ProjectDatasetSetupResult;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.util.CommonLib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
@FixMethodOrder
public class ContentApiElasticTest extends ElasticApiBaseTest {

  @LocalServerPort
  int localTestPort;

  @MockBean
  @Qualifier("jobManagerBean")
  JobManager jobManager;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;

  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;

  @Autowired
  @Qualifier("dataCreatorUtils")
  private DataCreatorUtils dataCreatorUtils;

  @Autowired
  private AppConfig appConfig;

  @Before
  public void esSetUp() throws IOException {
    this.esTestUtils.refreshAllIndices();
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @After
  public void esTearDown() {
  }

  public ProjectDatasetSetupResult createProjectDatasetSetup() {

    ProjectDatasetSetupResult setup = new ProjectDatasetSetupResult();
    setup.setClientId(Integer.toString(this.clientDetail.getId()));
    setup.setProjectId(Integer.toString(this.projectDetail.getId()));
    setup.setDatasetId(Integer.toString(this.datasetDetail.getId()));
    int jobId = ThreadLocalRandom.current().nextInt(75, 101);
    setup.setJobId("" + jobId);
    setup.setClientIdInt(this.clientDetail.getId());
    setup.setProjectIdInt(this.projectDetail.getId());
    setup.setDatasetIdInt(this.datasetDetail.getId());
    return setup;
  }

  @Test
  public void testIndexIsCreated() {

    String nltoolsAliasName = elasticSearchProps.getNltoolsIndexAlias();
    String intentsAliasName = elasticSearchProps.getClassificationIndexAlias();
    IndicesExistsResponse response = elasticSearchClient.admin().indices()
        .prepareExists(nltoolsAliasName, intentsAliasName)
        .get(TimeValue.timeValueMillis(1000));
    assertTrue(response.isExists());
  }

  @Test
  public void testAddIntentsByTranscriptionHash() {

    try {
      this.setProjectDatasetTransformed();

      String jobId = dataCreatorUtils.createRandomIdStr(5);
      int clientId = dataCreatorUtils.createRandomId(5);
      int projectId = dataCreatorUtils.createRandomId(5);
      int datasetId = dataCreatorUtils.createRandomId(5);

      List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

      List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
          .getMakeReservationHash1Documents(jobId, clientId,
              projectId, datasetId);

      allDocuments.addAll(makeResHash1Documents);

      List<TranscriptionDocumentForIndexing> makeResHash2Documents = this.esTestUtils
          .getMakeReservationHash2Documents(jobId, clientId,
              projectId, datasetId);

      allDocuments.addAll(makeResHash2Documents);

      List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
          .getPointsBalanceDocuments(jobId, clientId,
              projectId, datasetId);
      allDocuments.addAll(pointsbalanceDocuments);

      String indexUrl = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId);
      ResponseEntity<Void> indexEntity = this.restTemplate
          .postForEntity(indexUrl, allDocuments, Void.class);

      Thread.sleep(20000);

      assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      AddIntentRequest addIntentRequest = dataCreatorUtils.createRandomIntentRequest();
      List<String> transcriptionHashList = new ArrayList<>();
      transcriptionHashList.add(makeResHash1Documents.get(0).getTranscriptionHash());
      transcriptionHashList.add(makeResHash2Documents.get(0).getTranscriptionHash());
      addIntentRequest.setTranscriptionHashList(transcriptionHashList);

      String tagUrl = String.format("%scontent/%d/tag", basePath, projectId);
      ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
          .postForEntity(tagUrl, addIntentRequest, UpdateIntentResponse.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, entity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(true);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));
      searchRequest.setFilter(filter);

      String searchUrl = String.format("%ssearch/%s", basePath, projectId);
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
          .postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      assertEquals(2, documentCollection.getTotal().longValue());
      List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
      taggedDocuments.stream()
          .forEach(doc -> assertEquals(addIntentRequest.getIntent(), doc.getIntent()));

    } catch (Exception ex) {
      fail();
    }

  }

  @Test
  public void testAddIntentsByTranscriptionHashMultiDatasetWithDatasetMissing() {

    try {

      int projectId = dataCreatorUtils.createRandomId(5);
      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(true);
      filter.setUntagged(false);
      searchRequest.setFilter(filter);

      String searchUrl = String.format("%ssearch/%s", basePath, projectId);

      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
              .postForEntity(searchUrl, searchRequest,
                      TranscriptionDocumentDetailCollection.class);

      Thread.sleep(50000);
    } catch (Exception ex) {
      fail();
    }
  }

  @Test
  public void testAddIntentsByTranscriptionHashMultiDataset() {

    try {

      this.setProjectDatasetTransformed();
      int clientId = dataCreatorUtils.createRandomId(5);
      int projectId = dataCreatorUtils.createRandomId(5);
      int datasetId1 = dataCreatorUtils.createRandomId(5);
      int datasetId2 = dataCreatorUtils.createRandomId(5);
      int datasetId3 = dataCreatorUtils.createRandomId(5);
      int datasetId4 = dataCreatorUtils.createRandomId(5);

      String jobId1 = dataCreatorUtils.createRandomIdStr(2);

      String jobId2 = dataCreatorUtils.createRandomIdStr(2);

      String jobId3 = dataCreatorUtils.createRandomIdStr(2);

      List<TranscriptionDocumentForIndexing> makeReservationDocuments = this.esTestUtils
          .getMakeReservationHash2Documents(jobId1, clientId, projectId,
              datasetId1);

      List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
          .getPointsBalanceDocuments(jobId2, clientId, projectId,
              datasetId2);

      List<TranscriptionDocumentForIndexing> agentRequestDocuments = this.esTestUtils
          .getAgentRequestDocuments(jobId3, clientId, projectId, datasetId3);

      List<TranscriptionDocumentForIndexing> makeReservationDocumentsOverriden = this.esTestUtils
          .getMakeReservationHash2Documents(jobId1, clientId,
              projectId, datasetId4);

      String indexUrl1 = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId1);

      String indexUrl2 = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId2);

      String indexUrl3 = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId3);

      String indexUrl4 = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId4);

      ResponseEntity<Void> indexEntity1 = this.restTemplate
          .postForEntity(indexUrl1, makeReservationDocuments, Void.class);

      Thread.sleep(20000);

      assertEquals(HttpStatus.OK, indexEntity1.getStatusCode());

      ResponseEntity<Void> indexEntity2 = this.restTemplate
          .postForEntity(indexUrl2, pointsbalanceDocuments, Void.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, indexEntity2.getStatusCode());

      ResponseEntity<Void> indexEntity3 = this.restTemplate
          .postForEntity(indexUrl3, agentRequestDocuments, Void.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, indexEntity3.getStatusCode());

      ResponseEntity<Void> indexEntity4 = this.restTemplate
          .postForEntity(indexUrl4, makeReservationDocumentsOverriden, Void.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, indexEntity4.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();
      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      AddIntentRequest addIntentRequest = dataCreatorUtils.createRandomIntentRequest();

      List<String> transcriptionHashList = new ArrayList<>();
      transcriptionHashList.add(makeReservationDocuments.get(0).getTranscriptionHash());
      transcriptionHashList.add(pointsbalanceDocuments.get(0).getTranscriptionHash());
      addIntentRequest.setTranscriptionHashList(transcriptionHashList);

      String tagUrl = String.format("%scontent/%d/tag", basePath, projectId);
      ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
          .postForEntity(tagUrl, addIntentRequest, UpdateIntentResponse.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, entity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();
      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      List<String> datasetsId = new ArrayList<>();
      datasetsId.add(String.valueOf(datasetId1));
      datasetsId.add(String.valueOf(datasetId2));
      datasetsId.add(String.valueOf(datasetId3));
      datasetsId.add(String.valueOf(datasetId4));
      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(true);
      filter.setUntagged(false);
      filter.setDatasets(datasetsId);
      searchRequest.setFilter(filter);

      String searchUrl = String.format("%ssearch/%s", basePath, projectId);

      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
          .postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);

      Thread.sleep(50000);
      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();

      Thread.sleep(90000);

      assertEquals(2, documentCollection.getTotal().longValue());
      List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
      taggedDocuments.stream()
          .forEach(doc -> assertEquals(addIntentRequest.getIntent(), doc.getIntent()));

    } catch (Exception ex) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testAddIntentsByTranscriptionHashWithMetadataInGuide() {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();

    int clientId = dataCreatorUtils.createRandomId(5);
    int projectId = dataCreatorUtils.createRandomId(5);
    int datasetId = dataCreatorUtils.createRandomId(5);

    String jobId = dataCreatorUtils.createRandomIdStr(5);

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
        .getPointsBalanceDocuments(jobId, clientId, projectId, datasetId);
    allDocuments.addAll(pointsbalanceDocuments);

    String indexUrl = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId);
    ResponseEntity<Void> indexEntity = this.restTemplate
        .postForEntity(indexUrl, allDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setUsername("john.doe");
    addIntentRequest.setIntent("reservation-make");
    List<String> transcriptionHashList = new ArrayList<>();
    transcriptionHashList.add(pointsbalanceDocuments.get(0).getTranscriptionHash());
    addIntentRequest.setTranscriptionHashList(transcriptionHashList);

    String tagUrl = String.format("%scontent/%d/tag", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
        .postForEntity(tagUrl, addIntentRequest, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    // Test that meta data was saved
    String intentsGuideURL = String
        .format("%ssearch/%s/intentguide?sortBy=count:desc", basePath, projectId);

    ResponseEntity<TaggingGuideDocumentDetail[]> taggingGuideResponse = this.restTemplate
        .getForEntity(intentsGuideURL,
            TaggingGuideDocumentDetail[].class);
    assertEquals(HttpStatus.OK, taggingGuideResponse.getStatusCode());
    TaggingGuideDocument[] body = taggingGuideResponse.getBody();
    Arrays.stream(body).forEach(doc -> assertFalse(doc.getExamples().isEmpty()));
    Arrays.stream(body).forEach(doc -> assertFalse(doc.getKeywords().isEmpty()));

  }

  @Test
  public void testUpdateIntentsByTranscriptionHash() {

    try {
      this.setProjectDatasetTransformed();

      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      int clientId = setup.getClientIdInt();
      int projectId = setup.getProjectIdInt();
      int datasetId = setup.getDatasetIdInt();

      String jobId = setup.getJobId();

      List<TranscriptionDocumentForIndexing> allDocuments = this.esTestUtils
          .getMakeReservationHash1Documents(jobId, clientId, projectId, datasetId);

      String hashToUpdateIntent = allDocuments.get(0).getTranscriptionHash();

      String indexUrl = String
          .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
              datasetId);
      ResponseEntity<Void> indexEntity = this.restTemplate
          .postForEntity(indexUrl, allDocuments, Void.class);

      Thread.sleep(20000);

      assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      AddIntentRequest addIntentRequest = new AddIntentRequest();
      addIntentRequest.setUsername("john.doe");
      addIntentRequest.setIntent("reservation-make");
      List<String> transcriptionHashList = new ArrayList<>();
      transcriptionHashList.add(hashToUpdateIntent);
      addIntentRequest.setTranscriptionHashList(transcriptionHashList);

      String tagUrl = String.format("%scontent/%d/tag", basePath, projectId, datasetId);
      ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
          .postForEntity(tagUrl, addIntentRequest, UpdateIntentResponse.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, entity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(true);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));

      searchRequest.setFilter(filter);

      String searchUrl = String.format("%ssearch/%s", basePath, projectId);
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
          .postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      assertEquals(documentCollection.getTotal().longValue(), 1);
      List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
      taggedDocuments.stream().forEach(doc -> assertEquals(doc.getIntent(), "reservation-make"));

      AddIntentRequest updateIntentRequest = new AddIntentRequest();
      updateIntentRequest.setUsername("man.mar");
      updateIntentRequest.setIntent("res-change");
      List<String> updateTranscriptionHashList = new ArrayList<>();
      updateTranscriptionHashList.add(hashToUpdateIntent);
      updateIntentRequest.setTranscriptionHashList(updateTranscriptionHashList);

      String updateTagUrl = String.format("%scontent/%d/tag/update", basePath, projectId);
      ResponseEntity<UpdateIntentResponse> updateEntity = this.restTemplate
          .postForEntity(updateTagUrl, updateIntentRequest, UpdateIntentResponse.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, updateEntity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      Thread.sleep(appConfig.getTestCaseTimeout());

      searchEntity = this.restTemplate
          .postForEntity(searchUrl, searchRequest, TranscriptionDocumentDetailCollection.class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      searchEntity.getBody().getTranscriptionList().stream()
          .forEach(doc -> assertEquals("res-change", doc.getIntent()));

    } catch (Exception ex) {
      fail();
    }
  }

  @Test
  public void testDeleteRecords() {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int clientId = dataCreatorUtils.createRandomId(5);
    int projectId = dataCreatorUtils.createRandomId(5);
    int datasetId = dataCreatorUtils.createRandomId(5);

    String jobId = dataCreatorUtils.createRandomIdStr(5);

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
        .getMakeReservationHash1Documents(jobId, clientId, projectId,
            datasetId);
    allDocuments.addAll(makeResHash1Documents);

    List<TranscriptionDocumentForIndexing> makeResHash2Documents = this.esTestUtils
        .getMakeReservationHash2Documents(jobId, clientId, projectId,
            datasetId);
    allDocuments.addAll(makeResHash2Documents);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
        .getPointsBalanceDocuments(jobId, clientId, projectId, datasetId);
    allDocuments.addAll(pointsbalanceDocuments);

    String indexUrl = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId);
    ResponseEntity<Void> indexEntity = this.restTemplate
        .postForEntity(indexUrl, allDocuments, Void.class);

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    String deleteUrl = String.format("%scontent/%d/datasets/%d", basePath, projectId, datasetId);
    ResponseEntity<Void> deleteEntity = this.restTemplate
        .exchange(deleteUrl, HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteEntity.getStatusCode());
  }

  @Test
  public void testAddCommentByTranscriptionHash() throws InterruptedException {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int clientId = dataCreatorUtils.createRandomId(5);
    int projectId = dataCreatorUtils.createRandomId(5);
    int datasetId = dataCreatorUtils.createRandomId(5);

    String jobId = dataCreatorUtils.createRandomIdStr(5);

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeHash1Documents = this.esTestUtils
        .getMakeHashDocuments(jobId, clientId, projectId, datasetId,
            "test add comment transcription 1");
    allDocuments.addAll(makeHash1Documents);

    List<TranscriptionDocumentForIndexing> makeHash2Documents = this.esTestUtils
        .getMakeHashDocuments(jobId, clientId, projectId, datasetId,
            "test add comment transcription 2");
    allDocuments.addAll(makeHash2Documents);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
        .getPointsBalanceDocuments(jobId, clientId, projectId, datasetId);
    allDocuments.addAll(pointsbalanceDocuments);

    String indexUrl = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId);
    ResponseEntity<Void> indexEntity = this.restTemplate
        .postForEntity(indexUrl, allDocuments, Void.class);

    Thread.sleep(5000);

    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddCommentRequest addCommentRequest = new AddCommentRequest();
    addCommentRequest.setUsername("john.doe");
    addCommentRequest.setComment("Test Comment!");
    List<String> transcriptionHashList = new ArrayList<>();
    transcriptionHashList.add(makeHash1Documents.get(0).getTranscriptionHash());
    transcriptionHashList.add(makeHash2Documents.get(0).getTranscriptionHash());
    addCommentRequest.setTranscriptionHashList(transcriptionHashList);

    String tagUrl = String.format("%scontent/%d/comment", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
        .postForEntity(tagUrl, addCommentRequest, UpdateIntentResponse.class);
    Thread.sleep(2000);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("Expected to create 2 commented items", 2,
        entity.getBody().getTotalCount().intValue());

    this.esTestUtils.refreshNltoolsIndex();

    SearchRequest searchRequest = new SearchRequest();
    SearchRequestFilter filter = new SearchRequestFilter();
    filter.hasComment(true);
    filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));
    searchRequest.setFilter(filter);

    String searchUrl = String.format("%ssearch/%s", basePath, projectId);
    ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
        .postForEntity(searchUrl, searchRequest,
            TranscriptionDocumentDetailCollection.class);
    assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
    TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
    //        assertEquals("Expected search to return 2 commented items", 2, documentCollection.getTotal().longValue());
    List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
    taggedDocuments.stream().forEach(doc -> assertEquals(doc.getComment(), "Test Comment!"));
  }

  @Test
  public void testAddOver1000CommentsByTranscriptionHash() {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int clientId = dataCreatorUtils.createRandomId(5);
    int projectId = dataCreatorUtils.createRandomId(5);
    int datasetId = dataCreatorUtils.createRandomId(5);

    String jobId = dataCreatorUtils.createRandomIdStr(5);
    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();
    List<String> allDocumentHashs = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeHash1Documents;
    for (int i = 0; i <= 1024; i++) {

      makeHash1Documents = this.esTestUtils
          .getMakeHashDocuments(jobId, clientId, projectId, datasetId,
              "test add over 1000 comments transcription " + i);
      allDocuments.addAll(makeHash1Documents);
      allDocumentHashs.add(makeHash1Documents.get(0).getTranscriptionHash());
    }

    String indexUrl = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId);
    ResponseEntity<Void> indexEntity = this.restTemplate
        .postForEntity(indexUrl, allDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddCommentRequest addCommentRequest = new AddCommentRequest();
    addCommentRequest.setUsername("john.doe");
    addCommentRequest.setComment("Test Comment!");
    addCommentRequest.setTranscriptionHashList(allDocumentHashs);

    String commentUrl = String.format("%scontent/%d/comment", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
        .postForEntity(commentUrl, addCommentRequest, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(allDocumentHashs.size(), entity.getBody().getTotalCount().intValue());

    this.esTestUtils.refreshNltoolsIndex();

    SearchRequest searchRequest = new SearchRequest();
    SearchRequestFilter filter = new SearchRequestFilter();
    filter.hasComment(true);
    filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));

    searchRequest.setFilter(filter);

    String searchUrl = String.format("%ssearch/%s", basePath, projectId);
    ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
        .postForEntity(searchUrl, searchRequest,
            TranscriptionDocumentDetailCollection.class);
    assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
    TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
    // assertEquals(allDocumentHashs.size(), documentCollection.getTotal().longValue());
    List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
    taggedDocuments.stream().forEach(doc -> assertEquals(doc.getComment(), "Test Comment!"));
  }

  @Test
  public void testRemoveCommentByTranscriptionHash() throws InterruptedException {

    this.setProjectDatasetTransformed();

    //   ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int clientId = dataCreatorUtils.createRandomId(5);
    int projectId = dataCreatorUtils.createRandomId(5);
    int datasetId = dataCreatorUtils.createRandomId(5);

    String jobId = dataCreatorUtils.createRandomIdStr(5);

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeHash1Documents = this.esTestUtils
        .getMakeHashDocuments(jobId, clientId, projectId, datasetId,
            "test remove comment transcription 1");
    allDocuments.addAll(makeHash1Documents);

    List<TranscriptionDocumentForIndexing> makeHash2Documents = this.esTestUtils
        .getMakeHashDocuments(jobId, clientId, projectId, datasetId,
            "test remove comment transcription 2");
    allDocuments.addAll(makeHash2Documents);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
        .getPointsBalanceDocuments(jobId, clientId, projectId, datasetId);
    allDocuments.addAll(pointsbalanceDocuments);

    String indexUrl = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId);

    ResponseEntity<Void> indexEntity = this.restTemplate
        .postForEntity(indexUrl, allDocuments, Void.class);

    Thread.sleep(5000);

    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddCommentRequest addCommentRequest = new AddCommentRequest();
    addCommentRequest.setUsername("john.doe");
    addCommentRequest.setComment("Test Comment!");
    List<String> transcriptionHashList = new ArrayList<>();
    transcriptionHashList.add(makeHash1Documents.get(0).getTranscriptionHash());
    transcriptionHashList.add(makeHash2Documents.get(0).getTranscriptionHash());
    addCommentRequest.setTranscriptionHashList(transcriptionHashList);

    String tagUrl = String.format("%scontent/%d/comment", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity = this.restTemplate
        .postForEntity(tagUrl, addCommentRequest, UpdateIntentResponse.class);
    Thread.sleep(2000);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("Expected to create 2 comments", 2, entity.getBody().getTotalCount().intValue());

    this.esTestUtils.refreshNltoolsIndex();

    SearchRequest searchRequest = new SearchRequest();
    SearchRequestFilter filter = new SearchRequestFilter();
    filter.hasComment(true);
    filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));

    searchRequest.setFilter(filter);

    String searchUrl = String.format("%ssearch/%s", basePath, projectId);
    ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = this.restTemplate
        .postForEntity(searchUrl, searchRequest,
            TranscriptionDocumentDetailCollection.class);
    assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
    TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
    // assertEquals("Expected search to return 2 commented items", 2, documentCollection.getTotal().longValue());
    List<TranscriptionDocumentDetail> taggedDocuments = documentCollection.getTranscriptionList();
    taggedDocuments.stream().forEach(doc -> assertEquals(doc.getComment(), "Test Comment!"));

    // TEST REMOVING COMMENT
    AddCommentRequest removeCommentRequest = new AddCommentRequest();
    removeCommentRequest.setUsername("john.doe");
    removeCommentRequest.setComment("");
    List<String> removeTranscriptionHashList = new ArrayList<>();
    removeTranscriptionHashList.add(makeHash1Documents.get(0).getTranscriptionHash());
    removeCommentRequest.setTranscriptionHashList(removeTranscriptionHashList);

    ResponseEntity<UpdateIntentResponse> removeEntity = this.restTemplate
        .postForEntity(tagUrl, removeCommentRequest, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, removeEntity.getStatusCode());
    assertEquals("Expected to remove 1 comment", 1,
        removeEntity.getBody().getTotalCount().intValue());

    this.esTestUtils.refreshNltoolsIndex();

    ResponseEntity<TranscriptionDocumentDetailCollection> removeSearchEntity = this.restTemplate
        .postForEntity(searchUrl, searchRequest,
            TranscriptionDocumentDetailCollection.class);
    assertEquals(HttpStatus.OK, removeSearchEntity.getStatusCode());
    TranscriptionDocumentDetailCollection removeDocumentCollection = removeSearchEntity.getBody();
    assertNotNull(removeDocumentCollection);
    //        assertEquals("Expected search to return 1 commented items", 1, removeDocumentCollection.getTotal().longValue());
  }

  @Ignore
  @Test
  public void testAddNewIntent() {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();

    int projectId = dataCreatorUtils.createRandomId(5);

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setComments("comment1\ncomment2");

    String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);
    ResponseEntity<TaggingGuideDocument> intentEntity = this.restTemplate
        .postForEntity(addIntentUrl, doc, TaggingGuideDocument.class);

    TaggingGuideDocument addedDoc = intentEntity.getBody();
    assertNotNull(addedDoc.getId());
    assertNotNull(addedDoc.getIntent());
    assertEquals(addedDoc.getIntent(), "reservation-make");

  }

  @Ignore
  @Test
  public void testAddNewRuTag() {

    this.setProjectDatasetTransformed();

    //   ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int projectId = dataCreatorUtils.createRandomId(5);

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setRutag("reservation-rutag");
    doc.setComments("comment1\ncomment2");

    String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);
    ResponseEntity<TaggingGuideDocument> intentEntity = this.restTemplate
        .postForEntity(addIntentUrl, doc, TaggingGuideDocument.class);

    TaggingGuideDocument addedDoc = intentEntity.getBody();
    assertNotNull(addedDoc.getId());
    assertNotNull(addedDoc.getRutag());
    assertEquals(addedDoc.getRutag(), "reservation-rutag");
  }

  @Test
  public void testAddDuplicateIntent() {

    try {
      this.setProjectDatasetTransformed();
      int projectId = dataCreatorUtils.createRandomId(5);

      TaggingGuideDocument doc1 = new TaggingGuideDocument();
      String intent = dataCreatorUtils.createRandomIntent();

      dataCreatorUtils.createRandomIntent();
      doc1.setIntent(intent);
      doc1.setComments(dataCreatorUtils.createRandomStringData());

      String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);

      ResponseEntity<TaggingGuideDocument> intentEntity1 = this.restTemplate
          .postForEntity(addIntentUrl, doc1, TaggingGuideDocument.class);
      Thread.sleep(appConfig.getTestCaseTimeout());

      TaggingGuideDocument addedDoc1 = intentEntity1.getBody();
      assertNotNull(addedDoc1.getId());

      this.esTestUtils.refreshClassificationIndex();

      TaggingGuideDocument doc2 = new TaggingGuideDocument();
      doc2.setIntent(intent);

      ResponseEntity<Error> intentEntity2 = this.restTemplate
          .postForEntity(addIntentUrl, doc2, Error.class);
      assertEquals(intentEntity2.getStatusCode(), HttpStatus.CONFLICT);
    } catch (Exception ex) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testAddDuplicateIntentCaseInsensitive() {

    try {
      this.setProjectDatasetTransformed();

      //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();

      int projectId = dataCreatorUtils.createRandomId(5);

      TaggingGuideDocument doc1 = new TaggingGuideDocument();
      doc1.setIntent("reservation-make");
      doc1.setComments("comment1\ncomment2");

      String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);

      ResponseEntity<TaggingGuideDocument> intentEntity1 = this.restTemplate
          .postForEntity(addIntentUrl, doc1, TaggingGuideDocument.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      TaggingGuideDocument addedDoc1 = intentEntity1.getBody();
      assertNotNull(addedDoc1.getId());

      this.esTestUtils.refreshClassificationIndex();

      TaggingGuideDocument doc2 = new TaggingGuideDocument();
      doc2.setIntent("RESERVATION-MAKE");

      ResponseEntity<Error> intentEntity2 = this.restTemplate
          .postForEntity(addIntentUrl, doc2, Error.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.CONFLICT, intentEntity2.getStatusCode());

    } catch (Exception ex) {
      fail();
    }
  }

  @Ignore
  @Test
  public void testUpdateIntent() throws JsonProcessingException {

    this.setProjectDatasetTransformed();

    //  ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int projectId = dataCreatorUtils.createRandomId(5);

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setRutag("reservation-rutag");
    doc.setDescription("description");

    String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);
    ResponseEntity<TaggingGuideDocument> intentEntity = this.restTemplate
        .postForEntity(addIntentUrl, doc, TaggingGuideDocument.class);

    TaggingGuideDocument addedDoc = intentEntity.getBody();
    assertNotNull(addedDoc.getId());

    ArrayList<PatchDocument> patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/intent");
    patchDocument.setValue("reservation-cancel");

    patchRequest.add(patchDocument);

    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

    String updateIntentUrl = String
        .format("%scontent/%s/intents/%s", basePath, projectId, addedDoc.getId());
    ResponseEntity<TaggingGuideDocument> updatedEntity = this.restTemplate
        .exchange(updateIntentUrl, HttpMethod.PATCH, httpEntity,
            TaggingGuideDocument.class);
    TaggingGuideDocument updatedDoc = updatedEntity.getBody();
    assertNotNull(updatedDoc);

    assertEquals(updatedDoc.getIntent(), "reservation-cancel");
  }

  @Test
  public void testUpdateRutag() throws JsonProcessingException {

    this.setProjectDatasetTransformed();

    //   ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int projectId = dataCreatorUtils.createRandomId(5);

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setRutag("reservation-rutag");
    doc.setDescription("description");

    String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);
    ResponseEntity<TaggingGuideDocument> intentEntity = this.restTemplate
        .postForEntity(addIntentUrl, doc, TaggingGuideDocument.class);

    TaggingGuideDocument addedDoc = intentEntity.getBody();
    assertNotNull(addedDoc.getId());

    ArrayList<PatchDocument> patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/rutag");
    patchDocument.setValue("reservation-rutagnew");

    patchRequest.add(patchDocument);

    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

    String updateIntentUrl = String
        .format("%scontent/%s/intents/%s", basePath, projectId, addedDoc.getId());
    ResponseEntity<TaggingGuideDocument> updatedEntity = this.restTemplate
        .exchange(updateIntentUrl, HttpMethod.PATCH, httpEntity,
            TaggingGuideDocument.class);
    TaggingGuideDocument updatedDoc = updatedEntity.getBody();
    assertNotNull(updatedDoc);
    assertEquals(updatedDoc.getRutag(), "reservation-rutagnew");
  }

  @Ignore
  @Test
  public void testDeleteIntent() {

    this.setProjectDatasetTransformed();

    //    ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
    int projectId = dataCreatorUtils.createRandomId(5);

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");

    String addIntentUrl = String.format("%scontent/%s/intents", basePath, projectId);
    ResponseEntity<TaggingGuideDocument> intentEntity = this.restTemplate
        .postForEntity(addIntentUrl, doc, TaggingGuideDocument.class);

    TaggingGuideDocument addedDoc = intentEntity.getBody();
    assertNotNull(addedDoc.getId());

    String deleteIntentUrl = String
        .format("%scontent/%s/intents/%s", basePath, projectId, addedDoc.getId());
    this.restTemplate.delete(deleteIntentUrl);
  }

  @Ignore
  @Test
  public void testDatasetIntentInheritance() {

    this.setProjectDatasetTransformed();

    int projectId = dataCreatorUtils.createRandomId(5);
    int clientId = dataCreatorUtils.createRandomId(5);
    int datasetId1 = dataCreatorUtils.createRandomId(5);
    int datasetId2 = dataCreatorUtils.createRandomId(5);
    int datasetId3 = dataCreatorUtils.createRandomId(5);
    int datasetId4 = dataCreatorUtils.createRandomId(5);
    int datasetId5 = dataCreatorUtils.createRandomId(5);

    List<TranscriptionDocumentForIndexing> makeReservationDocuments = this.esTestUtils
        .getMakeReservationHash2Documents("1", clientId, projectId,
            datasetId1);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments = this.esTestUtils
        .getPointsBalanceDocuments("2", clientId, projectId, datasetId2);

    List<TranscriptionDocumentForIndexing> agentRequestDocuments = this.esTestUtils
        .getAgentRequestDocuments("3", clientId, projectId, datasetId3);

    List<TranscriptionDocumentForIndexing> makeReservationDocumentsOverriden = this.esTestUtils
        .getMakeReservationHash2Documents("1", clientId, projectId,
            datasetId4);

    String indexUrl1 = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId1);

    String indexUrl2 = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId2);

    String indexUrl3 = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId3);

    String indexUrl4 = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId4);

    ResponseEntity<Void> indexEntity1 = this.restTemplate
        .postForEntity(indexUrl1, makeReservationDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity1.getStatusCode());

    ResponseEntity<Void> indexEntity2 = this.restTemplate
        .postForEntity(indexUrl2, pointsbalanceDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity2.getStatusCode());

    ResponseEntity<Void> indexEntity3 = this.restTemplate
        .postForEntity(indexUrl3, agentRequestDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity3.getStatusCode());

    ResponseEntity<Void> indexEntity4 = this.restTemplate
        .postForEntity(indexUrl4, makeReservationDocumentsOverriden, Void.class);
    assertEquals(HttpStatus.OK, indexEntity4.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddIntentRequest addIntentRequest1 = new AddIntentRequest();
    addIntentRequest1.setUsername("john.doe");
    addIntentRequest1.setIntent("reservation-make");
    List<String> transcriptionHashList1 = new ArrayList<>();
    transcriptionHashList1.add(makeReservationDocuments.get(0).getTranscriptionHash());
    addIntentRequest1.setTranscriptionHashList(transcriptionHashList1);

    String tagUrl1 = String.format("%scontent/%d/datasets/%d/tag", basePath, projectId, datasetId1);
    ResponseEntity<UpdateIntentResponse> tagEntity1 = this.restTemplate
        .postForEntity(tagUrl1, addIntentRequest1, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, tagEntity1.getStatusCode());

    AddIntentRequest addIntentRequest2 = new AddIntentRequest();
    addIntentRequest2.setUsername("john.doe");
    addIntentRequest2.setIntent("pointsbalance-choice");
    List<String> transcriptionHashList2 = new ArrayList<>();
    transcriptionHashList2.add(pointsbalanceDocuments.get(0).getTranscriptionHash());
    addIntentRequest2.setTranscriptionHashList(transcriptionHashList2);

    String tagUrl2 = String.format("%scontent/%d/datasets/%d/tag", basePath, projectId, datasetId2);
    ResponseEntity<UpdateIntentResponse> tagEntity2 = this.restTemplate
        .postForEntity(tagUrl2, addIntentRequest2, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, tagEntity2.getStatusCode());

    AddIntentRequest addIntentRequest3 = new AddIntentRequest();
    addIntentRequest3.setUsername("john.doe");
    addIntentRequest3.setIntent("agent-request");
    List<String> transcriptionHashList3 = new ArrayList<>();
    transcriptionHashList3.add(agentRequestDocuments.get(0).getTranscriptionHash());
    addIntentRequest3.setTranscriptionHashList(transcriptionHashList3);

    String tagUrl3 = String.format("%scontent/%d/datasets/%d/tag", basePath, projectId, datasetId3);
    ResponseEntity<UpdateIntentResponse> tagEntity3 = this.restTemplate
        .postForEntity(tagUrl3, addIntentRequest3, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, tagEntity3.getStatusCode());

    AddIntentRequest addIntentRequest4 = new AddIntentRequest();
    addIntentRequest4.setUsername("john.doe");
    addIntentRequest4.setIntent("reservation-confirm");
    List<String> transcriptionHashList4 = new ArrayList<>();
    transcriptionHashList4.add(makeReservationDocumentsOverriden.get(0).getTranscriptionHash());
    addIntentRequest4.setTranscriptionHashList(transcriptionHashList4);

    String tagUrl4 = String.format("%scontent/%d/datasets/%d/tag", basePath, projectId, datasetId4);
    ResponseEntity<UpdateIntentResponse> tagEntity4 = this.restTemplate
        .postForEntity(tagUrl4, addIntentRequest4, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, tagEntity4.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    allDocuments
        .addAll(this.esTestUtils.getAgentRequestDocuments("5", clientId, projectId, datasetId5));
    allDocuments.addAll(
        this.esTestUtils.getMakeReservationHash2Documents("5", clientId, projectId, datasetId5));

    String indexUrl5 = String
        .format("%scontent/%d/projects/%d/datasets/%d/index", basePath, clientId, projectId,
            datasetId5);

    ResponseEntity<Void> indexEntity5 = this.restTemplate
        .postForEntity(indexUrl5, allDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity5.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    List<Integer> projectDatasetIds = new ArrayList<>();
    projectDatasetIds.add(datasetId1);
    projectDatasetIds.add(datasetId2);
    projectDatasetIds.add(datasetId3);
    projectDatasetIds.add(datasetId4);
    projectDatasetIds.add(datasetId5);

    DatasetIntentInheritance datasetIntentInheritance = new DatasetIntentInheritance();
    datasetIntentInheritance.setDatasetId("5");
    datasetIntentInheritance.setProjectId("" + projectId);
    datasetIntentInheritance.setRequestedBy("john.doe");
    datasetIntentInheritance.setStatus(DatasetIntentInheritanceStatus.PENDING);

    Mockito.doReturn(projectDatasetIds).when(this.projectDatasetManager)
        .listDatasetIdsByProjectId(anyString());

    Mockito.doReturn(datasetIntentInheritance).when(this.datasetManager)
        .getLastPendingInheritaceForDataset(anyString());

    Mockito.doReturn(datasetIntentInheritance).when(this.datasetManager)
        .updateIntentInheritance((DatasetIntentInheritance) anyObject());

    try {
      ContentManager.SeedIntentDocumentsStats seedStats = this.contentManager
          .seedIntentDocuments(clientId, projectId, datasetId5, "john.doe");
      assertEquals(2, seedStats.getTotalSeeded());
      assertEquals(2, seedStats.getTotalTagged());
      assertEquals(2, seedStats.getUniqueTagged());
      assertEquals(1, seedStats.getTotalTaggedMultipleIntents());
      assertEquals(1, seedStats.getUniqueTaggedMultipleIntents());
    } catch (ApplicationException e) {
      fail();
    }
  }
}