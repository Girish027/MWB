import fetch from 'isomorphic-fetch';
import { Map } from 'immutable';
import _ from 'lodash';
import store from 'state/configureStore';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import * as appActions from 'state/actions/actions_app';
import { clientChange } from 'state/actions/actions_header';
import * as actionsTaggingGuide from 'state/actions/actions_taggingguide';
import { createNewModel, modelCheckBatchTestState, modelBatchTestResults } from 'state/actions/actions_models';
import api from 'utils/api';
import * as apiUtils from 'utils/apiUtils';
import getUrl, { pathKey } from 'utils/apiUrls';
import { getLanguage } from 'state/constants/getLanguage';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import Constants from 'constants/Constants';
import validationUtil from 'utils/ValidationUtil';
import { RouteNames } from 'utils/routeHelpers';
import { headerIcon } from 'styles';
import * as types from './types';


const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

const projectLoadRequest = ({ projectId, promise }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_LOAD_REQUEST,
  projectId,
  promise,
});

const projectLoadSuccess = ({ projectId, project }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_LOAD_SUCCESS,
  projectId,
  project,
});

const projectLoadError = ({ projectId }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_LOAD_ERROR,
  projectId,
});

const projectDatasetsLoadRequest = ({ projectId, promise }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_REQUEST,
  projectId,
  promise,
});

const projectDatasetsLoadSuccess = ({ projectId, datasets }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS,
  projectId,
  datasets,
});

const projectDatasetsLoadError = ({ projectId }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_ERROR,
  projectId,
});

const datasetsStatusesLoadRequest = ({ trackIds, promise }) => ({
  type: types.PROJECTS_MANAGER_DATASETS_STATUSES_LOAD_REQUEST,
  trackIds,
  promise,
});

const datasetsStatusesLoadError = ({ trackIds, error }) => ({
  type: types.PROJECTS_MANAGER_DATASETS_STATUSES_LOAD_ERROR,
  trackIds,
  error,
});

const datasetsStatusesLoadSuccess = ({ trackIds, statuses }) => ({
  type: types.PROJECTS_MANAGER_DATASETS_STATUSES_LOAD_SUCCESS,
  trackIds,
  statuses,
});
const projectConfigsLoadRequest = ({ projectId, promise }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_CONFIGS_LOAD_REQUEST,
  projectId,
  promise,
});

const projectConfigsLoadSuccess = ({ projectId, configs }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_CONFIGS_LOAD_SUCCESS,
  projectId,
  configs,
});

const projectConfigsLoadError = ({ projectId }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_CONFIGS_LOAD_ERROR,
  projectId,
});

export const projectIntentsLoadRequest = ({ projectId, promise }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_INTENTS_LOAD_REQUEST,
  projectId,
  promise,
});

export const projectIntentsLoadSuccess = ({ projectId, intents }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_INTENTS_LOAD_SUCCESS,
  projectId,
  intents,
});

export const projectIntentsLoadError = ({ projectId }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_INTENTS_LOAD_ERROR,
  projectId,
});

export const projectUpdated = ({ projectId, project }) => {
  projectId = `${projectId}`;
  return {
    type: types.PROJECTS_MANAGER_PROJECT_UPDATED,
    projectId,
    project,
  };
};

export const datasetUpdated = ({ projectId, datasetId, dataset }) => {
  projectId = `${projectId}`;
  datasetId = `${datasetId}`;
  return {
    type: types.PROJECTS_MANAGER_DATASET_UPDATED,
    projectId,
    datasetId,
    dataset,
  };
};

const projectModelsLoadRequest = ({ projectId, promise }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_MODELS_LOAD_REQUEST,
  projectId,
  promise,
});

const projectModelsLoadSuccess = ({ projectId, models }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_MODELS_LOAD_SUCCESS,
  projectId,
  models,
});

export const modelUpdated = ({ projectId, modelId, model }) => {
  projectId = `${projectId}`;
  modelId = `${modelId}`;
  return {
    type: types.PROJECTS_MANAGER_MODEL_UPDATED,
    projectId,
    modelId,
    model,
  };
};

