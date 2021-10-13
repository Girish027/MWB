/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.ui.ElasticApiBaseTest;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.util.ErrorMessage;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder
public class ContentApiTest extends ElasticApiBaseTest {

  @Test
  public void testTagTranscriptionProjectNotFoundError() {
    this.setProjectInvalid("invalidProjectId");
    String url = String.format("%scontent/%s/datasets/1/tag", basePath, "invalidProjectId");
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals("project_not_found", entity.getBody().getErrorCode());
  }

  @Test
  public void testTagTranscriptionDatasetNotAssociatedError() {
    this.setProjectDatasetNotAssociated();
    String url = String.format("%scontent/%s/datasets/%s/tag", basePath,
        this.projectDetail.getId(), "invalidDatasetId");
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, entity.getBody().getMessage());
  }

  @Test
  public void testTagTranscriptionNotTransformedError() {
    this.setProjectDatasetNotTransformed();
    this.projectManager.addDatasetProjectMapping(Integer.toString(this.projectDetail.getClientId()),
        Integer.toString(this.projectDetail.getId()), Integer.toString(this.datasetDetail.getId()),
        currentUserId);
    String url = String.format("%scontent/%s/datasets/%s/tag", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, entity.getBody().getMessage());
  }

  @Test
  public void testUpdateTranscriptionProjectNotFoundError() {
    this.setProjectInvalid("weirdProjectId");
    String url = String.format("%scontent/%s/datasets/%s/tag/update", basePath,
        "weirdProjectId", this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals("project_not_found", entity.getBody().getErrorCode());
  }

  @Test
  public void testUpdateTranscriptionDatasetNotAssociatedError() {
    this.setProjectDatasetNotAssociated();
    String url = String.format("%scontent/%s/datasets/%s/tag/update", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, entity.getBody().getMessage());
  }

  @Test
  public void testUpdateTranscriptionNotTransformedError() {
    this.setProjectDatasetNotTransformed();
    String url = String.format("%scontent/%s/datasets/%s/tag/update", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, entity.getBody().getMessage());
  }

  @Test
  public void testDeleteTranscriptionProjectNotFoundError() {
    this.setProjectInvalid("randomProjectId");
    String url = String.format("%scontent/%s/datasets/1/tag/delete", basePath, "randomProjectId");
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals("project_not_found", entity.getBody().getErrorCode());
  }

  @Test
  public void testDeleteTranscriptionDatasetNotAssociatedError() {
    this.setProjectDatasetNotAssociated();
    String url = String.format("%scontent/%s/datasets/%s/tag/delete", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, entity.getBody().getMessage());
  }

  @Test
  public void testDeleteTranscriptionNotTransformedError() {
    this.setProjectDatasetNotTransformed();
    String url = String.format("%scontent/%s/datasets/%s/tag/delete", basePath,
        this.projectDetail.getId(), this.datasetDetail.getId());
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(url, null, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, entity.getBody().getMessage());
  }

}