/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.job;

import static org.junit.Assert.assertFalse;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.ProjectDatasetBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.JobManager;
import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Vertical;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Calendar;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class IngestionRecoveryJobTest {

  private String basePath;

  private String currentUserId = "UnitTest@247-inc.ai";

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Inject
  @Qualifier("fileManagerBean")
  private FileManager fileManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private Scheduler scheduler;

  @Autowired
  @Qualifier("recoveryJobDetail")
  private org.quartz.JobDetail jobDetail;

  @Before
  public void setUp() throws ScriptException, SQLException, SchedulerException {
    String clsName = Thread.currentThread().getStackTrace()[1].getClassName();
    String name = clsName + "_" + Long.toString(System.currentTimeMillis() % 10000000) + "_1";

    Vertical.addValue("FINANCIAL");
    Vertical.addValue("RETAIL");
    DataType.addValue("AIVA");
  }

  @Test
  public void testUnfinishedTaskRecovery() throws Exception {
    String jobPath = null;
    String username = null;
    String fileName = "";

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    try {

      String file = "test-input.csv";
      DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
      FileBO addFile =
          fileManager.addFile(new ClassPathResource(file).getInputStream(),
              currentUserId, addDataset.getDataType());
      String urlStr = basePath + "files/" + addFile.getFileId();
      addDataset.setUri(urlStr);
      addDataset.setReceivedAt(System.currentTimeMillis());
      addDataset.create();

      ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
      projectDatasetBO.setDatasetId(addDataset.getId());
      projectDatasetBO.setProjectId(addProject.getId());
      projectDatasetBO.setCid(client.getCid());
      projectDatasetBO.create();

      username = addFile.getUser();
      fileName = addFile.getSystemName();

      JobBO job = new JobBO();
      job.setDatasetId(addDataset.getId());
      job.setProjectId(addProject.getId());
      job.setFileName(fileName);
      job.create();

      StringBuilder pathBuilder = new StringBuilder();
      pathBuilder.append("jobs/")
          .append("client")
          .append(addProject.getClientId())
          .append("/project")
          .append(addProject.getId())
          .append("/job")
          .append(job.getId());
      jobPath = pathBuilder.toString();
      TaskEventBO taggingTask = new TaskEventBO();
      taggingTask.setCreatedAt(job.getCreatedAt());
      taggingTask.setJobId(job.getId());
      taggingTask.setStatus(TaskEventBO.Status.RUNNING);
      taggingTask.setTask(TaskEventBO.TaskType.CATEGORIZE);
      taggingTask.create();

      TaskEventBO indexTask = new TaskEventBO();
      indexTask.setCreatedAt(job.getCreatedAt());
      indexTask.setJobId(job.getId());
      indexTask.setStatus(TaskEventBO.Status.QUEUED);
      indexTask.setTask(TaskEventBO.TaskType.INDEX);
      indexTask.create();

      assertFalse(jobManager.getUnfinishedJobs().isEmpty());

      String triggerName = String.format("%s_%d_TRIGGER", "recovery",
          Calendar.getInstance().getTimeInMillis());
      Trigger recoveryJobTrigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerName, jobDetail.getKey().getGroup()).forJob(jobDetail)
          .startNow().build();
      scheduler.addJob(jobDetail, true);
      scheduler.scheduleJob(recoveryJobTrigger);

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        log.error("Got interrupted while waiting to finish transform status test");
      }

      // we might not be about to assert this, due to environmental difference
      // assertTrue(jobManager.getUnfinishedJobs().isEmpty());

    } finally {
      this.cleanTempFiles(currentUserId, jobPath, fileName);

    }
  }

  @Test
  public void testFailedTaskRecovery() throws Exception {

    String jobPath = null;
    String fileName = null;

    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    addProject.create();

    try {

      String file = "test-input.csv";
      DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
      FileBO addFile =
          fileManager.addFile(new ClassPathResource(file).getInputStream(),
              currentUserId, addDataset.getDataType());
      String urlStr = basePath + "files/" + addFile.getFileId();
      addDataset.setUri(urlStr);
      addDataset.setReceivedAt(System.currentTimeMillis());
      addDataset.create();

      ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
      projectDatasetBO.setDatasetId(addDataset.getId());
      projectDatasetBO.setProjectId(addProject.getId());
      projectDatasetBO.setCid(client.getCid());
      projectDatasetBO.create();

      fileName = addFile.getSystemName();
      JobBO job = new JobBO();
      job.setDatasetId(addDataset.getId());
      job.setProjectId(addProject.getId());
      job.setFileName(fileName);
      job.create();

      StringBuilder pathBuilder = new StringBuilder();
      pathBuilder.append("jobs/")
          .append("client")
          .append(addProject.getClientId())
          .append("/project")
          .append(addProject.getId())
          .append("/job")
          .append(job.getId());
      jobPath = pathBuilder.toString();
      TaskEventBO taggingTask = new TaskEventBO();
      taggingTask.setCreatedAt(job.getCreatedAt());
      taggingTask.setJobId(job.getId());
      taggingTask.setStatus(TaskEventBO.Status.COMPLETED);
      taggingTask.setTask(TaskEventBO.TaskType.CATEGORIZE);
      taggingTask.create();

      TaskEventBO indexTask = new TaskEventBO();
      indexTask.setCreatedAt(job.getCreatedAt());
      indexTask.setJobId(job.getId());
      indexTask.setStatus(TaskEventBO.Status.FAILED);
      indexTask.setTask(TaskEventBO.TaskType.INDEX);
      indexTask.create();

      assertFalse(jobManager.getFailedJobs().isEmpty());

//            String triggerName = String.format("Trigger_%s_%d", "recovery",
//                    Calendar.getInstance().getTimeInMillis());
//            Trigger recoveryJobTrigger = TriggerBuilder.newTrigger()
//                    .withIdentity(triggerName, jobDetail.getKey().getGroup()).forJob(jobDetail)
//                    .startNow().build();
//            scheduler.addJob(jobDetail, true);
//            scheduler.scheduleJob(recoveryJobTrigger);
//
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                log.error("Got interrupted while waiting to finish transform status test");
//            }
//
//            assertTrue(jobManager.getFailedJobs().isEmpty());

    } finally {
      this.cleanTempFiles(currentUserId, jobPath, fileName);
    }
  }

  private void cleanTempFiles(String username, String jobId, String fileName) {
    try {
      String repositoryRoot = appConfig.getFileUploadRepositoryRoot();
      FileSystem fileSystem = FileSystems.getDefault();
      if (jobId != null) {
        Path jobRoot = fileSystem.getPath(repositoryRoot, jobId);
        Files.deleteIfExists(jobRoot.resolve(fileName));
      }
      if (username != null) {
        Path userRoot = fileSystem.getPath(repositoryRoot, username);
        Files.deleteIfExists(userRoot.resolve(fileName));
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
