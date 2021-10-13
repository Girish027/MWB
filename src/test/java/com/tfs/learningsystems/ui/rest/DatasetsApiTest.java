/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.auth.AuthValidationBaseTest;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.BusinessObject;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.DatasetDetail;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.util.CommonLib;
import java.util.Calendar;
import java.util.Collections;
import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DatasetsApiTest extends AuthValidationBaseTest {

  private static String currentUserId = "UnitTest@247.ai";
  private String basePath;
  private ClientBO testClient;
  private ProjectBO testProject;

  SecurityContext securityContext = Mockito.mock(SecurityContext.class);

  @Autowired
  private TestRestTemplate restTemplate;

  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Inject
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix();
  }

  @Before
  public void setUp() throws Exception {

    super.setUp();
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    this.testClient = ModelUtils.getTestClientObject(name);
    this.testClient.create();

    this.restTemplate.getRestTemplate()
        .setInterceptors(Collections.singletonList((request, body, execution) -> {
          request.getHeaders().add("X-247-UserId", currentUserId);
          return execution.execute(request, body);
        }));

    DataType.addValue("SPEECH");
    DataType.addValue("OMNICHANNEL");
    DataType.addValue("AIVA");

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    testProject = ModelUtils.getTestProjectObject(testClient.getId(), currentUserId, name);
    testProject.create();

    this.setClientAdminInUserGroup();

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void testGetDataset() {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset = datasetManager.addDataset(addDatasetRequest, currentUserId);
    projectManager.addDatasetProjectMapping(this.testClient.getId().toString(),
            this.testProject.getId().toString(),
            dataset.getId().toString(), currentUserId);

    ResponseEntity<DatasetBO> entity =
        this.restTemplate
            .getForEntity(basePath + "/v1/clients/" + this.testClient.getId()
                    + "/projects/" + this.testProject.getId() + "/datasets/" + dataset.getId(),
                DatasetBO.class);
    DatasetBO returnedDataset = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(dataset.getId(), returnedDataset.getId());
    assertEquals(returnedDataset.getName(), dataset.getName());
    returnedDataset.delete();
  }

  @Test
  public void testAddDataset() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);

    ResponseEntity<DatasetBO> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    createdDataset.delete();
  }

  @Test
  public void testAddDatasetWithDefaultSource() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
            .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    ResponseEntity<DatasetBO> entity = this.restTemplate
            .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    assertEquals(DatasetBO.Source.E.getValue(), createdDataset.getSource());
    createdDataset.delete();
  }

  @Test
  public void testAddDatasetWithExternalSource() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
            .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset= addDatasetRequest.getDataset();
    dataset.setSource(DatasetBO.Source.E);
    ResponseEntity<DatasetBO> entity = this.restTemplate
            .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    assertEquals(DatasetBO.Source.E.getValue(), createdDataset.getSource());
    createdDataset.delete();
  }

  @Test
  public void testAddDatasetWithInternalSource() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
            .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset= addDatasetRequest.getDataset();
    dataset.setSource(DatasetBO.Source.I);
    ResponseEntity<DatasetBO> entity = this.restTemplate
            .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    assertEquals(DatasetBO.Source.I.getValue(), createdDataset.getSource());
    createdDataset.delete();
  }

  @Test
  public void testAddDatasetWithAgentSource() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
            .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset= addDatasetRequest.getDataset();
    dataset.setSource(DatasetBO.Source.A);
    ResponseEntity<DatasetBO> entity = this.restTemplate
            .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.FORBIDDEN, entity.getStatusCode());
  }

  @Test
  public void testPostDataset() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-dataset.csv"));
    map.add("name", name);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);
    String url = basePath + "/v1/clients/" + this.testClient.getId()
        + "/projects/" + this.testProject.getId() + "/datasets";
    ResponseEntity<DatasetBO> entity1 = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, DatasetBO.class);

    DatasetBO createdDataset = entity1.getBody();
    assertEquals(HttpStatus.CREATED, entity1.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    createdDataset.delete();
  }

  @Test
  public void testAddDatasetInheritingIntents() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    addDatasetRequest.setAutoTagDataset(true);
    addDatasetRequest.setProjectId(Integer.toString(project.getId()));

    ResponseEntity<DatasetBO> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    DatasetBO createdDataset = entity.getBody();
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    assertEquals(createdDataset.getCreatedAt(), createdDataset.getModifiedAt());
    assertTrue(timeStamp < createdDataset.getCreatedAt());
    createdDataset.delete();
    project.delete();
  }

  @Test
  public void testAddDatasetNotFoundClientId() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    addDatasetRequest.getDataset().setClientId(null);

    ResponseEntity<Error> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
  }


  @Test
  public void testAddDatasetInvalidClientId() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    addDatasetRequest.getDataset().setClientId("a");

    ResponseEntity<Error> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, Error.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
  }

  @Test
  public void testListDatasets() {

    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-dataset.csv"));
    map.add("name", name);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
            new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
                    headers);
    String url = basePath + "/v1/clients/" + this.testClient.getId()
            + "/projects/" + this.testProject.getId() + "/datasets";
    ResponseEntity<DatasetBO> entity1 = this.restTemplate
            .exchange(url, HttpMethod.POST, requestEntity, DatasetBO.class);

    ResponseEntity<DatasetsDetail> entity =
        this.restTemplate.getForEntity(basePath + "/v1/clients/" + this.testClient.getId()
            + "/projects/" + this.testProject.getId() + "/datasets", DatasetsDetail.class);
    DatasetsDetail datasets = entity.getBody();
    assertFalse(datasets.isEmpty());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    entity1.getBody().delete();
  }

  @Test
  public void testDeleteAndReAddDataset() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO createdDataset = datasetManager.addDataset(addDatasetRequest, currentUserId);

    this.restTemplate.delete(basePath + "v1/datasets/" + createdDataset.getId());
    ResponseEntity<Error> entity = this.restTemplate.getForEntity(
        basePath + "v1/datasets/" + createdDataset.getId(), Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

    ResponseEntity<DatasetBO> createdEntity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    assertEquals(HttpStatus.CREATED, createdEntity.getStatusCode());
    createdDataset.delete();
  }

  @Test
  public void testPatchDataset() throws JsonProcessingException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO datasetDetail = datasetManager.addDataset(addDatasetRequest, currentUserId);

    projectManager.addDatasetProjectMapping(this.testClient.getId().toString(),
                    this.testProject.getId().toString(),
                    datasetDetail.getId().toString(), currentUserId);

    assertNotNull(datasetManager.getDatasetById(Integer.toString(datasetDetail.getId())));
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/description");
    patchDocument.setValue("Changed description");
    patchRequest.add(patchDocument);

    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);

    String url = basePath + "/v1/clients/" + this.testClient.getId()
        + "/projects/" + this.testProject.getId() + "/datasets/" + datasetDetail.getId();
    ResponseEntity<DatasetDetail> entity =
        this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, DatasetDetail.class);
    DatasetDetail patchedDataset = entity.getBody();
    assertThat(entity.getStatusCode(), is(equalTo(HttpStatus.OK)));
    assertThat(patchedDataset.getDescription(), is(equalTo("Changed description")));

    assertEquals(datasetDetail.getCreatedAt(), datasetDetail.getModifiedAt());
    assertTrue(patchedDataset.getCreatedAt() < patchedDataset.getModifiedAt());
    datasetDetail.delete();

    // test patch on non-existing dataset
    url = basePath + "/v1/clients/" + this.testClient.getId()
        + "/projects/" + this.testProject.getId() + "/datasets" + (Integer.MAX_VALUE - 100);
    ResponseEntity<Error> unknownEntity = this.restTemplate.exchange(url,
        HttpMethod.PATCH, httpEntity, Error.class);
    assertThat(unknownEntity.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));

  }


  @Test
  public void testAddUnicodeDataset() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);

    DatasetBO dataset = addDatasetRequest.getDataset();
    dataset.setName("hükan");
    dataset.setDescription("hükan's dataset");
    addDatasetRequest.setDataset(dataset);
    ResponseEntity<DatasetBO> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    // NT-2373
    BusinessObject.sanitize(dataset);

    DatasetBO createdDataset = entity.getBody();
    assertNotNull(createdDataset);
    assertEquals(dataset.getName(), createdDataset.getName());
    assertEquals(dataset.getDescription(), createdDataset.getDescription());
    createdDataset.delete();

  }

  @Test
  public void testDuplicateDataset() {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO baseDataset = addDatasetRequest.getDataset();
    baseDataset.setName("dataset");
    baseDataset.setDescription("bad dataset");
    DatasetBO dataset1 = baseDataset;
    addDatasetRequest.setDataset(dataset1);
    ResponseEntity<DatasetBO> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, DatasetBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    ResponseEntity<Error> duplicateEntity =
        this.restTemplate.postForEntity(basePath + "v1/datasets", addDatasetRequest,
            Error.class);
    assertEquals(HttpStatus.CONFLICT, duplicateEntity.getStatusCode());
    entity.getBody().delete();

    ClientBO secondClient = ModelUtils.getTestClientObject(name);
    secondClient.create();
    dataset1.setClientId(Integer.toString(secondClient.getId()));
    addDatasetRequest.setDataset(dataset1);
    entity =
        this.restTemplate.postForEntity(basePath + "v1/datasets", addDatasetRequest,
            DatasetBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    entity.getBody().delete();
  }

  @Test
  public void testRequiredDatasetFields() {
    AddDatasetRequest addDatasetRequest = new AddDatasetRequest();
    DatasetBO dataset = new DatasetBO();
    dataset.setDescription("bad dataset");
    addDatasetRequest.setDataset(dataset);

    ResponseEntity<Error> entity = this.restTemplate
        .postForEntity(basePath + "v1/datasets", addDatasetRequest, Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
  }

  @Test
  public void testGetDatasetErrorConditions() {
    ResponseEntity<Error> entity =
        this.restTemplate
            .getForEntity(basePath + "v1/datasets/" + (Integer.MAX_VALUE - 100), Error.class);
    assertNotNull(entity);
    assertThat(entity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));

    ResponseEntity<Error> deletedEntity =
        this.restTemplate
            .getForEntity(basePath + "v1/datasets/" + (Integer.MAX_VALUE - 100), Error.class);
    assertNotNull(deletedEntity);
    assertThat(deletedEntity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));
  }

