package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.VerifiedTranscriptionsResponse;
import com.tfs.learningsystems.ui.model.VerifyRequest;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/v1/content")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the Verify Content API")
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-08-21T16:55:37.740-07:00")
public class ContentVerifyApi {

  private final ContentVerifyApiService delegate;

  @Autowired
  public ContentVerifyApi(final ContentVerifyApiService delegate) {
    this.delegate = delegate;
  }

  @POST
  @Path("/{projectId}/verify")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(
      value = "Runs tag verification tests against data for project",
      notes = "", response = VerifiedTranscriptionsResponse.class,
      tags = {"post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200,
          message = "Expected response to a valid request",
          response = VerifiedTranscriptionsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400,
          message = "Project does not have a transformed data set.",
          response = VerifiedTranscriptionsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404,
          message = "Invalid Project id / Specified dataset not associated with project",
          response = VerifiedTranscriptionsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500,
          message = "Internal Server Error",
          response = VerifiedTranscriptionsResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503,
          message = "Elasticsearch Service Unavailable",
          response = VerifiedTranscriptionsResponse.class)})
  public Response verifyIntents(
      @ApiParam(value = "The id of the project", required = true)
      @PathParam("projectId") String projectId,
      @ApiParam(value = "The start index to fetch the results from",
          defaultValue = "0")
      @DefaultValue("0") @QueryParam("startIndex") Integer startIndex,
      @ApiParam(value = "The page size of the results starting from the startIndex",
          defaultValue = "1000")
      @DefaultValue("1000") @QueryParam("limit") @Min(0) @Max(1000) Integer limit,
      @ApiParam(value = "An array of fields to hierarchically sort in order of their apperance in the array",
          defaultValue = "normalizedFormGroup:asc")
      @DefaultValue("normalizedFormGroup:asc") @QueryParam("sortBy") List<String> sortBy,
      @ApiParam(value = "") VerifyRequest search) throws NotFoundException {

    return delegate.verifyIntents(null, projectId, startIndex, limit, sortBy, search
    );

  }
}
