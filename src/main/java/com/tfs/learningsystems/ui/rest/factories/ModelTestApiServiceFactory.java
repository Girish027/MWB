package com.tfs.learningsystems.ui.rest.factories;

import com.tfs.learningsystems.ui.rest.ModelTestApiService;
import com.tfs.learningsystems.ui.rest.impl.ModelTestApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-02-21T16:16:07.557-08:00")
public class ModelTestApiServiceFactory {

  private final static ModelTestApiService service = new ModelTestApiServiceImpl();

  public static ModelTestApiService getModelTestApi() {
    return service;
  }
}
