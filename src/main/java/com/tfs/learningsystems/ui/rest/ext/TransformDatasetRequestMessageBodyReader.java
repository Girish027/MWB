/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.ext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.ui.model.TransformDatasetRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class TransformDatasetRequestMessageBodyReader
    implements MessageBodyReader<TransformDatasetRequest> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {

    return type == TransformDatasetRequest.class;
  }

  @Override
  public TransformDatasetRequest readFrom(Class<TransformDatasetRequest> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(entityStream, TransformDatasetRequest.class);
    } catch (JsonParseException e) {
      log.error("Failed to read message - ", e);
      throw new BadRequestException(
          "Invalid request. Error deserializing to TransformDatasetRequest.");
    } catch (Exception e) {
      log.error("Failed to read message - ", e);
      throw new BadRequestException("Unknown error.");
    }
  }

}
