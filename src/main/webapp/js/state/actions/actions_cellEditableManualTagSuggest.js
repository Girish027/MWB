import store from 'state/configureStore';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import * as types from './types';

let controller;
export const stateCreate = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE,
  stateKey,
});
export const stateReset = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_RESET,
  stateKey,
});

export const stateRemove = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_REMOVE,
  stateKey,
});

const getSuggestedTagsRequest = ({ stateKey, projectId, intent }) => ({
  type: types.GET_SUGGESTED_TAGS_REQUEST,
  stateKey,
  projectId,
  intent,
});

const getSuggestedTagsError = ({
  stateKey, projectId, intent, error,
}) => ({
  type: types.GET_SUGGESTED_TAGS_ERROR,
  stateKey,
  projectId,
  intent,
  error,
});

const getSuggestedTagsSuccess = ({
  stateKey, projectId, intent, suggestedTags,
}) => ({
  type: types.GET_SUGGESTED_TAGS_SUCCESS,
  stateKey,
  projectId,
  intent,
  suggestedTags,
});

export const getSuggestedTags = ({ stateKey, projectId, intent }) => (dispatch, getState) => new Promise((resolve, reject) => {
  if (controller) {
    controller.abort();
  }
  controller = new AbortController();
  const signal = controller.signal;
  dispatch(getSuggestedTagsRequest({ stateKey, projectId, intent }));
  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.suggestedTags, { projectId, intent, clientId });

  fetch(fetchUrl, {
    signal,
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      const suggestedTags = json && json.length ? json : [];
      const intentIndex = suggestedTags.indexOf(intent);
      if (intentIndex >= 0) {
        suggestedTags.slice(intentIndex, 1);
      }
      dispatch(getSuggestedTagsSuccess({
        stateKey, projectId, intent, suggestedTags,
      }));
      resolve([...suggestedTags]);
    })
    .catch((error) => {
      if (error.name === 'AbortError') { return; }
      dispatch(getSuggestedTagsError({
        stateKey, projectId, intent, error,
      }));
      reject(error);
    });
});

export const cursorDown = ({ stateKey }) => ({
  type: types.CELL_SUGGEST_CURSOR_DOWN,
  stateKey,
});

export const cursorUp = ({ stateKey }) => ({
  type: types.CELL_SUGGEST_CURSOR_UP,
  stateKey,
});
