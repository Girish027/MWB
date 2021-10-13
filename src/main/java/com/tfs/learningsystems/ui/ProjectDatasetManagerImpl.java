/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.ProjectDatasetBO;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.dao.ProjectDatasetDao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author jkarpala
 */
@Slf4j
@Component
@Qualifier("projectDatasetManagerBean")
public class ProjectDatasetManagerImpl implements ProjectDatasetManager {

  @Inject
  @Qualifier("projectDatasetDaoBean")
  private ProjectDatasetDao projectDatasetDao;

  @Inject
  private JsonConverter jsonConverter;

  /* (non-Javadoc)
   * @see com.tfs.learningsystems.ui.ProjectDatasetManager#countDatasetsForProjectById(java.lang.String)
   */
  @Override
  public Long countDatasetsForProjectById(String clientId, String projectId) {

    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    if(ActionContext.getClientId()!=null) {
      paramMap.put(ProjectDatasetBO.FLD_CID, ActionContext.getClientId());
    }
    paramMap.put(ProjectDatasetBO.FLD_PROJECT_ID, projectId);

    return (projectDataset.count(paramMap));
  }

  @Override
  public List<Integer> listDatasetIdsByProjectId(String projectId) {
    return projectDatasetDao.getDatasetIds(projectId);
  }

  /* (non-Javadoc)
   * @see com.tfs.learningsystems.ui.ProjectDatasetManager#listDatasetsForProjectById(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<DatasetBO> listDatasetsForProjectById(String projectId, Integer startIndex,
                                                    Integer limit, String filter, String sortBy, String sortOrder) {
    return projectDatasetDao.getDatasets(projectId, startIndex, limit, filter, sortBy,
            sortOrder);
  }

  /* (non-Javadoc)
   * @see com.tfs.learningsystems.ui.ProjectDatasetManager#removeDatasetProjectMapping(java.lang.String)
   */
  @Override
  public void removeDatasetProjectMapping(String datasetId) {

    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ProjectDatasetBO.FLD_DATASET_ID, datasetId);
    List<ProjectDatasetBO> all = projectDataset.list(paramMap, null);
    for (ProjectDatasetBO one : all) {
      log.info("Removing mapping from dataset {} to project {}", datasetId, one.getProjectId());
      one.delete();
    }
  }


  /*
   * (non-Javadoc)
   *
   * @see com.tfs.learningsystems.ui.DatasetManager#removeDatasetProjectMapping(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void removeDatasetProjectMapping(String clientId, String datasetId, String projectId) {
    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    if(ActionContext.getClientId()!=null)
      paramMap.put(ProjectDatasetBO.FLD_CID, ActionContext.getClientId());
    paramMap.put(ProjectDatasetBO.FLD_PROJECT_ID, projectId);
    paramMap.put(ProjectDatasetBO.FLD_DATASET_ID, datasetId);

    List<ProjectDatasetBO> all = projectDataset.list(paramMap, null);
    for (ProjectDatasetBO one : all) {
      log.info("Removing mapping from dataset {} to project {}", datasetId, one.getProjectId());
      one.delete();
    }

  }


  /*
   * (non-Javadoc)
   *
   * @see com.tfs.learningsystems.ui.DatasetManager#removeProjectDatasetMapping(java.lang.String,)
   */
  @Override
  public void removeProjectDatasetMapping(String projectId) {
    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ProjectDatasetBO.FLD_PROJECT_ID, projectId);

    List<ProjectDatasetBO> all = projectDataset.list(paramMap, null);
    for (ProjectDatasetBO one : all) {
      log.info("Removing mapping from dataset {} to project {}", one.getDatasetId(), projectId);
      one.delete();
    }
  }


  @Override
  public boolean isProjectDatasetValid(String projectId, String datasetId) {
    //TODO Client_Isolation remove
    return (getProjectDataset(projectId, datasetId) != null);
  }

  /**
   * Get project dataset object given projectId and datasetId
   *
   * @return ProjectDataset object
   */

  public ProjectDatasetBO getProjectDataset(String projectId, String datasetId) {

    ProjectDatasetBO projectDataset = new ProjectDatasetBO();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(ProjectDatasetBO.FLD_PROJECT_ID, projectId);
    paramMap.put(ProjectDatasetBO.FLD_DATASET_ID, datasetId);
    projectDataset = projectDataset.findOne(paramMap);
    return projectDataset;
  }
}
