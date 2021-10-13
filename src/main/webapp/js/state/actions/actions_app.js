
import _ from 'lodash';
import getUrl, { pathKey } from 'utils/apiUrls';
import { fetchUtil } from 'utils/fetchUtils';
import goToRoute from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import axios from 'axios';
import api from 'utils/api';
import { setDefaultsForAPI } from 'utils/api';
import * as types from './types';

// Note to developers:
// Both Warning and Good Messages are shown for the period of notificationInterval.
// Bad, Persistant - till user closes the notification. This should only be used sparingly.
export const callLogIngestAPI = (message, logLevel) => {
  const promise = new Promise((resolve, reject) => {
    const url = getUrl(pathKey.logIngest, { logLevel });
    return new Promise((resolve, reject) => {
      api.post({
        url,
        headers: {
          'Content-Type': 'text/plain',
        },
        data: message,
      });
    });
  });
};

export const displayBadRequestMessage = (message) => (dispatch) => {
  dispatch({
    type: types.BAD_REQUEST,
    value: message,
  });

  callLogIngestAPI(message, Constants.LOG_LEVEL_ERROR);
};

export const displayWarningRequestMessage = (message, notificationInterval = Constants.NOTIFICATION.interval) => (dispatch) => {
  dispatch({
    type: types.WARNING_REQUEST,
    value: message,
  });

  callLogIngestAPI(message, Constants.LOG_LEVEL_WARNING);

  setTimeout(() => {
    dispatch(stopShowingServerMessage());
  }, notificationInterval);
};

export const displayGoodRequestMessage = (message, notificationInterval = Constants.NOTIFICATION.interval) => (dispatch) => {
  dispatch({
    type: types.GOOD_REQUEST,
    value: message,
  });

  setTimeout(() => {
    dispatch(stopShowingServerMessage());
  }, notificationInterval);
};

export const displayPersistantMessage = message => ({
  type: types.PERSISTENT_REQUEST,
  value: message,
});

export const stopShowingServerMessage = () => ({
  type: types.APP_STOP_SHOWING_SERVER_MESSAGE,
});

export const updateLoginSuccess = csrfToken => ({
  type: types.LOGIN_SUCCESS,
  value: csrfToken,
});

export const updateLoginInfo = (userId, userName, userDetails) => ({
  type: types.LOGIN_INFO,
  value: {
    userId,
    userName,
    userDetails,
  },
});

export const logoutFromOktaSuccess = () => ({
  type: types.LOGOUT_OKTA,
});

export const logoutFromMWBSuccess = () => ({
  type: types.LOGOUT_MWB,
});

export const contextMenuChange = contextMenuState => ({
  type: types.CONTEXT_MENU_CHANGE,
  contextMenuState,
});

export const modalDialogChange = modalDialogState => ({
  type: types.MODAL_DIALOG_CHANGE,
  modalDialogState,
});

const actionFileRemoveRequest = ({ fileId }) => ({
  type: types.FILE_REMOVE_REQUEST,
  fileId,
});

const actionFileRemoveError = ({ fileId, error }) => ({
  type: types.FILE_REMOVE_ERROR,
  fileId,
  error,
});

const actionFileRemoveSuccess = ({ fileId }) => ({
  type: types.FILE_REMOVE_SUCCESS,
  fileId,
});

export const updateUserGroups = data => ({
  type: types.UPDATE_USER_GROUPS,
  data,
});

const onFetchFailureRemoveFile = (error, params) => (dispatch) => {
  const { fileId } = params;
  dispatch(actionFileRemoveError({ fileId, error }));
};

const onFetchSuccessRemoveFile = (data, params) => (dispatch) => {
  const { fileId } = params;
  dispatch(actionFileRemoveSuccess({ fileId }));
};

export const removeFile = ({ fileId }) => (dispatch, getState) => {
  dispatch(actionFileRemoveRequest({ fileId }));

  const state = getState();
  const fetchUrl = getUrl(pathKey.files, { fileId, clientId: state.header.client.id });

  fetchUtil({
    fetchUrl,
    fetchMethod: 'delete',
    onFetchSuccess: onFetchSuccessRemoveFile,
    onFetchFailure: onFetchFailureRemoveFile,
    dispatch,
    getState,
    params: {
      fileId,
    },
  });
};

const onFetchSuccessGetUserGroups = data => (dispatch) => {
  dispatch(updateUserGroups(data));
};

export const getUserGroups = ({ csrfToken }) => (dispatch, getState) => {
  const fetchUrl = getUrl(pathKey.userGroups, { csrfToken });

  fetchUtil({
    fetchUrl,
    fetchMethod: 'get',
    onFetchSuccess: onFetchSuccessGetUserGroups,
    dispatch,
    getState,
  });
};

export const onFetchSuccessGetCSRFToken = data => (dispatch, getState) => {
  const csrfToken = data.value;
  setDefaultsForAPI(csrfToken);
  dispatch(updateLoginSuccess(csrfToken));
  dispatch(getUserGroups({ csrfToken }));
};

export const getCSRFToken = userId => (dispatch, getState) => {
  const fetchUrl = getUrl(pathKey.csrfToken);

  fetchUtil({
    fetchUrl,
    fetchMethod: 'get',
    onFetchSuccess: onFetchSuccessGetCSRFToken,
    dispatch,
    getState,
    params: {
      userId,
    },
  });
};

const logoutFromMWB = () => (dispatch, getState) => {
  dispatch(logoutFromOktaSuccess());
  dispatch(logoutFromMWBSuccess());
};

export const logoutFromOkta = () => (dispatch, getState) => {
  const state = getState();
  const { oAuthLogoutURL } = state.app;
  if (oAuthLogoutURL) {
    fetch(oAuthLogoutURL, {
      method: 'DELETE',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    })
      .then((response) => {
        window.location.href = Constants.LOGOUT;
        switch (response.status) {
        case 204:
          logoutFromMWB();
          logAmplitudeEvent(AmplitudeConstants.LOGOUT_EVENT, state);
          break;
        case 404:
        default:
          // TODO send log for these scenarios
          break;
        }
      })
      .catch((error) => {
        // TODO send log for these scenarios
      });
  }
};

export const changeRoute = (routeName, data, history) => (dispatch, getState) => {
  const state = getState();
  const routeData = Object.assign({}, {
    client: !_.isNil(state.header.client) ? state.header.client : {
      id: 0,
      name: 'Loading...',
    },
    projectId: state.selectedProjectId,
  }, data);
  goToRoute(routeName, routeData, history);
};
