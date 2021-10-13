/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
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
import java.util.Arrays;
import java.util.Calendar;

import static com.tfs.learningsystems.util.Constants.TRAINING_OUTPUT_PURGE_TTL_IN_DAYS;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class TrainingOutputApiTest {

  private String basePath;
  private String userId = "UnitTest@247.ai";

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
  public void testPurgedTrainingOutputsDownload() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();
    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), userId, name);
    project.create();
    ModelBO modelObject = ModelUtils.getModelObject(project.getId());
    modelObject.create();
    modelObject.setCreatedAt(Calendar.getInstance().getTimeInMillis() - ((TRAINING_OUTPUT_PURGE_TTL_IN_DAYS + 5) * 24 * 60 * 60 * 1000));
    modelObject.update();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.valueOf("application/json"));
    HttpEntity < String > httpEntity = new HttpEntity < > (headers);
    String url = basePath + "clients/" + client.getId() + "/projects/" + project.getId() + "/models/" + modelObject.getId() + "/training-outputs";
    ResponseEntity < byte[] > entity =
            this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

  }

}