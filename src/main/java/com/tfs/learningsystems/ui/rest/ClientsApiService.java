/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.PatchRequest;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-21T13:18:07.066-04:00")
public abstract class ClientsApiService {

  public abstract Response createClient(Client client, UriInfo uriInfo)
      throws NotFoundException;

  public abstract Response deleteClientById(String clientId)
      throws NotFoundException;

  public abstract Response getClientById(String clientId)
      throws NotFoundException;

  public abstract Response listClients(Integer limit, Integer startIndex, Boolean showVerticals,
      boolean showDeleted,
      UriInfo uriInfo) ;

  public abstract Response patchClientById(String clientId, PatchRequest jsonPatch)
      throws NotFoundException;


}