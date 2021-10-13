package com.tfs.learningsystems.ui.rest.factories;

import com.tfs.learningsystems.ui.rest.ConfigsApiService;
import com.tfs.learningsystems.ui.rest.impl.ConfigsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-10-11T14:48:26.319-04:00")
public class ConfigsApiServiceFactory {

  private final static ConfigsApiService service = new ConfigsApiServiceImpl();

  public static ConfigsApiService getConfigsApi() {
    return service;
  }
}
