/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;


import com.tfs.learningsystems.ui.model.AddCommentRequest;
import com.tfs.learningsystems.ui.model.AddIntentRequest;
import com.tfs.learningsystems.ui.model.DeleteIntentRequest;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentForIndexing;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocument;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-11T23:16:26.062-08:00")
public abstract class ContentApiService {

  public abstract Response indexNewTranscriptions(String clientId, String projectId,
      String datasetId,
      List<TranscriptionDocumentForIndexing> transcriptionDocuments) throws NotFoundException;

  public abstract Response addIntentByTranscriptionHash(String projectId, String datasetId,
      AddIntentRequest addIntentRequest)
      throws NotFoundException;

  public abstract Response updateIntentByTranscriptionHash(String projectId, String datasetId,
      AddIntentRequest updateIntentRequest)
      throws NotFoundException;

  public abstract Response deleteIntentByTranscriptionHash(String projectId, String datasetId,
      DeleteIntentRequest deleteIntentRequest)
      throws NotFoundException;

  public abstract Response deleteIntentGuideFromProject(String projectId) throws NotFoundException;

  public abstract Response addCommentByTranscriptionHash(String projectId, String datasetId,
      AddCommentRequest addCommentRequest)
      throws NotFoundException;

  public abstract Response deleteRecords(String projectId, String datasetId)
      throws NotFoundException;

  public abstract Response addNewIntent(String projectId, TaggingGuideDocument taggingGuideDocument)
      throws NotFoundException;

  public abstract Response patchIntentById(String projectId, String intentId,
      PatchRequest intentJsonPatch) throws NotFoundException;

  public abstract Response deleteIntentById(String projectId, String intentId)
      throws NotFoundException;

  public abstract Response addIntentByTranscriptionHash(final String projectId,
      final AddIntentRequest addIntentRequest);

  public abstract Response updateIntentByTranscriptionHash(final String projectId,
      final AddIntentRequest updateIntentRequest);

  public abstract Response deleteIntentByTranscriptionHash(final String projectId,
      final DeleteIntentRequest deleteIntentRequest);

  public abstract Response addCommentByTranscriptionHash(final String projectId,
      final AddCommentRequest addCommentRequest);

}
