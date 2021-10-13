package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.ApiUtil_NegativeTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class Negative_Scenario_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Autowired
  ApiUtil_NegativeTest apiUtil_Negative;

  @Autowired
  private AppConfig appConfig;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verify() throws IOException, InterruptedException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();
    Integer Invalid_projectID = projectID + 10;

    //Get project using invalid projectID
    apiUtil_Negative.testGetProject(Invalid_projectID);

    //POST File import
    String responseToken = apiUtil.testFileImportLargeFile();
    String json_D1 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D1 = apiUtil.testColMapping(responseToken, json_D1);

    //POST dataset
    String dsetName1 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    DatasetBO dataset1 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName1, uri_D1);
    AddDatasetRequest addDset1 = IntegrationUtilTest.addDatasetReq(dataset1, projectID);
    ResponseEntity<DatasetBO> createDatasetEntity = apiUtil.testCreateDataset(addDset1);
    Integer dataset1ID = createDatasetEntity.getBody().getId();

    //Add Invalid dataset to a project
    Integer Invalid_datasetID = dataset1ID + 10;
    ResponseEntity<Void> linkProjDset1 = apiUtil_Negative
        .linkDsetsToProj(clientID, Invalid_projectID, Invalid_datasetID);
    assertEquals(HttpStatus.NOT_FOUND, linkProjDset1.getStatusCode());

    //ADD dataset to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset1ID);
    //Verify conflict when project and dataset mapping already exists
    ResponseEntity<Void> linkProjToDset1 = apiUtil_Negative
        .linkDsetsToProj(clientID, projectID, dataset1ID);
    assertEquals(HttpStatus.CONFLICT, linkProjToDset1.getStatusCode());

    //Transform invalid dataset
    ResponseEntity<TaskEventDetail> transfromDset = apiUtil_Negative
        .testDatasetTransform(clientID, projectID, Invalid_datasetID, "false");
    assertEquals(HttpStatus.NOT_FOUND, transfromDset.getStatusCode());

    //Transform dataset
    apiUtil.testDatasetTransform(clientID, projectID, dataset1ID, "false");

    //Transform already transformed dataset
    ResponseEntity<TaskEventDetail> transfromDataset = apiUtil_Negative
        .testDatasetTransform(clientID, projectID, dataset1ID, "false");
    //assertEquals(HttpStatus.CONFLICT, transfromDataset.getStatusCode()); BUG Response code 500 instead of 409

    //Get status of a dataset after transformation
    apiUtil_Negative.getDsetTransformationStatus(projectID, Invalid_datasetID);

    //POST File import for dataset2
    String responseToken_D2 = apiUtil.testFileImport();
    String json_D2 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D2 = apiUtil.testColMapping(responseToken_D2, json_D2);

    //Create dataset2 using existing dataset name
    DatasetBO dataset2 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName1, uri_D2);
    AddDatasetRequest addDset2Request = IntegrationUtilTest.addDatasetReq(dataset2, projectID);
    apiUtil_Negative.testCreateDataset(addDset2Request); //BUG NT-2350

   //Get config using non-existing configID
    //MWB only creates odd numbered ids so using even number for configID always results in an error scenario
    String Invalid_configID = "10000002";
    apiUtil_Negative.testGetConfigData(String.valueOf(clientID), Invalid_configID);

    //Create a model using non-existing project, dataset and config Id
    apiUtil_Negative.testCreateModel(cId, clientID, Invalid_projectID, Invalid_configID, Invalid_datasetID);

    //Get valid default config
    ResponseEntity<ModelConfigCollection> getProjectConfig = apiUtil
        .testProjectConfig(clientID, projectID);
    String configID = IntegrationUtilTest.getDefaultConfigId(getProjectConfig);

    //Create a model
    ResponseEntity<ModelBO> createModelEntity = apiUtil.testCreateModel(cId, clientID, projectID, configID, dataset1ID,
        Constants.DIGITAL_MODEL);
    Integer modelDBId = createModelEntity.getBody().getId();
    //Build a model
    apiUtil.testModelBuild(clientID, modelDBId);

    Thread.sleep(appConfig.getTestCaseLongerTimeout());
    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, modelDBId);
    //Get model
    String Invalid_modelUUID = apiUtil.testGetModel(clientID, projectID).getBody().get(0).getId();

    //Utterance test
    ResponseEntity<EvaluationResponse> utteranceTest = apiUtil_Negative
        .utteranceTest(clientID, projectID, Invalid_modelUUID);
    //assertEquals(HttpStatus.NOT_FOUND, utteranceTest.getStatusCode()); BUG Response code 500 instead of 404

    //BATCH TEST
    ResponseEntity<EvaluationResponse> postBatchTests = apiUtil_Negative
        .batchTest(clientID, projectID, dataset1ID, Invalid_modelUUID);
    //assertEquals(HttpStatus.NOT_FOUND, postBatchTests.getStatusCode());//BUG Response code 200 instead of 404

    apiUtil_Negative.listBatchTests(clientID, Invalid_projectID, Invalid_modelUUID);

    /*
      Speech Testing
     */

    /// upload speech config
    ResponseEntity<ModelConfigBO> modelConfigBOResponseEntity = apiUtil.testuploadConfig(clientID,projectID);
    configID = modelConfigBOResponseEntity.getBody().getId().toString();


    //Create a model
    createModelEntity = apiUtil
        .testCreateModel(cId, clientID, projectID, configID, dataset1ID,Constants.DIGITAL_SPEECH_MODEL);

    Integer speechModelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);
    //Build a model
    apiUtil.testModelBuild(clientID, speechModelDBId);

    Thread.sleep(appConfig.getTestCaseLongerTimeout());
    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, speechModelDBId);
    //Get model
    String modelUUID = apiUtil.testGetModel(clientID, projectID).getBody().get(1).getModelId();


    // With direct URL
    ResponseEntity<String> utteranceTestforSpeech = apiUtil_Negative.utteranceTestWithInvalidData(clientID,projectID,modelUUID);


  }
}
