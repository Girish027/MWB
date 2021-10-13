/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.rest.ElasticSearchTestUtils;
import com.tfs.learningsystems.util.CommonLib;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SearchManagerImplTest extends ElasticApiBaseTest {


  @Autowired
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;

  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;

  @Before
  public void setUpSearchImpl() throws Exception {

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @After
  public void tearDownSearchImpl() throws Exception {
  }

  @Test
  public void testIsFieldExists() {
    this.setProjectDatasetTransformed();

    int clientId = this.clientDetail.getId();
    String projectId = Integer.toString(this.projectDetail.getId());
    String datasetId = Integer.toString(this.datasetDetail.getId());

    List<TranscriptionDocumentForIndexing> allDocuments = new ArrayList<>();

    List<TranscriptionDocumentForIndexing> makeResHash1Documents = this.esTestUtils
        .getMakeReservationHash1Documents("1", clientId, Integer.valueOf(projectId),
            Integer.valueOf(datasetId));

    allDocuments.addAll(makeResHash1Documents);

    String indexUrl = String.format("%scontent/%d/projects/%s/datasets/%s/index", basePath,
        clientId, projectId, datasetId);

    ResponseEntity<Void> indexEntity =
        this.restTemplate.postForEntity(indexUrl, allDocuments, Void.class);

    assertEquals(HttpStatus.OK, indexEntity.getStatusCode());

    this.esTestUtils.refreshNltoolsIndex();

    assertTrue(this.searchManager
        .isFieldExists(clientId, projectId, Collections.singletonList(datasetId), "documentType"));

    assertFalse(this.searchManager
        .isFieldExists(clientId, projectId, Collections.singletonList(datasetId), "blahblah"));
  }

}
