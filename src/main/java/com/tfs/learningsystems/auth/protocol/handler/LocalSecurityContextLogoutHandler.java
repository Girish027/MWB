/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.auth.protocol.handler;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalSecurityContextLogoutHandler extends SecurityContextLogoutHandler {

  @Value("${spring.profiles.active}")
  private String activeProfile;

  @Value("${tfs.okta.url}")
  private String oktaUrl;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    super.logout(request, response, authentication);

    try {

      log.info("Logging out!! Active Profile: {}", activeProfile);
      response.sendRedirect(oktaUrl);

    } catch (IOException e) {
      log.info("Error during logging off {}", e);
    }
  }

}
