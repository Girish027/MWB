/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.auth.filter;

import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.okta.jwt.JoseException;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtHelper;
import com.okta.jwt.JwtVerifier;
import com.okta.spring.config.OktaOAuth2Properties;
import com.okta.spring.oauth.ConfigurableAccessTokenConverter;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ClientBO;
import com.tfs.learningsystems.db.MwbItsClientMapBO;
import com.tfs.learningsystems.ui.ClientManager;
import com.tfs.learningsystems.ui.CsrfTokenManager;
import com.tfs.learningsystems.ui.ITSAPIManager;
import com.tfs.learningsystems.ui.dao.JdbcClientDao;
import com.tfs.learningsystems.ui.model.ClientDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.CommonUtils;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.BooleanHolder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerMapping;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "okta.oauth2")
public class AuthValidationFilter extends GenericFilterBean {


  JwtVerifier jwtVerifier = null;
  @Value("${okta.oauth2.issuer}")
  private String issuerUrl;
  @Value("${okta.oauth2.audience}")
  private String audience;
  @Autowired
  private AppConfig appConfig;
  @Inject
  @Qualifier("clientManagerBean")
  private ClientManager clientManager;

  @Autowired
  @Qualifier("itsAPIManagerBean")
  private ITSAPIManager itsApiManager;

  @Autowired
  private JdbcClientDao jdbcClientDao;

  @Autowired
  @Qualifier("csrfTokenManager")
  private CsrfTokenManager csrfTokenManager;

  @Autowired
  private com.fasterxml.jackson.databind.ObjectMapper jsonObjectMapper;

  private JwtAccessTokenConverter jwtAccessTokenConvertor = new JwtAccessTokenConverter();
  private OktaOAuth2Properties oktaOAuth2Properties = new OktaOAuth2Properties();
  private BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();
  private NullEventPublisher nullEventPublisher = new NullEventPublisher();
  private Authentication testAuth = null;

  //Not Thread safe
  private JwtVerifier getJWTVerifier() throws IOException, ParseException {
    if (jwtVerifier == null) {
      jwtVerifier = new JwtHelper().setIssuerUrl(issuerUrl)
              .setAudience(audience)  //default one is api://default
              .build();
    }
    return jwtVerifier;
  }


  /* For Testing Purposes */
  public OAuth2Authentication getTestAuth(HttpServletRequest httpServletRequest, String emailId) {

    String userEmailId = emailId != null ? emailId : "unittestuser";
    Authentication user = new UsernamePasswordAuthenticationToken(userEmailId, "N/A", null);
    OAuth2Request request = new OAuth2Request(null, null, null,
            true, null, null, null, null, null);
    OAuth2Authentication auth = new OAuth2Authentication(request, user);
    Map<String, Object> authorizationDetails = new HashMap<>();
    Map<String, String> claimDataMap = new HashMap<>();
    claimDataMap.put(Constants.MWB_ROLE_CLIENTADMIN, Constants.MWB_ROLE_CLIENTADMIN);
    authorizationDetails.put(Constants.OKTA_USER_GROUPS, claimDataMap);
    auth.setDetails(new OAuth2AuthenticationDetails(httpServletRequest));
    ((OAuth2AuthenticationDetails) (auth.getDetails())).setDecodedDetails(authorizationDetails);
    log.info("Get authorization for DM users");

    return auth;

  }

