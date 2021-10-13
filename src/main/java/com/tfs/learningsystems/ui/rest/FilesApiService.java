/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. 
 * All Rights Reserved. 
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.search.file.model.FileColumnMappingSelectionList;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-11-22T16:11:15.902-08:00")
public abstract class FilesApiService {

  public abstract Response importFile(String datatype, InputStream fileInputStream,
      HttpServletRequest request) throws NotFoundException;

  public abstract Response generateUserSelectedColumnsFile(String token, boolean ignoreFirstRow,
      FileColumnMappingSelectionList columnMappings) throws NotFoundException;

  public abstract Response addFile(String username, String datatype, InputStream fileInputStream,
      UriInfo uriInfo, HttpServletRequest request) throws NotFoundException;

  public abstract Response deleteFileById(String fileId) throws NotFoundException;

  public abstract Response getFileById(String fileId) throws NotFoundException;

  public abstract Response listFiles(Integer limit, Integer startIndex) throws NotFoundException;

  public abstract Response patchFileById(String fileId, PatchRequest jsonPatch)
      throws NotFoundException;

}
