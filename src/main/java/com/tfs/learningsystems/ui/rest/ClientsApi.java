/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;


import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.ClientsDetail;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import javax.validation.Valid;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the clients API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-21T13:18:07.066-04:00")
public class ClientsApi {

  private final ClientsApiService clientsApiService;

  @Autowired
  public ClientsApi(ClientsApiService service) {
    clientsApiService = service;
  }

  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/clients")
  @io.swagger.annotations.ApiOperation(value = "Create a client", notes = "",
      response = ClientDetail.class, tags = {"clients"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 201,
      message = "Client succesfully created", response = ClientDetail.class),
      @io.swagger.annotations.ApiResponse(code = 400, message = "Client could not be created",
          response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class)})
  public Response createClient(@ApiParam(value = "Client object") @Valid Client client,
      @Context UriInfo uriInfo) throws NotFoundException {
    return clientsApiService.createClient(client, uriInfo);
  }


  @DELETE
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/clients/{clientId}")
  @io.swagger.annotations.ApiOperation(value = "Delete a client", notes = "",
      response = ClientDetail.class, tags = {"clients"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 201,
      message = "Client succesfully created", response = ClientDetail.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Client could not be deleted",
          response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class)})

  public Response deleteClient(
      @ApiParam(value = "The id of the client to retrieve",
          required = true, example = "159") @PathParam("clientId") String clientId) throws NotFoundException {

    return clientsApiService.deleteClientById(clientId);
  }


  @PATCH
  @Path("/clients/{clientId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies a client",
      notes = "Modifies an attribute of a client without required parameters",
      response = ClientDetail.class, tags = {"clients"})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200,
          message = "OK", response = ClientDetail.class),
      @io.swagger.annotations.ApiResponse(code = 401,
          message = "Unauthorized to call the API", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 400,
          message = "Client could not be modified", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 409,
          message = "Conflict (Client name already exists)", response = Error.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class)})
  public Response patchClientById(
      @ApiParam(value = "The id of the client", required = true, example = "153") @PathParam("clientId") String clientId,
      @ApiParam(value = "Patch request in JSON", example = "[{\"op\": \"REPLACE\",\"path\": \"/description\",\"value\": \"use this description\"}]", required = true) @Valid PatchRequest jsonPatch)
      throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    return clientsApiService.patchClientById(clientId, jsonPatch);

  }

  @GET
  @Path("/clients/{clientId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Info for a specific client", notes = "",
      response = ClientDetail.class, tags = {"clients"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "Expected response to a valid request", response = ClientDetail.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class)})
  public Response getClientById(
      @ApiParam(value = "The id of the client to retrieve",
          required = true, example = "153") @PathParam("clientId") String clientId) throws NotFoundException {
    ApiParameterSanitizer.sanitize(clientId);
    return clientsApiService.getClientById(clientId);
  }

  @GET
  @Path("/clients")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all clients", notes = "",
      response = ClientsDetail.class, tags = {"clients"})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "An paged array of clients", response = ClientsDetail.class),
      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Error.class)})
  public Response listClients(
      @ApiParam(
          value = "How many items to return at one time (max 1000)",
          defaultValue = "1000") @DefaultValue("1000") @QueryParam("limit") @Min(0) @Max(1000) Integer limit,
      @ApiParam(value = "starting index",
          defaultValue = "0", example ="0") @DefaultValue("0") @QueryParam("startIndex") @Min(0) Integer startIndex,
      @ApiParam(value = "enable vertical filtering",
          defaultValue = "false") @QueryParam("showVerticals") Boolean showVerticals,
      @ApiParam(value = "show disabled",
          defaultValue = "false") @DefaultValue("false") @QueryParam("showDeleted") boolean showDeleted,
      @Context UriInfo uriInfo) throws NotFoundException {

    return clientsApiService.listClients(limit, startIndex, showVerticals, showDeleted, uriInfo);

  }

}