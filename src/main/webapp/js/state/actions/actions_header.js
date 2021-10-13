import fetch from 'isomorphic-fetch';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import * as apiUtils from 'utils/apiUtils';
import { fetchUtil } from 'utils/fetchUtils';
import Constants from 'constants/Constants';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import * as appActions from './actions_app';
import * as types from './types';
// HEADER

export const setActionItems = actionItems => ({
  type: types.SET_APP_HEADER_ACTION_ITEMS,
  actionItems,
});

export const openProject = projectId => ({
  type: types.PROJECT_OPEN,
  projectId,
});

export const closeProject = () => ({
  type: types.PROJECT_CLOSE,
  projectId: null,
});

export const clientChange = newClient => ({
  type: types.CLIENT_CHANGE,
  client: newClient,
});

export const clearSelectedClient = () => ({
  type: types.CLEAR_SELECTED_CLIENT,
});

export const invalidateProjectlist = () => ({
  type: types.INVALIDATE_PROJECT_LIST,
});

// CLIENT LIST
export const receiveClientList = clientList => ({
  type: types.RECEIVE_CLIENT_LIST,
  clientList,
  receivedAt: Date.now(),
});

export const updateVersionInfo = versionInfo => ({
  type: types.VERSION_INFO,
  versionInfo,
});

const fetchUrl = getUrl(pathKey.version);
export const fetchVersionInfo = (userId, csrfToken) => dispatch => fetch(fetchUrl, {
  credentials: 'same-origin',
  headers: {
    'X-CSRF-TOKEN': csrfToken,
  },
})
  .then(errorMessageUtil.handleErrors)
  .then(response => response.json())
  .then((json) => {
    dispatch(updateVersionInfo(json));
  })
  .catch(error => errorMessageUtil.handleErrors(
    error, dispatch,
    appActions.displayBadRequestMessage,
  ));

const onFetchSuccessClientList = json => (dispatch) => {
  const clientsList = [];
  if (json && json.length) {
    json.forEach((client) => {
      const newClient = apiUtils.normalizeIds(client);
      // ignore any client without ITS ClientId, appId and accountId
      const { itsClientId, itsAppId, itsAccountId } = newClient;
      if (itsClientId && itsAppId && itsAccountId) {
        clientsList.push(newClient);
      }
    });
    const alphabeticalClientList = clientsList.sort((a, b) => a.itsClientId.localeCompare(b.itsClientId));
    dispatch(receiveClientList(alphabeticalClientList));
  } else {
    dispatch(appActions.modalDialogChange({
      type: Constants.DIALOGS.UNAUTHORIZED_USER,
      dispatch,
    }));
  }
};

const onFetchFailureClientList = (error, response) => (dispatch) => {
  if (response.status === Constants.SC_FORBIDDEN) {
    dispatch(appActions.modalDialogChange({
      type: Constants.DIALOGS.UNAUTHORIZED_USER,
      dispatch,
    }));
  } else if (response.status === Constants.SC_OK) {
    dispatch(appActions.modalDialogChange({
      type: Constants.DIALOGS.UNAUTHORIZED_USER,
      message: Constants.CLIENT_PICKER_ERROR,
      header: Constants.CLIENT_PICK_ERROR_TITLE,
      dispatch,
    }));
  }
};

export const fetchClientList = (userId, csrfToken, itsClientId, itsAppId) => (dispatch, getState) => {
  const fetchUrl = getUrl(pathKey.clients, { itsClientId, itsAppId });
  fetchUtil({
    fetchUrl,
    fetchMethod: 'get',
    onFetchSuccess: onFetchSuccessClientList,
    onFetchFailure: onFetchFailureClientList,
    dispatch,
    getState,
  });
};

export const onClientChange = newClient => (dispatch, getState) => {
  dispatch(clientChange(newClient));
  const state = getState();
  logAmplitudeEvent(AmplitudeConstants.SELECT_CLIENT_EVENT, state);
};
