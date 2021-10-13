/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.config.ElasticSearchPropertyConfig;
import com.tfs.learningsystems.testutil.DataCreatorUtils;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.DocumentType;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.SearchRequestFilter;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.rest.ElasticSearchTestUtils.ProjectDatasetSetupResult;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.util.CommonLib;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SearchApiElasticTest extends ElasticApiBaseTest {

  @LocalServerPort
  int localTestPort;

  @MockBean
  @Qualifier("jobManagerBean")
  JobManager jobManager;

  @Autowired
  private ElasticSearchPropertyConfig elasticSearchProps;

  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;

  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;

  @Inject
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("dataCreatorUtils")
  private DataCreatorUtils dataCreatorUtils;

  @Autowired
  private AppConfig appConfig;

  @Before
  public void esSetUp() throws Exception {
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

  public void addIntent(ProjectDatasetSetupResult setup) throws Exception {

    String projectId = setup.getProjectId();

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setComments("comment1\ncomment2");

    contentManager.addNewIntent(null, projectId, doc);

    this.esTestUtils.refreshClassificationIndex();


  }

  public void tagTranscription(ProjectDatasetSetupResult setup) throws Exception {

    TaggingGuideDocument doc = new TaggingGuideDocument();
    doc.setIntent("reservation-make");
    doc.setRutag("reservation-make");
    doc.setComments("comment1\ncomment2");

    String jobId = setup.getJobId();
    int clientId = setup.getClientIdInt();
    int datasetId = setup.getDatasetIdInt();
    int projectId = setup.getProjectIdInt();
    String username = "test.account";

    List<TranscriptionDocumentForIndexing> transcriptions = this.esTestUtils
        .getMakeReservationHash1Documents(jobId, clientId, projectId, datasetId);
    transcriptions
        .addAll(this.esTestUtils.getAgentRequestDocuments(jobId, clientId, projectId, datasetId));

    List<String> transcriptionHashList = new ArrayList<>();
    transcriptionHashList.add(transcriptions.get(0).getTranscriptionHash());

    contentManager.indexNewTranscriptions(String.valueOf(clientId), String.valueOf(projectId),
        String.valueOf(datasetId), transcriptions, username);

    this.esTestUtils.refreshNltoolsIndex();

    String intent = doc.getIntent();
    String rutag = doc.getRutag();

    contentManager
        .addIntentByTranscriptionHashList(String.valueOf(clientId), String.valueOf(projectId),
            String.valueOf(datasetId), intent, rutag, username, transcriptionHashList);

    this.esTestUtils.refreshAllIndices();


  }

  @Ignore
  @Test
  public void testCaseInsensitiveIntentSearch() {

    try {
      this.setProjectDatasetTransformed();
      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      this.addIntent(setup);
      Thread.sleep(appConfig.getTestCaseTimeout());
      String intentSearchUrl =
          String.format("%ssearch/%s/intents?q=%s", basePath, setup.getProjectId(), "RES");
      ResponseEntity<String[]> searchEntity =
          this.restTemplate.getForEntity(intentSearchUrl, String[].class);

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertArrayEquals(new String[]{"reservation-make"}, searchEntity.getBody());

    } catch (Exception e) {
      fail();
    }
  }


  @Test
  public void testSearchResultsCountShouldMatchLimit() {

    try {
      this.setProjectDatasetTransformed();
      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      this.tagTranscription(setup);
      Thread.sleep(appConfig.getTestCaseTimeout());
      String intentSearchUrl =
          String.format("%ssearch/%s?startIndex=0&limit=1&sortBy=count:desc", basePath,
              setup.getProjectId());
      SearchRequest searchRequest = new SearchRequest();
      searchRequest.query("");
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(false);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(setup.datasetId));
      searchRequest.setFilter(filter);
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity =
          this.restTemplate.postForEntity(intentSearchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertTrue(searchEntity.getStatusCode().is2xxSuccessful());
      assertTrue(documentCollection.getTranscriptionList().size() == 1);
      assertTrue(documentCollection.getTotal().longValue() == 2);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testSearchResultsCountExceedingMaxLimit() {

    try {
      this.setProjectDatasetTransformed();
      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      this.tagTranscription(setup);
      Thread.sleep(appConfig.getTestCaseTimeout());
      String intentSearchUrl =
          String.format("%ssearch/%s?startIndex=0&limit=1001&sortBy=count:desc", basePath,
              setup.getProjectId());
      SearchRequest searchRequest = new SearchRequest();
      searchRequest.query("");
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(false);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(setup.datasetId));
      searchRequest.setFilter(filter);
      ResponseEntity<String> searchEntity =
          this.restTemplate.postForEntity(intentSearchUrl, searchRequest,
              String.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertTrue(!searchEntity.getStatusCode().is2xxSuccessful());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void testCaseInsensitiveSearchForManualTag() {

    try {
      this.setProjectDatasetTransformed();
      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      this.tagTranscription(setup);

      SearchRequest searchRequest = new SearchRequest();
      searchRequest.query("granular_intent:RES*");
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(true);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(setup.datasetId));
      searchRequest.setFilter(filter);

      String searchUrl = String.format("%ssearch/%s", basePath, setup.getProjectId());
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity =
          this.restTemplate.postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);
      Thread.sleep(appConfig.getTestCaseTimeout());
      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      assertTrue(documentCollection.getTotal().longValue() > 0);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testMultiDatasetSearch() {

    try {
      this.setProjectDatasetTransformed();
      int clientId = dataCreatorUtils.createRandomId(5);
      int projectId = dataCreatorUtils.createRandomId(5);
      int datasetId1 = dataCreatorUtils.createRandomId(5);
      int datasetId2 = dataCreatorUtils.createRandomId(5);
      int datasetId3 = dataCreatorUtils.createRandomId(5);
      int datasetId4 = dataCreatorUtils.createRandomId(5);
      List<TranscriptionDocumentForIndexing> makeReservationDocuments = this.esTestUtils
          .getMakeReservationHash2Documents("1", clientId, projectId, datasetId1);

      List<TranscriptionDocumentForIndexing> pointsbalanceDocuments =
          this.esTestUtils.getPointsBalanceDocuments("2", clientId, projectId, datasetId2);

      List<TranscriptionDocumentForIndexing> agentRequestDocuments =
          this.esTestUtils.getAgentRequestDocuments("3", clientId, projectId, datasetId3);

      List<TranscriptionDocumentForIndexing> makeReservationDocumentsOverriden = this.esTestUtils
          .getMakeReservationHash2Documents("1", clientId, projectId, datasetId4);

      String indexUrl1 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId1);

      String indexUrl2 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId2);

      String indexUrl3 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId3);

      String indexUrl4 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId4);

      ResponseEntity<Void> indexEntity1 =
          this.restTemplate.postForEntity(indexUrl1, makeReservationDocuments, Void.class);
      Thread.sleep(20000);
      assertEquals(HttpStatus.OK, indexEntity1.getStatusCode());

      ResponseEntity<Void> indexEntity2 =
          this.restTemplate.postForEntity(indexUrl2, pointsbalanceDocuments, Void.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertEquals(HttpStatus.OK, indexEntity2.getStatusCode());

      ResponseEntity<Void> indexEntity3 =
          this.restTemplate.postForEntity(indexUrl3, agentRequestDocuments, Void.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertEquals(HttpStatus.OK, indexEntity3.getStatusCode());

      ResponseEntity<Void> indexEntity4 =
          this.restTemplate.postForEntity(indexUrl4, makeReservationDocumentsOverriden, Void.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      assertEquals(HttpStatus.OK, indexEntity4.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();
      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      List<String> datasetsId = new ArrayList<>();
      datasetsId.add(String.valueOf(datasetId1));
      datasetsId.add(String.valueOf(datasetId2));
      datasetsId.add(String.valueOf(datasetId3));
      datasetsId.add(String.valueOf(datasetId4));
      SearchRequest searchRequest = new SearchRequest();
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setDatasets(datasetsId);
      searchRequest.setFilter(filter);
      String searchUrl = String.format("%ssearch/%s", basePath, projectId);
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity =
          this.restTemplate.postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      assertNotNull(documentCollection);

      filter.setDatasets(Collections.singletonList(String.valueOf(datasetId1)));
      searchRequest.setFilter(filter);

      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity2 =
          this.restTemplate.postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);
      Thread.sleep(appConfig.getTestCaseLongerTimeout());
      assertEquals(HttpStatus.OK, searchEntity2.getStatusCode());
      TranscriptionDocumentDetailCollection documentCollection2 = searchEntity2.getBody();
      assertNotNull(documentCollection2);
      assertThat(documentCollection2.getTotal(), is(lessThan(documentCollection.getTotal())));

    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testNumTokensRangeQuerySearch() {

    try {
      this.setProjectDatasetTransformed();
      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      int clientId = setup.clientIdInt;
      int projectId = setup.projectIdInt;
      int datasetId = setup.datasetIdInt;
      String jobId = setup.jobId;

      List<TranscriptionDocumentForIndexing> numTokensDocuments =
          getNumTokensRangeDocuments(jobId, clientId, projectId, datasetId);

      String indexUrl = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId);
      ResponseEntity<Void> indexEntity =
          this.restTemplate.postForEntity(indexUrl, numTokensDocuments, Void.class);
      Thread.sleep(10000);
      assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();
      Thread.sleep(2000);

      SearchRequest searchRequest = new SearchRequest();
      searchRequest.query("numTokens:[1 TO 10]");
      SearchRequestFilter filter = new SearchRequestFilter();
      filter.setTagged(false);
      filter.setUntagged(false);
      filter.setDatasets(Collections.singletonList(String.valueOf(datasetId)));
      searchRequest.setFilter(filter);
      String searchUrl = String.format("%ssearch/%s", basePath, projectId);

      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity =
          this.restTemplate.postForEntity(searchUrl, searchRequest,
              TranscriptionDocumentDetailCollection.class);
      Thread.sleep(5000);
      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      Thread.sleep(5000);
      TranscriptionDocumentDetailCollection documentCollection = searchEntity.getBody();
      assertEquals(4, documentCollection.getTotal().intValue());

    } catch (Exception e) {
      fail();
    }
  }

  private List<TranscriptionDocumentForIndexing> getNumTokensRangeDocuments(
      String jobId, int clientId, int projectId, int datasetId) {

    List<TranscriptionDocumentForIndexing> documentList = new ArrayList<>();

    String dummy = "dummy";
    String dataType = "Chat/Text";
    TranscriptionDocumentForIndexing doc1 = this.esTestUtils
        .makeDocument(jobId, "V", clientId, projectId, datasetId,
            dataType, 1, 1, dummy, dummy, dummy,
            DocumentType.ORIGINAL.type(), DigestUtils.sha1Hex(
                UUID.randomUUID().toString()));
    documentList.add(doc1);

    TranscriptionDocumentForIndexing doc2 = this.esTestUtils
        .makeDocument(jobId, "V", clientId, projectId, datasetId,
            dataType, 3, 1, dummy, dummy, dummy,
            DocumentType.ORIGINAL.type(), DigestUtils.sha1Hex(UUID.randomUUID().toString()));
    documentList.add(doc2);

    TranscriptionDocumentForIndexing doc3 = this.esTestUtils
        .makeDocument(jobId, "V", clientId, projectId, datasetId,
            dataType, 7, 1, dummy, dummy, dummy,
            DocumentType.ORIGINAL.type(), DigestUtils.sha1Hex(UUID.randomUUID().toString()));
    documentList.add(doc3);

    TranscriptionDocumentForIndexing doc4 = this.esTestUtils
        .makeDocument(jobId, "V", clientId, projectId, datasetId,
            dataType, 10, 1, dummy, dummy, dummy,
            DocumentType.ORIGINAL.type(), DigestUtils.sha1Hex(UUID.randomUUID().toString()));
    documentList.add(doc4);

    return documentList;
  }

}