  private void sendErrorResponse(HttpServletResponse servletResponse, int code, String message)
          throws IOException {

    setClientAppCookies(servletResponse,
            null, null, null, null, null);

    if(code == HttpServletResponse.SC_UNAUTHORIZED){
      servletResponse.sendRedirect(appConfig.getOktaURL());
    }

    log.info("Unauthorized access - {} ", message);

    Error error = new Error();
    error.setCode(code);
    error.setErrorCode("" + code);
    error.setMessage("Authentication Error -"+ message);

    servletResponse.setStatus(code);
    servletResponse.setContentType("application/json");
    servletResponse.setCharacterEncoding("UTF-8");
    servletResponse.getWriter().write(jsonObjectMapper.writeValueAsString(error));

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    try {
      //creates session if not exists
      HttpSession session = httpServletRequest.getSession();
      ActionContext.clear();
      MDC.clear();
      MDC.put("session_id", session.getId());

      log.debug("Prepare authorization - {} ", session.getId());

      //By Pass Auth for Tests and API calls from APIGEE
      String apiAccessToken = httpServletRequest.getHeader(Constants.API_ACCESS_TOKEN);
      Boolean isAPIcall = apiAccessToken != null ? SecurityUtil.checkMD5Hash(apiAccessToken, appConfig.getApigeeApiAccessKey()) : Boolean.FALSE;
      String userEmailId = httpServletRequest.getHeader(Constants.EMAIL);

      if (appConfig.getIsTest() || isAPIcall) {
        if (testAuth == null) {
          testAuth = getTestAuth(httpServletRequest, userEmailId);
        }
        nullEventPublisher.publishAuthenticationSuccess(testAuth);
        SecurityContextHolder.getContext().setAuthentication(testAuth);
        String userId = "test_user";
        if (testAuth != null && testAuth.getPrincipal() != null) {
          userId = testAuth.getPrincipal().toString();
        }
        MDC.put("user_id", userId);

        ClientBO clientBO = this.jdbcClientDao.getClientByName("ModelingWorkbench");

        ActionContext.init(clientBO.getCid(), null, clientBO.getId(), "ModelingWorkbench",
                "mwb-team@247-inc.com", Arrays.asList("MWB_ROLE_TEST"));

        chain.doFilter(httpServletRequest, httpServletResponse);
        return;
      }

      //Checking for Host Header Injection attack
      String host = httpServletRequest.getHeader(Constants.HOST_HEADER);
      log.info("Before host header: {}", host);
      if(!(appConfig.getServerDomain().equals(host)
              || host != null
              && (host.equals(Constants.HEALTH_IP)
              || host.endsWith(Constants.DOMAIN_ENDS_WITH)))) {
        httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "host is not valid.");
        return;
      }

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      //Try read Oauth2 Authorization Header.
      Authentication bearerAuth = bearerTokenExtractor.extract(httpServletRequest);

      OAuth2AuthenticationDetails details = null;
      String token = null;
      if (bearerAuth != null) {
        token = bearerAuth.getPrincipal().toString();
        auth = readAuthentication(token);
        details = new OAuth2AuthenticationDetails(httpServletRequest);
        ((OAuth2Authentication) auth).setDetails(details);
        nullEventPublisher.publishAuthenticationSuccess(auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } else if (auth.getClass() == OAuth2Authentication.class) {
        details = (OAuth2AuthenticationDetails) auth.getDetails();
        token = details.getTokenValue();
        if (token == null) {
          httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  "Authorization header required");
          return;
        }
      }

      log.debug("Prepare authorization using - {} ", auth.getClass().getSimpleName());

      if (auth.getClass() == OAuth2Authentication.class) {

        Jwt jwt = null;
        try {
          jwt = getJWTVerifier().decodeAccessToken(token);
          if (jwt == null) {
            log.info("Unauthorized - JWT is null ");
            this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token not found!!");
            return;
          }
        } catch (JoseException | ParseException e) {
          log.error(e.getMessage(), e);
          //expired JWT
          SecurityContextHolder.clearContext();
          if(session != null) {
            session.invalidate();
          }
          if (e.getCause().getClass().equals(BadJWTException.class)) {
            this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token Expired. Re-authentication required");
          } else if(e.getClass().equals(ParseException.class)) {
            this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Failed while parsing token. Re-authentication required");
          }
          return;
        }

        Object authorizationDecodedDetails = fillAuthorizationDetails(jwt);
        ((OAuth2AuthenticationDetails) (((OAuth2Authentication) auth).getDetails()))
                .setDecodedDetails(authorizationDecodedDetails);

        BooleanHolder ifItsClient = new BooleanHolder(false);
        //Get client id from cookie or request.
        String clientId = getClientID(httpServletRequest, ifItsClient);
        String itsAppId = getAppID(httpServletRequest);

        String clientIdFromDb = null;
        ClientBO clientDetail = null;

        MwbItsClientMapBO mwbItsClientMapBO = null;
        List<String> userRoles = new LinkedList<>();
        String clientName = null;

        String itsClientName = null;
        String itsAppName = null;

        String path = httpServletRequest.getRequestURI()
                .substring(httpServletRequest.getContextPath().length());

        String httpMethod = httpServletRequest.getMethod();

        boolean ifClientsGetCall = false;

        if (path.endsWith("/clients") && httpMethod.equalsIgnoreCase("GET")) {
          ifClientsGetCall = true;
        }

        // Setting clients detail for user loggedin.
        Map<String, ClientDetail> clientDetailMap = (Map<String, ClientDetail>) session.getAttribute(Constants.OKTA_USER_CLIENTS);
        Map<String, Object> detailsMap = CommonUtils.getUserAuthDetailsMap();
        if(clientDetailMap == null) {
          String userId = (String) detailsMap.get(Constants.USER_SUB);
          clientDetailMap = itsApiManager.findClientsByUserId(userId);
          session.setAttribute(Constants.OKTA_USER_CLIENTS, clientDetailMap);
        }

        // To check whether user has access to client.
        clientIdFromDb = CommonUtils.getQueryParams(httpServletRequest.getRequestURI());
        if(clientIdFromDb != null){
          mwbItsClientMapBO = clientManager.getITSClientByClientId(clientIdFromDb);
          if(mwbItsClientMapBO != null){
            itsAppName = mwbItsClientMapBO.getItsAppId().toUpperCase();
            itsClientName = mwbItsClientMapBO.getItsClientId().toUpperCase();
            if(!CommonUtils.isUserSpecificClientType(itsClientName) &&
                    !CommonUtils.isUserClientAuthorized(clientDetailMap, itsClientName + "_" + itsAppName)){
              log.error("Unauthorized - user has no access to client - {} ", clientIdFromDb);
              this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_FORBIDDEN, "No access to " + clientIdFromDb);
              return;
            }
          }
        }

        //Get authorization details
        Map<String, String> userGroups = CommonUtils.getUserGroups();

        if(CommonUtils.isUserExternalType()) {
          userRoles.add(Constants.MWB_ROLE_EXTERNAL);
        }

        if(userGroups != null) {
          for (String grp : userGroups.keySet()) {
            if (Constants.ITS_GROUP.equals(grp)) {
              userRoles.add(Constants.ITS_GROUP);
              addCookieToResponse(httpServletResponse,
                      Constants.ITS_GROUP_NAME, Constants.ITS_GROUP);
            }
          }
        }

        //Client Isolation
        //TODO : Once we have client id for all the REST API if we get empty client id send 403 error
        if (!StringUtils.isEmpty(clientId)) {
          if (ifItsClient.value && !StringUtils.isEmpty(itsAppId)) {

            mwbItsClientMapBO = clientManager.getITSClientByItsClientId(clientId, itsAppId);

            if (mwbItsClientMapBO != null) {
              clientIdFromDb =
                      mwbItsClientMapBO.getId() != null ? mwbItsClientMapBO.getId().toString() : null;
            }

            itsClientName = clientId;

            if (!StringUtils.isEmpty(clientIdFromDb)) {
              clientDetail = clientManager.getClientById(clientIdFromDb);
            } else {
              log.error("Unauthorized -invalid client ID  - {} ",
                      clientId);

              if (!ifClientsGetCall) {
                this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_FORBIDDEN,
                        "No access to " + clientIdFromDb);

                return;
              }
            }
          } else {
            clientIdFromDb = clientId;
            clientDetail = clientManager.getClientById(clientIdFromDb);
            mwbItsClientMapBO = clientManager.getITSClientByClientId(clientIdFromDb);
            if (clientDetail == null) {
              // Invalid client id
              log.error("Unauthorized - invalid client ID - {} ", clientIdFromDb);
              this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_FORBIDDEN,
                      "No access to " + clientIdFromDb);
              return;
            }
          }

