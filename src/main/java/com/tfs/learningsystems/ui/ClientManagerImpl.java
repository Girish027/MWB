/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.ClientBO.State;
import com.tfs.learningsystems.db.ModelDeploymentDetailsBO;
import com.tfs.learningsystems.db.ModelDeploymentDetailsBO.Active;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.db.VectorizerBO;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.dao.JdbcClientDao;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Qualifier("clientManagerBean")
public class ClientManagerImpl implements ClientManager {

  private static final String CLIENT_CREATE_ERROR = "Client create request error : %s";
  @Autowired
  ValidationManager validationManager;

  @Autowired
  @Qualifier("itsAPIManagerBean")
  private ITSAPIManager itsApiManager;

  @Autowired
  private Environment env;
  @Autowired
  private JdbcClientDao jdbcClientDao;

  @Inject
  private JsonConverter jsonConverter;


  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  @Qualifier("vectorizerManagerBean")
  private VectorizerManager vectorizerManager;

  @Autowired
  @Qualifier("preferenceManagerBean")
  private PreferenceManager preferenceManager;

  @Override
  public ClientBO getClientById(String clientId) {
    ClientBO client = new ClientBO();
    client = client.findOne(clientId);
    return client;
  }

  @Override
  public ClientBO getClientByName(String clientName) {
    ClientBO client = new ClientBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ClientBO.FLD_NAME, clientName);
    client = client.findOne(conditions);
    return client;
  }

  @Override
  public MwbItsClientMapBO getITSClientByClientId(String clientId) {
    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();
    mwbItsClientMapBO = mwbItsClientMapBO.findOne(MwbItsClientMapBO.FLD_MWB_ID, clientId);
    return mwbItsClientMapBO;
  }

  @Override
  public MwbItsClientMapBO getITSClientByItsClientId(String itsclientId, String appID) {

    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

    Map<String, Object> mwbItsClientConditions = new HashMap<>();

    mwbItsClientConditions.put(MwbItsClientMapBO.FLD_ITS_CLIENT_ID, itsclientId);
    mwbItsClientConditions.put(MwbItsClientMapBO.FLD_ITS_APP_ID, appID);

    mwbItsClientMapBO = mwbItsClientMapBO.findOne(mwbItsClientConditions);

    return mwbItsClientMapBO;
  }

  @Override
  public List<ClientBO> getClients(int startIndex, int count, Boolean showVerticals,
      boolean showDeleted) {
    ClientBO client = new ClientBO();
    Map<String, Object> conditions = new HashMap<>();
    if (!showDeleted) {
      conditions.put(ClientBO.FLD_STATE, ClientBO.State.ENABLED.toString());
    } else {
      conditions.put(ClientBO.FLD_STATE, ClientBO.State.DISABLED.toString());
    }
    if (showVerticals != null) {
      conditions.put(ClientBO.FLD_IS_VERTICAL, showVerticals);
    }

    List<ClientBO> clients = client.page(conditions, startIndex, count, null);
    return (clients);
  }

  @Override
  public List<MwbItsClientMapBO> getItsClientsMap(int startIndex, int count) {
    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();
    Map<String, Object> conditions = new HashMap<>();
    List<MwbItsClientMapBO> mwbItsClientMaps = mwbItsClientMapBO
        .page(conditions, startIndex, count, null);
    return (mwbItsClientMaps);
  }

  @Override
  public Long countClients() {
    ClientBO clientBO = new ClientBO();
    return (clientBO.count(new HashMap<String, Object>()));
  }

  @Override
  public ClientDetail createClient(Client client) {

    validationManager.validateClientCreate(client);

    ClientBO createdClient = new ClientBO();
    createdClient.setState(ClientBO.State.ENABLED);
    if (StringUtils.isNotEmpty(client.getName())) {
      createdClient.setName(client.getName());
    } else {
      createdClient.setName(client.getItsClientId());
    }
    createdClient.setAddress(client.getAddress());
    createdClient.setDescription(client.getDescription());
    createdClient.setIsVertical(client.getIsVertical());
    createdClient = createdClient.create();

    VectorizerBO vectorizerBO = vectorizerManager.getLatestVectorizerByTechnology(Constants.USE_LARGE);
    preferenceManager.addPreference(createdClient.getId().toString(), Constants.VECTORIZER_TYPE, createdClient.getId().toString(),
              vectorizerBO.getId(), Constants.PREFERENCE_CLIENT_LEVEL, true);

    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

    mwbItsClientMapBO.setItsAppId(client.getItsAppId());
    mwbItsClientMapBO.setItsAccountId(client.getItsAccountId());
    mwbItsClientMapBO.setItsClientId(client.getItsClientId());
    mwbItsClientMapBO.setId(createdClient.getId());
    mwbItsClientMapBO.setDescription(createdClient.getDescription());

    mwbItsClientMapBO = mwbItsClientMapBO.create();

    ClientDetail clientDetail = new ClientDetail();

    try {
      BeanUtils.copyProperties(clientDetail, createdClient);

      clientDetail.setItsAccountId(mwbItsClientMapBO.getItsAccountId());
      clientDetail.setItsAppId(mwbItsClientMapBO.getItsAppId());
      clientDetail.setItsClientId(mwbItsClientMapBO.getItsClientId());


    } catch (Exception e) {
      log.error(String.format(CLIENT_CREATE_ERROR, client.getItsClientId()), e);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }

    return clientDetail;
  }

  @Override
  public Map<String, ClientDetail> getClientDetailsMapByUserId(String userId) {
    Map<String, ClientDetail> clientDetailMap = null;
    if(userId != null) {
      clientDetailMap = itsApiManager.findClientsByUserId(userId);
    }
    return clientDetailMap;
  }

  @Override
  public ClientBO deleteClient(String clientId) {
    ClientBO clientBO = new ClientBO();

    clientBO = clientBO.findOne(clientId);

    if (clientBO == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("client_not_found");
      error.setMessage("Client '" + clientId + "' not found");
      throw new NotFoundException(error);
    }
    clientBO.setState(State.DISABLED);

    clientBO.update();

    return clientBO;
  }

  @Override
  public ClientDetail patchClient(String clientId, PatchRequest patchRequest) {

    ClientDetail clientDetail = new ClientDetail();
    ClientBO clientBO = new ClientBO();

    clientBO = clientBO.findOne(clientId);

    if (clientBO == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("client_not_founnd");
      error.setMessage("Client '" + clientId + "' not found");
      throw new NotFoundException(error);
    }

    MwbItsClientMapBO mwbItsClientMapBO = new MwbItsClientMapBO();

    mwbItsClientMapBO = mwbItsClientMapBO.findOne(clientId);

    boolean ifModuleChange = false;

    String accountId = null;
    String appId = null;

    PatchRequest clientPatchRequest = null;

    PatchRequest clientDetailsPatchRequest = null;

    for (PatchDocument patchDocument : patchRequest) {

      int appidIndex = patchDocument.getPath().indexOf(MwbItsClientMapBO.FLD_ITS_APP_ID);

      int accountidIndex = patchDocument.getPath().indexOf(MwbItsClientMapBO.FLD_ITS_ACCOUNT_ID);

      if (appidIndex >= 1 || accountidIndex >= 1) {

        if (accountidIndex >= 1) {
          accountId = patchDocument.getValue().toString();
        }

        if (appidIndex >= 1) {
          appId = patchDocument.getValue().toString();
        }

        if (clientPatchRequest == null) {
          clientPatchRequest = new PatchRequest();
        }

        clientPatchRequest.add(patchDocument);

      } else if (patchDocument.getPath().indexOf(ClientBO.FLD_IS_VERTICAL, 1) > -1 ||
          patchDocument.getPath().indexOf(ClientBO.FLD_ADDRESS, 1) > -1 ||
          patchDocument.getPath().indexOf(ClientBO.FLD_NAME, 1) > -1 ||
          patchDocument.getPath().indexOf(ClientBO.FLD_DEPLOYMENT_MODULE, 1) > -1) {

        if (patchDocument.getPath().indexOf(ClientBO.FLD_DEPLOYMENT_MODULE, 1) > -1) {
          ifModuleChange = true;
        }

        if (clientDetailsPatchRequest == null) {
          clientDetailsPatchRequest = new PatchRequest();
        }

        clientDetailsPatchRequest.add(patchDocument);
      } else if (patchDocument.getPath().indexOf(ClientBO.FLD_DESCRIPTION, 1) > -1) {

        if (clientPatchRequest == null) {
          clientPatchRequest = new PatchRequest();
        }

        clientPatchRequest.add(patchDocument);

        if (clientDetailsPatchRequest == null) {
          clientDetailsPatchRequest = new PatchRequest();
        }

        clientDetailsPatchRequest.add(patchDocument);

      } else {
        Error error = new Error();
        error.setCode(Status.BAD_REQUEST.getStatusCode());
        error.setErrorCode("Client_field_not_Supported");
        error.setMessage("Client field not supported");
        throw new NotFoundException(error);
      }


    }

    if (clientPatchRequest != null) {
      validationManager
          .validateClientAppAccUpdate(clientId, accountId, appId, mwbItsClientMapBO);
    }

    if (clientDetailsPatchRequest != null) {
      clientBO = jsonConverter.patch(clientDetailsPatchRequest, clientBO, ClientBO.class);
      clientBO.setModifiedAt(Calendar.getInstance().getTimeInMillis());
      clientBO.update();

    }

    if (clientPatchRequest != null) {
      mwbItsClientMapBO = jsonConverter
          .patch(clientPatchRequest, mwbItsClientMapBO, MwbItsClientMapBO.class);
      mwbItsClientMapBO.update();
    }

    if (ifModuleChange) {
      ModelDeploymentDetailsBO modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put(ModelDeploymentDetailsBO.FLD_CLIENT_ID, clientBO.getId());
      Sort sort = Sort.by(Sort.Direction.DESC,
          ModelDeploymentDetailsBO.FLD_CLIENT_ID);

      List<ModelDeploymentDetailsBO> modelDeploymentDetailsBOs = modelDeploymentDetailsBO
          .list(paramMap, sort);

      modelDeploymentDetailsBOs.stream().forEach(u -> u.setIsActive(Active.INACTIVE));

      for (ModelDeploymentDetailsBO modelDeploymentDetailsBOElem : modelDeploymentDetailsBOs) {
        modelDeploymentDetailsBOElem.update();
      }


    }
    try {
      BeanUtils.copyProperties(clientDetail, clientBO);
      BeanUtils.copyProperties(clientDetail, mwbItsClientMapBO);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error(String.format("Client patch request error : %s", clientId), e);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    } catch (Exception e) {
      log.error(String.format(CLIENT_CREATE_ERROR, clientId), e);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }

    return clientDetail;
  }
}
