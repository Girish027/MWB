/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the Data Management API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-02-08T10:33:40.646-05:00")
public class DataManagementApi {

  private final DataManagementApiService dataManagementApiService;
  @Autowired
  private AppConfig appConfig;

  @Autowired
  public DataManagementApi(DataManagementApiService dataManagementApiService) {

    this.dataManagementApiService = dataManagementApiService;

  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/export")
  @Consumes({"application/json"})
  @Produces({"application/zip", "application/octet-stream"})
  @io.swagger.annotations.ApiOperation(
      value = "Export all of the data associated to project and dataset in application/zip format",
      notes = "", response = File.class, tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "Export data into a Zip file", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Project not found to export", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 408,
          message = "Request timed out", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response export(
      @ApiParam(value = "the client ID of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map dataset to",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "") SearchRequest search,
      @ApiParam(value = "The operator for query string", defaultValue = "AND") @DefaultValue("AND") @QueryParam("op") String queryOperator)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return dataManagementApiService.export(clientId, projectId, search, queryOperator);

  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/export")
  @Consumes({"application/json"})
  @Produces({"application/zip", "application/octet-stream"})
  @io.swagger.annotations.ApiOperation(
      value = "Export all of the data associated to project and dataset in application/zip format",
      notes = "", response = File.class, tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "Export data into a Zip file", response = File.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Project or Dataset not found to export", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 408,
          message = "Request timed out", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response export(
      @ApiParam(value = "the client ID of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map dataset to",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset to map",
          required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "The operator for query string", defaultValue = "AND") @DefaultValue("AND") @QueryParam("op") String queryOperator)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return dataManagementApiService.export(clientId, projectId, datasetId, queryOperator);

  }


  @GET
  @Path("/clients/{clientId}/projects/{projectId}/taggingguide/export")
  @Consumes({"application/json"})
  @Produces({"text/csv"})
  @io.swagger.annotations.ApiOperation(value = "Export tagging guide for a project in text/csv format", notes = "", response = File.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Export tagging guide for a project to a CSV file", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 404, message = "Project not found", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 408, message = "Request Timeout", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable", response = File.class)})
  public Response exportTaggingGuideForProject(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map dataset to", required = true) @PathParam("projectId") String projectId)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return dataManagementApiService.exportTaggingGuideForProject(clientId, projectId);

  }

  @PUT
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/transform")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "transforms a dataset for a project", notes = "",
      response = TaskEventBO.class, tags = {"put",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 202,
      message = "ACCEPTED", response = TaskEventBO.class),

      @io.swagger.annotations.ApiResponse(code = 401,
          message = "Unauthorized to call the API", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Project or Dataset cannot be found", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 409,
          message = "Dataset has already been transformed", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response transformDataset(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      ,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset to map",
          required = true) @PathParam("datasetId") String datasetId,
      @Context UriInfo uriInfo,
      @ApiParam(value = "Boolean flag to indicate whether to use model for suggested category",
          required = false) @QueryParam("useModelForSuggestedCategory") Boolean useModelForSuggestedCategory)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return dataManagementApiService
        .transformDataset(clientId, projectId, datasetId, uriInfo, useModelForSuggestedCategory);

  }

  @PUT
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/transform/retry")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "retry a failed transformation job", notes = "",
      response = TaskEventBO.class, tags = {"put",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 202,
      message = "ACCEPTED", response = TaskEventBO.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Project or Dataset cannot be found", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 409,
          message = "Dataset has already been transformed, or indexed",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response transformRetry(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map dataset to",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset to map",
          required = true) @PathParam("datasetId") String datasetId,
      @Context UriInfo uriInfo,
      @ApiParam(value = "Boolean flag to indicate whether to use model for suggested category",
          required = false) @QueryParam("useModelForSuggestedCategory") Boolean useModelForSuggestedCategory)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);
    return dataManagementApiService
        .transformRetry(clientId, projectId, datasetId, uriInfo, useModelForSuggestedCategory);

  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/transform/status")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Retrieves the status of a transform job",
      notes = "", response = TaskEventBO.class, tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "OK", response = TaskEventBO.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Transformation job cannot be found", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response transformStatus(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,

      @ApiParam(value = "The id of the project to map dataset to",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset to map",
          required = true) @PathParam("datasetId") String datasetId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return dataManagementApiService.transformStatus(clientId, projectId, datasetId);

  }


  @POST
  @Path("/clients/{clientId}/projects/transform/status")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Retrieves the status of all transform jobs for requested projects", notes = "", response = ProjectTaskStatusResponse.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ProjectTaskStatusResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request, e.g if no project or dataset ids are given", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable", response = Error.class)})
  public Response transformStatusForProjects(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "Project and dataset ids to get the status", required = true) Map<String, List<String>> projectDatasets)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);

    ApiParameterSanitizer.sanitizeMapListValues(projectDatasets);

    if (projectDatasets != null && !projectDatasets.isEmpty()) {
      ApiParameterSanitizer.sanitizeSet(projectDatasets.keySet());
    }

    return dataManagementApiService.transformStatusForProjects(clientId, projectDatasets);
  }

  @DELETE
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/transform")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Delete a failed transform job",
      notes = "", response = Void.class, tags = {"delete",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 204,
      message = "OK", response = Void.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Transformation job cannot be found", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 409,
          message = "Dataset has already been transformed, or indexed",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response deleteFailedTransformation(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map dataset to",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset to map",
          required = true) @PathParam("datasetId") String datasetId) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return dataManagementApiService.deleteFailedTransformation(clientId, projectId, datasetId);

  }

  @PUT
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/transform/cancel")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Cancel a transform job",
      notes = "", response = Void.class, tags = {"delete",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "OK", response = TaskEventBO.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Transformation job cannot be found", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 409,
          message = "Dataset has already been transformed, or indexed",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Service Unavailable",
          response = Error.class)})
  public Response cancelTransformation(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project",
          required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset",
          required = true) @PathParam("datasetId") String datasetId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);

    return dataManagementApiService.cancelTransformation(clientId, projectId, datasetId);

  }
}
