package com.tfs.learningsystems.ui.rest;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.ui.model.PatchRequest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-08-15T00:03:23.342-07:00")
public abstract class ModelsApiService {

  public abstract Response configureModel(String clientId, String projectId, ModelBO model, boolean trainNow, UriInfo uriInfo,
       String modelType, String modelTechnology, boolean toDefault)
          throws NotFoundException;

  public abstract Response downloadModel(String clientId, String projectId, String id)
      throws NotFoundException;

  public abstract Response downloadModelStats(String clientId, String projectId, String id)
      throws NotFoundException;

  public abstract Response queueModelForBuilding(String clientId, String id, UriInfo uriInfo, String modelType)
      throws NotFoundException;

  public abstract Response getModelBuildingStatus(String clientId, String projectId, String id,
      UriInfo uriInfo)
      throws NotFoundException;

  public abstract Response getModelById(String clientId, String projectId, String id)
      throws NotFoundException;

  public abstract Response getModelsForProject(String clientId, String projectId)
      throws NotFoundException;

  public abstract Response updateModelConfig(String clientId, String projectId, String id, PatchRequest jsonPatch)
          throws NotFoundException;

  public abstract Response updateCombinedModelConfig(String clientId, String projectId, String modelId, String digitalHostedUrl)
          throws NotFoundException;

  public abstract Response downloadTrainingOutputs(final String clientId, String projectId,
      final String id);

  public abstract Response deleteModelById(String clientId, String projectId, String modelId);

  public abstract Response getTrainingDataForModel(String clientId, String projectId, String id)
      throws NotFoundException;

}

