/*******************************************************************************
 * Copyright © [24]7 Customer, Inc.
 * All Rights Reserved.
 ******************************************************************************/

package com.tfs.learningsystems.config;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JerseyServletRegistrationConfig {

  @Autowired
  private ApplicationContext appCtx;


  @Bean
  public ServletRegistrationBean privateJersey() {
    ServletRegistrationBean privateJersey = new ServletRegistrationBean(
        new ServletContainer(this.register()));
    privateJersey.addUrlMappings("/nltools/private/*");
    privateJersey.setName("PrivateJersey");
    privateJersey.setLoadOnStartup(0);
    return privateJersey;
  }

  @Bean
  @Conditional(value = ProductionDeploymentCondition.class)
  public ServletRegistrationBean publicJersey() {
    ServletRegistrationBean publicJersey = new ServletRegistrationBean(
        new ServletContainer(this.register()));
    publicJersey.addUrlMappings("/nltools/public/*");
    publicJersey.setName("PublicJersey");
    publicJersey.setLoadOnStartup(1);
    return publicJersey;
  }

  private ResourceConfig register() {
    ResourceConfig config = this.getResourceConfig();
    log.info("Jersey resource classes found:");
    appCtx.getBeansWithAnnotation(Path.class)
        .forEach((name, resource) -> {
          log.info(" -> {}", resource.getClass().getName());
          config.register(resource);
        });
    appCtx.getBeansWithAnnotation(Provider.class)
        .forEach((name, resource) -> {
          log.info(" -> {}", resource.getClass().getName());
          config.register(resource);
        });
    return config;
  }

  private ResourceConfig getResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.register(new NLToolsBinder());
    config.register(MultiPartFeature.class);
        /*config.register(new LoggingFeature(
                java.util.logging.Logger.getLogger(
                        LoggingFeature.DEFAULT_LOGGER_NAME),
                Level.FINE,
                LoggingFeature.Verbosity.PAYLOAD_TEXT,
                null
                ));*/
    return config;
  }

}
