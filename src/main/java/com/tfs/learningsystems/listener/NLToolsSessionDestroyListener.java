/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.listener;

import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NLToolsSessionDestroyListener implements ApplicationListener<ApplicationEvent> {

  @Autowired
  HttpSession httpSession;

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof HttpSessionCreatedEvent) { //If event is a session created event
      log.debug("Session is created");
    } else if (event instanceof HttpSessionDestroyedEvent) { //If event is a session destroy event
      log.debug("Session is destory for: {}", httpSession.getAttribute("username"));
    } else if (event instanceof AuthenticationSuccessEvent) { //If event is a session destroy event
      log.debug("Athentication is success  :"); //log data
    } else {
      log.debug("Unknown event");
    }
  }
}
