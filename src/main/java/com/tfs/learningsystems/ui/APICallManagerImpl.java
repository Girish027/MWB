package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2Module;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2ModulePackage;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2ModuleTarget;
import com.tfs.learningsystems.ui.nlmodel.model.TFSDeploy2Target;
import com.tfs.learningsystems.ui.nlmodel.model.TFSClientsModuleDetails;
import com.tfs.learningsystems.util.Constants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.ssl.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * Class to implement HTTP API Call  methods  and analyzing the response of the http calls
 */
@Component
@Qualifier("apiCallManager")
@Slf4j
public class APICallManagerImpl implements APICallManager {


  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestTemplate restTemplate;

  public static <T> T json2GenericObject(String jsonString,
      TypeReference<T> tr) {

    ObjectMapper objectMapper = new ObjectMapper();
    if (jsonString == null || "".equals(jsonString)) {
      return null;
    } else {
      try {
        return (T) objectMapper.readValue(jsonString, tr);
      } catch (Exception e) {
        log.warn("json error:" + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Method to creating HttpHeaders
   * @param username
   * @param password
   * @return
   */

  @Override
  public HttpHeaders createAuthHeaders(String username, String password) {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(
            auth.getBytes(StandardCharsets.US_ASCII));
    String authHeader = "Basic " + new String(encodedAuth);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", authHeader);
    return headers;
  }

  /**
   * Method to make a post call
   * @param headers
   * @param url
   * @param map
   * @return
   */
  @Override
  public ResponseEntity<String> restTemplatePostCall(HttpHeaders headers, String url,
      LinkedMultiValueMap<String, Object> map, ResponseErrorHandler responseErrorHandler) {

    restTemplate.setErrorHandler(responseErrorHandler);

    ResponseEntity<String> response;

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
        map, headers);
    response = restTemplate
        .exchange(url, HttpMethod.POST, requestEntity, String.class);

    return response;
  }

  /**
   * Method to make http get Call
   * @param headers
   * @param builder
   * @return
   */
  @Override
  public ResponseEntity<String> restTemplateGetCall(HttpHeaders headers,
      UriComponentsBuilder builder, ResponseErrorHandler responseErrorHandler) {

    restTemplate.setErrorHandler(responseErrorHandler);

    ResponseEntity<String> response;

    HttpEntity<String> request = new HttpEntity<>(headers);

    response = restTemplate
        .exchange(builder.build().encode().toUri(), HttpMethod.GET, request, String.class);

    return response;
  }

  /**
   * Method to set content type for HttpHeaders
   * @return HttpHeaders
   */
  @Override
  public HttpHeaders fillHeadersDetails() {

    HttpHeaders headers = createAuthHeaders(appConfig.getMwbServiceACUserName(),
        appConfig.getMwbServiceACUserPwd());

    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

    return headers;

  }


  /**
   * Method to to retrieve post call response body result to get type pf Java Pojo Objects
   * @param response
   * @param cls
   * @param httpStatus
   * @param responseBodyParam
   * @param errorMessage
   * @return	ArrayList of possibleDeploy2 Reponse Object List
   * @throws IOException
   */
  @Override
  public ArrayList reponseStatusCheck(ResponseEntity<String> response, Class<?> cls,
      HttpStatus httpStatus, String responseBodyParam, String errorMessage, String url)
      throws IOException, ClassNotFoundException {

    checkHttpReturnStatus(response, httpStatus, errorMessage, url);

    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    if(cls == TFSClientsModuleDetails.class) {
      ArrayList responseBody =  mapper.readValue(response.getBody(), ArrayList.class);
      return mapper.convertValue(responseBody, new TypeReference<ArrayList<TFSClientsModuleDetails>>() {});
    }

    HashMap<String, ArrayList> responseBody = mapper.readValue(response.getBody(), HashMap.class);

    ArrayList objectList = responseBody.get(responseBodyParam);

    if (cls == TFSDeploy2Module.class) {
      return mapper
          .convertValue(objectList, new TypeReference<ArrayList<TFSDeploy2Module>>() {
          });
    } else if (cls == TFSDeploy2ModulePackage.class) {
      return mapper
          .convertValue(objectList, new TypeReference<ArrayList<TFSDeploy2ModulePackage>>() {
          });
    } else if (cls == TFSDeploy2Target.class) {
      return mapper
          .convertValue(objectList, new TypeReference<ArrayList<TFSDeploy2Target>>() {
          });
    } else if (cls == TFSDeploy2ModuleTarget.class) {
      return mapper
          .convertValue(objectList, new TypeReference<ArrayList<TFSDeploy2ModuleTarget>>() {
          });
    }

    return new ArrayList();

  }

  /**
   * Method to check http reponse for required code.
   * @param response
   * @param httpStatus
   * @param errorMessage
   */
  @Override
  public void checkHttpReturnStatus(ResponseEntity<String> response, HttpStatus httpStatus,
      String errorMessage, String url) {

    if (response.getStatusCode() != httpStatus) {
      log.error(String.format(errorMessage, url, response.getBody()));
      throw new InvalidRequestException(
          new Error(response.getStatusCode().value(), Constants.DEPLOY2_TO_ERROR_CODE,
              response.getBody()));
    }
  }
}
