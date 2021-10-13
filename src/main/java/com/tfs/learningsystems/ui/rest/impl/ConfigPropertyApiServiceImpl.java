/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.rest.ConfigPropertyApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2018-07-02T14:57:41.657-07:00")
@Slf4j
public class ConfigPropertyApiServiceImpl extends ConfigPropertyApiService {

  private static final List<String> BLACKLIST_CONFIG_PROPERTIES = Arrays
      .asList("locationClassesFilename");
  @Qualifier("appConfig")
  @Autowired
  private AppConfig appConfig;

  @Override
  public Response getProjectConfigProperty(String name)
      throws NotFoundException {
    Map<String, String> response = new HashMap<>();
    //Validate input params
    if (name == null || name.isEmpty()) {
      response
          .put("error", String.format("Properties list to retrieve is empty or invalid!!", name));
      return Response.status(Status.BAD_REQUEST).entity(response).build();
    }

    List<String> propertiesList = Arrays.asList(name.split(","));
    Map<String, Object> result = appConfig.toPropertiesMap();
    for (String prop : propertiesList) {
      if (!result.containsKey(prop) || BLACKLIST_CONFIG_PROPERTIES.contains(prop)) {
        response.clear();
        response.put("error", String.format("Property %s not found in config properties", prop));
        return Response.status(Status.BAD_REQUEST).entity(response).build();
      } else {
        response.put(prop, result.get(prop).toString());
      }
    }
    return Response.ok().entity(response).build();
  }

  @Override
  public Response getCurrentUserRoles() throws NotFoundException {
    Map<String, String> response = new HashMap<>();
    List<String> roles = ActionContext.getRoles();
    for (String one : roles) {
      List<String> restrictions = new LinkedList<>();
      if (Constants.ROLE_BASED_DENY_MAP.containsKey(one)) {
        restrictions = Constants.ROLE_BASED_DENY_MAP.get(one);
      }
      if(restrictions.size() > 0){
        response.put(one, String.join(",", AuthUtil.getAllRestrictedURIList(restrictions)));
      }
      else {
        response.put(one, Constants.NO_RESTRICTIONS_ROLE);
      }
    }
    return Response.ok().entity(response).build();
  }

}
