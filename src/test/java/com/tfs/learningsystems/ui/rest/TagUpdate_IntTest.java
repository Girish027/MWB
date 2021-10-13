package com.tfs.learningsystems.ui.rest;

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
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@EnableAutoConfiguration
@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TagUpdate_IntTest extends ApiUtilTest {

  @Autowired
  ApiUtilTest apiUtil;

  /* Test steps :
    1 Create a dataset without intent/RU tag and verify tags in tag and tagging guide page
    2 Bulk tag dataset and verify tags in tag and tagging guide page
    3 Bulk untag dataset and verify tags in tag and tagging guide page
    4 Add new tag in tag page and verify newly added tag in tagging guide
    5 Add new tag in tagging guide and verify newly added tag in tag page
    6 Update tag in tagging guide and verify whether it is getting updated in tag page
    7 Update tag in tag page and verify whether it is getting updated in tagging guide
    8 Delete dataset and verify tagging guide
   */


  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }


  @Test
  public void verifyTagUpdate() throws IOException, InterruptedException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());
    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // Create a project POST  method
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
    assertEquals(1, listDsetByProj.getBody().size());
    assertEquals(dataset.getName(), listDsetByProj.getBody().get(0).getName());
    //Transform dataset
    apiUtil.testDatasetTransform(clientID, projectID, datasetID, "false");
    //Get status of a dataset after transformation
    ResponseEntity<TaskEventBO> getTransformStatus = apiUtil
        .getDsetTransformationStatus(clientID, projectID, datasetID);

    Thread.sleep(1000);
    //Export dataset
    apiUtil.testExportDataset(clientID, projectID, datasetID);

    //Post stats for dataset in tag page
    SearchRequest search = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> postTaggingStats = apiUtil
        .testPostTaggingStats(projectID, search);

    Thread.sleep(4000);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        postTaggingStats.getBody().getAll().getTotal());
    assertEquals("0", postTaggingStats.getBody().getIntents().toString());
    //Get stats for dataset
    ResponseEntity<StatsResponse> getTaggingStats = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("0", Long.toString(getTaggingStats.getBody().getAll().getTagged()));
    assertEquals("0.0", Float.toString(getTaggingStats.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats.getBody().getAll().getTotal());
    assertEquals("0", Long.toString(getTaggingStats.getBody().getUnique().getTagged()));
    assertEquals("0.0", Float.toString(getTaggingStats.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats.getBody().getUnique().getTotal());

    //Shows all transcripts for dataset and verify untagged transcripts
    SearchRequest searchTran = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 1, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = apiUtil
        .testAllTranscriptions(projectID, searchTran);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions.getBody().getTotal());
    List<TranscriptionDocumentDetail> transcriptionList = showAllTranscriptions.getBody()
        .getTranscriptionList();
    int transcriptionListSize = showAllTranscriptions.getBody().getTranscriptionList().size();
    for (int m = 0; m < transcriptionListSize; m++) {
      assertNull(showAllTranscriptions.getBody().getTranscriptionList().get(m).getIntent());
    }

    //Bulk tag dataset
    AddIntentRequest addIntentRequest = IntegrationUtilTest
        .bulkTagUpdate(transcriptionList, currentUserId);
    apiUtil.testTagProject(projectID, addIntentRequest);
    Thread.sleep(2000);

    //Post stats for dataset in tagging page
    SearchRequest search_after_tag = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> taggingStats_after_tag = apiUtil
        .testPostTaggingStats(projectID, search_after_tag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_after_tag.getBody().getAll().getTotal());
    assertEquals("1", taggingStats_after_tag.getBody().getIntents().toString());

    //Get stats for dataset after bulk tag
    ResponseEntity<StatsResponse> getTaggingStats_after_tag = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("11", Long.toString(getTaggingStats_after_tag.getBody().getAll().getTagged()));
    assertEquals("100.0",
        Float.toString(getTaggingStats_after_tag.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_after_tag.getBody().getAll().getTotal());
    assertEquals("11",
        Long.toString(getTaggingStats_after_tag.getBody().getUnique().getTagged()));
    assertEquals("100.0",
        Float.toString(getTaggingStats_after_tag.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_after_tag.getBody().getUnique().getTotal());

    //Verify tagging guide stats
    apiUtil.getTaggingGuideStats(projectID);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide = apiUtil
        .getTaggingGuide(projectID);
    Map<String, String> tagMap = IntegrationUtilTest.testTaggingGuide(getTaggingGuide.getBody());
    assertEquals(addIntentRequest.getIntent(), tagMap.get("intent"));
    assertEquals("11", tagMap.get("count"));
    assertEquals("100.0", tagMap.get("percentage"));

    //Shows all transcripts for dataset and verify tagged transcripts
    SearchRequest searchTran_after_tag = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_after_tag = apiUtil
        .testAllTranscriptions(projectID, searchTran_after_tag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_after_tag.getBody().getTotal());
    Integer expectedAutoTagCount = 1;
    for (int l = 0; l < transcriptionListSize; l++) {
      /*assertEquals(expectedAutoTagCount,
          showAllTranscriptions_after_tag.getBody().getTranscriptionList().get(l)
              .getAutoTagCount()); BUG : NT-2083*/
      assertEquals(addIntentRequest.getIntent(),
          showAllTranscriptions_after_tag.getBody().getTranscriptionList().get(l).getIntent());
      //assertEquals(addIntentRequest.getRutag(), showAllTranscriptions_after_tag.getBody().getTranscriptionList().get(l).getRutag()); BUG: NT-1974
    }

    //Bulk untag dataset
    AddIntentRequest deleteIntentRequest = IntegrationUtilTest
        .bulkUntagUpdate(transcriptionList, currentUserId);
    apiUtil.testUntagProject(projectID, deleteIntentRequest);
    Thread.sleep(1000);

    //Post stats for dataset in tag page
    SearchRequest search_untag = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> taggingStats_after_untag = apiUtil
        .testPostTaggingStats(projectID, search_untag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_after_untag.getBody().getAll().getTotal());

    //Get stats for dataset after bulk untag
    ResponseEntity<StatsResponse> getTaggingStats_untag = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("0", Long.toString(getTaggingStats_untag.getBody().getAll().getTagged()));
    assertEquals("0.0", Float.toString(getTaggingStats_untag.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_untag.getBody().getAll().getTotal());
    assertEquals("0", Long.toString(getTaggingStats_untag.getBody().getUnique().getTagged()));
    assertEquals("0.0", Float.toString(getTaggingStats_untag.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_untag.getBody().getUnique().getTotal());

    //Verify untagged transcripts
    SearchRequest searchTran_untag = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 1, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_untag = apiUtil
        .testAllTranscriptions(projectID, searchTran_untag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_untag.getBody().getTotal());
    assertFalse(showAllTranscriptions_untag.getBody().getTranscriptionList().toString()
        .contains(addIntentRequest.getIntent()));
    for (int u = 0; u < transcriptionListSize; u++) {
      assertNull(showAllTranscriptions_untag.getBody().getTranscriptionList().get(u).getIntent());
    }

    //Verify tagging guide stats
    apiUtil.getTaggingGuideStats(projectID);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_untag = apiUtil
        .getTaggingGuide(projectID);
    Map<String, String> taggingMap = IntegrationUtilTest
        .testTaggingGuide(getTaggingGuide_untag.getBody());
    assertEquals(addIntentRequest.getIntent(), taggingMap.get("intent"));
    assertEquals("0", taggingMap.get("count"));
    assertEquals("0.0", taggingMap.get("percentage"));

    //Add new  tag in tag page
    AddIntentRequest newIntent = IntegrationUtilTest
        .addNewIntent(transcriptionList, currentUserId, "new-intent");
    apiUtil.testTagProject(projectID, newIntent);
    Thread.sleep(1000);

    //Post stats for dataset in tag page
    SearchRequest search_addNewIntent_stats = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> addNewIntent_stats = apiUtil
        .testPostTaggingStats(projectID, search_addNewIntent_stats);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        addNewIntent_stats.getBody().getAll().getTotal());
    /* assertEquals("100.0", addNewIntent_stats.getBody().getAll().getPercent().toString());*/ //NT-2757 to be fixed.

    //Verify new tag in tag page for all transcripts
    SearchRequest searchNewTag = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions_newTag = apiUtil
        .testAllTranscriptions(projectID, searchNewTag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showAllTranscriptions_newTag.getBody().getTotal());
    assertFalse(showAllTranscriptions_newTag.getBody().getTranscriptionList().toString()
        .contains(addIntentRequest.getIntent()));
    for (int n = 0; n < transcriptionListSize; n++) {
      assertEquals(newIntent.getIntent(),
          showAllTranscriptions_newTag.getBody().getTranscriptionList().get(n).getIntent());
    }

    //Verify the new tag in tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_newTag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals(newIntent.getIntent(),
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(0)
            .getIntent());
    assertEquals("11", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(0)
            .getCount()));
    assertEquals(addIntentRequest.getIntent(),
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(1)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(1)
            .getCount()));

    //Add a new tag in tagging guide
    ResponseEntity<TaggingGuideDocument> add_tag_tagging_guide = apiUtil
        .testAddTagInTaggingGuide(projectID);
    TaggingGuideDocument addedDoc = add_tag_tagging_guide.getBody();
    assertNotNull(addedDoc.getId());
    assertEquals(addedDoc.getIntent(), "new-tag");
    assertEquals(addedDoc.getRutag(), "new-rutag");
    String intentId = addedDoc.getId();

    //Apply newly added tag in tagging guide to transcripts
    AddIntentRequest tagUpdateRequest = IntegrationUtilTest
        .addNewIntent(transcriptionList, currentUserId,
            add_tag_tagging_guide.getBody().getIntent());
    apiUtil.testTagUpdate(projectID, tagUpdateRequest);
    Thread.sleep(1000);

    //Verify tag count in tagging guide
    SearchRequest search_taggingGuideTagDset = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> taggingStats_taggingGuideTagDset = apiUtil
        .testPostTaggingStats(projectID, search_taggingGuideTagDset);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_taggingGuideTagDset.getBody().getAll().getTotal());
    //  assertEquals("3", taggingStats_taggingGuideTagDset.getBody().getIntents().toString()); //NT-2757 to be fixed.

    //Get stats for dataset
    ResponseEntity<StatsResponse> getTaggingStats_taggingGuideTagDset = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("11",
        Long.toString(getTaggingStats_taggingGuideTagDset.getBody().getAll().getTagged()));
    assertEquals("100.0",
        Float.toString(getTaggingStats_taggingGuideTagDset.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_taggingGuideTagDset.getBody().getAll().getTotal());
    assertEquals("11",
        Long.toString(getTaggingStats_taggingGuideTagDset.getBody().getUnique().getTagged()));
    assertEquals("100.0",
        Float.toString(getTaggingStats_taggingGuideTagDset.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getTaggingStats_taggingGuideTagDset.getBody().getUnique().getTotal());

    //Shows all transcripts for dataset and verify tagged transcripts
    SearchRequest searchTran_taggingGuideTagDset = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTran_taggingGuideTagDset = apiUtil
        .testAllTranscriptions(projectID, searchTran_taggingGuideTagDset);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showTran_taggingGuideTagDset.getBody().getTotal());
    for (int p = 0; p < transcriptionListSize; p++) {
      assertEquals(tagUpdateRequest.getIntent(),
          showTran_taggingGuideTagDset.getBody().getTranscriptionList().get(p).getIntent());
    }

    //Modify new tag in tagging guide.
    HttpEntity<String> httpEntity = IntegrationUtilTest
        .updateTagInTaggingGuide("/intent", "update-intent");
    ResponseEntity<TaggingGuideDocument> updatedEntity =
        apiUtil.testTagUpdateInTaggingGuide(projectID, httpEntity, intentId);
    assertEquals(HttpStatus.OK, updatedEntity.getStatusCode());
    assertEquals("update-intent", updatedEntity.getBody().getIntent());
    Thread.sleep(1000);

    //POST stats for dataset
    SearchRequest search_updatedtaggingGuideIntent = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> taggingStats_updatedtaggingGuideIntent = apiUtil
        .testPostTaggingStats(projectID, search_updatedtaggingGuideIntent);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        taggingStats_updatedtaggingGuideIntent.getBody().getAll().getTotal());
    //    assertEquals("3", taggingStats_updatedtaggingGuideIntent.getBody().getIntents().toString()); //NT-2757 to be fixed.

    //Get stats for dataset
    ResponseEntity<StatsResponse> getStats_updatedtaggingGuideIntent = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("11",
        Long.toString(getStats_updatedtaggingGuideIntent.getBody().getAll().getTagged()));
    assertEquals("100.0",
        Float.toString(getStats_updatedtaggingGuideIntent.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getStats_updatedtaggingGuideIntent.getBody().getAll().getTotal());
    assertEquals("11",
        Long.toString(getStats_updatedtaggingGuideIntent.getBody().getUnique().getTagged()));
    assertEquals("100.0",
        Float.toString(getStats_updatedtaggingGuideIntent.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getStats_updatedtaggingGuideIntent.getBody().getUnique().getTotal());

    //Verify tag updated in tagging guide is applied to transcripts
    SearchRequest searchTran_updatedtagGuideIntent = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTran_updatedtagGuideIntent = apiUtil
        .testAllTranscriptions(projectID, searchTran_updatedtagGuideIntent);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showTran_updatedtagGuideIntent.getBody().getTotal());
    /* for (int q = 0; q < transcriptionListSize; q++) {
      assertEquals(updatedEntity.getBody().getIntent(),
          showTran_updatedtagGuideIntent.getBody().getTranscriptionList().get(q).getIntent());
    }*/ //NT-2757 to be fixed.

    //Modify existing tag in tag page
    AddIntentRequest modifyTagIntentRequest = IntegrationUtilTest
        .addNewIntent(transcriptionList, currentUserId, "modified-tag");
    apiUtil.testTagUpdate(projectID, modifyTagIntentRequest);
    Thread.sleep(1000);

    //Post stats for dataset in tag page
    SearchRequest search_tag = IntegrationUtilTest
        .searchRequest(Collections.singletonList(Integer.toString(datasetID)));
    ResponseEntity<StatsResponse> tagStats = apiUtil
        .testPostTaggingStats(projectID, search_tag);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        tagStats.getBody().getAll().getTotal());
    //   assertEquals("4", tagStats.getBody().getIntents().toString()); //NT-2757 to be fixed.

    //Get stats for dataset after bulk untag
    ResponseEntity<StatsResponse> getStats_untag = apiUtil
        .getTaggingStats(projectID, datasetID);
    assertEquals("11", Long.toString(getStats_untag.getBody().getAll().getTagged()));
    assertEquals("100.0", Float.toString(getStats_untag.getBody().getAll().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getStats_untag.getBody().getAll().getTotal());
    assertEquals("11", Long.toString(getStats_untag.getBody().getUnique().getTagged()));
    assertEquals("100.0", Float.toString(getStats_untag.getBody().getUnique().getPercent()));
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        getStats_untag.getBody().getUnique().getTotal());

    //Verify modified tag for transcripts
    SearchRequest searchTran_modifiedIntent = IntegrationUtilTest
        .getTranscript(Collections.singletonList(Integer.toString(datasetID)), 2, "", false);
    ResponseEntity<TranscriptionDocumentDetailCollection> showTran_modifiedTag = apiUtil
        .testAllTranscriptions(projectID, searchTran_modifiedIntent);
    assertEquals(Long.valueOf(getTransformStatus.getBody().getRecordsProcessed().longValue()),
        showTran_modifiedTag.getBody().getTotal());
    for (int j = 0; j < transcriptionListSize; j++) {
      assertEquals(modifyTagIntentRequest.getIntent(),
          showTran_modifiedTag.getBody().getTranscriptionList().get(j).getIntent());
    }

    //Verify the modified tag in tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_modifiedTag = apiUtil
        .getTaggingGuide(projectID);
    assertEquals(modifyTagIntentRequest.getIntent(),
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_modifiedTag.getBody()).get(0)
            .getIntent());
    assertEquals("11", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_modifiedTag.getBody()).get(0)
            .getCount()));
  /*  assertEquals(updatedEntity.getBody().getIntent(),
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_modifiedTag.getBody()).get(3)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_modifiedTag.getBody()).get(3)
            .getCount()));*/  //NT-2757 to be fixed.

    //Delete dataset
    apiUtil.testDeleteDataset(clientID, projectID, datasetID);

    //Verify tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_dset_delete = apiUtil
        .getTaggingGuide(projectID);
    Map<String, String> tagGuideMap = IntegrationUtilTest
        .testTaggingGuide(getTaggingGuide_dset_delete.getBody());
    assertEquals("update-intent", tagGuideMap.get("intent"));
    assertEquals("0", tagGuideMap.get("count"));
    assertEquals("0.0", tagGuideMap.get("percentage"));
  }

}