/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/

package com.tfs.learningsystems.ui;

import java.io.IOException;
import java.util.ArrayList;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.util.UriComponentsBuilder;

public interface APICallManager {


  public HttpHeaders createAuthHeaders(String username, String password);


  public ResponseEntity<String> restTemplatePostCall(HttpHeaders headers, String url,
      LinkedMultiValueMap<String, Object> map, ResponseErrorHandler responseErrorHandler);

  public ResponseEntity<String> restTemplateGetCall(HttpHeaders headers,
      UriComponentsBuilder builder, ResponseErrorHandler responseErrorHandler);

  public HttpHeaders fillHeadersDetails();


  ArrayList reponseStatusCheck(ResponseEntity<String> response, Class<?> cls,
      HttpStatus httpStatus, String responseBodyParam, String errorMessage, String url)
      throws IOException, ClassNotFoundException;

  public void checkHttpReturnStatus(ResponseEntity<String> response, HttpStatus httpStatus,
      String errorMessage, String url);


}
