/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelTestBatchBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.model.BatchTestInfo;
import com.tfs.learningsystems.ui.model.BatchTestInfo.TypeEnum;
import com.tfs.learningsystems.ui.model.BatchTestResultsResponse;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ModelTestApiTest {

  private static String basePath;
  private static ClientBO client;
  private static ProjectBO project;
  private static String currentUserId = "UnitTest@247.ai";
  /**
   * the variable to have different types of models Key indicates the type - (With
   * description/without description etc)
   */
  private static HashMap<String, ModelBO> models = new HashMap<String, ModelBO>();
  /**
   * Datasets for unit tests -- Key : id of the dataset for unit testing
   */
  private static HashMap<String, DatasetBO> datasets = new HashMap<String, DatasetBO>();
  /**
   * batch tests for unit testing -- Key: identifier for unit test
   */
  private static HashMap<String, ModelTestBatchBO> batchTests = new HashMap<String, ModelTestBatchBO>();
  private static boolean isDatabaseSetUpDone = false;
  @Autowired
  @Qualifier("fileManagerBean")
  private FileManager fileManager;
  @Autowired
  private TestRestTemplate restTemplate;

  private static ModelTestBatchBO getBatchTest() {
    ModelTestBatchBO batchTest = new ModelTestBatchBO();
    batchTest.setClientId(client.getId().longValue());
    batchTest.setProjectId(project.getId().longValue());
    batchTest.setResult_file("/tmp/model_test/test.csv"); // this is just an entry for now
    batchTest.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    batchTest.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    return batchTest;

  }

  @BeforeClass
  public static void beforeClass() throws ScriptException {
    log.info(
        "START EXECUTING TEST CLASS [" + Thread.currentThread().getStackTrace()[1].getClassName()
            + "]");
  }

  /**
   * tears down any project, model, batch tests etc created for unit tests in the class
   */
  @AfterClass
  public static void tearDown() {
    try {
      for (ModelTestBatchBO batchTest : batchTests.values()) {
        batchTest.delete();
      }

      for (ModelBO model : models.values()) {
        model.delete();
      }

      for (DatasetBO dataset : datasets.values()) {
        dataset.delete();
      }
      if (project.hasId()) {
        project.delete();
      }
      if (client.hasId()) {
        client.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("END EXECUTING TEST CLASS [" + Thread.currentThread().getStackTrace()[1].getClassName()
        + "]");
  }

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1";
  }

  private ModelBO getModel() {
    ModelBO model = new ModelBO();
    model.setModelId(UUID.randomUUID().toString());
    model.setCid(client.getCid());
    model.setProjectId(project.getId());
    model.setConfigId(1);
    model.setUserId(currentUserId);
    model.setVersion(1);
    model.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
    model.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
    return model;
  }

  private DatasetBO getDataset() throws Exception {
    String file = "test-input.csv";
    DatasetBO newDataset = ModelUtils.getTestDatasetObject(client.getId(), project.getId(), client.getName());
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            newDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    newDataset.setUri(urlStr);
    return newDataset;

  }

  private void doDatabaseSetUp() {
    if (!isDatabaseSetUpDone) {
      //create client
      String className = "ModelTestApi";
      String name = className + "_" + Long.toString(System.currentTimeMillis() % 10000000);
      client = ModelUtils.getTestClientObject(name);
      client.setCreatedAt(Calendar.getInstance().getTimeInMillis() - 1);
      client.setModifiedAt(Calendar.getInstance().getTimeInMillis());
      client.create();

      log.info("Created client -" + client.getName() + " for ModelTestApiTests");

      //create project
      project = ModelUtils
          .getTestProjectObject(client.getId(), currentUserId, "projectFor_" + name);
      project.setClientId(client.getId());
      project.setOwnerId(currentUserId);
      project.create();

      try {
        //create datasets
        String[] datasetIds = {"1", "2", "3"};
        for (String id : datasetIds) {
          DatasetBO newDataset = getDataset();
          newDataset.setName("Dataset_" + id + "_" + name);
          newDataset.create();
          datasets.put(id, newDataset);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      //create a model with description and based on dataset 1
      ModelBO model = getModel();
      model.setName("Model_1_" + client.getName());
      model.setDescription("This is the description of the model for unit test");
      model.setDatasetIds(Collections.singletonList(datasets.get("1").getId().toString()));
      model.create();
      models.put("withDescription", model);

      //create a model without description and based on dataset 2 and 3
      model = getModel();
      model.setName("Model_2_" + client.getName());
      List<String> ids = new ArrayList<>();
      ids.add(datasets.get("2").getId().toString());
      ids.add(datasets.get("3").getId().toString());
      model.setDatasetIds(ids);
      model.create();
      models.put("withoutDescription", model);

      //create a model. This model will not have any batch tests
      model = getModel();
      model.setName("Model_3_" + client.getName());
      model.setDatasetIds(Collections.singletonList(datasets.get("1").getId().toString()));
      model.create();
      models.put("noBatchTests", model);

      //create a model without description and based on dataset 2 and 3
      model = getModel();
      model.setName("Model_3_" + client.getName());
      model.setDatasetIds(Collections.singletonList(datasets.get("2").getId().toString()));
      model.setDatasetIds(ids);
      model.create();
      models.put("limitAndStartIndex", model);

      //create successful batch test for Model_1 with payload of datasets 2 and 3
      ModelTestBatchBO batchTest = getBatchTest();
      batchTest.setModelId(models.get("withDescription").getModelId());
      batchTest.setRequestPayload(datasets.get("2").getId() + "," + datasets.get("3").getId());
      batchTest.setStatus("success");
      batchTest.create();
      batchTests.put("Model_1, 2 datasets, success", batchTest);

      //create failed batch for Model_1 with payload of just 1 dataset
      batchTest = getBatchTest();
      batchTest.setModelId(models.get("withDescription").getModelId());
      batchTest.setRequestPayload(datasets.get("2").getId().toString());
      batchTest.setStatus("failed");
      batchTest.create();
      batchTests.put("Model_1, 1 dataset, failed", batchTest);

      //create 22 batch test entries. - for testing limit and startIndex
      for (int i = 1; i <= 22; i++) {
        ModelTestBatchBO test = getBatchTest();
        test.setModelId(models.get("limitAndStartIndex").getModelId());
        test.setRequestPayload(datasets.get("2").getId().toString());
        test.setStatus("success");
        test.create();
        batchTests.put("Model_2_batchTestNo_" + i, test);
      }

      isDatabaseSetUpDone = true;
    }
  }

  @Test
  public void testStatusEnum_NT1535() {

    String correctOne = EvaluationResponse.StatusEnum.IN_PROGRESS.toString();
    String wrongOne = "in-progress";

    EvaluationResponse.StatusEnum decoded = EvaluationResponse.StatusEnum
        .valueOf(correctOne.toUpperCase());
    assertTrue(EvaluationResponse.StatusEnum.IN_PROGRESS.equals(decoded));
    try {
      EvaluationResponse.StatusEnum.valueOf(wrongOne.toUpperCase());
      fail("Shouldn't be about to get here while decoding a wrong enum value - " + wrongOne);
    } catch (Exception e) {
      // that's expected
    }
  }

  //
  // please do not remove this 'ignore' test. It means for manual testing, since the model might be changed.
  // TODO Mock the dependencies for unit testing evalUtterance and evalDataset
  //
  @Ignore
  @Test
  public void testEvalUttrances() {
    doDatabaseSetUp();

    List<String> uttrances = new LinkedList<>();
    uttrances.add("how are you");

    String modelId = models.get("withDescription").getModelId();
    String apiPath = constructBaseAPIPath(client.getId(), project.getId(), modelId)
        .append("/eval_transcriptions")
        .toString();

    ResponseEntity<EvaluationResponse> entity =
        this.restTemplate.postForEntity(apiPath, uttrances, EvaluationResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    EvaluationResponse returnedClient = entity.getBody();
    assertFalse(returnedClient.getEvaluations().isEmpty());
    assertEquals(returnedClient.getModelId(), modelId);
  }

  // TODO Needs to be implmented using dynamic model Id, mock the dependencies
  @Ignore
  @Test
  public void testEvalDatasets() {
    doDatabaseSetUp();
    List<String> datasetIds = new LinkedList<>();
    datasetIds.add(
        "2");                                      // for testing. Todo, Need to be dynamic per test

    String modelId = models.get("withDescription").getModelId();
    String apiPath = constructBaseAPIPath(client.getId(), project.getId(), modelId)
        .append("/eval_datasets")
        .toString();

    ResponseEntity<EvaluationResponse> entity =
        this.restTemplate.postForEntity(apiPath, datasetIds, EvaluationResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    EvaluationResponse returnedClient = entity.getBody();
    assertFalse(returnedClient.getTestId().isEmpty());
    assertEquals(returnedClient.getModelId(), modelId);
  }

  /**
   * ******************************
   * List Batch Test API Unit Tests
   * ******************************
   */

  /**
   * ListBatchTest Scenarios :-
   *
   * 1. Project Id is invalid 2. model id is invalid 3. any of the datasets are not valid 4.
   * Response has required data 5. model desc - optional 6. batch test info - optional 7. if batch
   * test info is present - check the required info 8. Batch tests are returned latest -> oldest
   * (descending order of time of creation) 9. Batch Test name is having default value
   *
   * ***limit and startIndex scenarios *** 10. limit not provided 11. limit is provided 12.
   * startIndex not provided 13. startIndex is provided 14. both limit and startIndex is given
   */

  @Test
  public void testListBatchTest_projectIdInvalid_errorResponse() {
    doDatabaseSetUp();

    // create successful batch test for a deleted project
    Integer projectId = 999999;
    ModelTestBatchBO batchTest = getBatchTest();
    batchTest.setProjectId((long) projectId);
    batchTest.setModelId(UUID.randomUUID().toString());
    batchTest.setRequestPayload(datasets.get("1").getId().toString());
    batchTest.setStatus("success");
    batchTest.create();
    batchTests.put("deleted project, 1 dataset, success", batchTest);

    String modelId = batchTests.get("deleted project, 1 dataset, success").getModelId();
    String apiPath = constructBatchTestApiPath(client.getId(), projectId, modelId);
    ResponseEntity<Error> entity = this.restTemplate.getForEntity(apiPath, Error.class);

    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    Error resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     * "code": 404,
     * "errorCode": "project_not_found",
     * "message": "project '9999' not found"
     * }
     */
    assertEquals( new Error(404, "model_not_found",
        String.format("Model not found")),resultsResponse);
  }

  @Test
  public void testListBatchTest_modelIdInvalid_errorResponse() {
    doDatabaseSetUp();

    // create successful batch test for a non existent model
    ModelTestBatchBO batchTest = getBatchTest();
    batchTest.setModelId(UUID.randomUUID().toString());
    batchTest.setRequestPayload(datasets.get("1").getId().toString());
    batchTest.setStatus("success");
    batchTest.create();
    batchTests.put("deleted model, 1 dataset, success", batchTest);

    String modelId = batchTests.get("deleted model, 1 dataset, success").getModelId();
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), modelId);
    ResponseEntity<Error> entity = this.restTemplate.getForEntity(apiPath, Error.class);

    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    Error resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     * "code": 404,
     * "errorCode": "model_not_found",
     * "message": "model 'UUID' not found"
     * }
     */
    assertEquals(resultsResponse, new Error(404, "model_not_found",
        String.format("model '%s' not found", modelId)));
  }

  @Test
  public void testListBatchTest_requiredDataIsPresent_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("withDescription");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     *  .....
     * }
     *
     */

    assertEquals(resultsResponse.getProjectId(), project.getId().toString());
    assertEquals(resultsResponse.getModelId(), model.getModelId());
    assertEquals(resultsResponse.getModelName(), model.getName());
    assertEquals(resultsResponse.getModelVersion(), model.getVersion().toString());
  }

  @Test
  public void testListBatchTest_descriptionIsPresent_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("withDescription");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     *   "modelDescription": "tora the cat",
     *   .....
     * }
     *
     */

    assertEquals(resultsResponse.getModelDescription(), model.getDescription());
  }

  @Test
  public void testListBatchTest_descriptionIsNotPresent_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("withoutDescription");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     *   .....
     * }
     *
     */
    assertNull(resultsResponse.getModelDescription());
  }

  @Test
  public void testListBatchTest_batchTestsNotAvailable_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("noBatchTests");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     * }
     *
     */
    assertEquals(resultsResponse.getBatchTestInfo().size(), 0);
  }

  @Test
  public void testListBatchTest_batchTestsAvailable_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("withDescription");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response should be similar to the below sample
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     *   "modelDescription": "tora the cat",
     *   "batchTestInfo": [
     *     {
     *       "testId": "64fc4148-3ca7-4958-a40c-cd7a8f02a7cc",         //batch test 2 was successful with 2 dataset inputs
     *       "type": "DATASETS",
     *       "status": "SUCCESS",
     *       "requestPayload": "YesNo,trainingRU-2",
     *       "createdAt": "1536876619527",
     *       "batchTestName": "BatchTest_1536876619527"
     *     },
     *     {
     *       "testId": "54d08224-1fb4-4e5f-b335-7aea46d73f82",          // Batch test 1 has failed with single dataset input
     *       "type": "DATASETS",
     *       "status": "FAILED",
     *       "requestPayload": "YesNo",
     *       "createdAt": "1536778357179",
     *       "batchTestName": "BatchTest_1536778357179"
     *     }
     *   ]
     * }
     *
     */
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();
    assertThat(batchTestInfoList, hasSize(2));

    //verify each batchTestInfo
    BatchTestInfo batchTest_1 = batchTestInfoList.get(0);
    ModelTestBatchBO expectedBatchTest_1 = batchTests.get("Model_1, 1 dataset, failed");
    assertEquals(batchTest_1.getTestId(), expectedBatchTest_1.getId());
    assertEquals(batchTest_1.getType(), TypeEnum.DATASETS);
    assertEquals(batchTest_1.getStatus().toString(), expectedBatchTest_1.getStatus());
    assertEquals(batchTest_1.getCreatedAt(), expectedBatchTest_1.getCreatedAt().toString());

    assertEquals(batchTest_1.getBatchTestName(), "BatchTest_" + expectedBatchTest_1.getCreatedAt());

    // verify response has the single dataset name
    String expectedDatasetNames_1 = datasets.get("2").getId().toString();
    assertEquals(batchTest_1.getRequestPayload(), expectedDatasetNames_1);

    BatchTestInfo batchTest_2 = batchTestInfoList.get(1);
    ModelTestBatchBO expectedBatchTest_2 = batchTests.get("Model_1, 2 datasets, success");
    assertEquals(batchTest_2.getTestId(), expectedBatchTest_2.getId());
    assertEquals(batchTest_2.getType(), TypeEnum.DATASETS);
    assertEquals(batchTest_2.getStatus().toString(), expectedBatchTest_2.getStatus());
    assertEquals(batchTest_2.getCreatedAt(), expectedBatchTest_2.getCreatedAt().toString());

    assertEquals(batchTest_2.getBatchTestName(), "BatchTest_" + expectedBatchTest_2.getCreatedAt());

    // verify response has comma separated dataset names when there are more than 1 dataset
    String expectedDatasetNames_2 = datasets.get("2").getId().toString() + "," + datasets.get("3").getId().toString();
    assertEquals(batchTest_2.getRequestPayload(), expectedDatasetNames_2);
  }

  @Test
  public void testListBatchTest_batchTestsLatestFirst_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("withDescription");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    /*
     * Expected Response should be similar to the below sample
     * {
     *   "projectId": "1",
     *   "modelId": "8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451",
     *   "modelName": "tora",
     *   "modelVersion": "1",
     *   "modelDescription": "tora the cat",
     *   "batchTestInfo": [
     *     {
     *       ....
     *       "createdAt": "1536876619527",
     *       "batchTestName": "BatchTest_1536876619527"
     *     },
     *     {
     *       ...
     *       "createdAt": "1536778357179",
     *       "batchTestName": "BatchTest_1536778357179"
     *     }
     *   ]
     * }
     *
     */
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();
    assertThat(batchTestInfoList, hasSize(2));

    //verify that timestamp of the first batch test is newer than the second batch test
    BatchTestInfo batchTest1 = batchTestInfoList.get(0);
    BatchTestInfo batchTest2 = batchTestInfoList.get(1);
    assertTrue(
        Long.parseLong(batchTest1.getCreatedAt()) > Long.parseLong(batchTest2.getCreatedAt()));
  }

  @Test
  public void testListBatchTest_limitNotProvided_20batchTests() {
    doDatabaseSetUp();
    ModelBO model = models.get("limitAndStartIndex");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId());
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();
    // 20 is the default size/limit
    assertThat(batchTestInfoList, hasSize(20));
  }

  @Test
  public void testListBatchTest_limitIsProvidedAs5_5BatchTests() {
    doDatabaseSetUp();
    ModelBO model = models.get("limitAndStartIndex");
    String apiPath =
        constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId()) + "&limit=5";
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();

    // should have 5 entries (limit is provided)
    assertThat(batchTestInfoList, hasSize(5));
  }

  @Test
  public void testListBatchTest_startIndexNotProvided_getsPageZero() {
    doDatabaseSetUp();
    ModelBO model = models.get("limitAndStartIndex");
    String apiPath =
        constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId()) + "&limit=2";
    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();

    // should have 2 entries (limit is provided), latest created batch tests should be present
    assertThat(batchTestInfoList, hasSize(2));
    assertEquals(batchTestInfoList.get(0).getTestId(),
        batchTests.get("Model_2_batchTestNo_22").getId());
    assertEquals(batchTestInfoList.get(1).getTestId(),
        batchTests.get("Model_2_batchTestNo_21").getId());
  }

  @Test
  public void testListBatchTest_startIndexIsProvidedAs1_getsPageTwo() {
    doDatabaseSetUp();
    ModelBO model = models.get("limitAndStartIndex");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId())
        + "&startIndex=1";

    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();

    // should have 2 entries (total entries = 22, default limit = 20)
    assertThat(batchTestInfoList, hasSize(2));
    assertEquals(batchTestInfoList.get(0).getTestId(),
        batchTests.get("Model_2_batchTestNo_2").getId());
    assertEquals(batchTestInfoList.get(1).getTestId(),
        batchTests.get("Model_2_batchTestNo_1").getId());
  }

  @Test
  public void testListBatchTest_limitAndStartIndexProvided_verifyResponse() {
    doDatabaseSetUp();
    ModelBO model = models.get("limitAndStartIndex");
    String apiPath = constructBatchTestApiPath(client.getId(), project.getId(), model.getModelId())
        + "&startIndex=2&limit=5";

    ResponseEntity<BatchTestResultsResponse> entity = this.restTemplate
        .getForEntity(apiPath, BatchTestResultsResponse.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    BatchTestResultsResponse resultsResponse = entity.getBody();
    assertNotNull(resultsResponse.getBatchTestInfo());

    List<BatchTestInfo> batchTestInfoList = resultsResponse.getBatchTestInfo();

    // should have 5 entries from page 3 => batchTests Nos: 8, 9, 10, 11, 12
    assertThat(batchTestInfoList, hasSize(5));
    assertEquals(batchTestInfoList.get(0).getTestId(),
        batchTests.get("Model_2_batchTestNo_12").getId());
    assertEquals(batchTestInfoList.get(1).getTestId(),
        batchTests.get("Model_2_batchTestNo_11").getId());
    assertEquals(batchTestInfoList.get(2).getTestId(),
        batchTests.get("Model_2_batchTestNo_10").getId());
    assertEquals(batchTestInfoList.get(3).getTestId(),
        batchTests.get("Model_2_batchTestNo_9").getId());
    assertEquals(batchTestInfoList.get(4).getTestId(),
        batchTests.get("Model_2_batchTestNo_8").getId());
  }


  private String constructBatchTestApiPath(Integer clientId, Integer projectId, String modelId) {
    return constructBaseAPIPath(clientId, projectId, modelId)
        .append("/batch_tests")
        .append("?testModelType=" + Constants.DIGITAL_MODEL)
        .toString();
  }

  private StringBuilder constructBaseAPIPath(Integer clientId, Integer projectId, String modelId) {
    return new StringBuilder()
        .append("/nltools/private/v1")
        .append("/clients/")
        .append(clientId)
        .append("/projects/")
        .append(projectId)
        .append("/models/")
        .append(modelId);
  }

}
