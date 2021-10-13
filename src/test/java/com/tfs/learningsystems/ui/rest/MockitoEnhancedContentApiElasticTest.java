/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;

import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideManager;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MockitoEnhancedContentApiElasticTest extends ElasticApiBaseTest {

  @LocalServerPort
  int localTestPort;

  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;

  @MockBean
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @MockBean
  @Qualifier("taggingGuideManager")
  private TaggingGuideManager taggingGuideManager;

  @MockBean
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Before
  public void esSetUp() throws IOException {
    this.esTestUtils.refreshAllIndices();
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @After
  public void esTearDown() {
  }

  @Test
  public void testDeleteIntentGuideFromProjectNoNodeAvailableException() {
    this.setProjectValid();

    try {
      Mockito.doThrow(new NoNodeAvailableException("No node available"))
          .when(this.contentManager)
          .deleteProjectIntentsFromGuide(Mockito.any(),Mockito.any());

      ResponseEntity<Void> deleteResponse = this.restTemplate.exchange(
          basePath + "content/" + this.projectDetail.getId() + "/intentguide/",
          HttpMethod.DELETE, null, Void.class);
      assertEquals(HttpStatus.SERVICE_UNAVAILABLE, deleteResponse.getStatusCode());

    } catch (ApplicationException e) {
      fail();
    }
  }

  @Test
  public void testDeleteIntentGuideFromProjectApplicationException() {
    this.setProjectValid();

    try {
      Mockito.doThrow(new ApplicationException())
          .when(this.contentManager)
          .deleteProjectIntentsFromGuide(Mockito.any(),Mockito.any());

      ResponseEntity<Void> deleteResponse = this.restTemplate.exchange(
          basePath + "content/" + this.projectDetail.getId() + "/intentguide/",
          HttpMethod.DELETE, null, Void.class);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, deleteResponse.getStatusCode());

    } catch (ApplicationException e) {
      fail();
    }
  }

  @Test
  public void testIndexNewDocumentsInvalidProjectDataset() {
    this.setProjectDatasetNotAssociated();

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();
    String indexUrl = String.format("%scontent/%s/projects/%s/datasets/%s/index",
        basePath, this.clientDetail.getId(), this.projectDetail.getId(),
        this.datasetDetail.getId());
    ResponseEntity<Error> indexEntity = this.restTemplate.postForEntity(
        indexUrl, allDocuments, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, indexEntity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, indexEntity.getBody().getMessage());
  }

  @Test
  public void testIndexNewDocumentsNoTransformedDataset() {
    this.setProjectDatasetNotTransformed();

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();
    String indexUrl = String.format("%scontent/%s/projects/%s/datasets/%s/index",
        basePath, this.clientDetail.getId(), this.projectDetail.getId(),
        this.datasetDetail.getId());
    ResponseEntity<Error> indexEntity = this.restTemplate.postForEntity(
        indexUrl, allDocuments, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, indexEntity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, indexEntity.getBody().getMessage());
  }

  @Test
  public void testIndexNewDocumentsIndexNotFoundException() {
    this.setProjectDatasetTransformed();

    try {
      Mockito.doThrow(new IndexNotFoundException("Index nltools not Found"))
          .when(this.contentManager)
          .indexNewTranscriptions(anyString(), anyString(), anyString(),
              anyListOf(TranscriptionDocumentForIndexing.class),
              anyString());
      List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();
      String indexUrl = String.format("%scontent/%s/projects/%s/datasets/%s/index",
          basePath, this.clientDetail.getId(), this.projectDetail.getId(),
          this.datasetDetail.getId());
      ResponseEntity<Error> indexEntity = this.restTemplate.postForEntity(
          indexUrl, allDocuments, Error.class);
      assertEquals(HttpStatus.SERVICE_UNAVAILABLE, indexEntity.getStatusCode());
    } catch (ApplicationException e) {
      fail();
    }
  }

  @Test
  public void testIndexNewDocumentsApplicationException() {
    this.setProjectDatasetTransformed();

    try {
      Mockito.doThrow(new ApplicationException())
          .when(this.contentManager)
          .indexNewTranscriptions(anyString(), anyString(), anyString(),
              anyListOf(TranscriptionDocumentForIndexing.class), anyString());
      List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();
      String indexUrl = String.format("%scontent/%s/projects/%s/datasets/%s/index",
          basePath, this.clientDetail.getId(), this.projectDetail.getId(),
          this.datasetDetail.getId());
      ResponseEntity<Error> indexEntity = this.restTemplate.postForEntity(
          indexUrl, allDocuments, Error.class);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, indexEntity.getStatusCode());
    } catch (ApplicationException e) {
      fail();
    }
  }

  @Test
  public void testDeleteRecordsInvalidProjectId() {
    this.setProjectInvalid("unknownProjectId");

    String deleteUrl = String.format("%scontent/%s/datasets/%s", basePath,
        "unknownProjectId", this.datasetDetail.getId());
    ResponseEntity<Error> deleteEntity = this.restTemplate.exchange(deleteUrl,
        HttpMethod.DELETE, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, deleteEntity.getStatusCode());
    assertEquals("Project 'unknownProjectId' not found", deleteEntity.getBody().getMessage());
  }

  @Test
  public void testDeleteRecordsProjectDatasetNotAssociated() {
    this.setProjectDatasetNotAssociated();

    String deleteUrl = String.format("%scontent/%s/datasets/%s", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> deleteEntity = this.restTemplate.exchange(deleteUrl,
        HttpMethod.DELETE, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, deleteEntity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, deleteEntity.getBody().getMessage());
  }

  @Test
  public void testDeleteRecordsDatasetNotTransformed() {
    this.setProjectDatasetNotTransformed();

    this.projectManager.addDatasetProjectMapping(Integer.toString(this.projectDetail.getClientId()),
        Integer.toString(this.projectDetail.getId()), Integer.toString(this.datasetDetail.getId()),
        currentUserId);
    String deleteUrl = String.format("%scontent/%s/datasets/%s", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> deleteEntity = this.restTemplate.exchange(deleteUrl,
        HttpMethod.DELETE, null, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, deleteEntity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, deleteEntity.getBody().getMessage());
  }

  @Test
  public void addIntentByTranscriptionHashIndexNotFoundException() {
    this.setProjectDatasetTransformed();

    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setIntent("res-make");
    addIntentRequest.setTranscriptionHashList(null);
    addIntentRequest.setUsername("man.mar");
    try {


      Mockito.doThrow(new IndexNotFoundException("nltools"))
          .when(this.contentManager)
          .addIntentByTranscriptionHashList(Mockito.any(),Mockito.any(), Mockito.any(),Mockito.any(),
              Mockito.any(), Mockito.any(), Mockito.any());

      String indexUrl = String.format("%scontent/%s/datasets/%s/tag",
          basePath, this.projectDetail.getId(), this.datasetDetail.getId());
      ResponseEntity<Error> addIntentEntity = this.restTemplate.postForEntity(
          indexUrl, addIntentRequest, Error.class);
      assertEquals(HttpStatus.SERVICE_UNAVAILABLE, addIntentEntity.getStatusCode());
      assertEquals(ErrorMessage.SEARCH_UNAVAILABLE, addIntentEntity.getBody().getMessage());
    } catch (ApplicationException e) {
      fail();
    }
  }

  @Test
  public void addIntentByTranscriptionHashApplicationException() {
    this.setProjectDatasetTransformed();

    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setIntent("res-make");
    addIntentRequest.setTranscriptionHashList(null);
    addIntentRequest.setUsername("man.mar");
    try {
      Mockito.doThrow(new ApplicationException())
          .when(this.contentManager)
          .addIntentByTranscriptionHashList(Mockito.any(),Mockito.any(), Mockito.any(),Mockito.any(),
              Mockito.any(), Mockito.any(), Mockito.any());

      String indexUrl = String.format("%scontent/%s/datasets/%s/tag",
          basePath, this.projectDetail.getId(), this.datasetDetail.getId());
      ResponseEntity<Error> addIntentEntity = this.restTemplate.postForEntity(
          indexUrl, addIntentRequest, Error.class);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, addIntentEntity.getStatusCode());
    } catch (ApplicationException e) {
      fail();
    }
  }

}
