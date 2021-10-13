package com.tfs.learningsystems.testutil;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.ModelConfigDetail;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.util.Constants;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class ApiUtil_NegativeTest {

  final String setUriPath = "https://tagging.247-inc.com:8443/nltools/private/v1/files/";
  public String currentUserId = "IntegrationTest@247.ai";
  @Autowired
  ApiUtilTest apiUtil;
  long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
  private String basePath;
  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  public void testGetProject(Integer projectID) {
    ResponseEntity<ProjectBO> getProjectEntity = this.restTemplate
        .getForEntity(apiUtil.createCompleteURL((basePath + "projects/" + projectID)),
            ProjectBO.class);
    assertEquals(HttpStatus.NOT_FOUND, getProjectEntity.getStatusCode());
  }

  public void testCreateDataset(AddDatasetRequest addDset) {
    ResponseEntity<DatasetBO> dsetEntity = this.restTemplate
        .postForEntity(apiUtil.createCompleteURL(basePath + "datasets"), addDset, DatasetBO.class);
    //assertEquals(HttpStatus.BAD_REQUEST, dsetEntity.getStatusCode()); BUG NT-2350
  }

  public void getDsetTransformationStatus(Integer projectID, Integer datasetID) {
    ResponseEntity<TaskEventBO> getTransformStatus = this.restTemplate.getForEntity(
        apiUtil.createCompleteURL(
            basePath + "projects/" + projectID + "/datasets/" + datasetID + "/transform/status"),
        TaskEventBO.class);
    assertEquals(HttpStatus.NOT_FOUND, getTransformStatus.getStatusCode());
  }

  public ResponseEntity<Void> linkDsetsToProj(Integer clientId, Integer projectID,
      Integer datasetID) {
    ResponseEntity<Void> linkProjDset = this.restTemplate
        .exchange(apiUtil.createCompleteURL(
            basePath + "clients/" + clientId + "/projects/" + projectID + "/datasets/" + datasetID),
            HttpMethod.PUT,
            null, Void.class);
    return linkProjDset;
  }

  public ResponseEntity<TaskEventDetail> testDatasetTransform(Integer clientId, Integer projectID,
      Integer datasetID, String suggestedCategory) {
    ResponseEntity<TaskEventDetail> transfromDset = this.restTemplate

        .exchange(apiUtil.createCompleteURL(
            basePath + "clients/" + clientId + "/projects/" + projectID + "/datasets/" + datasetID
                + "/transform?useModelForSuggestedCategory=" + suggestedCategory), HttpMethod.PUT,
            null,
            TaskEventDetail.class);
    return transfromDset;
  }

  public void testGetConfigData(String clientId, String configId) {
    ResponseEntity<ModelConfigDetail> getDefaultConfigEntity = this.restTemplate
        .getForEntity(apiUtil.createCompleteURL(
            basePath + "clientId/" + clientId + "/configs/" + configId + "/data"),
            ModelConfigDetail.class);
    assertEquals(HttpStatus.NOT_FOUND, getDefaultConfigEntity.getStatusCode());
  }

  public void testModelBuild(Integer clientId, Integer modelDBId) {
    ResponseEntity<Void> buildEntity = this.restTemplate
        .postForEntity(apiUtil.createCompleteURL(
            basePath + "clients/" + clientId + "/models/" + modelDBId + "/build"), null,
            Void.class);
    assertEquals(HttpStatus.BAD_REQUEST, buildEntity.getStatusCode());
  }

  public ResponseEntity<EvaluationResponse> utteranceTest(Integer clientId, Integer projectID,
      String modelUUID) {
    ResponseEntity<EvaluationResponse> utteranceTestEntity = this.restTemplate
        .postForEntity(apiUtil.createCompleteURL(
            basePath + "clients" + clientId + "/projects/" + projectID + "/models/" + modelUUID
                + "/eval_transcriptions?testModelType=" + Constants.DIGITAL_MODEL),
            IntegrationUtilTest.addUtterance(), EvaluationResponse.class);
    return utteranceTestEntity;
  }

  public ResponseEntity<String> testSpeechUtterance(Integer clientId, Integer projectID,
      String modelUUID, String fileType, LinkedMultiValueMap<String, Object> map) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/").append(modelUUID).append
            ("/eval_utterance?fileType=" + fileType);
    System.out.println("API in testSpeechUtterance is -->" + completeURL.toString());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    ResponseEntity<String> utteranceTestEntity = restTemplate.exchange(
        completeURL.toString(),
        HttpMethod.POST,
        requestEntity,
        String.class);

    return utteranceTestEntity;
  }

  public ResponseEntity<String> utteranceTestWithInvalidData(Integer clientId, Integer projectID,
      String modelUUID) {

    StringBuilder baseURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/").append(modelUUID);

    StringBuilder completeURL = baseURL.append("/eval_utterance?fileType=link");

    System.out.println("API in utteranceTestWithInvalidData is -->" + completeURL.toString());

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    /*Testing with absent audioURL param */
    System.out.println("Testing with missing audioURL param");
    ResponseEntity<String> utteranceTestEntity = testSpeechUtterance(clientId, projectID,
        modelUUID, "link", map);
    System.out.println(utteranceTestEntity);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(utteranceTestEntity);
    assertEquals(HttpStatus.BAD_REQUEST, utteranceTestEntity.getStatusCode());

    /* Testing with invalid link */
    System.out.println("Testing with invalid link");
    map.clear();
    map.add("audioURL", "http://google.com");
    utteranceTestEntity = testSpeechUtterance(clientId, projectID,
        modelUUID, "link", map);
    System.out.println(utteranceTestEntity);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(utteranceTestEntity);
    assertEquals(HttpStatus.BAD_REQUEST, utteranceTestEntity.getStatusCode());

    /* Testing with empty audio file */
    System.out.println("Testing with empty audio file");
    completeURL = baseURL.append("/eval_utterance?fileType=recording");

    System.out.println("API in utteranceTestWithInvalidData is -->" + completeURL.toString());

    map.clear();
    map.add("audioFile", new FileSystemResource("src/test/resources/emptyAudio.wav"));
    utteranceTestEntity = testSpeechUtterance(clientId, projectID,
        modelUUID, "recording", map);
    System.out.println(utteranceTestEntity);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(HttpStatus.BAD_REQUEST, utteranceTestEntity.getStatusCode());
    return utteranceTestEntity;
  }

  public ResponseEntity<EvaluationResponse> batchTest(Integer clientId, Integer projectID,
      Integer datasetID, String modelUUID) {
    List<String> dsetsForBatchTest = new LinkedList<>();
    dsetsForBatchTest.add(Integer.toString(datasetID));
    ResponseEntity<EvaluationResponse> postBatchTests = this.restTemplate
        .postForEntity(apiUtil.createCompleteURL(
            basePath + "clients" + clientId + "/projects/" + projectID + "/models/" + modelUUID
                + "/eval_datasets"),
            dsetsForBatchTest, EvaluationResponse.class);
    return postBatchTests;
  }

  public void listBatchTests(Integer clientId, Integer projectID, String modelUUID) {
    ResponseEntity<Error> listBatchTests = this.restTemplate
        .getForEntity(apiUtil.createCompleteURL(
            basePath + "clients" + clientId + "/projects/" + projectID + "/models/" + modelUUID
                + "/batch_tests"),
            Error.class);
    assertEquals(HttpStatus.NOT_FOUND, listBatchTests.getStatusCode());
  }


  public void testCreateModel(String cid, Integer clientId, Integer projectID,
      String configID,
      Integer datasetID) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models");

    System.out.println("API in testCreateModel is -->" + completeURL.toString());

    ModelBO model = IntegrationUtilTest
        .getModelObject(cid, projectID, configID, datasetID, currentUserId,
            Constants.DIGITAL_MODEL);
    ResponseEntity<ModelBO> createModelEntity = this.restTemplate
        .postForEntity(apiUtil.createCompleteURL(completeURL.toString()), model, ModelBO.class);
    assertEquals(HttpStatus.NOT_FOUND, createModelEntity.getStatusCode());
  }
}
