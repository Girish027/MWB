import 'regenerator-runtime/runtime';

import getUrl, { pathKey } from 'utils/apiUrls';

import { fetchUtil } from 'utils/fetchUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { initializeAnalytics, logAmplitudeEvent } from 'utils/amplitudeUtils';
import api from 'utils/api';
import * as types from './types';

const appConfigSuccess = data => ({
  type: types.APP_CONFIG_SUCCESS,
  data,
});

const appConfigFailure = data => ({
  type: types.APP_CONFIG_FAILURE,
  data,
});

const appConfigRequest = () => ({
  type: types.APP_CONFIG_REQUEST,
});

export const onAppConfigSuccess = data => (dispatch, getState) => {
  dispatch(appConfigSuccess(data));
  const state = getState();
  initializeAnalytics(state);
  logAmplitudeEvent(AmplitudeConstants.LOGIN_EVENT, state);
};

export const onAppConfigFailure = error => (dispatch) => {
  dispatch(appConfigFailure(error));
};

export const fetchAppConfig = () => (dispatch, getState) => {
  dispatch(appConfigRequest());
  const fetchUrl = getUrl(pathKey.appConfig);

  api.get({
    url: fetchUrl,
    onApiSuccess: onAppConfigSuccess,
    onApiError: onAppConfigFailure,
    dispatch,
    getState,
  });
};