          if (clientDetail != null) {
            //only set ActionContext if current client exists in mysql config db, else anyhow before selecting existing client Actioncontext is not used.
            clientName = clientDetail.getName();
          }

          if (mwbItsClientMapBO != null) {
            //only set ActionContext if current client exists in mysql config db, else anyhow before selecting existing client Actioncontext is not used.
            itsClientName = mwbItsClientMapBO.getItsClientId().toUpperCase();
            itsAppName = mwbItsClientMapBO.getItsAppId().toUpperCase();

          }

          if (StringUtils.isNotEmpty(clientName) && StringUtils.isNotEmpty(itsClientName)
                  && StringUtils.isNotEmpty(itsAppName)
                  && !CommonUtils.isUserSpecificClientType(itsClientName)
                  && !CommonUtils.isUserClientAuthorized(
                  clientDetailMap, itsClientName + "_" + itsAppName)) {

            log.error("Unauthorized - user has no access to client - {} ", clientIdFromDb);

            if (!ifClientsGetCall) {
              this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_FORBIDDEN,
                      "No access to " + clientIdFromDb);
              return;
            }
          } else {
            log.info("Authorized - user changed client ID - {} ", clientIdFromDb);
            // the caller changed client ID
            // Again, it is a temporary solution of using cookie to remember the selected client,
            // until we retrofit all request with client ID

            httpServletResponse = setClientAppCookies(httpServletResponse,
                    clientIdFromDb,
                    mwbItsClientMapBO != null ? mwbItsClientMapBO.getItsClientId() : null,
                    mwbItsClientMapBO != null ? mwbItsClientMapBO.getItsAccountId() : null,
                    mwbItsClientMapBO != null ? mwbItsClientMapBO.getItsAppId() : null,
                    null);
          }
        }

        if (clientDetail != null) {
          String emailId = auth.getPrincipal().toString();
          ActionContext.init(clientDetail.getCid(), session.getId(), clientDetail.getId(),
                  clientDetail.getName(), emailId, userRoles);
        }
        String requestPath = ((HttpServletRequest) request).getPathInfo();
        String requestHttpMethod = ((HttpServletRequest) request).getMethod();

        // Reset user roles in Action contezt thread.
        ActionContext.setRoles(userRoles);

        if (requestPath != null &&
                !isURIAuthorized(userRoles, requestPath, requestHttpMethod)) {

          this.sendErrorResponse(httpServletResponse, HttpServletResponse.SC_FORBIDDEN,
                  " unauthorized access to " + requestPath);
          return;
        }
        MDC.put("user_id", auth.getPrincipal().toString());
        MDC.put("client_id", clientIdFromDb);
        addCookieToResponse(httpServletResponse, Constants.SESSION_USERNAME_FIELD,
                auth.getPrincipal().toString());

        setUTF8EncodedCookie(httpServletResponse, Constants.SESSION_USER_DETAILS,
                detailsMap.toString());

        setCSRFToken(httpServletRequest, httpServletResponse);
      }
    } catch (InvalidTokenException ex) {
      log.error("Failed while Authenticating", ex);
      httpServletResponse = setClientAppCookies(httpServletResponse,
              null, null, null, null, null);

      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
              "Invalid Token!! Re-authentication required");

      httpServletResponse.sendRedirect(appConfig.getOktaURL());

      return;
    } catch (Exception e) {
      log.error("Failed while Authenticating", e);
      httpServletResponse = setClientAppCookies(httpServletResponse,
              null, null, null, null, null);
      httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Backend Error");
      return;
    }

    chain.doFilter(httpServletRequest, httpServletResponse);
  }

  private HttpServletResponse setClientAppCookies(HttpServletResponse httpServletResponse,
                                                  String clientId,
                                                  String itsClientId, String itsAccountID, String itsAppId, String standardClientId) {

    addCookieToResponse(httpServletResponse,
            Constants.SESSION_CLIENTID_FIELD, clientId);

    addCookieToResponse(httpServletResponse,
            Constants.SESSION_ITS_CLIENTID_FIELD, itsClientId);

    addCookieToResponse(httpServletResponse,
            Constants.SESSION_ITS_ACCOUNTID_FIELD, itsAccountID);

    addCookieToResponse(httpServletResponse,
            Constants.SESSION_ITS_APP_ID_FIELD, itsAppId);

    addCookieToResponse(httpServletResponse,
            Constants.SESSION_STD_CLIENT_ID_FIELD, standardClientId);

    return httpServletResponse;
  }

  private String getAppID(HttpServletRequest httpServletRequest) {

    String appId = null;
    appId = httpServletRequest.getParameter(Constants.SESSION_ITS_APP_ID_FIELD);
    if (!StringUtils.isEmpty(appId)) {
      return appId;
    }
    Cookie[] cookies = httpServletRequest.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (Constants.SESSION_ITS_APP_ID_FIELD.equals(cookie.getName())) {
          appId = cookie.getValue();
          return appId;
        }
      }
    }
    return appId;
  }

  private String getClientID(HttpServletRequest httpServletRequest, BooleanHolder ifItsClient) {

    String clientId = null;
    clientId = httpServletRequest.getParameter(Constants.SESSION_ITS_CLIENTID_FIELD);
    if (!StringUtils.isEmpty(clientId)) {
      ifItsClient.value = true;
      return clientId;
    }

    final Map<String, String> pathVariables = (Map<String, String>) httpServletRequest
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    if (pathVariables != null && !pathVariables.isEmpty()) {
      clientId = pathVariables.get(Constants.SESSION_CLIENTID_FIELD);
      ifItsClient.value = false;
      if (!StringUtils.isEmpty(clientId)) {
        return clientId;
      }
    }

    clientId = httpServletRequest.getParameter(Constants.SESSION_CLIENTID_FIELD);

    if (!StringUtils.isEmpty(clientId)) {
      ifItsClient.value = false;
      return clientId;
    }

    Cookie[] cookies = httpServletRequest.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        //TODO: temporary solution of using cookie to remember the selected client, until we retrofit all request with client ID
        if (Constants.SESSION_ITS_CLIENTID_FIELD.equals(cookie.getName())) {
          clientId = cookie.getValue();
          if (!StringUtils.isEmpty(clientId)) {
            ifItsClient.value = true;
            return clientId;
          } else {
            break;
          }
        }
      }
    }

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        //TODO: temporary solution of using cookie to remember the selected client, until we retrofit all request with client ID
        if (Constants.SESSION_CLIENTID_FIELD.equals(cookie.getName())) {
          clientId = cookie.getValue();
          if (!StringUtils.isEmpty(clientId)) {
            ifItsClient.value = false;
            return clientId;
          }
        }
      }
    }
    return clientId;
  }


  private OAuth2Authentication readAuthentication(String token) {
    jwtAccessTokenConvertor.setAccessTokenConverter(
            new ConfigurableAccessTokenConverter(oktaOAuth2Properties.getScopeClaim(),
                    oktaOAuth2Properties.getRolesClaim()));
    return jwtAccessTokenConvertor.extractAuthentication(decode(token));
  }

  private Map<String, Object> decode(String token) {
    try {
      Jwt jwt = getJWTVerifier().decodeAccessToken(token);
      return jwt.getClaims();
    } catch (Exception e) {
      throw new InvalidTokenException("Cannot convert access token to JSON", e);
    }
  }

  private boolean isURIAuthorized(List<String> userRoles, String requestPath, String requestHttpMethod) {

    // verify if user has access to a resource based on its role
    Boolean isAuthorized = true;
    if (userRoles == null) {
      //
      // backward compatibility
      //
      return (isAuthorized);
    }

    for (String role : userRoles) {
      if (!Constants.ROLE_BASED_DENY_MAP.containsKey(role.trim())) {
        continue;
      }
      List<String> resources = Constants.ROLE_BASED_DENY_MAP.get(role.trim());
      for (String restricted : resources) {
        Map<String, String> resourceSplitMap = AuthUtil.splitMethodURLFromResource(restricted);
        if (requestPath.matches(resourceSplitMap.get("URI"))) {
          if(resourceSplitMap.get("METHOD") != null && !requestHttpMethod.equals(resourceSplitMap.get("METHOD")))
            return isAuthorized;
          return (false);
        }
      }
    }
    return isAuthorized;
  }

  private void addCookieToResponse(HttpServletResponse httpServletResponse, String name,
                                   String value) {
    Cookie cookie = AuthUtil.createCookie(name, value, appConfig.getCookieMaxAge(), false);
    httpServletResponse.addCookie(cookie);
  }


  private void setUTF8EncodedCookie(HttpServletResponse httpServletResponse, String name, String value) {
    byte[] bytes = value.getBytes();
    byte[] encoded = Base64.encodeBase64(bytes);
    String encodedString = new String(encoded, StandardCharsets.US_ASCII);

    addCookieToResponse( httpServletResponse,  name,
            encodedString);
  }

  private void setCSRFToken(HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse) {
    if (csrfTokenManager.getTokenFromSession(httpServletRequest) == null) {
      String token = csrfTokenManager.generateAndSaveToken(httpServletRequest, httpServletResponse);
      httpServletResponse.addHeader(CsrfTokenManager.TOKEN_HEADER_NAME, token);
    }
  }

  private Map<String, Object> fillAuthorizationDetails(Jwt jwt) {
    Map<String, Object> authorizationDetails = new HashMap<>();
    fillOktaClaimDetails(jwt, authorizationDetails, Constants.OKTA_USER_CLIENTS);
    fillOktaClaimDetails(jwt, authorizationDetails, Constants.OKTA_USER_GROUPS);
    return authorizationDetails;
  }

  private void fillOktaClaimDetails(Jwt jwt, Map<String, Object> authorizationDetails,
                                    String oktaclaim) {
    JSONArray clients = (JSONArray) jwt.getClaims().get(oktaclaim);
    if (clients != null) {
      Map<String, String> claimDataMap = new HashMap<>();

      //convert clients object to a hashmap for efficient searching
      for (int i = 0; i < clients.size(); i++) {
        claimDataMap
                .put(clients.get(i).toString().toUpperCase(), clients.get(i).toString().toUpperCase());
      }
      authorizationDetails.put(oktaclaim, claimDataMap);
    }
  }

  private static final class NullEventPublisher implements AuthenticationEventPublisher {

    public void publishAuthenticationFailure(AuthenticationException exception,
                                             Authentication authentication) {
      // Do nothing
    }

    public void publishAuthenticationSuccess(Authentication authentication) {
      // Do nothing
    }
  }

}
