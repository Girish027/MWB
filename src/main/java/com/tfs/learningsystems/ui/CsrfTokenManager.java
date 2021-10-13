/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CsrfTokenManager {

  public final static String TOKEN_HEADER_NAME = "X-CSRF-TOKEN";

  public final static String TOKEN_PARAMETER_NAME = "_tk";

  public final static String TOKEN_ATTRIBUTE_NAME = "csrfToken";

  public final static List<String> METHODS_TO_CHECK = Collections.unmodifiableList(
      Arrays.asList("POST", "PUT", "DELETE", "PATCH"));

  public String generateAndSaveToken(HttpServletRequest request, HttpServletResponse response);

  public String getTokenFromSession(final HttpServletRequest request);

  public boolean validateToken(final HttpServletRequest request);
}
