/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/

package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ModelDeploymentDetailsBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.Project;
import com.tfs.learningsystems.ui.model.ProjectDetailDatasetTaskStatus;
import com.tfs.learningsystems.ui.model.ProjectDetail;

import java.util.List;
import java.util.Locale;

public interface ProjectManager {


  /**
   * Return project based on id
   *
   * @return ProjectDetail
   */
  public ProjectBO getProjectById(String projectId);

  /**
   * Return project based on id
   *
   * @return ProjectDetail
   */
  public ProjectBO getProjectById(String clientId, String projectId);

  /**
   * Return project based on id, include soft deleted in search
   *
   * @return ProjectDetail
   */
  public ProjectBO getProjectById(String clientId, String projectId, boolean showDeleted);

  /**
   * Add project
   *
   * @return ProjectDetail of added object
   */
  public ProjectBO addProject(String clientId, Project project);

  /**
   * Soft delete project
   */
  public ProjectDetail deleteProject(String clientId, String projectId, String currentUserId);

  /**
   * undo soft delete of project
   */
  public void undeleteProject(String clientId, String projectId, String currentUserId);

  /**
   * Update project data
   *
   * @return updated project
   */
  public ProjectBO updateProject(String clientId, String projectId, PatchRequest patchRequest,
      String currentUserId);

  /**
   * Promote project data
   *
   * @return promote project
   */
  public List<ProjectDetailDatasetTaskStatus> promoteProject(String clientId, String projectId, String globalProjectId, String globalProjectName);

  /**
   * Demote project data
   *
   * @return demoted project
   */
  public ProjectBO demoteProject(String clientId, String projectId);

  /**
   * List projects
   *
   * @return list of project details
   */
  public List<ProjectDetailDatasetTaskStatus> getProjects(String filterClientId, int startIndex,
      int count, String filter, String sortBy,
      String sortOrder, boolean showDeleted);

  /**
   * Count projects
   *
   * @param showDeleted whether to count soft deleted items
   * @return count of projects
   */
  public long countProjects(String clientId, boolean showDeleted);

//    /**
//     * test if group is associated to a project
//     * @param groupId
//     * @return <code>true</code> if group is associated to a project, else <code>false</code>
//     */
//    public boolean isGroupAssociatedToProject(String groupId);
//
//    /**
//     * test if user is owner of a project
//     * @param userId
//     * @param groupId
//     * @return <code>true</code> if user is owner of a project, else <code>false</code>
//     */
//    public boolean isUserOwnerOfProject(String userId, String groupId);

  /**
   * test whether project has a transformed dataset
   *
   * @return <code>true</code> if project has a transformed dataset, else <code>false</code>
   */

  //TODO Client_Isolation to use when Datasets API uses clientIsolation
  //  public List<DatasetBO> hasTransformedDataset(String clientId ,String projectId);
  public List<DatasetBO> hasTransformedDataset(String projectId);

  /**
   * Map data set to project
   */
  public void addDatasetProjectMapping(String clientId, String projectId, String datasetId,
      String currentUserId);

  /**
   * remove dataset to project mapping
   */
  public void removeDatasetProjectMapping(String clientId, String datasetId, String projectId,
      String currentUserId);

  /**
   * remove all mappings for a project
   */
  public void removeProjectDatasetMapping(String clientId, String projectId);

  /**
   * @return count of datasets mapped to specified project
   */
  public Long countDatasetsForProjectById(String clientId, String projectId);

  /**
   * @return list of dataset details mapped to specified project
   */
  public List<DatasetBO> listDatasetsForProjectById(String clientId, String projectId,
      Integer startIndex, Integer limit, String filter, String sortBy,
      String sortOrder);

  /**
   * Get locale for the project
   */
  public Locale getProjectLocale(String clientId, String projectId);

  /**
   * Get locale for the project
   */
  public Locale getProjectLocale(String projectId);

  /**
   * set a dataset on a project to auto tag
   */
  public void addDatasetIntentInheritance(final String datasetId,
      final String projectId, final String currentUserId);

  /**
   * Update deployment details on update of liveModelId in project
   */
  public ModelDeploymentDetailsBO updateDeploymentDetails(String projectId);
}
