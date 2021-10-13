/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/v1/taggingguide")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the v1 API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-08T18:54:19.161-07:00")
public class TaggingGuideApi {

  private final TaggingGuideApiService taggingGuideApiService;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  public TaggingGuideApi(TaggingGuideApiService taggingGuideApiService) {

    this.taggingGuideApiService = taggingGuideApiService;
  }

  @POST
  @Path("/{clientId}/{projectId}/import")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add intent tagging guide to a project", notes = "", response = void.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TaggingGuideImportStatBO.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = Error.class)})
  public Response addIntentGuideToProject(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @FormDataParam("file") InputStream fileInputStream)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);

    ApiParameterSanitizer.sanitize(projectId);

    return taggingGuideApiService.importGuide(projectId, fileInputStream);
  }

  @POST
  @Path("/{clientId}/{projectId}/import/{token}/commit")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Commit the staged intent tagging guide to the project", notes = "", response = TaggingGuideImportStatBO.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TaggingGuideImportStatBO.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = Error.class)})
  public Response commitIntentGuideToProject(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The token id of the transaction", required = true) @PathParam("token") String token)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(token);

    return taggingGuideApiService.commitImportGuide(projectId, token);
  }

  @POST
  @Path("/{clientId}/{projectId}/import/{token}/abort")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Abort the staged intent tagging guide to the project", notes = "", response = void.class, tags = {
      "delete",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully deleted", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id or token", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = void.class)})
  public Response abortAddingIntentGuideToProject(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The token id of the transaction", required = true) @PathParam("token") String token)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(token);

    return taggingGuideApiService.abortImportGuide(projectId, token);
  }

  @POST
  @Path("/{clientId}/{projectId}/import/{token}/column/mapping")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Create a project", notes = "", response = TaggingGuideColumnMappingSelectionList.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Column mappings successfully added for the user", response = TaggingGuideColumnMappingSelectionList.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id", response = TaggingGuideColumnMappingSelectionList.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TaggingGuideColumnMappingSelectionList.class)})
  public Response addMappings(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      ,
      @ApiParam(value = "The token of the import session", required = true) @PathParam("token") String token
      ,
      @ApiParam(value = "Conditional stating if first row to be ignored", required = false) @DefaultValue("true") @QueryParam("ignoreFirstRow") boolean ignoreFirstRow
      ,
      @ApiParam(value = "Column mapping selection") TaggingGuideColumnMappingSelectionList columnMappings)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(token);

    return taggingGuideApiService
        .importGuideAddMappings(projectId, token, ignoreFirstRow, columnMappings);
  }

  @GET
  @Path("/columns")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all available tagging guide columns", notes = "", response = TaggingGuideColumnList.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An array of tagging guide columns", response = TaggingGuideColumnList.class)})
  public Response listColumns()
      throws NotFoundException {

    return taggingGuideApiService.listColumns();
  }

  @GET
  @Path("/projects/{clientId}/{projectId}/import/column/mapping")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all tagging guide mappings for the project", notes = "", response = TaggingGuideColumnMappingCollection.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An array of Column mappings for the project", response = TaggingGuideColumnMappingCollection.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid projectId", response = TaggingGuideColumnMappingCollection.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TaggingGuideColumnMappingCollection.class)})
  public Response listMappings(
      @ApiParam(value = "the client ID", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId)
      throws NotFoundException {

    return taggingGuideApiService.listMappings(projectId);
  }
}
