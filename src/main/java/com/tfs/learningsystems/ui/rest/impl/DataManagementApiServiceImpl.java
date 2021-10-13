/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.ui.DataManagementManager;
import com.tfs.learningsystems.ui.dao.ProjectDatasetDao;
import com.tfs.learningsystems.ui.model.DatasetTaskStatusResponse;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.ProjectTaskStatusResponse;
import com.tfs.learningsystems.ui.model.SearchRequest;
import com.tfs.learningsystems.ui.model.error.InvalidRequestException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.ui.rest.DataManagementApiService;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
    date = "2017-02-08T10:33:40.646-05:00")
public class DataManagementApiServiceImpl extends DataManagementApiService {

  @Inject
  private DataManagementManager dataManagementManager;

  @Inject
  @Qualifier("projectDatasetDaoBean")
  private ProjectDatasetDao projectDatasetDao;

  @Autowired
  private AppConfig appConfig;

  private String validateUser() {

    String currentUserEmail = AuthUtil.getPrincipalFromSecurityContext(null);
    if (currentUserEmail == null) {
      throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    return currentUserEmail;
  }


  @Override
  public Response export(final String clientId, final String projectId, final String datasetId,

      String queryOperator) throws NotFoundException {

    log.info("Export for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {

      File allTrascriptionsFile = dataManagementManager
          .export(clientId, projectId, Collections.singletonList(datasetId),
              queryOperator, false, false, false, null);
      File uniqueTrascriptsFile = dataManagementManager
          .exportUnique(clientId, projectId, Collections.singletonList(datasetId),
              queryOperator, false, false);

      List<File> files = new ArrayList<>(2);
      files.add(allTrascriptionsFile);
      files.add(uniqueTrascriptsFile);

      //packing files
      for (File file : files) {
        //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fileInputStream = new FileInputStream(file);

        IOUtils.copy(fileInputStream, zipOutputStream);

        fileInputStream.close();
        zipOutputStream.closeEntry();
      }

      if (zipOutputStream != null) {
        zipOutputStream.finish();
        zipOutputStream.flush();
        IOUtils.closeQuietly(zipOutputStream);
      }
      IOUtils.closeQuietly(bufferedOutputStream);
      IOUtils.closeQuietly(byteArrayOutputStream);

      byte[] byteArray = byteArrayOutputStream.toByteArray();
      return Response.ok().header("Content-Length", byteArray.length)
          .header("Content-Disposition", "attachment; filename=output.zip")
          .entity(byteArray).build();

    } catch (Exception e) {
      log.error("failed to export - " + projectId + " - " + datasetId, e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());

    }
  }

  @Override
  public Response export(final String clientId, final String projectId,
      final SearchRequest searchRequest,
      String queryOperator) throws NotFoundException {

    log.info("Export for SessionId:{}---Clientid:{}---Projectid:{}", ActionContext.getSessionId(),
        ActionContext.getClientId(), projectId);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {

      List<String> datasetIds = null;
      if (searchRequest != null && searchRequest.getFilter() != null) {
        datasetIds = searchRequest.getFilter().getDatasets();
      } else {
        datasetIds = projectDatasetDao.getDatasetIds(projectId).stream().map(String::valueOf).collect(Collectors.toList());
      }

      File allTrascriptionsFile = dataManagementManager
          .export(clientId, projectId, datasetIds, queryOperator, false, false, false, null);
      File uniqueTrascriptsFile = dataManagementManager
          .exportUnique(clientId, projectId, datasetIds, queryOperator, false, false);

      List<File> files = new ArrayList<>(2);
      files.add(allTrascriptionsFile);
      files.add(uniqueTrascriptsFile);

      //packing files
      for (File file : files) {
        //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fileInputStream = new FileInputStream(file);

        IOUtils.copy(fileInputStream, zipOutputStream);

        fileInputStream.close();
        zipOutputStream.closeEntry();
      }

      if (zipOutputStream != null) {
        zipOutputStream.finish();
        zipOutputStream.flush();
        IOUtils.closeQuietly(zipOutputStream);
      }
      IOUtils.closeQuietly(bufferedOutputStream);
      IOUtils.closeQuietly(byteArrayOutputStream);

      byte[] byteArray = byteArrayOutputStream.toByteArray();
      return Response.ok().header("Content-Length", byteArray.length)
          .header("Content-Disposition", "attachment; filename=output.zip")
          .entity(byteArray).build();

    } catch (Exception e) {
      log.error("failed to export - " + projectId, e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());

    }
  }

  @Override

  public Response transformDataset(String clientId, String projectId, String datasetId,

      UriInfo uriInfo, Boolean useModelForSuggestedCategory) throws NotFoundException {

    log.info(
        "TransformDataset SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}---useModelForSuggestedCategory:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId,
        useModelForSuggestedCategory);
    String currentUserEmail = this.validateUser();

    TaskEventBO transform = dataManagementManager
        .transform(clientId, projectId, datasetId, currentUserEmail,
            useModelForSuggestedCategory);

    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    URI locationURI = uriBuilder.path("/status").build();
    return Response.accepted(transform).location(locationURI).build();
  }

  @Override
  public Response transformRetry(String clientId, String projectId, String datasetId,

      UriInfo uriInfo, Boolean useModelForSuggestedCategory) throws NotFoundException {

    log.info(
        "TransformRetry SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}---useModelForSuggestedCategory:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId,
        useModelForSuggestedCategory);
    String currentUserEmail = this.validateUser();
    TaskEventBO transform = dataManagementManager
        .retryTransform(clientId, projectId, datasetId, currentUserEmail,
            useModelForSuggestedCategory);

    UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
    String path = uriInfo.getPath();
    path = path.replace("retry", "status");
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(appConfig.getRestUrlPrefix()).append(path);
    URI locationURI = uriBuilder.replacePath(pathBuilder.toString()).build();
    return Response.accepted(transform).location(locationURI).build();
  }

  @Override
  public Response transformStatus(String clientId, String projectId, String datasetId)
      throws NotFoundException {

    TaskEventBO status = dataManagementManager.status(clientId, projectId, datasetId);
    if (status == null) {
      throw new NotFoundException(new Error(Response.Status.NOT_FOUND.getStatusCode(),
          "transformation_job_not_found", "Transformation job for projectId:'" + projectId

          + "' datasetId:'" + datasetId + "' not found"));
    }
    return Response.ok(status).build();
  }

  /*@Override
  public Response exportAll(final String projectId, final String datasetId,
      String queryOperator) throws NotFoundException {

    log.info("ExportAll for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    String clientId = null;
    File file = dataManagementManager
        .export(clientId, projectId, Collections.singletonList(datasetId), queryOperator, false,
            false, false);

    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      return Response.ok().header("Content-Length", file.length())
          .header("Content-Disposition", "attachment; filename=" + file.getName())
          .entity(fileInputStream).build();

    } catch (IOException e) {
      log.error("failed to export all - " + projectId, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());

    }

  }

  @Override
  public Response exportUnique(final String projectId, final String datasetId,
      String queryOperator) throws NotFoundException {

    log.info("ExportUnique for SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    String clientId = null;
    File file = dataManagementManager
        .exportUnique(clientId, projectId, Collections.singletonList(datasetId), queryOperator,
            false,
            false);

    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      return Response.ok().header("Content-Length", file.length())
          .header("Content-Disposition", "attachment; filename=" + file.getName())
          .entity(fileInputStream).build();

    } catch (IOException e) {

      log.error("failed to export unique - " + projectId + " - " + datasetId, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());

    }

  }

  @Override
  public Response exportAll(final String projectId, final SearchRequest searchRequest,
      String queryOperator) throws NotFoundException {

    log.info("ExportAll for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    List<String> datasetIds = null;
    if (searchRequest != null && searchRequest.getFilter() != null) {
      datasetIds = searchRequest.getFilter().getDatasets();

    }
    String clientId = null;
    File file = dataManagementManager
        .export(clientId, projectId, datasetIds, queryOperator, false, false, false);
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      return Response.ok().header("Content-Length", file.length())
          .header("Content-Disposition", "attachment; filename=" + file.getName())
          .entity(fileInputStream).build();

    } catch (IOException e) {
      log.error("failed to export all - " + projectId, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }

  }

  @Override
  public Response exportUnique(final String projectId, final SearchRequest searchRequest,
      String queryOperator) throws NotFoundException {

    log.info("ExportUnique for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    List<String> datasetIds = null;
    if (searchRequest != null && searchRequest.getFilter() != null) {
      datasetIds = searchRequest.getFilter().getDatasets();
    }
    String clientId = null;
    File file = dataManagementManager
        .exportUnique(clientId, projectId, datasetIds, queryOperator, false, false);

    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      return Response.ok().header("Content-Length", file.length())
          .header("Content-Disposition", "attachment; filename=" + file.getName())
          .entity(fileInputStream).build();

    } catch (IOException e) {

      log.error("failed to export unique - " + projectId, e);
      throw new ServerErrorException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());

    }

  }*/

  @Override
  public Response exportTaggingGuideForProject(final String clientId, final String projectId)
      throws NotFoundException {

    log.info("Export TaggingGuide for SessionId:{}---Clientid:{}---Projectid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId);

    String userEmail = this.validateUser();

    File file = dataManagementManager.exportTaggingGuideForProject(clientId, projectId, userEmail);

    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      return Response.ok().header("Content-Length", file.length())
          .header("Content-Disposition", "attachment; filename=" + file.getName())
          .entity(fileInputStream).build();


    } catch (IOException e) {
      log.error("failed to export tagging guide - " + projectId, e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.FILE_ACCESS_MESSAGE))
          .build());
    }
  }

  @Override
  public Response deleteFailedTransformation(String clientId, String projectId, String datasetId)
      throws NotFoundException {

    log.info("DeleteFailedtransformation SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    String currentUserEmail = this.validateUser();

    dataManagementManager
        .deleteFailedTransformation(clientId, projectId, datasetId, currentUserEmail);

    return Response.noContent().build();
  }

  @Override
  public Response transformStatusForDatasets(List<String> datasetIds) throws NotFoundException {

    try {
      if (datasetIds == null || datasetIds.isEmpty()) {
        throw new InvalidRequestException(
            new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                "Dataset ids cannot be null or empty"));
      }
      DatasetTaskStatusResponse datasetTaskStatusResponse = dataManagementManager
          .transformStatusForDatasets(datasetIds);
      return Response.ok(datasetTaskStatusResponse).build();
    } catch (Exception e) {

      log.error("failed to get transform status ", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              e.getMessage()))
          .build());

    }

  }

  @Override
  public Response cancelTransformation(String clientId, String projectId, String datasetId)

      throws NotFoundException {

    log.info("CancelTransformation SessionId:{}---Clientid:{}---Projectid:{}---Datasetid:{}",
        ActionContext.getSessionId(), ActionContext.getClientId(), projectId, datasetId);

    this.validateUser();

    TaskEventBO cancelled = dataManagementManager
        .cancelTransformation(clientId, projectId, datasetId);

    if (cancelled != null) {
      return Response.ok(cancelled).build();
    } else {
      return Response.serverError()
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              "Failed to cancel job")).build();

    }
  }

  @Override

  public Response transformStatusForProjects(String clientId,
      Map<String, List<String>> projectDatasets) throws NotFoundException {

    try {
      if (projectDatasets == null || projectDatasets.isEmpty()) {

        throw new InvalidRequestException(
            new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                "Project ids cannot be null or empty"));
      } else {

        for (List<String> list : projectDatasets.values()) {

          if (list == null || list.isEmpty()) {
            throw new InvalidRequestException(
                new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                    "Dataset ids cannot be null or empty"));
          }
        }
      }
      ProjectTaskStatusResponse projectTaskStatusResponse = dataManagementManager

          .transformStatusForProjects(clientId, projectDatasets);

      return Response.ok(projectTaskStatusResponse).build();
    } catch (Exception e) {
      log.error("failed to get transform status - ", e);
      throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              e.getMessage()))
          .build());

    }

  }

}