//    @Test
//    public void testPaging() {
//        String offset = "0";
//        String totalCount = "2";
//        AddDatasetRequest addDatasetRequest1 = ModelUtils.getTestAddDatasetRequestObject();
//        Dataset dataset1 = addDatasetRequest1.getDataset().name("Dataset One").description("bad dataset");
//        addDatasetRequest1.setDataset(dataset1);
//        ResponseEntity<DatasetDetail> entity = this.restTemplate
//                .postForEntity(basePath + "v1/datasets", addDatasetRequest1, DatasetDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//
//        AddDatasetRequest addDatasetRequest2 = ModelUtils.getTestAddDatasetRequestObject();
//        Dataset dataset2 = addDatasetRequest2.getDataset().name("Dataset two").description("good dataset");
//        addDatasetRequest2.setDataset(dataset2);
//        entity = this.restTemplate.postForEntity(basePath + "v1/datasets", addDatasetRequest2,
//                DatasetDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//
//        ResponseEntity<DatasetsDetail> readEntity = this.restTemplate
//                .getForEntity(basePath + "v1/datasets?limit=1&startIndex=0", DatasetsDetail.class);
//        DatasetsDetail datasets = readEntity.getBody();
//        assertEquals(1, datasets.size());
//        assertEquals(HttpStatus.OK, readEntity.getStatusCode());
//        assertEquals(dataset1.getName(), datasets.get(0).getName());
//        assertEquals(offset, readEntity.getHeaders().getFirst("X-Offset"));
//        assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));
//
//
//        offset = "1";
//
//        readEntity = this.restTemplate.getForEntity(readEntity.getHeaders().getLocation(),
//                DatasetsDetail.class);
//        datasets = readEntity.getBody();
//        assertEquals(1, datasets.size());
//        assertEquals(HttpStatus.OK, readEntity.getStatusCode());
//        assertEquals(dataset2.getName(), datasets.get(0).getName());
//        assertEquals(offset, readEntity.getHeaders().getFirst("X-Offset"));
//        assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));
//
//        // Handle error case
//        ResponseEntity<Error> read = this.restTemplate
//                .getForEntity(basePath + "v1/datasets?limit=10&startIndex=10", Error.class);
//        assertEquals(HttpStatus.BAD_REQUEST, read.getStatusCode());
//    }

  @Test
  public void testDatasetEquivalence() {
    DatasetBO dataset1 = new DatasetBO();
    dataset1.setName("Dataset One");
    dataset1.setDescription("Big Dataset");
    dataset1.setClientId("1");
    dataset1.setReceivedAt(0L);
    dataset1.setDataType("OMNICHANNEL");
    dataset1.setUri("https://localhost:8443/tmp/tempfile.csv");

    DatasetBO dataset2 = new DatasetBO();
    dataset2.setName("Dataset One");
    dataset2.setDescription("Big Dataset");
    dataset2.setClientId("1");
    dataset2.setReceivedAt(0L);
    dataset2.setDataType("OMNICHANNEL");
    dataset2.setUri("https://localhost:8443/tmp/tempfile.csv");

    assertEquals(dataset1, dataset2);

    dataset2.setDataType("SPEECH");
    assertNotEquals(dataset1, dataset2);
  }

  @Test
  public void testPrettyPrint() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset = datasetManager.addDataset(addDatasetRequest, currentUserId);
    String stringResponse =
        this.restTemplate.getForObject(basePath + "v1/datasets/" + dataset.getId(), String.class);
    String prettyStringResponse =
        this.restTemplate
            .getForObject(basePath + "v1/datasets/" + dataset.getId() + "?pretty", String.class);
    assertNotEquals(stringResponse, prettyStringResponse);
    assertThat(prettyStringResponse, containsString("\n"));
    dataset.delete();

  }

  @Test
  public void testToJsonString() throws Exception {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(this.testClient.getId(), this.testProject.getId(), name);
    DatasetBO dataset = addDatasetRequest.getDataset();
    String datasetJsonString = dataset.toJsonString();
    ObjectMapper mapper = new ObjectMapper();
    DatasetBO readValue = mapper.readValue(datasetJsonString, DatasetBO.class);
    assertEquals(readValue, dataset);

    DatasetBO datasetDetail = new DatasetBO();
    BeanUtils.copyProperties(datasetDetail, dataset);
    String datasetDetailJsonString = datasetDetail.toJsonString();
    DatasetBO pdReadValue = mapper.readValue(datasetDetailJsonString, DatasetBO.class);
    assertEquals(pdReadValue, datasetDetail);

  }

}
