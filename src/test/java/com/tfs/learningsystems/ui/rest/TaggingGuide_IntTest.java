package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.testutil.ApiUtilTest;
import com.tfs.learningsystems.testutil.IntegrationUtilTest;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideImportStats;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
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
public class TaggingGuide_IntTest {

  @Autowired
  ApiUtilTest apiUtil;

  @Before
  public void setUp() throws IOException {
    apiUtil.setTestRestTemplateHeaders();
  }


  @Test
  public void verifyTaggingGuideImport() throws IOException, InterruptedException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    //Get clientId of ModelingWorkbench client
    ResponseEntity<ClientBO[]> clients = apiUtil.testGetClients();
    Integer clientID = IntegrationUtilTest.getClientId(clients.getBody());

    String cId = IntegrationUtilTest.getCId(clients.getBody());

    // POST project
    ResponseEntity<ProjectBO> createProjectEntity = apiUtil.testCreateProject(cId, clientID, name);
    Integer projectID = createProjectEntity.getBody().getId();

    //Import tagging guide
    String token = apiUtil.testImportTaggingGuide(clientID, projectID); //BUG NT-2338

    //Tagging guide column mapping
    ResponseEntity<TaggingGuideColumnMappedResponse> importTaggingGuideColMapping = apiUtil
        .testImportTaggingGuideColMapping(clientID, projectID, token);  //BUG NT-2339
    assertEquals("4", Long.toString(importTaggingGuideColMapping.getBody().getValidTagCount()));

    //Get tagging guide columns
    ResponseEntity<TaggingGuideColumnList> getTaggingGuideColumns = apiUtil
        .testGetTaggingGuideColumns();
    assertEquals("intent", getTaggingGuideColumns.getBody().get(0).getName());
    assertEquals(true, getTaggingGuideColumns.getBody().get(0).getRequired());
    assertEquals("rutag", getTaggingGuideColumns.getBody().get(1).getName());
    assertEquals(false, getTaggingGuideColumns.getBody().get(1).getRequired());
    assertEquals("description", getTaggingGuideColumns.getBody().get(2).getName());
    assertEquals(false, getTaggingGuideColumns.getBody().get(2).getRequired());
    assertEquals("comments", getTaggingGuideColumns.getBody().get(3).getName());
    assertEquals(false, getTaggingGuideColumns.getBody().get(3).getRequired());
    assertEquals("examples", getTaggingGuideColumns.getBody().get(4).getName());
    assertEquals(false, getTaggingGuideColumns.getBody().get(4).getRequired());
    assertEquals("keywords", getTaggingGuideColumns.getBody().get(5).getName());
    assertEquals(false, getTaggingGuideColumns.getBody().get(5).getRequired());

    //apiUtil.getTaggingGuideImportColMapping(projectID); NT-2341
    ResponseEntity<TaggingGuideImportStatBO> commit = apiUtil
        .testImportTaggingGuideCommit(clientID, projectID, token);
    assertEquals("4", Long.toString(commit.getBody().getValidTagCount()));
    Thread.sleep(20000);

    //Get tagging guide import stats
    ResponseEntity<TaggingGuideImportStats> getTaggingGuideStats = apiUtil
        .getTaggingGuideStats(projectID);
    assertEquals("4", Long.toString(getTaggingGuideStats.getBody().getValidTagCount()));

    //Get tagging guide
    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide_newTag = apiUtil
        .getTaggingGuide(projectID);

    Thread.sleep(20000);

    assertEquals("agent-q",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(0)
            .getIntent());
    assertEquals("agent-quer",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(1)
            .getIntent());
    assertEquals("agent-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(2)
            .getIntent());
    assertEquals("reservation-query",
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(3)
            .getIntent());
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(0)
            .getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(1)
            .getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(2)
            .getCount()));
    assertEquals("0", Long.toString(
        IntegrationUtilTest.taggingGuideDocument(getTaggingGuide_newTag.getBody()).get(3)
            .getCount()));

  }
}
