/*******************************************************************************
 * Copyright © [24]7 Customer, Inc.
 * All Rights Reserved.
 ******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Provider
@Slf4j
public class CorsFilter implements ContainerResponseFilter {

  //TODO: Generalize this code. this is to specific for this invocation that is required from swagger UI
  // Use the code in https://github.com/resteasy/Resteasy/blob/master/resteasy-jaxrs/src/main/java/org/jboss/resteasy/plugins/interceptors/CorsFilter.java
  // to imprve
  @Override
  public void filter(ContainerRequestContext request,
      ContainerResponseContext response) throws IOException {
    log.debug("in Jersey filter {}", request.getHeaders());
    response.getHeaders().add("Access-Control-Allow-Origin", "http://127.0.0.1:50519");
    response.getHeaders().add("Access-Control-Allow-Headers",
        "origin, content-type, accept, authorization");
    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
    response.getHeaders().add("Access-Control-Allow-Methods",
        "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
  }
}
