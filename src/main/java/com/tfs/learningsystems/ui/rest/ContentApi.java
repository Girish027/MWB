/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DeleteIntentRequest;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.util.ApiParameterSanitizer;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/content")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the content API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-11T23:16:26.062-08:00")
public class ContentApi {

  @Qualifier("contentApiService")
  private final ContentApiService contentApiService;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  public ContentApi(ContentApiService contentApiService) {

    this.contentApiService = contentApiService;

  }

  @POST
  @Path("/{clientId}/projects/{projectId}/datasets/{datasetId}/index")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Indexing new transcriptions", notes = "", response = void.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set / Invalid transcriptionHashList; No transcriptions were tagged.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Client Id / Project Id / Dataset Id / Specified dataset not associated with project", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = void.class)})
  public Response indexNewTranscriptions(
      @ApiParam(value = "The id of the client", required = true) @PathParam("clientId") String clientId,
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "the id of the dataset", required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "", required = true) List<TranscriptionDocumentForIndexing> transcriptionDocuments)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(datasetId);

    return contentApiService
        .indexNewTranscriptions(clientId, projectId, datasetId, transcriptionDocuments);

  }

  @POST
  @Path("/{projectId}/tag")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Tagging transcriptions. (Adding intents to transcriptions.)", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set / Invalid transcriptionHashList; No transcriptions were tagged.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 409, message = "Transcription conflict", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response addIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "", required = true) AddIntentRequest addIntentRequest)
      throws NotFoundException {

    return contentApiService.addIntentByTranscriptionHash(projectId, addIntentRequest);

  }

  @POST
  @Path("/{projectId}/datasets/{datasetId}/tag")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Tagging transcriptions. (Adding intents to transcriptions.)", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set / Invalid transcriptionHashList; No transcriptions were tagged.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 409, message = "Transcription conflict", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response addIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "the id of the dataset", required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "", required = true) AddIntentRequest addIntentRequest)
      throws NotFoundException {

    return contentApiService.addIntentByTranscriptionHash(projectId, datasetId, addIntentRequest);

  }

  @POST
  @Path("/{projectId}/tag/update")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Updating intent of Tagged transcriptions.", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 409, message = "Transcription conflict", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response updateIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "", required = true) AddIntentRequest updateIntentRequest)
      throws NotFoundException {

    return contentApiService.updateIntentByTranscriptionHash(projectId, updateIntentRequest);

  }

  @POST
  @Path("/{projectId}/datasets/{datasetId}/tag/update")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Updating intent of Tagged transcriptions.", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 409, message = "Transcription conflict", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response updateIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "the id of the dataset", required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "", required = true) AddIntentRequest updateIntentRequest)
      throws NotFoundException {

    return contentApiService
        .updateIntentByTranscriptionHash(projectId, datasetId, updateIntentRequest);

  }

  @POST
  @Path("/{projectId}/tag/delete")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes tags from transcriptions", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "A transcription on the list is not tagged / Project does not have a transformed data set", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response deleteIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "", required = true) DeleteIntentRequest deleteIntentRequest)
      throws NotFoundException {

    return contentApiService.deleteIntentByTranscriptionHash(projectId, deleteIntentRequest);

  }

  @POST
  @Path("/{projectId}/datasets/{datasetId}/tag/delete")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes tags from transcriptions", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "A transcription on the list is not tagged / Project does not have a transformed data set", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response deleteIntentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "the id of the dataset", required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "", required = true) DeleteIntentRequest deleteIntentRequest)
      throws NotFoundException {

    return contentApiService
        .deleteIntentByTranscriptionHash(projectId, datasetId, deleteIntentRequest);

  }

  @DELETE
  @Path("/{projectId}/intentguide")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Delete intent tagging guide from a project", notes = "", response = void.class, tags = {
      "delete",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = void.class)})
  public Response deleteIntentGuideFromProject(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId)
      throws NotFoundException {

    return contentApiService.deleteIntentGuideFromProject(projectId);

  }

  @POST
  @Path("/{projectId}/datasets/{datasetId}/comment")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add/update comment to transcription", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set / Invalid transcriptionHashList; No comments were added.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response addCommentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "the id of the dataset", required = true) @PathParam("datasetId") String datasetId,
      @ApiParam(value = "", required = true) AddCommentRequest addCommentRequest)
      throws NotFoundException {

    return contentApiService.addCommentByTranscriptionHash(projectId, datasetId, addCommentRequest);

  }

  @POST
  @Path("/{projectId}/comment")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Add/update comment to transcription", notes = "", response = UpdateIntentResponse.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set / Invalid transcriptionHashList; No comments were added.", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = UpdateIntentResponse.class)})
  public Response addCommentByTranscriptionHash(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "", required = true) AddCommentRequest addCommentRequest)
      throws NotFoundException {

    return contentApiService.addCommentByTranscriptionHash(projectId, addCommentRequest);

  }

  @DELETE
  @Path("/{projectId}/datasets/{datasetId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes all records in ElasticSearch for a given dataset", notes = "", response = void.class, tags = {
      "delete",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Project does not have a transformed data set.", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id / Specified dataset not associated with project", response = UpdateIntentResponse.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch Service Unavailable", response = void.class)})
  public Response deleteRecords(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The id of the dataset", required = true) @PathParam("datasetId") String datasetId)
      throws NotFoundException {

    return contentApiService.deleteRecords(projectId, datasetId);

  }

  @POST
  @Path("/{projectId}/intents")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Create a new intent and its supportive information", notes = "", response = void.class, tags = {
      "post",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully deleted", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid intent", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid Project id or token", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Requested intent already exists", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class)})
  public Response addNewIntent(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The tagging guide intent document", required = true) TaggingGuideDocument taggingGuideDocument)
      throws NotFoundException {

    ApiParameterSanitizer.sanitize(taggingGuideDocument.getIntent());
    ApiParameterSanitizer.sanitize(taggingGuideDocument.getRutag());

    return contentApiService.addNewIntent(projectId, taggingGuideDocument);

  }

  @PATCH
  @Path("/{projectId}/intents/{intentId}")
  @Consumes({"application/json-patch+json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Modifies an intent document", notes = "Modifies one or more attributes of an intent document", response = TaggingGuideDocument.class, tags = {
      "patch",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = TaggingGuideDocument.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project or intent id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch service unavailable", response = Error.class)})
  public Response patchIntentById(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The document id of the intent to patch", required = true) @PathParam("intentId") String intentId,
      @ApiParam(value = "", required = true) @Valid PatchRequest intentJsonPatch)
      throws NotFoundException {

    return contentApiService.patchIntentById(projectId, intentId, intentJsonPatch);

  }

  @DELETE
  @Path("/{projectId}/intents/{intentId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @io.swagger.annotations.ApiOperation(value = "Deletes a tagging guide document", notes = "", response = void.class, tags = {
      "delete",})
  @io.swagger.annotations.ApiResponses(value = {
      @io.swagger.annotations.ApiResponse(code = 204, message = "OK", response = void.class),

      @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized to call the API", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid project or intent id", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Error.class),

      @io.swagger.annotations.ApiResponse(code = 503, message = "Elasticsearch service unavailable", response = Error.class)})
  public Response deleteIntentById(
      @ApiParam(value = "The id of the project", required = true) @PathParam("projectId") String projectId,
      @ApiParam(value = "The document id of the intent to delete", required = true) @PathParam("intentId") String intentId)
      throws NotFoundException {

    return contentApiService.deleteIntentById(projectId, intentId);

  }
}
