/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.listener;

import com.tfs.learningsystems.config.AppConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NLToolsSessionListener implements HttpSessionListener {

  @Autowired
  private AppConfig appConfig;

  private String getUsernameFromSecurityContext(HttpSessionEvent se) {
    String username = null;
    HttpSession httpSession = se.getSession();
    SecurityContext securityContext = (SecurityContextImpl) httpSession
        .getAttribute("SPRING_SECURITY_CONTEXT");
    if (securityContext != null) {
      Authentication authentication = securityContext.getAuthentication();
      if (authentication != null) {
        username = (String) authentication.getPrincipal();
      } else {
        log.warn("[NLToolsSessionListener] Authentication is null");
      }
    } else {
      log.warn("[NLToolsSessionListener] Security context is not set.");
    }

    return username;
  }

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    log.debug("[NLToolsSessionListener] Session is created");
    se.getSession().setMaxInactiveInterval(appConfig.getMaxPerUserSessions());
    this.getUsernameFromSecurityContext(se);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    String username = this.getUsernameFromSecurityContext(se);
    log.debug("[NLToolsSessionListener] Session is being destroyed for user:", username);
  }

}
