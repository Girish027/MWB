/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import com.tfs.learningsystems.util.Constants;

@Slf4j
@Component
@Path("/v1")
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the ingestion API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2019-11-18T13:56:05.802-04:00")
public class IngestionApi {

    @POST
    @Path("ingest/{level}/log")
    @Consumes({"text/plain"})
    @io.swagger.annotations.ApiOperation(value = "Ingest the log message",
            notes = "Ingests the log message received from the clients to backend",
            response = Void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 204,
                    message = "OK", response = Void.class),
            @io.swagger.annotations.ApiResponse(code = 401,
                    message = "Unauthorized to call the API", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 415,
                    message = "Unsupported media type", response = Error.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error",
                    response = Error.class)
    })
    public Response ingestLog(
        @ApiParam(value = "log level", required = true) @PathParam("level") String level,
        @ApiParam(value = "ingest logs", required = true) @Valid String message)
    throws NotFoundException {
        
        String logMessage = Constants.ING_API_LOG_MESSAGE_PREFIX.concat(message);
        
        if (Constants.ING_API_LOG_LEVEL_ERROR.equalsIgnoreCase(level)) {
            log.error(logMessage);
        } else if (Constants.ING_API_LOG_LEVEL_WARNING.equalsIgnoreCase(level)) {
            log.warn(logMessage);
        } else {
            log.info(logMessage);
        }

        return Response.status(204).build();
    }
}