const projectModelsLoadError = ({ projectId }) => ({
  type: types.PROJECTS_MANAGER_PROJECT_MODELS_LOAD_ERROR,
  projectId,
});

const doClientsMatch = (project) => {
  const state = store.getState();

  if (state.header.client.id !== project.clientId) {
    return true;
  }
  return false;
};

const getClientInfo = (clientId) => {
  const state = store.getState();

  const clientInfo = _.find(state.header.clientList, { id: clientId });
  return clientInfo;
};

const onClickBuildModel = (projectId, clientId, selectedProjectId, history) => (dispatch, getState) => {
  if (projectId && history && clientId) {
    dispatch(createNewModel(projectId));
    dispatch(appActions.changeRoute(RouteNames.CREATEMODEL, { clientId, projectId }, history));
    if (projectId !== selectedProjectId) {
      dispatch(actionsTaggingGuide.requestProject(projectId));
      dispatch(actionsTaggingGuide.requestLastImportInfo(projectId));
      dispatch(actionsTaggingGuide.requestSearch(projectId, { property: 'count', direction: 'desc' }));
    }
  }
  dispatch(appActions.modalDialogChange(null));
};

export const onFetchTaggingStatsSuccess = (projectId, clientId, selectedProjectId, response = {}) => (dispatch, getState) => {
  const { all = {} } = response;
  const { percent = 0 } = all;
  let cancelChildren = Constants.NO;
  let okChildren = Constants.YES;
  let message = Constants.UPLOAD_DATASET_SUCCESS_WITH_TAG_MESSAGE;
  let okVisible = true;
  if (percent < 100 && percent > 0) {
    message = Constants.UPLOAD_DATASET_SUCCESS_MESSAGE;
  } else if (percent === 0) {
    cancelChildren = Constants.CLOSE;
    okVisible = false;
    message = Constants.UPLOAD_DATASET_SUCCESS_WITHOUT_TAG_MESSAGE;
  }
  dispatch(appActions.modalDialogChange({
    dispatch,
    clientId,
    projectId,
    type: Constants.DIALOGS.PROGRESS_DIALOG,
    message,
    header: Constants.UPLOAD_DATASET_SUCCESS,
    showHeader: true,
    showFooter: true,
    cancelVisible: true,
    okVisible,
    showSpinner: false,
    closeIconVisible: true,
    cancelChildren,
    okChildren,
    onOk: history => dispatch(onClickBuildModel(projectId, clientId, selectedProjectId, history)),
    styleOverride: {
      childContainer: {
        marginTop: '30px',
        marginBottom: '10px',
      },
      content: {
        top: '160px',
        maxWidth: '380px',
        maxHeight: '260px',
        left: 'calc((100vw - 500px) / 2)',
      },
      ...headerIcon,
    },
  }));
};

export const onFetchTaggingStatsFailure = (error = {}) => (dispatch, getState) => {
  const { message } = error;
  dispatch(appActions.modalDialogChange(null));
  dispatch(appActions.displayBadRequestMessage(message));
};

const fetchTaggingStats = ({
  projectId, clientId, selectedProjectId, filter,
}) => (dispatch, getState) => {
  const url = getUrl(pathKey.datasetStats, { projectId, clientId });

  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: filter,
      onApiSuccess: response => onFetchTaggingStatsSuccess(projectId, clientId, selectedProjectId, response),
      onApiError: onFetchTaggingStatsFailure,
    });
  });
};

const checkDatasetTransformationCompletedStatus = ({ statuses, clientId }) => (dispatch, getState) => {
  const state = getState();
  const selectedProjectId = state.projectListSidebar.selectedProjectId;
  if (!_.isNil(statuses) && !_.isEmpty(statuses)) {
    for (const projectId in statuses) {
      if (!statuses.hasOwnProperty(projectId)) {
        continue;
      }
      for (const datasetId in statuses[projectId]) {
        if (!statuses[projectId].hasOwnProperty(datasetId)) {
          continue;
        }
        const task = statuses[projectId][datasetId].task;
        const status = statuses[projectId][datasetId].status;
        if (task && task.toUpperCase() === Constants.INDEX
          && status && status.toUpperCase() === Constants.STATUS.COMPLETED) {
          dispatch(fetchTaggingStats({
            projectId, clientId, selectedProjectId, filter: { filter: { datasets: [datasetId] } },
          }));
          if (selectedProjectId === projectId) {
            dispatch(actionsTaggingGuide.requestProject(projectId));
            dispatch(actionsTaggingGuide.requestLastImportInfo(projectId));
            dispatch(actionsTaggingGuide.requestSearch(projectId, { property: 'count', direction: 'desc' }));
          }
        }
      }
    }
  }
};

