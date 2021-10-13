/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.DatasetManager;
import com.tfs.learningsystems.ui.ValidationManager;
import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetsDetail;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TransformDatasetRequest;
import com.tfs.learningsystems.ui.model.DatasetStatsResponse;
import com.tfs.learningsystems.ui.rest.DatasetsApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.CSVFileUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.InputStream;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2016-11-15T11:10:28.084-05:00")
public class DatasetsApiServiceImpl extends DatasetsApiService {

  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Override
  public Response postDataset(String clientId, String projectId, String name, String source,
      InputStream fileInputStream, boolean ignoreFirstRow,
      String columnMappings, String dataType,
      String description, UriInfo uriInfo)
      throws NotFoundException {

    validationManager.validateClientAndProject(clientId, projectId);

    log.info(
        "CreateDataset SessionId:{}---Clientid:{}---Projectid:{}---Datasetname:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, name);
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    FileColumnMappingSelectionList fileColumnMappingSelectionList = CSVFileUtil
        .getDefaultColumnMappings();
    if (columnMappings != null) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        fileColumnMappingSelectionList = mapper
            .readValue(columnMappings, FileColumnMappingSelectionList.class);
      } catch (Exception e) {
        log.error(ErrorMessage.COLUMN_MAPPING_CONVERT_ERROR);
        Error error = new Error();
        error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        error.setMessage(ErrorMessage.BAD_COLUMN_MAPPING);
        throw new BadRequestException(
            Response.status(Response.Status.BAD_REQUEST).entity(error).build());

      }
    }

    DatasetBO createdDataset = datasetManager
        .postDataset(clientId, projectId, name, source, fileInputStream, ignoreFirstRow,
            fileColumnMappingSelectionList, dataType, description, currentUserEmail);
    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + createdDataset.getId()).build();
    return Response.created(locationURI).entity(createdDataset).build();
  }

  @Override
  public Response createDataset(AddDatasetRequest addDatasetRequest, UriInfo uriInfo)
      throws NotFoundException {

    log.info(
        "CreateDataset SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}--autoTagDataset:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), addDatasetRequest.getProjectId(),
        addDatasetRequest.getDataset().getId(), addDatasetRequest.isAutoTagDataset());
    DatasetBO createdDataset = null;
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    createdDataset = datasetManager.addDataset(addDatasetRequest, currentUserEmail);
    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("" + createdDataset.getId()).build();
    return Response.created(locationURI).entity(createdDataset).build();
  }

  @Override
  public Response deleteDatasetById(String clientId, String projectId, String datasetId)
      throws NotFoundException {
    // TODO validate client <-> project <-> dataset
    validationManager.validateClientAndProject(clientId, projectId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    log.info("DeleteDataset SessionId:{}---Clientid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), datasetId);
    Response.Status deleteStatus = Response.Status.NO_CONTENT;
    try {
      datasetManager.deleteDataset(datasetId);
    } catch (Exception e) {
      log.error("failed to delete dataset by id - " + datasetId, e);
      if (!(e instanceof WebApplicationException)) {
        deleteStatus = Response.Status.INTERNAL_SERVER_ERROR;
      } else {
        throw e;
      }
    }

    return Response.status(deleteStatus).build();
  }

  @Override
  public Response getDatasetById(String clientId, String projectId, String datasetId)
      throws NotFoundException {
    validationManager.validateClientAndProject(clientId, projectId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    DatasetBO dataset = datasetManager.getDatasetById(clientId, projectId, datasetId);
    if (dataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("dataset_not_found");
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new com.tfs.learningsystems.ui.model.error.NotFoundException(error);
    }

    return Response.ok(dataset).build();
  }

  @Override
  public Response validDatasetForModelBuildingById(String clientId, String projectId, String datasetId)
          throws NotFoundException {
    validationManager.validateClientAndProject(clientId, projectId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, true);
    DatasetStatsResponse validateResponse = datasetManager.validDatasetForModelBuildingById(clientId, projectId, datasetId);
    return Response.ok(validateResponse).build();
  }

  @Override
  public Response listDatasets(Integer limit, Integer startIndex, String filterClientId,
      UriInfo uriInfo) throws NotFoundException {
    Long totalCount = datasetManager.countDatasets();
    DatasetsDetail datasets = new DatasetsDetail();
    if (totalCount == null) {
      Error error = new Error();
      error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
      error.setErrorCode("data_error");
      error.setMessage("Unable to load datasets");
      throw new InternalServerErrorException(
          Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build());
    } else if (startIndex > totalCount) {
      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("invalid_startIndex");
      error.setMessage("startIndex is greater than the total count");
      throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    } else {
      datasets.addAll(datasetManager.getDatasets(startIndex, limit, "", filterClientId));
      if (!datasets.isEmpty()) {
        datasets.get(0).setOffset(startIndex);
        datasets.get(0).setTotalCount(totalCount.longValue());
      }
      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      long nextPage = (limit + startIndex) > totalCount ? totalCount : limit + startIndex;
      URI locationURI = uriBuilder.replaceQueryParam("startIndex", nextPage)
          .replaceQueryParam("limit", limit).build();
      return Response.ok(datasets).header("X-Total-Count", totalCount)
          .header("X-Offset", startIndex).location(locationURI).build();
    }
  }

  @Override
  public Response patchDatasetById(String clientId, String projectId, String datasetId,
      PatchRequest jsonPatch)
      throws NotFoundException {
    validationManager.validateClientAndProject(clientId, projectId);
    validationManager.validateProjectDatasetEntry(projectId, datasetId, false);
    validationManager.validatePatchCall(jsonPatch, Constants.DATASET_FIELDS);
    log.info("PatchDataset SessionId:{}---Clientid:{}---Datasetid:{}", ActionContext.getSessionId(),
        ActionContext.getClientId(), datasetId);
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    DatasetBO modifiedDataset = datasetManager
        .updateDataset(datasetId, jsonPatch, currentUserEmail);
    return Response.ok(modifiedDataset).build();
  }

  @Override
  public Response transformDataset(String datasetId,
      TransformDatasetRequest transformDatasetRequest) throws NotFoundException {

    log.info("TransformDataset SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(),
        transformDatasetRequest.getProjectId(), datasetId);
    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      log.error("Transform dataset - unauthrized {} - {}", currentUserEmail, datasetId);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    DatasetBO datasetDetail = this.datasetManager.getDatasetById(datasetId);
    if (datasetDetail == null) {
      log.error(String.format("Transform dataset Failure, datasetid no found: %s", datasetId));
      throw new com.tfs.learningsystems.ui.model.error
          .NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
          null, "Dataset doesnt exist"));
    }

    try {
      log.info(String.format("Transforming dataset: %s", datasetId));
      this.datasetManager.transformDataset(datasetDetail, transformDatasetRequest);
      return Response.ok().build();
    } catch (ApplicationException e) {
      log.error(String.format("Transform dataset failed for datasetid: %s", datasetId), e);
      throw new InternalServerErrorException(Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.BACKEND_ERROR)).build());
    }

  }
}
