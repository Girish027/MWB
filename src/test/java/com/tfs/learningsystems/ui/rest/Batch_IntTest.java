package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.CidGenerator;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.testutil.OAuth2Generate;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.BatchTestInfo;
import com.tfs.learningsystems.ui.model.BatchTestInfo.TypeEnum;
import com.tfs.learningsystems.ui.model.BatchTestResultsResponse;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class Batch_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Autowired
  OAuth2Generate oAuth2Generate;

  @Autowired
  CidGenerator cidGenerator;

  @Autowired
  private TestRestTemplate restTemplate;

  private String basePath;

  @Autowired
  private AppConfig appConfig;

  /* Test steps :
    1 Create a dataset with intent/RU tag
    2 Create a model using dataset created
    3 Request batch test
    4 Verify utterance test and batch test results
    5 Delete  dataset
    6 Delete model
   */
  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyBatchTest() throws InterruptedException, IOException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cid = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cid, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();

    // Get project
    apiUtil.testGetProject(clientID, projectID, createProjectEntity);

    //POST File import
    String responseToken = apiUtil.testFileImportLargeFile();
    String json = IntegrationUtilTest.getJsonWithTags(1);
    String uri = apiUtil.testColMapping(responseToken, json);

    //POST dataset
    String dsetName = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    DatasetBO dataset = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName, uri);
    AddDatasetRequest addDset = IntegrationUtilTest.addDatasetReq(dataset, projectID);
    ResponseEntity<DatasetBO> createDatasetEntity = apiUtil.testCreateDataset(addDset);
    Integer datasetID = createDatasetEntity.getBody().getId();
    apiUtil.testGetDataset(clientID, projectID, datasetID, createDatasetEntity);

    //ADD dataset to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, datasetID);
    //List datasets of a project
    ResponseEntity<DatasetsDetail> listDsetByProj = apiUtil.testDsetsOfProj(clientID, projectID);

    assertEquals(1, listDsetByProj.getBody().size());
    assertEquals(dataset.getName(), listDsetByProj.getBody().get(0).getName());

    //Transform dataset
    apiUtil.testDatasetTransform(clientID, projectID, datasetID, "false");
    //Get status of a dataset after transformation
    apiUtil.getDsetTransformationStatus(clientID, projectID, datasetID);

    ResponseEntity<ModelConfigCollection> getProjectConfig = apiUtil
        .testProjectConfig(clientID, projectID);
    String configID = IntegrationUtilTest.getDefaultConfigId(getProjectConfig);

    //Get config
    apiUtil.testGetConfigData(String.valueOf(clientID), configID).getBody().getId();
    //Create a model
    ResponseEntity<ModelBO> createModelEntity = apiUtil
        .testCreateModel(cid, clientID, projectID, configID, datasetID,Constants.DIGITAL_MODEL);

    Integer modelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);
    //Build a model
    apiUtil.testModelBuild(clientID, modelDBId);

    Thread.sleep(appConfig.getTestCaseLongerTimeout());
    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, modelDBId);
    //Get model
    String modelUUID = apiUtil.testGetModel(clientID, projectID).getBody().get(0).getModelId();

    //Utterance test
    ResponseEntity<EvaluationResponse> utteranceTest = apiUtil
        .transcriptionTest(clientID, projectID, modelUUID,Constants.DIGITAL_MODEL);

    //BATCH TEST
    String batchTestID = apiUtil.batchTest(clientID, projectID, datasetID, modelUUID);
    String batchTestResult = apiUtil
        .getBatchTestResult(clientID, projectID, modelUUID, batchTestID);
    Thread.sleep(2000);
    List<String> ls = (List<String>) IntegrationUtilTest.decodeBatchTestMessage(batchTestResult);

    BatchTestResultsResponse batchTestResultsResponse = apiUtil
        .listBatchTests(clientID, projectID, modelUUID);

    TFSModel model = apiUtil.testGetModel(clientID, projectID).getBody().get(0);
    assertEquals(batchTestResultsResponse.getModelId(), modelUUID);
    assertEquals(batchTestResultsResponse.getModelName(), model.getName());
    assertEquals(batchTestResultsResponse.getModelVersion(),
        ((Integer) model.getVersion()).toString());
    assertEquals(batchTestResultsResponse.getModelDescription(), model.getDescription());
    assertEquals(batchTestResultsResponse.getProjectId(), model.getProjectId());

    assertEquals(batchTestResultsResponse.getBatchTestInfo().size(), 1);
    BatchTestInfo batchTestInfo = batchTestResultsResponse.getBatchTestInfo().get(0);
    assertEquals(batchTestInfo.getTestId(), batchTestID);
    assertEquals(batchTestInfo.getType(), TypeEnum.DATASETS);
    assertEquals(batchTestInfo.getBatchTestName(), "BatchTest_" + batchTestInfo.getCreatedAt());

    ////////////////////////////////////////////////
    //// Creating a Speech model       /////////////
    ////////////////////////////////////////////////

    /// upload speech config
    ResponseEntity<ModelConfigBO> modelConfigBOResponseEntity = apiUtil.testuploadConfig(clientID,projectID);
    configID = modelConfigBOResponseEntity.getBody().getId().toString();


    //Create a model
    createModelEntity = apiUtil
        .testCreateModel(cid, clientID, projectID, configID, datasetID,Constants.DIGITAL_SPEECH_MODEL);

    Integer speechModelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);
    //Build a model
    apiUtil.testModelBuild(clientID, speechModelDBId);

    Thread.sleep(appConfig.getTestCaseLongerTimeout());
    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, speechModelDBId);
    //Get model
    modelUUID = apiUtil.testGetModel(clientID, projectID).getBody().get(1).getModelId();

    //Utterance test
    // With file upload (doesnt matter whether it is recorded or uploaded)
    ResponseEntity<EvaluationResponse> utteranceTestforSpeech = apiUtil
        .utteranceSpeechTestRecording(clientID, projectID, modelUUID);

    // With direct URL
    utteranceTestforSpeech = apiUtil
        .utteranceSpeechTestLink(clientID, projectID, modelUUID);

    //Digital transcription test for speech model
   utteranceTest = apiUtil
        .transcriptionTest(clientID, projectID, modelUUID, Constants.DIGITAL_MODEL);

    apiUtil.testModelDelete(clientID, projectID, speechModelDBId);

    apiUtil.testGetDeleteModel(clientID, projectID, speechModelDBId);

    //Delete model
//    apiUtil.testModelDelete(clientID, projectID, modelDBId);
//
//    apiUtil.testGetDeleteModel(clientID, projectID, modelDBId);

    //Delete dataset
    apiUtil.testDeleteDataset(clientID, projectID, datasetID);

    Thread.sleep(50000);
  }
}
