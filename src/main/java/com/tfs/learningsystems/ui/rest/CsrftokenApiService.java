/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-23T13:26:33.448-08:00")
public abstract class CsrftokenApiService {

  public abstract Response getCSRFToken(HttpServletRequest request) throws NotFoundException;
}
