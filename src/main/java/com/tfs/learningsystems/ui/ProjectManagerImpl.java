/*******************************************************************************
 * Copyright © [24]7 Customer, Inc. All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.ui;

import com.tfs.learningsystems.db.DatasetBO;
import com.tfs.learningsystems.db.JobBO;
import com.tfs.learningsystems.db.ProjectBO;
import com.tfs.learningsystems.db.ProjectDatasetBO;
import com.tfs.learningsystems.db.TaskEventBO;
import com.tfs.learningsystems.db.ModelDeploymentDetailsBO;
import com.tfs.learningsystems.db.ModelDeploymentMapBO;
import com.tfs.learningsystems.helper.JobHelper;
import com.tfs.learningsystems.json.JsonConverter;
import com.tfs.learningsystems.ui.dao.ProjectDao;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.PatchRequest;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.Project;
import com.tfs.learningsystems.ui.model.ProjectDetailDatasetTaskStatus;
import com.tfs.learningsystems.ui.model.ProjectDetail;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import com.tfs.learningsystems.ui.model.error.NotFoundException;
import com.tfs.learningsystems.util.AuthUtil;
import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.quartz.Trigger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("projectManagerBean")
public class ProjectManagerImpl implements ProjectManager {

  @Inject
  @Qualifier("projectDaoBean")
  private ProjectDao projectDao;

  @Inject
  private JsonConverter jsonConverter;

  @Inject
  @Qualifier("validationManagerBean")
  private ValidationManager validationManager;

  @Inject
  @Qualifier("projectDatasetManagerBean")
  private ProjectDatasetManager projectDatasetManager;

  @Inject
  @Qualifier("datasetManagerBean")
  private DatasetManager datasetManager;

  @Autowired
  @Qualifier("jobManagerBean")
  private JobManager jobManager;

  @Autowired
  private DataManagementManager dataManagementManager;

  @Autowired
  private JobHelper jobHelper;


  @Override
  public ProjectBO getProjectById(String projectId) {

    // TODO Client_Isolation - remove
    ProjectBO projectBO = new ProjectBO();
    projectBO = projectBO.findOne(projectId);
    return (projectBO);
  }


  @Override
  public ProjectBO getProjectById(String clientId, String projectId) {

    ProjectBO project = new ProjectBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditions.put(ProjectBO.FLD_PROJECT_ID, projectId);

    project = project.findOne(conditions);

    return (project);
  }


  @Override
  public ProjectBO getProjectById(String clientId, String projectId, boolean showDeleted) {

    ProjectBO project = new ProjectBO();
    project = project.findOne(projectId);
    if (showDeleted && !project.isDisabled()) {
      return (null);
    }
    return (project);
  }

  @Override
  @Transactional
  public ProjectBO addProject(String clientId, Project project) {
    ProjectBO projectBO = new ProjectBO();
    BeanUtils.copyProperties(project,projectBO);
    projectBO.setClientId(Integer.valueOf(clientId));
    projectBO.setVertical(project.getVertical().getName());
    projectBO.setType(ProjectBO.Type.valueOf(project.getType().toString()));
    validationManager.validateProjectCreate(clientId, projectBO);
    validationManager.validateProjectNameExists(clientId, projectBO.getName());
    validationManager.validateGlobalProjectName(projectBO.getType(), projectBO.getName());
    projectBO.setState(ProjectBO.State.ENABLED);
    long currentTime = Calendar.getInstance().getTimeInMillis();
    projectBO.setCreatedAt(currentTime);
    projectBO.setModifiedAt(currentTime);
    projectBO.setModifiedBy(AuthUtil.getPrincipalFromSecurityContext(null));
    projectBO.create();

    return (projectBO);
  }

  @Override
  @Transactional
  public ProjectDetail deleteProject(String clientId, String projectId, String currentUserEmail) {

    ProjectBO project = validationManager.validateClientAndProject(clientId, projectId);

    project.setState(ProjectBO.State.DISABLED);
    project.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    project.setModifiedBy(currentUserEmail);
    project.update();

    ProjectDetail projectDetail = new ProjectDetail();
    projectDetail
            .id(project.getId().toString())
            .name(project.getName())
            .clientId(project.getClientId().toString());

    return projectDetail;
  }

  @Override
  public void undeleteProject(String clientId, String projectId, String currentUserEmail) {

    ProjectBO project = validationManager.validateClientAndProject(clientId, projectId);

    if (!project.isDisabled()) {
      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setErrorCode("project_not_disabled");
      error.setMessage(
          "Model with id '" + project.getId() + "' is not disabled, cannot enable");
      throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    } else {
      project.setState(ProjectBO.State.ENABLED);
      project.setModifiedAt(Calendar.getInstance().getTimeInMillis());
      project.setModifiedBy(currentUserEmail);
      project.update();
    }

  }

  @Override
  public ProjectBO updateProject(String clientId, String projectId, PatchRequest patchRequest,
      String currentUserEmail) {

    validationManager.validatePatchCall(patchRequest, Constants.PROJECT_FIELDS);

    ProjectBO unmodifiedProject = validationManager.validateClientAndProject(clientId, projectId);

    // For Project Rename Feature checking if projectname already exist?
    validationManager.ifProjectNameChangePatchRequest(clientId, unmodifiedProject.getType(), patchRequest);

    validationManager.validateProjectIdForModel(clientId, patchRequest, projectId);

    boolean isLiveModelIdUpdate = validationManager.validateLiveAndPreviewModelId(clientId, patchRequest, projectId);

    if(Boolean.TRUE.equals(isLiveModelIdUpdate)) {
        this.updateDeploymentDetails(projectId);
    }

    ProjectBO modifiedProject = jsonConverter.patch(patchRequest, unmodifiedProject, ProjectBO.class);
    modifiedProject.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    modifiedProject.setModifiedBy(currentUserEmail);
    modifiedProject.update();

    return (modifiedProject);
  }

  @Override
  public ModelDeploymentDetailsBO updateDeploymentDetails(String projectId) {
    ModelDeploymentDetailsBO modelDeploymentDetailsBO = new ModelDeploymentDetailsBO();
    modelDeploymentDetailsBO = validationManager.validateProjectAndModelId(projectId);
    return (modelDeploymentDetailsBO);
  }

  @Override
  public ProjectBO demoteProject(String clientId, String projectId) {

    ProjectBO unmodifiedGlobalProject = validationManager.validateClientAndProject(clientId, projectId);
    ProjectBO modifiedGlobalProject = this.updateNameAndGlobalType(unmodifiedGlobalProject);
    return (modifiedGlobalProject);
  }

  @Override
  public List<ProjectDetailDatasetTaskStatus> promoteProject(String clientId, String projectId, String globalProjectId, String globalProjectName) {

    ProjectBO unmodifiedProject = validationManager.validateClientAndProject(clientId, projectId);
    List<Integer> projectIDs = new ArrayList<>();

    if(globalProjectName != null && !globalProjectName.isEmpty()) {
      validationManager.validateGlobalProjectName(ProjectBO.Type.GLOBAL.toString(), globalProjectName);
      if(globalProjectId != null && !globalProjectId.isEmpty()) {
        ProjectBO unmodifiedGlobalProject = validationManager.validateClientAndProject(clientId, globalProjectId);
        if(!unmodifiedGlobalProject.getName().equalsIgnoreCase(globalProjectName)){
          Error error = new Error();
          error.setCode(Response.Status.NOT_FOUND.getStatusCode());
          error.setErrorCode("global_data_provided_mismatch");
          error.setMessage("Unable to promote model. Mismatch global data");
          throw new NotFoundException(error);
        }

        ProjectBO modifiedGlobalProject = this.updateNameAndGlobalType(unmodifiedGlobalProject);
        projectIDs.add(modifiedGlobalProject.getId());

      } else if(globalProjectId == null) {
        // checking if global project with same name already exist?
        validationManager.validateProjectNameExists(clientId, globalProjectName);
      }
    } else {
      Error error = new Error();
      error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
      error.setMessage("Global model name is null");
      throw new BadRequestException(
              Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }

    unmodifiedProject.setOriginalName(unmodifiedProject.getName());
    unmodifiedProject.setName(globalProjectName);
    unmodifiedProject.setType(ProjectBO.Type.GLOBAL);
    unmodifiedProject.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    unmodifiedProject.update();
    projectIDs.add(unmodifiedProject.getId());

    List<ProjectDetailDatasetTaskStatus> projects = projectDao.getProjects(0, 2, "", "ID",
            "ASC", false, clientId, projectIDs);

    return (projects);
  }

  private ProjectBO updateNameAndGlobalType(ProjectBO unmodifiedGlobalProject) {
    Long timestamp = Calendar.getInstance().getTimeInMillis();
    if(unmodifiedGlobalProject.getOriginalName() != null  && !unmodifiedGlobalProject.getOriginalName().isEmpty()){
      unmodifiedGlobalProject.setName(unmodifiedGlobalProject.getOriginalName());
    }else {
      unmodifiedGlobalProject.setName(unmodifiedGlobalProject.getName() + "_" + timestamp);
    }

    unmodifiedGlobalProject.setType(ProjectBO.Type.NODE);
    unmodifiedGlobalProject.setOriginalName("");
    unmodifiedGlobalProject.setModifiedAt(timestamp);
    unmodifiedGlobalProject.update();
    return unmodifiedGlobalProject;
  }

  @Override
  public List<ProjectDetailDatasetTaskStatus> getProjects(String filterClientId, int startIndex,
      int count,
      String filter, String sortBy, String sortOrder, boolean showDeleted) {

    return projectDao.getProjects(startIndex, count, filter, sortBy, sortOrder, showDeleted,
        filterClientId, null);
  }

  @Override
  public long countProjects(String clientId, boolean showDeleted) {

    ProjectBO projectBO = new ProjectBO();
    Map<String, Object> params = new HashMap<>();
    params.put(ProjectBO.FLD_STATE,
        showDeleted ? ProjectBO.State.DISABLED.toString() : ProjectBO.State.ENABLED.toString());
    return (projectBO.count(params));

  }

  @Override
  public List<DatasetBO> hasTransformedDataset(String projectId) {

    validationManager.validateProjectId(projectId);

    List<DatasetBO> datasets = projectDatasetManager.listDatasetsForProjectById(projectId,
        0, Integer.MAX_VALUE, "", "ID", "ASC");

    List<DatasetBO> transformedDatasets = new ArrayList<>();
    for (DatasetBO ds : datasets) {
      try {
        TaskEventBO transformStatus =
            dataManagementManager.status(null, projectId, Integer.toString(ds.getId()));

        if (null != transformStatus && transformStatus.getPercentComplete() == 100) {
          transformedDatasets.add(ds);
        }
      } catch (NotFoundException ex) {
        log.info("Not found transformed dataset for project - {}", projectId);
        // Catching NotFoundException
      }
    }

    return transformedDatasets;

  }

    /*  // TODO Client_Isolation done on Datasets API
    @Override
    public List<DatasetBO> hasTransformedDataset (String clientId ,String projectId) {

        validationManager.validateClientAndProject(clientId, projectId);

        List<DatasetBO> datasets = projectDatasetManager.listDatasetsForProjectById(projectId,
        0, Integer.MAX_VALUE, "", "ID", "ASC");

        List<DatasetBO> transformedDatasets = new ArrayList<>();
        for (DatasetBO ds : datasets) {
            try {
                TaskEventBO transformStatus =
                dataManagementManager.status(clientId, projectId, Integer.toString(ds.getId()));
                if (null != transformStatus&&transformStatus.getPercentComplete() == 100) {
                    transformedDatasets.add(ds);
                }
            } catch (NotFoundException ex) {
                log.info("Not found transformed dataset for project - {}", projectId);
                // Catching NotFoundException
            }
        }
        return transformedDatasets;
    }

    */

  @Override
  public void addDatasetProjectMapping(String clientId, String projectId, String datasetId,
      String currentUserEmail) {

    ProjectBO projectBO = validationManager.validateProject(clientId, projectId);

    DatasetBO dataset = validationManager.validateClientAndDataset(clientId, datasetId);

    if (dataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("dataset_not_found");
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new NotFoundException(error);
    } else if (Boolean.TRUE.equals(this.validationManager.ifValidClientProjectDatasetEntry(projectId, datasetId))) {
      Error error = new Error();
      error.setCode(Response.Status.CONFLICT.getStatusCode());
      error.setErrorCode("mapping_already_exists");
      error.setMessage("Model '" + projectId + "' already has dataset '" + datasetId + "'");
      throw new AlreadyExistsException(error);
    }

    projectBO.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    projectBO.setModifiedBy(currentUserEmail);
    projectBO.update();

    ProjectDatasetBO projectDatasetBO = new ProjectDatasetBO();
    projectDatasetBO.setProjectId(Integer.parseInt(projectId));
    projectDatasetBO.setDatasetId(Integer.parseInt(datasetId));
    projectDatasetBO.setCid(projectBO.getCid());
    projectDatasetBO.create();

  }

  @Override
  @Transactional
  public void removeDatasetProjectMapping(String clientId, String datasetId, String projectId,
      String currentUserEmail) {

    ProjectBO project = new ProjectBO();

    Map<String, Object> projectConditions = new HashMap<>();
    projectConditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    projectConditions.put(ProjectBO.FLD_PROJECT_ID, projectId);

    project = project.findOne(projectConditions);

    DatasetBO dataset = new DatasetBO();

    Map<String, Object> dataSetConditions = new HashMap<>();

    dataSetConditions.put(DatasetBO.FLD_CLIENT_ID, clientId);
    dataSetConditions.put(DatasetBO.FLD_DATASET_ID, datasetId);

    dataset = dataset.findOne(dataSetConditions);

    if (dataset == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("dataset_not_found");
      error.setMessage("Dataset '" + datasetId + "' not found");
      throw new NotFoundException(error);
    } else if (project == null) {
      Error error = new Error();
      error.setCode(Response.Status.NOT_FOUND.getStatusCode());
      error.setErrorCode("project_not_found");
      error.setMessage("Project '" + projectId + "' not found");
      throw new NotFoundException(error);
    }

    JobBO jobDetail = this.jobManager.getJobByProjectDataset(projectId, datasetId);

    if (jobDetail != null) {
      org.quartz.JobDetail deleteJobDetail =
          this.jobHelper.createDeleteDataJobDetail(projectId, datasetId);
      Trigger createTrigger =
          this.jobHelper.createTrigger(deleteJobDetail, false);

      if (!this.jobHelper.scheduleJob(deleteJobDetail,
          createTrigger)) {
        throw new ServerErrorException("Failed to start remove data job",
            Status.SERVICE_UNAVAILABLE);
      }

      String jobId = Integer.toString(jobDetail.getId());
      this.jobManager.deleteTasksForJobId(jobId);
      jobDetail.delete();

      try {
        Path buildJobPath =
            this.jobHelper.buildJobPath(Integer.toString(project.getClientId()), projectId, jobId);
        FileUtils.cleanDirectory(buildJobPath.toFile());
        Files.delete(buildJobPath);
      } catch (IOException e) {
        log.warn("Failed to clean job directory - {} - {}", projectId, jobId);
        throw new ServerErrorException(ErrorMessage.FILE_ACCESS_MESSAGE,
            Status.INTERNAL_SERVER_ERROR, e);
      }
    }
    this.projectDatasetManager.removeDatasetProjectMapping(clientId, datasetId, projectId);

    project.setModifiedAt(Calendar.getInstance().getTimeInMillis());
    project.setModifiedBy(currentUserEmail);
    project.update();
  }

  @Override
  public void removeProjectDatasetMapping(String clientId, String projectId) {

    this.projectDatasetManager.removeProjectDatasetMapping(projectId);
  }

  @Override
  public Long countDatasetsForProjectById(String clientId, String projectId) {

    return this.projectDatasetManager.countDatasetsForProjectById(clientId, projectId);
  }

  @Override
  public List<DatasetBO> listDatasetsForProjectById(String clientId, String projectId,
      Integer startIndex,
      Integer limit, String filter, String sortBy, String sortOrder) {

    return this.projectDatasetManager.listDatasetsForProjectById(projectId, startIndex, limit,
        filter, sortBy, sortOrder);
  }

  @Override
  public Locale getProjectLocale(String clientId, String projectId) {

    ProjectBO project = new ProjectBO();
    Map<String, Object> conditions = new HashMap<>();
    conditions.put(ProjectBO.FLD_CLIENT_ID, clientId);
    conditions.put(ProjectBO.FLD_PROJECT_ID, projectId);

    project = project.findOne(conditions);

    if (project == null) {
      return Locale.getDefault();
    }
    String projectLocale = project.getLocale();
    if (projectLocale == null) {
      return Locale.getDefault();
    }

    return Locale.forLanguageTag(projectLocale);
  }


  @Override
  public Locale getProjectLocale(String projectId) {

    //TODO Client_Isolation - remove

    ProjectBO project = new ProjectBO();
    project = project.findOne(projectId);
    if (project == null) {
      return Locale.getDefault();
    }
    String projectLocale = project.getLocale();
    if (projectLocale == null) {
      return Locale.getDefault();
    }

    return Locale.forLanguageTag(projectLocale);
  }

  @Override
  public void addDatasetIntentInheritance(final String datasetId,
      final String projectId, final String currentUserEmail) {

    datasetManager.addDatasetIntentInheritance(datasetId, projectId, currentUserEmail);
  }
}
