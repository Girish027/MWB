/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Provide utility methods related to authentication.
 */
@Slf4j
public final class AuthUtil {

  /**
   * Private constructor.
   */
  private AuthUtil() {
  }

  /**
   * Method to create an on-demand cookie.
   *
   * @param cookieName Name of the cookie
   * @param cookieValue Value of the cookie
   * @param maxAge maximum age of the cookie
   * @param secure whether this is a secure cookie
   * @return Cookie
   */
  public static Cookie createCookie(
      final String cookieName,
      final String cookieValue,
      final int maxAge,
      final boolean secure) {
    Cookie cookie = new Cookie(cookieName, cookieValue);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setSecure(secure);
    cookie.setHttpOnly(false);
    return cookie;
  }

  /**
   * @return String principalName
   */
  public static String getPrincipalFromSecurityContext(String defaultValue) {
    String userEmail = defaultValue;
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (principal != null) {
        userEmail = principal.toString();
      }
    }

    return userEmail;
  }

  /**
   * @return String AuthHeader
   */
  public static String getWebRecoAuthHeader(String clientId, String secret)
      throws InvalidKeyException, NoSuchAlgorithmException {
    String authHeaderString = null;
    try {
      final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss";
      final Date currentTime = new Date();
      final SimpleDateFormat sdf =
          new SimpleDateFormat(DATE_FORMAT);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      String timeValue = sdf.format(currentTime) + " GMT";
      String authString = timeValue + "\n/" + clientId + "/WebReco";
      Mac sha256HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
      sha256HMAC.init(secretKey);
      String hash = Base64.encodeBase64String(sha256HMAC.doFinal(authString.getBytes()));
      authHeaderString = "SharedKeyLite:" + timeValue + ":" + clientId + ":" + hash;

    } catch (Exception e) {
      log.error("Error while getting webreco auth header", e);
      throw e;
    }
    return authHeaderString;
  }

  /**
   * A Utility function to split httpMethod and URL from the resource string. 
   * Restricted URLs are added in 'EXTERNAL_DENIED_RESOURCES' list in Contants file.
   * @param resource
   * @return
   */
  public static Map<String, String> splitMethodURLFromResource(String resource) {
    Map<String, String> splitedResource = new HashMap<>();
    if ( resource != null) {
      String[] resourceSplit = resource.split(":");
      if ( resourceSplit.length == 2) {
        if (!"".equals(resourceSplit[0].trim()))
          splitedResource.put("METHOD", resourceSplit[0]);
        splitedResource.put("URI", resourceSplit[1]);
      }
    }
    return splitedResource;
  }

  /**
   * A Utility function to get all URL from the restricted resource list. 
   * Restricted URLs are added in 'EXTERNAL_DENIED_RESOURCES' list in Contants file.
   * @param restrictedList
   * @return
   */
  public static List<String> getAllRestrictedURIList(List<String> restrictedList) {
    List<String> urlList = new ArrayList<>();
    Map<String, String> splitedResource = null;

    for ( String restricted: restrictedList) {
      splitedResource = splitMethodURLFromResource(restricted);
      urlList.add(splitedResource.get("URI"));
    }
    return urlList;
  }
}