/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/resources")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the resources API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-14T12:57:20.434-05:00")
public class ResourcesApi {

  private final ResourcesApiService delegate;

  @Autowired
  public ResourcesApi(ResourcesApiService service) {
    this.delegate = service;
  }

  @GET
  @Path("/datatypes")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all Data Set types", notes = "", response = String.class, responseContainer = "List", tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An array of strings listing Data Set Types", response = String.class, responseContainer = "List"),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = String.class, responseContainer = "List")})
  public Response listDataTypes()
      throws NotFoundException {
    return delegate.listDataTypes();
  }

  @GET
  @Path("/verticals")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all Vertical Markets", notes = "", response = String.class, responseContainer = "List", tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An array of strings listing Vertical Markets", response = String.class, responseContainer = "List"),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = String.class, responseContainer = "List")})
  public Response listVerticals()
      throws NotFoundException {
    return delegate.listVerticals();
  }

  @GET
  @Path("/locales")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all supported locales", notes = "", response = String.class, responseContainer = "List", tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "An array of strings listing locales", response = String.class, responseContainer = "List"),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = String.class, responseContainer = "List")})
  public Response listLanguages()
      throws NotFoundException {
    return delegate.listLocales();
  }
}
