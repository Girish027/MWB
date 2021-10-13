import fetch from 'isomorphic-fetch';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import * as _ from 'lodash';
import store from 'state/configureStore';

import getUrl, { pathKey } from 'utils/apiUrls';
import { normalizeIds } from 'utils/apiUtils';
import { getLanguage } from 'state/constants/getLanguage';
import * as appActions from './actions_app';
import * as projectManagerActions from './actions_projectsmanager';
import * as types from './types';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

const actionRequestProject = projectId => ({
  type: types.TAGGING_GUIDE_REQUEST_PROJECT,
  projectId,
});

const actionReceiveProject = response => ({
  type: types.TAGGING_GUIDE_RECEIVE_PROJECT,
  project: response,
});

const actionRequestLastImportInfo = projectId => ({
  type: types.TAGGING_GUIDE_REQUEST_LAST_IMPORT_INFO,
  projectId,
});

const actionReceiveLastImportInfo = response => ({
  type: types.TAGGING_GUIDE_RECEIVE_LAST_IMPORT_INFO,
  info: response,
});

const actionRequestSearch = (projectId, sort) => ({
  type: types.TAGGING_GUIDE_REQUEST_SEARCH,
  projectId,
  sort,
});


const actionReceiveSearch = ({ searchResults, usedForTaggingRatio }) => ({
  type: types.TAGGING_GUIDE_RECEIVE_SEARCH,
  searchResults,
  usedForTaggingRatio,
});

const actionRequestTagRemove = (projectId, objectId, index) => ({
  type: types.TAGGING_GUIDE_REQUEST_TAG_REMOVE,
  projectId,
  objectId,
  index,
});

const actionReceiveTagRemove = (projectId, objectId, index) => ({
  type: types.TAGGING_GUIDE_RECEIVE_TAG_REMOVE,
  projectId,
  objectId,
  index,
});

const actionRequestTagUpdate = (projectId, objectId, values) => ({
  type: types.TAGGING_GUIDE_REQUEST_TAG_UPDATE,
  projectId,
  objectId,
  values,
});

const actionReceiveTagUpdate = (projectId, objectId, values) => ({
  type: types.TAGGING_GUIDE_RECEIVE_TAG_UPDATE,
  projectId,
  objectId,
  values,
});

const actionRequestTagCreate = (projectId, values) => ({
  type: types.TAGGING_GUIDE_REQUEST_TAG_CREATE,
  projectId,
  values,
});

const actionReceiveTagCreate = (projectId, objectId, values) => ({
  type: types.TAGGING_GUIDE_RECEIVE_TAG_CREATE,
  projectId,
  objectId,
  values,
});

export const reset = () => ({
  type: types.TAGGING_GUIDE_RESET,
});


