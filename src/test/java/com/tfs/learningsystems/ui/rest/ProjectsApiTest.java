/*******************************************************************************
 * Copyright © [24]7 Customer, One All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.testutil.ModelUtils;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.util.CommonLib;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ProjectsApiTest {

  private String basePath;

  private String currentUserId = "UnitTest@247.ai";

  private String modelTechnology = "n-gram";

  private String clientLevel = "client";

  @Autowired
  private TestRestTemplate restTemplate;
  @Inject
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;
  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;
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
    String name = clsName + "_" + System.currentTimeMillis() % 10000000;

    CommonLib.Auth.addAccessToken(this.restTemplate.getRestTemplate());

    Vertical.addValue("FINANCIAL");
    Vertical.addValue("RETAIL");
    DataType.addValue("AIVA");

  }

  @Test
  public void testGetProject() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.create();

    ResponseEntity<ProjectBO> entity =
        this.restTemplate
            .getForEntity(basePath + "clients/" + client.getId() + "/projects/" + project.getId(),
                ProjectBO.class);
    ProjectBO returnedProject = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(project.getId(), returnedProject.getId());
    assertEquals(name, returnedProject.getName());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    project.delete();
    client.delete();
  }

  @Test
  public void testAddProject() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    Project project = new Project();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
    testPreference.create();

    ResponseEntity<ProjectBO> entity = this.restTemplate
        .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
            ProjectBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    ProjectBO createdProject = entity.getBody();
    assertEquals(createdProject.getCreatedAt(), createdProject.getModifiedAt());
    assertTrue(timeStamp < createdProject.getCreatedAt());
    client.delete();
    testPreference.delete();
    testVectorizer.delete();
  }

  @Test
  public void testAddGlobalProject() {
    long timeStamp = Calendar.getInstance().getTimeInMillis() - 1;
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String projectName = GlobalModelName.ROOT_INTENT.toString();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, projectName);
    Project project = new Project();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));
    project.setType(Project.TypeEnum.GLOBAL);

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
    testPreference.create();

    ResponseEntity<ProjectBO> entity = this.restTemplate
            .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
                    ProjectBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    ProjectBO createdProject = entity.getBody();
    assertEquals(createdProject.getCreatedAt(), createdProject.getModifiedAt());
    assertTrue(timeStamp < createdProject.getCreatedAt());
    client.delete();
    testPreference.delete();
    testVectorizer.delete();
  }

  @Test
  public void testAddNodeProjectNotValidName() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String projectName = GlobalModelName.ROOT_INTENT.toString();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, projectName);
    Project project = new Project();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));

    ResponseEntity<Error> entity = this.restTemplate
            .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
                    Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    client.delete();
  }

  @Test
  public void testAddGlobalProjectNotValidName() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    Project project = new Project();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));
    project.setType(Project.TypeEnum.GLOBAL);

    ResponseEntity<Error> entity = this.restTemplate
            .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
                    Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    client.delete();
  }

  @Test
  public void testAddProjectTransactional() {

    // create a project which creates a group
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    Project project = new ProjectDetail();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
    testPreference.create();

    ResponseEntity<ProjectBO> entity = this.restTemplate
        .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
            ProjectBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    // try to create a new project with the same name, should get a conflict on project name,
    // but after creating new group.
    ResponseEntity<Error> errorEntity =
        this.restTemplate
            .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
                Error.class);
    assertEquals(HttpStatus.CONFLICT, errorEntity.getStatusCode());
    client.delete();
    testPreference.delete();
    testVectorizer.delete();
  }

  @Ignore
  @Test
  public void testAddProjectNotFoundOwnerId() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.setOwnerId("abc");
    ResponseEntity<Error> entity =
        this.restTemplate
            .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
                Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    client.delete();
  }

  @Test
  public void testAddProjectNotFoundClientId() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ProjectBO project = ModelUtils
        .getTestProjectObject((Integer.MAX_VALUE - 300), currentUserId, name);

    ResponseEntity<Error> entity =
        this.restTemplate
            .postForEntity(basePath + "clients/" + project.getClientId() + "/projects", project,
                Error.class);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
  }

  @Test
  public void testPromoteProjectWithGlobal() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO projectNode = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    projectNode.create();

    String projectName = GlobalModelName.ROOT_INTENT.toString();
    ProjectBO projectGlobal = ModelUtils.getTestProjectObject(client.getId(), currentUserId, projectName);
    projectGlobal.setType(ProjectBO.Type.GLOBAL);
    projectGlobal.create();

    ResponseEntity<ProjectsDetail> entity = this.restTemplate.exchange(
            basePath + "clients/" + client.getId() + "/projects/" + projectNode.getId() + "/promote?globalProjectId="
                    + projectGlobal.getId() + "&globalProjectName=" + projectGlobal.getName(),
            HttpMethod.PUT, null, ProjectsDetail.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    client.delete();
    projectNode.delete();
    projectGlobal.delete();
  }

  @Test
  public void testPromoteProjectWithoutGlobal() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String projectName = GlobalModelName.ROOT_INTENT.toString();
    ProjectBO projectNode = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    projectNode.create();

    ResponseEntity<ProjectsDetail> entity = this.restTemplate.exchange(
            basePath + "clients/" + client.getId() + "/projects/" + projectNode.getId() + "/promote?globalProjectName=" + projectName,
            HttpMethod.PUT, null, ProjectsDetail.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());
    ProjectsDetail promotedProject = entity.getBody();
    for (ProjectDetailDatasetTaskStatus project : promotedProject) {
      assertEquals(project.getType(), Project.TypeEnum.GLOBAL);
      assertEquals(project.getId(), projectNode.getId().toString());
      assertEquals(project.getName(), projectName);
      assertEquals(project.getOriginalName(), projectNode.getName());
    }
    client.delete();
    projectNode.delete();
  }

  @Test
  public void testDemoteProject() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + System.currentTimeMillis() % 10000000;

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    String projectName = GlobalModelName.ROOT_INTENT.toString();
    ProjectBO projectGlobal = ModelUtils.getTestProjectObject(client.getId(), currentUserId, projectName);
    projectGlobal.setType(ProjectBO.Type.GLOBAL);
    projectGlobal.setOriginalName(name);
    projectGlobal.create();

    ResponseEntity<ProjectBO> entity = this.restTemplate.exchange(
            basePath + "clients/" + client.getId() + "/projects/" + projectGlobal.getId() + "/demote",
            HttpMethod.PUT, null, ProjectBO.class);

    assertEquals(HttpStatus.OK, entity.getStatusCode());

    ProjectBO demotedProject = entity.getBody();
    assertEquals(demotedProject.getId(), projectGlobal.getId());
    assertEquals(demotedProject.getType(), ProjectBO.Type.NODE.toString());
    assertEquals(demotedProject.getName(), name);
    assertEquals(demotedProject.getOriginalName(), null);

    client.delete();
    projectGlobal.delete();
  }

//    @Test
//    public void testListDatasetsForProjectById() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//
//        this.projectManager.addDatasetProjectMapping(addDataset.getId(), Integer.toString(addProject.getId()),
//                currentUserId);
//
//        // test service
//        ResponseEntity<DatasetsDetail> response = this.restTemplate.getForEntity(
//                basePath + "projects/" + addProject.getId() + "/datasets", DatasetsDetail.class);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        DatasetsDetail datasets = response.getBody();
//        assertEquals(1, datasets.size());
//        assertEquals(addDataset.getName(), datasets.get(0).getName());
//    }

  //    @Test
//    public void testAddDatasetToProjectMapping() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//        // test service
//        ResponseEntity<Void> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/" + addProject.getId(),
//                HttpMethod.PUT, null, Void.class);
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//        ProjectBO modifiedProject = projectManager.getProjectById(Integer.toString(addProject.getId()));
//        assertTrue(modifiedProject.getCreatedAt() < modifiedProject.getModifiedAt());
//    }
//
//    @Test
//    public void testAddDatasetToProjectMappingDatasetNotFound() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        // test service
//        ResponseEntity<Error> response =
//                this.restTemplate.exchange(basePath + "projects/1/datasets/" + addProject.getId(),
//                        HttpMethod.PUT, null, Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        ProjectBO modifiedProject = projectManager.getProjectById(Integer.toString(addProject.getId()));
//        assertEquals(modifiedProject.getCreatedAt(), modifiedProject.getModifiedAt());
//    }
//
//    @Test
//    public void testAddDatasetToProjectMappingProjectNotFound() {
//
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//        // test service
//        ResponseEntity<Error> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/1", HttpMethod.PUT, null,
//                Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//    }
//
//    @Test
//    public void testAddDatasetToProjectMappingAlreadyExists() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//        ResponseEntity<Void> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/" + addProject.getId(),
//                HttpMethod.PUT, null, Void.class);
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//        ResponseEntity<Error> responseError = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/" + addProject.getId(),
//                HttpMethod.PUT, null, Error.class);
//        assertEquals(HttpStatus.CONFLICT, responseError.getStatusCode());
//    }
//
//    @Test
//    public void testAddDatasetToProjectMappingWAutoTag() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//        // test service
//        ResponseEntity<Void> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/" + addProject.getId() + "?autoTagDataset=true",
//                HttpMethod.PUT, null, Void.class);
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//        ProjectBO modifiedProject = projectManager.getProjectById(Integer.toString(addProject.getId()));
//        assertTrue(modifiedProject.getCreatedAt() < modifiedProject.getModifiedAt());
//        assertNotNull(datasetManager.getLastPendingInheritaceForDataset(addDataset.getId()));
//    }
//
//    @Test
//    public void testRemoveDatasetToProjectMapping() {
//
//        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
//        String name = methodName+ "_" + Long.toString(System.currentTimeMillis() % 10000000);
//        ClientBO client = ModelUtils.getTestClientObject(name);
//        client.create();
//
//        ProjectBO addProject = ModelUtils.getTestProjectObject(client.getId(), name);
//        addProject.create();
//
//        String file = "test-input.csv";
//        DatasetBO addDataset = ModelUtils.getTestDatasetObject(client.getId(), addProject.getId(), name);
//        String urlStr = basePath + "files/" + name;
//        addDataset.setUri(urlStr);
//        addDataset.create();
//
//        ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
//        projectDatasetBO.setDatasetId(addDataset.getId());
//        projectDatasetBO.setProjectId(addProject.getId());
//        projectDatasetBO.setCid(client.getCid());
//        projectDatasetBO.create();
//
//        ProjectBO modifiedProject1 = new ProjectBO();
//        modifiedProject1 = modifiedProject1.findOne(Integer.toString(addProject.getId()));
//
//        // test service
//        ResponseEntity<Void> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/" + addProject.getId(),
//                HttpMethod.DELETE, null, Void.class);
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//        ProjectBO modifiedProject2 = new ProjectBO();
//        modifiedProject2 = modifiedProject1.findOne(Integer.toString(addProject.getId()));
//        assertTrue(modifiedProject1.getModifiedAt() < modifiedProject2.getModifiedAt());
//
//    }
//
//    @Test
//    public void testRemoveDatasetToProjectMappingDatasetNotFound() {
//        ProjectBO addProject =
//                this.projectManager.addProject(ModelUtils.getTestProjectObject());
//
//        // test service
//        ResponseEntity<Error> response =
//                this.restTemplate.exchange(basePath + "projects/1/datasets/" + addProject.getId(),
//                        HttpMethod.DELETE, null, Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        ProjectBO projectDetail = projectManager.getProjectById(Integer.toString(addProject.getId()));
//        assertEquals(projectDetail.getCreatedAt(), projectDetail.getModifiedAt());
//    }
//
//    @Test
//    public void testRemoveDatasetToProjectMappingProjectNotFound() {
//
//        DatasetDetail addDataset =
//                this.datasetManager.addDataset(ModelUtils.getTestAddDatasetRequestObject(), currentUserId);
//        // test service
//        ResponseEntity<Error> response = this.restTemplate.exchange(
//                basePath + "projects/" + addDataset.getId() + "/datasets/1", HttpMethod.DELETE,
//                null, Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//    }
//
//    @Test
//    public void testListProjects() {
//        Project project = ModelUtils.getTestProjectObject();
//        projectManager.addProject(project);
//        ResponseEntity<ProjectsDetail> entity =
//                this.restTemplate.getForEntity(basePath + "projects", ProjectsDetail.class);
//        ProjectsDetail projects = entity.getBody();
//        assertEquals(projects.size(), 1);
//        assertEquals(HttpStatus.OK, entity.getStatusCode());
//    }
//
  @Test
  public void testListDeletedProject() {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);

    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();

    ProjectBO projectBO = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    Project project = new Project();
    BeanUtils.copyProperties(projectBO,project);
    project.setVertical(Vertical.valueOf(projectBO.getVertical()));
    project.setClientId(String.valueOf(projectBO.getClientId()));

    VectorizerBO testVectorizer = ModelUtils.getVectorizer(modelTechnology);
    testVectorizer.create();

    PreferencesBO testPreference = ModelUtils.getPreference(client.getId(), client.getId().toString(), clientLevel, Constants.VECTORIZER_TYPE, testVectorizer.getId());
    testPreference.create();

    ResponseEntity<ProjectBO> entity = this.restTemplate
        .postForEntity(basePath + "clients/" + client.getId() + "/projects", project,
            ProjectBO.class);
    assertEquals(HttpStatus.CREATED, entity.getStatusCode());

    projectBO = entity.getBody();

    this.restTemplate
        .delete(basePath + "clients/" + client.getId() + "/projects/" + projectBO.getId());

    ResponseEntity<ProjectsDetail> entities = this.restTemplate
        .getForEntity(basePath + "clients/" + client.getId() + "/projects?showDeleted=true",
            ProjectsDetail.class);
    ProjectsDetail projects = entities.getBody();
    assertTrue(projects.size() >= 1);
    assertEquals(HttpStatus.OK, entities.getStatusCode());
    client.delete();
    testVectorizer.delete();
    testPreference.delete();
  }

//    @Test
//    public void testListProjectByClient() {
//        Project project = ModelUtils.getTestProjectObject();
//        projectManager.addProject(project);
//        ResponseEntity<ProjectsDetail> entity = this.restTemplate
//                .getForEntity(basePath + "projects?clientId=1", ProjectsDetail.class);
//        ProjectsDetail projects = entity.getBody();
//        assertEquals(1, projects.size());
//        assertEquals(HttpStatus.OK, entity.getStatusCode());
//        ResponseEntity<ProjectsDetail> entity2 = this.restTemplate
//                .getForEntity(basePath + "projects?clientId=2", ProjectsDetail.class);
//        ProjectsDetail projects2 = entity2.getBody();
//        assertEquals(0, projects2.size());
//        assertEquals(HttpStatus.OK, entity2.getStatusCode());
//    }
//
//    @Test
//    public void testListDeletedProjectByClient() {
//        Project project = ModelUtils.getTestProjectObject();
//        projectManager.addProject(project);
//        ResponseEntity<ProjectsDetail> entity = this.restTemplate
//                .getForEntity(basePath + "projects?clientId=1", ProjectsDetail.class);
//        ProjectsDetail projects = entity.getBody();
//        assertEquals(projects.size(), 1);
//        assertEquals(HttpStatus.OK, entity.getStatusCode());
//        this.restTemplate.delete(basePath + "projects/1");
//        ResponseEntity<ProjectsDetail> entity2 = this.restTemplate
//                .getForEntity(basePath + "projects?clientId=1", ProjectsDetail.class);
//        ProjectsDetail projects2 = entity2.getBody();
//        assertEquals(projects2.size(), 0);
//        assertEquals(HttpStatus.OK, entity2.getStatusCode());
//        ResponseEntity<ProjectsDetail> entity3 = this.restTemplate.getForEntity(
//                basePath + "projects?clientId=1&showDeleted=true", ProjectsDetail.class);
//        ProjectsDetail projects3 = entity3.getBody();
//        assertEquals(projects3.size(), 1);
//        assertEquals(HttpStatus.OK, entity3.getStatusCode());
//
//
//    }
//
//    @Test
//    public void testDeleteProject() {
//        Project project = ModelUtils.getTestProjectObject();
//        ProjectBO projectDetail = projectManager.addProject(project);
//        this.restTemplate.delete(basePath + "projects/" + projectDetail.getId());
//        ResponseEntity<Error> entity = this.restTemplate
//                .getForEntity(basePath + "projects/" + projectDetail.getId(), Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
//        ProjectBO deletedProject = projectManager.getProjectById(Integer.toString(projectDetail.getId()), true);
//        assertTrue(projectDetail.getModifiedAt() < deletedProject.getModifiedAt());
//
//    }
//
//    @Test
//    public void testDeleteProjectTransactional() {
//        Project project = ModelUtils.getTestProjectObject();
//        ProjectBO projectDetail = projectManager.addProject(project);
//
//        // add a second project to associate group too
//        ProjectBO projectDetail2 =
//                projectManager.addProject(ModelUtils.getTestProjectObject().name("TestProject2"));
//
//        // update the project 2 to map to the group 2
//        PatchRequest patchRequest = new PatchRequest();
//        PatchDocument patchDocument = new PatchDocument();
//        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
//        patchDocument.setPath("/groupId");
//        patchDocument.setValue("1");
//        patchRequest.add(patchDocument);
//        projectManager.updateProject(Integer.toString(projectDetail2.getId()), patchRequest, "0");
//
//        // try to delete project 1, should fail on the second step of deleting group because it's
//        // associated to project 2
//        this.restTemplate.delete(basePath + "projects/" + projectDetail.getId());
//
//        // the delete should be rolled back so project 1 should still exist
//        ResponseEntity<Error> entity =
//                this.restTemplate.getForEntity(basePath + "projects/" + projectDetail.getId(), Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
//    }
//
//    @Test
//    public void testUndeleteProject() {
//        Project project = ModelUtils.getTestProjectObject();
//        ProjectBO addProject = projectManager.addProject(project);
//        this.restTemplate.delete(basePath + "projects/1");
//        ResponseEntity<Error> deletedEntity =
//                this.restTemplate.getForEntity(basePath + "projects/1", Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, deletedEntity.getStatusCode());
//
//        ProjectBO deletedProject = projectManager.getProjectById(Integer.toString(addProject.getId()), true);
//
//        ResponseEntity<ProjectDetail> revivedEntity = this.restTemplate
//                .postForEntity(basePath + "/projects/1/undelete", project, ProjectDetail.class);
//        assertEquals(HttpStatus.OK, revivedEntity.getStatusCode());
//
//        ProjectDetail undeletedProject = revivedEntity.getBody();
//        assertTrue(deletedProject.getModifiedAt() < undeletedProject.getModifiedAt());
//
//        // negative tests
//        ResponseEntity<Error> revivedErrorEntity = this.restTemplate
//                .postForEntity(basePath + "projects/2/undelete", project, Error.class);
//        assertEquals(HttpStatus.NOT_FOUND, revivedErrorEntity.getStatusCode());
//
//        // calling undelete on an ENABLED project should be idempotent operation
//        project.state(Project.StateEnum.ENABLED);
//        ResponseEntity<Error> undeleteErrorEntity = this.restTemplate
//                .postForEntity(basePath + "projects/1/undelete", project, Error.class);
//        assertEquals(HttpStatus.BAD_REQUEST, undeleteErrorEntity.getStatusCode());
//
//    }
//
//    @Test
//    public void testPatchProject() throws JsonProcessingException {
//        Project project = ModelUtils.getTestProjectObject();
//        ProjectBO projectDetail = projectManager.addProject(project);
//        assertNotNull(projectManager.getProjectById(Integer.toString(projectDetail.getId())));
//        PatchRequest patchRequest = new PatchRequest();
//        PatchDocument patchDocument = new PatchDocument();
//        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
//        patchDocument.setPath("/description");
//        patchDocument.setValue("Changed description");
//        patchRequest.add(patchDocument);
//
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonPatch = mapper.writeValueAsString(patchRequest);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf("application/json-patch+json"));
//        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);
//
//        String url = basePath + "projects/" + projectDetail.getId();
//        ResponseEntity<ProjectDetail> entity =
//                this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectDetail.class);
//        ProjectDetail patchedProject = entity.getBody();
//        assertThat(entity.getStatusCode(), is(equalTo(HttpStatus.OK)));
//        assertThat(patchedProject.getDescription(), is(equalTo("Changed description")));
//        assertEquals(projectDetail.getCreatedAt(), projectDetail.getModifiedAt());
//        assertTrue(patchedProject.getCreatedAt() < patchedProject.getModifiedAt());
//
//
//        // test patch on non-existing project
//        url = basePath + "projects/2";
//        ResponseEntity<Error> errorEntity =
//                this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, Error.class);
//        assertThat(errorEntity.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
//    }
//
//    @Test
//    public void testChangeProjectName() throws JsonProcessingException {
//        Project project = ModelUtils.getTestProjectObject();
//        ProjectBO projectDetail = projectManager.addProject(project);
//        assertNotNull(projectManager.getProjectById(Integer.toString(projectDetail.getId())));
//        PatchRequest patchRequest = new PatchRequest();
//        PatchDocument patchDocument = new PatchDocument();
//        patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
//        patchDocument.setPath("/name");
//        patchDocument.setValue(projectDetail.getName()+"_changed");
//        patchRequest.add(patchDocument);
//
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonPatch = mapper.writeValueAsString(patchRequest);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf("application/json-patch+json"));
//        HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);
//
//        String url = basePath + "projects/" + projectDetail.getId();
//        ResponseEntity<ProjectDetail> entity =
//                this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectDetail.class);
//        ProjectDetail patchedProject = entity.getBody();
//        assertThat(entity.getStatusCode(), is(equalTo(HttpStatus.OK)));
//        assertThat(patchedProject.getName(), is(equalTo(projectDetail.getName()+"_changed")));
//        assertEquals(projectDetail.getCreatedAt(), projectDetail.getModifiedAt());
//        assertTrue(patchedProject.getCreatedAt() < patchedProject.getModifiedAt());
//
//
//        // test to make sure you can still create a project with old name
//        try {
//            projectManager.addProject(project);
//        } catch (Exception e) {
//            fail(e.getMessage());
//
//        }
//    }
//
//    @Test
//    public void testAddUnicodeProject() {
//        Project project = ModelUtils.getTestProjectObject();
//        project.setName("hükan");
//        project.setDescription("hükan's group");
//        ResponseEntity<ProjectDetail> entity = this.restTemplate
//                .postForEntity(basePath + "projects", project, ProjectDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//        ProjectDetail createdProject = entity.getBody();
//        assertNotNull(createdProject);
//        assertEquals(createdProject.getName(), project.getName());
//        assertEquals(createdProject.getDescription(), project.getDescription());
//    }
//
//    @Ignore
//    @Test
//    public void testDuplicateProject() {
//
//        Project project1 = ModelUtils.getTestProjectObject();
//        project1.setName("project");
//        project1.setDescription("bad project");
//        ResponseEntity<ProjectDetail> entity = this.restTemplate
//                .postForEntity(basePath + "projects", project1, ProjectDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//
//        Project project2 = ModelUtils.getTestProjectObject();
//        project2.setName("project");
//        project2.setDescription("bad project");
//        ResponseEntity<Error> errorEntity =
//                this.restTemplate.postForEntity(basePath + "projects", project2, Error.class);
//        assertEquals(HttpStatus.CONFLICT, errorEntity.getStatusCode());
//
//        Project project3 = ModelUtils.getTestProjectObject();
//        project3.setName("project");
//        project3.setDescription("bad project");
//        project3.setClientId("2");
//        entity =
//                this.restTemplate.postForEntity(basePath + "projects", project3, ProjectDetail.class);
//        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
//    }
//
//    @Test
//    public void testRequiredProjectFields() {
//        Project project = new Project();
//        project.setDescription("bad project");
//
//        ResponseEntity<Error> entity =
//                this.restTemplate.postForEntity(basePath + "projects", project, Error.class);
//        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
//    }
//
//
//
//    @Test
//    public void testGetProjectErrorConditions() {
//        ResponseEntity<Error> entity =
//                this.restTemplate.getForEntity(basePath + "projects/1", Error.class);
//        assertNotNull(entity);
//        assertThat(entity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));
//
//        ResponseEntity<Error> deletedEntity =
//                this.restTemplate.getForEntity(basePath + "projects/1", Error.class);
//        assertThat(deletedEntity.getStatusCode(), (equalTo(HttpStatus.NOT_FOUND)));
//    }
//
////    @Test
////    public void testPaging() {
////        String offset = "0";
////        String totalCount = "2";
////        Project project1 =
////                ModelUtils.getTestProjectObject().name("Project One").description("bad project");
////        ResponseEntity<ProjectDetail> entity = this.restTemplate
////                .postForEntity(basePath + "projects", project1, ProjectDetail.class);
////        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
////
////        Project project2 =
////                ModelUtils.getTestProjectObject().name("Project Group").description("good project");
////        entity = this.restTemplate.postForEntity(basePath + "projects", project2,
////                ProjectDetail.class);
////        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
////
////        ResponseEntity<ProjectsDetail> readEntity = this.restTemplate
////                .getForEntity(basePath + "projects?limit=1&startIndex=0", ProjectsDetail.class);
////        ProjectsDetail projects = readEntity.getBody();
////        assertEquals(projects.size(), 1);
////        assertEquals(HttpStatus.OK, readEntity.getStatusCode());
////        assertEquals(projects.get(0).getName(), project1.getName());
////        assertEquals(offset, readEntity.getHeaders().getFirst("X-Offset"));
////        assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));
////
////
////        offset = "1";
////
////        readEntity = this.restTemplate.getForEntity(readEntity.getHeaders().getLocation(),
////                ProjectsDetail.class);
////        projects = readEntity.getBody();
////        assertEquals(projects.size(), 1);
////        assertEquals(HttpStatus.OK, readEntity.getStatusCode());
////        assertEquals(projects.get(0).getName(), project2.getName());
////        assertEquals(offset, readEntity.getHeaders().getFirst("X-Offset"));
////        assertEquals(totalCount, readEntity.getHeaders().getFirst("X-Total-Count"));
////
////        // Handle error case
////        ResponseEntity<Error> read = this.restTemplate
////                .getForEntity(basePath + "projects?limit=10&startIndex=10", Error.class);
////        assertEquals(HttpStatus.BAD_REQUEST, read.getStatusCode());
////    }
//
//
//
//    @Test
//    public void testProjectEquivalence() {
//        Project project1 = new Project();
//        project1.name("Project One").description("Big Project").clientId("1").groupId("1")
//        .ownerId("1").vertical(Vertical.valueOf("FINANCIAL"));
//
//        Project project2 = new Project();
//        project2.name("Project One").description("Big Project").clientId("1").groupId("1")
//        .ownerId("1").vertical(Vertical.valueOf("FINANCIAL"));
//
//        assert project1.equals(project2);
//
//        project2.vertical(Vertical.valueOf("RETAIL"));
//        assert !project1.equals(project2);
//    }
//
//    @Test
//    public void testProjectDetailEquivalence() {
//        ProjectDetail project1 = new ProjectDetail();
//        project1.id("1").createdAt(0L).modifiedAt(0L).name("Project One").description("Big Project")
//        .clientId("1").groupId("1").ownerId("1").vertical(Vertical.valueOf("FINANCIAL"));
//
//        ProjectDetail project2 = new ProjectDetail();
//        project2.id("1").createdAt(0L).modifiedAt(0L).name("Project One").description("Big Project")
//        .clientId("1").groupId("1").ownerId("1").vertical(Vertical.valueOf("FINANCIAL"));
//
//        assert project1.equals(project2);
//
//        project2.vertical(Vertical.valueOf("RETAIL"));
//
//        assert !project1.equals(project2);
//        assert project1.hashCode() != project2.hashCode();
//
//        assert !project1.equals(null);
//
//        assert project1.equals(project1);
//    }
//
//    @Test
//    public void testPrettyPrint() {
//        Project project = ModelUtils.getTestProjectObject();
//        projectManager.addProject(project);
//        String stringResponse =
//                this.restTemplate.getForObject(basePath + "projects/1", String.class);
//        String prettyStringResponse =
//                this.restTemplate.getForObject(basePath + "projects/1?pretty", String.class);
//        assertNotEquals(stringResponse, prettyStringResponse);
//        assertThat(prettyStringResponse, containsString("\n"));
//
//    }
//
//    @Test
//    public void testToJsonString() throws Exception {
//        Project project = ModelUtils.getTestProjectObject();
//        String projectJsonString = project.toJsonString();
//        ObjectMapper mapper = new ObjectMapper();
//        Project readValue = mapper.readValue(projectJsonString, Project.class);
//        assertEquals(readValue, project);
//
//        ProjectDetail projectDetail = new ProjectDetail();
//        BeanUtils.copyProperties(projectDetail, project);
//        String projectDetailJsonString = projectDetail.toJsonString();
//        ProjectDetail pdReadValue = mapper.readValue(projectDetailJsonString, ProjectDetail.class);
//        assertEquals(pdReadValue, projectDetail);
//
//    }

  @Test
  public void testPatchProject() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();
    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.create();
    ModelBO modelObject = ModelUtils.getModelObject(project.getId());
    modelObject.create();
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/deployableModelId");
    patchDocument.setValue(modelObject.getId());
    patchRequest.add(patchDocument);
    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<String>(jsonPatch, headers);
    String url = basePath + "clients/" + client.getId()+"/projects/"+project.getId();
    ResponseEntity<ProjectBO> entity =
              this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectBO.class);
      ProjectBO patchedProject = entity.getBody();
      assertEquals(HttpStatus.OK, entity.getStatusCode());
      assertEquals(patchedProject.getDeployableModelId(),modelObject.getId());
      assertTrue(patchedProject.getCreatedAt() < patchedProject.getModifiedAt());

      client.delete();
      project.delete();
      modelObject.delete();
  }

  @Test
  public void testPatchProjectForDeployableModel() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();
    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.create();
    ModelBO modelObject = ModelUtils.getModelObject(project.getId());
    modelObject.create();
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/deployableModelId");
    patchDocument.setValue(modelObject.getId());
    patchRequest.add(patchDocument);

    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (jsonPatch, headers);
    String url = basePath + "clients/" + client.getId() + "/projects/" + project.getId();
    ResponseEntity < ProjectBO > entity =
            this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectBO.class);
    ProjectBO patchedProject = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    //prevent deletion of deployable marked model code has been tested below in this testcase only as the delete part has to be executed after marking the model deployable
    String deleteURL = basePath + "clients/" + client.getId() + "/projects/" + project.getId() + "/models/" + patchedProject.getDeployableModelId();
    ResponseEntity < Void > responseEntity =
            this.restTemplate.exchange(deleteURL, HttpMethod.DELETE, null, Void.class);
    assertNotEquals(HttpStatus.OK, responseEntity.getStatusCode());
    client.delete();
    modelObject.delete();
    project.delete();
  }

  @Test
  public void testPatchProjectForLiveModel() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();
    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.create();
    ModelBO modelObject = ModelUtils.getModelObject(project.getId());
    modelObject.create();
    modelObject.setModelId(modelObject.getId().toString());
    modelObject.update();
    ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
    modelJobQueueBO.setModelId(modelObject.getId().toString());
    modelJobQueueBO.setToken(modelObject.getId().toString());
    modelJobQueueBO.setStatus(ModelJobQueueBO.Status.COMPLETED);
    modelJobQueueBO.setStartedAt(System.currentTimeMillis());
    modelJobQueueBO.setModelType(modelObject.getModelType());
    modelJobQueueBO.create();
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/liveModelId");
    patchDocument.setValue(modelObject.getId());
    patchRequest.add(patchDocument);
    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (jsonPatch, headers);
    String url = basePath + "clients/" + client.getId() + "/projects/" + project.getId();
    ResponseEntity < ProjectBO > entity =
            this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectBO.class);
    ProjectBO patchedProject = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(modelObject.getId().toString(), patchedProject.getLiveModelId());
    String deleteURL = basePath + "clients/" + client.getId() + "/projects/" + project.getId() + "/models/" + patchedProject.getLiveModelId();
    ResponseEntity < Void > responseEntity =
            this.restTemplate.exchange(deleteURL, HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    client.delete();
    project.delete();
    modelObject.delete();
    modelJobQueueBO.delete();
  }

  @Test
  public void testPatchProjectForPreviewedModel() throws IOException {
    String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    String name = methodName + "_" + Long.toString(System.currentTimeMillis() % 10000000);
    ClientBO client = ModelUtils.getTestClientObject(name);
    client.create();
    ProjectBO project = ModelUtils.getTestProjectObject(client.getId(), currentUserId, name);
    project.create();
    ModelBO modelObject = ModelUtils.getModelObject(project.getId());
    modelObject.create();
    modelObject.setModelId(modelObject.getId().toString());
    modelObject.update();
    ModelJobQueueBO modelJobQueueBO = new ModelJobQueueBO();
    modelJobQueueBO.setModelId(modelObject.getId().toString());
    modelJobQueueBO.setToken(modelObject.getId().toString());
    modelJobQueueBO.setStatus(ModelJobQueueBO.Status.COMPLETED);
    modelJobQueueBO.setStartedAt(System.currentTimeMillis());
    modelJobQueueBO.setModelType(modelObject.getModelType());
    modelJobQueueBO.create();
    PatchRequest patchRequest = new PatchRequest();
    PatchDocument patchDocument = new PatchDocument();
    patchDocument.setOp(PatchDocument.OpEnum.REPLACE);
    patchDocument.setPath("/previewModelId");
    patchDocument.setValue(modelObject.getId());
    patchRequest.add(patchDocument);
    ObjectMapper mapper = new ObjectMapper();
    String jsonPatch = mapper.writeValueAsString(patchRequest);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity < String > httpEntity = new HttpEntity < String > (jsonPatch, headers);
    String url = basePath + "clients/" + client.getId() + "/projects/" + project.getId();
    ResponseEntity < ProjectBO > entity =
            this.restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, ProjectBO.class);
    ProjectBO patchedProject = entity.getBody();
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(modelObject.getId().toString(), patchedProject.getPreviewModelId());
    String deleteURL = basePath + "clients/" + client.getId() + "/projects/" + project.getId() + "/models/" + patchedProject.getPreviewModelId();
    ResponseEntity < Void > responseEntity =
            this.restTemplate.exchange(deleteURL, HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    client.delete();
    project.delete();
    modelObject.delete();
    modelJobQueueBO.delete();
  }
}