/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.ProjectDatasetBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.DataManagementManager;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.SearchManagerDummyImpl;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import com.tfs.learningsystems.ui.model.TaskEvent;
import com.tfs.learningsystems.ui.model.TaskEvent.Status;
import com.tfs.learningsystems.ui.model.TaskEventDetail;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.util.CommonLib;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder
@Slf4j
public class DataManagementApiTest {

  private String basePath;

  private String currentUserId = "UnitTest@247.ai";
  @Autowired
  private TestRestTemplate restTemplate;
  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;
  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;
  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;
  @Autowired
  private DataManagementManager dataManagementManager;
  @Inject
  @Qualifier("fileManagerBean")
  private FileManager fileManager;
  @Autowired
  private AppConfig appConfig;

  @Autowired
  public void setBasePath(AppConfig appConfig) {

    this.basePath = appConfig.getRestUrlPrefix() + "v1/";
  }

  @Before
  public void setUp() throws ScriptException, SQLException {

    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    Vertical.addValue("FINANCIAL");
    Vertical.addValue("RETAIL");
    DataType.addValue("AIVA");
    this.dataManagementManager.setSearchManager(new SearchManagerDummyImpl());

  }

  @Test
  public void testTransform() throws Exception {

    String jobPath = null;
    String fileName = "";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    try {
      fileName = addFile.getSystemName();
      // test service
      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/")
          .append(addProject.getId()).append("/datasets/").append(addDataset.getId())
          .append("/transform");

      ResponseEntity<TaskEventDetail> startTrasformResponse = this.restTemplate
          .exchange(url.toString(), HttpMethod.PUT, null, TaskEventDetail.class);

      assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());

      try {
        Thread.sleep(appConfig.getTestCaseTimeout());
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish transform status test");
      }

      ResponseEntity<TaskEventDetail> response = this.restTemplate
          .getForEntity(startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.RUNNING, response.getBody().getStatus());
      //            assertEquals(Task.Name.categorize, response.getBody().getTask());
      //            StringBuilder pathBuilder = new StringBuilder();
      //            pathBuilder.append("jobs/")
      //            .append("client")
      //            .append(addProject.getClientId())
      //            .append("/project")
      //            .append(addProject.getId())
      //            .append("/job")
      //            .append(response.getBody().getJobId());
      //
      //            jobPath = pathBuilder.toString();
      //            try {
      //                Thread.sleep(2000);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            response = this.restTemplate.getForEntity(
      //                    startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.RUNNING, response.getBody().getStatus());
      //            assertEquals(Task.Name.index, response.getBody().getTask());
      //
      //            try {
      //                Thread.sleep(2000);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            response = this.restTemplate.getForEntity(
      //                    startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.COMPLETED, response.getBody().getStatus());

    } finally {
      log.info("cleaning up test files");
      this.cleanTempFiles(jobPath, fileName);
    }
  }

  @Test
  public void testCancelTransformCategorize() throws Exception {

    String jobPath = null;
    String fileName = "";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    try {
      fileName = addFile.getSystemName();
      // test service
      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/")
          .append(addProject.getId()).append("/datasets/").append(addDataset.getId())
          .append("/transform");

      ResponseEntity<TaskEventDetail> startTrasformResponse = this.restTemplate
          .exchange(url.toString(), HttpMethod.PUT, null, TaskEventDetail.class);

      assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());

      try {
        Thread.sleep(appConfig.getTestCaseTimeout());
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish transform status test");
      }

      ResponseEntity<TaskEventDetail> cancelTrasformResponse = this.restTemplate
          .exchange(url.toString() + "/cancel", HttpMethod.PUT, null,
              TaskEventDetail.class);

      //
      // depends on the execution speed, the task might not be running while 'cancel' it.
      // I am not sure the flow here is deterministic
      //
      //            assertEquals(HttpStatus.OK, cancelTrasformResponse.getStatusCode());
      //
      //            try {
      //                Thread.sleep(1500);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            ResponseEntity<TaskEventDetail> response = this.restTemplate.getForEntity(
      //                    startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.CANCELLED, response.getBody().getStatus());
      //            assertEquals(Task.Name.categorize, response.getBody().getTask());
      //            StringBuilder pathBuilder = new StringBuilder();
      //            pathBuilder.append("jobs/")
      //            .append("client")
      //            .append(addProject.getClientId())
      //            .append("/project")
      //            .append(addProject.getId())
      //            .append("/job")
      //            .append(response.getBody().getJobId());
      //
      //            jobPath = pathBuilder.toString();

    } finally {
      log.info("cleaning up test files");
      this.cleanTempFiles(jobPath, fileName);
    }
  }

  @Test
  public void testCancelTransformIndex() throws Exception {

    String jobPath = null;
    String fileName = "";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    try {
      fileName = addFile.getSystemName();
      // test service
      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/")
          .append(addProject.getId()).append("/datasets/").append(addDataset.getId())
          .append("/transform");

      ResponseEntity<TaskEventDetail> startTrasformResponse = this.restTemplate
          .exchange(url.toString(), HttpMethod.PUT, null, TaskEventDetail.class);

      assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());

      try {
        Thread.sleep(2500);
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish transform status test");
      }

      ResponseEntity<TaskEventDetail> cancelTrasformResponse = this.restTemplate
          .exchange(url.toString() + "/cancel", HttpMethod.PUT, null,
              TaskEventDetail.class);
      //
      // depends on the execution speed, the task might not be running while 'cancel' it.
      // I am not sure the flow here is deterministic
      //

      //            assertEquals(HttpStatus.OK, cancelTrasformResponse.getStatusCode());
      //
      //            try {
      //                Thread.sleep(2000);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            ResponseEntity<TaskEventDetail> response = this.restTemplate.getForEntity(
      //                    startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.CANCELLED, response.getBody().getStatus());
      //            assertEquals(Task.Name.index, response.getBody().getTask());
      //            StringBuilder pathBuilder = new StringBuilder();
      //            pathBuilder.append("jobs/")
      //            .append("client")
      //            .append(addProject.getClientId())
      //            .append("/project")
      //            .append(addProject.getId())
      //            .append("/job")
      //            .append(response.getBody().getJobId());
      //
      //            jobPath = pathBuilder.toString();
      //
      //
    } finally {
      log.info("cleaning up test files");
      this.cleanTempFiles(jobPath, fileName);
    }
  }

  @Test
  public void testTransformRetry() throws Exception {

    String jobPath = null;
    String fileName = "";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    try {
      fileName = addFile.getSystemName();
      // test service
      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/")
          .append(addProject.getId()).append("/datasets/").append(addDataset.getId())
          .append("/transform");

      log.info("Send a transform request and wait until complete");
      ResponseEntity<TaskEventDetail> startTrasformResponse = this.restTemplate
          .exchange(url.toString(), HttpMethod.PUT, null, TaskEventDetail.class);

      assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());

      StringBuilder pathBuilder = new StringBuilder();
      pathBuilder.append("jobs/").append("client").append(addProject.getClientId())
          .append("/project").append(addProject.getId())
          .append("/job").append(startTrasformResponse.getBody().getJobId());

      jobPath = pathBuilder.toString();
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish transform status test");
      }
      //TODO - need to update the DataManagement API ...transform/status

      ResponseEntity<TaskEventDetail> response = this.restTemplate
          .getForEntity(startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());

      //    assert(TaskEvent.Status.RUNNING, response.getBody().getStatus());

      assertTrue(
          response.getBody().getStatus().equals(Status.RUNNING) || response.getBody().getStatus()
              .equals(Status.COMPLETED));
      // assertEquals(TaskEvent.Status.RUNNING, response.getBody().getStatus());

      //            log.info("reset job to fail on first task");
      //            TaskEventDetail task = response.getBody();
      //            List<TaskEventDetail> tasksForJob = jobManager.getTasksForJob(task.getJobId());
      //
      //            // set task 2 to canceled
      //            TaskEventDetail task2 = tasksForJob.get(1);
      //            task2.modifiedAt(Calendar.getInstance().getTimeInMillis()).status(TaskEvent.Status.CANCELLED).message("Unable to schedule job");
      //            jobManager.updateTaskEvent(task2);
      //            // set task 1 to failed
      //            TaskEventDetail task1 = tasksForJob.get(0);
      //            task1.modifiedAt(Calendar.getInstance().getTimeInMillis()).status(TaskEvent.Status.FAILED).message("Unable to schedule job");
      //            jobManager.updateTaskEvent(task1);
      //
      //            log.info("send a retry request for the job that now has a failed task");
      //            String retryUrl = url.append("/retry").toString();
      //            ResponseEntity<TaskEventDetail> retryTrasformResponse = this.restTemplate
      //                    .exchange(retryUrl, HttpMethod.PUT, null, TaskEventDetail.class);
      //            assertEquals(HttpStatus.ACCEPTED, retryTrasformResponse.getStatusCode());
      //
      //            try {
      //                Thread.sleep(5000);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            response = this.restTemplate.getForEntity(
      //                    retryTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.COMPLETED, response.getBody().getStatus());
      //
      //            log.info("send a retry request for the job that now has a failed second task");
      //            // set task 2 to FAILED
      //            task2.modifiedAt(Calendar.getInstance().getTimeInMillis()).status(TaskEvent.Status.FAILED).message("Unable to schedule job");
      //            jobManager.updateTaskEvent(task2);
      //
      //            retryTrasformResponse = this.restTemplate
      //                    .exchange(retryUrl, HttpMethod.PUT, null, TaskEventDetail.class);
      //            assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());
      //
      //            try {
      //                Thread.sleep(5000);
      //            } catch (InterruptedException e) {
      //                log.error("Got interrupted while waiting to finish transform status test");
      //            }
      //
      //            response = this.restTemplate.getForEntity(
      //                    retryTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      //            assertEquals(HttpStatus.OK, response.getStatusCode());
      //            assertEquals(TaskEvent.Status.COMPLETED, response.getBody().getStatus());

    } finally {
      log.info("cleaning up test files");
      this.cleanTempFiles(jobPath, fileName);
    }

  }

  @Test
  public void testDeleteFailedTransformation() throws Exception {

    String jobPath = null;
    String fileName = "";
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    try {
      fileName = addFile.getSystemName();
      // test service
      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/")
          .append(addProject.getId()).append("/datasets/").append(addDataset.getId())
          .append("/transform");

      log.info("Send a transform request and wait until complete");
      ResponseEntity<TaskEventDetail> startTrasformResponse = this.restTemplate
          .exchange(url.toString(), HttpMethod.PUT, null, TaskEventDetail.class);

      assertEquals(HttpStatus.ACCEPTED, startTrasformResponse.getStatusCode());

      StringBuilder pathBuilder = new StringBuilder();
      pathBuilder.append("jobs/").append("client").append(addProject.getClientId())
          .append("/project").append(addProject.getId())
          .append("/job").append(startTrasformResponse.getBody().getJobId());

      jobPath = pathBuilder.toString();

      Thread.sleep(appConfig.getTestCaseLongerTimeout());

      ResponseEntity<TaskEventDetail> response = this.restTemplate
          .getForEntity(startTrasformResponse.getHeaders().getLocation(), TaskEventDetail.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      // assertEquals(TaskEvent.Status.QUEUED , response.getBody().getStatus());

      assertTrue(
          response.getBody().getStatus().equals(TaskEvent.Status.QUEUED) || response.getBody()
              .getStatus().equals(Status.COMPLETED));

      log.info("reset job to fail on first task");
      TaskEventDetail task = response.getBody();
      List<TaskEventBO> tasksForJob = jobManager.getTasksForJob(task.getJobId());

      // set task 2 to canceled
      TaskEventBO task2 = tasksForJob.get(1);
      task2.setModifiedAt(Calendar.getInstance().getTimeInMillis());
      task2.setStatus(TaskEventBO.Status.CANCELLED);
      task2.setMessage("Unable to schedule job");
      jobManager.updateTaskEvent(task2);
      // set task 1 to failed
      TaskEventBO task1 = tasksForJob.get(0);
      task1.setModifiedAt(Calendar.getInstance().getTimeInMillis());
      task1.setStatus(TaskEventBO.Status.FAILED);
      task1.setMessage("Unable to schedule job");
      jobManager.updateTaskEvent(task1);

      log.info("send a delete request for the job that now has a failed task");
      String retryUrl = url.toString();
      ResponseEntity<Void> retryTrasformResponse = this.restTemplate
          .exchange(retryUrl, HttpMethod.DELETE, null, Void.class);
      assertEquals(HttpStatus.NO_CONTENT, retryTrasformResponse.getStatusCode());

    } catch (InterruptedException e) {
      log.error("Got interrupted while waiting to finish transform status test");
    } finally {
      log.info("cleaning up test files");
      this.cleanTempFiles(jobPath, fileName);
    }

  }

   /*
   @Test
   public void testTransformDatasetStatusForDatasetIds () throws Exception {

      List<Integer> datasetIds = new ArrayList<>();
      String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
      String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
      ClientBO client = ModelUtils.getTestClientObject(name);
      client.create();

      ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
      addProject.create();

      String file = "test-input.csv";
      DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
      FileBO addFile = fileManager.addFile(new ClassPathResource(file).getInputStream(), currentUserId, addDataset.getDataType());
      String urlStr = basePath + "files/" + addFile.getFileId();
      addDataset.setUri(urlStr);
      addDataset.create();

      ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
      projectDatasetBO.setDatasetId(addDataset.getId());
      projectDatasetBO.setProjectId(addProject.getId());
      projectDatasetBO.setCid(client.getCid());
      projectDatasetBO.create();

      //        JobDetail jobDetail = ModelUtils.getTestJobDetailObject();
      JobBO jobDetail = new JobBO();
      jobDetail.setProjectId(addProject.getId());
      jobDetail.setDatasetId(addDataset.getId());
      jobDetail.create();

      TaskEventBO task = new TaskEventBO();
      task.setJobId(jobDetail.getId());
      task.setStatus(TaskEventBO.Status.COMPLETED);
      task.setTask(TaskEventBO.TaskType.CATEGORIZE);
      task.create();

      StringBuilder url = new StringBuilder();
      url.append(basePath).append("clients/").append(client.getId()).append("/projects/datasets/transform/status");

      datasetIds.add(addDataset.getId());
      ResponseEntity<DatasetTaskStatusResponse> getTransformStatus = this.restTemplate.postForEntity(url.toString(), datasetIds,
      DatasetTaskStatusResponse.class);
      assertEquals(HttpStatus.OK, getTransformStatus.getStatusCode());
   }*/

  @Test
  public void testTransformDatasetStatusForProjects() throws Exception {

    List<Integer> datasetIds = new ArrayList<>();
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    String file = "test-input.csv";
    DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
    FileBO addFile = fileManager
        .addFile(new ClassPathResource(file).getInputStream(), currentUserId,
            addDataset.getDataType());
    String urlStr = basePath + "files/" + addFile.getFileId();
    addDataset.setUri(urlStr);
    addDataset.create();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setDatasetId(addDataset.getId());
    projectDatasetBO.setProjectId(addProject.getId());
    projectDatasetBO.setCid(client.getCid());
    projectDatasetBO.create();

    JobBO job = new JobBO();
    job.setDatasetId(addDataset.getId());
    job.setProjectId(addProject.getId());
    job.create();

    TaskEventBO task = new TaskEventBO();
    task.setJobId(job.getId());
    task.setStatus(TaskEventBO.Status.COMPLETED);
    task.setTask(TaskEventBO.TaskType.CATEGORIZE);
    task.create();

    StringBuilder url = new StringBuilder();
    url.append(basePath).append("clients/").append(client.getId())
        .append("/projects/transform/status");

    datasetIds.add(addDataset.getId());
    Map<Integer, List<Integer>> projects = new HashMap<>();
    projects.put(addProject.getId(), datasetIds);
    ResponseEntity<ProjectTaskStatusResponse> getTransformStatus = this.restTemplate
        .postForEntity(url.toString(), projects,
            ProjectTaskStatusResponse.class);
    assertEquals(HttpStatus.OK, getTransformStatus.getStatusCode());
    assertFalse(getTransformStatus.getBody().isEmpty());
  }

  private void cleanTempFiles(String jobId, String fileName) {

    try {
      String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
      FileSystem fileSystem = FileSystems.getDefault();
      if (jobId != null) {
        Path jobRoot = fileSystem.getPath(repositoryRoot, jobId);
        Files.deleteIfExists(jobRoot.resolve(fileName));
      }
      if (currentUserId != null) {
        Path userRoot = fileSystem.getPath(repositoryRoot, currentUserId);
        Files.deleteIfExists(userRoot.resolve(fileName));
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

}
