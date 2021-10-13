/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-14T12:57:20.434-05:00")
public abstract class ResourcesApiService {

  public abstract Response listDataTypes() throws NotFoundException;

  public abstract Response listVerticals() throws NotFoundException;

  public abstract Response listLocales() throws NotFoundException;
}