export const loadProject = (projectId, history) => (dispatch, getState) => {
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { csrfToken } = state.app;
    const clientId = state.header.client.id;
    const fetchUrl = getUrl(pathKey.projectById, { projectId, clientId });

    fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'GET',
      headers: {
        'X-CSRF-TOKEN': csrfToken,
      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const newProject = apiUtils.normalizeIds(json);
        const project = {/* new Project({ */
          id: newProject.id,
          clientId: newProject.clientId,
          name: newProject.name,
          created: newProject.createdAt,
          locale: newProject.locale,
          description: newProject.description,
          vertical: newProject.vertical,
        };
        const isClientDifferent = doClientsMatch(project);
        if (isClientDifferent) {
          const clientInfo = getClientInfo(project.clientId);
          if (clientInfo) {
            dispatch(clientChange(clientInfo));
            dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.projectLatest(clientInfo.name)));
            dispatch(projectLoadSuccess({ projectId, project }));
          }
        } else {
          dispatch(projectLoadSuccess({ projectId, project }));
        }
        resolve(project);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(projectLoadError({ projectId, error }));
        reject(error);
      });
  });

  dispatch(projectLoadRequest({ projectId, promise }));

  return promise;
};

export const loadProjectDatasets = projectId => (dispatch, getState) => {
  projectId = `${projectId}`;
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { csrfToken } = state.app;
    const { userDetails: { userType: loginUserType } } = state.app;
    const clientId = state.header.client.id;
    const fetchUrl = getUrl(pathKey.projectDatasets, { projectId, clientId });

    fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'GET',
      headers: {
        'X-CSRF-TOKEN': csrfToken,

      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const datasets = {};
        if (json && json.length) {
          json.forEach((item) => {
            const newDataset = apiUtils.normalizeIds(item);
            const isClickable = !((newDataset.source === Constants.DATASET_INTERNAL_FLAG) && (loginUserType === Constants.USER_TYPE_EXTERNAL));
            datasets[newDataset.id] = { /* new Dataset( */
              _key: newDataset.id,
              id: newDataset.id,
              clientId: newDataset.clientId,
              projectId,
              name: newDataset.name,
              type: newDataset.dataType,
              description: newDataset.description || '',
              locale: newDataset.locale,
              createdAt: newDataset.createdAt,
              modifiedBy: newDataset.modifiedBy,
              status: newDataset.transformationStatus,
              task: newDataset.transformationTask,
              percentComplete: newDataset.percentComplete,
              errorCode: newDataset.errorCode,
              isClickable,
            };
          });
        }
        const datasetsMap = Map(datasets);
        dispatch(projectDatasetsLoadSuccess({ projectId, datasets: datasetsMap }));
        resolve(datasetsMap);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(projectDatasetsLoadError({ projectId, error }));
        reject(error);
      });
  });

  dispatch(projectDatasetsLoadRequest({ projectId, promise }));

  return promise;
};

export const loadDatasetsStatuses = trackIds => (dispatch, getState) => {
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { app, header } = state;
    const { csrfToken } = app;
    const clientId = header.client.id;

    // TODO: improvements
    const transformedTrackIds = Object.assign({}, trackIds);
    for (const projectId in transformedTrackIds) {
      if (!transformedTrackIds.hasOwnProperty(projectId)) {
        continue;
      }
      transformedTrackIds[`${projectId}`] = Object.keys(transformedTrackIds[projectId]);
    }

    const fetchUrl = getUrl(pathKey.projectDatasetsTransformStatus, { clientId });

    fetch(fetchUrl, {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken,

      },
      body: JSON.stringify(transformedTrackIds),
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        dispatch(datasetsStatusesLoadSuccess({ trackIds, statuses: json }));
        dispatch(checkDatasetTransformationCompletedStatus({ statuses: json, clientId }));
        resolve(json);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(datasetsStatusesLoadError({ trackIds, error }));
        reject(error);
      });
  });

  dispatch(datasetsStatusesLoadRequest({ trackIds, promise }));

  return promise;
};

