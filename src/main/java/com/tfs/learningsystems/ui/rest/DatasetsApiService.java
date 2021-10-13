/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.ui.model.AddDatasetRequest;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.TransformDatasetRequest;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-02T23:00:11.057+05:30")
public abstract class DatasetsApiService {

  /**
   * to create dataset
   */
  public abstract Response postDataset(String clientId, String projectId, String name, String source,
      InputStream fileInputStream, boolean ignoreFirstRow,
      String columnMappings, String dataType, String description, UriInfo uriInfo)
      throws NotFoundException;

  /**
   * to create dataset
   */
  public abstract Response createDataset(AddDatasetRequest addDatasetRequest, UriInfo uriInfo)
      throws NotFoundException;

  /**
   * to deLEte the dataset
   */
  public abstract Response deleteDatasetById(String clientId, String projectId, String datasetId) throws NotFoundException;

  /**
   * load dataset by id
   */
  public abstract Response getDatasetById(String clientId, String projectId, String datasetId) throws NotFoundException;

  /**
   * load dataset by id
   */
  public abstract Response validDatasetForModelBuildingById(String clientId, String projectId, String datasetId) throws NotFoundException;

  /**
   * to load list of datasets
   */
  public abstract Response listDatasets(Integer limit, Integer startIndex, String filterClientId,
      UriInfo uriInfo) throws NotFoundException;

  /**
   * update a dataset
   */
  public abstract Response patchDatasetById(String clientId, String projectId, String datasetId, PatchRequest jsonPatch)
      throws NotFoundException;

  /**
   * to ingest the dataset details into ES
   */
  public abstract Response transformDataset(String datasetId,
      TransformDatasetRequest transformDatasetRequest) throws NotFoundException;

}
