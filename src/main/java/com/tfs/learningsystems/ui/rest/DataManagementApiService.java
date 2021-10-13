/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-02-08T10:33:40.646-05:00")
public abstract class DataManagementApiService {

  public abstract Response export(String clientId, String projectId, String datasetId,
      String queryOperator) throws NotFoundException;

  public abstract Response export(String clientId, String projectId, SearchRequest searchRequest,
      String queryOperator) throws NotFoundException;

  public abstract Response transformDataset(String clientId, String projectId, String datasetId,
      UriInfo uriInfo, Boolean useModelForSuggestedCategory) throws NotFoundException;

  public abstract Response transformRetry(String clientId, String projectId, String datasetId,
      UriInfo uriInfo, Boolean useModelForSuggestedCategory) throws NotFoundException;

  public abstract Response transformStatus(String clientId, String projectId, String datasetId)
      throws NotFoundException;

  //public abstract Response exportAll (String projectId, String datasetId, String queryOperator) throws NotFoundException;

  //public abstract Response exportUnique (String projectId, String datasetId, String queryOperator) throws NotFoundException;

  //public abstract Response exportAll (String projectId, SearchRequest searchRequest, String queryOperator) throws NotFoundException;

  //public abstract Response exportUnique (String projectId, SearchRequest searchRequest, String queryOperator) throws NotFoundException;

  public abstract Response exportTaggingGuideForProject(String clientId, String projectId)
      throws NotFoundException;

  public abstract Response deleteFailedTransformation(String clientId, String projectId,
      String datasetId) throws NotFoundException;

  public abstract Response transformStatusForDatasets(List<String> datasetIds)
      throws NotFoundException;

  public abstract Response cancelTransformation(String clientId, String projectId, String datasetId)
      throws NotFoundException;

  public abstract Response transformStatusForProjects(String clientId,
      Map<String, List<String>> projectDatasets) throws NotFoundException;
}
