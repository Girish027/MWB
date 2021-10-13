package com.tfs.learningsystems.ui.rest.factories;

import com.tfs.learningsystems.ui.rest.ProjectsApiService;
import com.tfs.learningsystems.ui.rest.impl.ProjectsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-10-11T15:06:25.584-04:00")
public class ProjectsApiServiceFactory {

  private final static ProjectsApiService service = new ProjectsApiServiceImpl();

  public static ProjectsApiService getProjectsApi() {
    return service;
  }
}
