/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
@Qualifier("csrfTokenManager")
@Slf4j
public class CsrfTokenManagerImpl implements CsrfTokenManager {

  private CsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();

  @Override
  public String generateAndSaveToken(HttpServletRequest request, HttpServletResponse response) {
    CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
    log.info("Generated new CSRF Token with header: {}, parameter: {}, value {}",
        csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken());
    this.csrfTokenRepository.saveToken(csrfToken, request, response);
    return csrfToken.getToken();
  }

  @Override
  public String getTokenFromSession(final HttpServletRequest request) {
    String token = null;
    CsrfToken csrfToken = this.csrfTokenRepository.loadToken(request);
    if (csrfToken != null) {
      token = csrfToken.getToken();
    } else {
      log.warn("csrf token for the request is null");
    }

    return token;
  }

  @Override
  public boolean validateToken(final HttpServletRequest request) {
    boolean valid = false;

    String tokenFromHeader = request.getHeader(TOKEN_HEADER_NAME);
    String tokenFromSession = this.getTokenFromSession(request);
    if (tokenFromHeader != null && tokenFromSession != null && tokenFromHeader
        .equals(tokenFromSession)) {
      valid = true;
    } else {
      log.warn("Token not valid, headerToken {} sessionToken {}",
          tokenFromHeader, tokenFromSession);
    }

    return valid;
  }

}
