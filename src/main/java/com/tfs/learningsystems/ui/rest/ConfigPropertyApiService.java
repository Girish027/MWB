package com.tfs.learningsystems.ui.rest;

import javax.ws.rs.core.Response;

/*
    This class will provide REST API implementation for retrieving config properties from our
    application.properties file. Implementation will inject Config manager by which all the properties
    can be retrieved.
    Right now it doesnt support PATCH(for updating config properties) or DELETE for deleteing some
    config properties
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-07-02T14:57:41.657-07:00")
public abstract class ConfigPropertyApiService {

  // this method will retrieve single/multiple config property seperated by comma.
  public abstract Response getProjectConfigProperty(String PropertyName) throws NotFoundException;

  public abstract Response getCurrentUserRoles() throws NotFoundException;
}
