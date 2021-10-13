/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.ui.model.DataType;
import com.tfs.learningsystems.ui.model.Locale;
import com.tfs.learningsystems.ui.model.Vertical;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.ResourcesApiService;
import javax.ws.rs.core.Response;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-14T12:54:01.338-05:00")
public class ResourcesApiServiceImpl extends ResourcesApiService {

  @Override
  public Response listVerticals() throws NotFoundException {
    return Response.ok().entity(Vertical.values()).build();
  }

  @Override
  public Response listDataTypes() throws NotFoundException {
    return Response.ok().entity(DataType.values()).build();
  }

  @Override
  public Response listLocales() throws NotFoundException {
    return Response.ok().entity(Locale.values()).build();
  }

}
