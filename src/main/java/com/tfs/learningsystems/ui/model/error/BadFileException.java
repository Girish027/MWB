package com.tfs.learningsystems.ui.model.error;

import com.tfs.learningsystems.ui.model.Error;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Created by huzefa.siyamwala on 4/25/19.
 */
public class BadFileException extends WebApplicationException {

  private static final long serialVersionUID = 4828116606039813211L;

  public BadFileException(String msg) {
    super("Unreadable File or Bad File format", Status.BAD_REQUEST);
  }

  public BadFileException(String user, Throwable t) {
    super("Unreadable File or Bad File format", t, Status.BAD_REQUEST);
  }

  public BadFileException(Error error, Throwable t) {
    super(error.getMessage(), t, Response.status(Status.BAD_REQUEST).entity(error).build());
  }
}
