/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.Project;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-27T13:56:05.802-04:00")
public abstract class ProjectsApiService {

  public abstract Response createProject(String clientId, Project project,
      UriInfo uriInfo) throws NotFoundException;

  public abstract Response deleteProjectById(String clientId, String projectId)
      throws NotFoundException;

  public abstract Response getProjectById(String clientId, String projectId)
      throws NotFoundException;

  public abstract Response listProjects(String filterClientId, Integer limit, Integer startIndex,
      boolean showDeleted, UriInfo uriInfo) throws NotFoundException;

  public abstract Response patchProjectById(String clientId, String projectId,
      PatchRequest jsonPatch) throws NotFoundException;

  public abstract Response promoteProjectById(String clientId, String projectId,
                                            String globalProjectId, String globalProjectName) throws NotFoundException;

  public abstract Response demoteProjectById(String clientId, String projectId) throws NotFoundException;

  public abstract Response addDatasetToProjectMappingById(String clientId,
      String projectId, String datasetId, final Boolean autoTagDataset) throws NotFoundException;

  public abstract Response removeDatasetToProjectMappingById(String clientId, String projectId,
      String datasetId) throws NotFoundException;

  public abstract Response listDatasetsForProjectById(String clientId, String projectId,
      Integer limit, Integer startIndex,
      UriInfo uriInfo);

  public abstract Response undeleteProjectById(String clientId, String projectId)
      throws NotFoundException;

  public abstract Response getConfigsForProject(String clientId, String projectId)
      throws NotFoundException;

}
