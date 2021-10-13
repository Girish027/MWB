/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.*;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.Error;

@Component
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the projects API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-27T13:56:05.802-04:00")
public class ProjectsApi {

  private final ProjectsApiService projectsApiService;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  public ProjectsApi(ProjectsApiService projectsApiService) {

    this.projectsApiService = projectsApiService;

  }

  @POST
  @Path("/clients/{clientId}/projects")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Create a project", notes = "",
          response = ProjectBO.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 201,
          message = "Project succesfully created", response = ProjectBO.class),
          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Project could not be created", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response createProject(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId
          , @ApiParam(value = "Project object") @Valid Project project,
          @Context UriInfo uriInfo)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    return projectsApiService.createProject(clientId, project, uriInfo);
  }

  // TODO: revert limit back to 100 after fixing MD-153
  @DELETE
  @Path("/clients/{clientId}/projects/{projectId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes a project.",
          notes = "Deletes the project and its all associated projects and artifacts",
          response = void.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = com.tfs.learningsystems.ui.model.Error.class)})
  public Response deleteProjectById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project to delete",
                  required = true, example = "561") @PathParam("projectId") String projectId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    return projectsApiService.deleteProjectById(clientId, projectId);

  }


  @GET
  @Path("/clients/{clientId}/projects/{projectId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Info for a specific Project", notes = "",
          response = ProjectBO.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "Expected response to a valid request", response = ProjectBO.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = ProjectBO.class)})
  public Response getProjectById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project to retrieve",
                  required = true, example = "561") @PathParam("projectId") String projectId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return projectsApiService.getProjectById(clientId, projectId);

  }

  @GET
  @Path("/clients/{clientId}/projects")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all projects", notes = "",
          response = ProjectsDetail.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "An paged array of Projects", response = ProjectsDetail.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = ProjectBO.class)})
  public Response listProjects(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "How many items to return at one time (max 500)",
                  defaultValue = "500") @DefaultValue("500") @QueryParam("limit") @Min(0) @Max(500) Integer limit,
          @ApiParam(value = "starting index",
                  defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") @Min(0) Integer startIndex,
          @ApiParam(value = "show disabled",
                  defaultValue = "false") @DefaultValue("false") @QueryParam("showDeleted") boolean showDeleted,
          @Context UriInfo uriInfo)
          throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);

    return projectsApiService.listProjects(clientId, limit, startIndex, showDeleted,
            uriInfo);
  }

  @PATCH
  @Path("clients/{clientId}/projects/{projectId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a project",
          notes = "Modifies an attribute of a project without required parameters",
          response = ProjectBO.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "OK", response = ProjectBO.class),
          @io.swagger.annotations.ApiResponse(code = 401,
                  message = "Unauthorized to call the API", response = void.class),
          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Project could not be modified", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "User is not authorized", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 409,
                  message = "Project name already exists", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response patchProjectById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project to patch",
                  required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "Patch request", required = true) @Valid PatchRequest jsonPatch)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return projectsApiService.patchProjectById(clientId, projectId, jsonPatch);

  }

  @PUT
  @Path("clients/{clientId}/projects/{projectId}/promote")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Promote a project",
          notes = "Promote a project with required parameters",
          response = ProjectsDetail.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "OK", response = ProjectsDetail.class),
          @io.swagger.annotations.ApiResponse(code = 401,
                  message = "Unauthorized to call the API", response = void.class),
          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Project could not be promoted", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 409,
                  message = "Project name already exists", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response promoteProjectById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project to promote",
                  required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "Global project id", example = "456") @QueryParam("globalProjectId") String globalProjectId,
          @ApiParam(value = "Global project name", required = true, example = "Root_Intent") @QueryParam("globalProjectName") String globalProjectName)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(globalProjectName);

    return projectsApiService.promoteProjectById(clientId, projectId, globalProjectId, globalProjectName);
  }

  @PUT
  @Path("clients/{clientId}/projects/{projectId}/demote")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Demote a project",
          notes = "Demote a project with required parameters",
          response = ProjectBO.class, tags = {"projects"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "OK", response = ProjectBO.class),
          @io.swagger.annotations.ApiResponse(code = 401,
                  message = "Unauthorized to call the API", response = void.class),
          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Project could not be promoted", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 409,
                  message = "Project name already exists", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response demoteProjectById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project to demote",
                  required = true, example = "561") @PathParam("projectId") String projectId)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return projectsApiService.demoteProjectById(clientId, projectId);
  }

  @PUT
  @Path("clients/{clientId}/projects/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Map a specified dataset to a specific project",
          notes = "", response = void.class, tags = {"put",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Dataset could not be mapped to project", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 409,
                  message = "Conflict (Mapping already exists)", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response addDatasetToProjectMappingById(
          @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project",
                  required = true) @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset",
                  required = true) @PathParam("datasetId") String datasetId,
          @ApiParam(value = "The id of the dataset",
                  required = false) @QueryParam("autoTagDataset") @DefaultValue("false") Boolean autoTagDataset)
          throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return projectsApiService
            .addDatasetToProjectMappingById(clientId, projectId, datasetId, autoTagDataset);

  }


  @GET
  @Path("/clients/{clientId}/projects/{projectId}/datasets")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all data sets mapped to project", notes = "",
          response = DatasetsDetail.class, tags = {"datasets"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "An paged array of Data Sets", response = DatasetsDetail.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response listDatasets(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project",
                  required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "How many items to return at one time (max 100)", example = "100",
                  defaultValue = "100") @DefaultValue("100") @QueryParam("limit") @Min(0) @Max(100) Integer limit,
          @ApiParam(value = "starting index", example = "0",
                  defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") @Min(0) Integer startIndex,
          @Context UriInfo uriInfo)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    return projectsApiService.listDatasetsForProjectById(clientId, projectId, limit, startIndex,
            uriInfo);
  }

  @DELETE
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(
          value = "Remove specified dataset mapping for a specific project", notes = "",
          tags = {"datasets"},
          response = void.class)
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Dataset could not be removed from project", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response removeDatasetToProjectMappingById(
          @ApiParam(value = "The id of the client", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project",
                  required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset",
                  required = true, example = "721") @PathParam("datasetId") String datasetId) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return projectsApiService.removeDatasetToProjectMappingById(clientId, projectId, datasetId);

  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/configs")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(
          value = "Get all conigurations for a project", notes = "",
          response = ModelConfigCollection.class, tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK",
                  response = ModelConfigCollection.class),

          @io.swagger.annotations.ApiResponse(code = 404,
                  message = "Invalid project Id",
                  response = ModelConfigCollection.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal server error",
                  response = ModelConfigCollection.class)})
  public Response getConfigsForProject(
          @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId,
          @ApiParam(value = "The project Id", required = true)
          @PathParam("projectId") String projectId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    return projectsApiService.getConfigsForProject(clientId, projectId);

  }
}
