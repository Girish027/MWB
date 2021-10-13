package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.ConfigProperty;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/v1")
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the getProperty API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-07-02T14:57:41.657-07:00")
public class ConfigPropertyApi {

  private final ConfigPropertyApiService delegate;

  @Autowired
  public ConfigPropertyApi(ConfigPropertyApiService configPropertyApiService) {
    delegate = configPropertyApiService;
  }

  @GET
  @Path("/configProperties")
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get the project configuration property", notes = "", response = ConfigProperty.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Configuration property", response = ConfigProperty.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = ConfigProperty.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ConfigProperty.class)})
  public Response getProjectConfigProperty(@ApiParam(value = "The property name to retrieve",
      required = true) @QueryParam("propertyName") String propertyName)
      throws NotFoundException {
    return delegate.getProjectConfigProperty(propertyName);
  }

  @GET
  @Produces({"application/json"})
  @Path("/userGroups")
  @io.swagger.annotations.ApiOperation(value = "Get user roles", notes = "", response = ConfigProperty.class, tags = {
      "get",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "User Roles", response = ConfigProperty.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = ConfigProperty.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ConfigProperty.class)})
  public Response getUserRoles()
      throws NotFoundException {
    return delegate.getCurrentUserRoles();
  }
}
