package com.tfs.learningsystems.testutil;

import com.tfs.learningsystems.config.AppConfig;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Generate {

  @Autowired
  AppConfig appConfig;

  public void OAuth2Generate() {
    String tokenName = "oktaToken";

    //AuthenticationScheme.header
    List<String> scopes = new ArrayList<String>();
    scopes.add("openid");
    ClientCredentialsResourceDetails clientCredentialsResourceDetails = new ClientCredentialsResourceDetails();
    clientCredentialsResourceDetails.setAccessTokenUri(
        "https://sso-247-inc.oktapreview.com/oauth2/ausf216lcwnycLULe0h7/v1/token");
    clientCredentialsResourceDetails.setClientId("0oaeosn6hzU0Z06r80h7");
    clientCredentialsResourceDetails.setClientSecret("yfq09vjZZSjajW-PPVJM84A_cmVLfqwHuTNtM8ys");
    clientCredentialsResourceDetails.setScope(scopes);
    clientCredentialsResourceDetails.setGrantType("authorization_code");
    clientCredentialsResourceDetails.setTokenName("accessToken");

    OAuth2RestTemplate test = new OAuth2RestTemplate(clientCredentialsResourceDetails);
    final OAuth2AccessToken accessToken = test.getAccessToken();

    final String accessTokenAsString = accessToken.getValue();


  }

}
