package com.tfs.learningsystems.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.helper.JobHelper;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.OrionResponseHandler;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;
import com.tfs.learningsystems.util.CommonUtils;
import com.tfs.learningsystems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by huzefa.siyamwala on 8/22/17.
 */
@Component
@Slf4j
@Qualifier("orionManagerBean")
public class OrionManagerImpl implements OrionManager {

  public static final String ORION_MODEL_FETCH_ERROR = "reason";

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private JobHelper jobHelper;


  @Override
  public void reinit() {
    // Do not know
  }

  @Override
  public String postModelToOrion(File inputFile, File configFile, File speechWordClassFile, Boolean isUnbundled, String digitalHostedUrl, String modelType, String modelTechnology, String vectorizerVersion) {
    try {
      // FOR UI TESTING ONLY !!
      LinkedMultiValueMap<String, Object> map =
              new LinkedMultiValueMap<>();
      if(inputFile != null){
        map.add("trainingFile",
                new FileSystemResource(inputFile.getPath()));
      }
      map.add("configuration",
              new FileSystemResource(configFile.getPath()));

      if(speechWordClassFile!=null) {
        map.add("customWordclassFile",
                new FileSystemResource(speechWordClassFile.getPath()));
      }
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
              new HttpEntity<>(map,
                      headers);
      restTemplate.setErrorHandler(new OrionResponseHandler());

      log.info(appConfig.getOrionURL());
      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(appConfig.getOrionURL());

      if(digitalHostedUrl != null && !digitalHostedUrl.isEmpty()) {
        uriComponentsBuilder.queryParam("digitalHostedUrl", digitalHostedUrl);
      }

      if(modelType != null && !modelType.isEmpty()) {
        uriComponentsBuilder.queryParam("modelType", modelType);
      }

      if(modelTechnology != null && !modelTechnology.isEmpty()) {
        uriComponentsBuilder.queryParam("modelTechnology", modelTechnology);
      }

      if(vectorizerVersion != null) {
        uriComponentsBuilder.queryParam("vectorizerVersion", vectorizerVersion);
      }

      UriComponents builder;
      if(isUnbundled!=null) {
        builder = uriComponentsBuilder
                .queryParam("isUnbundled", isUnbundled.toString()).build();
      }else{
        builder = uriComponentsBuilder.build();
      }
      log.info("HS:  " + builder.toString());
      //.build().encode(Charset.forName("UTF-8")).toUri();
      ResponseEntity<String> result = restTemplate
              .exchange(builder.toUriString(), HttpMethod.POST,
                      requestEntity, String.class);
      if (result.getStatusCode() != HttpStatus.ACCEPTED) {
        log.error(String.format(
                "Failed while posting model to %s"
                        + "\nResponse received from Orion: %s",
                appConfig.getOrionURL(),
                result.getBody()));
        throw new InvalidRequestException(
                new Error(result.getStatusCode().value(), Constants.ORION_ERROR,
                        result.getBody()));
      }
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, String> responseBody =
              mapper.readValue(result.getBody(), HashMap.class);
      String orionResourceLink = responseBody.get("link");
      String modelUUID = orionResourceLink
              .substring(orionResourceLink.lastIndexOf('/') + 1);
      log.info("Orion started building model with uuid: " + modelUUID);
      return modelUUID;

    } catch (WebApplicationException wae) {
      log.error(Constants.ORION_MODEL_POST_ERROR, wae);
      throw wae;
    } catch (Exception ex) {
      log.error(Constants.ORION_MODEL_POST_ERROR, ex);
      throw new RuntimeException(Constants.ORION_MODEL_POST_ERROR,
              ex);
    }
  }

