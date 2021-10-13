package com.tfs.learningsystems.ui.rest.factories;

import com.tfs.learningsystems.ui.rest.ModelsApiService;
import com.tfs.learningsystems.ui.rest.impl.ModelsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-14T23:59:55.888-07:00")
public class ModelsApiServiceFactory {

  private final static ModelsApiService service = new ModelsApiServiceImpl();

  public static ModelsApiService getModelsApi() {
    return service;
  }
}
