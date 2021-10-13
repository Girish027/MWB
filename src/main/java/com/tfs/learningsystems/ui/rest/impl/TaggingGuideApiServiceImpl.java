/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.TaggingGuideApiService;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideColumnsManager;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideManager;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappedResponse;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideStagedImportResponse;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.InputStream;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-08T18:34:35.247-07:00")
public class TaggingGuideApiServiceImpl extends TaggingGuideApiService {

  @Inject
  @Qualifier("taggingGuideManager")
  private TaggingGuideManager taggingGuideManager;

  @Inject
  @Qualifier("taggingGuideColumnsManager")
  private TaggingGuideColumnsManager columnsManager;

  @Override
  public Response importGuide(String projectId, InputStream fileInputStream)
      throws NotFoundException {

    log.info("Import Guide for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    try {
      String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      TaggingGuideStagedImportResponse importResponse = taggingGuideManager.saveGuide(
          projectId, fileInputStream, userEmail);

      return Response.ok(importResponse).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to import guide - ", e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (ApplicationException e) {
      log.error("Failed to import guide - ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
          new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build();
    }
  }

  @Override
  public Response abortImportGuide(String projectId, String token)
      throws NotFoundException {

    taggingGuideManager.abortImportGuide(projectId, token);
    return Response.ok().build();
  }

  @Override
  public Response commitImportGuide(String projectId, String token) throws NotFoundException {

    try {
      String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      TaggingGuideImportStatBO importStats = taggingGuideManager
          .commitImportGuide(projectId, userEmail, token);
      return Response.ok(importStats).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to commit guide - ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
              null, ErrorMessage.BACKEND_ERROR))
          .build();
    }
  }

  @Override
  public Response importGuideAddMappings(String projectId, String token, boolean ignoreFirstRow,
      TaggingGuideColumnMappingSelectionList columnMappings) throws NotFoundException {

    String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    try {
      TaggingGuideColumnMappedResponse stagedResponse = taggingGuideManager
          .addColumnMappingsAndGetStats(projectId, userEmail, token,
              ignoreFirstRow, columnMappings);
      return Response.ok(stagedResponse).build();
    } catch (ApplicationException e) {
      log.error("Failed to add mapping - ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
          new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build();
    }
  }

  @Override
  public Response listColumns() throws NotFoundException {

    TaggingGuideColumnList columnList = columnsManager.getColumns();
    return Response.ok().entity(columnList).build();
  }

  @Override
  public Response listMappings(String projectId) throws NotFoundException {

    String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    TaggingGuideColumnMappingSelectionList columnMappings = columnsManager
        .getColumnMappings(userEmail, projectId);
    return Response.ok().entity(columnMappings).build();
  }
}
