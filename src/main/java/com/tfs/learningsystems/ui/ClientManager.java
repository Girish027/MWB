/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/

package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.PatchRequest;

import java.util.List;
import java.util.Map;

public interface ClientManager {

  public ClientBO getClientById(String clientId);

  public ClientBO getClientByName(String clientName);

  public MwbItsClientMapBO getITSClientByClientId(String clientId);

  public MwbItsClientMapBO getITSClientByItsClientId(String itsClientId, String appID);

  public List<ClientBO> getClients(int startIndex, int count, Boolean showVerticals,
      boolean showDeleted);

  List<MwbItsClientMapBO> getItsClientsMap(int startIndex, int count);

  public Long countClients();

  public ClientDetail createClient(Client client);

  public Map<String, ClientDetail> getClientDetailsMapByUserId(String userId);

  public ClientBO deleteClient(String clientId);

  public ClientDetail patchClient(String clientId, PatchRequest jsonPatch);
}
