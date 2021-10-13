/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.ui.CsrfTokenManager;
import com.tfs.learningsystems.ui.model.CSRFToken;
import com.tfs.learningsystems.ui.rest.CsrftokenApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-23T13:26:33.448-08:00")
public class CsrftokenApiServiceImpl extends CsrftokenApiService {

  @Autowired
  @Qualifier("csrfTokenManager")
  CsrfTokenManager csrfTokenManager;

  @Override
  public Response getCSRFToken(
      HttpServletRequest request) throws NotFoundException {
    String token = csrfTokenManager.getTokenFromSession(request);
    CSRFToken csrfToken = new CSRFToken();
    csrfToken.setValue(token);
    return Response.ok(csrfToken).build();
  }
}
