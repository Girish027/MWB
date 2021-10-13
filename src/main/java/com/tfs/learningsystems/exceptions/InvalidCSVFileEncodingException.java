/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.exceptions;

public class InvalidCSVFileEncodingException extends ApplicationException {

  /**
   *
   */
  private static final long serialVersionUID = 1922593357601142602L;

  public InvalidCSVFileEncodingException() {
  }

  public InvalidCSVFileEncodingException(String message) {
    super(message);
  }

  public InvalidCSVFileEncodingException(Throwable cause) {
    super(cause);
  }

  public InvalidCSVFileEncodingException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidCSVFileEncodingException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
