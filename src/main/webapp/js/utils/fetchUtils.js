require('es6-promise').polyfill();
require('isomorphic-fetch');

import 'regenerator-runtime/runtime';

import _ from 'lodash';

import errorMessageUtil from 'utils/ErrorMessageUtil';
import * as appActions from 'state/actions/actions_app';

export const fetchUtil = async (fetchData) => {
  const {
    fetchUrl,
    fetchMethod = 'get',
    onFetchSuccess = null,
    onFetchFailure = null,
    fetchHeaders = {},
    fetchRequestData = {},
    dispatch,
    getState,
    params = null,
    contentType = 'application/json',
    accept = 'application/json',
  } = fetchData;

  let response;

  try {
    const state = getState();
    const { csrfToken } = state.app;
    let { userId } = state.app;

    if (_.isNil(userId) && !_.isNil(params)) {
      userId = params.userId;
    }

    const headers = {
      ...fetchHeaders,
    };

    if (!_.isNil(csrfToken)) {
      headers['X-CSRF-TOKEN'] = csrfToken;
    }

    if (!_.isNil(contentType)) {
      headers['Content-Type'] = contentType;
    }

    if (!_.isNil(accept)) {
      headers.Accept = accept;
    }

    response = await fetch(fetchUrl, {
      credentials: 'same-origin',
      method: fetchMethod,
      headers,
      ...fetchRequestData,
    });

    const responseJSON = await response.json();
    if (onFetchSuccess) {
      dispatch(onFetchSuccess(responseJSON));
    }
  } catch (error) {
    if (onFetchFailure) {
      dispatch(onFetchFailure(error, response));
    }
    errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
  }
};
