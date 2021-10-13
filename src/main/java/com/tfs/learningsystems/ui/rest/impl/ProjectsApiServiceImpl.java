/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.ui.*;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.rest.ProjectsApiService;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Component
@Slf4j
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-27T13:56:05.802-04:00")
public class ProjectsApiServiceImpl extends ProjectsApiService {

  @Inject
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Inject
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;

  @Autowired
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;


  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;


  @Inject
  private DataManagementManager dataManagementManager;

  @Autowired
  private ConfigManager configManager;

  @Autowired
  private ContentManager contentManager;

  @Autowired
  @Qualifier("preferenceManagerBean")
  private PreferenceManager preferenceManager;

  @Autowired
  @Qualifier("vectorizerManagerBean")
  private VectorizerManager vectorizerManager;

  @Override
  public Response createProject(String clientId, Project project, UriInfo uriInfo)
      throws NotFoundException {

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    ProjectBO createdProject = null;
    createdProject = projectManager.addProject(clientId, project);

    VectorizerBO vectorizerBO = vectorizerManager.getVectorizerByClientProject(clientId, null);

    preferenceManager.addPreference(clientId, Constants.VECTORIZER_TYPE, createdProject.getId().toString(), vectorizerBO.getId(),
            Constants.PREFERENCE_MODEL_LEVEL, true);

    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + createdProject.getId()).build();
    log.info("Project cerated - {}", createdProject.getId());
    return Response.created(locationURI).entity(createdProject).build();
  }

  @Override
  public Response deleteProjectById(String clientId, String projectId)
      throws NotFoundException {
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      log.error("Project cannot be deleted - unauthorized user - " + currentUserEmail);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    Response.Status deleteStatus = Response.Status.NO_CONTENT;
    ProjectDetail project = new ProjectDetail();
    try {
      project = projectManager.deleteProject(clientId, projectId, currentUserEmail);
      log.info("Project is deleted - {} ", projectId);
    } catch (Exception e) {
      log.error("Failed to delete project by id - " + projectId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Error(
          Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
          ErrorMessage.BACKEND_ERROR))
          .build();
    }
    return Response.ok().build();
  }


  @Override
  public Response getProjectById(String clientId, String projectId)
      throws NotFoundException {

    validationManager.validateClient(clientId);
    ProjectBO project = projectManager.getProjectById(clientId, projectId);
    if (project == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new Error(Response.Status.NOT_FOUND.getStatusCode(), null,
              "")).build();
    }

    return Response.ok(project).build();
  }


  @Override
  public Response listProjects(String filterClientId, Integer limit, Integer startIndex,
      boolean showDeleted,
      UriInfo uriInfo) throws NotFoundException {

    validationManager.validateClient(filterClientId);

    // TODO Client_Isolation make sure client Id is getting authenticated in this flow in auth Filter.

    Long totalCount = projectManager.countProjects(filterClientId, showDeleted);
    ProjectsDetail projects = new ProjectsDetail();
    if (totalCount == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    } else if (startIndex > totalCount) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    } else {
      projects.addAll(projectManager
          .getProjects(filterClientId, startIndex, limit, "", "ID", "ASC", showDeleted));
      if (!projects.isEmpty()) {
        projects.get(0).setOffset(startIndex);
        projects.get(0).setTotalCount(totalCount.longValue());
      }
      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      long nextPage = (limit + startIndex) > totalCount ? totalCount : limit + startIndex;
      URI locationURI = uriBuilder.replaceQueryParam("startIndex", nextPage)
          .replaceQueryParam("limit", limit).build();
      return Response.ok(projects).header("X-Total-Count", totalCount)
          .header("X-Offset", startIndex).location(locationURI).build();
    }
  }

  @Override
  public Response patchProjectById(String clientId, String projectId, PatchRequest jsonPatch)
      throws NotFoundException {

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    ProjectBO modifiedProject = projectManager.updateProject(clientId, projectId, jsonPatch, currentUserEmail);
    return Response.ok(modifiedProject).build();
  }

  @Override
  public Response promoteProjectById(String clientId, String projectId, String globalProjectId, String globalProjectName)
          throws NotFoundException {
    
    ProjectsDetail promotedProjects = new ProjectsDetail();
    promotedProjects.addAll(projectManager
            .promoteProject(clientId, projectId, globalProjectId, globalProjectName));
    return Response.ok(promotedProjects).build();
  }

  @Override
  public Response demoteProjectById(String clientId, String projectId)
          throws NotFoundException {

    ProjectBO demotedProject = projectManager.demoteProject(clientId, projectId);
    return Response.ok(demotedProject).build();
  }

  @Override
  public Response addDatasetToProjectMappingById(String clientId, String projectId,
      String datasetId,
      final Boolean autoTagDataset) throws NotFoundException {

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    validationManager.validateClient(clientId);
    projectManager.addDatasetProjectMapping(clientId, projectId, datasetId, currentUserEmail);
    if (autoTagDataset) {
      projectManager.addDatasetIntentInheritance(datasetId, projectId, currentUserEmail);
    }
    log.info("Associated dataset - {} with project - {} ", datasetId, projectId);
    return Response.noContent().build();
  }


  @Override
  public Response removeDatasetToProjectMappingById(String clientId, String projectId,
      String datasetId) throws NotFoundException {
    validationManager.validateClientAndProject(clientId, projectId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    projectManager.removeDatasetProjectMapping(clientId, datasetId, projectId, currentUserEmail);

    this.datasetManager.deleteDataset(clientId, datasetId);
    this.contentManager.deleteRecords(clientId, projectId, datasetId);

    return Response.noContent().build();
  }


  @Override
  public Response listDatasetsForProjectById(String clientId, String projectId, Integer limit,
      Integer startIndex, UriInfo uriInfo) {

    validationManager.validateClientAndProject(clientId, projectId);

    Long totalCount = projectManager.countDatasetsForProjectById(clientId, projectId);
    DatasetsDetail datasets = new DatasetsDetail();

    if (totalCount == null) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    } else if (startIndex > totalCount) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    } else {

      List<DatasetBO> datasetList = projectManager
          .listDatasetsForProjectById(clientId, projectId, startIndex, limit, "", "ID", "DESC");
      for (DatasetBO dataset : datasetList) {
        TaskEventBO taskEvent = dataManagementManager
            .status(clientId, projectId, Integer.toString(dataset.getId()));

        if (taskEvent != null) {
          if (taskEvent.getStatus().equals(TaskEvent.Status.COMPLETED)
              && taskEvent.getPercentComplete() != 100) {
            dataset.setTransformationStatus(TaskEvent.Status.QUEUED.toString());
          } else {
            dataset.setTransformationStatus(taskEvent.getStatus().toString());
          }
          dataset.setTransformationTask(taskEvent.getTask().toString());

        } else {
          dataset.setTransformationStatus(TaskEvent.Status.NULL.toString());
          dataset.setTransformationTask(null);
        }

        datasets.add(dataset);
      }

      if (!datasets.isEmpty()) {
        datasets.get(0).setOffset(startIndex);
        datasets.get(0).setTotalCount(totalCount.longValue());
      }

      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      long nextPage = (limit + startIndex) > totalCount ? totalCount : limit + startIndex;
      URI locationURI = uriBuilder.replaceQueryParam("startIndex", nextPage)
          .replaceQueryParam("limit", limit).build();
      return Response.ok(datasets).header("X-Total-Count", totalCount)
          .header("X-Offset", startIndex).location(locationURI).build();
    }
  }

  @Override
  public Response undeleteProjectById(String clientId, String projectId)
      throws NotFoundException {

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      log.error("Project cannot be undeleted - unauthorized - " + projectId);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    Response.Status deleteStatus = Response.Status.OK;
    ProjectBO project = null;
    try {
      projectManager.undeleteProject(clientId, projectId, currentUserEmail);
      project = projectManager.getProjectById(clientId, projectId, false);
    } catch (com.tfs.learningsystems.ui.model.error.NotFoundException ue) {
      log.error("Failed to undelete project by id, not found - " + projectId, ue);
      deleteStatus = Response.Status.NOT_FOUND;
    } catch (BadRequestException be) {
      log.error("Failed to undelete project by id - " + projectId, be);
      deleteStatus = Response.Status.BAD_REQUEST;
    } catch (Exception e) {
      log.error("Failed to undelete project by id - " + projectId, e);
      deleteStatus = Response.Status.INTERNAL_SERVER_ERROR;
    }
    return Response.status(deleteStatus).entity(project).build();
  }

  @Override
  public Response getConfigsForProject(String clientId, final String projectId) throws
      com.tfs.learningsystems.ui.rest.NotFoundException {

    // TODO Client_Isolation  add client id as part of getModelConfigsByProject
    ModelConfigCollection configDetails = this.configManager.getModelConfigsByProject(clientId, projectId);
    return Response.ok().entity(configDetails).build();
  }
}