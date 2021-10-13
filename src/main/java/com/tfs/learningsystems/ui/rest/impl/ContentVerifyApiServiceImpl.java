package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.ContentVerifyManager;
import com.tfs.learningsystems.ui.ProjectDatasetManager;
import com.tfs.learningsystems.ui.ProjectManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.VerifiedTranscriptionsResponse;
import com.tfs.learningsystems.ui.model.VerifyRequest;
import com.tfs.learningsystems.ui.rest.ContentVerifyApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.util.ErrorMessage;
import java.util.List;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-08-21T16:55:37.740-07:00")
public class ContentVerifyApiServiceImpl extends ContentVerifyApiService {

  @Autowired
  ContentVerifyManager contentVerifyManager;

  @Autowired
  @Qualifier("projectManagerBean")
  private ProjectManager projectManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;


  @Override
  public Response verifyIntents(String clientId, String projectId, Integer startIndex,
      Integer limit, List<String> sortBy, VerifyRequest search) throws NotFoundException {

    ProjectBO projectDetail = null;
    projectDetail = validationManager.validateProjectId(projectId);

    log.debug("sortByList is : {}", String.join(",", sortBy));
    if (search == null) {
      search = new VerifyRequest();
    }
    List<String> datasetIds = search.getFilter() == null ?
        null :
        search.getFilter().getDatasetIds();
    if (datasetIds != null && !datasetIds.isEmpty()) {
      for (String datasetId : datasetIds) {
        validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
      }
    }

    validationManager.validateProjectTransformedStatus(projectId);

    try {

      VerifiedTranscriptionsResponse response = contentVerifyManager
          .verifyIntents(clientId, projectId, search, startIndex, limit,
              sortBy);

      return Response.ok().entity(response).build();

    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.warn("Index not found, or node not available - {}", projectId);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(
              Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
              null, ErrorMessage.SEARCH_UNAVAILABLE)).build();
    } catch (Exception e) {
      String msg = "Failed to search for transcriptions";
      log.warn(msg, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
              null, ErrorMessage.BACKEND_ERROR)).build();

    }

  }
}
