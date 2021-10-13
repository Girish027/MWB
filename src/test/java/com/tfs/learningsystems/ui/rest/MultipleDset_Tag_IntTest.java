package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.io.IOException;
import java.util.ArrayList;
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
public class MultipleDset_Tag_IntTest extends ApiUtilTest {

  @Autowired
  ApiUtilTest apiUtil;

  /*Test steps
  1 Create dataset1 with intent/RU tag and verify tagging guide
  2 Search transcripts having intent = agent-query in dataset1 and replace intent with intent = agent-q
  3 Verify tagging guide
  4 Create dataset2 using same CSV file as step 1
  5 Verify agent-q is replaced with agent-query for dataset1 and verify tagging guide
  6 Get transcription list having reservation-query for dataset1
  7 Bulk untag for reservation-query for dataset1
  8 Verify transcription lists having reservation-query or untagged transcriptions for both datasets
  9 Verify tagging guide
  10 Bulk tag untagged transcripts
  11 Verify tagging guide
  12 Bulk untag all transcripts and verify intents are displayed in alphabetical order in tagging guide
  13 Tag 2 transcripts with new intent for both datasets and verify tagging guide
  14 Add new tag in tagging guide and apply it to untagged transcript
  15 Update newly added tag in tagging guide and verify whether is applied to datasets transcriptions
  16 Search for untagged transcripts having reservation keyword and add comment to one of the transcript in dataset1
  17 Verify same transcript for dataset2 has the comment
  18 Delete comment for dataset2 and verify its deleted for dataset1 as well
  */
  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }

  @Test
  public void verifyTagForMultipleDset() throws IOException, InterruptedException {

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
    String json_D1 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D1 = apiUtil.testColMapping(responseToken, json_D1);
    //POST dataset
    String dsetName1 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    DatasetBO dataset1 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, dsetName1, uri_D1);
    AddDatasetRequest addDset1 = IntegrationUtilTest.addDatasetReq(dataset1, projectID);
    ResponseEntity<DatasetBO> createDatasetEntity = apiUtil.testCreateDataset(addDset1);
    Integer dataset1ID = createDatasetEntity.getBody().getId();
    //ADD dataset to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset1ID);
    //List datasets of a project
    ResponseEntity<DatasetsDetail> listDsetByProj = apiUtil.testDsetsOfProj(clientID, projectID);

    assertEquals(1, listDsetByProj.getBody().size());
    assertEquals(dataset1.getName(), listDsetByProj.getBody().get(0).getName());
    //Transform dataset
    apiUtil.testDatasetTransform(clientID, projectID, dataset1ID, "false");
    //Get status of a dataset after transformation
    apiUtil.getDsetTransformationStatus(clientID, projectID, dataset1ID);
    //Get status of a dataset after transformation
    ResponseEntity<TaskEventBO> getTransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, dataset1ID);
    Thread.sleep(1000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_D1 = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(0).getIntent());
    assertEquals("6", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(0).getCount()));
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(1).getIntent());
    assertEquals("5", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(1).getCount()));

    //Search transcripts having intent = agent-query in dataset1
    int Transcripts_agent_query = 5;
    SearchRequest searchNewTag = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset1ID)), 2,
            "granular_intent:agent-query", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = apiUtil
        .testAllTranscriptions(projectID, searchNewTag);
    assertEquals(Transcripts_agent_query, (int) (long) showAllTranscriptions.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList_agent_query = showAllTranscriptions
        .getBody()
        .getTranscriptionList();

    //Modify agent-query tag with agent-q in dataset1
    AddIntentRequest modifyTagIntentRequest = IntegrationUtilTest
        .addNewIntent(transcriptionList_agent_query, currentUserId, "agent-q");
    apiUtil.testTagUpdate(projectID, modifyTagIntentRequest);
    Thread.sleep(3000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getUpdatedTaggingGuide_D1 = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(0)
            .getIntent());
    assertEquals("6", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(0)
            .getCount()));
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(1)
            .getIntent());