// ToDo - change to use dispatch and thunk so getState is automatically passed
const loadModelState = ({ projectId, modelId }) => {
  // Check for status only if projectId is same as selectedProjectId.
  const state = store.getState();
  const { selectedProjectId } = state.projectListSidebar;
  if (projectId !== selectedProjectId) {
    return;
  }
  modelId = `${modelId}`;
  return new Promise((resolve, reject) => {
    const { csrfToken } = state.app;
    const clientId = state.header.client.id;
    const fetchUrl = getUrl(pathKey.modelState, { modelId, projectId, clientId });

    fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'GET',
      headers: {
        'X-CSRF-TOKEN': csrfToken,

      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const {
          modelUUID, modelType, statusMessage, status,
        } = json;
        resolve({
          projectId, modelId, modelToken: modelUUID, modelType, statusMessage, status: status ? (`${status}`).toUpperCase() : 'NULL',
        });
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, store.dispatch, appActions.displayBadRequestMessage);
        /* assume its NULL */
        resolve({
          projectId, modelId, status: 'NULL', modelToken: '', modelType: '',
        });
      });
  });
};

export const loadProjectModels = projectId => (dispatch, getState) => {
  projectId = `${projectId}`;
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { csrfToken } = state.app;
    const clientId = state.header.client.id;
    const fetchUrl = getUrl(pathKey.modelsForProject, { projectId, clientId });
    fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'GET',
      headers: {
        'X-CSRF-TOKEN': csrfToken,

      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const models = {};
        const stateRequests = [];
        if (json && json.length) {
          json.forEach((item) => {
            const newModel = apiUtils.normalizeModel(item);
            models[newModel.id] = { /* new Model( */
              _key: newModel.id,
              id: newModel.id,
              modelToken: newModel.modelId,
              projectId: projectId || null,
              name: newModel.name || '',
              description: newModel.description || '',
              version: newModel.version || '',
              modelType: newModel.modelType || '',
              vectorizer_id: newModel.vectorizer_id || null,
              vectorizerTechnology: newModel.technology_type || '',
              vectorizer_technology_version: newModel.technology_version || null,
              created: newModel.createdAt || null,
              updated: newModel.updatedAt || null,
              datasetIds: newModel.datasetIds || [],
              configId: newModel.configId || null,
              userId: newModel.userId || null,
              speechModelId: newModel.speechModelId || '',
              digitalHostedUrl: newModel.digitalHostedUrl || '',
              speechConfigId: newModel.speechConfigId || '',
            };
            stateRequests.push(loadModelState({ projectId, modelId: newModel.id }));
          });
        }

        if (!stateRequests.length) {
          const modelsMap = Map(models);
          dispatch(projectModelsLoadSuccess({ projectId, models: modelsMap }));
          resolve(modelsMap);
        }

        Promise.all(stateRequests)
          .then((values) => {
            values.forEach((v) => {
              if (v.modelId && v.status && models[v.modelId]) {
                models[v.modelId].status = v.status;
              }
            });
            const modelsMap = Map(models);
            dispatch(projectModelsLoadSuccess({ projectId, models: modelsMap }));
            resolve(modelsMap);
          })
          .catch((error) => {
            errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
            dispatch(projectModelsLoadError({ projectId, error }));
            reject(error);
          });
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(projectModelsLoadError({ projectId, error }));
        reject(error);
      });
  });

  dispatch(projectModelsLoadRequest({ projectId, promise }));

  return promise;
};

