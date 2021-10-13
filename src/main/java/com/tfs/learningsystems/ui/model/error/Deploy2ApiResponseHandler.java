package com.tfs.learningsystems.ui.model.error;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;


@Slf4j
public class Deploy2ApiResponseHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    if (response.getStatusCode() != HttpStatus.ACCEPTED
        && response.getStatusCode() != HttpStatus.OK) {
      return false;
    }
    return true;
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    if (response.getStatusCode() != HttpStatus.ACCEPTED
        && response.getStatusCode() != HttpStatus.OK) {
      log.error("Response Received from Deploy2 service: " + response.getBody().toString());
    }
  }
}
