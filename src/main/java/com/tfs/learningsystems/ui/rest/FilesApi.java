/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. 
 * All Rights Reserved. 
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.ui.model.FileEntryCollection;
import com.tfs.learningsystems.ui.model.FileEntryDetail;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/files")
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the files API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-22T16:11:15.902-08:00")
public class FilesApi {

  private final FilesApiService delegate;

  @Autowired
  public FilesApi(FilesApiService apiService) {
    this.delegate = apiService;
  }

  @POST
  @Path("/import")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add a file", notes = "", response = FileStagedImportResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 201, message = "File succesfully added", response = FileStagedImportResponse.class),
      @io.swagger.annotations.ApiResponse(code = 400, message = "Files could not be added", response = FileStagedImportResponse.class),
      @io.swagger.annotations.ApiResponse(code = 401, message = "User not authenticated", response = FileStagedImportResponse.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileStagedImportResponse.class)})
  public Response importFile(
      @FormDataParam("datatype") String datatype,
      @FormDataParam("file") InputStream fileInputStream,
      @Context HttpServletRequest request
  )
      throws NotFoundException {
    return delegate.importFile(datatype, fileInputStream, request);
  }

  @POST
  @Path("/import/{token}/column/mapping")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add file with user selected columns", notes = "", response = FileBO.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 201, message = "File succesfully added", response = FileBO.class),
      @io.swagger.annotations.ApiResponse(code = 400, message = "Files could not be added", response = FileBO.class),
      @io.swagger.annotations.ApiResponse(code = 401, message = "User not authenticated", response = FileBO.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileBO.class)})
  public Response generateUserSelectedColumnsFile(
      @ApiParam(value = "The token of the import session", required = true) @PathParam("token") String token,
      @ApiParam(value = "Conditional stating if first row to be ignored", required = false) @DefaultValue("true") @QueryParam("ignoreFirstRow") boolean ignoreFirstRow,
      @ApiParam(value = "Column mapping selection") FileColumnMappingSelectionList columnMappings,
      @Context UriInfo uriInfo)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(token);
    return delegate.generateUserSelectedColumnsFile(token, ignoreFirstRow, columnMappings);
  }

  @POST

  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add a file", notes = "", response = FileEntryDetail.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 201, message = "File succesfully added", response = FileEntryDetail.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Files could not be added", response = FileEntryDetail.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "User not authenticated", response = FileEntryDetail.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileEntryDetail.class)})
  public Response addFile(
      @FormDataParam("username") String username,
      @FormDataParam("datatype") String datatype,
      @FormDataParam("file") InputStream fileInputStream,
      @Context UriInfo uriInfo,
      @Context HttpServletRequest request
  )
      throws NotFoundException {
    return delegate.addFile(username, datatype, fileInputStream, uriInfo, request);
  }

  @DELETE
  @Path("/{fileId}")

  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes a user.", notes = "Deletes the file", response = void.class, tags = {
      "delete",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = void.class)})
  public Response deleteFileById(
      @ApiParam(value = "The id of the file to delete", required = true) @PathParam("fileId") String fileId)
      throws NotFoundException {
    return delegate.deleteFileById(fileId);
  }

  @GET
  @Path("/{fileId}")

  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Info for a specific File", notes = "", response = FileEntryDetail.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Expected response to a valid request", response = FileEntryDetail.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileEntryDetail.class)})
  public Response getFileById(
      @ApiParam(value = "The id of the file to retrieve", required = true) @PathParam("fileId") String fileId)
      throws NotFoundException {
    return delegate.getFileById(fileId);
  }

  @GET

  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all files", notes = "", response = FileEntryCollection.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An paged array of Files", response = FileEntryCollection.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileEntryCollection.class)})
  public Response listFiles(
      @ApiParam(value = "How many items to return at one time (max 100)", defaultValue = "25") @DefaultValue("25") @QueryParam("limit") @Min(0) @Max(100) Integer limit
      ,
      @ApiParam(value = "starting index", defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") @Min(0) Integer startIndex)
      throws NotFoundException {
    return delegate.listFiles(limit, startIndex);
  }

  @PATCH
  @Path("/{fileId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a file (currently on name)", notes = "Modifies an attribute of a file without required parameters", response = FileEntryDetail.class, tags = {
      "patch",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = FileEntryDetail.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = FileEntryDetail.class)})
  public Response patchFileById(
      @ApiParam(value = "The id of the file to patch", required = true) @PathParam("fileId") String fileId
      , @ApiParam(value = "", required = true) PatchRequest jsonPatch)
      throws NotFoundException {
    return delegate.patchFileById(fileId, jsonPatch);
  }
}
