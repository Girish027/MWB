package com.tfs.learningsystems.ui.rest;


import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.ModelConfigDetail;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class Model_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyModel() throws InterruptedException, IOException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Resources api
    apiUtil.testGetDatatypes();
    apiUtil.testGetLocales();
    apiUtil.testGetVerticals();

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();

    //POST File import
    String responseToken = apiUtil.testFileImport();
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
    Assert.assertEquals(1, listDsetByProj.getBody().size());
    Assert.assertEquals(dataset.getName(), listDsetByProj.getBody().get(0).getName());
    //Transform dataset
    apiUtil.testDatasetTransform(clientID, projectID, datasetID, "false");
    //Get status of a dataset after transformation
    apiUtil.getDsetTransformationStatus(clientID, projectID, datasetID);



    // 1st way: post config, configure model and than do build action on it
    //Post config
    ResponseEntity<ModelConfigBO> modelConfig = apiUtil.testPostConfig(projectID,
        "support_files/config.json");
    String configID = modelConfig.getBody().getId().toString();
    //Get config
    ResponseEntity<ModelConfigBO> getModelConfig = apiUtil.testGetConfig(configID);
    Assert.assertEquals(modelConfig.getBody(), getModelConfig.getBody());
    //Get config data
    ResponseEntity<ModelConfigDetail> getConfigData = apiUtil
        .testGetConfigData(String.valueOf(clientID), configID);
    Assert.assertEquals(modelConfig.getBody().getConfigFile(),
        getConfigData.getBody().getConfigFile());
    Assert.assertEquals(modelConfig.getBody().getName(), getConfigData.getBody().getName());
    Assert.assertEquals(modelConfig.getBody().getDescription(),
        getConfigData.getBody().getDescription());
    //assertEquals(modelConfig.getBody().getProjectId(), getConfigData.getBody().getProjectId()); BUG NT-2260
    Assert
        .assertEquals(modelConfig.getBody().getCreatedAt(), getConfigData.getBody().getCreatedAt());

    ResponseEntity<ModelConfigCollection> getProjectConfig = apiUtil
        .testProjectConfig(clientID, projectID);

    String default_configID = IntegrationUtilTest.getDefaultConfigId(getProjectConfig);
    //Get config
    apiUtil.testGetConfigData(String.valueOf(clientID), default_configID).getBody().getId();
    //Get system default config data
    apiUtil.testGetConfigData(String.valueOf(clientID), default_configID);

    //Create a model
    ResponseEntity<ModelBO> createModelEntity = apiUtil
        .testCreateModel(cId, clientID, projectID, default_configID, datasetID,
            Constants.DIGITAL_MODEL);
    Integer modelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);
    //Build a model
    apiUtil.testModelBuild(clientID, modelDBId);

    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, modelDBId);
    // Get model
    apiUtil.testGetModel(clientID, projectID);

    Thread.sleep(70000);

    List<ProjectModelRequest> projectModels = new ArrayList<>();

    ProjectModelRequest projectModelRequest=new ProjectModelRequest();

    projectModelRequest.setModelId(modelDBId.toString());
    projectModelRequest.setProjectId(projectID.toString());

    projectModels.add(projectModelRequest);

    // TODO:  service account needs to be set up for github for deployment
    //apiUtil.testPublishModel(clientID, projectModels);


    apiUtil.testModelDelete(clientID, projectID, modelDBId);

    apiUtil.testGetDeleteModel(clientID, projectID, modelDBId);

    // 2nd way: Build model with one single post call

    //Create a model
    createModelEntity = apiUtil
        .testPostModelWithLastUsedConfig(cId, clientID, projectID, datasetID,
            Constants.DIGITAL_MODEL);
    modelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);

    //Get state of a model after building
    apiUtil.getModelState(clientID, projectID, modelDBId);

    // Get Training data for model
    apiUtil.getModelTrainingData(clientID, projectID, modelDBId);
    
    // Get model
    apiUtil.testGetModel(clientID, projectID).getBody().get(0).getModelId();
  }

}
