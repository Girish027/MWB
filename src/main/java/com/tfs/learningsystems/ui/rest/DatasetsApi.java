/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import java.io.InputStream;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
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
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the datasets API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-01-02T23:00:11.057+05:30")
public class DatasetsApi {

  private final DatasetsApiService datasetsApiService;

  @Autowired
  public DatasetsApi(DatasetsApiService service) {
    this.datasetsApiService = service;
  }

  @POST
  @Path("/clients/{clientId}/projects/{projectId}/datasets")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add a new dataset", notes = "", tags = {"datasets"},
          response = DatasetBO.class)
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 201,
          message = "Dataset succesfully created", response = DatasetBO.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Dataset could not be created", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "Operation is not allowed", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response postDataset(
          @ApiParam(value = "client id", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "project id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "dataset name",
                  required = true, example = "September Chat data") @FormDataParam("name") String name,
          @ApiParam(value = "source",
                  required = false, example = "I") @DefaultValue("E") @FormDataParam("source") String source,
          @ApiParam(value = "csv file to be uploaded",
                  required = true) @FormDataParam("file") InputStream fileInputStream,
          @ApiParam(value = "Condition stating if first row to be ignored",
                  required = false) @DefaultValue("true") @QueryParam("ignoreFirstRow") boolean ignoreFirstRow,
          @ApiParam(value = "Column mapping selection",
                  required = false , example = "[{\"id\": \"1\",\"columnName\": \"transcription\",\"columnIndex\": \"1\",\"displayName\": \"Transcription\"},{\"id\": \"7\",\"columnName\": \"rutag\",\"columnIndex\": \"0\",\"displayName\": \"Rollup Intent\"}]") @FormDataParam("columnMappings") String columnMappings,
          @ApiParam(value = "Datatype of dataset uploaded",
                  required = false, allowableValues = "Audio/Voice (Live),Audio/Voice (Data Collection),Synthetic/Text,Virtual Assistant/Text,Email/Text,Social/Text,Chat/Text") @DefaultValue("Virtual Assistant/Text") @FormDataParam("dataType") String dataType,
          @ApiParam(value = "description for the dataset",
                  required = false, example = "September chat data") @FormDataParam("description") String description,
          @Context UriInfo uriInfo)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    return datasetsApiService.postDataset(clientId, projectId, name, source, fileInputStream, ignoreFirstRow,
            columnMappings, dataType, description, uriInfo);
  }

  @POST
  @Path("/datasets")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Create a Data Set", notes = "",
          response = DatasetBO.class)
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 201,
          message = "Dataset succesfully created", response = DatasetBO.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Dataset could not be created", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "Operation is not allowed", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response createDataset(
          @ApiParam(value = "Dataset object") @Valid AddDatasetRequest addDatasetRequest,
          @Context UriInfo uriInfo)
          throws NotFoundException {
    return datasetsApiService.createDataset(addDatasetRequest, uriInfo);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Info for a specific Data Set", notes = "",tags = {"datasets"},
          response = DatasetBO.class)
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "Expected response to a valid request", response = DatasetBO.class),

          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "Operation is not allowed", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response getDatasetById(
          @ApiParam(value = "client id", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "project id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset to retrieve",
                  required = true, example = "721") @PathParam("datasetId") String datasetId) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);
    return datasetsApiService.getDatasetById(clientId,projectId,datasetId);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}/validate")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Info for a specific Data Set", notes = "",tags = {"datasets"},
          response = DatasetBO.class)
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "Expected response to a valid request", response = DatasetBO.class),
          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "Operation is not allowed", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response validDatasetById(
          @ApiParam(value = "client id", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "project id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset to retrieve",
                  required = true, example = "721") @PathParam("datasetId") String datasetId) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);
    return datasetsApiService.validDatasetForModelBuildingById(clientId,projectId,datasetId);
  }

  @PATCH
  @Path("/clients/{clientId}/projects/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a dataset",
          notes = "Modifies an attribute of a dataset without required parameters",
          tags = {"datasets"},
          response = DatasetBO.class)
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
          message = "OK", response = DatasetBO.class),
          @io.swagger.annotations.ApiResponse(code = 403,
                  message = "Operation is not allowed", response = Error.class),
          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                  response = Error.class)})
  public Response patchDatasetById(
          @ApiParam(value = "client id", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "project id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset to patch",
                  required = true, example = "721") @PathParam("datasetId") String datasetId,
          @ApiParam(value = "Patch request body", required = true, example = "[{\"op\": \"REPLACE\",\"path\": \"/description\",\"value\": \"use this description\"}]") PatchRequest jsonPatch) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(datasetId);
    return datasetsApiService.patchDatasetById(clientId,projectId,datasetId, jsonPatch);
  }
}