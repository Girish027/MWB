/**
 * Copyright ©[24]7 Customer, Inc. All Rights Reserved.
 */
/**
 *
 */
package com.tfs.learningsystems.ui.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

/**
 * TBased on Stackoverflow description
 */
@Provider
@Component
public class PrettyFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext reqCtx, ContainerResponseContext respCtx)
      throws IOException {

    UriInfo uriInfo = reqCtx.getUriInfo();
    // log.info("prettyFilter: "+uriInfo.getPath());

    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    if (queryParameters.containsKey("pretty")) {
      ObjectWriterInjector.set(new IndentingModifier(true));
    }

  }

  public static class IndentingModifier extends ObjectWriterModifier {

    private final boolean indent;

    public IndentingModifier(boolean indent) {
      this.indent = indent;
    }


    @Override
    public ObjectWriter modify(EndpointConfigBase<?> endpointConfigBase,
        MultivaluedMap<String, Object> multivaluedMap, Object o, ObjectWriter objectWriter,
        JsonGenerator jsonGenerator) throws IOException {
      if (indent) {
        jsonGenerator.useDefaultPrettyPrinter();
      }
      return objectWriter;
    }
  }
}
