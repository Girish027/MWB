package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelList;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;

@Service
@Path("/v1")
@Consumes({"application/json"})
@io.swagger.annotations.Api(description = "the v1 API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public class ModelsApi {

  private final ModelsApiService modelsApiService;

  @Autowired
  public ModelsApi(ModelsApiService modelsApiService) {
    this.modelsApiService = modelsApiService;
  }

  @POST
  @Path("/clients/{clientId}/projects/{projectId}/models")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Create a model", notes = "", response = ModelBO.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "model configuration added", response = ModelBO.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request to create a model", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response configureModel(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true, example = "159") @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project to map model to", required = true, example = "561") @PathParam("projectId") String projectId,
      @ApiParam(value = "Model object") ModelBO model,
      @ApiParam(value = "Whether to start training immediately", defaultValue = "false")
      @DefaultValue("false") @QueryParam("trainNow")
          boolean trainNow, @Context UriInfo uriInfo,
      @ApiParam(value = "Operation type value", example = "speech or classifier") @QueryParam("modelType") String modelType,
      @ApiParam(value = "The model technology type", example = "n-gram") @DefaultValue("n-gram") @QueryParam("modelTechnology") String modelTechnology,
      @ApiParam(value = "Default value", example = "false") @DefaultValue("false") @QueryParam("toDefault") boolean toDefault)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    return modelsApiService.configureModel(clientId, projectId, model, trainNow, uriInfo, modelType, modelTechnology, toDefault);
  }


  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/download")
  @Consumes({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Model file of a model", notes = "", response = File.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = File.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid model id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response downloadModel(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The project to map model to", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true) @PathParam("modelId") String modelId)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.downloadModel(clientId, projectId, modelId);
  }


  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/statistics")
  @Consumes({"application/json"})
  @Produces({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @io.swagger.annotations.ApiOperation(value = "Statistics about a specific model", notes = "", response = File.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 400, message = "Model not built", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Model id, Model not found", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 503, message = "Model Statistics file not ready", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response downloadModelStats(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The project to map model to", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true) @PathParam("modelId") String modelId)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.downloadModelStats(clientId, projectId, modelId);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/training-outputs")
  @Consumes({"application/json"})
  @Produces({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @io.swagger.annotations.ApiOperation(value = "Training output from a specific model", notes = "", response = File.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = File.class),
      @io.swagger.annotations.ApiResponse(code = 400, message = "Model not built", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Model id, Model not found, or file not ready", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response downloadTrainingOutputs(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "Project Id of the model", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true) @PathParam("modelId") String modelId)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.downloadTrainingOutputs(clientId, projectId, modelId);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/status")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Status of the model creation request", notes = "", response = void.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Request OK. Model is still being built", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 303, message = "Check the other location header", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = void.class)})
  public Response getModelBuildingStatus(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The project to map model to", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench Model id", required = true) @PathParam("modelId") String modelId
      , @Context UriInfo uriInfo)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);

    return modelsApiService.getModelBuildingStatus(clientId, projectId, modelId, uriInfo);
  }


  @DELETE
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Delete a specific model", notes = "", response = ModelBO.class, tags = {
      "models",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ModelBO.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid model id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response deleteModelById(
      @ApiParam(value = "The id of the client of the project to map model to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The  the project to map model to", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true) @PathParam("modelId") String modelId)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.deleteModelById(clientId, projectId, modelId);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Metadata about a specific model", notes = "", response = ModelBO.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ModelBO.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid model id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response getModelById(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true, example = "159") @PathParam("clientId") String clientId,
      @ApiParam(value = "The project Id", required = true, example = "561") @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true, example = "768") @PathParam("modelId") String modelId)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.getModelById(clientId, projectId, modelId);
  }

  @PATCH
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a model configuration", notes = "Modifies one or more attributes of a model configuration", response = ModelBO.class, tags = {
          "models"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ModelBO.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = java.lang.Error.class)})
  public Response updateModelConfig(
          @ApiParam(value = "The id of the client of the project to map dataset to", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The project Id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The Modelling Workbench id of the model to patch", required = true, example = "768") @PathParam("modelId") String modelId
          , @ApiParam(value = "Actual Patch Request", required = true) PatchRequest jsonPatch)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.updateModelConfig(clientId, projectId, modelId, jsonPatch);
  }

  @PATCH
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/speech")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "combine SLM model with SSI url", notes = "combine SLM model with SSI url", response = ModelBO.class, tags = {
          "models"})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ModelBO.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = java.lang.Error.class)})
  public Response updateCombinedModelConfig(
          @ApiParam(value = "The id of the client of the project to map dataset to", required = true, example = "159") @PathParam("clientId") String clientId,
          @ApiParam(value = "The project Id", required = true, example = "561") @PathParam("projectId") String projectId,
          @ApiParam(value = "The Modelling Workbench id of the model to patch", required = true, example = "768") @PathParam("modelId") String modelId,
          @ApiParam(value = "DigitalHostedUrl", required = true) @QueryParam("digitalHostedUrl") String digitalHostedUrl)
          throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    ApiParameterSanitizer.sanitize(digitalHostedUrl);
    return modelsApiService.updateCombinedModelConfig(clientId, projectId, modelId, digitalHostedUrl);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models")
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get all models for a project", notes = "", response = TFSModelList.class, tags = {
      "models"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TFSModelList.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project Id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Error.class)})
  public Response getModelsForProject(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true, example = "159") @PathParam("clientId") String clientId,
      @ApiParam(value = "The project Id", required = true, example = "561") @PathParam("projectId") String projectId)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);

    return modelsApiService.getModelsForProject(clientId, projectId);
  }

  @POST
  @Path("/clients/{clientId}/models/{modelId}/build")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Queue this model for building", notes = "", response = void.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted request to build the model", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Error.class)})
  public Response v1ModelsIdBuildPost(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The Modelling Workbench Model id", required = true) @PathParam("modelId") String modelId,
      @Context UriInfo uriInfo,
      @ApiParam(value = "Operation type value", example = "speech") @QueryParam("modelType") String modelType)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.queueModelForBuilding(clientId, modelId, uriInfo, modelType);
  }

  @GET
  @Path("/clients/{clientId}/projects/{projectId}/models/{modelId}/trainingData")
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get training data used for building this model", notes = "", response = File.class, tags = {
      "models",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TFSModelList.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project Id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Error.class)})
  public Response getModelsForProject(
      @ApiParam(value = "The id of the client of the project to map dataset to", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The project Id", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The Modelling Workbench model id", required = true) @PathParam("modelId") String modelId)

      throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);
    ApiParameterSanitizer.sanitize(projectId);
    ApiParameterSanitizer.sanitize(modelId);
    return modelsApiService.getTrainingDataForModel(clientId, projectId, modelId);
  }


}