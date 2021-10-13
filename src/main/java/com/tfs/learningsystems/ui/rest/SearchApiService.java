/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.AuditFilter;
import com.tfs.learningsystems.ui.model.SearchRequest;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-03-17T08:25:19.581-07:00")
public abstract class SearchApiService {

  public abstract Response getAuditDocuments(String projectId, String datasetId,
      AuditFilter auditFilter, List<String> sortBy)
      throws NotFoundException;

  public abstract Response getMatchedIntents(String projectId, String q) throws NotFoundException;

  public abstract Response search(String projectId, String datasetId, Integer startIndex,
      Integer limit, String op, List<String> sortBy,
      SearchRequest search) throws NotFoundException;

  public abstract Response searchTaggingGuideDocuments(String projectId, List<String> sortBy)
      throws NotFoundException;

  public abstract Response stats(String clientId, String projectId, String datasetId) throws NotFoundException;

  public abstract Response listReportFields(String projectId, String datasetId)
      throws NotFoundException;

  public abstract Response getTaggingGuideImportStats(String projectId) throws NotFoundException;

  public abstract Response search(final String projectId,
      final Integer startIndex, final Integer limit, final String op,
      final List<String> sortBy, final SearchRequest search);

  public abstract Response stats(String projectId, SearchRequest search);

}
