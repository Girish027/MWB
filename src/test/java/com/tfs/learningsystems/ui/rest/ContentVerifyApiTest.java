package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.model.VerifiedTranscriptionsResponse;
import com.tfs.learningsystems.ui.model.VerifyRequest;
import com.tfs.learningsystems.util.CommonLib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ContentVerifyApiTest extends ElasticApiBaseTest {

  @Autowired
  private ElasticSearchTestUtils esTestUtils;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ConfigManager configManager;

  public String clientId;
  public String projectId;
  public String datasetId;
  public String userId;

  @Before
  public void setUp() throws IOException {

    super.setUp();
    configManager.oldReloadDefaultConfig(appConfig.getOldEnglishConfigArchiveFilename(),
        "system_default_en_0", "en");

    this.esTestUtils.refreshAllIndices();
    clientId = Integer.toString(super.clientDetail.getId());
    projectId = Integer.toString(super.projectDetail.getId());
    datasetId = Integer.toString(super.datasetDetail.getId());
    Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
    this.addingDataToES();

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @After
  public void esTearDown() {

  }

  private void addingDataToES() {
    int clientId = Integer.valueOf(this.clientId).intValue();
    int projectId = Integer.valueOf(this.projectId).intValue();
    int datasetId = Integer.valueOf(this.datasetId).intValue();
    this.setProjectDatasetTransformed();
    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
        .getMakeReservationHash1Documents("1", clientId, projectId, datasetId);
    allDocuments.addAll(makeResHash1Documents);

    List<TranscriptionDocumentForIndexing> makeResHash2Documents = this.esTestUtils
        .getMakeReservationHash2Documents("1", clientId, projectId, datasetId);
    allDocuments.addAll(makeResHash2Documents);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments =
        this.esTestUtils.getPointsBalanceDocuments("1", clientId, projectId, datasetId);
    allDocuments.addAll(pointsbalanceDocuments);

    String indexUrl = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
        clientId, projectId, datasetId);
    ResponseEntity<Void> indexEntity =
        super.restTemplate.postForEntity(indexUrl, allDocuments, Void.class);
    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    AddIntentRequest addIntentRequest = new AddIntentRequest();
    addIntentRequest.setUsername("john.doe");
    addIntentRequest.setIntent("reservation-make");
    List<String> transcriptionHashList = new ArrayList<>();
    for (TranscriptionDocumentForIndexing allDocument : allDocuments) {
      transcriptionHashList.add(allDocument.getTranscriptionHash());
    }

    addIntentRequest.setTranscriptionHashList(transcriptionHashList);

    String tagUrl =
        String.format("%scontent/%d/tag", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity = super.restTemplate.postForEntity(tagUrl,
        addIntentRequest, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    addIntentRequest = new AddIntentRequest();
    addIntentRequest.setUsername("john.doe");
    addIntentRequest.setIntent("reservation-change");
    transcriptionHashList = new ArrayList<>();
    for (TranscriptionDocumentForIndexing document : makeResHash2Documents) {
      transcriptionHashList.add(document.getTranscriptionHash());
    }
    addIntentRequest.setTranscriptionHashList(transcriptionHashList);

    String updateTagUrl =
        String.format("%scontent/%d/tag/update", basePath, projectId);
    ResponseEntity<UpdateIntentResponse> entity2 = super.restTemplate.postForEntity(updateTagUrl,
        addIntentRequest, UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, entity2.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();
  }


  @Test
  public void testVerifyIntents() {

    try {
      String searchUrl = String.format("%scontent/%s/verify", basePath, projectId);

      VerifyRequest request = new VerifyRequest();
      request.setConfigId("2");
      Thread.sleep(10000);
      ResponseEntity<VerifiedTranscriptionsResponse> searchEntity =
          super.restTemplate.postForEntity(searchUrl, request,
              VerifiedTranscriptionsResponse.class);

      Thread.sleep(10000);

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());

      Thread.sleep(10000);

      VerifiedTranscriptionsResponse documentCollection = searchEntity.getBody();

      Thread.sleep(10000);

      assertThat(documentCollection.getTotal(), is(greaterThan(0L)));

    } catch (Exception ex) {
      fail();
    }
  }


}
