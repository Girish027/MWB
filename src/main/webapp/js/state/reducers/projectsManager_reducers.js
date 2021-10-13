import { Map } from 'immutable';
import _ from 'lodash';
import * as types from 'state/actions/types';
import Constants from 'constants/Constants';

export const defaultState = {
  projects: Map(),
  projectDatasets: Map(),
  trackDatasetsIds: {},
  projectModels: Map(),
  projectIntents: Map(),
  trackModelsIds: {},
  trackModelsBatchTestIds: {},
  speechModelTestResults: {},
  digitalModelTestResults: {},
  modelBatchTestResults: {},
  modelBatchJobRequest: {},
  projectConfigs: Map(),
  model: null,
  tuneModelId: null,
  viewModelId: null,
  modelViewReadOnly: false,
  clientDataLoaded: false,
  datasetsLoaded: false,
  modelsLoaded: false,
  configsLoaded: false,
  intentsLoaded: false,
  listOfBatchTests: { },
};

const isClientDataLoaded = (state, stateSlice) => {
  let isDataLoaded = false;

  switch (stateSlice) {
  case 'models':
    isDataLoaded = state.configsLoaded && state.datasetsLoaded && state.intentsLoaded;
    break;
  case 'datasets':
    isDataLoaded = state.configsLoaded && state.modelsLoaded && state.intentsLoaded;
    break;
  case 'configs':
    isDataLoaded = state.modelsLoaded && state.datasetsLoaded && state.intentsLoaded;
    break;
  case 'intents':
    isDataLoaded = state.modelsLoaded && state.datasetsLoaded && state.configsLoaded;
    break;
  default:
    isDataLoaded = state.modelsLoaded
        && state.datasetsLoaded
        && state.configsLoaded
        && state.intentsLoaded;
    break;
  }

  return isDataLoaded;
};

function projectsManagerReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_PROJECT_LOAD_SUCCESS: {
    const { projectId, project } = action;
    return Object.assign({}, state, {
      projects: state.projects.set(projectId, project),
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS: {
    const { projectId, datasets } = action;

    const { trackDatasetsIds } = state;
    datasets.forEach((d) => {
      if (
        d.status === 'QUEUED'
          || d.status === 'RUNNING'
          || d.status === 'STARTED'

      ) {
        if (!trackDatasetsIds[projectId]) {
          trackDatasetsIds[projectId] = {};
        }
        trackDatasetsIds[projectId][d.id] = d.id;
      } else if (trackDatasetsIds[projectId]) {
        delete trackDatasetsIds[projectId][d.id];
        if (Object.keys(trackDatasetsIds[projectId]).length === 0) {
          delete trackDatasetsIds[projectId];
        }
      }
    });

    const clientDataLoaded = isClientDataLoaded(state, 'datasets');

    return Object.assign({}, state, {
      projectDatasets: state.projectDatasets.set(projectId, datasets),
      trackDatasetsIds,
      datasetsLoaded: true,
      clientDataLoaded,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_PROJECT_MODELS_LOAD_SUCCESS: {
    const { projectId, models } = action;
    const { trackModelsIds } = state;

    models.forEach((model) => {
      if (
        model.status === 'QUEUED'
            || model.status === 'RUNNING'
      ) {
        if (!trackModelsIds[projectId]) {
          trackModelsIds[projectId] = {};
        }
        trackModelsIds[projectId][model.id] = model.id;
      } else if (trackModelsIds[projectId]) {
        delete trackModelsIds[projectId][model.id];
        if (Object.keys(trackModelsIds[projectId]).length === 0) {
          delete trackModelsIds[projectId];
        }
      }
    });

    const clientDataLoaded = isClientDataLoaded(state, 'models');

    return Object.assign({}, state, {
      projectModels: state.projectModels.set(projectId, models),
      trackModelsIds,
      modelsLoaded: true,
      clientDataLoaded,
    });
  }

  case types.PROJECTS_MANAGER_PROJECT_INTENTS_LOAD_SUCCESS: {
    const { projectId, intents } = action;
    let projectIntents = new Map(state.projectIntents);
    projectIntents = projectIntents.set(projectId, intents);

    const clientDataLoaded = isClientDataLoaded(state, 'intents');
    return Object.assign({}, state, {
      projectIntents,
      intentsLoaded: true,
      clientDataLoaded,
    });
  }

  case types.TAGGING_GUIDE_RECEIVE_TAG_CREATE: {
    const { projectId, values } = action;
    let projectIntents = new Map(state.projectIntents);
    let searchResults = projectIntents.get(projectId);
    searchResults = [values, ...searchResults];
    projectIntents = projectIntents.set(projectId, searchResults);
    return Object.assign({}, state, {
      projectIntents,
    });
  }

  case types.TAGGING_GUIDE_RECEIVE_TAG_REMOVE: {
    const { projectId, index } = action;
    let projectIntents = new Map(state.projectIntents);
    let searchResults = projectIntents.get(projectId);
    searchResults.splice(index, 1);
    projectIntents = projectIntents.set(projectId, searchResults);
    return Object.assign({}, state, {
      projectIntents,
    });
  }

  case types.TAGGING_GUIDE_RECEIVE_TAG_UPDATE: {
    const { projectId, values } = action;
    let projectIntents = new Map(state.projectIntents);
    let searchResults = projectIntents.get(projectId);
    let existingTag = searchResults.findIndex(item => item.id === values.id);
    searchResults[existingTag] = Object.assign({}, searchResults[existingTag], values);
    projectIntents = projectIntents.set(projectId, searchResults);
    return Object.assign({}, state, {
      projectIntents,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_DATASETS_STATUSES_LOAD_SUCCESS: {
    const { statuses } = action;
    let { trackDatasetsIds, projectDatasets } = state;

    for (const projectId in statuses) {
      if (!statuses.hasOwnProperty(projectId)) {
        continue;
      }
      let datasets = projectDatasets.get(projectId);
      if (!datasets) {
        continue;
      }
      for (const datasetId in statuses[projectId]) {
        if (!statuses[projectId].hasOwnProperty(datasetId)) {
          continue;
        }
        let dataset = datasets.get(datasetId);
        if (!dataset) {
          continue;
        }
        dataset = Object.assign({}, dataset, {
          task: statuses[projectId][datasetId].task ? (`${statuses[projectId][datasetId].task}`).toUpperCase() : 'NULL',
          status: statuses[projectId][datasetId].status ? (`${statuses[projectId][datasetId].status}`).toUpperCase() : 'NULL',
          errorCode: statuses[projectId][datasetId].errorCode,
          percentComplete: statuses[projectId][datasetId].percentComplete,
        });
        datasets = datasets.set(datasetId, dataset);

        if (
          dataset.status !== 'QUEUED'
            && dataset.status !== 'RUNNING'
            && dataset.status !== 'STARTED'
        ) {
          if (trackDatasetsIds[projectId]) {
            delete trackDatasetsIds[projectId][datasetId];
            if (Object.keys(trackDatasetsIds[projectId]).length === 0) {
              delete trackDatasetsIds[projectId];
            }
          }
        }
      }
      projectDatasets = projectDatasets.set(projectId, datasets);
    }
    /* TODO: optimization of immutable maping */

    return Object.assign({}, state, {
      projectDatasets,
      trackDatasetsIds,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_PROJECT_CONFIGS_LOAD_SUCCESS: {
    const { projectId, configs } = action;

    const clientDataLoaded = isClientDataLoaded(state, 'configs');
    return Object.assign({}, state, {
      projectConfigs: state.projectConfigs.set(projectId, configs),
      configsLoaded: true,
      clientDataLoaded,
    });
  }

  case types.PROJECT_CLOSE:
  case types.CLEAR_SELECTED_CLIENT:
  case types.CLIENT_CHANGE: {
    return Object.assign({}, state, defaultState);
  }

  case types.RECEIVE_PROJECTS_BY_CLIENT: {
    const projectList = action.projects;
    let { projects, projectDatasets, trackDatasetsIds } = state;
    if (projectList.length) {
      projectList.forEach((p) => {
        const project = {/* new Project({ */
          id: p.id,
          clientId: p.clientId,
          name: p.name,
          created: p.createdAt,
          locale: p.locale,
          description: p.description,
          vertical: p.vertical,
          deployableModelId: p.deployableModelId,
          previewModelId: p.previewModelId,
          liveModelId: p.liveModelId,
        };
        const projectId = project.id;
        projects = projects.set(project.id, project);

        const { datasetTaskStatusList } = p;
        if (datasetTaskStatusList && datasetTaskStatusList.length) {
          datasetTaskStatusList.forEach((d) => {
            let datasets = projectDatasets.get(project.id);
            if (!datasets) {
              datasets = Map();
            }
            let dataset = datasets.get(d.id);
            if (!dataset) {
              dataset = {}; /* new Dataset( */
            }
            dataset.id = d.id;
            dataset.projectId = projectId;
            dataset.clientId = project.clientId;
            dataset.name = d.name;
            dataset.status = d.status ? (`${d.status}`).toUpperCase() : 'NULL';
            dataset.task = d.task ? (`${d.task}`).toUpperCase() : 'NULL';
            dataset.percentComplete = d.percentComplete ? d.percentComplete : null;

            datasets = datasets.set(dataset.id, dataset);
            projectDatasets = projectDatasets.set(project.id, datasets);

            if (
              dataset.status === 'QUEUED'
                || dataset.status === 'RUNNING'
                || dataset.status === 'STARTED'
            ) {
              if (!trackDatasetsIds[projectId]) {
                trackDatasetsIds[projectId] = {};
              }
              trackDatasetsIds[projectId][d.id] = d.id;
            } else if (trackDatasetsIds[projectId]) {
              delete trackDatasetsIds[projectId][d.id];
              if (Object.keys(trackDatasetsIds[projectId]).length === 0) {
                delete trackDatasetsIds[projectId];
              }
            }
          });
        }
      });

      return Object.assign({}, state, {
        projects,
        projectDatasets,
        trackDatasetsIds,
      });
    }
    return state;
  }

  case types.PROJECTS_MANAGER_PROJECT_UPDATED: {
    const { projectId, project } = action;
    return Object.assign({}, state, {
      projects: state.projects.set(projectId, project),
    });
  }

  case types.PROJECTS_MANAGER_DATASET_UPDATED:
  case types.PROJECT_DATASET_ADD: {
    const { projectId, datasetId, dataset } = action;
    let { projectDatasets, trackDatasetsIds } = state;
    let datasets = projectDatasets.get(projectId);
    if (!datasets) {
      datasets = Map();
    }
    const newDataset = Object.assign({ id: datasetId }, datasets.get(datasetId), dataset);
    datasets = datasets.set(datasetId, newDataset);
    projectDatasets = projectDatasets.set(projectId, datasets);
    if (
      newDataset.status === 'QUEUED'
        || newDataset.status === 'RUNNING'
        || newDataset.status === 'STARTED'

    ) {
      if (!trackDatasetsIds[projectId]) {
        trackDatasetsIds[projectId] = {};
      }
      trackDatasetsIds[projectId][newDataset.id] = newDataset.id;
    } else if (trackDatasetsIds[projectId]) {
      delete trackDatasetsIds[projectId][newDataset.id];
      if (Object.keys(trackDatasetsIds[projectId]).length === 0) {
        delete trackDatasetsIds[projectId];
      }
    }

    return Object.assign({}, state, {
      projectDatasets,
      trackDatasetsIds,
    });
  }

  case types.ADD_NEW_PROJECT: {
    const { project } = action;

    return Object.assign({}, state, {
      projects: state.projects.set(project.id, project),
    });
  }

  case types.PROJECT_UPDATE: {
    const { projects } = action;
    if (projects.length > 0) {
      let index;
      let clonedProjects = new Map(state.projects);
      for (index = 0; index < projects.length; index++) {
        const { id } = projects[index];
        const stateProject = clonedProjects.get(id);
        if (stateProject && id) {
          const updatedProject = Object.assign({}, stateProject, projects[index]);
          clonedProjects = clonedProjects.set(id, updatedProject);
        }
      }

      return Object.assign({}, state, {
        projects: clonedProjects,
      });
    }
    return state;
  }

  case types.PROJECT_STATUS_UPDATE: {
    const { projectId, status } = action;
    let projects = new Map(state.projects);
    if (projectId) {
      let project = projects.get(projectId);
      const updatedProject = Object.assign({}, project, {
        status,
      });
      projects = projects.set(projectId, updatedProject);
      return Object.assign({}, state, {
        projects,
      });
    }
    return state;
  }

  case types.PROJECT_DELETE: {
    let index;
    let found = false;
    const projectId = action.id;
    if (state.projects.get(projectId)) {
      found = true;
    }
    if (found) {
      let projectDatasets = new Map(state.projectDatasets);
      let projectModels = new Map(state.projectModels);
      let projectIntents = new Map(state.projectIntents);
      let projectConfigs = new Map(state.projectConfigs);
      let projects = new Map(state.projects);
      let trackDatasetsIds = Object.assign({}, state.trackDatasetsIds);
      let trackModelsIds = Object.assign({}, state.trackModelsIds);
      if (!_.isEmpty(trackDatasetsIds) && Object.keys(trackDatasetsIds[projectId]).length > 0) {
        delete trackDatasetsIds[projectId];
      }

      if (!_.isEmpty(projectDatasets) && projectDatasets.get(projectId)) {
        projectDatasets = projectDatasets.delete(projectId);
      }

      if (!_.isEmpty(projectModels) && projectModels.get(projectId)) {
        projectModels = projectModels.delete(projectId);
      }

      if (!_.isEmpty(projectIntents) && projectIntents.get(projectId)) {
        projectIntents = projectIntents.delete(projectId);
      }

      if (!_.isEmpty(projectConfigs) && projectConfigs.get(projectId)) {
        projectConfigs = projectConfigs.delete(projectId);
      }
      if (!_.isEmpty(trackModelsIds) && !_.isEmpty(trackModelsIds[projectId]) && Object.keys(trackModelsIds[projectId]).length > 0) {
        delete trackModelsIds[projectId];
      }
      projects = projects.delete(projectId);

      return Object.assign({}, state, {
        projects,
        projectDatasets,
        trackDatasetsIds,
        projectModels,
        projectIntents,
        trackModelsIds,
        projectConfigs,
      });
    }
    return state;
  }

  case types.DATASET_REMOVED_FROM_PROJECT: {
    const { projectId, datasetId } = action;
    let { projectDatasets, trackDatasetsIds } = state;

    let datasets = projectDatasets.get(projectId);
    if (datasets) {
      const dataset = datasets.get(datasetId);
      if (dataset) {
        datasets = datasets.delete(datasetId);
        projectDatasets = projectDatasets.set(projectId, datasets);

        if (
          trackDatasetsIds[projectId]
            && (
              datasets.status === 'QUEUED'
              || datasets.status === 'RUNNING'
              || datasets.status === 'STARTED'
            )
        ) {
          delete trackDatasetsIds[projectId][datasets.id];
          if (Object.keys(trackDatasetsIds[projectId]).length === 0) {
            delete trackDatasetsIds[projectId];
          }
        }
      }
    }

    return Object.assign({}, state, {
      projectDatasets,
      trackDatasetsIds,
    });
  }

  case types.MODEL_DELETED_FROM_PROJECT: {
    const { projectId, modelId } = action;
    let { projectModels, trackModelsIds } = state;
    const { QUEUED, RUNNING } = Constants.STATUS;

    let models = projectModels.get(projectId);
    let trackModelIds = _.cloneDeep(trackModelsIds);

    if (models) {
      const model = models.get(modelId);
      if (model) {
        models = models.delete(modelId);
        projectModels = projectModels.set(projectId, models);

        if (
          trackModelIds[projectId]
               && (
                 model.status === QUEUED,
                 model.status === RUNNING
               )
        ) {
          delete trackModelIds[projectId][model.id];
          if (Object.keys(trackModelIds[projectId]).length === 0) {
            delete trackModelIds[projectId];
          }
        }
      }
    }

    return Object.assign({}, state, {
      projectModels,
      trackModelsIds: trackModelIds,
    });
  }

  // TODO: both CONFIG_CREATED and CONFIG_UPDATED looks the same. Please test and combine them.
  case types.CONFIG_CREATED: {
    const { config } = action;
    let { projectConfigs } = state;
    if (config.id && config.projectId) {
      let configs = projectConfigs.get(config.projectId);
      if (!configs) {
        configs = Map();
      }
      const newConfig = Object.assign({}, configs.get(config.id), config);
      configs = configs.set(newConfig.id, newConfig);
      projectConfigs = projectConfigs.set(config.projectId, configs);

      return Object.assign({}, state, {
        projectConfigs,
      });
    }
    return state;
  }

  case types.CONFIG_UPDATED: {
    const { config } = action;
    let { projectConfigs } = state;
    if (config.id && config.projectId) {
      let configs = projectConfigs.get(config.projectId);
      if (!configs) {
        configs = Map();
      }
      const newConfig = Object.assign({}, configs.get(config.id), config);
      configs = configs.set(newConfig.id, newConfig);
      projectConfigs = projectConfigs.set(config.projectId, configs);

      return Object.assign({}, state, {
        projectConfigs,
      });
    }
    return state;
  }

  case types.MODEL_UPDATE: {
    const { model } = action;
    let { projectModels } = state;
    if (model.id && model.projectId) {
      let models = projectModels.get(model.projectId);
      if (!models) {
        models = Map();
      }
      const updatedModel = Object.assign({}, models.get(model.id), model);
      models = models.set(updatedModel.id, updatedModel);
      projectModels = projectModels.set(model.projectId, models);

      return Object.assign({}, state, {
        projectModels,
        model,
      });
    }
    return state;
  }

  case types.UPDATE_SPEECH_MODEL_ID_FOR_DIGITAL_MODEL: {
    const { modelId, projectId, speechModelId } = action;
    let projectModels = new Map(state.projectModels);
    if (modelId && projectId && speechModelId) {
      let models = projectModels.get(projectId);
      if (!models) {
        models = Map();
      }
      const updatedModel = Object.assign({}, models.get(modelId), { speechModelId });
      models = models.set(updatedModel.id, updatedModel);
      projectModels = projectModels.set(projectId, models);
      return Object.assign({}, state, {
        projectModels,
      });
    }
    return state;
  }

  case types.MODEL_CREATED: {
    const { model } = action;
    let { projectModels } = state;
    if (model.id && model.projectId) {
      let models = projectModels.get(model.projectId);
      if (!models) {
        models = Map();
      }
      const newModel = Object.assign({}, models.get(model.id), model);
      models = models.set(newModel.id, newModel);
      projectModels = projectModels.set(model.projectId, models);

      return Object.assign({}, state, {
        projectModels,
        model: null,
        tuneModelId: null,
        viewModelId: null,
        modelViewReadOnly: false,
      });
    }
    return state;
  }
  case types.CLEAR_MODEL_DATA: {
    return Object.assign({}, state, {
      model: null,
      tuneModelId: null,
      viewModelId: null,
      modelViewReadOnly: false,

    });
  }
  case types.CLEAR_MODEL_TEST_RESULTS: {
    return Object.assign({}, state, {
      digitalModelTestResults: {},
      speechModelTestResults: {},
    });
  }
  case types.CLEAR_MODEL_BATCH_TEST_RESULTS: {
    return Object.assign({}, state, {
      modelBatchTestResults: {},
      modelBatchJobRequest: {},
    });
  }
  case types.RECIEVE_MODEL_TEST_RESULTS: {
    if (action.speechResults) {
      return Object.assign({}, state, {
        speechModelTestResults: action.modelTestResults,
      });
    }
    return Object.assign({}, state, {
      digitalModelTestResults: action.modelTestResults,
    });
  }
  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.MODEL_BATCH_TEST_RESULTS: {
    const { modelBatchJobRequest, trackModelsBatchTestIds } = state;

    const { projectId, modelId, modelTestJobId } = action;

    if (action.modelBatchTestResults.type === 'ok') {
      if (action.modelBatchTestResults.message) {
        if (trackModelsBatchTestIds[projectId]) {
          if (trackModelsBatchTestIds[projectId][modelId]) {
            if (trackModelsBatchTestIds[projectId][modelId][modelTestJobId]) {
              delete trackModelsBatchTestIds[projectId][modelId][modelTestJobId];
              if (Object.keys(trackModelsBatchTestIds[projectId][modelId]).length === 0) {
                delete trackModelsBatchTestIds[projectId][modelId];
              }
              if (Object.keys(trackModelsBatchTestIds[projectId]).length === 0) {
                delete trackModelsBatchTestIds[projectId];
              }
            }
          }
        }
      }
      return Object.assign({}, state, {
        modelBatchTestResults: action,
      });
    }
    return state;
  }

  case types.LIST_BATCH_TESTS_INFO: {
    let { model } = state;
    model = Object.assign({}, model, { listOfBatchTests: action.listOfBatchTests });
    return Object.assign({}, state, { model });
  }

  case types.CLEAR_LIST_BATCH_TESTS_INFO: {
    let { model } = state;
    model = Object.assign({}, model, { listOfBatchTests: {} });
    return Object.assign({}, state, { model });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.CHECK_MODEL_TEST_RESULTS_FAILED: {
    const { modelBatchJobRequest, trackModelsBatchTestIds } = state;
    const { projectId, modelId, modelTestJobId } = action;

    if (trackModelsBatchTestIds[projectId]) {
      if (trackModelsBatchTestIds[projectId][modelId]) {
        if (trackModelsBatchTestIds[projectId][modelId][modelTestJobId]) {
          delete trackModelsBatchTestIds[projectId][modelId][modelTestJobId];
          if (Object.keys(trackModelsBatchTestIds[projectId][modelId]).length === 0) {
            delete trackModelsBatchTestIds[projectId][modelId];
          }
          if (Object.keys(trackModelsBatchTestIds[projectId]).length === 0) {
            delete trackModelsBatchTestIds[projectId];
          }
        }
      }
    }

    if (modelBatchJobRequest.testId === modelTestJobId) {
      return Object.assign({}, state, {
        modelBatchTestResults: {},
        modelBatchJobRequest: {},
      });
    }

    return state;
  }
  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.MODEL_BATCH_JOB_REQUEST: {
    const { projectId, testId, modelId } = (action.modelBatchJobRequest);
    const { trackModelsBatchTestIds } = state;

    if (!trackModelsBatchTestIds[projectId]) {
      trackModelsBatchTestIds[projectId] = {};
    }
    if (!trackModelsBatchTestIds[projectId][modelId]) {
      trackModelsBatchTestIds[projectId][modelId] = {};
    }
    if (!trackModelsBatchTestIds[projectId][modelId][testId]) {
      trackModelsBatchTestIds[projectId][modelId][testId] = testId;
    }
    return Object.assign({}, state, {
      modelBatchJobRequest: action.modelBatchJobRequest,
      trackModelsBatchTestIds,
    });
  }
  case types.NEW_MODEL: {
    return Object.assign({}, state, {
      model: null,
      tuneModelId: null,
      viewModelId: null,
      modelViewReadOnly: false,
      speechModelTestResults: {},
      digitalModelTestResults: {},
      modelBatchTestResults: {},
      modelBatchJobRequest: {},
    });
  }
  case types.MODEL_EDIT_UPDATE: {
    const { model } = action;

    return {
      ...state,
      model: action.model,
    };
  }
  case types.TUNE_MODEL_ID: {
    const { tuneModelId } = action;

    return Object.assign({}, state, {
      tuneModelId,
      viewModelId: null,
      modelViewReadOnly: false,
    });
  }
  case types.VIEW_MODEL_ID: {
    const { viewModelId } = action;

    return Object.assign({}, state, {
      viewModelId,
      tuneModelId: null,
      modelViewReadOnly: true,
    });
  }
  case types.CLEAR_VIEW_MODEL_VIEW: {
    return Object.assign({}, state, {
      viewModelId: null,
      modelViewReadOnly: false,
    });
  }
  case types.SHOW_MODEL_NAVIGATION_CONFIRMATION_DIALOG: {
    const { showModelNavigationConfirmationDialog } = action;
    return Object.assign({}, state, {
      showModelNavigationConfirmationDialog,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_MODEL_UPDATED: {
    const { projectId, modelId, model } = action;
    let { projectModels, trackModelsIds } = state;

    let models = projectModels.get(projectId);
    if (!models) {
      models = Map();
    }
    const newModel = Object.assign({ id: modelId }, models.get(modelId), model);
    models = models.set(modelId, newModel);
    projectModels = projectModels.set(projectId, models);

    if (
      newModel.status === 'QUEUED'
        || newModel.status === 'RUNNING'

    ) {
      if (!trackModelsIds[projectId]) {
        trackModelsIds[projectId] = {};
      }
      trackModelsIds[projectId][newModel.id] = newModel.id;
    } else if (trackModelsIds[projectId]) {
      delete trackModelsIds[projectId][newModel.id];
      if (Object.keys(trackModelsIds[projectId]).length === 0) {
        delete trackModelsIds[projectId];
      }
    }

    return Object.assign({}, state, {
      projectModels,
      trackModelsIds,
    });
  }

  default:
    return state;
  }
}

export { projectsManagerReducer };
