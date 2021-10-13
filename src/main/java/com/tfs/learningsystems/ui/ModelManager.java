package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.ModelBO;
import com.tfs.learningsystems.exceptions.ApplicationException;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import com.tfs.learningsystems.ui.model.TFSDeploymentModuleDetails;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModel;
import com.tfs.learningsystems.ui.nlmodel.model.TFSModelJobState;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

public interface ModelManager {


  public String queueModelForBuilding(String clientId, String id, String modelType, String modelTechnology, String vectorizerVersion)
      throws ApplicationException;

  public List<TFSModel> listOfModelsForProject(String clientId, String projectId)
      throws ApplicationException;

  public TFSModelJobState getModelStatus(String clientId, String modelId)
      throws ApplicationException;

  public ModelBO getModel(String clientId, String projectId, String modelId)
      throws ApplicationException;

  public File getModelFile(String clientId, String projectId, String modelId)
      throws ApplicationException;

  public Response deleteModel(String clientId, String projectId, String modelId)
      throws ApplicationException;

  public ModelBO updateModel(String clientId, String projectId, String modelDBId, PatchRequest patchRequest);

  public String updateCombinedModel(String clientId, String projectId, String modelDBId, String digitalHostedUrl);

  public File getModelStatistics(String clientId, String modelId) throws ApplicationException;

  public File getModelTrainingOutputs(String clientId, String modelId) throws ApplicationException;

  public TFSDeploymentModuleDetails publishModel(String clientId,
      List<ProjectModelRequest> projectModels, String tag, String userEmail)
      throws ApplicationException;

  public File getModelTrainingData(String clientId, String projectId, String modelId) throws ApplicationException;

  public TFSDeploymentModuleDetails getClientTags(String clientId);

  public TFSDeploymentModuleDetails getClientTagDetails(String clientId, String tagName);

  public ModelBO getDigitalModelByModelId(String modelId);
}
