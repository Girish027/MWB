package com.tfs.learningsystems.ui.rest;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.IntentResponse;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class IntentAndSearch_IntTest extends ApiUtilTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyIntentAndSearch() throws IOException, InterruptedException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cId = IntegrationUtilTest.getCId(clients.getBody());

    //Post project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();

    //POST File import
    String responseToken = apiUtil.testFileImport();
    String json_D1 = IntegrationUtilTest.getJsonWithTags(2);
    String uri_D1 = apiUtil.testColMapping(responseToken, json_D1);
    //POST dataset1
    String dsetName1 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    DatasetBO dataset1 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName1, uri_D1);
    AddDatasetRequest addDset1Request = IntegrationUtilTest.addDatasetReq(dataset1, projectID);
    ResponseEntity<DatasetBO> createDatasetEntity = apiUtil.testCreateDataset(addDset1Request);
    Integer dataset1ID = createDatasetEntity.getBody().getId();
    //ADD dataset1 to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset1ID);
    //List datasets of a project
    ResponseEntity<DatasetsDetail> listDsetByProj = apiUtil.testDsetsOfProj(clientID, projectID);
    assertEquals(1, listDsetByProj.getBody().size());
    assertEquals(dataset1.getName(), listDsetByProj.getBody().get(0).getName());
    //Transform dataset1
    apiUtil.testDatasetTransform(clientID, projectID, dataset1ID, "false");
    //Get status of a dataset1 after transformation
    ResponseEntity<TaskEventBO> getTransformStatus_D1 = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset1ID);

    Thread.sleep(1000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> taggingGuide = apiUtil
        .getTaggingGuide(projectID);
    Thread.sleep(60000);
    assertTrue(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide.getBody()).isEmpty());

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
    apiUtil.testDatasetTransform(clientID, projectID, dataset2ID, "false");
    //Get status of a dataset2 after transformation
    ResponseEntity<TaskEventBO> getTransformStatus_D2 = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset2ID);

    Thread.sleep(1000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagGuide = apiUtil
        .getTaggingGuide(projectID);
    assertFalse(
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).isEmpty());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(0)
            .getIntent());
    assertEquals("12", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(0)
            .getCount()));
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(1)
            .getIntent());
    assertEquals("10", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(1)
            .getCount()));

    //Verify tagged transcripts based on make reservation* search query
    SearchRequest searchTran = IntegrationUtilTest
        .getDatasetTranscript(2, "make reservation", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> getTranscriptions_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, searchTran);
    assertEquals("4", getTranscriptions_D2.getBody().getTotal().toString());

    //Verify tagged transcripts based on make reservation -need search query
    SearchRequest searchReq = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset2ID)), 2,
            "make reservation -need", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> getTrans_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, searchReq);
    assertEquals("3", getTrans_D2.getBody().getTotal().toString());

    //Verify tagged transcripts based on exact search
    SearchRequest exactSearchReq = IntegrationUtilTest
        .getDatasetTranscript(2, "\\\"make reservation\\\"", false);

    ResponseEntity<TranscriptionDocumentDetailCollection> getExactTrans_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, exactSearchReq);
    assertEquals("1", getExactTrans_D2.getBody().getTotal().toString());

    //Verify tagged transcripts based on wild card phrases
    SearchRequest wildCardPhrasesSearchReq = IntegrationUtilTest
        .getDatasetTranscript(2, "to make *", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> wildCard_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, wildCardPhrasesSearchReq);
    assertEquals("2", wildCard_D2.getBody().getTotal().toString());

    //Verify tagged transcripts based on wild card phrases
    SearchRequest combineSearchReq = IntegrationUtilTest
        .getDatasetTranscript(3, "flight OR cancel", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> combineSearch_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, combineSearchReq);
    assertEquals("2", combineSearch_D2.getBody().getTotal().toString());

    //Shows all transcripts for dataset2 and verify tagged transcripts
    SearchRequest searchTran_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, searchTran_D2);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_D2.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList_D2 = showAllTranscriptions_D2.getBody()
        .getTranscriptionList();
    for (int m = 0; m < transcriptionList_D2.size(); m++) {
      assertNotNull(showAllTranscriptions_D2.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Shows all transcripts for dataset1 and verify tagged transcripts
    SearchRequest searchTran_D1 = IntegrationUtilTest
        .getDatasetTranscript(2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTranscriptions_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, searchTran_D1);
    assertEquals(Long.valueOf(getTransformStatus_D2.getBody().getRecordsProcessed().longValue()),
        showTranscriptions_D2.getBody().getTotal());
    List<TranscriptionDocumentDetail> list_D2 = showAllTranscriptions_D2.getBody()
        .getTranscriptionList();
    for (int a = 0; a < list_D2.size(); a++) {
      assertNotNull(showTranscriptions_D2.getBody().getTranscriptionList().get(a).getIntent());
    }

    //Get matched intents
    ResponseEntity<IntentResponse> getMatchedIntents = apiUtil.testGetIntents(projectID, "agent");
    assertEquals("agent-query", getMatchedIntents.getBody().get(0));
    assertEquals(Integer.parseInt("1"), getMatchedIntents.getBody().size());

    //Get matched intents
    ResponseEntity<IntentResponse> findMatchedIntents = apiUtil.testGetIntents(projectID, "test");
    assertTrue(findMatchedIntents.getBody().isEmpty());
    assertEquals(Integer.parseInt("0"), findMatchedIntents.getBody().size());

    //Add comment for dataset2
    AddCommentRequest addComment_D2 = IntegrationUtilTest
        .commentUpdate(transcriptionList_D2, currentUserId, "D2 comment");
    apiUtil.testDatasetComment(projectID, dataset2ID, addComment_D2);
    Thread.sleep(2000);

    //Shows all transcripts for dataset2 and verify tagged transcripts having comment
    SearchRequest searchCommentedTran_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> showCommentedTranscriptions_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, searchCommentedTran_D2);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        showCommentedTranscriptions_D2.getBody().getTotal());
    for (int n = 0; n < transcriptionList_D2.size(); n++) {
      assertEquals(addComment_D2.getComment(),
          showCommentedTranscriptions_D2.getBody().getTranscriptionList().get(n).getComment());
    }

    //Shows all transcripts for dataset2 having reservation AND make word and verify tagged transcripts
    SearchRequest search_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "reservation AND make", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTrans_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, search_D2);
    assertEquals("4",
        showTrans_D2.getBody().getTotal().toString());
    List<TranscriptionDocumentDetail> trans_D2 = showTrans_D2.getBody()
        .getTranscriptionList();
    for (int m = 0; m < trans_D2.size(); m++) {
      assertNotNull(showTrans_D2.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Update tag for project
    AddIntentRequest modifyTagIntentRequest = IntegrationUtilTest
        .addNewIntent(transcriptionList_D2, currentUserId, "modified-tag");
    apiUtil.testTagUpdate(projectID, modifyTagIntentRequest);
    Thread.sleep(2000);

    //Shows all transcripts for dataset2 and verify tagged transcripts having intent = modified-tag
    SearchRequest transcriptions_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "granular_intent:modified*", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> show_trans_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, transcriptions_D2);
    assertEquals(Long.valueOf(getTransformStatus_D2.getBody().getRecordsProcessed().longValue()),
        show_trans_D2.getBody().getTotal());
    for (int l = 0; l < trans_D2.size(); l++) {
      assertEquals(modifyTagIntentRequest.getIntent(),
          show_trans_D2.getBody().getTranscriptionList().get(l).getIntent());
      assertEquals(addComment_D2.getComment(),
          show_trans_D2.getBody().getTranscriptionList().get(l).getComment());
    }

    //Verify commented transcriptions based on search query
    SearchRequest searchComment_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "comment:D2", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> commentedTran_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, searchComment_D2);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        commentedTran_D2.getBody().getTotal());

    //Verify transcriptions based on combined search segments
    SearchRequest combinedSearchSegments_D2 = IntegrationUtilTest
        .getDatasetTranscript(2, "granular_intent:mo* AND comment:D2", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> combinedSearchSegmentsTran_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, combinedSearchSegments_D2);
    assertEquals("11",
        combinedSearchSegmentsTran_D2.getBody().getTotal().toString());

    //Shows all transcripts for dataset1 having reservation word and verify tagged transcripts
    SearchRequest searchTranRequest_D1 = IntegrationUtilTest
        .getDatasetTranscript(2, "reser*", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTranscriptions_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, searchTranRequest_D1);
    assertEquals("0",
        showTranscriptions_D1.getBody().getTotal().toString());

    //Shows all transcripts for dataset1 and verify transcripts
    SearchRequest searchTranD1 = IntegrationUtilTest
        .getDatasetTranscript(2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, searchTranD1);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_D1.getBody().getTotal());
    List<TranscriptionDocumentDetail> untagged_transcriptionList_D1 = showAllTranscriptions_D1
        .getBody()
        .getTranscriptionList();
    for (int b = 0; b < untagged_transcriptionList_D1.size(); b++) {
      assertEquals("modified-tag",
          showAllTranscriptions_D1.getBody().getTranscriptionList().get(b).getIntent());
    }

    //Bulk tag dataset1
    AddIntentRequest addIntent = IntegrationUtilTest
        .bulkTagUpdate(untagged_transcriptionList_D1, currentUserId);
    apiUtil.testTagProject(projectID, addIntent);
    Thread.sleep(8000);

    //Shows all transcripts for dataset1 and verify tagged transcripts
    SearchRequest tagged_transcriptions_D1 = IntegrationUtilTest
        .getDatasetTranscript(2, "granular_intent:test*", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> getTaggedTranscriptions_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, tagged_transcriptions_D1);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        getTaggedTranscriptions_D1.getBody().getTotal());

    //Shows all transcripts for dataset1 having reservation word and verify tagged transcripts
    SearchRequest searchTranscriptions_D1 = IntegrationUtilTest
        .getDatasetTranscript(3, "granular_intent:modified-tag", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTran_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, searchTranscriptions_D1);
    assertEquals("0",
        showTran_D1.getBody().getTotal().toString());

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagging_Guide = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(0).getIntent());
    assertEquals(Long.parseLong("22"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(0).getCount());
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(1).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(1).getCount());
    assertEquals("modified-tag",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(2).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(2).getCount());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(3).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody()).get(3).getCount());

    String intentIdForUpdate = IntegrationUtilTest.taggingGuideDocument(tagging_Guide.getBody())
        .get(0).getId();

    //Modify tag in tagging guide.
    HttpEntity<String> httpEntity = IntegrationUtilTest
        .updateTagInTaggingGuide("/intent", "Update-Intent"); //test-intent
    ResponseEntity<TaggingGuideDocument> updatedEntity =
        apiUtil.testTagUpdateInTaggingGuide(projectID, httpEntity, intentIdForUpdate);
    assertEquals(HttpStatus.OK, updatedEntity.getStatusCode());
    assertEquals("Update-Intent", updatedEntity.getBody().getIntent());
    Thread.sleep(5000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagging_Guide1 = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("Update-Intent",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(0).getIntent());
    assertEquals(Long.parseLong("22"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(0).getCount());
    assertEquals("test-rutag",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(0).getRutag());
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(1).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(1).getCount());
    assertEquals("modified-tag",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(2).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(2).getCount());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(3).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(3).getCount());

    //Get matched intents
    ResponseEntity<IntentResponse> getMatchingIntents = apiUtil
        .testGetIntents(projectID, "Update-Int");
    assertEquals("Update-Intent", getMatchingIntents.getBody().get(0));
    //assertEquals(Integer.parseInt("1"), getMatchingIntents.getBody().size()); BUG NT-2325

    String intentId = IntegrationUtilTest.taggingGuideDocument(tagging_Guide1.getBody()).get(0)
        .getId();

    //Shows all transcripts for dataset1 and verify untagged transcripts
    SearchRequest search_D1 = IntegrationUtilTest
        .getDatasetTranscript(3, "granular_intent:Update*", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTranscription_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_D1);
    assertEquals(Long.valueOf(getTransformStatus_D1.getBody().getRecordsProcessed().longValue()),
        showTranscription_D1.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList_D1 = showTranscription_D1.getBody()
        .getTranscriptionList();
    for (int m = 0; m < transcriptionList_D1.size(); m++) {
      assertEquals("Update-Intent",
          showTranscription_D1.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Delete Intent
    apiUtil.deleteIntentInTaggingGuide(projectID, intentId);
    Thread.sleep(1000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagging_Guide2 = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(0).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(0).getCount());
    assertEquals("modified-tag",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(1).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(1).getCount());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(2).getIntent());
    assertEquals(Long.parseLong("0"),
        IntegrationUtilTest.taggingGuideDocument(tagging_Guide2.getBody()).get(2).getCount());

    //Shows all transcripts for project and verify untagged transcripts
    SearchRequest search = IntegrationUtilTest
        .getDatasetTranscript(3, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTranscriptions = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search);
    assertEquals("11", showTranscriptions.getBody().getTotal().toString());
    List<TranscriptionDocumentDetail> list = showTranscriptions.getBody()
        .getTranscriptionList();
    for (int m = 0; m < list.size(); m++) {
      assertNull(showTranscriptions.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Delete tagging guide
    apiUtil.deleteTaggingGuide(projectID);

    //Delete datasets
    //apiUtil.deleteDataset(projectID, dataset1ID); BUG NT-2276
    apiUtil.testDeleteDataset(clientID, projectID, dataset1ID);
    apiUtil.testDeleteDataset(clientID, projectID, dataset2ID);

    //Delete project
    apiUtil.testDeleteProject(clientID, projectID);

  }

}
