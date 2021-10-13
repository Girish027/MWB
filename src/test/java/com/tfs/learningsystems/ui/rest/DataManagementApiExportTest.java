/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.DataCreatorUtils;
import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.rest.ElasticSearchTestUtils.ProjectDatasetSetupResult;
import com.tfs.learningsystems.util.CommonLib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder
@Slf4j
public class DataManagementApiExportTest extends ElasticApiBaseTest {

  public static final String UTF8_BOM = "\uFEFF";
  @LocalServerPort
  int localTestPort;
  @MockBean
  @Qualifier("jobManagerBean")
  JobManager jobManager;
  @Autowired
  @Qualifier("elasticSearchClient")
  private org.elasticsearch.client.Client elasticSearchClient;
  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;
  @Autowired
  @Qualifier("dataCreatorUtils")
  private DataCreatorUtils dataCreatorUtils;


  private static String removeUTF8BOM(String s) {

    if (s.startsWith(UTF8_BOM)) {
      s = s.substring(1);
    }
    return s;
  }

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

    JobBO jobDetail = new JobBO();
    jobDetail.setId(Integer.parseInt(setup.jobId));
    Mockito.doReturn(jobDetail).when(this.jobManager)
        .getJobByProjectDataset(anyString(), anyString());
    Mockito.doReturn(this.projectDetail).when(this.projectManager).getProjectById(anyString());
    Mockito.doReturn(this.projectDetail).when(this.projectManager)
        .getProjectById(anyString(), anyString(), anyBoolean());
    return setup;
  }

  @Test
  public void testExport() throws Exception {

    try {

      this.setProjectDatasetTransformed();

      ElasticSearchTestUtils.ProjectDatasetSetupResult setup = this.createProjectDatasetSetup();
      String jobId = setup.getJobId();

      int clientId = setup.getClientIdInt();
      int projectId = setup.getProjectIdInt();
      int datasetId = setup.getDatasetIdInt();

      ProjectBO project = new ProjectBO();
      project.setId(projectId);
      project.setClientId(clientId);
      project.setClientId(clientId);
      project.setLocale(String.valueOf(Locale.getDefault()));

      Mockito.doReturn(project).when(this.projectManager)
          .getProjectById(String.valueOf(project.getId()));

           /* Mockito.doReturn(project).when(this.projectManager).getProjectById(String.valueOf(project.getClientId()),
            String.valueOf(project.getId()));*/

      Mockito.doReturn(project).when(this.validationManager)
          .validateClientAndProject(String.valueOf(project.getClientId()),
              String.valueOf(project.getId()));

      Mockito.doReturn(Locale.getDefault()).when(this.projectManager)
          .getProjectLocale(String.valueOf(project.getClientId()),
              String.valueOf(project.getId()));

      List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

      List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
          .getMakeReservationHash1Documents(jobId, clientId, projectId,
              datasetId);
      allDocuments.addAll(makeResHash1Documents);

      List<TranscriptionDocumentForIndexing> makeResHash2Documents = this.esTestUtils
          .getMakeReservationHash2Documents(jobId, clientId, projectId,
              datasetId);
      allDocuments.addAll(makeResHash2Documents);

      List<TranscriptionDocumentForIndexing> pointsbalanceDocuments =
          this.esTestUtils.getPointsBalanceDocuments(jobId, clientId, projectId, datasetId);
      allDocuments.addAll(pointsbalanceDocuments);

      String indexUrl = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
          clientId, projectId, datasetId);
      ResponseEntity<Void> indexEntity =
          this.restTemplate.postForEntity(indexUrl, allDocuments, Void.class);

      Thread.sleep(5000);

      assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

      this.esTestUtils.refreshNltoolsIndex();

      log.info("test export");
      StringBuilder url = new StringBuilder();
      String exportUrl = url.append(basePath).append("clients/").append(clientId)
          .append("/projects/").append(projectId).append("/datasets/")
          .append(datasetId).append("/export").toString();

      ResponseEntity<String> exportResponse = this.restTemplate
          .getForEntity(exportUrl, String.class);

      Thread.sleep(1000);

      assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
      assertThat(exportResponse.getHeaders().getContentLength(), greaterThan(0l));
    } catch (Exception ex) {
      fail();
    }

  }


  @Test
  public void testExportMultiDataset() throws Exception {

    this.setProjectDatasetTransformed();
    int clientId = 101;
    int projectId = 51;
    int datasetId1 = 41;
    int datasetId2 = 42;

    ProjectBO project = new ProjectBO();
    project.setId(projectId);
    project.setClientId(clientId);
    project.setClientId(clientId);
    project.setLocale(String.valueOf(Locale.getDefault()));

    Mockito.doReturn(project).when(this.projectManager)
        .getProjectById(String.valueOf(project.getId()));

    Mockito.doReturn(project).when(this.projectManager)
        .getProjectById(String.valueOf(project.getClientId()),
            String.valueOf(project.getId()));

    Mockito.doReturn(project).when(this.validationManager)
        .validateClientAndProject(String.valueOf(project.getClientId()),
            String.valueOf(project.getId()));

    Mockito.doReturn(Locale.getDefault()).when(this.projectManager)
        .getProjectLocale(String.valueOf(project.getClientId()),
            String.valueOf(project.getId()));

    List<TranscriptionDocumentForIndexing> makeReservationDocuments = this.esTestUtils
        .getMakeReservationHash2Documents("51", clientId, projectId,
            datasetId1);

    List<TranscriptionDocumentForIndexing> pointsbalanceDocuments =
        this.esTestUtils.getPointsBalanceDocuments("52", clientId, projectId, datasetId2);

    String indexUrl1 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
        clientId, projectId, datasetId1);

    String indexUrl2 = String.format("%scontent/%d/projects/%d/datasets/%d/index", basePath,
        clientId, projectId, datasetId2);

    ResponseEntity<Void> indexEntity1 =
        this.restTemplate.postForEntity(indexUrl1, makeReservationDocuments, Void.class);

    Thread.sleep(2000);
    assertEquals(HttpStatus.OK, indexEntity1.getStatusCode());

    ResponseEntity<Void> indexEntity2 =
        this.restTemplate.postForEntity(indexUrl2, pointsbalanceDocuments, Void.class);

    Thread.sleep(2000);

    assertEquals(HttpStatus.OK, indexEntity2.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    log.info("test export");
    StringBuilder url = new StringBuilder();
    String exportUrl = url.append(basePath).append("clients/").append(clientId).append("/projects/")
        .append(projectId).append("/export").toString();

    ResponseEntity<String> exportResponse = this.restTemplate.getForEntity(exportUrl, String.class);

    assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
    assertThat(exportResponse.getHeaders().getContentLength(), greaterThan(0l));

  }

}