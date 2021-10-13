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
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Object already exists") // 409
public class AlreadyExistsException extends WebApplicationException {

  private static final long serialVersionUID = 4828116606039813211L;

  public AlreadyExistsException(String message) {
    super(message, Response.status(Response.Status.CONFLICT).entity(message).build());
  }

  public AlreadyExistsException(Error error) {
    super(error.getMessage(), Response.status(Response.Status.CONFLICT).entity(error).build());
  }

  public AlreadyExistsException(Error error, Throwable t) {
    super(error.getMessage(), t, Response.status(Response.Status.CONFLICT).entity(error).build());
  }
}
