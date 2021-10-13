/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyList;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.testutil.CidGenerator;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.DatasetDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.util.CommonLib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

public class ElasticApiBaseTest {

  protected String basePath;

  protected ClientBO clientDetail;

  protected ProjectBO projectDetail;

  protected DatasetBO datasetDetail;

  protected String currentUserId = "0";
  @Autowired
  protected TestRestTemplate restTemplate;
  @MockBean
  @Qualifier("projectManagerBean")
  protected ProjectManager projectManager;
  @MockBean
  @Qualifier("validationManagerBean")
  protected ValidationManager validationManager;
  @MockBean
  @Qualifier("preferenceManagerBean")
  protected PreferenceManager preferenceManager;
  @MockBean
  @Qualifier("datasetManagerBean")
  protected DatasetManager datasetManager;
  @MockBean
  @Qualifier("projectDatasetManagerBean")
  protected ProjectDatasetManager projectDatasetManager;
  @Autowired
  CidGenerator cidGenerator;

  @Autowired
  public void setBasePath(AppConfig appConfig) {
    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws IOException {

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    Vertical.addValue("FINANCIAL");
    DataType.addValue("AIVA");

    this.clientDetail = new ClientBO();
    this.clientDetail.setId(ThreadLocalRandom.current().nextInt(20, 31));
    this.clientDetail.setCid(cidGenerator.generateTestCId(clientDetail, clientDetail.getId()));

    this.projectDetail = new ProjectBO();
    int projectId = ThreadLocalRandom.current().nextInt(100, 100001);
    this.projectDetail.setId(projectId);
    this.projectDetail.setClientId(this.clientDetail.getId());
    this.projectDetail.setOwnerId(currentUserId);
    this.projectDetail.setStartAt(100L);
    this.projectDetail.setLocale("en");

    this.datasetDetail = new DatasetBO();
    int datasetId = ThreadLocalRandom.current().nextInt(projectId, projectId + 50);
    this.datasetDetail.setId(datasetId);
    this.datasetDetail.setClientId(Integer.toString(this.clientDetail.getId()));

    Mockito.doReturn(java.util.Locale.getDefault()).when(this.datasetManager)
        .getDatasetLocale(anyString());
    Mockito.doReturn(java.util.Locale.getDefault()).when(this.projectManager)
        .getProjectLocale(anyString());
  }

  public void setProjectValid() {
    Mockito.doNothing().when(this.validationManager).validateDatasetIds(anyList());
    Mockito.doReturn(this.projectDetail).when(this.validationManager).validateProjectAndStart(anyString());
    Mockito.doReturn(this.projectDetail).when(this.validationManager)
        .validateProjectId(anyString());
  }

  public void setProjectInvalid(String projectId) {
    Mockito.doReturn(null).when(this.projectManager).getProjectById(anyString());
    Mockito.doReturn(null).when(this.projectManager)
        .getProjectById(anyString(), anyString(), anyBoolean());
    Error error = new Error();
    error.setCode(Response.Status.NOT_FOUND.getStatusCode());
    error.setErrorCode("project_not_found");
    error.setMessage("Project '" + projectId + "' not found");
    NotFoundException nfe = new NotFoundException(error);
    Mockito.doThrow(nfe).when(this.validationManager).validateProjectId(anyString());
    Mockito.doThrow(nfe).when(this.validationManager).validateProjectAndStart(anyString());
    Mockito.doThrow(nfe).when(this.validationManager).validateProjectDatasetEntry(anyString(), anyString(), anyBoolean());
  }

  public void setProjectDatasetAssociated() {
    this.setProjectValid();
    Mockito.doReturn(true).when(this.validationManager).validateProjectDatasetEntry(anyString(), anyString(), anyBoolean());
  }

  public void setProjectDatasetNotAssociated() {
    this.setProjectValid();
    Error error = new Error();
    error.setCode(Response.Status.NOT_FOUND.getStatusCode());
    error.setErrorCode(Constants.DATASET_NOT_FOUND);
    error.setMessage(ErrorMessage.PROJECT_DATASET_NOT_ASSOCIATED);
    NotFoundException nfe = new NotFoundException(error);
    Mockito.doThrow(nfe).when(this.validationManager).validateProjectDatasetEntry(anyString(), anyString(), anyBoolean());
  }

  public void setProjectDatasetTransformed() {
    this.setProjectDatasetAssociated();
    List<DatasetBO> datasetDetailList = new ArrayList<>();
    datasetDetailList.add(this.datasetDetail);
    Mockito.doReturn(datasetDetailList).when(this.validationManager).validateProjectTransformedStatus(anyString());
  }

  public void setProjectDatasetNotTransformed() {
    this.setProjectDatasetAssociated();
    Error error = new Error();
    error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
    error.setErrorCode("model_not_transformed");
    error.setMessage(ErrorMessage.PROJECT_NOT_TRANSFORMED);
    BadRequestException bre = new BadRequestException(
            Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    Mockito.doThrow(bre).when(this.validationManager).validateProjectTransformedStatus(anyString());
  }
}