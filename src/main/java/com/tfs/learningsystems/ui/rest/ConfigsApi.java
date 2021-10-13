package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.ModelConfigBO;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ModelConfigDetail;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import java.io.InputStream;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the configs API")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-10-11T15:06:25.584-04:00")
public class ConfigsApi {

  private final ConfigsApiService configsApiService;

  @Autowired
  public ConfigsApi(final ConfigsApiService configsApiService) {
    this.configsApiService = configsApiService;
  }

  @POST
  @Path("/configs")
  @Consumes({"multipart/form-data"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add a model config archive",
          notes = "",
          response = ModelConfigDetail.class,
          tags = {"post",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 201,
                  message = "Config File succesfully added",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Invalid request",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 401,
                  message = "User not authenticated",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response addConfig(
          @FormDataParam("name") String name,
          @FormDataParam("description") String description,
          @FormDataParam("projectId") String projectId,
          @FormDataParam("cid") String cid,
          @FormDataParam("file") InputStream fileInputStream,
          @Context UriInfo uriInfo) throws NotFoundException {

    return configsApiService
            .addConfig(name, cid, description, projectId, fileInputStream, uriInfo);
  }

  @POST
  @Path("/configs")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add a model config archive",
          notes = "",
          response = ModelConfigBO.class,
          tags = {"post",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 201,
                  message = "Config File succesfully added",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Invalid request",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 401,
                  message = "User not authenticated",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response addConfig(
          @ApiParam(value = "The Model Config object", required = true) @Valid ModelConfigBO config,
          @Context UriInfo uriInfo) throws NotFoundException {
    return configsApiService.addConfig(config, uriInfo);
  }

  @GET
  @Path("/configs/{configId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get Config Metadata",
          notes = "",
          response = ModelConfigBO.class,
          tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "Expected response to a valid request",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response getConfigById(
          @ApiParam(value = "The id of the config to retrieve", required = true)
          @PathParam("configId") String configId) throws NotFoundException {
    return configsApiService.getConfigById(configId);
  }

  @GET
  @Path("/clients/{clientId}/configs/{configId}/data")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get Config Metadata",
          notes = "",
          response = ModelConfigBO.class,
          tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "Expected response to a valid request",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response getConfigDataById(
          @ApiParam(value = "The id of the config to retrieve", required = true)
          @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the config to retrieve", required = true)
          @PathParam("configId") String configId) throws NotFoundException {

    ApiParameterSanitizer.sanitize(clientId);

    ApiParameterSanitizer.sanitize(configId);

    return configsApiService.getConfigDataById(clientId, configId);
  }

  @GET
  @Path("/clients/{clientId}/configs/{configId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get model config",
          notes = "",
          response = ModelConfigBO.class,
          tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "Expected response to a valid request",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response getSpeechConfig(
          @ApiParam(value = "The id of the client", required = true)
          @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the config to retrieve", required = true)
          @PathParam("configId") String configId,
          @Context UriInfo uriInfo) throws NotFoundException {
    return configsApiService.getWordClassFromConfig(clientId, configId, uriInfo);
  }


  @GET
  @Path("/configs/{configId}/download")
  @Consumes({"application/json"})
  @Produces({"application/zip", "application/octet-stream"})
  @io.swagger.annotations.ApiOperation(value = "Download config archive",
          notes = "", response = void.class,
          tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "Expected response to a valid request",
                  response = void.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = void.class)})
  public Response getConfigFilesById(
          @ApiParam(value = "The id of the config file to retrieve", required = true)
          @PathParam("configId") String configId) throws NotFoundException {
    return configsApiService.getConfigFilesById(configId);
  }

  @GET
  @Path("/configs")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all configs", notes = "",
          response = ModelConfigBO.class,
          tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200,
                  message = "An paged array of Configs",
                  response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error",
                  response = ModelConfigBO.class)})
  public Response listConfigs(
          @ApiParam(value = "How many items to return at one time (max 100)",
                  defaultValue = "25") @DefaultValue("25")
          @QueryParam("limit") @Min(0) @Max(100) Integer limit,
          @ApiParam(value = "starting index", defaultValue = "0")
          @DefaultValue("0") @QueryParam("startIndex") Integer startIndex) throws NotFoundException {
    return configsApiService.listConfigs(limit, startIndex);
  }

  @PATCH
  @Path("/configs/{id}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a model configuration", notes = "Modifies one or more attributes of a model configuration", response = ModelConfigBO.class, tags = {
          "patch",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ModelConfigBO.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid request", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response updateModelConfig(
          @ApiParam(value = "The id of the config to patch", required = true) @PathParam("id") String id
          , @ApiParam(value = "", required = true) PatchRequest jsonPatch)
          throws NotFoundException {
    return configsApiService.patchConfig(id, jsonPatch);
  }
}