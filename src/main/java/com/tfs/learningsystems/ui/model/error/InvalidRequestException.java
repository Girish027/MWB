/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.model.error;

import com.tfs.learningsystems.ui.model.Error;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid Rquest")  // 400
public class InvalidRequestException extends WebApplicationException {

  private static final long serialVersionUID = 1348265038506496422L;

  public InvalidRequestException(Error error) {
    super(error.getMessage(), Response.status(Response.Status.BAD_REQUEST).entity(error).build());
  }
}
