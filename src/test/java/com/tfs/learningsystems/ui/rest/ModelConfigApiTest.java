/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ConfigManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ModelConfigApiTest {

  private static String currentUserId = "UnitTest@247.ai";
  private String basePath;
  private ClientBO testClient;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ConfigManager configManager;
  @Autowired
  private AppConfig appConfig;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws ScriptException, SQLException, IOException {
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    this.testClient = ModelUtils.getTestClientObject(name);
    this.testClient.create();

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

  }

  @Test
  public void testAddDefaultConfig() {
    try {
      this.configManager.reloadDefaultConfig(appConfig.getEnglishConfigArchiveFilename(),
          Constants.DEFAULT_ENGLISH_CONFIG_ID, "en");
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testAddConfig() throws IOException, SchedulerException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", Integer.toString(project.getId()));
    map.add("file", new ClassPathResource("support_files/config.json"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "configs";
    ResponseEntity<ModelConfigBO> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, ModelConfigBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    ModelConfigBO body = entity.getBody();
    assertNotNull(body);
    body.delete();
    project.delete();

  }

  @Test
  public void testAddConfigWData() throws IOException, SchedulerException {

    String methodName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    ModelConfigBO configDetail = new ModelConfigBO();
    configDetail.setName(name);
    configDetail.setDescription(name);
    configDetail.setProjectId(project.getId());
    this.readArchiveFile(configDetail,
        new ClassPathResource("support_files/config.json").getInputStream());

    String url = basePath + "configs";

    ResponseEntity<ModelConfigBO> entity = this.restTemplate
        .postForEntity(url, configDetail, ModelConfigBO.class);

    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    ModelConfigBO body = entity.getBody();
    assertNotNull(body);
    body.delete();
    project.delete();

  }

  @Test
  public void testGetConfig() throws IOException, SchedulerException {
    String methodName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", project.getId());
    map.add("file", new ClassPathResource("support_files/config.json"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "configs";
    ResponseEntity<ModelConfigBO> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, ModelConfigBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    ModelConfigBO body = entity.getBody();

    assertNotNull(body);

    url = basePath + "configs/" + body.getId();
    ResponseEntity<ModelConfigBO> getEntity = this.restTemplate
        .getForEntity(url, ModelConfigBO.class);

    assertTrue(getEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(getEntity.getBody());
    body.delete();
    project.delete();


  }

  @Test
  public void testGetConfigWData() throws IOException, SchedulerException {
    String methodName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setCid(this.testClient.getCid());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", project.getId());
    map.add("cid", project.getCid());
    map.add("file", new ClassPathResource("support_files/config.json"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "configs";

    ResponseEntity<ModelConfigBO> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, ModelConfigBO.class);

    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    ModelConfigBO body = entity.getBody();

    assertNotNull(body);

    url = basePath + "clients/" + this.testClient.getId() + "/configs/" + body.getId() + "/data";

    ResponseEntity<ModelConfigBO> getEntity = this.restTemplate
        .getForEntity(url, ModelConfigBO.class);

    assertTrue(getEntity.getStatusCode().is2xxSuccessful());
    assertNotNull(getEntity.getBody());
    assertNotNull(getEntity.getBody().getConfigFile());
    body.delete();
    project.delete();

  }

  @Test
  public void testGetConfigFile() throws IOException, SchedulerException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", project.getId());
    map.add("file", new ClassPathResource("support_files/config.json"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    // Add new config
    String url = basePath + "configs";
    ResponseEntity<ModelConfigBO> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, ModelConfigBO.class);

    assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    ModelConfigBO body = entity.getBody();

    assertNotNull(body);

    // Download newly created config
    url = basePath + "configs/" + body.getId() + "/download";

    restTemplate.getRestTemplate().getMessageConverters().add(
        new ByteArrayHttpMessageConverter());

    headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    ResponseEntity<byte[]> response = restTemplate.exchange(
        url, HttpMethod.GET, httpEntity, byte[].class, "1");

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertThat(response.getHeaders().getContentLength(), greaterThan(0l));
    assertThat(response.getBody().length, greaterThan(0));

    // Add downloaded config to make sure file is valid
    File file = File.createTempFile("config", ".json");
    file.deleteOnExit();
    Files.write(file.toPath(), response.getBody());

    map = new LinkedMultiValueMap<>();
    map.add("name", "name_2_" + name);
    map.add("description", name);
    map.add("projectId", project.getId());
    map.add("file", new FileSystemResource(file));

    headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity1 =
        new HttpEntity<>(map,
            headers);

    url = basePath + "configs";

    ResponseEntity<ModelConfigBO> addEntity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity1, ModelConfigBO.class);

    assertEquals(HttpStatus.CREATED, addEntity.getStatusCode());
    ModelConfigBO body1 = addEntity.getBody();
    assertNotNull(body1);

    body.delete();
    body1.delete();
    project.delete();
  }

  // Currently our application doesn't throw an error while uploading an empty config and uses default , needs to be discussed.
  @Ignore
  @Test
  public void testAddConfigEmptyFile() throws IOException {
    File file = File.createTempFile("config", ".json");
    file.deleteOnExit();

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new FileSystemResource(file));
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", project.getId());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "configs";

    ResponseEntity<Error> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, Error.class);
    log.info(entity.getBody().toString());

    assertTrue(entity.getStatusCode().is4xxClientError());
    project.delete();

  }


  public void testAddConfigInvalidFile() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("name", name);
    map.add("description", name);
    map.add("projectId", project.getId());
    map.add("file", new ClassPathResource("test-bad-input.csv"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "configs";

    ResponseEntity<Error> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, Error.class);
    log.info(entity.getBody().toString());

    assertTrue(entity.getStatusCode().is4xxClientError());
    project.delete();
  }

  @Test
  public void testPatchConfig() throws IOException {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ProjectBO project = ModelUtils
        .getTestProjectObject(this.testClient.getId(), currentUserId, name);
    project.setClientId(this.testClient.getId());
    project.setOwnerId(currentUserId);
    project.create();

    ModelConfigBO configDetail = new ModelConfigBO();
    configDetail.setName(name);
    configDetail.setDescription(name);
    configDetail.setProjectId(project.getId());
    configDetail.setUserId(currentUserId);
    this.readArchiveFile(configDetail,
        new ClassPathResource("support_files/config.json").getInputStream());
    String changedDescription = "CHANGED DESCRIPTION!";
    ModelConfigBO createdConfig =
        this.configManager.addModelConfig(configDetail);
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/description");
    patchDocument.setValue(changedDescription);
    patchRequest.add(patchDocument);

    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);
    String url = basePath + "configs/" + createdConfig.getId();

    ResponseEntity<ModelConfigBO> responseEntity = this.restTemplate
        .exchange(url, HttpMethod.PATCH, httpEntity,
            ModelConfigBO.class);

    assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.OK)));
    ModelConfigBO patchedConfig = responseEntity.getBody();
    assertThat(patchedConfig.getDescription(), is(equalTo(changedDescription)));
    patchedConfig.delete();
    project.delete();

  }

  private void readArchiveFile(ModelConfigBO configDetail, InputStream inputStream)
      throws IOException {

    int readFiles = 0;
    String configFile = null;
    if (inputStream != null) {
      inputStream.available();
    }

    if (inputStream != null) {
      configFile = IOUtils.toString(inputStream, Charset.defaultCharset());
      readFiles++;
    }

    if (readFiles < 1) {
      throw new IOException("invalid_config_archive_file - missing config.json");
    }
    configDetail.setConfigFile(configFile);
  }
}
