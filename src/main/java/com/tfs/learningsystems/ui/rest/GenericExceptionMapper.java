/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.Error;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Provider
@Slf4j
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

  @Autowired
  Environment env;

  @Override
  public Response toResponse(Throwable ex) {
    if (!env.acceptsProfiles("test")) {
      log.error(ex.getMessage(), ex);
    }
    Error error = new Error();
    if (ex instanceof WebApplicationException) {
      Response response = ((WebApplicationException) ex).getResponse();
      // Already built out error response just return it
      if (response.getEntity() instanceof Error) {
        return response;
      } else {
        error.setCode(response.getStatus());

        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SERVER_ERROR)) {
          if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            error.setErrorCode("server_error");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            error.setErrorCode("service_unavailable");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            error.setErrorCode("gateway_timeout");
            error.setMessage("Service timed out");
          } else if (response.getStatus() == HttpStatus.NOT_IMPLEMENTED.value()) {
            error.setErrorCode("not_implemented");
            error.setMessage("Service has not been implemented");
          } else if (response.getStatus() == HttpStatus.NETWORK_AUTHENTICATION_REQUIRED.value()) {
            error.setErrorCode("network_authentication_required");
            error.setMessage("Authentication required");
          } else {
            error.setErrorCode("server_error");
            error.setMessage(ex.getMessage());
          }

        } else {
          if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            error.setErrorCode("not_found");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            error.setErrorCode("conflict");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            error.setErrorCode("bad_request");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            error.setErrorCode("unauthorized");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.FORBIDDEN.value()) {
            error.setErrorCode("forbidden");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            error.setErrorCode("unsupported_media_type");
            error.setMessage(ex.getMessage());
          } else if (response.getStatus() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            error.setErrorCode("method_not_allowed");
            error.setMessage(ex.getMessage());
          } else {
            error.setErrorCode("client_error");
            error.setMessage(ex.getMessage());
          }
        }
      }
    } else {
      // default to server error
      error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
      error.setErrorCode("server_error");
      error.setMessage(ex.getMessage());
    }

    return Response.status(error.getCode()).entity(error).type(MediaType.APPLICATION_JSON)
        .build();
  }
}
