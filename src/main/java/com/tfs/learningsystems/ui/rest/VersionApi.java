/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.Version;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/version")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the version API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-07-04T11:21:16.376-04:00")
public class VersionApi {

  private final VersionApiService delegate;

  @Autowired
  public VersionApi(VersionApiService service) {
    this.delegate = service;
  }

  @GET
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List Version information for services",
      notes = "", response = Version.class, tags = {"get",})
  @io.swagger.annotations.ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200,
      message = "An array of strings listing Vertical Markets", response = Version.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
          response = Version.class)})
  public Response getVersion() throws NotFoundException {
    return delegate.getVersion();
  }
}
