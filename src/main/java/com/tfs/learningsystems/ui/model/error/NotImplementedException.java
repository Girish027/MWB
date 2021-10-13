/**
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 */
/**
 *
 */
package com.tfs.learningsystems.ui.model.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author jkarpala
 */
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED, reason = "Service Not Implemented")  // 501
public class NotImplementedException extends WebApplicationException {

  private static final long serialVersionUID = 4828116606039813211L;

  public NotImplementedException(String message) {

    super(message, Response.Status.NOT_IMPLEMENTED);
  }
}
