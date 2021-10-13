/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.FileColumnBO;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.util.CommonLib;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class FilesApiTest {

  private String basePath;

  private String currentUserId = "UnitTest@247-inc.ai";
  @Autowired
  private TestRestTemplate restTemplate;
  @Inject
  @Qualifier("fileManagerBean")
  private FileManager fileManager;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    DataType.addValue("SPEECH");
    DataType.addValue("OMNICHANNEL");
    DataType.addValue("AIVA");
    this.populateFileCols();
  }

  @Test
  public void testAddFile() throws IOException, SchedulerException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-input.csv"));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files";
    ResponseEntity<FileEntryDetail> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, FileEntryDetail.class);
    log.info(entity.getBody().toString());

    assertTrue(entity.getStatusCode().is2xxSuccessful());

    fileManager.deleteFileById(entity.getBody().getFileId());
  }

  @Test
  public void testAddFileWrongEncoding() throws IOException, SchedulerException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-input-iso-8859-1.csv"));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files";
    ResponseEntity<Error> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, Error.class);
    log.info(entity.getBody().toString());
    assertTrue(entity.getStatusCode().is4xxClientError());

  }

  @Test
  public void testAddFileEmptyFile() throws IOException {
    File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
    file.deleteOnExit();
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new FileSystemResource(file));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files";
    ResponseEntity<Error> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, Error.class);
    log.info(entity.getBody().toString());

    assertTrue(entity.getStatusCode().is4xxClientError());
  }

  @Test
  public void testAddFileInvalidFile() throws IOException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-bad-input.csv"));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files";
    ResponseEntity<Error> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, Error.class);
    log.info(entity.getBody().toString());
    // Initial columns are no longer required to include transcription and
    // filename as the user can assign them in the ui.
    assertTrue(entity.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void testImportFile() throws IOException, SchedulerException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-input.csv"));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files/import";
    ResponseEntity<FileStagedImportResponse> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, FileStagedImportResponse.class);
    FileStagedImportResponse fileStagedImportResponse = entity.getBody();

    assertTrue(entity.getStatusCode().is2xxSuccessful());
    assertNotNull(fileStagedImportResponse);

    log.info(fileStagedImportResponse.toString());

    fileManager.deleteFileById(fileStagedImportResponse.getToken());
  }

  @Test
  public void testGenerateUserSelectedColumnsFile()
      throws IOException, SchedulerException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", new ClassPathResource("test-input.csv"));
    map.add("username", currentUserId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<LinkedMultiValueMap<String, Object>>(map,
            headers);

    String url = basePath + "files/import";
    System.out.println("url-->" + url);
    ResponseEntity<FileStagedImportResponse> entity = this.restTemplate
        .exchange(url, HttpMethod.POST, requestEntity,
            FileStagedImportResponse.class);
    FileStagedImportResponse fileStagedImportResponse = entity.getBody();
    assertTrue(entity.getStatusCode().is2xxSuccessful());
    assertNotNull(fileStagedImportResponse);
    log.info(fileStagedImportResponse.toString());

    ObjectMapper mapper = new ObjectMapper();
    String json = "["
        + "{\"id\":\"2\",\"columnName\":\"filename\",\"columnIndex\":\"4\",\"displayName\":\"Filename\"},"
        + "{\"id\":\"1\",\"columnName\":\"transcription\",\"columnIndex\":\"9\",\"displayName\":\"Transcription\"},"
        + "{\"id\":\"3\",\"columnName\":\"inheritedIntent\",\"columnIndex\":\"10\",\"displayName\":\"Intent Tag\"}"
        + "]";
    FileColumnMappingSelectionList fileColumnMappingSelections =
        mapper.readValue(json, FileColumnMappingSelectionList.class);

    url = url + "/" + fileStagedImportResponse.getToken() + "/column/mapping?ignoreFirstRow=true";

    System.out.println("url-->" + url);

    ResponseEntity<FileEntryDetail> columnEntity = this.restTemplate
        .postForEntity(url, fileColumnMappingSelections, FileEntryDetail.class);
    assertTrue(columnEntity.getStatusCode().is2xxSuccessful());

    // Check for Bug NT-1055 fix
    FileEntryDetail fileEntryDetail = columnEntity.getBody();
    Path filePath = Paths.get("").toAbsolutePath().resolve(fileEntryDetail.getSystemName());
    assertTrue(Files.notExists(filePath));

    fileManager.deleteFileById(fileStagedImportResponse.getToken());
    fileManager.deleteFileById(fileEntryDetail.getFileId());

  }

  private void populateFileCols() {
    log.info("Populating file columns");
    Resource resource = new ClassPathResource("file_columns.csv");
    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
      List<String[]> lineTokensList = reader
          .lines()
          .filter(line -> !line.startsWith("#"))
          .map(line -> line.split(","))
          .filter(lineTokens -> lineTokens.length == 3)
          .collect(Collectors.toList());
      for (String[] lineTokens : lineTokensList) {
        FileColumnBO column = new FileColumnBO();
        column.setName(lineTokens[0]);
        column.setDisplayName(lineTokens[2]);
        column.setRequired(Boolean.valueOf(lineTokens[1]));
        column.create();
      }
    } catch (IOException e) {
      log.error("Failed seeding file columns", e);
    }
  }

}
