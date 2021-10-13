/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.ClientsDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.rest.ClientsApiService;
import com.tfs.learningsystems.util.CommonUtils;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-10-21T13:18:07.066-04:00")
@Slf4j
public class ClientsApiServiceImpl extends ClientsApiService {


  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;


  @Override
  public Response createClient(Client client, UriInfo uriInfo)
      throws NotFoundException {

    ClientDetail createdClient = clientManager.createClient(client);

    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + createdClient.getId()).build();
    return Response.created(locationURI).entity(createdClient).build();

  }

  @Override
  public Response deleteClientById(String clientId)
      throws NotFoundException {

    ClientBO client = clientManager.deleteClient(clientId);
    MwbItsClientMapBO mwbItsClientMapBO = clientManager.getITSClientByClientId(clientId);

    ClientDetail clientDetail = new ClientDetail();

    if (client == null || mwbItsClientMapBO == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("client_not_found");
      error.setMessage("Client '" + clientId + "' not found");
      throw new NotFoundException(error);
    }

    try {
      BeanUtils.copyProperties(clientDetail, mwbItsClientMapBO);
      BeanUtils.copyProperties(clientDetail, client);

    } catch (IllegalAccessException iae) {
      String message = String.format("Error retriving client details : %s", iae.getMessage());
      log.error(message);
      throw new ApplicationException(message, iae);
    } catch (InvocationTargetException ite) {
      String message = String.format("Error creating client details : %s", ite.getMessage());
      log.error(message);
      throw new ApplicationException(message, ite);
    }

    return Response.ok().build();
  }

  @Override
  public Response getClientById(String clientId)
      throws NotFoundException {
    ClientBO client = clientManager.getClientById(clientId);

    MwbItsClientMapBO mwbItsClientMapBO = clientManager.getITSClientByClientId(clientId);

    if (client == null || mwbItsClientMapBO == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("client_not_found");
      error.setMessage("Client '" + clientId + "' not found");
      throw new NotFoundException(error);
    }

    ClientDetail clientDetail = new ClientDetail();

    try {
      BeanUtils.copyProperties(clientDetail, mwbItsClientMapBO);
      BeanUtils.copyProperties(clientDetail, client);

    } catch (IllegalAccessException iae) {
      String message = String.format("Error retriving client details : %s", iae.getMessage());
      log.error(message);
      throw new ApplicationException(message, iae);
    } catch (InvocationTargetException ite) {
      String message = String.format("Error creating client details : %s", ite.getMessage());
      log.error(message);
      throw new ApplicationException(message, ite);
    }

    return Response.ok(clientDetail).build();
  }

  @Override
  public Response listClients(Integer limit, Integer startIndex, Boolean showVerticals,
      boolean showDeleted,
      UriInfo uriInfo) {

    log.info("get clients starting from {}, limit of {}", startIndex, limit);

    Long totalCount = clientManager.countClients();

    if (totalCount == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    } else if (startIndex > totalCount) {
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(
              Status.BAD_REQUEST.getStatusCode(), null,
              "start index greater than total count"))
          .build();
    } else {
      //Get all clients list
      List<ClientBO> clientDetailsAll = clientManager
          .getClients(startIndex, limit, showVerticals, showDeleted);

      List<MwbItsClientMapBO> clientITSMapAll = clientManager
          .getItsClientsMap(startIndex, limit);

      ClientsDetail clients = null;
      try {
        clients = setUserClientAuthorization(clientDetailsAll, clientITSMapAll);
      } catch (InvocationTargetException ite) {

        String message = String.format("Error creating request: %s", ite.getMessage());
        log.error(message);
        throw new ApplicationException(message, ite);

      } catch (IllegalAccessException ilae) {
        String message = String.format("Error creating request: %s", ilae.getMessage());
        log.error(message);
        throw new ApplicationException(message, ilae);
      }

      if (clients !=null && !clients.isEmpty()) {
        clients.get(0).setOffset(startIndex);
        clients.get(0).setTotalCount(totalCount.longValue());
      }
      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      long nextPage = (limit + startIndex) > totalCount ? totalCount : limit + startIndex;
      URI locationURI = uriBuilder.replaceQueryParam("startIndex", nextPage)
          .replaceQueryParam("limit", limit).build();
      return Response.ok(clients).header("X-Total-Count", totalCount).header("X-Offset", startIndex)
          .location(locationURI).build();
    }
  }

  private ClientsDetail setUserClientAuthorization(List<ClientBO> dbClientDetails,
      List<MwbItsClientMapBO> dbMWBITSMapDetails)
      throws InvocationTargetException, IllegalAccessException {
    //Get authorization details

    List<ClientDetail> clientDetailsAll = new ArrayList<>();
    ClientDetail clientDetailObj;
    ClientBO currentClient;
    ClientDetail clientDetail;

    for (int dbClientIndex = 0; dbClientIndex < dbClientDetails.size(); dbClientIndex++) {
      clientDetail = new ClientDetail();
      BeanUtils.copyProperties(clientDetail, dbClientDetails.get(dbClientIndex));
      clientDetailsAll.add(clientDetail);

    }

    HashMap<String, String> itsAccountMap = new HashMap();
    HashMap<String, String> itsClientMap = new HashMap();
    HashMap<String, String> itsAppMap = new HashMap();

    for (int mwbClientIndex = 0; mwbClientIndex < dbMWBITSMapDetails.size(); mwbClientIndex++) {
      itsAccountMap.put(dbMWBITSMapDetails.get(mwbClientIndex).getId().toString(),
          dbMWBITSMapDetails.get(mwbClientIndex).getItsAccountId());
      itsAppMap.put(dbMWBITSMapDetails.get(mwbClientIndex).getId().toString(),
          dbMWBITSMapDetails.get(mwbClientIndex).getItsAppId());
      itsClientMap.put(dbMWBITSMapDetails.get(mwbClientIndex).getId().toString(),
          dbMWBITSMapDetails.get(mwbClientIndex).getItsClientId());
    }

    for (int clientIndex = 0; clientIndex < clientDetailsAll.size(); clientIndex++) {
      ClientDetail finalClientDetailObj = clientDetailsAll.get(clientIndex);
      finalClientDetailObj.setItsClientId(itsClientMap.get(finalClientDetailObj.getId()));
      finalClientDetailObj.setItsAppId(itsAppMap.get(finalClientDetailObj.getId()));
      finalClientDetailObj.setItsAccountId(itsAccountMap.get(finalClientDetailObj.getId()));
    }

    String userId = CommonUtils.getUserId();
    Map<String, ClientDetail> clientDetailMap = clientManager.getClientDetailsMapByUserId(userId);
    Map<String, String> userGroups = CommonUtils.getUserGroups();
    Map<String, String> userClients = CommonUtils.getUserClients();

    ClientsDetail clients = new ClientsDetail();
    Map<String, ClientDetail> clientsMapByItsClientId = null;

    if(clientDetailMap != null && !clientDetailMap.isEmpty()) {
      clientsMapByItsClientId  = mapByItsClientId(clientDetailMap);
      if(clientDetailsAll.size() > 0) {
        for (ClientDetail c : clientDetailsAll) {
          if(c.getItsClientId() != null && c.getItsAppId() != null ) {
            String key = (c.getItsClientId() + "_" + c.getItsAppId()).toUpperCase();
            if(clientDetailMap.containsKey(key)) {
              ClientDetail c1 = clientDetailMap.remove(key);
              c.standardClientName(c1.getStandardClientName())
                      .clientDisplayName(c1.getClientDisplayName())
                      .roles(c1.getRoles())
                      .appDisplayName(c1.getAppDisplayName());
              clients.add(c);
            }
          }
        }
      }
      if(!clientDetailMap.isEmpty()) {
        clientDetailMap.entrySet().stream().forEach(client -> {
          ClientDetail c1 = client.getValue();
          Client c = new Client();
          String itsClientId = c1.getItsClientId();
          String itsAppId = c1.getItsAppId();
          String itsAccountId = itsClientId + itsAppId;
          String name = itsClientId + "-" + itsAppId + "-" + itsAccountId;
          if(clientManager.getClientByName(name) == null) {
            c.itsClientId(itsClientId)
                    .itsAppId(itsAppId)
                    .itsAccountId(itsAccountId)
                    .description(itsClientId)
                    .name(name);
            ClientDetail c2 = clientManager.createClient(c);
            c2.standardClientName(c1.getStandardClientName())
                    .clientDisplayName(c1.getClientDisplayName())
                    .roles(c1.getRoles())
                    .appDisplayName(c1.getAppDisplayName());

            clients.add(c2);
          }
        });
      }
    } else if (userId == null && userGroups != null && userGroups.containsKey(Constants.MWB_ROLE_CLIENTADMIN)) {
      for (ClientDetail c : clientDetailsAll) {
        String itsClientId = c.getItsClientId();
        if(itsClientId != null) {
          c.standardClientName(itsClientId).roles(Arrays.asList(Constants.OPERATOR_ROLE));
        }
        clients.add(c);
      }
    }

    if(userClients != null) {
      for (ClientDetail c : clientDetailsAll) {
        String itsClientId = c.getItsClientId();
        if (itsClientId != null && (userClients.containsKey(itsClientId.toUpperCase())
                || userClients.containsKey(c.getName().toUpperCase())
                || userClients.containsKey(Constants.STAR))) {
          if(clientsMapByItsClientId != null && !clientsMapByItsClientId.isEmpty()) {
            ClientDetail c2 = clientsMapByItsClientId.get(itsClientId);
            if(c2 != null) {
              c.clientDisplayName(c2.getClientDisplayName())
                      .standardClientName(c2.getStandardClientName())
                      .roles(c2.getRoles())
                      .appDisplayName(c2.getAppDisplayName());
            } else {
              c.standardClientName(itsClientId);
            }
          } else {
            c.standardClientName(itsClientId);
          }

          if(!clients.contains(c)) {
            clients.add(c);
          }
        }
      }
    }
    return clients;
  }

  private Map<String, ClientDetail> mapByItsClientId(Map<String, ClientDetail> clientDetailMap) {
    Map<String, ClientDetail> clients = new HashMap<>();
    clientDetailMap.forEach((k, v) -> clients.put(v.getItsClientId(), v));
    return clients;
  }

  @Override
  public Response patchClientById(String clientId, PatchRequest jsonPatch)
      throws NotFoundException {
    ClientDetail clientDetail = clientManager.patchClient(clientId, jsonPatch);

    return Response.ok(clientDetail).build();
  }

}
