/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/

package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.*;
import com.tfs.learningsystems.ui.model.Client;
import com.tfs.learningsystems.ui.model.DatasetDetailsModel;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.ProjectModelRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ValidationManager {


  /**
   * Validate project ID points to a valid project.
   *
   * @return ProjectDetail
   */
  public ProjectBO validateProjectId(String projectId);


  /**
   * Validate client ID
   *
   * @return ClientBO
   */
  public ClientBO validateClient(String clientId);


  /**
   * Validate source
   *
   * @return void
   */
  public boolean validateUser(String source);

  /**
   * Validate project  belongs to clint Id
   *
   * @return ProjectBO
   */

  public ProjectBO validateProject(String clientId, String projectId);


  /**
   * Validate project ID points to a valid project, and that project has a start date, if not set a
   * start date
   */

  public ProjectBO validateProjectAndStart(String projectId);

  public ProjectBO validateClientAndProject(String clientId, String projectId);

  public ProjectBO validateProjectCreate(String clientId, ProjectBO project);

  public void validateGlobalProjectName(String projectType, String globalProjectName);

  public void validateProjectNameExists(String clientId, String projectName);

  public void ifProjectNameChangePatchRequest(String clientId, String projectType, PatchRequest patchRequest);

  public void validatePatchCall(PatchRequest patchRequest, String flag);

  public DatasetBO validateClientAndDataset(String clientId, String datasetId);

  public DatasetDetailsModel validateClientProjectDataSet(String clientId, String projectId,
      String datasetId);

  public Boolean ifValidClientProjectDatasetEntry(String projectId, String datasetId);

  public ModelBO validateModelCreate(String clientId, String projectID, ModelBO model);

  public void validateDatasetIds(List<String> datasetIds);

  public Boolean validateProjectDatasetEntry(String projectId, String datasetId, Boolean modelType);

  public ModelBO validateAndGetModel(String clientId, String projectId, String id, String typeOfId);

  public List<ModelBO> validateAndGetDeployableModelsAndProjects(String clientId,
      List<ProjectModelRequest> projectModels, List<String> invalidModelIds,
      Map<Integer, String> validProjects, Map<Integer, Integer> validProjectModels, StringBuilder modelSummary, String typeOfId);

  public Boolean validateClientCreate(Client client);

  public List<DatasetBO> validateProjectTransformedStatus(String projectId);

  public Boolean validateRolesByClientId(String clientId);

  public void validateClientAppAccUpdate(String clientId, String accountId, String appId,
      MwbItsClientMapBO mwbItsClientMapBO);

  public void validateProjectIdForModel(String clientId, PatchRequest patchRequest, String projectId);

  public Boolean validateLiveAndPreviewModelId(String clientId, PatchRequest patchRequest, String projectId);

  public void validateDeployableModel(String clientId, String projectId, String modelId);

  public void validatePreviewAndLiveModel(String clientId, String projectId, String modelId);

  /**
   * validate vectorizer record
   * @param id vectorizer record's id that needs to get validated
   * @return validated record for the requested id
   */
  public VectorizerBO validateVectorizer(String id);

  /**
   * validate preference record for the given client id and preference id
   * @param id preference record's id that needs to get validated
   * @return validated record for the requested id
   */
  public PreferencesBO validatePreference(String clientId, String id);

  /**
   * validate preference record for the given client id and preference id
   * @param level the preference's level at which the record is to be added (client/model)
   * @param attribute the id for the level (clientid/modelId)
   */
  public void validateLevelTypeAndAttribute(String level, String attribute, String type, String clientId);

  /**
   * validate preference record for the given client id and preference id
   * @param preferencesBO the preference that is getting updated
   * @param patchRequest contains the list of documents that needs to get updated
   */
  public void PreferencePatchRequest(String clientId, PreferencesBO preferencesBO, PatchRequest patchRequest);

  /**
   * validate preference level based on setdefault incase of model Level preference
   */
  public Boolean validateLevel(String level, Boolean setDefault);

  public ModelDeploymentDetailsBO validateProjectAndModelId(String projectId);
}
