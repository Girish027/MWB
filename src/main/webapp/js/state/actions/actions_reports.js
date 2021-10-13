import store from 'state/configureStore';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import fetch from 'isomorphic-fetch';

import getUrl, { pathKey } from 'utils/apiUrls';
import * as types from './types';

export const reset = () => ({
  type: types.REPORTS_RESET,
});

export const setProjectId = projectId => ({
  type: types.REPORTS_SET_PROJECT_ID,
  projectId,
});

export const setIncomingFilter = ({ projectId, datasets }) => ({
  type: types.REPORTS_SET_INCOMING_FILTER,
  projectId,
  datasets,
});

export const setFilter = ({
  datasets, reportType, entries, interval, timeFrom, timeTo,
}) => ({
  type: types.REPORTS_SET_FILTER,
  datasets,
  reportType,
  entries,
  interval,
  timeFrom,
  timeTo,
});

const getReportTypesRequest = ({ projectId, datasetId }) => ({
  type: types.REPORTS_GET_REPORT_TYPES_REQUEST,
  projectId,
  datasetId,
});

const getReportTypesError = ({ projectId, datasetId, error }) => ({
  type: types.REPORTS_GET_REPORT_TYPES_ERROR,
  projectId,
  datasetId,
  error,
});

const getReportTypesSuccess = ({ projectId, datasetId, reportTypes }) => ({
  type: types.REPORTS_GET_REPORT_TYPES_SUCCESS,
  projectId,
  datasetId,
  reportTypes,
});

export const getReportTypes = ({ projectId, datasetId }) => (dispatch, getState) => new Promise((resolve, reject) => {
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;
  dispatch(getReportTypesRequest({ projectId, datasetId }));
  const fetchUrl = getUrl(pathKey.reportFields, { projectId, datasetId, clientId });

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
      dispatch(getReportTypesSuccess({ projectId, datasetId, reportTypes: [...json] }));
      resolve([...json]);
    })
    .catch((error) => {
      dispatch(getReportTypesError({ projectId, datasetId, error }));
      reject(error);
    });
});