/*    assertEquals(
            IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(1).getRutag(),
            IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(1)
                    .getRutag());*/
    assertEquals(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(1).getKeywords(),
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(1)
            .getKeywords());
    assertEquals(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_D1.getBody()).get(1).getExamples(),
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(1)
            .getExamples());
    assertEquals("5", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(1)
            .getCount()));
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(2)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getUpdatedTaggingGuide_D1.getBody()).get(2)
            .getCount()));

    //Search transcripts having intent = agent-q in dataset1
    SearchRequest searchUpdatedTag = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset1ID)), 2,
            "granular_intent:agent-q", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_updatedTag = apiUtil
        .testAllTranscriptions(projectID, searchUpdatedTag);
    assertEquals(Transcripts_agent_query,
        (int) (long) showAllTranscriptions_updatedTag.getBody().getTotal());

    SearchRequest searchUpdatedTagCamel = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset1ID)), 2,
            "granular_intent:Agent-Q", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_updatedTagCamel = apiUtil
        .testAllTranscriptions(projectID, searchUpdatedTag);
    assertEquals(Transcripts_agent_query,
        (int) (long) showAllTranscriptions_updatedTagCamel.getBody().getTotal());

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
    Thread.sleep(2000);

    //Search transcripts having intent = agent-q in dataset1
    SearchRequest search_test = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(dataset1ID)), 2,
            "granular_intent:agent-q", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D1 = apiUtil
        .testAllTranscriptions(projectID, search_test);
    assertEquals("0", showAllTranscriptions_D1.getBody().getTotal().toString());

    //Search transcripts having intent = agent-query in dataset1
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_D1_agentQuery = apiUtil
        .testAllTranscriptions(projectID, searchNewTag);
    assertEquals(Transcripts_agent_query,
        (int) (long) showAllTranscriptions_D1_agentQuery.getBody().getTotal());

    //Search transcripts having intent = agent-query in dataset1 and dataset2
    List<String> dsets = new ArrayList<>();
    dsets.add(Integer.toString(dataset1ID));
    dsets.add(Integer.toString(dataset2ID));
    SearchRequest search = IntegrationUtilTest
        .getTranscript(dsets, 2, "granular_intent:agent-query", false);

    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_allDsets = apiUtil
        .testAllTranscriptions(projectID, search);
    int documentCount = Math.toIntExact(
        showAllTranscriptions_allDsets.getBody().getTranscriptionList().get(0).getDocumentCount());
    assertEquals(dsets.size(), documentCount);
    assertEquals(Transcripts_agent_query * documentCount,
        (int) (long) showAllTranscriptions_allDsets.getBody().getTotal() * documentCount);

    SearchRequest capitalTagSearch = IntegrationUtilTest
        .getTranscript(dsets, 2, "granular_intent:AGENT-QUERY", false);

    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_allDsets_CapSearch = apiUtil
        .testAllTranscriptions(projectID, capitalTagSearch);
    int documentCountCapSearch = Math.toIntExact(
        showAllTranscriptions_allDsets_CapSearch.getBody().getTranscriptionList().get(0)
            .getDocumentCount());
    assertEquals(dsets.size(), documentCount);
    assertEquals(Transcripts_agent_query * documentCountCapSearch,
        (int) (long) showAllTranscriptions_allDsets_CapSearch.getBody().getTotal()
            * documentCountCapSearch);

    //Search tagged transcripts in both datasets
    SearchRequest searchTran = IntegrationUtilTest
        .getTranscript(dsets, 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTran_allDsets = apiUtil
        .testAllTranscriptions(projectID, searchTran);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showTran_allDsets.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList = showTran_allDsets.getBody()
        .getTranscriptionList();
    int count = 0;
    for (int q = 0; q < transcriptionList.size(); q++) {
      assertNotNull(showTran_allDsets.getBody().getTranscriptionList().get(q).getIntent());
      count++;
    }
    assertEquals(11, count);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(0).getIntent());
    assertEquals("12", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(0).getCount()));
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(1).getIntent());
    assertEquals("10", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(1).getCount()));
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(2).getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide.getBody()).get(2).getCount()));

    //Get transcription list having reservation-query for dataset1
    int Transcripts_reservation_query = 6;
    SearchRequest search_reservation_query = IntegrationUtilTest
        .getDatasetTranscript(2, "granular_intent:reservation-query", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> tran_reservation_query = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_reservation_query);
    assertEquals(Transcripts_reservation_query,
        (int) (long) tran_reservation_query.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList_reservation_query = tran_reservation_query
        .getBody()
        .getTranscriptionList();

    //Bulk untag for reservation-query for dataset1
    AddIntentRequest delete_reservation_query = IntegrationUtilTest
        .bulkUntagUpdate(transcriptionList_reservation_query, currentUserId);
    apiUtil.untagDataset(projectID, dataset1ID, delete_reservation_query);
    Thread.sleep(1000);

    //Get transcription list having reservation-query for dataset2
    SearchRequest reservation_query_D2 = IntegrationUtilTest
        .getDatasetTranscript(3, "granular_intent:reservation-query", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> tran_reservation_query_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, reservation_query_D2);
    assertEquals("0", tran_reservation_query_D2.getBody().getTotal().toString());

    //Get transcription list having reservation-query for both datasets
    SearchRequest reservation_query = IntegrationUtilTest
        .getTranscript(dsets, 3, "granular_intent:reservation-query", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> reservation_query_transcripts = apiUtil
        .testAllTranscriptions(projectID, reservation_query);
    assertEquals("0", reservation_query_transcripts.getBody().getTotal().toString());

    //Get transcription which are untagged for both datasets
    SearchRequest untagged_transcriptions = IntegrationUtilTest.getTranscript(dsets, 1, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> untagged_transcripts = apiUtil
        .testAllTranscriptions(projectID, untagged_transcriptions);
    assertEquals("6", untagged_transcripts.getBody().getTotal().toString());
    List<TranscriptionDocumentDetail> untagged_transcriptionList = untagged_transcripts.getBody()
        .getTranscriptionList();

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_intent_delete = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(0)
            .getIntent());
    assertEquals("10", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(0)
            .getCount()));
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(1)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(1)
            .getCount()));
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(2)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_intent_delete.getBody()).get(2)
            .getCount()));

    //Bulk tag
    AddIntentRequest addIntent = IntegrationUtilTest
        .bulkTagUpdate(untagged_transcriptionList, currentUserId);
    apiUtil.testTagProject(projectID, addIntent);
    Thread.sleep(2000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> taggingGuide_after_tag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(0)
            .getIntent());
    //assertEquals("test-rutag",IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(0).getRutag()); BUG NT-2201
    assertEquals("12", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(0)
            .getCount()));
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(1)
            .getIntent());
    assertEquals("10", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(1)
            .getCount()));
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(2)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(2)
            .getCount()));
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(3)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_after_tag.getBody()).get(3)
            .getCount()));

    //Bulk untag dataset
    AddIntentRequest deleteIntentRequest = IntegrationUtilTest
        .bulkUntagUpdate(transcriptionList, currentUserId);
    apiUtil.testUntagProject(projectID, deleteIntentRequest);
    Thread.sleep(2000);

    //Verify untagged transcripts
    SearchRequest searchTran_untag = IntegrationUtilTest.getTranscript(dsets, 1, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_untag = apiUtil
        .testAllTranscriptions(projectID, searchTran_untag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_untag.getBody().getTotal());
    for (int u = 0; u < transcriptionList.size(); u++) {
      assertNull(showAllTranscriptions_untag.getBody().getTranscriptionList().get(u).getIntent());
    }
    List<TranscriptionDocumentDetail> partial_transcriptionList = showAllTranscriptions_untag
        .getBody()
        .getTranscriptionList().subList(0, 2);

    //Post stats for datasets in tag page
    SearchRequest search_datasets = IntegrationUtilTest.searchRequest(dsets);
    ResponseEntity<StatsResponse> taggingStats = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals("11",
        taggingStats.getBody().getUnique().getTotal().toString());
    assertEquals("22",
        taggingStats.getBody().getAll().getTotal().toString());
    assertEquals("4",
        taggingStats.getBody().getIntents().toString());

    //Verify tagging guide when all transcripts are untagged
    ResponseEntity<TaggingGuideDocumentDetail[]> taggingGuide_untag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(0).getIntent());
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(1).getIntent());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(2).getIntent());
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(3).getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(0).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(1).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(2).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(3).getCount()));

    //Bulk tag partial transcripts
    AddIntentRequest addIntentPartial = IntegrationUtilTest
        .bulkTagUpdate(partial_transcriptionList, currentUserId);
    apiUtil.testTagProject(projectID, addIntentPartial);
    Thread.sleep(2000);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> taggingGuide_partial_tag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_partial_tag.getBody()).get(0)
            .getIntent());
    assertEquals("4", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_partial_tag.getBody()).get(0)
            .getCount()));

    //Verify partial tagged transcripts in tag page for both datasets
    SearchRequest searchTran_partial = IntegrationUtilTest.getTranscript(dsets, 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showPartialTranscriptions = apiUtil
        .testAllTranscriptions(projectID, searchTran_partial);
    assertEquals("2",
        showPartialTranscriptions.getBody().getTotal().toString());
    for (int m = 0; m < partial_transcriptionList.size(); m++) {
      assertEquals("test-intent",
          showPartialTranscriptions.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Post stats for datasets in tag page when partially tagged
    ResponseEntity<StatsResponse> taggingStats_partial = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals("4",
        taggingStats_partial.getBody().getIntents().toString());
    assertEquals("22",
        taggingStats_partial.getBody().getAll().getTotal().toString());
    assertEquals("4",
        taggingStats_partial.getBody().getAll().getTagged().toString());
    assertNotEquals("0",
        taggingStats_partial.getBody().getAll().getPercent().toString());
    assertEquals("11",
        taggingStats_partial.getBody().getUnique().getTotal().toString());
    assertEquals("2",
        taggingStats_partial.getBody().getUnique().getTagged().toString());
    assertNotEquals("0",
        taggingStats_partial.getBody().getUnique().getPercent().toString());

    //Bulk untag dataset
    deleteIntentRequest = IntegrationUtilTest
        .bulkUntagUpdate(transcriptionList, currentUserId);
    apiUtil.testUntagProject(projectID, deleteIntentRequest);
    Thread.sleep(2000);

    //Verify untagged transcripts
    searchTran_untag = IntegrationUtilTest.getTranscript(dsets, 1, "", false);
    showAllTranscriptions_untag = apiUtil
        .testAllTranscriptions(projectID, searchTran_untag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_untag.getBody().getTotal());
    for (int u = 0; u < transcriptionList.size(); u++) {
      assertNull(showAllTranscriptions_untag.getBody().getTranscriptionList().get(u).getIntent());
    }
    partial_transcriptionList = showAllTranscriptions_untag
        .getBody()
        .getTranscriptionList().subList(0, 2);

    //Post stats for datasets in tag page
    search_datasets = IntegrationUtilTest.searchRequest(dsets);
    taggingStats = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals("11",
        taggingStats.getBody().getUnique().getTotal().toString());
    assertEquals("22",
        taggingStats.getBody().getAll().getTotal().toString());
    assertEquals("4",
        taggingStats.getBody().getIntents().toString());

    //Verify tagging guide when all transcripts are untagged
    taggingGuide_untag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(0).getIntent());
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(1).getIntent());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(2).getIntent());
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(3).getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(0).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(1).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(2).getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(taggingGuide_untag.getBody()).get(3).getCount()));

    //Bulk tag partial transcripts
    addIntentPartial = IntegrationUtilTest
        .bulkTagUpdate(partial_transcriptionList, currentUserId);
    apiUtil.testTagProject(projectID, addIntentPartial);
    Thread.sleep(2000);

    //POST File import for dataset3
    String responseToken_D3 = apiUtil.testNoTagFileImport();
    String json_D3 = IntegrationUtilTest.getJsonWithTags(1);
    String uri_D3 = apiUtil.testColMapping(responseToken_D3, json_D3);

    //POST dataset3
    String datasetName3 =
        methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_3";
    DatasetBO dataset3 = IntegrationUtilTest.getTestDatasetObject(clientID, projectID, datasetName3, uri_D3);
    AddDatasetRequest addDset3Request = IntegrationUtilTest.addDatasetReq(dataset3, projectID);
    ResponseEntity<DatasetBO> dataset3Entity = apiUtil.testCreateDataset(addDset3Request);
    Integer dataset3ID = dataset3Entity.getBody().getId();

    //ADD dataset3 to a project created.
    apiUtil.linkDsetsToProj(clientID, projectID, dataset3ID);
    //List dataset3 of a project
    ResponseEntity<DatasetsDetail> listDset3ByProj = apiUtil.testDsetsOfProj(clientID, projectID);
    assertEquals(dataset3.getName(), listDset3ByProj.getBody().get(2).getName());
    assertEquals(3, listDset3ByProj.getBody().size());

    apiUtil.testDatasetTransform(clientID, projectID, dataset3ID, "true");
    Thread.sleep(2000);

    dsets.add(Integer.toString(dataset3ID));

    SearchRequest searchTran_partial_uploadD3 = IntegrationUtilTest
        .getTranscript(dsets, 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showPartialTranscriptions_withD3 = apiUtil
        .testAllTranscriptions(projectID, searchTran_partial_uploadD3);
    assertEquals("2",
        showPartialTranscriptions_withD3.getBody().getTotal().toString());
    for (int m = 0; m < partial_transcriptionList.size(); m++) {
      assertEquals("test-intent",
          showPartialTranscriptions.getBody().getTranscriptionList().get(m).getIntent());
    }

    search_datasets = IntegrationUtilTest.searchRequest(dsets);
    //Post stats for datasets in tag page when partially tagged
    ResponseEntity<StatsResponse> taggingStats_partial_withUpload = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals("4",
        taggingStats_partial_withUpload.getBody().getIntents().toString());
    assertEquals("33",
        taggingStats_partial_withUpload.getBody().getAll().getTotal().toString());
    assertEquals("6",
        taggingStats_partial_withUpload.getBody().getAll().getTagged().toString());
    assertNotEquals("0",
        taggingStats_partial_withUpload.getBody().getAll().getPercent().toString());
    assertEquals("11",
        taggingStats_partial_withUpload.getBody().getUnique().getTotal().toString());
    assertEquals("2",
        taggingStats_partial_withUpload.getBody().getUnique().getTagged().toString());
    assertNotEquals("0",
        taggingStats_partial_withUpload.getBody().getUnique().getPercent().toString());

    // dsets.remove(Integer.toString(dataset3ID));

    //Add a new tag in tagging guide
    ResponseEntity<TaggingGuideDocument> add_tag_tagging_guide = apiUtil
        .testAddTagInTaggingGuide(projectID);
    TaggingGuideDocument addedDoc = add_tag_tagging_guide.getBody();
    assertNotNull(addedDoc.getId());
    assertEquals(addedDoc.getIntent(), "new-tag");
    assertEquals(addedDoc.getRutag(), "new-rutag");
    String intentId = addedDoc.getId();

    //Search untagged transcriptions in dataset2
    SearchRequest untagged_Transcripts = IntegrationUtilTest.getDatasetTranscript(1, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> untagged_Transcripts_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset2ID, untagged_Transcripts);
    assertEquals("9", untagged_Transcripts_D2.getBody().getTotal().toString());
    List<TranscriptionDocumentDetail> transcriptionList_untagged_D2 = untagged_Transcripts_D2
        .getBody()
        .getTranscriptionList().subList(0, 1);

    //Apply newly added tagging guide tag to few transcripts
    AddIntentRequest tagUpdateRequest = IntegrationUtilTest
        .addNewIntent(transcriptionList_untagged_D2, currentUserId,
            add_tag_tagging_guide.getBody().getIntent());
    apiUtil.testTagUpdate(projectID, tagUpdateRequest);
    Thread.sleep(1000);

    //Verify transcripts having intent tag = new-tag in both datasets

    SearchRequest search_taggingGuide_new_tag_Cap = IntegrationUtilTest
        .getTranscript(dsets, 2, "granular_intent:NEW*", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> tagGuideTagTranscriptionsCaps = apiUtil
        .testAllTranscriptions(projectID, search_taggingGuide_new_tag_Cap);
    assertEquals("1",
        tagGuideTagTranscriptionsCaps.getBody().getTotal().toString());

    SearchRequest search_taggingGuide_tag = IntegrationUtilTest
        .getTranscript(dsets, 2, "granular_intent:new*", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> tagGuideTagTranscriptions = apiUtil
        .testAllTranscriptions(projectID, search_taggingGuide_tag);
    assertEquals("1",
        tagGuideTagTranscriptions.getBody().getTotal().toString());
    for (int n = 0; n < tagGuideTagTranscriptions.getBody().getTranscriptionList().size(); n++) {
      assertEquals(add_tag_tagging_guide.getBody().getIntent(),
          tagGuideTagTranscriptions.getBody().getTranscriptionList().get(n).getIntent());
    }
    Thread.sleep(2000);

    //Post stats for datasets in tag page
    ResponseEntity<StatsResponse> tagStats = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals("33",
        tagStats.getBody().getAll().getTotal().toString());
    assertEquals("11",
        tagStats.getBody().getUnique().getTotal().toString());
    assertEquals("5", tagStats.getBody().getIntents().toString());
    assertEquals("9",
        tagStats.getBody().getAll().getTagged().toString());
    assertEquals("3",
        tagStats.getBody().getUnique().getTagged().toString());

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagGuide = apiUtil.getTaggingGuide(projectID);
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(0).getIntent());
    assertEquals("6", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(0).getCount()));
    assertEquals("new-tag",
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(1).getIntent());
    assertEquals("3", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide.getBody()).get(1).getCount()));

    //Modify newly added tag in tagging guide.
    HttpEntity<String> httpEntity = IntegrationUtilTest
        .updateTagInTaggingGuide("/intent", "update-intent");
    ResponseEntity<TaggingGuideDocument> updatedEntity =
        apiUtil.testTagUpdateInTaggingGuide(projectID, httpEntity, intentId);
    assertEquals(HttpStatus.OK, updatedEntity.getStatusCode());
    assertEquals("update-intent", updatedEntity.getBody().getIntent());
    Thread.sleep(1000);

    //POST stats for dataset
    ResponseEntity<StatsResponse> taggingStats_updatedtaggingGuideIntent = apiUtil
        .testPostTaggingStats(projectID, search_datasets);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_updatedtaggingGuideIntent.getBody().getUnique().getTotal());
    assertEquals("5", taggingStats_updatedtaggingGuideIntent.getBody().getIntents().toString());
    assertEquals("9",
        taggingStats_updatedtaggingGuideIntent.getBody().getAll().getTagged().toString());
    assertEquals("3",
        taggingStats_updatedtaggingGuideIntent.getBody().getUnique().getTagged().toString());

    //Verify transcripts does not have new-tag
    ResponseEntity<TranscriptionDocumentDetailCollection> transcriptions = apiUtil
        .testAllTranscriptions(projectID, search_taggingGuide_tag);
    assertEquals("0",
        transcriptions.getBody().getTotal().toString());

    //Verify transcripts having intent tag = unpdate-intent in both datasets
    SearchRequest search_updated_tag = IntegrationUtilTest
        .getTranscript(dsets, 2, "granular_intent:update-intent", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> tagGuideUpdatedTagTranscriptions = apiUtil
        .testAllTranscriptions(projectID, search_updated_tag);
    assertEquals("1",
        tagGuideUpdatedTagTranscriptions.getBody().getTotal().toString());
    for (int j = 0; j < tagGuideUpdatedTagTranscriptions.getBody().getTranscriptionList().size();
        j++) {
      assertEquals("update-intent",
          tagGuideUpdatedTagTranscriptions.getBody().getTranscriptionList().get(j).getIntent());
    }

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> tagGuide_update_intent = apiUtil
        .getTaggingGuide(projectID);
    assertEquals("test-intent",
        IntegrationUtilTest.taggingGuideDocument(tagGuide_update_intent.getBody()).get(0)
            .getIntent());
    assertEquals("6", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide_update_intent.getBody()).get(0)
            .getCount()));
    assertEquals("update-intent",
        IntegrationUtilTest.taggingGuideDocument(tagGuide_update_intent.getBody()).get(1)
            .getIntent());
    assertEquals("3", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(tagGuide_update_intent.getBody()).get(1)
            .getCount()));

    //Search for transcripts having reservation keyword
    SearchRequest search_reservation = IntegrationUtilTest
        .getDatasetTranscript(1, "reservation", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> reservationTranscripts_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_reservation);
    assertEquals("6",
        reservationTranscripts_D1.getBody().getTotal().toString());

    //Search for transcripts having reservation keyword
    SearchRequest search_reservation_camel = IntegrationUtilTest
        .getDatasetTranscript(1, "Reservation", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> reservationTranscripts_Camel_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_reservation_camel);
    assertEquals("6",
        reservationTranscripts_Camel_D1.getBody().getTotal().toString());

    for (int p = 0; p < reservationTranscripts_D1.getBody().getTranscriptionList().size(); p++) {
      //assertTrue(reservationTranscripts_D1.getBody().getTranscriptionList().get(p).getAutoTagString().contains("reservation")); BUG-2132
      assertTrue(reservationTranscripts_D1.getBody().getTranscriptionList().get(p)
          .getTextStringForTagging().contains("reservation"));
    }
    Thread.sleep(1000);
    List<TranscriptionDocumentDetail> transcriptionList_D1 = reservationTranscripts_D1.getBody()
        .getTranscriptionList().subList(4, 5);

    //Add comment for a transcript in dataset1
    AddCommentRequest addCommentRequest = IntegrationUtilTest
        .commentUpdate(transcriptionList_D1, currentUserId, "Test comment");
    apiUtil.testAddComment(projectID, addCommentRequest);
    Thread.sleep(1000);

    //Search for transcripts having comment in dataset2
    SearchRequest search_comment = IntegrationUtilTest.getDatasetTranscript(1, "", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> transcriptWithComment_D2 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_comment);
    assertEquals("1",
        transcriptWithComment_D2.getBody().getTotal().toString());
    assertEquals(transcriptionList_D1.get(0).getTextStringForTagging(),
        transcriptWithComment_D2.getBody().getTranscriptionList().get(0).getTextStringForTagging());
    assertEquals(transcriptionList_D1.get(0).getIntent(),
        transcriptWithComment_D2.getBody().getTranscriptionList().get(0).getIntent());
    for (int p = 0; p < transcriptWithComment_D2.getBody().getTranscriptionList().size(); p++) {
      assertEquals(addCommentRequest.getComment(),
          transcriptWithComment_D2.getBody().getTranscriptionList().get(p).getComment());
    }
    List<TranscriptionDocumentDetail> transcriptionList_D2 = transcriptWithComment_D2.getBody()
        .getTranscriptionList();

    //Delete comment for a transcript in dataset2
    AddCommentRequest deleteCommentRequest = IntegrationUtilTest
        .commentUpdate(transcriptionList_D2, currentUserId, "");
    apiUtil.testAddComment(projectID, deleteCommentRequest);
    Thread.sleep(1000);

    //Search transcript does not have comment in dataset1
    SearchRequest search_comment_D1 = IntegrationUtilTest.getDatasetTranscript(1, "", true);
    ResponseEntity<TranscriptionDocumentDetailCollection> transcriptWithoutComment_D1 = apiUtil
        .testDatasetTranscriptions(projectID, dataset1ID, search_comment_D1);
    assertEquals("0",
        transcriptWithoutComment_D1.getBody().getTotal().toString());

    //Get matched intents
    ResponseEntity<IntentResponse> getMatchedIntents = apiUtil.testGetIntents(projectID, "agent");
    assertEquals("agent-q", getMatchedIntents.getBody().get(0));
    assertEquals("agent-query", getMatchedIntents.getBody().get(1));

    // Test scenario implementation blocked by BUG NT-2144

    //Delete datasets
    apiUtil.testDeleteDataset(clientID, projectID, dataset1ID);
    apiUtil.testDeleteDataset(clientID, projectID, dataset2ID);
    apiUtil.testDeleteDataset(clientID, projectID, dataset3ID);
  }
}
