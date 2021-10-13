package com.tfs.learningsystems.testutil;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.BatchTestResultsResponse;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.model.IntentResponse;
import com.tfs.learningsystems.ui.model.ModelConfigCollection;
import com.tfs.learningsystems.ui.model.ModelConfigDetail;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.ui.model.ReportField;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideImportStats;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideStagedImportResponse;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class ApiUtilTest {

  final String setUriPath = "https://tagging.247-inc.com:8443/nltools/private/v1/files/";
  public String currentUserId = "IntegrationTest@247.ai";
  @Autowired
  AppConfig appConfig;
  long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
  private String basePath;
  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  public String createCompleteURL(String uri) {
    String env = appConfig.getIntEnv();
    String endpoint = setEnvironment(env);
    String resultUri = endpoint + uri;
    return resultUri;
  }

  public String setEnvironment(String env) {
    String endpoint = "";
    switch (env.toLowerCase()) {
      case "qa":
        endpoint = "https://qa-ai-tools.247-inc.net";
        break;
      case "dev":
        endpoint = "https://dev-ai-tools.247-inc.net";
        break;
      case "psr":
        endpoint = "https://psr-ai-tools.247-inc.net";
        break;
      case "stable":
        endpoint = "https://stable-ai-tools.247-inc.net";
        break;
      default:
        endpoint = "";
        break;
    }
    return endpoint;
  }

  public String manualToken() {
    String token = appConfig.getIntAccessToken();
    System.out.println("Token is..." + token);
    return token;
  }

  public void setTestRestTemplateHeaders() {
    Header header = new BasicHeader("Authorization", manualToken());
    List<Header> headers = new ArrayList<Header>();
    headers.add(header);
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
    restTemplate.getRestTemplate()
        .setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
  }

  public Integer testCreateClient(String name) {
    ClientBO client = IntegrationUtilTest.getTestClientObject(name);
    client.create();
    Integer clientID = client.getId();
    assertNotNull(clientID);
    return clientID;
  }

  public ResponseEntity<ClientBO> createClient(String name) {
    ClientBO client1 = IntegrationUtilTest.getTestClientObject(name);

    StringBuilder completeURL = new StringBuilder(basePath).append("clients");

    System.out.println("API in testCreateClient is -->" + completeURL.toString());

    ResponseEntity<ClientBO> createClient = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), client1, ClientBO.class);
    assertEquals(HttpStatus.CREATED, createClient.getStatusCode());
    assertTrue(timeStamp < createClient.getBody().getCreatedAt());
    return createClient;
  }


  public ResponseEntity<ProjectBO> testCreateProject(String cId, Integer clientID, String name) {
    ProjectBO project = IntegrationUtilTest
        .getTestProjectObject(cId, clientID, currentUserId, name);

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientID)
        .append("/projects");

    System.out.println("API in testCreateProject is -->" + completeURL.toString());

    ResponseEntity<ProjectBO> createProjectEntity = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), project, ProjectBO.class);
    assertEquals(HttpStatus.CREATED, createProjectEntity.getStatusCode());
    assertTrue(timeStamp < createProjectEntity.getBody().getCreatedAt());
    return createProjectEntity;
  }

  public void testGetClient(Integer clientID) throws InterruptedException {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientID);

    System.out.println("API in testGetClient is -->" + completeURL.toString());

    ResponseEntity<ClientBO> getClientEntity = this.restTemplate

        .getForEntity(createCompleteURL(completeURL.toString()), ClientBO.class);
    ClientBO getClient = getClientEntity.getBody();
    Thread.sleep(1000);
    assertEquals(HttpStatus.OK, getClientEntity.getStatusCode());
  }

  public ResponseEntity<ClientBO[]> testGetClients() throws InterruptedException, IOException {
    StringBuilder completeURL = new StringBuilder(basePath)
        .append("clients?appid=referencebot&clientid=ModelingWorkbench");

    System.out.println("API in testGetClients is -->" + completeURL.toString());

    ResponseEntity<ClientBO[]> getClientsEntity = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), ClientBO[].class);
    Thread.sleep(1000);
    assertEquals(HttpStatus.OK, getClientsEntity.getStatusCode());
    return getClientsEntity;
  }


  public void testDeleteProject(Integer clientId, Integer projectId)
          throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectId);

    System.out.println("completeURL in testDeleteProject is -->" + completeURL.toString());

    ResponseEntity<Void> deleteProject= this.restTemplate
            .exchange(createCompleteURL(completeURL.toString()),
                    HttpMethod.DELETE, null, Void.class);

    this.restTemplate.delete(createCompleteURL(completeURL.toString()));

    Thread.sleep(12000);

    ResponseEntity<Error> entity = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), Error.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  public void testGetProject(Integer clientId, Integer projectID,
      ResponseEntity<ProjectBO> createdProjectEntity) throws InterruptedException {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID);

    System.out.println("API in testGetProject is -->" + completeURL.toString());

    ResponseEntity<ProjectBO> getProjectEntity = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), ProjectBO.class);

    ProjectBO getProject = getProjectEntity.getBody();
    Thread.sleep(1000);
    assertEquals(HttpStatus.OK, getProjectEntity.getStatusCode());
    assertEquals(createdProjectEntity.getBody().getName(), getProject.getName());
    assertEquals(createdProjectEntity.getBody().getClientId(), getProject.getClientId());
    assertEquals(createdProjectEntity.getBody().getOwnerId(), getProject.getOwnerId());
    assertEquals(createdProjectEntity.getBody().getVertical(), getProject.getVertical());
    assertEquals(createdProjectEntity.getBody().getDescription(), getProject.getDescription());
    assertEquals(createdProjectEntity.getBody().getLocale(), getProject.getLocale());
    assertEquals(createdProjectEntity.getBody().getState(), getProject.getState());
    assertEquals(createdProjectEntity.getBody().getModifiedAt(), getProject.getModifiedAt());
  }

  public String testFileImport() {
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = IntegrationUtilTest
        .fileImport(currentUserId);
    StringBuilder completeURL = new StringBuilder(basePath).append("files/import");

    System.out.println("API in testFileImport is -->" + completeURL.toString());

    ResponseEntity<FileStagedImportResponse> fileImportEntity = this.restTemplate
        .exchange(createCompleteURL(completeURL.toString()), HttpMethod.POST, requestEntity,
            FileStagedImportResponse.class);
    assertTrue(fileImportEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(fileImportEntity.getBody());
    String responseToken = fileImportEntity.getBody().getToken();
    return responseToken;
  }

  public String testFileImportLargeFile() {
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = IntegrationUtilTest
        .fileImportLargeFile(currentUserId);
    StringBuilder completeURL = new StringBuilder(basePath).append("files/import");

    System.out.println("API in testFileImportLargeFile is -->" + completeURL.toString());

    ResponseEntity<FileStagedImportResponse> fileImportEntity = this.restTemplate
        .exchange(createCompleteURL(completeURL.toString()), HttpMethod.POST, requestEntity,
            FileStagedImportResponse.class);
    assertTrue(fileImportEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(fileImportEntity.getBody());
    String responseToken = fileImportEntity.getBody().getToken();
    return responseToken;
  }


  public String testNoTagFileImport() {
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = IntegrationUtilTest
        .fileImportNoTag(currentUserId);
    StringBuilder completeURL = new StringBuilder(basePath).append("files/import");

    System.out.println("API in testFileImport is -->" + completeURL.toString());

    ResponseEntity<FileStagedImportResponse> fileImportEntity = this.restTemplate

        .exchange(createCompleteURL(completeURL.toString()), HttpMethod.POST, requestEntity,
            FileStagedImportResponse.class);

    assertTrue(fileImportEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(fileImportEntity.getBody());
    String responseToken = fileImportEntity.getBody().getToken();
    return responseToken;
  }

  public String testColMapping(String responseToken, String json) throws IOException {
    StringBuilder completeURL = new StringBuilder(basePath).append("files/import/")
        .append(responseToken)
        .append("/column/mapping?ignoreFirstRow=true");

    System.out.println("API in testColMapping is -->" + completeURL.toString());

    ResponseEntity<FileEntryDetail> columnEntity = this.restTemplate.postForEntity(

        createCompleteURL(completeURL.toString()),
        IntegrationUtilTest.fileColMapping(json), FileEntryDetail.class);
    assertTrue(columnEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(columnEntity.getBody());
    String uri = setUriPath + columnEntity.getBody().getFileId();
    return uri;
  }

  public ResponseEntity<DatasetBO> testCreateDataset(AddDatasetRequest addDset) {
    StringBuilder completeURL = new StringBuilder(basePath).append("datasets");

    System.out.println("API in testCreateDataset is -->" + completeURL.toString());

    ResponseEntity<DatasetBO> dsetEntity = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()),
            addDset, DatasetBO.class);

    assertEquals(HttpStatus.CREATED, dsetEntity.getStatusCode());
    assertTrue(timeStamp < dsetEntity.getBody().getCreatedAt());
    return dsetEntity;
  }

  public void testGetDataset(Integer clientId, Integer projectID, Integer datasetID, ResponseEntity<DatasetBO> dataset) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId).
            append("/projects/").append(projectID).append("/datasets/").append(datasetID);

    System.out.println("API in testGetDataset is -->" + completeURL.toString());

    ResponseEntity<DatasetBO> getDatasetEntity = this.restTemplate

        .getForEntity(createCompleteURL(completeURL.toString()), DatasetBO.class);

    DatasetBO getDataset = getDatasetEntity.getBody();
    assertEquals(HttpStatus.OK, getDatasetEntity.getStatusCode());
    assertEquals(dataset.getBody().getClientId(), getDataset.getClientId());
    assertEquals(dataset.getBody().getName(), getDataset.getName());
    assertTrue(timeStamp < getDataset.getReceivedAt());
    assertEquals(dataset.getBody().getDataType(), getDataset.getDataType());
    assertEquals(dataset.getBody().getDescription(), getDataset.getDescription());
    assertEquals(dataset.getBody().getLocale(), getDataset.getLocale());
    assertEquals(dataset.getBody().getUri(), getDataset.getUri());
    assertEquals(dataset.getBody().getModifiedAt(), getDataset.getModifiedAt());
  }

  public void linkDsetsToProj(Integer clientId, Integer projectID, Integer datasetID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID)
        .append("/datasets/").append(datasetID);

    System.out.println("API in linkDsetsToProj is -->" + completeURL.toString());

    ResponseEntity<Void> linkProjDset1 = this.restTemplate

        .exchange(createCompleteURL(completeURL.toString()), HttpMethod.PUT,
            null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, linkProjDset1.getStatusCode());
  }

  public ResponseEntity<DatasetsDetail> testDsetsOfProj(Integer clientId, Integer projectID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID)
        .append("/datasets");
    System.out.println("API in testDsetsOfProj is -->" + completeURL.toString());
    ResponseEntity<DatasetsDetail> listDsetByProj = this.restTemplate

        .getForEntity(createCompleteURL(completeURL.toString()), DatasetsDetail.class);

    assertEquals(HttpStatus.OK, listDsetByProj.getStatusCode());
    return listDsetByProj;
  }

  public void testDatasetTransform(Integer clientId, Integer projectID, Integer datasetID,
      String suggestedCategory)
      throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID)
        .append("/datasets/").append(datasetID)
        .append("/transform?useModelForSuggestedCategory=")
        .append(suggestedCategory);

    System.out.println("API in testDatasetTransform is -->" + completeURL.toString());

    ResponseEntity<TaskEventDetail> transfromDset = this.restTemplate

        .exchange(createCompleteURL(completeURL.toString()), HttpMethod.PUT, null,
            TaskEventDetail.class);

    assertEquals(HttpStatus.ACCEPTED, transfromDset.getStatusCode());

    assertTrue(timeStamp < transfromDset.getBody().getCreatedAt());
    assertTrue(transfromDset.getBody().getRecordsImported() > transfromDset.getBody()
        .getRecordsProcessed());

    Map<Integer, List<Integer>> projects = new HashMap<>();
    List<Integer> datasetIds = new ArrayList<>();
    datasetIds.add(datasetID);
    projects.put(projectID, datasetIds);
    int i = 0;
    int transformTimeout = appConfig.getIntPollingCount();
    String transformResult = null;

    long timeStampTransformationStart = Calendar.getInstance().getTimeInMillis() - 1;

    StringBuilder statusURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/transform/status");
    System.out.println("API to check status postTransformStatus is -->" + statusURL.toString());

    while (i < transformTimeout) {

      ResponseEntity<String> postTransformStatus = this.restTemplate

          .postForEntity(createCompleteURL(statusURL.toString()), projects, String.class);

      assertEquals(HttpStatus.OK, postTransformStatus.getStatusCode());
      assertFalse(postTransformStatus.getBody().isEmpty());
      transformResult = postTransformStatus.getBody();
      System.out.print("Tranformation result..." + transformResult + "\n");
      if (transformResult.contains("INDEX") && transformResult.contains("COMPLETED")) {
        break;
      } else {
        Thread.sleep(5000);
        i++;
      }
    }

    if (transformResult != null) {
      assertTrue(transformResult.contains("COMPLETED"));
    }

    long timeStampTransformationEnd = Calendar.getInstance().getTimeInMillis() - 1;
    long transformationTime_D1 = timeStampTransformationEnd - timeStampTransformationStart;
    System.out.print("Time taken for dataset transformation....." + transformationTime_D1 + "\n");
  }

  public ResponseEntity<TaskEventBO> getDsetTransformationStatus(Integer clientId,
      Integer projectID,
      Integer datasetID)
      throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID)
        .append("/datasets/").append(datasetID)
        .append("/transform/status");

    System.out.println("API in getDsetTransformationStatus is -->" + completeURL.toString());

    ResponseEntity<TaskEventBO> getTransformStatus = this.restTemplate.getForEntity(
        createCompleteURL(completeURL.toString()),
        TaskEventBO.class);

    assertEquals(HttpStatus.OK, getTransformStatus.getStatusCode());
    assertEquals("COMPLETED", getTransformStatus.getBody().getStatus());
    assertEquals("INDEX", getTransformStatus.getBody().getTask());
    assertEquals(getTransformStatus.getBody().getRecordsImported(),
        getTransformStatus.getBody().getRecordsProcessed());
    return getTransformStatus;
  }

  public void testExportDataset(Integer clientID, Integer projectID, Integer datasetID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientID).
        append("/projects/").append(projectID)
        .append("/datasets/").append(datasetID)
        .append("/export");

    System.out.println("API in testExportDataset is -->" + completeURL.toString());

    ResponseEntity<String> exportResponse = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()),
            String.class);
    assertEquals(HttpStatus.OK, exportResponse.getStatusCode());
    assertThat(exportResponse.getHeaders().getContentLength(), greaterThan(0l));
  }

  public ResponseEntity<ModelConfigBO> testPostConfig(Integer projectID, String file) {
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = IntegrationUtilTest
        .configImport("m-cfg", "Test model", projectID, file);
    ResponseEntity<ModelConfigBO> postModelConfig = this.restTemplate

        .exchange(createCompleteURL(basePath + "configs"), HttpMethod.POST, requestEntity,
            ModelConfigBO.class);
    assertEquals(HttpStatus.CREATED, postModelConfig.getStatusCode());
    assertNotNull(postModelConfig.getBody());
    return postModelConfig;
  }

  public ResponseEntity<ModelConfigBO> testGetConfig(String configID) {
    ResponseEntity<ModelConfigBO> getConfig = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "configs/" + configID), ModelConfigBO.class);
    assertEquals(HttpStatus.OK, getConfig.getStatusCode());
    return getConfig;
  }

  public ResponseEntity<ModelConfigCollection> testProjectConfig(Integer clientId,
      Integer projectID) {

    String completeUrl = basePath + "clients/" + clientId + "/projects/" + projectID + "/configs";

    ResponseEntity<ModelConfigCollection> getProjectConfig = this.restTemplate

        .getForEntity(createCompleteURL(completeUrl), ModelConfigCollection.class);

    assertEquals(HttpStatus.OK, getProjectConfig.getStatusCode());

    return getProjectConfig;
  }

  /*public ResponseEntity<ModelConfigDetail> testGetConfigData(String clientId ,String configId) {
    ResponseEntity<ModelConfigDetail> getDefaultConfigEntity = this.restTemplate
        .getForEntity(basePath + "clients/" + clientId +"/configs/" + configId + "/data", ModelConfigDetail.class);
    System.out.println(getDefaultConfigEntity.getStatusCode().is2xxSuccessful());
    //assertTrue(getDefaultConfigEntity.getStatusCode().is2xxSuccessful());
    System.out.println(getDefaultConfigEntity.getBody());
    assertNotNull(getDefaultConfigEntity.getBody());
    return getDefaultConfigEntity;
  }*/

  public ResponseEntity<ModelConfigDetail> testGetConfigData(String clientId, String configId) {
    ResponseEntity<ModelConfigDetail> getDefaultConfigEntity = this.restTemplate
        .getForEntity(
            createCompleteURL(basePath + "clients/" + clientId + "/configs/" + configId + "/data"),
            ModelConfigDetail.class);
    assertTrue(getDefaultConfigEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(getDefaultConfigEntity.getBody());
    return getDefaultConfigEntity;
  }

  public ResponseEntity<ModelBO> testCreateModel(String cid, Integer clientId, Integer projectID,
      String configID,
      Integer datasetID, String modelType) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models");

    System.out.println("API in testCreateModel is -->" + completeURL.toString());

    ModelBO model = IntegrationUtilTest
        .getModelObject(cid, projectID, configID, datasetID, currentUserId, modelType);
    ResponseEntity<ModelBO> createModelEntity = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), model, ModelBO.class);
    ModelBO model1 = createModelEntity.getBody();
    assertEquals(HttpStatus.CREATED, createModelEntity.getStatusCode());
    assertTrue(timeStamp < model1.getCreatedAt());
    return createModelEntity;
  }

  public ResponseEntity<ModelBO> testPostModelWithLastUsedConfig(String cid, Integer clientId,
      Integer projectID,
      Integer datasetID, String modelType) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models?trainNow=true");

    System.out.println("API in testPostModelWithLastUsedConfig is -->" + completeURL.toString());

    ModelBO model = IntegrationUtilTest
        .getModelObject(cid, projectID, null, datasetID, currentUserId, modelType);
    ResponseEntity<ModelBO> createModelEntity = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), model, ModelBO.class);
    ModelBO model1 = createModelEntity.getBody();
    assertEquals(HttpStatus.CREATED, createModelEntity.getStatusCode());
    assertTrue(timeStamp < model1.getCreatedAt());
    return createModelEntity;
  }

  public ResponseEntity<ModelConfigBO> testuploadConfig(Integer clientId, Integer projectID)
      throws IOException {
    try {
      StringBuilder completeURL = new StringBuilder(basePath).append("configs/");
      ModelConfigBO modelConfigBO = new ModelConfigBO();
      modelConfigBO.setProjectId(projectID);
      modelConfigBO.setName("test" + String.valueOf(System.currentTimeMillis()));
      String configFileContent = new String(
          Files.readAllBytes(
              Paths.get(new ClassPathResource("speechConfig.json").getFile().getAbsolutePath())),
          "UTF-8");

      modelConfigBO.setConfigFile(configFileContent);
      ResponseEntity<ModelConfigBO> postconfig = this.restTemplate
          .postForEntity(completeURL.toString(), modelConfigBO, ModelConfigBO.class);
      return postconfig;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }


  public ResponseEntity<TFSModelList> testGetModel(Integer clientId, Integer projectID) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models");

    System.out.println("API in testGetModel is -->" + completeURL.toString());

    ResponseEntity<TFSModelList> getModel = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), TFSModelList.class);
    assertEquals(HttpStatus.OK, getModel.getStatusCode());

    System.out.println("model uuid is -->" + getModel.getBody() + "\n\n");

    return getModel;
  }

  public void getModel(Integer clientId, Integer modelID, ResponseEntity<ModelBO> testCreateModel) {
    ResponseEntity<ModelBO> getModel = this.restTemplate

        .getForEntity(createCompleteURL(basePath + "clients/" + clientId + "/models/" + modelID),
            ModelBO.class);
    assertEquals(HttpStatus.OK, getModel.getStatusCode());
  }


  public void testGetDeleteModel(Integer clientId, Integer projectID, Integer modelDBId) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models");

    ResponseEntity<TFSModelList> models = this.restTemplate

        .getForEntity(createCompleteURL(completeURL.toString()), TFSModelList.class);

    assertEquals(HttpStatus.OK, models.getStatusCode());

    boolean ifModelAvailable = false;

    ArrayList<TFSModel> tfsModels = new ArrayList<>();

    tfsModels = models.getBody();

    for (TFSModel model : tfsModels) {
      if (model.getId().equals(modelDBId)) {
        ifModelAvailable = true;
        break;
      }

    }

    assertEquals(false, ifModelAvailable);


  }

  public void testModelBuild(Integer clientId, Integer modelDBId) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/models/").append(modelDBId).append("/build");

    System.out.println("API in testModelBuild is -->" + completeURL.toString());

    ResponseEntity<Void> buildEntity = this.restTemplate

        .postForEntity(createCompleteURL(completeURL.toString()), null, Void.class);
    assertEquals(HttpStatus.ACCEPTED, buildEntity.getStatusCode());
  }

  public void testDeleteDataset(Integer clientId, Integer projectId, Integer datasetId)
          throws InterruptedException {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
            .append("/projects/").append(projectId)
            .append("/datasets/").append(datasetId);

    System.out.println("API in testDeleteDataset is -->" + completeURL.toString());

    ResponseEntity<Void> deleteDataset= this.restTemplate
            .exchange(createCompleteURL(completeURL.toString()),
                    HttpMethod.DELETE, null, Void.class);

    Thread.sleep(6000);

    this.restTemplate.delete(createCompleteURL(completeURL.toString()));

    Thread.sleep(6000);

    ResponseEntity<Error> entity = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), Error.class);

    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  public void getModelState(Integer clientId, Integer projectId, Integer modelDBId) throws InterruptedException {
    int j = 0;
    int modelBuildTimeout = appConfig.getIntPollingCount();
    long timeStampModelBuildStart = Calendar.getInstance().getTimeInMillis() - 1;
    String modelBuildResult = null;
    while (j < modelBuildTimeout) {
      StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId).
              append("/projects/").append(projectId).append("/models/").append(modelDBId).append("/status");

      System.out.println("API in getModelState is -->" + completeURL.toString());

      ResponseEntity<String> getModelBuildState = this.restTemplate
          .getForEntity(createCompleteURL(completeURL.toString()), String.class);
      assertEquals(HttpStatus.OK, getModelBuildState.getStatusCode());
      modelBuildResult = getModelBuildState.getBody();

      System.out.print("Model state after model building..." + getModelBuildState.getBody() + "\n");

      if (modelBuildResult != null && modelBuildResult.contains("COMPLETED")) {
        break;
      } else {
        Thread.sleep(5000);
        j++;
      }
    }

    if (modelBuildResult != null) {
      assertTrue(modelBuildResult.contains("COMPLETED"));
    }

    long timeStampModelBuildEnd = Calendar.getInstance().getTimeInMillis() - 1;
    long modleBuildTime = timeStampModelBuildEnd - timeStampModelBuildStart;
    System.out.print("Time taken for model building....." + modleBuildTime + "\n");
  }

  public void getModelTrainingData(Integer clientId, Integer projectId, Integer modelDBId)
      throws InterruptedException {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectId).append("/models/").append(modelDBId).append("/trainingData");

    System.out.println("API in getModelTrainingData is -->" + completeURL.toString());

    ResponseEntity<String> getModelBuildState = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()), String.class);
    assertEquals(HttpStatus.OK, getModelBuildState.getStatusCode());
    assertNotNull(getModelBuildState.getBody());

  }

  public ResponseEntity<EvaluationResponse> transcriptionTest(Integer clientId, Integer projectID,
      String modelUUID, String modelType) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/").append(modelUUID).append
            ("/eval_transcriptions?testModelType=" + modelType);

    System.out.println("API in utteranceTest is -->" + completeURL.toString());

    ResponseEntity<EvaluationResponse> utteranceTestEntity = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()),
            IntegrationUtilTest.addUtterance(), EvaluationResponse.class);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(HttpStatus.OK, utteranceTestEntity.getStatusCode());
    assertFalse(utteranceTestEntity.getBody().getEvaluations().get(0).getIntents().isEmpty());
    return utteranceTestEntity;
  }

  public ResponseEntity<EvaluationResponse> testSpeechUtterance(Integer clientId, Integer projectID,
      String modelUUID, String fileType, LinkedMultiValueMap<String, Object> map) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/").append(modelUUID).append
            ("/eval_utterance?fileType=" + fileType);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    ResponseEntity<EvaluationResponse> utteranceTestEntity = restTemplate.exchange(
        completeURL.toString(),
        HttpMethod.POST,
        requestEntity,
        EvaluationResponse.class);

    return utteranceTestEntity;
  }

  public ResponseEntity<EvaluationResponse> utteranceSpeechTestRecording(Integer clientId,
      Integer projectID,
      String modelUUID) {

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("audioFile", new FileSystemResource("src/test/resources/test.wav"));
    ResponseEntity<EvaluationResponse> utteranceTestEntity = testSpeechUtterance(clientId,
        projectID,
        modelUUID, "recording", map);
    System.out.println(utteranceTestEntity);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(HttpStatus.OK, utteranceTestEntity.getStatusCode());
    assertFalse(utteranceTestEntity.getBody().getEvaluations().get(0).getIntents().isEmpty());
    return utteranceTestEntity;
  }

  public ResponseEntity<EvaluationResponse> utteranceSpeechTestLink(Integer clientId,
      Integer projectID,
      String modelUUID) {

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

    map.add("audioURL", "http://anvil.tellme.com/~hsiyamwala/speech.wav");
    ResponseEntity<EvaluationResponse> utteranceTestEntity = testSpeechUtterance(clientId,
        projectID,
        modelUUID, "link", map);

    System.out.println(utteranceTestEntity);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(HttpStatus.OK, utteranceTestEntity.getStatusCode());
    assertFalse(utteranceTestEntity.getBody().getEvaluations().get(0).getIntents().isEmpty());
    return utteranceTestEntity;
  }

  public String batchTest(Integer clientId, Integer projectID, Integer datasetID,
      String modelUUID) {
    List<String> dsetsForBatchTest = new LinkedList<>();
    dsetsForBatchTest.add(Integer.toString(datasetID));
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/")
        .append(modelUUID).append("/eval_datasets?testModelType=" + Constants.DIGITAL_MODEL);

    System.out.println("API in batchTest is -->" + completeURL.toString());

    ResponseEntity<EvaluationResponse> postBatchTests = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()),
            dsetsForBatchTest, EvaluationResponse.class);
    assertEquals(HttpStatus.OK, postBatchTests.getStatusCode());
    EvaluationResponse batchResponse = postBatchTests.getBody();
    String batchTestID = batchResponse.getTestId();
    assertFalse(batchResponse.getTestId().isEmpty());
    assertEquals(batchResponse.getModelId(), modelUUID);
    return batchTestID;
  }

  public String getBatchTestResult(Integer clientId, Integer projectID, String modelUUID,
      String batchTestID)
      throws InterruptedException {
    int k = 0;
    int batchTestTimeout = 250;
    long timeStampBatchTestStart = Calendar.getInstance().getTimeInMillis() - 1;
    String batchTestResult = null;
    while (k < batchTestTimeout) {
      StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
          .append("/projects/").append(projectID).append("/models/")
          .append(modelUUID).append("/check_eval/").append(batchTestID);

      System.out.println("API in getBatchTestResult is -->" + completeURL.toString());

      ResponseEntity<String> getBatchTest = this.restTemplate
          .getForEntity(createCompleteURL(completeURL.toString()),
              String.class);
      assertEquals(HttpStatus.OK, getBatchTest.getStatusCode());
      batchTestResult = getBatchTest.getBody();
      if (batchTestResult.contains("message")) {
        break;
      } else {
        Thread.sleep(5000);
        k++;
      }
    }
    long timeStampBatchTestEnd = Calendar.getInstance().getTimeInMillis() - 1;
    long batchTestTime = timeStampBatchTestEnd - timeStampBatchTestStart;
    System.out.print("Time taken for batch test....." + batchTestTime + "\n");
    System.out.println("Batch test result....." + batchTestResult);
    return batchTestResult;
  }

  public BatchTestResultsResponse listBatchTests(Integer clientId, Integer projectID,
      String modelUUID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectID).append("/models/")
        .append(modelUUID).append("/batch_tests");

    System.out.println("API in listBatchTests is -->" + completeURL.toString());

    ResponseEntity<BatchTestResultsResponse> batchTestsResultsResponse = this.restTemplate
        .getForEntity(completeURL.toString(),
            BatchTestResultsResponse.class);
    assertEquals(HttpStatus.OK, batchTestsResultsResponse.getStatusCode());
    BatchTestResultsResponse batchTestsResults = batchTestsResultsResponse.getBody();
    return batchTestsResults;
  }


  public ResponseEntity<StatsResponse> testPostTaggingStats(Integer projectID,
      SearchRequest search) {

    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("/stats");
    System.out.println("API in testPostTaggingStats is -->" + completeURL.toString());

    ResponseEntity<StatsResponse> taggingStats = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), search, StatsResponse.class);
    assertEquals(HttpStatus.OK, taggingStats.getStatusCode());
    return taggingStats;
  }

  public ResponseEntity<StatsResponse> getTaggingStats(Integer projectID, Integer datasetID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("/datasets/")
        .append(datasetID).append("/stats");

    System.out.println("API in getTaggingStats is -->" + completeURL.toString());

    ResponseEntity<StatsResponse> getTaggingStats = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()),
            StatsResponse.class);
    assertEquals(HttpStatus.OK, getTaggingStats.getStatusCode());
    return getTaggingStats;
  }

  public ResponseEntity<TranscriptionDocumentDetailCollection> testAllTranscriptions(
      Integer projectID, SearchRequest search) throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID);

    System.out.println("API in testAllTranscriptions is -->" + completeURL.toString());

    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), search,
            TranscriptionDocumentDetailCollection.class);
    Thread.sleep(20000);
    assertEquals(HttpStatus.OK, showAllTranscriptions.getStatusCode());
    return showAllTranscriptions;
  }


  public ResponseEntity<TranscriptionDocumentDetailCollection> testAllTranscriptionsWithPagination(
      Integer projectID, SearchRequest search, int startIndex, int limit)
      throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("?sortBy=count:desc").append("&startIndex=" + startIndex).append("&limit=" + limit);

    System.out
        .println("API in testAllTranscriptionsWithPagination is -->" + completeURL.toString());

    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), search,
            TranscriptionDocumentDetailCollection.class);
    Thread.sleep(20000);
    assertEquals(HttpStatus.OK, showAllTranscriptions.getStatusCode());
    return showAllTranscriptions;
  }

  public ResponseEntity<TranscriptionDocumentDetailCollection> testDatasetTranscriptions(
      Integer projectID, Integer datasetID, SearchRequest search) throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("/datasets/").append(datasetID);

    System.out.println("API in testDatasetTranscriptions is -->" + completeURL.toString());

    ResponseEntity<TranscriptionDocumentDetailCollection> showAllTranscriptions = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()), search,
            TranscriptionDocumentDetailCollection.class);
    Thread.sleep(5000);
    assertEquals(HttpStatus.OK, showAllTranscriptions.getStatusCode());
    return showAllTranscriptions;
  }

  public ResponseEntity<TaggingGuideImportStats> getTaggingGuideStats(Integer projectID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("/intentguide/importstats");

    System.out.println("API in getTaggingGuideStats is -->" + completeURL.toString());

    ResponseEntity<TaggingGuideImportStats> getTaggingGuideStats = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()),
            TaggingGuideImportStats.class);
    assertEquals(HttpStatus.OK, getTaggingGuideStats.getStatusCode());
    return getTaggingGuideStats;
  }

  public ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide(Integer projectID) {
    StringBuilder completeURL = new StringBuilder(basePath).append("search/").append(projectID)
        .append("/intentguide?sortBy=count:desc");

    System.out.println("API in getTaggingGuide is -->" + completeURL.toString());

    ResponseEntity<TaggingGuideDocumentDetail[]> getTaggingGuide = this.restTemplate
        .getForEntity(createCompleteURL(completeURL.toString()),
            TaggingGuideDocumentDetail[].class);
    assertEquals(HttpStatus.OK, getTaggingGuide.getStatusCode());
    return getTaggingGuide;
  }

  public ResponseEntity<IntentResponse> testGetIntents(Integer projectID, String query) {
    ResponseEntity<IntentResponse> searchEntity = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "search/" + projectID + "/intents?q=" + query),
            IntentResponse.class);
    assertEquals(HttpStatus.OK, searchEntity.getStatusCode());
    return searchEntity;
  }

  public void testTagProject(Integer projectID, AddIntentRequest addIntentRequest) {
    ResponseEntity<UpdateIntentResponse> bulkTagDataset = this.restTemplate
        .postForEntity(createCompleteURL(basePath + "content/" + projectID + "/tag"),
            addIntentRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, bulkTagDataset.getStatusCode());
  }

  public void testTagDataset(Integer projectID, Integer datasetID,
      AddIntentRequest addIntentRequest) {
    ResponseEntity<UpdateIntentResponse> tagDataset = this.restTemplate
        .postForEntity(createCompleteURL(
            basePath + "content/" + projectID + "/datasets/" + datasetID + "/tag"),
            addIntentRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, tagDataset.getStatusCode());
  }

  public void testUntagProject(Integer projectID, AddIntentRequest deleteIntentRequest) {
    ResponseEntity<UpdateIntentResponse> untagDataset = this.restTemplate

        .postForEntity(createCompleteURL(basePath + "content/" + projectID + "/tag/delete"),
            deleteIntentRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, untagDataset.getStatusCode());
  }

  public void untagDataset(Integer projectID, Integer datasetID,
      AddIntentRequest deleteIntentRequest) {
    ResponseEntity<UpdateIntentResponse> untagDataset = this.restTemplate
        .postForEntity(createCompleteURL(
            basePath + "content/" + projectID + "/datasets/" + datasetID + "/tag/delete"),
            deleteIntentRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, untagDataset.getStatusCode());
  }

  public ResponseEntity<TaggingGuideDocument> testAddTagInTaggingGuide(Integer projectID) {
    ResponseEntity<TaggingGuideDocument> add_tag_tagging_guide = this.restTemplate
        .postForEntity(createCompleteURL(basePath + "content/" + projectID + "/intents"),
            IntegrationUtilTest.addTagInTaggingGuide(),
            TaggingGuideDocument.class);
    assertEquals(HttpStatus.OK, add_tag_tagging_guide.getStatusCode());
    return add_tag_tagging_guide;
  }

  public void testTagUpdate(Integer projectID, AddIntentRequest tagUpdateRequest) {
    ResponseEntity<UpdateIntentResponse> taggingGuideTag = this.restTemplate
        .postForEntity(createCompleteURL(basePath + "content/" + projectID + "/tag/update"),
            tagUpdateRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, taggingGuideTag.getStatusCode());
  }

  public void testTagUpdateDataset(Integer projectID, Integer datasetID,
      AddIntentRequest tagUpdateRequest) {
    ResponseEntity<UpdateIntentResponse> taggingGuideTagDset = this.restTemplate
        .postForEntity(createCompleteURL(
            basePath + "content/" + projectID + "/datasets/" + datasetID + "/tag/update"),
            tagUpdateRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, taggingGuideTagDset.getStatusCode());
  }

  public ResponseEntity<TaggingGuideDocument> testTagUpdateInTaggingGuide(Integer projectID,
      HttpEntity<String> httpEntity, String intentId) {
    ResponseEntity<TaggingGuideDocument> updatedEntity =

        this.restTemplate
            .exchange(createCompleteURL(basePath + "content/" + projectID + "/intents/" + intentId),
                HttpMethod.PATCH,
                httpEntity, TaggingGuideDocument.class);
    assertEquals(HttpStatus.OK, updatedEntity.getStatusCode());
    return updatedEntity;
  }

  public void deleteIntentInTaggingGuide(Integer projectID, String intentId) {
    ResponseEntity<Void> deleteIntent = this.restTemplate
        .exchange(createCompleteURL(basePath + "content/" + projectID + "/intents/" + intentId),
            HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteIntent.getStatusCode());
  }

  public void testAddComment(Integer projectID, AddCommentRequest addCommentRequest)
      throws InterruptedException {
    StringBuilder completeURL = new StringBuilder(basePath).append("content/").append(projectID)
        .append("/comment");

    ResponseEntity<UpdateIntentResponse> addComment = this.restTemplate

        .postForEntity(createCompleteURL(completeURL.toString()), addCommentRequest,
            UpdateIntentResponse.class);
    Thread.sleep(1000);
    assertEquals(HttpStatus.OK, addComment.getStatusCode());
  }

  public void testDatasetComment(Integer projectID, Integer datasetID,
      AddCommentRequest addCommentRequest) {
    ResponseEntity<UpdateIntentResponse> addComment = this.restTemplate

        .postForEntity(createCompleteURL(
            basePath + "content/" + projectID + "/datasets/" + datasetID + "/comment"),
            addCommentRequest,
            UpdateIntentResponse.class);
    assertEquals(HttpStatus.OK, addComment.getStatusCode());
  }

  public ResponseEntity<ReportField[]> testGetReportFields(Integer projectID, Integer datasetID)
      throws IOException {
    ResponseEntity<ReportField[]> getReportFields = this.restTemplate.getForEntity(
        createCompleteURL(
            basePath + "search/" + projectID + "/datasets/" + datasetID + "/getReportFields"),
        ReportField[].class);
    assertEquals(HttpStatus.OK, getReportFields.getStatusCode());
    return getReportFields;
  }

  public void deleteDataset(Integer projectID, Integer datasetID) {
    this.restTemplate
        .delete(createCompleteURL(basePath + "projects/" + projectID + "/datasets/" + datasetID));
    ResponseEntity<Error> entity = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "datasets/" + datasetID), Error.class);
    //assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode()); BUG NT-2275
  }

  public void deleteDatasetES(Integer projectID, Integer datasetID) {
    ResponseEntity<Void> deleteDataset = this.restTemplate
        .exchange(createCompleteURL(basePath + "content/" + projectID + "/datasets/" + datasetID),
            HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteDataset.getStatusCode());
    ResponseEntity<Error> entity = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "datasets/" + datasetID), Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  public void deleteTaggingGuide(Integer projectID) {
    ResponseEntity<Void> deleteTaggingGuide = this.restTemplate
        .exchange(createCompleteURL(basePath + "content/" + projectID + "/intentguide"),
            HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteTaggingGuide.getStatusCode());
  }

  public void testExportTaggingGuide(Integer clientId, Integer projectID) {
    ResponseEntity<String> exportTaggingGuide = this.restTemplate
        .getForEntity(createCompleteURL(
            basePath + "clients/" + clientId + "/projects/" + projectID + "/taggingguide/export"),
            String.class);
    assertEquals(HttpStatus.OK, exportTaggingGuide.getStatusCode());
    assertThat(exportTaggingGuide.getHeaders().getContentLength(), greaterThan(0l));
  }

  public ResponseEntity<TaggingGuideColumnList> testGetTaggingGuideColumns() {
    ResponseEntity<TaggingGuideColumnList> getTaggingGuideColumns = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "taggingguide/columns"),
            TaggingGuideColumnList.class);
    assertEquals(HttpStatus.OK, getTaggingGuideColumns.getStatusCode());
    return getTaggingGuideColumns;
  }

  public void getTaggingGuideImportColMapping(String clientId, Integer projectId) {
    ResponseEntity<TaggingGuideColumnMappingCollection> taggingGuideImportColMapping = this.restTemplate
        .getForEntity(createCompleteURL(
            basePath + "projects/" + clientId + "/" + projectId + "/import/column/mapping"),
            TaggingGuideColumnMappingCollection.class);
    assertEquals(HttpStatus.OK, taggingGuideImportColMapping.getStatusCode()); //BUG NT-2341
  }

  public String testImportTaggingGuide(Integer clientId, Integer projectID) throws IOException {

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("Integration_Test_Tagging_Guide.csv"));
    map.add("username", currentUserId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
        map, headers);

    ResponseEntity<TaggingGuideStagedImportResponse> importTaggingGuide = this.restTemplate
        .exchange(
            createCompleteURL(basePath + "taggingguide/" + clientId + "/" + projectID + "/import"),
            HttpMethod.POST, requestEntity, TaggingGuideStagedImportResponse.class);

    assertEquals(HttpStatus.OK, importTaggingGuide.getStatusCode());
    assertNotNull(importTaggingGuide.getBody());
    String token = importTaggingGuide.getBody().getToken();
    return token;
  }

  public ResponseEntity<TaggingGuideColumnMappedResponse> testImportTaggingGuideColMapping(
      Integer clientId, Integer projectID, String token) {
    TaggingGuideColumnMappingSelectionList colMapping = new TaggingGuideColumnMappingSelectionList();
    TaggingGuideColumnMappingSelection column = new TaggingGuideColumnMappingSelection();
    column.setColumnName("intent");
    column.setColumnIndex("2");
    column.setDisplayName("Intent");
    colMapping.add(column);
    ResponseEntity<TaggingGuideColumnMappedResponse> importTaggingGuideColMapping = this.restTemplate

        .postForEntity(createCompleteURL(
            basePath + "taggingguide/" + clientId + "/" + projectID + "/import/" + token
                + "/column/mapping?ignoreFirstRow=true"), colMapping,
            TaggingGuideColumnMappedResponse.class);

    assertEquals(HttpStatus.OK, importTaggingGuideColMapping.getStatusCode());
    return importTaggingGuideColMapping;
  }

  public ResponseEntity<TaggingGuideImportStatBO> testImportTaggingGuideCommit(Integer clientId,
      Integer projectID, String token) {
    ResponseEntity<TaggingGuideImportStatBO> commit = this.restTemplate

        .postForEntity(createCompleteURL(
            basePath + "taggingguide/" + clientId + "/" + projectID + "/import/" + token
                + "/commit"), null, TaggingGuideImportStatBO.class);
    assertEquals(HttpStatus.OK, commit.getStatusCode());
    return commit;
  }

  public void testGetDatatypes() {
    ResponseEntity<String> getDatatypes = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "resources/datatypes"), String.class);
    assertEquals(HttpStatus.OK, getDatatypes.getStatusCode());
    assertTrue(getDatatypes.getBody().contains("Audio/Voice (Live)"));
    assertTrue(getDatatypes.getBody().contains("Audio/Voice (Data Collection)"));
    assertTrue(getDatatypes.getBody().contains("Chat/Text"));
    assertTrue(getDatatypes.getBody().contains("Virtual Assistant/Text"));
    assertTrue(getDatatypes.getBody().contains("Social/Text"));
    assertTrue(getDatatypes.getBody().contains("Email/Text"));
    assertTrue(getDatatypes.getBody().contains("Synthetic/Text"));
  }

  public void testGetVerticals() {
    ResponseEntity<String> getVerticals = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "resources/verticals"), String.class);
    assertEquals(HttpStatus.OK, getVerticals.getStatusCode());
    assertTrue(getVerticals.getBody().contains("FINANCIAL"));
    assertTrue(getVerticals.getBody().contains("HEALTHCARE"));
    assertTrue(getVerticals.getBody().contains("INSURANCE"));
    assertTrue(getVerticals.getBody().contains("RETAIL"));
    assertTrue(getVerticals.getBody().contains("TECHNOLOGY"));
    assertTrue(getVerticals.getBody().contains("TELCO"));
    assertTrue(getVerticals.getBody().contains("TRAVEL"));
    assertTrue(getVerticals.getBody().contains("UTILITIES"));
    assertTrue(getVerticals.getBody().contains("OTHER"));
  }

  public void testGetLocales() {
    ResponseEntity<String> getLocales = this.restTemplate
        .getForEntity(createCompleteURL(basePath + "resources/locales"), String.class);
    assertEquals(HttpStatus.OK, getLocales.getStatusCode());
    assertTrue(getLocales.getBody().contains("en-US"));
    assertTrue(getLocales.getBody().contains("fr-FR"));
    assertTrue(getLocales.getBody().contains("de-DE"));
  }

  public void testModelDelete(Integer clientId, Integer projectId, Integer modelDBId)
      throws InterruptedException {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientId)
        .append("/projects/").append(projectId)
        .append("/models/").append(modelDBId);

    System.out.println("API in testModelDelete is -->" + completeURL.toString());

    ResponseEntity<Void> deleteModel = this.restTemplate
        .exchange(createCompleteURL(completeURL.toString()),
            HttpMethod.DELETE, null, Void.class);

    Thread.sleep(6000);

    this.restTemplate.delete(createCompleteURL(completeURL.toString()));

    Thread.sleep(6000);

    ResponseEntity<Error> deletedModel = this.restTemplate
            .getForEntity(createCompleteURL(completeURL.toString()), Error.class);

    assertEquals(HttpStatus.NOT_FOUND, deletedModel.getStatusCode());

    System.out.println("model uuid deleted is -->" + modelDBId + "\n\n");

  }

  public void testPublishModel(Integer clientID, List<ProjectModelRequest> projectModels) {

    StringBuilder completeURL = new StringBuilder(basePath).append("clients/").append(clientID)
        .append("/publish");

    System.out.println("API in testPublishModel is -->" + completeURL.toString());

    ResponseEntity<Void> publishModel = this.restTemplate
        .postForEntity(createCompleteURL(completeURL.toString()),
            projectModels, Void.class);

    assertEquals(HttpStatus.OK, publishModel.getStatusCode());

  }
}
