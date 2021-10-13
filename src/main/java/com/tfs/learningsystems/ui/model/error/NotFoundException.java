/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model.error;

import com.tfs.learningsystems.ui.model.Error;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author jkarpala
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Client Not Found")  // 404
public class NotFoundException extends WebApplicationException {

  public NotFoundException(Error error) {
    super(error.getMessage(), Response.status(Response.Status.NOT_FOUND).entity(error).build());
  }
}
