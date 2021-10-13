package com.tfs.learningsystems.ui.rest;


import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import java.io.IOException;
import java.util.Collections;
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
public class AutoTagging_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  /* Test steps :
    1 Create a dataset with intent/RU tag
    2 Create a dataset without intent/RU tag
    3 Verify second dataset should have intent/RU tags as dataset1
    4 Delete both datasets
   */
  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyAutoTagUpdate() throws InterruptedException, IOException {

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
    Assert.assertEquals(1, listDset1ByProj.getBody().size());
    Assert.assertEquals(dataset1.getName(), listDset1ByProj.getBody().get(0).getName());
    //Transform dataset1
    apiUtil.testDatasetTransform(clientID, projectID, dataset1ID, "false");
    //Get status of a dataset1 after transformation
    ResponseEntity<TaskEventBO> getD1TransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset1ID);

    //Post stats for dataset1 in tag page
    SearchRequest search_D1 = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(dataset1ID)));
    ResponseEntity<StatsResponse> taggingStats_D1 = apiUtil
        .testPostTaggingStats(projectID, search_D1);
    Assert.assertEquals(
        Long.valueOf(getD1TransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_D1.getBody().getAll().getTotal());
    ResponseEntity<StatsResponse> getTaggingStats_D1 = apiUtil
        .getTaggingStats(projectID, dataset1ID);
    Assert.assertEquals("11", Long.toString(getTaggingStats_D1.getBody().getAll().getTagged()));
    Assert
        .assertEquals("100.0", Float.toString(getTaggingStats_D1.getBody().getAll().getPercent()));
    Assert.assertEquals(
        Long.valueOf(getD1TransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_D1.getBody().getAll().getTotal());
    Assert.assertEquals("11", Long.toString(getTaggingStats_D1.getBody().getUnique().getTagged()));
    Assert.assertEquals("100.0",
        Float.toString(getTaggingStats_D1.getBody().getUnique().getPercent()));
    Assert.assertEquals(
        Long.valueOf(getD1TransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_D1.getBody().getUnique().getTotal());

    //Shows all transcripts for dataset1 and verify manual tags
    SearchRequest searchTran_D1 = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset1ID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = apiUtil
        .testAllTranscriptions(projectID, searchTran_D1);
    Assert.assertEquals(
        Long.valueOf(getD1TransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions.getBody().getTotal());
    Assert.assertTrue(
        showAllTranscriptions.getBody().getTranscriptionList().toString().contains("agent-query"));
    Assert.assertTrue(showAllTranscriptions.getBody().getTranscriptionList().toString()
        .contains("reservation-query"));

    /* Retrieving all transcriptions via pagination */
    int limit = 5;
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptionsWithPages = apiUtil
        .testAllTranscriptionsWithPagination(projectID, searchTran_D1, 0, limit);
    TranscriptionDocumentDetailCollection documentCollection = showAllTranscriptions.getBody();
    Long totalDocs = documentCollection.getTotal();

    totalDocs -= limit;
    Long totalPages = (totalDocs / limit);
    if (totalDocs % limit != 0) {
      totalPages++;
    }
    int startIndex = limit;
    for (int j = 0; j < totalPages; j++) {
      ResponseEntity<TranscriptionDocumentDetailCollection> searchEntity = apiUtil
          .testAllTranscriptionsWithPagination(projectID, searchTran_D1, (startIndex + (j * limit)),
              limit);
      documentCollection = searchEntity.getBody();
      Assert
          .assertEquals(documentCollection.getStartIndex().intValue(), (startIndex + (j * limit)));
      Assert.assertEquals(documentCollection.getTranscriptionList().size(),
          totalDocs > limit ? limit : totalDocs);
      totalDocs -= limit;
    }

    //POST File import for dataset2
    String responseToken_D2 = apiUtil.testFileImport();
    String json_D2 = IntegrationUtilTest.getJsonWithTags(2);
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
    Assert.assertEquals(dataset2.getName(), listDset2ByProj.getBody().get(1).getName());
    Assert.assertEquals(2, listDset2ByProj.getBody().size());
    //Transform dataset2
    apiUtil.testDatasetTransform(clientID, projectID, dataset2ID, "false");
    //Get status of a dataset2 after transformation
    ResponseEntity<TaskEventBO> getD2TransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset2ID);

    //Post stats for dataset2 in tag page
    SearchRequest search_D2 = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(dataset2ID)));
    ResponseEntity<StatsResponse> taggingStats_D2 = apiUtil
        .testPostTaggingStats(projectID, search_D2);
    Assert.assertEquals(
        Long.valueOf(getD2TransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_D2.getBody().getAll().getTotal());
    ResponseEntity<StatsResponse> getTaggingStats_D2 = apiUtil
        .getTaggingStats(projectID, dataset2ID);
    Assert.assertEquals("11", Long.toString(getTaggingStats_D2.getBody().getAll().getTagged()));
    Assert
        .assertEquals("100.0", Float.toString(getTaggingStats_D2.getBody().getAll().getPercent()));
    Assert.assertEquals(
        Long.valueOf(getD2TransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_D2.getBody().getAll().getTotal());
    Assert.assertEquals("11", Long.toString(getTaggingStats_D2.getBody().getUnique().getTagged()));
    Assert.assertEquals("100.0",
        Float.toString(getTaggingStats_D2.getBody().getUnique().getPercent()));
    Assert.assertEquals(
        Long.valueOf(getD2TransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_D2.getBody().getUnique().getTotal());

    //Shows all transcripts for dataset2 and verify manual tags
    SearchRequest searchTran_D2 = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset2ID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D2 = apiUtil
        .testAllTranscriptions(projectID, searchTran_D2);
    Assert.assertEquals(
        Long.valueOf(getD2TransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_D2.getBody().getTotal());
    Assert.assertTrue(
        showAllTranscriptions.getBody().getTranscriptionList().toString().contains("agent-query"));
    Assert.assertTrue(showAllTranscriptions.getBody().getTranscriptionList().toString()
        .contains("reservation-query"));
    List<TranscriptionDocumentDetail> transcriptionList = showAllTranscriptions_D2.getBody()
        .getTranscriptionList();
    Assert.assertTrue(showAllTranscriptions_D2.getBody().getTranscriptionList().toString()
        .contains("agent-query"));
    Assert.assertTrue(showAllTranscriptions_D2.getBody().getTranscriptionList().toString()
        .contains("reservation-query"));
    int intentCount = 0;
    for (int l = 0; l < transcriptionList.size(); l++) {
      String intent = transcriptionList.get(l).getIntent();
      if (intent.equals("reservation-query")) {
        intentCount++;
      }
    }
    Assert.assertEquals(6, intentCount);

    //Delete datasets
    apiUtil.testDeleteDataset(clientID, projectID, dataset1ID);
    apiUtil.testDeleteDataset(clientID, projectID, dataset2ID);
  }

}
