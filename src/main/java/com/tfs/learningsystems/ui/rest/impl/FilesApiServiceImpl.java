/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest.impl;

import com.tfs.learningsystems.db.FileBO;
import com.tfs.learningsystems.exceptions.EmptyCSVFileException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileEncodingException;
import com.tfs.learningsystems.exceptions.InvalidCSVFileException;
import com.tfs.learningsystems.ui.FileManager;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.rest.ApiResponseMessage;
import com.tfs.learningsystems.ui.rest.FilesApiService;
import com.tfs.learningsystems.ui.rest.NotFoundException;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import com.tfs.learningsystems.ui.search.file.model.FileStagedImportResponse;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.ErrorMessage;
import java.io.InputStream;
import java.net.URI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-20T20:06:29.165-08:00")
@Slf4j
public class FilesApiServiceImpl extends FilesApiService {

  @Inject
  @Qualifier("fileManagerBean")
  private FileManager fileManager;

  @Override
  public Response getFileById(String fileId) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @Override
  public Response listFiles(Integer limit, Integer startIndex) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @Override
  public Response patchFileById(String fileId, PatchRequest jsonPatch) throws NotFoundException {
    // do some magic!
    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }

  @Override
  public Response importFile(String datatype, InputStream fileInputStream,
      HttpServletRequest request) throws NotFoundException {

    try {
      String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      FileStagedImportResponse importResponse = fileManager
          .importFile(fileInputStream, userEmail, datatype);
      return Response.ok(importResponse).build();
    } catch (IndexNotFoundException | NoNodeAvailableException e) {
      log.error("No index found or ES is not available", e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(
          new Error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), null,
              ErrorMessage.SEARCH_UNAVAILABLE)).build();
    } catch (BadRequestException| EmptyCSVFileException| InvalidCSVFileEncodingException| InvalidCSVFileException e) {
      log.error("Failed to import file", e);
      return Response.status(Response.Status.BAD_REQUEST).entity(
              new Error(Response.Status.BAD_REQUEST.getStatusCode(), null,
                      e.getMessage())).build();
    } catch (Exception e) {
      log.error("Failed to import file", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          new Error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null,
              ErrorMessage.IMPORTING_FILE_ERROR)).build();
    }

  }

  @Override
  public Response generateUserSelectedColumnsFile(String token, boolean ignoreFirstRow,
      FileColumnMappingSelectionList columnMappings) throws NotFoundException {

      String userEmail = AuthUtil.getPrincipalFromSecurityContext(null);
      FileBO fileEntry = fileManager
          .generateUserSelectedColumnsFile(userEmail, token, ignoreFirstRow, columnMappings);
      return Response.ok(fileEntry).build();
  }

  @Override
  public Response addFile(
      String username,
      String datatype,
      InputStream fileInputStream,
      UriInfo uriInfo, HttpServletRequest request) throws NotFoundException {

    String errorMessage = null;
    Response.Status responseStatus = Response.Status.CREATED;
    log.info("File being added {}, {}", username, fileManager);
    FileBO fileEntry;
    try {
      fileEntry = fileManager.addFile(fileInputStream, username, datatype);
      UriBuilder uriBuilder = uriInfo.getRequestUriBuilder();
      URI locationURI = uriBuilder.path("" + fileEntry.getFileId()).build();
      fileEntry.setUri(locationURI.toString());
      return Response.created(locationURI).entity(fileEntry).build();
    } catch (InvalidCSVFileEncodingException | EmptyCSVFileException | InvalidCSVFileException ie) {
      log.error("Invalid CSV file encoding - " + username + " - " + datatype, ie);
      errorMessage = ie.getMessage();
      responseStatus = Response.Status.BAD_REQUEST;
    } catch (Exception e) {
      log.error("Failed to add file - " + username + " - " + datatype, e);
      errorMessage = ErrorMessage.ADDING_FILE_ERROR;
      responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
    }
    return Response.status(responseStatus)
        .entity(new Error(responseStatus.getStatusCode(), null, errorMessage)).build();
  }

  @Override
  public Response deleteFileById(String fileId)
      throws NotFoundException {
    try {
      fileManager.deleteFileById(fileId);
    } catch (SchedulerException e) {
      log.error("Error scheduling the job", e);
    }
    return null;
  }
}