export const loadModelsStatuses = trackIds => (dispatch, getState) => new Promise((resolve, reject) => {
  const stateRequests = [],
    state = getState();
  for (const projectId in trackIds) {
    if (!trackIds.hasOwnProperty(projectId)) {
      continue;
    }
    for (const modelId in trackIds[projectId]) {
      if (!trackIds[projectId].hasOwnProperty(modelId)) {
        continue;
      }
      stateRequests.push(loadModelState({ projectId, modelId }));
    }
  }

  if (!stateRequests.length) {
    resolve();
  }

  Promise.all(stateRequests)
    .then((values) => {
      values.forEach((value) => {
        const {
          projectId, modelId, status, modelType, modelToken, statusMessage,
        } = value;
        if (modelId && status && projectId) {
          if (status === Constants.STATUS.COMPLETED) {
            logAmplitudeEvent(AmplitudeConstants.CREATE_MODEL_COMPLETED_EVENT, state, {
              projectId, modelDBId: modelId, modelId: modelToken, modelType,
            });
          } else if (status === Constants.STATUS.FAILED) {
            logAmplitudeEvent(AmplitudeConstants.CREATE_MODEL_FAILED_EVENT, state, {
              projectId, modelDBId: modelId, modelId: modelToken, modelType,
            });
          }
          dispatch(modelUpdated({ projectId, modelId, model: { status, modelToken, statusMessage } }));
        }
      });
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const loadProjectConfigs = projectId => (dispatch, getState) => {
  projectId = `${projectId}`;
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { csrfToken } = state.app;
    const clientId = state.header.client.id;
    const fetchUrl = getUrl(pathKey.modelConfigsForProject, { projectId, clientId });
    fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'GET',
      headers: {
        'X-CSRF-TOKEN': csrfToken,

      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const configs = {};
        if (json && json.length) {
          json.forEach((item) => {
            const normalizedItem = apiUtils.normalizeIds(item);
            if (validationUtil.validateJSONString(normalizedItem.configFile)) {
              const normalizedConfigFile = JSON.parse(normalizedItem.configFile);
              configs[normalizedItem.id] = { /* new Configs( */
                _key: normalizedItem.id,
                id: normalizedItem.id,
                projectId: projectId || null,
                name: normalizedItem.name || '',
                description: normalizedConfigFile.description || '',
                created: normalizedItem.createdAt || null,
                updated: normalizedItem.modifiedAt || null,
                user: normalizedItem.userId || null,
                out_of_domain_intent: normalizedConfigFile.trainingConfigs.out_of_domain_intent || null,
                // TODO: obtain status here
              };
            }
          });
        }

        const configsMap = Map(configs);
        dispatch(projectConfigsLoadSuccess({ projectId, configs: configsMap }));
        resolve(configsMap);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(projectConfigsLoadError({ projectId, error }));
        reject(error);
      });
  });

  dispatch(projectConfigsLoadRequest({ projectId, promise }));

  return promise;
};

export const loadModelsBatchTestStatuses = trackIds => (dispatch, getState) => new Promise((resolve, reject) => {
  const stateRequests = [];
  const jobRequests = [];
  for (const projectId in trackIds) {
    if (!trackIds.hasOwnProperty(projectId)) {
      continue;
    }
    for (const modelId in trackIds[projectId]) {
      if (!trackIds[projectId].hasOwnProperty(modelId)) {
        continue;
      }
      for (const testId in trackIds[projectId][modelId]) {
        if (!trackIds[projectId][modelId].hasOwnProperty(testId)) {
          continue;
        }
        const state = getState();
        const { userId, csrfToken } = state.app;
        const clientId = state.header.client.id;
        const data = {
          userId,
          clientId,
          csrfToken,
          projectId,
          modelId,
          modelTestJobId: testId,
        };
        stateRequests.push(modelCheckBatchTestState(data));
        jobRequests.push(data);
      }
    }
  }

  if (!stateRequests.length) {
    resolve();
  }

  Promise.all(stateRequests)
    .then((values) => {
      values.forEach((v, index) => {
        dispatch(modelBatchTestResults({
          modelBatchTestResults: v,
          modelId: jobRequests[index].modelId,
          projectId: jobRequests[index].projectId,
          modelTestJobId: jobRequests[index].modelTestJobId,
        }));
      });
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});
