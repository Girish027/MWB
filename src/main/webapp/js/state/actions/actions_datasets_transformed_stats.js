
import fetch from 'isomorphic-fetch';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import api from 'utils/api';
import { getLanguage } from 'state/constants/getLanguage';
import * as appActions from './actions_app';
import * as types from './types';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;
// DATASET

export const requestStats = () => ({
  type: types.REQUEST_DATASET_STATS,
});

export const receiveDatasetStatsSuccess = (datasetId, isDatasetsValid, isDatasetsTagged, datasetsIntents, isDatasetValid, isFullyTagged, uniqueRollupValue, datasetIntents) => ({
  type: types.RECEIVE_DATASET_VALIDATION_STATS,
  isDatasetsValid,
  isDatasetsTagged,
  datasetsIntents,
  isDatasetValid,
  isFullyTagged,
  uniqueRollupValue,
  datasetIntents,
  datasetId,
});

export const deleteDatasetValidationStats = datasetId => ({
  type: types.DELETE_DATASET_VALIDATION_STATS,
  datasetId,
});

export const receiveDatasetStatsFailure = (datasetId) => ({
  type: types.RECEIVE_DATASET_VALIDATION_STATS,
  isDatasetsValid: false,
  isDatasetsTagged: false,
  isDatasetValid: false,
  isFullyTagged: false,
  uniqueRollupValue: null,
  datasetIntents: [],
  datasetsIntents: [],
  datasetId,
});

export const resetDatasetValidationStats = () => ({
  type: types.RESET_DATASET_VALIDATION_STATS,
});

export const receiveStatsSuccess = statsResults => ({
  type: types.RECEIVE_DATASET_STATS_SUCCESS,
  statsResults,
});

export const onFetchDatasetStatsSuccess = (datasetId, response = {}) => (dispatch, getState) => {
  const {
    isDatasetValid, isDatasetFullyTagged, uniqueRollupValue, datasetIntentsSet,
  } = response;
  const {
    isDatasetsValid, isDatasetsTagged, datasetsIntents, validDatasetMap,
  } = getState().tagDatasets.validDatasetsStats;

  // calculate group datasets stats
  let isDatasetsTaggedNew = isDatasetsTagged && isDatasetFullyTagged;
  let isDatasetsValidNew = isDatasetsValid || isDatasetValid;
  let datasetsIntentsNew = [...datasetsIntents, ...datasetIntentsSet];
  let ruValue = null;
  if (!isDatasetsValidNew) {
    validDatasetMap.forEach((value) => {
      isDatasetsValidNew = isDatasetsValidNew || (ruValue !== null && value.uniqueRollupValue !== ruValue);
      ruValue = value.uniqueRollupValue;
    });
  }
  if (!isDatasetsValidNew) {
    dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.uniqueRollupCount));
  }
  dispatch(receiveDatasetStatsSuccess(datasetId, isDatasetsValidNew, isDatasetsTaggedNew, datasetsIntentsNew, isDatasetValid, isDatasetFullyTagged, uniqueRollupValue, datasetIntentsSet));
};

export const onFetchDatasetStatsFailure = (datasetId, error = {}) => (dispatch, getState) => {
  const { message } = error;
  dispatch(receiveDatasetStatsFailure(datasetId));
  dispatch(appActions.displayWarningRequestMessage(message));
};

export const fetchDatasetValidationStatsById = ({ projectId, datasetId }) => (dispatch, getState) => {
  dispatch(requestStats());
  const state = getState();
  const clientId = state.header.client.id;
  const fetchUrl = getUrl(pathKey.datasetValidationStatsById, { clientId, projectId, datasetId });

  api.get({
    url: fetchUrl,
    onApiSuccess: response => onFetchDatasetStatsSuccess(datasetId, response),
    onApiError: error => onFetchDatasetStatsFailure(datasetId, error),
    dispatch,
    getState,
  });
};

export const fetchDatasetStats = ({ projectId, filter }) => (dispatch, getState) => {
  dispatch(requestStats());

  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.datasetStats, { projectId, clientId });

  return fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': csrfToken,
    },
    body: JSON.stringify({ filter }),
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then(json => dispatch(receiveStatsSuccess(json)))
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};
