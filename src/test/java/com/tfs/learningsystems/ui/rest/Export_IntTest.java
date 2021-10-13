package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import java.io.IOException;
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
public class Export_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyExport() throws IOException, InterruptedException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());

    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();
    // Get project
    apiUtil.testGetProject(clientID, projectID, createProjectEntity);

    //POST File import
    String responseToken = apiUtil.testFileImport();
    String json = IntegrationUtilTest.getJsonWithTags(2);
    String uri = apiUtil.testColMapping(responseToken, json);

    //POST dataset without intent/RU tag
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
    ResponseEntity<TaskEventBO> getTransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, datasetID);
    Thread.sleep(1000);

    //Export dataset
    apiUtil.testExportDataset(clientID, projectID, datasetID);
    IntegrationUtilTest.testDatasetExport(); //BUG NT-2269

    //Export tagging guide
    apiUtil.testExportTaggingGuide(clientID, projectID); //BUG NT-2233

    //Delete dataset
    apiUtil.testDeleteDataset(clientID, projectID, datasetID);

  }

}
