/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.ContentManager;
import com.tfs.learningsystems.ui.ProjectDatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DeleteIntentRequest;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.model.UpdateIntentResponse;
import com.tfs.learningsystems.ui.rest.ContentApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.search.taggingguide.model.ClassificationDocument;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Qualifier("contentApiService")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-27T13:40:35.787-05:00")
public class ContentApiServiceImpl extends ContentApiService {

  @Inject
  @Qualifier("contentManagerBean")
  private ContentManager contentManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;


  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;


  @Override
  public Response indexNewTranscriptions(String clientId, String projectId, String datasetId,
      List<TranscriptionDocumentForIndexing> transcriptionDocuments) throws NotFoundException {

    log.info("Index transcription for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    try {
      String username = AuthUtil.getPrincipalFromSecurityContext(null);
      contentManager.indexNewTranscriptions(clientId, projectId, datasetId,
          transcriptionDocuments, username);
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {} - {}", clientId, projectId,
          datasetId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null,
              ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);
      return Response.serverError().build();
    }

    return Response.ok().build();
  }

  @Override
  public Response addIntentByTranscriptionHash(String projectId, String datasetId,
      AddIntentRequest addIntentRequest)
      throws NotFoundException {

    log.info("Add Intent for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse addIntentResponse;
    String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

    try {
      addIntentResponse = contentManager.addIntentByTranscriptionHashList(clientId,
          projectId,
          datasetId,
          addIntentRequest.getIntent(), addIntentRequest.getRutag(),
          addIntentRequest.getUsername(),
          addIntentRequest.getTranscriptionHashList());

    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, datasetId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);
      return Response.serverError().build();
    }

    if (addIntentResponse.getTotalCount() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.NO_TRANSCRIPTIONS_TAGGED)).build();
    }

    return Response.ok(addIntentResponse).build();
  }


  @Override
  public Response updateIntentByTranscriptionHash(String projectId, String datasetId,
      AddIntentRequest addIntentRequest)
      throws NotFoundException {

    log.info("Update Intent for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    UpdateIntentResponse addIntentResponse;
    try {

      validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
      validationManager.validateProjectTransformedStatus(projectId);

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      addIntentResponse = contentManager.updateIntentByTranscriptionHashList(clientId,
          projectId,
          datasetId,
          addIntentRequest.getIntent(), addIntentRequest.getRutag(),
          addIntentRequest.getUsername(),
          addIntentRequest.getTranscriptionHashList());
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, datasetId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);
      return Response.serverError().build();
    }
    return Response.ok(addIntentResponse).build();
  }

  @Override
  public Response deleteIntentByTranscriptionHash(String projectId, String datasetId,
      DeleteIntentRequest deleteIntentRequest)
      throws NotFoundException {

    log.info("Delete Intent for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse deleteIntentResponse;
    try {
      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      deleteIntentResponse = contentManager.deleteIntentByTranscriptionHashList(clientId,
          projectId,
          datasetId,
          deleteIntentRequest.getUsername(),
          deleteIntentRequest.getTranscriptionHashList());
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, datasetId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);
      return Response.serverError().build();
    }
    return Response.ok(deleteIntentResponse).build();
  }

  @Override
  public Response deleteIntentGuideFromProject(String projectId) throws NotFoundException {

    log.info("Delete IntentGuide for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);
    String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

    try {
      this.contentManager.deleteProjectIntentsFromGuide(clientId, projectId);
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} ", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.warn("deletion of intent guide failed exceptions - {} - {}", clientId, projectId);
      return Response.serverError().build();
    }
    return Response.noContent().build();
  }

  @Override
  public Response addCommentByTranscriptionHash(String projectId, String datasetId,
      AddCommentRequest addCommentRequest)
      throws NotFoundException {

    log.info("Add comment for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse commentIntentResponse;
    try {
      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      if (addCommentRequest.getComment().isEmpty()) {
        commentIntentResponse = contentManager.deleteCommentByTranscriptionHash(clientId,
            projectId,
            datasetId,
            addCommentRequest.getUsername(),
            addCommentRequest.getTranscriptionHashList());
      } else {
        commentIntentResponse = contentManager.addCommentByTranscriptionHash(clientId,
            projectId,
            datasetId,
            addCommentRequest.getComment(),
            addCommentRequest.getUsername(),
            addCommentRequest.getTranscriptionHashList());
      }
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, datasetId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);
      return Response.serverError().build();
    }

    if (commentIntentResponse.getTotalCount() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.NO_COMMENTS_ADDED)).build();
    }

    return Response.ok(commentIntentResponse).build();
  }

  @Override
  public Response deleteRecords(String projectId, String datasetId) throws NotFoundException {

    log.info("Delete records for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    try {
      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      this.contentManager.deleteRecords(clientId, projectId, datasetId);
    } catch (ApplicationException e) {
      String message = String.format("Failed deleting content from project %s and dataset %s : %s",
          projectId, datasetId, e.getMessage());
      log.error(message);

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    }
    return Response.noContent().build();
  }


  @Override
  public Response addNewIntent(String projectId, TaggingGuideDocument taggingGuideDocument)
      throws NotFoundException {

    log.info("Add Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);
    try {

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      ClassificationDocument classificationDocument = contentManager
          .addNewIntent(clientId, projectId, taggingGuideDocument);

      taggingGuideDocument = new TaggingGuideDocument();

      BeanUtils.copyProperties(classificationDocument, taggingGuideDocument);

      taggingGuideDocument.setRutag(classificationDocument.getClassification());

      taggingGuideDocument.setIntent(classificationDocument.getGranularIntent());

      taggingGuideDocument.setClassificationId(classificationDocument.getClassificationId());

      return Response.ok(taggingGuideDocument).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {}", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE)).build();
    } catch (ApplicationException e) {
      log.error(String.format("Failed adding new intent %s:",
          taggingGuideDocument.getIntent()), e);

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
              null, ErrorMessage.BACKEND_ERROR))
          .build();
    }
  }

  @Override
  public Response patchIntentById(String projectId, String intentId, PatchRequest intentJsonPatch)
      throws NotFoundException {

    log.info("Update Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);

    try {
      String username = AuthUtil.getPrincipalFromSecurityContext(null);

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;


      ClassificationDocument patchedDocument = contentManager
          .patchIntentById(clientId, projectId, intentId, intentJsonPatch,
              username);

      TaggingGuideDocument taggingGuideDocument = new TaggingGuideDocument();

      BeanUtils.copyProperties(patchedDocument, taggingGuideDocument);

      taggingGuideDocument.setRutag(patchedDocument.getClassification());

      taggingGuideDocument.setIntent(patchedDocument.getGranularIntent());

      return Response.ok(taggingGuideDocument).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, intentId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error(String.format("Failed updating intent %s", intentId));
      String message = ErrorMessage.BACKEND_ERROR;
      if(e.getMessage().contains("Duplicate intent")) {
        message = "Duplicate intent names are not allowed";
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
              null, message))
          .build();
    }
  }

  @Override

  public Response deleteIntentById(String projectId, String intentId) throws NotFoundException {

    log.info("Delete Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);
    try {
      String username = AuthUtil.getPrincipalFromSecurityContext(null);

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;


      contentManager.deleteIntentById(clientId, projectId, intentId, username);

      return Response.noContent().build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} - {}", projectId, intentId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      String message = String.format("Failed deleting intent %s: %s", intentId, e.getMessage());
      log.error(message);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR))
          .build();
    }
  }

  @Override
  public Response addIntentByTranscriptionHash(final String projectId,
      final AddIntentRequest addIntentRequest) {

    log.info("Add Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectAndStart(projectId);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse addIntentResponse;
    try {

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;
      addIntentResponse = contentManager.addIntentByTranscriptionHashList(clientId,
          projectId, null,
          addIntentRequest.getIntent(), addIntentRequest.getRutag(),
          addIntentRequest.getUsername(),
          addIntentRequest.getTranscriptionHashList());
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} ", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception: ", e);
      return Response.serverError().build();
    }

    if (addIntentResponse.getTotalCount() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.NO_TRANSCRIPTIONS_TAGGED)).build();
    }

    return Response.ok(addIntentResponse).build();
  }

  @Override
  public Response updateIntentByTranscriptionHash(final String projectId,
      final AddIntentRequest updateIntentRequest) {

    log.info("Update Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectAndStart(projectId);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse addIntentResponse;
    try {

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      addIntentResponse = contentManager.updateIntentByTranscriptionHashList(clientId,
          projectId,
          null,
          updateIntentRequest.getIntent(), updateIntentRequest.getRutag(),
          updateIntentRequest.getUsername(),
          updateIntentRequest.getTranscriptionHashList());
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} ", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception: ", e);
      return Response.serverError().build();
    }
    return Response.ok(addIntentResponse).build();
  }

  @Override
  public Response deleteIntentByTranscriptionHash(final String projectId,
      final DeleteIntentRequest deleteIntentRequest) {

    log.info("Delete Intent for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse deleteIntentResponse;
    String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;
    try {
      deleteIntentResponse = contentManager.deleteIntentByTranscriptionHashList(clientId,
          projectId,
          null,
          deleteIntentRequest.getUsername(),
          deleteIntentRequest.getTranscriptionHashList());
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} ", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), null,
              ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception - ", e);

      return Response.serverError().build();
    }
    return Response.ok(deleteIntentResponse).build();
  }


  @Override
  public Response addCommentByTranscriptionHash(final String projectId,
      final AddCommentRequest addCommentRequest) {

    log.info("Add comment for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    validationManager.validateProjectId(projectId);
    validationManager.validateProjectTransformedStatus(projectId);

    UpdateIntentResponse commentIntentResponse;
    try {

      String clientId = ActionContext.getLegacyClientId()!=null?ActionContext.getLegacyClientId().toString():null;

      if (addCommentRequest.getComment().isEmpty()) {

        commentIntentResponse = contentManager.deleteCommentByTranscriptionHash(clientId,
            projectId,
            null,
            addCommentRequest.getUsername(),
            addCommentRequest.getTranscriptionHashList());
      } else {
        commentIntentResponse = contentManager.addCommentByTranscriptionHash(clientId,
            projectId,
            null,
            addCommentRequest.getComment(),
            addCommentRequest.getUsername(),
            addCommentRequest.getTranscriptionHashList());
      }
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("index not found or node is unavailable - {} ", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Application exception: ", e);
      return Response.serverError().build();
    }

    if (commentIntentResponse.getTotalCount() == 0) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.NO_COMMENTS_ADDED)).build();
    }
    return Response.ok(commentIntentResponse).build();
  }
}

