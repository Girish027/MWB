/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ProjectDetail;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.ErrorMessage;
import java.sql.SQLException;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SearchApiTest {

  private String basePath;

  private String currentUserId = "UnitTest@247.ai";
  @Autowired
  private TestRestTemplate restTemplate;
  @Inject
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;
  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_2";
    client = ModelUtils.getTestClientObject(name);
    client.create();

    Vertical.addValue("FINANCIAL");
    DataType.addValue("AIVA");
  }

  @Test
  public void testGetIntentsProjectNotFoundError() {
    ResponseEntity<Error> entity =
        this.restTemplate
            .getForEntity(basePath + "search/" + (Integer.MAX_VALUE - 100) + "/intents",
                Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  @Test
  public void testGetNullIntentsEmptyResults() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    ProjectDetail projectDetail = new ProjectDetail();
    BeanUtils.copyProperties(project,projectDetail);
    projectDetail.setVertical(Vertical.valueOf(project.getVertical()));
    ProjectBO addProject =
        this.projectManager.addProject(client.getId().toString(), projectDetail);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(client.getId(), addProject.getId(), name);
    DatasetBO addDataset =
        this.datasetManager.addDataset(addDatasetRequest, currentUserId);
    this.projectManager
        .addDatasetProjectMapping(client.getId().toString(), Integer.toString(addProject.getId()),
            Integer.toString(addDataset.getId()), currentUserId);
    ResponseEntity<String[]> entity =
        this.restTemplate
            .getForEntity(basePath + "search/" + addProject.getId() + "/intents", String[].class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertArrayEquals(entity.getBody(), new String[]{});
  }

  @Test
  public void testSearchProjectNotFoundError() {
    ResponseEntity<Error> entity =
        this.restTemplate
            .postForEntity(basePath + "search/" + (Integer.MAX_VALUE - 10) + "/datasets/1", null,
                Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }


  @Test
  public void testSearchNotTransformedError() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    ProjectDetail projectDetail = new ProjectDetail();
    BeanUtils.copyProperties(project,projectDetail);
    projectDetail.setVertical(Vertical.valueOf(project.getVertical()));
    ProjectBO addProject =
        this.projectManager.addProject(client.getId().toString(), projectDetail);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(client.getId(), addProject.getId(), name);
    DatasetBO addDataset =
        this.datasetManager.addDataset(addDatasetRequest, currentUserId);

    this.projectManager
        .addDatasetProjectMapping(client.getId().toString(), Integer.toString(addProject.getId()),
            Integer.toString(addDataset.getId()), currentUserId);
    ResponseEntity<Error> entity =
        this.restTemplate.postForEntity(
            basePath + "search/" + addProject.getId() + "/datasets/" + addDataset.getId(), null,
            Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, entity.getBody().getMessage());
  }

  @Test
  public void testStatsProjectNotFoundError() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String url = basePath + "search/clients/" + client.getId()+ "/projects/"+ (Integer.MAX_VALUE - 100) + "/datasets/1/stats";

    ResponseEntity<Error> entity =
        this.restTemplate.getForEntity(url, Error.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals("project_not_found", entity.getBody().getErrorCode());
  }

//    @Test
//    public void testStatsDatasetNotAssociatedError(){
//        this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        ResponseEntity<Error> entity =
//                this.restTemplate.getForEntity(basePath + "search/1/datasets/1/stats", Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
//        assertEquals(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED, entity.getBody().getMessage());
//    }

  @Test
  public void testStatsNotTransformedError() {

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    ProjectDetail projectDetail = new ProjectDetail();
    BeanUtils.copyProperties(project,projectDetail);
    projectDetail.setVertical(Vertical.valueOf(project.getVertical()));
    ProjectBO addProject =
        this.projectManager.addProject(client.getId().toString(), projectDetail);

    AddDatasetRequest addDatasetRequest = ModelUtils
        .getTestAddDatasetRequestObject(client.getId(), addProject.getId(), name);
    DatasetBO addDataset =
        this.datasetManager.addDataset(addDatasetRequest, currentUserId);

    this.projectManager
        .addDatasetProjectMapping(client.getId().toString(), Integer.toString(addProject.getId()),
            Integer.toString(addDataset.getId()), currentUserId);

    ResponseEntity<Error> entity =
        this.restTemplate.getForEntity(
            basePath + "search/clients/" + client.getId() + " /projects/" + addProject.getId() + "/datasets/" + addDataset.getId()
                + "/stats", Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals(ErrorMessage.PROJECT_NOT_TRANSFORMED, entity.getBody().getMessage());
  }

}
