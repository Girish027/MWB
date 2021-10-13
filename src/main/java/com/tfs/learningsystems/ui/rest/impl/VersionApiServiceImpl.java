/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.ActuatorInfo;
import com.tfs.learningsystems.ui.model.ActuatorInfo.Build;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.VersionApiService;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-07-04T11:21:16.376-04:00")
@Slf4j
public class VersionApiServiceImpl extends VersionApiService {

  @Autowired
  ServletContext servletContext;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private AppConfig appConfig;

  @Override
  public Response getVersion() throws NotFoundException {

    try {
      Map<String, Build> response = new HashMap<>();
      Properties props = new Properties();
      Resource resource = new ClassPathResource("/META-INF/build-info.properties");
      InputStream inputStream = resource.getInputStream();
      props.load(inputStream);
      String version = props.get("build.version").toString();
      String artifact = props.get("build.artifact").toString();
      String name = props.get("build.name").toString();
      String revision = props.get("build.revision").toString();
      String group = props.get("build.group").toString();
      long time = Long.valueOf(props.get("build.buildDate").toString());
      String buildDate = props.get("build.buildDate").toString();
      Build worbenchBuild =
          new Build(version, artifact, name, revision, group, time, buildDate);
      response.put("ui", worbenchBuild);
      try {
        URI url = new URI(this.appConfig.getAutoCategorizerUrl());
        ResponseEntity<ActuatorInfo> responseEntity =
            restTemplate.getForEntity(url.resolve("/info"), ActuatorInfo.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
          ActuatorInfo body = responseEntity.getBody();
          Build catBuild = body.build;
          response.put("auto-categorizer-web", catBuild);
        } else {
          log.warn("Failed to get Categorizer version info - service response {}",
              responseEntity.getStatusCode().name());
        }

      } catch (Exception e) {
        log.warn("Failed to get Categorizer version info", e);
      }

      return Response.ok().entity(response).build();
    } catch (Exception e) {
      log.error("Failed to get version ", e);
      throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR);
    }

  }
}
