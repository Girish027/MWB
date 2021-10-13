package com.tfs.learningsystems.util;

import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;

@Slf4j
public class ErrorUtil {

  /**
   * Creates an error with http code and message
   */
  public static Error createError(int httpCode, String message) {
    Error error = new Error();
    error.setCode(httpCode);
    error.setMessage(message);
    return error;
  }

  /**
   * Creates an error with specific errorcode along with http code and message
   */
  public static Error createError(int httpCode, String errorCode, String message) {
    Error error = new Error();
    error.setCode(httpCode);
    error.setErrorCode(errorCode);
    error.setMessage(message);
    return error;
  }

  /**
   * Creates an error when a model/dataset/project etc is not found
   */
  public static Error notFoundError(int httpCode, String missingItem, String id) {
    Error error = new Error();
    error.setCode(httpCode);
    error.setErrorCode(missingItem + "_not_found");
    error.setMessage(missingItem + " '" + id + "' not found");
    return error;
  }

  public static void throwInvalidRequestException(String errorCode, String message){
    throw new InvalidRequestException(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
            errorCode, message));
  }
}
