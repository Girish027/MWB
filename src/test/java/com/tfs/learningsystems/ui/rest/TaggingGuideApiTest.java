/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.model.ProjectDetail;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideColumnsManager;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumn;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideImportStats;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideStagedImportResponse;
import com.tfs.learningsystems.util.CommonLib;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TaggingGuideApiTest {

  private static String currentUserId = "UnitTest@247.ai";
  @LocalServerPort
  int localTestPort;
  private String basePath;
  private AppConfig appConfig;
  private ClientBO testClient;
  private ProjectBO testProjectDetail;
  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  @Qualifier("elasticTestUtils")
  private ElasticSearchTestUtils esTestUtils;

  @Autowired
  @Qualifier("taggingGuideColumnsManager")
  private TaggingGuideColumnsManager taggingGuideColumnsManager;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.appConfig = appConfig;
    this.basePath = appConfig.getRestUrlPrefix() + "v1/taggingguide";
  }

  private List<TaggingGuideColumn> getColumnListToSeed() throws IOException {
    Resource resource = new ClassPathResource("tagging_guide_columns.csv");
    BufferedReader reader;

    reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
    List<String[]> lineTokensList = reader
        .lines()
        .map(line -> line.split(","))
        .filter(lineTokens -> lineTokens.length == 3)
        .collect(Collectors.toList());
    List<TaggingGuideColumn> columnList = new ArrayList<>(lineTokensList.size());
    for (String[] lineTokens : lineTokensList) {
      TaggingGuideColumn column = new TaggingGuideColumn();
      column.setName(lineTokens[0]);
      column.setDisplayName(lineTokens[2]);
      column.setRequired(Boolean.valueOf(lineTokens[1]));
      columnList.add(column);
    }
    return columnList;
  }

  @Before
  public void setUp() throws ScriptException, SQLException, IOException {
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
    this.esTestUtils.refreshAllIndices();
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    this.testClient = ModelUtils.getTestClientObject(name);
    this.testClient.create();

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);

    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    ProjectDetail projectDetail = new ProjectDetail();
    BeanUtils.copyProperties(project,projectDetail);
    projectDetail.setVertical(Vertical.valueOf(project.getVertical()));
    this.testProjectDetail = projectManager.addProject(project.getClientId().toString(), projectDetail);

    List<TaggingGuideColumn> columnList = this.getColumnListToSeed();
    this.taggingGuideColumnsManager.seedColumns(columnList);
  }

  @After
  public void esTearDown() {
  }

  @Test
  public void testImportGuide() throws Exception {
    String saveGuideUrl = String.format("http://localhost:%d%s/%s/%s/import", localTestPort,
        basePath, this.testClient.getId(), this.testProjectDetail.getId());

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(saveGuideUrl);
    try {
      File file = new ClassPathResource("test-tagging-guide.csv").getFile();

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());

      org.apache.http.HttpEntity entity = builder.build();
      post.setEntity(entity);
      post.addHeader(CommonLib.Auth.getAUTHHEADERNAME(), CommonLib.Auth.getAccessToken());
      HttpResponse response = client.execute(post);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK,
          HttpStatus.valueOf(response.getStatusLine().getStatusCode()));

      ObjectMapper objectMapper = new ObjectMapper();

      TaggingGuideStagedImportResponse stagedImportResponse = objectMapper.readValue(
          response.getEntity().getContent(), TaggingGuideStagedImportResponse.class);

      String columnMappingUrl = String
          .format("%s/%s/%s/import/%s/column/mapping?ignoreFirstRow=true",
              basePath, this.testClient.getId(), this.testProjectDetail.getId(),
              stagedImportResponse.getToken());

      TaggingGuideColumnMappingSelectionList columnMappings1 = new TaggingGuideColumnMappingSelectionList();
      TaggingGuideColumnMappingSelection column11 = new TaggingGuideColumnMappingSelection();
      column11.setColumnName("intent");
      column11.setColumnIndex("0");
      column11.setDisplayName("Intent");
      columnMappings1.add(column11);

      ResponseEntity<TaggingGuideColumnMappedResponse> columnMappingEntity =
          this.restTemplate.postForEntity(columnMappingUrl, columnMappings1,
              TaggingGuideColumnMappedResponse.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, columnMappingEntity.getStatusCode());

      TaggingGuideColumnMappedResponse columnMappedResponse = columnMappingEntity.getBody();

      assertEquals(columnMappedResponse.getValidTagCount(), 10);

      String commitUrl = String.format("%s/%s/%s/import/%s/commit",
          basePath, this.testClient.getId(), this.testProjectDetail.getId(),
          stagedImportResponse.getToken());

      ResponseEntity<TaggingGuideImportStats> commitEntity =
          this.restTemplate.postForEntity(commitUrl, null, TaggingGuideImportStats.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, commitEntity.getStatusCode());

      TaggingGuideImportStats importStats = commitEntity.getBody();

      assertEquals(10, importStats.getValidTagCount());

      this.esTestUtils.refreshClassificationIndex();

      String intentSearchUrl = String.format("%sv1/search/%s/intents?q=%s",
          appConfig.getRestUrlPrefix(), this.testProjectDetail.getId(),
          "acc");
      ResponseEntity<String[]> searchEntity =
          this.restTemplate.getForEntity(intentSearchUrl, String[].class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
      assertArrayEquals(searchEntity.getBody(), new String[]{"account-change",
          "account-vague", "account_login-get_help", "accounting-query"});
    } catch (IOException e) {
      fail();
    } catch (Exception ex) {
      fail();
    }

  }


  @Test
  public void testDeleteIntentsFromGuide() throws Exception {

    this.esTestUtils.refreshAllIndices();

    String saveGuideUrl = String.format("http://localhost:%d%s/%s/%s/import/", localTestPort,
        basePath, this.testClient.getId(), this.testProjectDetail.getId());

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(saveGuideUrl);
    try {
      File file = new ClassPathResource("test-tagging-guide.csv").getFile();

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());

      org.apache.http.HttpEntity entity = builder.build();
      post.setEntity(entity);
      post.addHeader(CommonLib.Auth.getAUTHHEADERNAME(), CommonLib.Auth.getAccessToken());
      HttpResponse response = client.execute(post);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK,
          HttpStatus.valueOf(response.getStatusLine().getStatusCode()));

      ObjectMapper objectMapper = new ObjectMapper();
      TaggingGuideStagedImportResponse stagedImportResponse = objectMapper.readValue(
          response.getEntity().getContent(), TaggingGuideStagedImportResponse.class);

      String columnMappingUrl = String
          .format("%s/%s/%s/import/%s/column/mapping?ignoreFirstRow=true",
              basePath, this.testClient.getId(), this.testProjectDetail.getId(),
              stagedImportResponse.getToken());

      TaggingGuideColumnMappingSelectionList columnMappings1 = new TaggingGuideColumnMappingSelectionList();
      TaggingGuideColumnMappingSelection column11 = new TaggingGuideColumnMappingSelection();
      column11.setColumnName("intent");
      column11.setColumnIndex("0");
      column11.setDisplayName("Intent");
      columnMappings1.add(column11);

      ResponseEntity<TaggingGuideColumnMappedResponse> columnMappingEntity =
          this.restTemplate.postForEntity(columnMappingUrl, columnMappings1,
              TaggingGuideColumnMappedResponse.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, columnMappingEntity.getStatusCode());

      TaggingGuideColumnMappedResponse columnMappedResponse = columnMappingEntity.getBody();

      assertEquals(columnMappedResponse.getValidTagCount(), 10);

      String commitUrl = String.format("%s/%s/%s/import/%s/commit",
          basePath, this.testClient.getId(), this.testProjectDetail.getId(),
          stagedImportResponse.getToken());

      ResponseEntity<TaggingGuideImportStats> commitEntity =
          this.restTemplate.postForEntity(commitUrl, null, TaggingGuideImportStats.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, commitEntity.getStatusCode());

      TaggingGuideImportStats importStats = commitEntity.getBody();

      assertEquals(10, importStats.getValidTagCount());

      this.esTestUtils.refreshClassificationIndex();

      String intentSearchUrl = String.format("%sv1/search/%s/intents?q=%s",
          appConfig.getRestUrlPrefix(), this.testProjectDetail.getId(),
          "acc");

      ResponseEntity<String[]> searchEntity =
          this.restTemplate.getForEntity(intentSearchUrl, String[].class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());

//      assertArrayEquals(searchEntity.getBody(), new String[]{"account-change",
//          "account-vague", "account_login-get_help", "accounting-query"});

      String deleteIntentsUrl = String.format("%sv1/content/%s/intentguide",
          appConfig.getRestUrlPrefix(), this.testProjectDetail.getId());
      ResponseEntity<Void> deleteResponse =
          this.restTemplate.exchange(deleteIntentsUrl, HttpMethod.DELETE, null, Void.class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

      this.esTestUtils.refreshClassificationIndex();

      ResponseEntity<String[]> searchEntity2 =
          this.restTemplate.getForEntity(intentSearchUrl, String[].class);

      Thread.sleep(appConfig.getTestCaseTimeout());

      assertEquals(HttpStatus.OK, searchEntity.getStatusCode());

      assertArrayEquals(new String[]{}, searchEntity2.getBody());

    } catch (IOException e) {
      fail();
    } catch (Exception ex) {
      fail();
    }
  }
}