export const requestProject = projectId => (dispatch, getState) => {
  dispatch(actionRequestProject(projectId));
  const state = getState();
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.projectById, { projectId, clientId });

  return fetch(fetchUrl, {
    credentials: 'same-origin',
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      const normalizedProject = normalizeIds(json);
      dispatch(actionReceiveProject(normalizedProject));
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const requestLastImportInfo = projectId => (dispatch, getState) => {
  dispatch(actionRequestLastImportInfo(projectId));

  const state = getState();
  const clientId = state.header.client.id;
  const fetchUrl = getUrl(pathKey.intentGuideStats, { projectId, clientId });
  return fetch(fetchUrl, {
    credentials: 'same-origin',
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then(json => dispatch(actionReceiveLastImportInfo(json)))
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const requestSearch = (projectId, sort) => (dispatch) => {
  dispatch(actionRequestSearch(projectId, sort));
  const promise = new Promise((resolve, reject) => {
    const state = store.getState();
    const { userId, csrfToken } = state.app;
    const clientId = state.header.client.id;
    const payload = {
      credentials: 'same-origin',
      method: 'get',
      headers: {
        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
    };

    const fetchUrl = getUrl(pathKey.intentGuideSearch, { projectId, sort, clientId });

    fetch(fetchUrl, payload)
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((json) => {
        const rawSearchResults = json || [];
        let searchResults = [];
        let usedForTaggingRatio = 0;
        if (rawSearchResults.length > 0) {
          let usedTags = 0;
          searchResults = _.map(rawSearchResults, (item, index) => {
            if (item.count > 0) {
              usedTags++;
            }
            return {
              index,
              id: item.id,
              count: item.count,
              percentage: item.percentage,
              intent: item.intent || '',
              rutag: item.rutag || '',
              description: item.description || '',
              examples: item.examples || '',
              keywords: item.keywords || '',
              comments: item.comments || '',
            };
          });
          usedForTaggingRatio = usedTags / searchResults.length;
        }

        dispatch(actionReceiveSearch({ searchResults, usedForTaggingRatio }));
        dispatch(projectManagerActions.projectIntentsLoadSuccess({ projectId, intents: searchResults }));
        resolve(searchResults);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
        dispatch(projectManagerActions.projectIntentsLoadError({ projectId, error }));
        reject(error);
      });
  });

  dispatch(projectManagerActions.projectIntentsLoadRequest({ projectId, promise }));

  return promise;
};

export const requestRemoveTag = ({
  userId, csrfToken, projectId, objectId, index, clientId,
}) => dispatch => new Promise((resolve, reject) => {
  dispatch(actionRequestTagRemove(projectId, objectId, index));

  const fetchUrl = getUrl(pathKey.intentGuideTag, { projectId, intentId: objectId, clientId });

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'delete',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(actionReceiveTagRemove(projectId, objectId, index));
      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.tagRemoved));
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const requestUpdateTag = ({
  userId, csrfToken, projectId, objectId, values, clientId,
}) => dispatch => new Promise((resolve, reject) => {
  dispatch(actionRequestTagUpdate(projectId, objectId, values));

  const bodyObj = [
    {
      op: 'REPLACE',
      path: '/intent',
      value: values.intent || '',
    },
    {
      op: 'REPLACE',
      path: '/rutag',
      value: values.rutag || '',
    },
    {
      op: 'REPLACE',
      path: '/description',
      value: values.description || '',
    },
    {
      op: 'REPLACE',
      path: '/keywords',
      value: values.keywords || '',
    },
    {
      op: 'REPLACE',
      path: '/examples',
      value: values.examples || '',
    },
    {
      op: 'REPLACE',
      path: '/comments',
      value: values.comments || '',
    },
  ];

  const fetchUrl = getUrl(pathKey.intentGuideTag, { projectId, intentId: objectId, clientId });

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'PATCH',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

      'Content-Type': 'application/json-patch+json',
    },
    body: JSON.stringify(bodyObj),
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      dispatch(actionReceiveTagUpdate(projectId, objectId, json));
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const requestCreateTag = ({
  userId, csrfToken, projectId, objectId, values, clientId,
}) => dispatch => new Promise((resolve, reject) => {
  dispatch(actionRequestTagCreate(projectId, objectId, values));

  const bodyObj = {
    id: null,
    intent: values.intent || '',
    rutag: values.rutag || '',
    description: values.description || '',
    examples: values.examples || '',
    keywords: values.keywords || '',
    comments: values.comments || '',
  };

  const fetchUrl = getUrl(pathKey.intentsForProject, { projectId, clientId });
  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

      'Content-Type': 'application/json',
    },
    body: JSON.stringify(bodyObj),
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      const row = Object.assign({}, json, {
        id: json.id,
        count: 0,
        percentage: 0,
        intent: json.intent || '',
        rutag: json.rutag || '',
        description: json.description || '',
        examples: json.examples || '',
        keywords: json.keywords || '',
        comments: json.comments || '',
      });
      dispatch(actionReceiveTagCreate(projectId, json.id, row));
      resolve(row);
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});
