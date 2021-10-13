/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.ui.model.TFSModelTagDetails;
import com.tfs.learningsystems.ui.nlmodel.model.TFSProjectModelList;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the client deployment API")
public class DeploymentApi {

  private final DeploymentApiService deploymentApiService;

  @Autowired
  public DeploymentApi(DeploymentApiService service) {
    deploymentApiService = service;
  }

  @POST
  @Path("/clients/{clientId}/tags/{tagName}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Publish Model files", notes = "", response = TFSProjectModelList.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TFSProjectModelList.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid client / tag", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response publishModel(
      @ApiParam(value = "The id of the client project models to publish ", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "Tag to use in github  tagging and package creation") @DefaultValue("") @PathParam("tagName") String tagName,
      @ApiParam(value = "", required = true) List<ProjectModelRequest> projectModels)

      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    Response response = deploymentApiService.publishModel(clientId, projectModels, tagName);
    return response;
  }


  @GET
  @Path("/clients/{clientId}/tags")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get Client's all tag details", notes = "", response = TFSModelTagDetails.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TFSModelTagDetails.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid client", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response getClientTags(
      @ApiParam(value = "The id of the client to get all the tag details of ", required = true) @PathParam("clientId") String clientId)

      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);

    Response response = deploymentApiService.getClientTags(clientId);

    return response;
  }


  @GET
  @Path("/clients/{clientId}/tags/{tagName}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get Client's tag details", notes = "", response = TFSProjectModelList.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TFSProjectModelList.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid client / tag ", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response getClientTag(
      @ApiParam(value = "The id of the client to get the tag", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "Tag name of the tag that needs to be fetched for the client ") @DefaultValue("") @PathParam("tagName") String tagName)

      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);

    Response response = deploymentApiService.getClientTag(clientId, tagName);

    return response;
  }


}