/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.exceptions;

public class EmptyCSVFileException extends ApplicationException {

  /**
   *
   */
  private static final long serialVersionUID = -623409250563957638L;

  public EmptyCSVFileException() {
  }

  public EmptyCSVFileException(String message) {
    super(message);
  }

  public EmptyCSVFileException(Throwable cause) {
    super(cause);
  }

  public EmptyCSVFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmptyCSVFileException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
