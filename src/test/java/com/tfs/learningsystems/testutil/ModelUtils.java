/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.testutil;

import com.ibm.icu.util.Calendar;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.ui.model.*;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class ModelUtils {

  public static ClientBO getTestClientObject(String name) {
    return (getTestClientBO(name, false));
  }

  public static ClientBO getTestVerticalClientObject(String name) {
    return (getTestClientBO(name, true));
  }

  private static ClientBO getTestClientBO(String name, boolean isVertical) {
    ClientBO client = new ClientBO();
    client.setName(name);
    client.setDescription("Company for Unit Test");
    client.setAddress("123 Fake St. San Francisco, CA, 94110, USA");
    client.setIsVertical(isVertical);
    return client;
  }

  public static MwbItsClientMapBO getTestMwbItsClientMapBO(Integer id, String itsClientId, String itsAppId) {
    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();
    mwbItsClientMapBO.setId(id);
    mwbItsClientMapBO.setItsAppId(itsAppId);
    mwbItsClientMapBO.setItsClientId(itsClientId);
    mwbItsClientMapBO.setItsAccountId(itsClientId + "_" + itsAppId);
    mwbItsClientMapBO.setDescription(itsClientId + "_" + itsAppId);
    return mwbItsClientMapBO;
  }

  public static Client getTestClient(String name , String itsClientId, String itsAppId, String itsAccountId, boolean isVertical) {
    Client client = new Client();
    client.setName(name);
    client.setDescription("Company for Unit Test");
    client.setAddress("123 Fake St. San Francisco, CA, 94110, USA");
    client.setIsVertical(isVertical);
    client.setItsClientId(itsClientId);
    client.setItsAccountId(itsAccountId);
    client.setItsAppId(itsAppId);
    return client;
  }
  //
  // should be depreciate after we changing all callers
  //
  @Deprecated
  public static Project getTestProjectObject() {
    Project project = new Project();
    project.setName("Unit Test");
    project.setDescription("Project for Unit Test");
    project.setClientId("1");
    project.setOwnerId("1");
    project.setVertical(Vertical.valueOf("FINANCIAL"));
    project.setState(Project.StateEnum.ENABLED);
    project.setLocale(Locale.EN_US);
    return project;
  }

  public static ProjectBO getTestProjectObject(int clientId, String userId, String projectName) {
    ProjectBO project = new ProjectBO();
    project.setName(projectName);
    project.setDescription("Project for Unit Test");
    project.setClientId(clientId);
    project.setOwnerId(userId);
    project.setVertical(Vertical.valueOf("FINANCIAL").toString());
    project.setState(ProjectBO.State.ENABLED);
    project.setLocale(Locale.EN_US);
    return project;
  }

  public static MetricsBO getTestMetricsObject(int clientId, String modelName, String nodeName, Timestamp metricDate, int volume, int escalation, int version) {
    MetricsBO metrics = new MetricsBO();
    metrics.setClientId(clientId);
    metrics.setModelName(modelName);
    metrics.setNodeName(nodeName);
    metrics.setMetricDate(metricDate);
    metrics.setVolume(volume);
    metrics.setVersion(version);
    metrics.setEscalation(escalation);
    return metrics;
  }

  public static DatasetBO getTestDatasetObject(int clientId,
      int projectId, String name) { //TODO Client_Isolation remove
    DatasetBO dataset = new DatasetBO();
    dataset.setName(name);
    dataset.setClientId(Integer.toString(clientId));
    dataset.setProjectId(Integer.toString(projectId));
    dataset.setDataType(DataType.valueOf("AIVA").getName());
    dataset.setDescription("Data set for unit test");
    dataset.setUri("https://localhost:8443/tmp/tempfile.csv");
    dataset.setLocale(Locale.EN_US);
    return dataset;
  }

  public static AddDatasetRequest getTestAddDatasetRequestObject(int clientId, int projectId, String name) {
    AddDatasetRequest addDatasetRequest = new AddDatasetRequest();
    addDatasetRequest.setDataset(getTestDatasetObject(clientId, projectId, name));
    return addDatasetRequest;
  }

  public static JobDetail getTestJobDetailObject() {
    JobDetail job = new JobDetail();
    job.setCreatedAt(Calendar.getInstance().getTimeInMillis());
    job.setProjectId(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE) + "");
    job.setDatasetId(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE) + "");
    job.setCreatedBy("0");
    return job;
  }

  public static TaskEventDetail getTestTaskEventDetailObject() {
    TaskEventDetail taskEventDetail = new TaskEventDetail();
    return taskEventDetail;
  }

  public static ClientBO setTestClient(String name, String cid) {
    ClientBO client = new ClientBO();
    client.setName(name);
    client.setCid(cid);
    client.setDescription("Client for Unit Test");
    client.setAddress("123 Fake St. San Francisco, CA, 94110, USA");
    client.setIsVertical(true);
    client.setState(ClientBO.State.ENABLED);
    return client;
  }

  public static ProjectBO setTestProject(int clientId, String userId, String projectName,
      String cid) {
    ProjectBO project = new ProjectBO();
    project.setName(projectName);
    project.setDescription("Project for Unit Test");
    project.setClientId(clientId);
    project.setOwnerId(userId);
    project.setVertical(Vertical.valueOf("FINANCIAL").toString());
    project.setState(ProjectBO.State.ENABLED);
    project.setLocale(Locale.EN_US);
    project.setCid(cid);
    return project;
  }

  public static DatasetBO setTestDataset(int clientId, int projectId, String name, String cid) {
    DatasetBO dataset = new DatasetBO();
    dataset.setName(name);
    dataset.setClientId(Integer.toString(clientId));
    dataset.setProjectId(Integer.toString(projectId));
    dataset.setDataType(DataType.valueOf("AIVA").getName());
    dataset.setDescription("Data set for unit test");
    dataset.setLocale(Locale.EN_US);
    dataset.setCid(cid);
    return dataset;
  }
  public static ModelBO getModelObject(int projectId) {
    ModelBO model = new ModelBO();
    model.setName("Test model");
    model.setDescription("Model for unit test");
    model.setProjectId(projectId);
    model.setConfigId(1);
    model.setDatasetIds(Collections.singletonList("some-dataset-id"));
    model.setVersion(1);
    model.setDigitalHostedUrl("");
    model.setCreatedAt(java.util.Calendar.getInstance().getTimeInMillis() - 1);
    model.setUpdatedAt(java.util.Calendar.getInstance().getTimeInMillis());
    return model;
  }

  public static PreferencesBO getPreference(int clientId, String attribute, String level, String type, int value) {
    PreferencesBO preference = new PreferencesBO();
    preference.setClient_id(clientId);
    preference.setAttribute(attribute);
    preference.setLevel(level);
    preference.setStatus(PreferencesBO.STATUS_ENABLED);
    preference.setType(type);
    preference.setValue(value);
    return preference;
  }

  public static VectorizerBO getVectorizer(String type) {
    VectorizerBO vectorizer = new VectorizerBO();
    vectorizer.setType(type);
    vectorizer.setIsLatest(VectorizerBO.IsLatest.ONE);
    return vectorizer;
  }

}
