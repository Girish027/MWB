/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. 
 * All Rights Reserved. 
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-12T17:44:35.590-04:00")
public class ApiException extends Exception {

  private int code;

  public ApiException(int code, String msg) {
    super(msg);
    this.code = code;
  }
}
