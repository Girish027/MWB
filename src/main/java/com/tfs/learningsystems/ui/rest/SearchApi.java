/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.AuditFilter;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.IntentResponse;
import com.tfs.learningsystems.ui.model.ReportField;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideImportStats;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/v1/search")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the search API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-03-17T08:25:19.581-07:00")
public class SearchApi {

  private final SearchApiService searchApiService;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  public SearchApi(SearchApiService searchApiService) {

    this.searchApiService = searchApiService;
  }

  @POST
  @Path("/{projectId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Search project transcriptions", notes = "", response = TranscriptionDocumentDetailCollection.class, tags = {
          "post",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = TranscriptionDocumentDetailCollection.class)})
  public Response search(
          @ApiParam(value = "The id of the project to retrieve", required = true) @PathParam("projectId") String projectId
          ,
          @ApiParam(value = "The start index to fetch the results from", defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") Integer startIndex
          ,
          @ApiParam(value = "The page size of the results starting from the startIndex", defaultValue = "100") @DefaultValue("100") @QueryParam("limit") @Min(0) @Max(1000) Integer limit
          ,
          @ApiParam(value = "The operator to be used between 2 words in a query string", defaultValue = "AND") @DefaultValue("AND") @QueryParam("op") String op
          ,
          @ApiParam(value = "An array of fields to hierarchically sort in order of their apperance in the array") @DefaultValue("count:asc") @QueryParam("sortBy") List<String> sortBy
          , @ApiParam(value = "") SearchRequest search)
          throws NotFoundException {

    return searchApiService.search(projectId, startIndex, limit, op, sortBy, search);

  }

  @POST
  @Path("/{projectId}/stats")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Gives basic stats of a project", notes = "", response = StatsResponse.class, tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "Expected response to a valid request", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 403, message = "User is not authorized", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = StatsResponse.class)})
  public Response stats(
          @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
          , @ApiParam(value = "") SearchRequest search)
          throws NotFoundException {

    return searchApiService.stats(projectId, search);

  }

  @POST
  @Path("/audit/{projectId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get all audit documents for a given project/dataset", notes = "", response = TranscriptionDocumentDetailCollection.class, tags = {
          "post",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = TranscriptionDocumentDetailCollection.class)})
  public Response getAuditDocuments(
          @ApiParam(value = "The id of the project to query", required = true) @PathParam("projectId") String projectId
          ,
          @ApiParam(value = "The id of the dataset to query") @QueryParam("datasetId") String datasetId
          , @ApiParam(value = "Search filters") AuditFilter auditFilter
          ,
          @ApiParam(value = "An array of fields to hierarchically sort in order of their apperance in the array") @QueryParam("sortBy") List<String> sortBy)
          throws NotFoundException {

    return searchApiService.getAuditDocuments(projectId, datasetId, auditFilter, sortBy);

  }

  @GET
  @Path("/{projectId}/intents")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Lookup intents/tags to help tag one or more transcriptions.", notes = "", response = IntentResponse.class, tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = IntentResponse.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = IntentResponse.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = IntentResponse.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = IntentResponse.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = IntentResponse.class)})
  public Response getMatchedIntents(
          @ApiParam(value = "The id of the project to retrieve", required = true) @PathParam("projectId") String projectId
          , @ApiParam(value = "Prefix to search for") @QueryParam("q") String q)
          throws NotFoundException {

    ApiParameterSanitizer.sanitize(q);

    return searchApiService.getMatchedIntents(projectId, q);

  }

  @GET
  @Path("/{projectId}/intentguide")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Search project tagging guide documents", notes = "", response = TaggingGuideDocumentDetail.class, responseContainer = "List", tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TaggingGuideDocumentDetail.class, responseContainer = "List"),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = TaggingGuideDocumentDetail.class, responseContainer = "List"),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TaggingGuideDocumentDetail.class, responseContainer = "List"),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = TaggingGuideDocumentDetail.class, responseContainer = "List")})
  public Response searchTaggingGuideDocuments(
          @ApiParam(value = "The id of the project to retrieve", required = true) @PathParam("projectId") String projectId
          ,
          @ApiParam(value = "An array of fields to hierarchically sort in order of their apperance in the array", defaultValue = "intent:asc") @DefaultValue("intent:asc") @QueryParam("sortBy") List<String> sortBy)
          throws NotFoundException {

    return searchApiService.searchTaggingGuideDocuments(projectId, sortBy);

  }

  @GET
  @Path("/{projectId}/intentguide/importstats")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Get tagging guide last import stats", notes = "", response = TaggingGuideImportStats.class, tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TaggingGuideImportStats.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response getTaggingGuideImportStats(
          @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId)
          throws NotFoundException {

    return searchApiService.getTaggingGuideImportStats(projectId);

  }

  @POST
  @Path("/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Search project transcriptions", notes = "", response = TranscriptionDocumentDetailCollection.class, tags = {
          "post",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 403, message = "User is not authorized", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = TranscriptionDocumentDetailCollection.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = TranscriptionDocumentDetailCollection.class)})
  public Response search(
          @ApiParam(value = "The id of the project to retrieve", required = true) @PathParam("projectId") String projectId
          ,
          @ApiParam(value = "The id of the dataset from the project", required = true) @PathParam("datasetId") String datasetId
          ,
          @ApiParam(value = "The start index to fetch the results from", defaultValue = "0") @DefaultValue("0") @QueryParam("startIndex") Integer startIndex
          ,
          @ApiParam(value = "The page size of the results starting from the startIndex", defaultValue = "100") @DefaultValue("100") @QueryParam("limit") @Min(0) @Max(100) Integer limit
          ,
          @ApiParam(value = "The operator to be used between 2 words in a query string", defaultValue = "AND") @DefaultValue("AND") @QueryParam("op") String op
          ,
          @ApiParam(value = "An array of fields to hierarchically sort in order of their apperance in the array") @DefaultValue("count:asc") @QueryParam("sortBy") List<String> sortBy
          , @ApiParam(value = "") SearchRequest search)
          throws NotFoundException {

    return searchApiService.search(projectId, datasetId, startIndex, limit, op, sortBy, search);

  }

  @GET
  @Path("clients/{clientId}/projects/{projectId}/datasets/{datasetId}/stats")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Gives basic stats of a project", notes = "", response = StatsResponse.class, tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "Expected response to a valid request", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 403, message = "User is not authorized", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = StatsResponse.class),

          @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = StatsResponse.class)})
  public Response stats(
          @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId,
          @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
          @ApiParam(value = "The id of the dataset", required = true) @PathParam("datasetId") String datasetId)
          throws NotFoundException {

    return searchApiService.stats(clientId, projectId, datasetId);

  }

  @GET
  @Path("/{projectId}/datasets/{datasetId}/getReportFields")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "List all fields for report visualization", notes = "", response = ReportField.class, responseContainer = "List", tags = {
          "get",})
  @io.swagger.annotations.ApiResponses(value = {
          @io.swagger.annotations.ApiResponse(code = 200, message = "An array of ReportField objects", response = ReportField.class, responseContainer = "List"),

          @io.swagger.annotations.ApiResponse(code = 403, message = "User is not authorized", response = java.lang.Error.class),

          @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ReportField.class, responseContainer = "List")})
  public Response listReportFields(
          @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId
          ,
          @ApiParam(value = "The id of the dataset", required = true) @PathParam("datasetId") String datasetId)
          throws NotFoundException {

    return searchApiService.listReportFields(projectId, datasetId);

  }
}
