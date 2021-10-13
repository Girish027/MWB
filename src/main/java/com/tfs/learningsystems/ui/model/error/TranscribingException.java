package com.tfs.learningsystems.ui.model.error;

import com.tfs.learningsystems.ui.model.Error;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Created by huzefa.siyamwala on 5/15/19.
 */
public class TranscribingException extends WebApplicationException  {
  private static final long serialVersionUID = 4828116606039813212L;

  public TranscribingException(String msg) {
    super("Unable to transcribe audio", Status.BAD_REQUEST);
  }

  public TranscribingException(String user, Throwable t) {
    super("Unable to transcribe audio", t, Status.BAD_REQUEST);
  }

  public TranscribingException(Error error, Throwable t) {
    super(error.getMessage(), t, Response.status(Status.BAD_REQUEST).entity(error).build());
  }

}
