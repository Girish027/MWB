/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.exceptions;

public class InvalidCSVFileException extends ApplicationException {

  private static final long serialVersionUID = -7500079906110501442L;

  /**
   *
   */

  public InvalidCSVFileException() {
  }

  public InvalidCSVFileException(String message) {
    super(message);
  }

  public InvalidCSVFileException(Throwable cause) {
    super(cause);
  }

  public InvalidCSVFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidCSVFileException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
