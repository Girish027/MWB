
import fetch from 'isomorphic-fetch';
import queryString from 'query-string';
import store from 'state/configureStore';
import { datasetUpdated } from 'state/actions/actions_projectsmanager';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import * as apiUtils from 'utils/apiUtils';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { getLanguage } from 'state/constants/getLanguage';
import * as projectsActions from 'state/actions/actions_projects';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;
import * as appActions from './actions_app';
import * as types from './types';

// DATASET

export const openDataset = dataset => ({
  type: types.DATASET_OPEN,
  dataset,
});

export const closeDataset = () => ({
  type: types.DATASET_CLOSE,
});

export const receiveDatasetFail = err => ({
  type: types.RECEIVE_DATASET_FAIL,
  error: err,
});

export const requestTransformDataset = transformingDatasetId => ({
  type: types.REQUEST_DATASET_TRANSFORM,
  transformingDatasetId,
});

export const receiveTransformDatasetProgress = progressData => ({
  type: types.RECEIVE_DATASET_TRANSFORM_PROGRESS,
  progressData,
});

export const receiveTransformDatasetSuccess = datasetId => ({
  type: types.RECEIVE_DATASET_TRANSFORM_SUCCESS,
  value: datasetId,
});

export const receiveTransformDatasetFail = () => ({
  type: types.RECEIVE_DATASET_TRANSFORM_FAIL,
});

export const resetTransformDataset = datasetId => ({
  type: types.RESET_DATASET_TRANSFORM,
  value: datasetId,
});

export const requestDataset = (datasetId, datasetSelectionIndex) => ({
  type: types.REQUEST_DATASET,
  datasetId,
  datasetSelectionIndex,
});

export const receiveDataset = dataset => ({
  type: types.RECEIVE_DATASET,
  dataset,
  receivedAt: Date.now(),
});
// DATASETS BY PROJECT ID

export const requestDatasetsByProjectId = projectId => ({
  type: types.REQUEST_DATASETS_BY_PROJECT_ID,
  requestProjectId: projectId,
});

export const receiveDatasetsByProjectId = datasets => ({
  type: types.RECEIVE_DATASETS_BY_PROJECT_ID,
  datasets,
  receivedAt: Date.now(),
});

// DATASET LIST

export const requestDatasetList = () => ({
  type: types.REQUEST_DATASET_LIST,
});

export const receiveDatasetList = datasetList => ({
  type: types.RECEIVE_DATASET_LIST,
  datasetList,
  receivedAt: Date.now(),
});

// DATASET

export const addDataSetName = name => ({
  type: types.DATASET_ADD_NAME,
  value: name,
});

export const addDataSetDescription = description => ({
  type: types.DATASET_ADD_DESCRIPTION,
  value: description,
});

export const addDataSetType = type => ({
  type: types.DATASET_ADD_TYPE,
  value: type,
});

export const projectDatasetAdd = ({ projectId, datasetId, dataset }) => ({
  type: types.PROJECT_DATASET_ADD,
  projectId,
  datasetId,
  dataset,
});

const datasetRemovedFromProject = ({ projectId, datasetId }) => ({
  type: types.DATASET_REMOVED_FROM_PROJECT,
  projectId,
  datasetId,
});

export const fetchDataset = (datasetId, selectionIndex) => (dispatch, getState) => {
  dispatch(requestDataset(datasetId, selectionIndex));
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;
  const projectId = state.projectListSidebar.selectedProjectId;
  let fetchUrl = getUrl(pathKey.datasetById, { datasetId, clientId, projectId });
  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      const dataset = apiUtils.normalizeIds(json);
      dispatch(receiveDataset(dataset));
    });
};

export const fetchDatasetTransform = (userId, datasetId, clientId, projectId, csrfToken, isRetry, suggestIntent = false) => {
  const queryParams = queryString.stringify({ useModelForSuggestedCategory: suggestIntent });
  let fetchUrl = getUrl(pathKey.datasetTransform, {
    clientId, projectId, datasetId, queryParams,
  });

  if (isRetry) {
    fetchUrl = getUrl(pathKey.datasetTransformRetry, {
      projectId, datasetId, queryParams, clientId,
    });
  }

  return (dispatch) => {
    dispatch(requestTransformDataset(datasetId));
    return fetch(fetchUrl, {
      credentials: 'same-origin',
      method: 'put',
      headers: {

        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ projectId }),
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        dispatch(datasetUpdated({
          projectId,
          datasetId,
          dataset: {
            status: json.status ? (`${json.status}`).toUpperCase() : 'NULL',
            task: json.task ? (`${json.task}`).toUpperCase() : 'NULL',
          },
        }));
      })
      .catch((error) => {
        dispatch(appActions.modalDialogChange(null));
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      });
  };
};

export const fetchDeleteTransformJob = (userId, datasetId, projectId, csrfToken, clientId) => {
  let fetchUrl = getUrl(pathKey.datasetTransform, {
    projectId, datasetId, queryParams: '', clientId,
  });

  return dispatch => fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'delete',
    headers: {

      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(resetTransformDataset(datasetId));
      dispatch(datasetUpdated({
        projectId,
        datasetId,
        dataset: { status: 'NULL' },
      }));
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

export const fetchCancelTransformJob = (userId, datasetId, projectId, csrfToken, clientId) => {
  let fetchUrl = getUrl(pathKey.datasetTransformCancel, { projectId, datasetId, clientId });

  return dispatch => fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'put',
    headers: {

      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(resp => resp.json())
    .then((statusResponse) => {
      dispatch(appActions.displayPersistantMessage('Waiting for Transformation Job to be cancelled.'));
      dispatch(receiveTransformDatasetProgress(statusResponse));
      dispatch(datasetUpdated({
        projectId,
        datasetId,
        dataset: { status: 'CANCELLED' },
      }));
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

export const addDatasetToProject = ({
  projectId, datasetId, dataset, autoTag,
}) => (dispatch, getState) => new Promise((resolve, reject) => {
  const state = getState();
  const app = state.app;
  const userId = app.userId;
  const csrfToken = app.csrfToken;
  const clientId = state.header.client.id;

  let fetchUrl = getUrl(pathKey.dataset, { projectId, datasetId, clientId });
  if (autoTag) {
    fetchUrl = getUrl(pathKey.addDatasetAutoTag, { projectId, datasetId, clientId });
  }

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'PUT',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    // .then(response => response.json())
    // .then((json) => {
    .then(() => {
      const normalizedDataset = apiUtils.normalizeIds(dataset);
      const newDataset = Object.assign({ _key: `d-${projectId}-${datasetId}`, projectId }, normalizedDataset);
      dispatch(projectDatasetAdd({ projectId, datasetId, dataset: newDataset }));
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const removeDatasetFromProject = ({ projectId, datasetId, clientId }) => (dispatch, getState) => new Promise((resolve, reject) => {
  const state = getState(),
    { csrfToken } = state.app;


  let fetchUrl = getUrl(pathKey.dataset, { projectId, datasetId, clientId });
  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'delete',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(projectsActions.refreshProjectsByClient(clientId));
      dispatch(datasetRemovedFromProject({ projectId, datasetId }));
      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.datasetDeleted));
      logAmplitudeEvent(AmplitudeConstants.DELETE_DATASET_COMPLETED_EVENT, state, {
        projectId, datasetId, clientDBId: clientId,
      });
      resolve();
    })
    .catch((error) => {
      logAmplitudeEvent(AmplitudeConstants.DELETE_DATASET_FAILED_EVENT, state, {
        projectId, datasetId, clientDBId: clientId,
      });
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});