  @Override
  public String patchModelToOrion(String modelUUID, File speechWordClassFile, Boolean isUnbundled, String digitalHostedUrl, File inputFile, String modelType, String modelTechnology, String vectorizerVersion) {
    try {
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

      if(speechWordClassFile != null) {
        map.add("customWordclassFile", new FileSystemResource(speechWordClassFile.getPath()));
      }
      if(inputFile != null){
        map.add("trainingFile", new FileSystemResource(inputFile.getPath()));
      }
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

      restTemplate.setErrorHandler(new OrionResponseHandler());
      log.info("Orion URL: " + appConfig.getOrionURL());
      StringBuilder sb = new StringBuilder();
      sb.append(appConfig.getOrionURL());
      sb.append("/");
      sb.append(modelUUID);

      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(sb.toString());

      UriComponents builder;

      if(isUnbundled != null) {
        uriComponentsBuilder.queryParam("isUnbundled", isUnbundled);
      }

      if(digitalHostedUrl != null && !digitalHostedUrl.isEmpty()) {
        uriComponentsBuilder.queryParam("digitalHostedUrl", digitalHostedUrl);
      }

      if(modelType != null && !modelType.isEmpty()) {
        uriComponentsBuilder.queryParam("modelType", modelType);
      }

      if(modelTechnology != null && !modelTechnology.isEmpty()) {
        uriComponentsBuilder.queryParam("modelTechnology", modelTechnology);
      }

      if(vectorizerVersion != null) {
        uriComponentsBuilder.queryParam("vectorizerVersion", vectorizerVersion);
      }

      builder = uriComponentsBuilder.build();
      ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.PATCH, requestEntity, String.class);
      if (result.getStatusCode() != HttpStatus.ACCEPTED) {
        log.error(String.format("Failed while updating model to %s" + "\nResponse received from Orion: %s", appConfig.getOrionURL(), result.getBody()));
        throw new InvalidRequestException( new Error(result.getStatusCode().value(), Constants.ORION_ERROR, result.getBody()));
      }
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, String> responseBody = mapper.readValue(result.getBody(), HashMap.class);
      String orionResourceLink = responseBody.get("link");

      if(orionResourceLink.endsWith("/")) {
        orionResourceLink = orionResourceLink.substring(0, orionResourceLink.length() - 1);
      }
      modelUUID = orionResourceLink.substring(orionResourceLink.lastIndexOf('/') + 1);
      log.info("Orion started building model with uuid: " + modelUUID);
      return modelUUID;
    } catch (WebApplicationException wae) {
      log.error(Constants.ORION_MODEL_PATCH_ERROR, wae);
      throw wae;
    } catch (Exception ex) {
      log.error(Constants.ORION_MODEL_PATCH_ERROR, ex);
      throw new RuntimeException(Constants.ORION_MODEL_PATCH_ERROR, ex);
    }
  }

  @Override
  public TFSModelJobState getModelBuildingStatus(String modelUUID) {
    try {
      String orionResourcePath =
              appConfig.getOrionURL() + Constants.FORWARD_SLASH + modelUUID + "/status";
      restTemplate.setErrorHandler(new OrionResponseHandler());
      ResponseEntity<String> result;
      int retryIndex = 0;

      long apiRetryTimeout = appConfig.getExternalApiRetryWaitTime();

      int apiRetryCount = appConfig.getExternalApiRetryCount();

      do {
        if (retryIndex > 0) {
          Thread.sleep(apiRetryTimeout);
          log.debug("Retry model building retry attempt {} for model ", retryIndex, modelUUID);

        }

        result =
                restTemplate.getForEntity(orionResourcePath, String.class);
      } while (result.getStatusCode() != HttpStatus.OK &&
              !result.getBody().contains(Constants.ORION_FAILED_STATUS)
              && retryIndex++ < apiRetryCount);
      if (result.getStatusCode() != HttpStatus.OK) {
        log.error("Failed while retrieving model status from Orion. "
                        + "Response received from Orion: status - {} reason - {} body - {} ",
                result.getStatusCode().toString(),
                result.getStatusCode().getReasonPhrase(),
                result.getBody());
        throw new InvalidRequestException(
                new Error(result.getStatusCode().value(), "orion_error",
                        result.getBody()));
      }
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, String> responseBody =
              mapper.readValue(result.getBody(), HashMap.class);
      TFSModelJobState modelJobState = new TFSModelJobState();
      modelJobState.setModelUUID(modelUUID);
      String status = responseBody.get("status");
      if (Constants.ORION_COMPLETE_STATUS.equalsIgnoreCase(status)
              || Constants.ORION_SPEECH_COMPLETE_STATUS.equalsIgnoreCase(status) || Constants.ORION_COMBINE_COMPLETE_STATUS.equalsIgnoreCase(status)) {
        modelJobState.setStatus(TFSModelJobState.Status.COMPLETED);
        modelJobState.setStatusMessage(status);
        if (responseBody.containsKey(Constants.END_AT)) {
          modelJobState.setEndedAt(
                  Long.valueOf(responseBody.get(Constants.END_AT)));
        } else {
          modelJobState.setEndedAt(System.currentTimeMillis());
        }
      } else if (status.startsWith(Constants.ORION_FAILED_STATUS)) {
        modelJobState.setStatus(TFSModelJobState.Status.FAILED);
        modelJobState.setStatusMessage(status);
        if (responseBody.containsKey(Constants.END_AT)) {
          modelJobState.setEndedAt(
                  Long.valueOf(responseBody.get(Constants.END_AT)));
        } else {
          modelJobState.setEndedAt(System.currentTimeMillis());
        }
      } else {
        modelJobState.setStatus(TFSModelJobState.Status.RUNNING);
        modelJobState.setStatusMessage(status);
      }

      return modelJobState;
    } catch (WebApplicationException wex) {
      log.error(Constants.ORION_MODEL_FETCH_ERROR, wex);
      throw wex;
    } catch (Exception ex) {
      log.error(Constants.ORION_MODEL_FETCH_ERROR, ex);
      throw new RuntimeException(
              Constants.ORION_MODEL_FETCH_ERROR, ex);
    }
  }

  @Override
  public File getBuiltModelFromOrion(String modelUUID) {
    String orionModelDownloadURL =
            appConfig.getOrionURL() + "/" + modelUUID;
    return this.getFileFromOrion(orionModelDownloadURL,
            "final_web2nl.model");
  }

  @Override
  public File getDigitalModelFromOrion(String modelUUID) {
    String orionModelDownloadURL =
            appConfig.getOrionURL() + "/" + modelUUID + "/digital";
    return this.getFileFromOrion(orionModelDownloadURL,
            "final_web2nl.model");
  }

  @Override
  public File getSpeechModelFromOrion(String modelUUID) {
    String orionModelDownloadURL =
            appConfig.getOrionURL() + "/" + modelUUID + "/slm";
    return this.getFileFromOrion(orionModelDownloadURL,
            "final_web2nl.model");
  }

  @Override
  public byte[] getBuiltModelByteStreamFromOrion(String modelUUID) {
    String orionModelDownloadURL =
            appConfig.getOrionURL() + "/" + modelUUID;
    return this.getModelBytesStreamFromOrion(orionModelDownloadURL);
  }

  @Override
  public File getBuildModelStatsFromOrion(String modelUUID) {
    String orionModelStatsDownloadURL = appConfig.getOrionURL() + "/" + modelUUID;
    if(!CommonUtils.isUserExternalType()) {
      orionModelStatsDownloadURL += "/statistics";
    } else {
      orionModelStatsDownloadURL += "/externalStatistics";
    }
    return this.getFileFromOrion(orionModelStatsDownloadURL,
            "model_stats.xlsx");
  }

  @Override
  public File getModelTrainingOutputsFromOrion(final String modelUUID) {
    String orionModelTrainingOutputDownloadURL =
            appConfig.getOrionURL() + "/" + modelUUID + "/training-outputs";
    return this.getFileFromOrion(orionModelTrainingOutputDownloadURL,
            "trainingOutputs.zip");
  }

  @Override
  public Boolean deleteBuiltModelFromOrion(final String modelUUID) {

    String orionModelDeleteURL =
            appConfig.getOrionURL() + "/" + modelUUID;

    HttpEntity<String> entity = setHeadersOnEntity();

    ResponseEntity<String> result = restTemplate
            .exchange(orionModelDeleteURL, HttpMethod.DELETE, entity, String.class,
                    "1");

    if (result.getStatusCode() != HttpStatus.OK) {
      log.error("Failed while deleting model  from Orion. "
                      + "Response received from Orion: status - {} reason - {} body - {} ",
              result.getStatusCode().toString(),
              result.getStatusCode().getReasonPhrase(),
              result.getBody());
      throw new InvalidRequestException(
              new Error(result.getStatusCode().value(), "orion_error",
                      result.getBody()));

    }
    return true;
  }

  private HttpEntity<String> setHeadersOnEntity() {

    restTemplate.getMessageConverters()
            .add(new ByteArrayHttpMessageConverter());

    HttpHeaders headers = new HttpHeaders();
    restTemplate.setErrorHandler(new OrionResponseHandler());

    // Need Accept JSON as errors and not ready are returned as JSON from ModelBuilder service
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_JSON));
    return new HttpEntity<>(headers);

  }

  public File getFileFromOrion(String url, final String defaultFileName) {
    File file = null;
    CommonUtils.deleteFilesFromTempFolder(appConfig.getGitHubRetryCount());

    try {
      file = restTemplate.execute(url, HttpMethod.GET, null, clientHttpResponse -> {
        try {
          HttpStatus statusCode = clientHttpResponse.getStatusCode();
          int rawStatusCode = clientHttpResponse.getRawStatusCode();
          InputStream body = clientHttpResponse.getBody();
          if (body != null && statusCode == HttpStatus.OK) {
            String filename = null;
            String fileExtension = null;
            try {
              ContentDisposition contentDisposition =
                      new ContentDisposition(
                              clientHttpResponse
                                      .getHeaders()
                                      .getFirst(HttpHeaders.CONTENT_DISPOSITION),
                              true);
              String fullFilename = contentDisposition.getFileName();
              int dotIndex = contentDisposition.getFileName().lastIndexOf('.');
              if (dotIndex > 0) {
                filename = fullFilename.substring(0, dotIndex);
                fileExtension = fullFilename.substring(dotIndex);
              }
            } catch (ParseException ex) {
              log.warn("Couldn't parse Content Disposition: {}", ex.getMessage());
            }
            if (filename == null || fileExtension == null) {
              int dotIndex = defaultFileName.lastIndexOf('.');
              if (dotIndex > 0) {
                filename = defaultFileName.substring(0, dotIndex);
                fileExtension = defaultFileName.substring(dotIndex);
              }
            }
            File ret = File.createTempFile(filename, fileExtension);
            ret.deleteOnExit();

            FileOutputStream fOS = new FileOutputStream(ret);
            StreamUtils.copy(clientHttpResponse.getBody(), fOS);
            fOS.close();
            return ret;
          } else {
            if (clientHttpResponse
                    .getHeaders()
                    .getFirst(HttpHeaders.CONTENT_TYPE)
                    .contains(MediaType.APPLICATION_JSON_VALUE)) {
              ObjectMapper mapper = new ObjectMapper();
              HashMap<String, String> responseBody = mapper.readValue(body, HashMap.class);
              if (responseBody.containsKey(ORION_MODEL_FETCH_ERROR)) {
                throw new WebApplicationException(
                        responseBody.get(ORION_MODEL_FETCH_ERROR), rawStatusCode);
              }
            }
            throw new WebApplicationException(statusCode.getReasonPhrase(), rawStatusCode);
          }
        } catch (RestClientResponseException e) {
          log.error("Failed get file from Orion!!", e);
          // Handle ModelBuilder error response
          if (e.getResponseHeaders()
                  .getFirst(HttpHeaders.CONTENT_TYPE)
                  .contains(MediaType.APPLICATION_JSON_VALUE)) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> responseBody =
                    mapper.readValue(e.getResponseBodyAsString(), HashMap.class);
            if (responseBody.containsKey(ORION_MODEL_FETCH_ERROR)) {
              throw new WebApplicationException(
                      responseBody.get(ORION_MODEL_FETCH_ERROR), e, e.getRawStatusCode());
            }
          }
          // Failed to reach Model Building service
          throw new WebApplicationException(
                  e.getResponseBodyAsString(), e, e.getRawStatusCode());
        }
      });
      file.deleteOnExit();
      return file;
    } catch (WebApplicationException wae) {
      log.error(Constants.ORION_FILE_FETCH_ERROR, wae);
      throw wae;
    } catch (Exception ex) {
      throw new RuntimeException(Constants.ORION_FILE_FETCH_ERROR, ex);
    } finally{
      if(file != null){
        file.deleteOnExit();
      }
    }
  }


  private byte[] getModelBytesStreamFromOrion(final String url) {
    try {
      ResponseEntity<byte[]> response = this.getModelBuilderFileIResonse(url);
      if (response.getStatusCode() == HttpStatus.OK
              && response.getBody() != null) {
        return response.getBody();
      } else {
        if (response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
                .contains(MediaType.APPLICATION_JSON_VALUE)) {
          ObjectMapper mapper = new ObjectMapper();
          HashMap<String, String> responseBody =
                  mapper.readValue(response.getBody(),
                          HashMap.class);
          if (responseBody.containsKey(ORION_MODEL_FETCH_ERROR)) {
            throw new WebApplicationException(
                    responseBody.get(ORION_MODEL_FETCH_ERROR),
                    response.getStatusCodeValue());
          }
        }

        // Failed to reach Model Building service
        throw new WebApplicationException(
                response.getStatusCode().getReasonPhrase(),
                response.getStatusCodeValue());
      }
    } catch (RestClientResponseException e) {
      log.error("Failed get file from Orion!!", e);
      // Handle ModelBuilder error response
      if (e.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
              .contains(MediaType.APPLICATION_JSON_VALUE)) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> responseBody =
                null;
        try {
          responseBody = mapper.readValue(e.getResponseBodyAsString(),
                  HashMap.class);
        } catch (Exception ex) {
          throw new RuntimeException(
                  Constants.ORION_FILE_FETCH_ERROR, ex);
        }
        if (responseBody.containsKey(ORION_MODEL_FETCH_ERROR)) {
          throw new WebApplicationException(
                  responseBody.get(ORION_MODEL_FETCH_ERROR), e,
                  e.getRawStatusCode());
        }
      }
      // Failed to reach Model Building service
      throw new WebApplicationException(e.getResponseBodyAsString(),
              e, e.getRawStatusCode());
    } catch (Exception ex) {
      throw new RuntimeException(
              Constants.ORION_FILE_FETCH_ERROR, ex);
    }
  }

  private ResponseEntity<byte[]> getModelBuilderFileIResonse(String url) {

    ResponseEntity<byte[]> response = null;
    try {
      restTemplate.getMessageConverters()
              .add(new ByteArrayHttpMessageConverter());

      HttpHeaders headers = new HttpHeaders();

      // Need Accept JSON as errors and not ready are returned as JSON from ModelBuilder service
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM,
              MediaType.APPLICATION_JSON));
      HttpEntity<String> entity = new HttpEntity<>(headers);
      response = restTemplate
              .exchange(url, HttpMethod.GET, entity, byte[].class,
                      "1");

    } catch (WebApplicationException wae) {
      log.error(Constants.ORION_FILE_FETCH_ERROR, wae);
      throw wae;
    } catch (Exception ex) {
      throw new RuntimeException(
              Constants.ORION_FILE_FETCH_ERROR, ex);
    }
    return response;
  }

}