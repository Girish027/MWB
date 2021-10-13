package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.error.Deploy2ApiResponseHandler;
import com.tfs.learningsystems.ui.nlmodel.model.TFSAccountsModuleDetails;
import com.tfs.learningsystems.ui.nlmodel.model.TFSAppsModuleDetails;
import com.tfs.learningsystems.ui.nlmodel.model.TFSClientsModuleDetails;
import com.tfs.learningsystems.ui.nlmodel.model.TFSProductsModuleDetails;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Qualifier("itsAPIManagerBean")
public class ITSAPIManagerImpl implements ITSAPIManager{

  @Inject
  @Qualifier("apiCallManager")
  private APICallManager apiCallManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestTemplate restTemplate;

  private Map<String, ClientDetail> mapByClientIdAndAppId(List<TFSClientsModuleDetails> clients) {

    Map<String, ClientDetail> clientsMap = new HashMap<>();
    for(TFSClientsModuleDetails client : clients) {
      List<TFSAppsModuleDetails> apps = client.getApps();
      List<String> roles = new ArrayList<>();
      TFSAccountsModuleDetails acc = client.getAccounts().stream().filter(account -> client.getClientId().equals(account.getAccountId())).findAny().orElse(null);
      if(acc != null) {
        TFSProductsModuleDetails prd = acc.getProducts().stream().filter(product -> (Constants.BOT_FRAMEWORK).equals(product.getProductId())).findAny().orElse(null);
        if(prd != null) {
          roles = prd.getRoles();
        }
      }

      for(TFSAppsModuleDetails app : apps) {
        String key = (client.getComponentClientId() + "_" + app.getAppId()).toUpperCase();
        ClientDetail clientDetail = new ClientDetail()
                .appDisplayName(app.getAppDisplayName())
                .clientDisplayName(client.getClientDisplayName())
                .standardClientName(client.getClientId())
                .itsClientId(client.getComponentClientId())
                .roles(roles)
                .itsAppId(app.getAppId());
        clientsMap.put(key, clientDetail);
      }
    }
    return clientsMap;
  }

  @Override
  public Map<String, ClientDetail> findClientsByUserId(String userId) {

    if(userId == null || userId.isEmpty()){
      return null;
    }

    StringBuilder errorMessage = new StringBuilder("Failed to retrieve clients details for userId = ")
            .append(userId)
            .append(" from ITS");
    List<TFSClientsModuleDetails> tfsClientsModuleDetails = null;

    // getModulesURL example value  https://stable.developer.sv2.247-inc.net/v1/integratedtoolsuite/clients?userid=00ug6hu2xlBSEwUnE0h7
    StringBuilder getModulesURL = new StringBuilder(appConfig.getItsApiURL())
            .append(Constants.FORWARD_SLASH).append(Constants.DEPLOY2_CLIENTS_STRING);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getModulesURL.toString())
            .queryParam(Constants.CLIENTS_USERID_FIELD, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "*/*");

    ResponseEntity<String> response  = apiCallManager
            .restTemplateGetCall(headers, builder, new Deploy2ApiResponseHandler());

    if (response != null) {
      try {
        tfsClientsModuleDetails =
            apiCallManager.reponseStatusCheck(
                    response,
                    TFSClientsModuleDetails.class,
                    HttpStatus.OK,
                    "",
                    errorMessage.toString(),
                    getModulesURL.toString());
      } catch (IOException | ClassNotFoundException e) {
        log.error("Failed to retrieve clients from ITS for userid ",
                userId, e);
      }
    }
    return mapByClientIdAndAppId(tfsClientsModuleDetails);
  }
}
