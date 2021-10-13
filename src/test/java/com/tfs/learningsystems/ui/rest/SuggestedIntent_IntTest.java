package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
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
public class SuggestedIntent_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Ignore
  //BUG  NT-2947
  @Test
  public void verifySuggestedIntent() throws IOException, InterruptedException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();

    //POST File import for dataset1
    String responseToken_D1 = apiUtil.testFileImport();
    String json_D1 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D1 = apiUtil.testColMapping(responseToken_D1, json_D1);

    //POST dataset1
    String dsetName1 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    DatasetBO dataset1 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName1, uri_D1);
    AddDatasetRequest addDset1 = IntegrationUtilTest.addDatasetReq(dataset1, projectID);
    ResponseEntity<DatasetBO> createDatasetEntity = apiUtil.testCreateDataset(addDset1);
    Integer dataset1ID = createDatasetEntity.getBody().getId();

    //ADD dataset1 to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset1ID);
    //List dataset1 of a project
    ResponseEntity<DatasetsDetail> listDset1ByProj = apiUtil.testDsetsOfProj(clientID, projectID);
    assertEquals(1, listDset1ByProj.getBody().size());
    assertEquals(dataset1.getName(), listDset1ByProj.getBody().get(0).getName());
    //Transform dataset1
    apiUtil.testDatasetTransform(clientID, projectID, dataset1ID, "false");
    //Get status of a dataset1 after transformation
    ResponseEntity<TaskEventBO> getD1TransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset1ID);

    //Get default config
    String configID = apiUtil.testGetConfigData(String.valueOf(clientID), "1").getBody().getId();
    //Create a model
    ResponseEntity<ModelBO> createModelEntity = apiUtil
        .testCreateModel(cId, clientID, projectID, configID, dataset1ID, Constants.DIGITAL_MODEL);
    Integer modelDBId = createModelEntity.getBody().getId();
    apiUtil.testGetModel(clientID, projectID);
    //Build a model
    apiUtil.testModelBuild(clientID, modelDBId);

    //POST File import for dataset2
    String responseToken_D2 = apiUtil.testFileImport();
    String json_D2 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D2 = apiUtil.testColMapping(responseToken_D2, json_D2);

    //POST dataset2
    String datasetName2 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_2";
    DatasetBO dataset2 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, datasetName2, uri_D2);
    AddDatasetRequest addDset2Request = IntegrationUtilTest.addDatasetReq(dataset2, projectID);
    ResponseEntity<DatasetBO> dataset2Entity = apiUtil.testCreateDataset(addDset2Request);
    Integer dataset2ID = dataset2Entity.getBody().getId();

    //ADD dataset2 to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset2ID);
    //List dataset2 of a project
    ResponseEntity<DatasetsDetail> listDset2ByProj = apiUtil.testDsetsOfProj(clientID, projectID);
    assertEquals(dataset2.getName(), listDset2ByProj.getBody().get(1).getName());
    assertEquals(2, listDset2ByProj.getBody().size());
    //Transform dataset2
    apiUtil.testDatasetTransform(clientID, projectID, dataset2ID, "true");
    //Get status of a dataset2 after transformation
    ResponseEntity<TaskEventBO> getD2TransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset2ID);

    //Shows all transcripts for dataset and verify tagged transcripts
    SearchRequest searchTran = IntegrationUtilTest.getTranscript(Collections
        .singletonList(Integer.toString(dataset2ID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D2 = apiUtil
        .testAllTranscriptions(projectID, searchTran);
    assertEquals(Long.valueOf(getD2TransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_D2.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList = showAllTranscriptions_D2.getBody()
        .getTranscriptionList();
    int transcriptionListSize = showAllTranscriptions_D2.getBody().getTranscriptionList().size();
    System.out.println("Search request...." + showAllTranscriptions_D2.getBody());
  }
}