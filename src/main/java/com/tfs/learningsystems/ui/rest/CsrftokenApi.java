/*******************************************************************************
 * Copyright © [24]7 Customer, Inc.
 * All Rights Reserved.
 ******************************************************************************/

package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.CSRFToken;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/csrftoken")
@Produces({MediaType.APPLICATION_JSON})
@io.swagger.annotations.Api(description = "the csrftoken API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-23T13:26:33.448-08:00")
public class CsrftokenApi {

  private final CsrftokenApiService delegate;

  @Autowired
  public CsrftokenApi(CsrftokenApiService service) {
    delegate = service;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @io.swagger.annotations.ApiOperation(value = "Get the CSRF token for the session", notes = "", response = CSRFToken.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "The CSRF token for the session", response = CSRFToken.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = CSRFToken.class)})
  public Response getCSRFToken(
      @Context HttpServletRequest request)
      throws NotFoundException {
    return delegate.getCSRFToken(request);
  }
}
