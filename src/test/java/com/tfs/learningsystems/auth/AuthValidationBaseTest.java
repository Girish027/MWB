package com.tfs.learningsystems.auth;

import com.tfs.learningsystems.ui.ITSAPIManager;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.util.Constants;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;


public class AuthValidationBaseTest {

    protected Authentication user;

    protected OAuth2Request request;

    protected OAuth2AuthenticationDetails oAuth2AuthenticationDetails;

    protected MockHttpServletRequest httpServletRequest;

    protected  OAuth2Authentication auth;

    @MockBean
    @Qualifier("itsAPIManagerBean")
    private ITSAPIManager itsApiManager;


    @Before
    public void setUp() throws Exception {

        this.user =  mock(UsernamePasswordAuthenticationToken.class);
        this.oAuth2AuthenticationDetails = mock(OAuth2AuthenticationDetails.class);
        this.httpServletRequest = new MockHttpServletRequest();
        this.auth = mock(OAuth2Authentication.class);
        this.auth.setDetails(new OAuth2AuthenticationDetails(httpServletRequest));
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(this.auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(this.auth.getUserAuthentication()).thenReturn(this.user);
        Mockito.when(this.auth.getDetails()).thenReturn(this.oAuth2AuthenticationDetails);

    }

    public void setDetailsMap(String email, String userType, String preferredName) {
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put(Constants.EMAIL, email);
        detailsMap.put(Constants.PREFERRED_USERNAME, preferredName);
        detailsMap.put(Constants.USER_TYPE, userType);
        Mockito.when(this.user.getDetails()).thenReturn(detailsMap);
    }

    public void setClientAdminInUserGroup() {
        Map<String, Object> authorizationDetails = new HashMap<>();
        Map<String, String> claimDataMap = new HashMap<>();
        claimDataMap.put(Constants.MWB_ROLE_CLIENTADMIN, Constants.MWB_ROLE_CLIENTADMIN);
        authorizationDetails.put(Constants.OKTA_USER_GROUPS, claimDataMap);
        Mockito.when(this.oAuth2AuthenticationDetails.getDecodedDetails()).thenReturn(authorizationDetails);
    }

    public void setUserIdOnDetailsMap(String userId) {
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put(Constants.USER_SUB, userId);
        Mockito.when(this.user.getDetails()).thenReturn(detailsMap);
    }

    public void setMockClientDetails(String itsClientId, String itsAppId, List<String> roles) {
        Map<String, ClientDetail> clients = new HashMap<>();
        ClientDetail clientDetail = new ClientDetail();
        clientDetail.roles(roles)
                .itsClientId(itsClientId)
                .itsAppId(itsAppId);
        String key = (itsClientId + "_" + itsAppId).toUpperCase();
        clients.put(key, clientDetail);
        Mockito.doReturn(clients).when(this.itsApiManager)
                .findClientsByUserId(anyString());
    }

    public void setAdminUserWithClientAttribute() {
        Map<String, Object> authorizationDetails = new HashMap<>();
        Map<String, String> claimDataMap = new HashMap<>();
        claimDataMap.put(Constants.STAR, Constants.STAR);
        authorizationDetails.put(Constants.OKTA_USER_CLIENTS, claimDataMap);
        Mockito.when(this.oAuth2AuthenticationDetails.getDecodedDetails()).thenReturn(authorizationDetails);
    }

    public void setSpecificUserWithClientAttribute(String clientName) {
        String capOneClient = clientName.toUpperCase();
        Map<String, Object> authorizationDetails = new HashMap<>();
        Map<String, String> claimDataMap = new HashMap<>();
        claimDataMap.put(capOneClient, capOneClient);
        authorizationDetails.put(Constants.OKTA_USER_CLIENTS, claimDataMap);
        Mockito.when(this.oAuth2AuthenticationDetails.getDecodedDetails()).thenReturn(authorizationDetails);
    }

    public void setEmptyUserWithClientAttribute() {
        Map<String, Object> authorizationDetails = new HashMap<>();
        Mockito.when(this.oAuth2AuthenticationDetails.getDecodedDetails()).thenReturn(authorizationDetails);
    }
}
