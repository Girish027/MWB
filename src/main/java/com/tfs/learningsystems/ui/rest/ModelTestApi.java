package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.BatchTestResultsResponse;
import com.tfs.learningsystems.ui.model.EvaluationResponse;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
@io.swagger.annotations.Api(description = "the test API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
public class ModelTestApi {

  private final ModelTestApiService delegate;

  @Autowired
  public ModelTestApi(ModelTestApiService service) {
    delegate = service;
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/batch_tests")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "get results of previous batch tests, their status and info", notes = "", response = BatchTestResultsResponse.class, tags = {})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully get the status", response = BatchTestResultsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id, model id, or the combination", response = BatchTestResultsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = BatchTestResultsResponse.class)})
  public Response listBatchTests(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      ,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      ,
      @ApiParam(value = "The id of the model", required = true) @PathParam("modelId") String modelId
      ,
      @ApiParam(value = "How many items to return at one time (max 20)", defaultValue = "20") @DefaultValue("20") @QueryParam("limit") Integer limit
      ,
      @ApiParam(value = "starting index", defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") Integer startIndex)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return delegate.listBatchTests(clientId, projectId, modelId, limit, startIndex);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/check_eval/{test_id}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "get status, and output of model test", notes = "", response = EvaluationResponse.class, tags = {})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully get the status", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id, model id, or the combination", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = EvaluationResponse.class)})
  public Response checkStatus(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      ,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      ,
      @ApiParam(value = "The id of the model", required = true) @PathParam("modelId") String modelId
      ,
      @ApiParam(value = "The id of the model test, returned from 'eval_datasets'", required = true) @PathParam("test_id") String testId)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    ApiParameterSanitizer.sanitize(testId);
    return delegate.checkStatus(clientId, projectId, modelId, testId);
  }

  @POST
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/eval_datasets")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Metadata about a intent classification for a specific utternace", notes = "", response = EvaluationResponse.class, tags = {})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Datasets are successfully put in queue for the user", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id, model id, or the combination", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = EvaluationResponse.class)})
  public Response evalDatasets(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      , @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      , @ApiParam(value = "The id of the model", required = true) @PathParam("modelId") String modelId
      , @ApiParam(value = "Type of model being tested", required = false) @DefaultValue("DIGITAL") @QueryParam("testModelType") String testModelType
      , @ApiParam(value = "", required = true) List<String> datasets)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);

    return delegate.evalDatasets(clientId, projectId, modelId, testModelType, datasets);
  }

  @POST
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/eval_transcriptions")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Metadata about a intent classification for a specific utternace", notes = "", response = EvaluationResponse.class, tags = {})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Utterances are successfully evaluated for the user", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id, model id, or the combination", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = EvaluationResponse.class)})
  public Response evalTranscriptions(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      , @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      , @ApiParam(value = "The id of the model", required = false) @PathParam("modelId") String modelId
      , @ApiParam(value = "Type of model being tested", required = false) @DefaultValue("DIGITAL") @QueryParam("testModelType") String testModelType
      , @ApiParam(value = "", required = false) List<String> utterances)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);

    return delegate.evalTranscriptions(clientId, projectId, modelId, testModelType, utterances);
  }

  @POST
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/eval_utterance")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Metadata about a intent classification for a specific utternace", notes = "", response = EvaluationResponse.class, tags = {})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Utterances are successfully evaluated for the user", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project id, model id, or the combination", response = EvaluationResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = EvaluationResponse.class)})
  public Response evalSpeechUtterance(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId
      ,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
      ,
      @ApiParam(value = "The id of the model", required = true) @PathParam("modelId") String modelId,
      @ApiParam(value = "type of file", required = true) @QueryParam("fileType") String fileType,
      @ApiParam(value = "url for audio file", required = false) @FormDataParam("audioURL") String audioURL,
      @ApiParam(value = "file data", required = false) @FormDataParam("audioFile") InputStream fileInputStream)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return delegate.evalSpeechUtterance(clientId, projectId, modelId, fileType,audioURL,fileInputStream);
  }
}
