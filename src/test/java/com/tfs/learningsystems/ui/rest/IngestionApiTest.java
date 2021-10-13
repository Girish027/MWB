/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.util.CommonLib;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.sql.SQLException;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class IngestionApiTest {

  private String basePath;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {
    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());
  }

  @Test
  public void testErrorMessageIngestion() {
    String logMessage = "Some test error log message";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/plain"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (logMessage, headers);
    String url = basePath + "ingest/error/log";
    ResponseEntity < Void > entity =
            this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  public void testWarningMessageIngestion() {
    String logMessage = "Some test error log message";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/plain"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (logMessage, headers);
    String url = basePath + "ingest/warning/log";
    ResponseEntity < Void > entity =
            this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

  @Test
  public void testInfoLogMessageIngestion() {
    String logMessage = "Some test error log message";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/plain"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (logMessage, headers);
    String url = basePath + "ingest/info/log";
    ResponseEntity < Void > entity =
            this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
  }

}