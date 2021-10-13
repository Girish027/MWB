/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. 
 * All Rights Reserved. 
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class Bootstrap extends HttpServlet {

  @Override
  public void init(ServletConfig config) throws ServletException {
    Info info = new Info().title("Swagger Server")
        .description("User APIs and their descriptions").termsOfService("")
        .contact(new Contact().email("")).license(new License().name("").url(""));

    //ServletContext context = config.getServletContext();
    Swagger swagger = new Swagger().info(info);

    new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
  }
}
