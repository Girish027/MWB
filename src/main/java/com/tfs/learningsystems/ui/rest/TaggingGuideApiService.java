/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.search.taggingguide.model.TaggingGuideColumnMappingSelectionList;
import java.io.InputStream;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-07-08T18:54:19.161-07:00")
public abstract class TaggingGuideApiService {

  public abstract Response importGuide(String projectId, InputStream fileInputStream)
      throws NotFoundException;

  public abstract Response abortImportGuide(String projectId, String token)
      throws NotFoundException;

  public abstract Response commitImportGuide(String projectId, String token)
      throws NotFoundException;

  public abstract Response importGuideAddMappings(String projectId, String token,
      boolean ignoreFirstRow, TaggingGuideColumnMappingSelectionList columnMappings)
      throws NotFoundException;

  public abstract Response listColumns() throws NotFoundException;

  public abstract Response listMappings(String projectId) throws NotFoundException;
}
