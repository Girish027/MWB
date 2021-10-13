/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;


import com.tfs.learningsystems.config.VisualizeConfig;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.TaggingGuideImportStatBO;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.ProjectDatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.SearchManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.AuditFilter;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ReportField;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.StatsResponse;
import com.tfs.learningsystems.ui.model.TranscriptionDocumentDetailCollection;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.rest.SearchApiService;
import com.tfs.learningsystems.ui.search.taggingguide.TaggingGuideImportStatsManager;
import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideDocumentDetail;
import com.tfs.learningsystems.util.ErrorMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-01-27T14:14:08.540-05:00")
@Slf4j
public class SearchApiServiceImpl extends SearchApiService {

  @Inject
  @Qualifier("visualizeConfigBean")
  private VisualizeConfig vConfig;

  @Inject
  @Qualifier("searchManagerBean")
  private SearchManager searchManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Autowired
  @Qualifier("taggingGuideImportStatsManager")
  private TaggingGuideImportStatsManager taggingGuideImportStatsManager;

  private static Error createError(int httpCode, String message) {

    Error error = new Error();
    error.setCode(httpCode);
    error.setMessage(message);
    return error;
  }

  @Override
  public Response getAuditDocuments(String projectId, String datasetId, AuditFilter auditFilter,
      List<String> sortBy)
      throws NotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Response getMatchedIntents(String projectId, String q) throws NotFoundException {

    validationManager.validateProjectId(projectId);
    List<String> matchedIntents;
    try {

      String clientId = null;
      matchedIntents = this.searchManager.getIntentsByPrefix(clientId, q, projectId);
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to get matached intent", e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (Exception e) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    }

    return Response.ok(matchedIntents).build();
  }


  @Override
  public Response search(String projectId, String datasetId, Integer startIndex,
      Integer limit, String queryOperator, List<String> sortByList,
      SearchRequest searchRequest) throws NotFoundException {

    long startTime = System.currentTimeMillis();
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);

    log.debug("sortByList is : {}", String.join(",", sortByList));

    TranscriptionDocumentDetailCollection docCollection;
    try {
      String clientId = null;
      docCollection = searchManager.getFilteredTranscriptions(clientId, projectId,
          Arrays.asList(datasetId), startIndex, limit, queryOperator, sortByList,
          searchRequest, datasetManager.getDatasetLocale(datasetId));
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to search - " + projectId + " - " + datasetId, e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (QueryNodeException qe) {
      log.error("Failed to search - " + projectId + " - " + datasetId, qe);
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.INVALID_SEARCH_QUERY))
          .build();
    } catch (WebApplicationException wae) {
      log.error("Failed to search - " + projectId + " - " + datasetId, wae);
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(Status.BAD_REQUEST.getStatusCode(),
              null, ErrorMessage.BACKEND_ERROR))
          .build();
    } catch (Exception e) {
      String msg = "Failed to search for transcriptions";
      log.error(msg, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
              null, ErrorMessage.BACKEND_ERROR))
          .build();

    }
    long endTime = System.currentTimeMillis();
    log.debug("Returning transcription list in {} milliseconds", (endTime - startTime));
    return Response.status(Response.Status.OK).entity(docCollection).build();
  }


  @Override
  public Response searchTaggingGuideDocuments(String projectId, List<String> sortBy)
      throws NotFoundException {

    validationManager.validateProjectId(projectId);
    List<TaggingGuideDocumentDetail> taggingGuideDocuments;
    try {
      String clientId = null;
      taggingGuideDocuments = this.searchManager
          .getTaggingGuideDocumentsForProject(clientId, projectId, sortBy);
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to search tagging guide - " + projectId, e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    }
    return Response.ok(taggingGuideDocuments).build();
  }

  @Override
  public Response stats(String clientId, String projectId, String datasetId) throws NotFoundException {

    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validateProjectTransformedStatus(projectId);
    try {
      StatsResponse stats = searchManager
          .getProjectDatasetStats(clientId, projectId, Arrays.asList(datasetId));
      return Response.ok(stats).build();

    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to search - " + projectId + " - " + datasetId, e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (BadRequestException e) {
      log.error("Failed to search - " + projectId + " - " + datasetId, e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build();
    }

  }


  @Override
  public Response listReportFields(String projectId, String datasetId) throws NotFoundException {

    log.info("Listing report fields for {} - {}", projectId, datasetId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    List<ReportField> responseList = new ArrayList<ReportField>(vConfig.getDefaultFields());
    ProjectBO project = projectManager.getProjectById(projectId);
    if (project != null) {
      Map<String, ReportField> fieldList = vConfig.getOptionalFields();
      for (String key : fieldList.keySet()) {
        if (searchManager
            .isFieldExists(project.getClientId(), projectId, Arrays.asList(datasetId), key)) {
          responseList.add(fieldList.get(key));
        }
      }
    }

    log.info("return {} report fields", responseList.size());
    return Response.ok().entity(responseList).build();

  }


  @Override
  public Response getTaggingGuideImportStats(String projectId) throws NotFoundException {

    validationManager.validateProjectId(projectId);
    TaggingGuideImportStatBO importStats = taggingGuideImportStatsManager
        .getLatestStatsForProject(projectId);
    return Response.ok(importStats).build();

  }

  @Override
  public Response search(final String projectId, final Integer startIndex,
      final Integer limit, final String op, final List<String> sortBy,
      final SearchRequest search) {

    long startTime = System.currentTimeMillis();

    log.debug("sortByList is : {}", String.join(",", sortBy));
    List<String> datasetIds = search.getFilter().getDatasets();
    validationManager.validateDatasetIds(datasetIds);
    validationManager.validateProjectTransformedStatus(projectId);

    for (String datasetId : datasetIds) {
      validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    }

    TranscriptionDocumentDetailCollection docCollection;
    try {
      String clientId = null;
      docCollection = searchManager
          .getFilteredTranscriptions(clientId, projectId, datasetIds, startIndex, limit, op, sortBy,
              search, this.projectManager.getProjectLocale(projectId));
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to search - " + projectId, e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), null,
              ErrorMessage.SEARCH_UNAVAILABLE)).build();
    } catch (QueryNodeException qe) {
      log.error("Failed to search - " + projectId, qe);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(), null
              , ErrorMessage.INVALID_SEARCH_QUERY)).build();
    } catch (WebApplicationException wae) {
      log.error("Failed to search - " + projectId, wae);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build();
    } catch (Exception e) {
      String msg = "Failed to search for transcriptions";
      log.error(msg, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build();
    }
    long endTime = System.currentTimeMillis();
    log.debug("Returning transcription list in {} milliseconds", (endTime - startTime));
    return Response.status(Response.Status.OK).entity(docCollection).build();
  }


  @Override
  public Response stats(final String projectId,
      final SearchRequest search) {

    validationManager.validateProjectTransformedStatus(projectId);
    try {
      List<String> datasetIds = null;
      if (search != null && search.getFilter() != null) {
        datasetIds = search.getFilter().getDatasets();
        if (datasetIds != null && !datasetIds.isEmpty()) {
          for(String datasetId: datasetIds) {
            validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
          }
        }
      }
      validationManager.validateDatasetIds(datasetIds);
      String clientId = null;
      StatsResponse stats = searchManager.getProjectDatasetStats(clientId, projectId, datasetIds);
      return Response.ok(stats).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("Failed to get states - " + projectId, e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), null,
              ErrorMessage.SEARCH_UNAVAILABLE))
          .build();
    } catch (BadRequestException e) {
      log.error("Failed to get states - " + projectId, e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new Error(Status.BAD_REQUEST.getStatusCode(), null, ErrorMessage.BACKEND_ERROR))
          .build();
    }
  }
}
