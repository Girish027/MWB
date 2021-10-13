package com.tfs.learningsystems.ui.rest.factories;

import com.tfs.learningsystems.ui.rest.ConfigPropertyApiService;
import com.tfs.learningsystems.ui.rest.impl.ConfigPropertyApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-07-02T14:57:41.657-07:00")
public class ConfigPropertyApiServiceFactory {

  private final static ConfigPropertyApiService configPropService = new ConfigPropertyApiServiceImpl();

  public static ConfigPropertyApiService getConfigPropertyApi() {
    return configPropService;
  }
}
