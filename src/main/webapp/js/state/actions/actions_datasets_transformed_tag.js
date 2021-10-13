
import fetch from 'isomorphic-fetch';
import * as _ from 'lodash';
import validationUtil from 'utils/ValidationUtil';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import store from 'state/configureStore';
import getUrl, { pathKey } from 'utils/apiUrls';
import { getLanguage } from 'state/constants/getLanguage';
import * as appActions from './actions_app';
import * as types from './types';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;
// TAG

export const updateColumnSort = sort => ({
  type: types.COLUMN_SORT_CHANGE,
  sort,
});

export const uncheckAllSelectedRows = () => ({
  type: types.UNCHECK_ALL_SELECTED_ROWS,
});

export const allRowsUnchecked = () => ({
  type: types.ALL_ROWS_UNCHECKED,
});

export const fireCursorUp = () => ({
  type: types.CURSOR_UP,
});

export const fireCursorDown = () => ({
  type: types.CURSOR_DOWN,
});

export const fireCursorLeft = () => ({
  type: types.CURSOR_LEFT,
});

export const fireCursorRight = () => ({
  type: types.CURSOR_RIGHT,
});

export const updateSearchResultsWithTaggedRow = (updateSearchData, username) => {
  // let newSearchResults = [];
  const oldSearchResults = updateSearchData.searchResults;
  const updatedRow = updateSearchData.updatedRow;
  const updatedIntent = updatedRow.fullResult.intent;

  const newSearchResults = _.map(oldSearchResults, (row) => {
    if (row.id === updatedRow.id) {
      row.manualTag = updatedIntent;
      row.fullResult.intent = updatedIntent;
      row.fullResult.taggedAt = Number(new Date());
      row.fullResult.taggedBy = username;
    }

    return { ...row };
  });

  return {
    type: types.TABLE_SEARCH_RESULTS_UPDATE,
    searchResults: newSearchResults,
  };
};

export const updateSearchResultsWithUntaggedRow = (updateSearchData, username) => {
  const oldSearchResults = updateSearchData.searchResults;
  const updatedRow = updateSearchData.updatedRow;
  const updatedIntent = '';

  const newSearchResults = _.map(oldSearchResults, (row) => {
    if (row.id === updatedRow.id) {
      row.manualTag = updatedIntent;
      row.fullResult.intent = updatedIntent;
      row.fullResult.deletedAt = Number(new Date());
      row.fullResult.deletedBy = username;
    }
    return { ...row };
  });

  return {
    type: types.TABLE_SEARCH_RESULTS_UPDATE,
    searchResults: newSearchResults,
  };
};

export const updateSearchResultsAfterBulkTag = newSearchResults => ({
  type: types.TABLE_SEARCH_RESULTS_UPDATE,
  searchResults: newSearchResults,
});

export const cancelUpdateRender = () => ({
  type: types.PROJECT_TABLE_CANCEL_RENDER,
});

export const updateUserEnteredTag = userEnteredTag => ({
  type: types.UPDATE_TAG_USER_ENTERED,
  userEnteredTag,
});

export const updateActiveCell = (activeCell, activeRowData, cancelUpdate, mousePosition) => ({
  type: types.UPDATE_CELL_ACTIVE,
  activeCell,
  activeRowData,
  cancelUpdateRender: cancelUpdate,
  mousePosition,
});

export const updateActiveBulkCell = activeCell => ({
  type: types.UPDATE_BULK_CELL_ACTIVE,
  activeCell,
});

export const updateIntent = intent => ({
  type: types.UPDATE_INTENT,
  intent,
});

// ////////////////////////////////////////// SUGGEST TAG ACTION

export const selectedSuggestTag = suggestTag => ({
  type: types.SELECTED_TAG_SUGGEST,
  selectedSuggestTag: suggestTag,
});

export const updateCellWithSuggestTag = updatedSuggestTag => ({
  type: types.UPDATE_WITH_TAG_SUGGEST,
  updatedSuggestTag,
});

export const forgetSelectedSuggestedTag = () => ({
  type: types.FORGET_SELECTED_SUGGESTED_TAG,
});

export const requestSuggestTag = () => ({
  type: types.REQUEST_TAG_SUGGEST,
});

export const receiveSuggestTagSuccess = result => ({
  type: types.RECEIVE_TAG_SUGGEST_SUCCESS,
  suggestTagResult: result,
});

export const receiveSuggestTagFail = result => ({
  type: types.RECEIVE_TAG_SUGGEST_FAIL,
  suggestTagResult: result,
});

export const removeTagSuggest = () => ({
  type: types.REMOVE_TAG_SUGGEST,
  suggestTagResult: { status: 4, suggestedTags: [] },
});

// ////////////////////////////////////////// MANUAL TAG ACTION

export const requestManualTag = () => ({
  type: types.REQUEST_TAG_MANUAL,
});


export const receiveManualTagSuccess = (manualTagResult, activeRowData) => ({
  type: types.RECEIVE_TAG_MANUAL_SUCCESS,
  activeRowData,
  manualTagResult,
  suggestTagResult: (manualTagResult.status !== 2) ? { status: 5, suggestedTags: [] } : { status: 2, suggestedTags: [] },
});

export const receiveManualTagFail = manualTagResult => ({
  type: types.RECEIVE_TAG_MANUAL_FAIL,
  manualTagResult,
  suggestTagResult: { status: manualTagResult.status, suggestedTags: [] },
});

export const requestDeleteManualTag = () => ({
  type: types.REQUEST_TAG_MANUAL_DELETE,
});

export const receiveDeleteManualTagSuccess = (manualTagResult, activeRowData) => ({
  type: types.RECEIVE_TAG_MANUAL_DELETE_SUCCESS,
  activeRowData,
  manualTagResult,
  suggestTagResult: (manualTagResult.status !== 2) ? { status: 5, suggestedTags: [] } : { status: 2, suggestedTags: [] },
});

export const receiveDeleteManualTagFail = manualTagResult => ({
  type: types.RECEIVE_TAG_MANUAL_DELETE_FAIL,
  manualTagResult,
  suggestTagResult: { status: manualTagResult.status, suggestedTags: [] },
});

// ////////////////////////////////////////// BULK TAG ACTION

export const updateDeletedBulkTagIntent = () => ({
  type: types.BULK_TAG_INTENT_DELETED,
});

export const requestManualBulkTag = () => ({
  type: types.REQUEST_BULK_TAG_MANUAL,
});

export const requestManualBulkTagUpdate = () => ({
  type: types.REQUEST_BULK_TAG_MANUAL_UPDATE,
});

export const requestManualBulkTagMixed = () => ({
  type: types.REQUEST_BULK_TAG_MANUAL_MIXED,
});

export const forgetSelectedSuggestedBulkTag = () => ({
  type: types.FORGET_SELECTED_SUGGESTED_BULK_TAG,
});

// BULK TAG NEW

export const receiveManualBulkTagSuccess = manualBulkTagResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_SUCCESS,
  manualBulkTagResult,
  suggestTagResult: (manualBulkTagResult.status !== 2) ? { status: 5, suggestedTags: [] } : { status: 2, suggestedTags: [] },
});

export const receiveManualBulkTagFail = manualBulkTagResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_FAIL,
  manualBulkTagResult,
  suggestTagResult: { status: manualBulkTagResult.status, suggestedTags: [] },
});

// BULK TAG UPDATE

export const receiveManualBulkTagUpdateSuccess = manualBulkTagUpdateResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_UPDATE_SUCCESS,
  manualBulkTagResult: manualBulkTagUpdateResult,
  suggestTagResult: (manualBulkTagUpdateResult.status !== 2) ? { status: 5, suggestedTags: [] } : { status: 2, suggestedTags: [] },
});

export const receiveManualBulkTagUpdateFail = manualBulkTagUpdateResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_UPDATE_FAIL,
  manualBulkTagResult: manualBulkTagUpdateResult,
  suggestTagResult: { status: manualBulkTagUpdateResult.status, suggestedTags: [] },
});

// BULK TAG MIXED - NEW & UPDATE

export const requestDeleteManualBulkTag = () => ({
  type: types.REQUEST_BULK_TAG_MANUAL_DELETE,
});

export const receiveDeleteManualBulkTagSuccess = manualBulkTagResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_DELETE_SUCCESS,
  manualBulkTagResult,
  suggestTagResult: (manualBulkTagResult.status !== 2) ? { status: 5, suggestedTags: [] } : { status: 2, suggestedTags: [] },
});

export const receiveDeleteManualBulkTagFail = manualBulkTagResult => ({
  type: types.RECEIVE_BULK_TAG_MANUAL_DELETE_FAIL,
  manualBulkTagResult,
  suggestTagResult: { status: manualBulkTagResult.status, suggestedTags: [] },
});

export const fetchSuggestTag = ({ projectId, intent }) => (dispatch, getState) => {
  dispatch(requestSuggestTag());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.suggestedTags, { projectId, intent, clientId });

  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      if (json.length > 0) {
        const tags = _.map(json, tag => ({ label: tag }));
        const realResult = { status: 3, suggestedTags: tags };
        dispatch(receiveSuggestTagSuccess(realResult));
      } else {
        const invalid = validationUtil.validateTopicGoal(intent);
        if (invalid) {
          // const tagSuggestWarningNewTag = [{label: lang.LABEL_TAG_NO_MATCHING}, {label: lang.LABEL_TAG_NEW}];
          dispatch(receiveSuggestTagSuccess({ status: 2, suggestedTags: [] }));
        } else {
          // const tagSuggestError = [{label: lang.LABEL_TAG_NO_MATCHING}, {label: lang.LABEL_TAG_ERROR_CREATE}];
          dispatch(receiveSuggestTagSuccess({ status: 1, suggestedTags: [] }));
        }
      }
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

// //////////////////////////////////////////////////////////////////////////////////// SINGLE TAG

export const fetchManualTag = ({
  projectId, intent, username, hashlist, updateSearchData, intentUpdated, activeRowData,
}) => (dispatch, getState) => {
  dispatch(requestManualTag());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ intent, username, transcriptionHashList: hashlist }),
  };

  let messageAction = 'added';
  let fetchUrl = getUrl(pathKey.addManualTag, { projectId, clientId });
  if (intentUpdated) {
    fetchUrl = getUrl(pathKey.updateManualTag, { projectId, clientId });
    messageAction = 'updated';
  }

  const goodMessage = `Manual tag "${intent}" ${messageAction}`;

  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      Promise.all([
        dispatch(receiveManualTagSuccess({ status: 1, message: '' }, activeRowData)),
        dispatch(appActions.displayGoodRequestMessage(goodMessage)),
        dispatch(updateSearchResultsWithTaggedRow(updateSearchData, username)),
      ]);
    })
    .catch((error) => {
      const manualTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveManualTagFail(manualTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};


export const fetchDeleteManualTag = ({
  projectId, username, hashlist, updateSearchData, activeRowData,
}) => (dispatch, getState) => {
  dispatch(requestDeleteManualTag());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ username, transcriptionHashList: hashlist }),
  };

  const fetchUrl = getUrl(pathKey.deleteTag, { projectId, clientId });
  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      Promise.all([
        dispatch(receiveDeleteManualTagSuccess({ status: 5, suggestedTags: [] }, activeRowData)),
        dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.granluarTagRemoved)),
        dispatch(updateSearchResultsWithUntaggedRow(updateSearchData, username)),
      ]);
    })
    .catch((error) => {
      const manualTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveDeleteManualTagFail(manualTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

const setTagRequest = ({
  projectId, hashList, intent, isUpdate,
}) => ({
  type: types.SET_TAG_REQUEST,
  projectId,
  hashList,
  intent,
  isUpdate,
});

const setTagError = ({
  projectId, hashList, intent, isUpdate, error,
}) => ({
  type: types.SET_TAG_ERROR,
  projectId,
  hashList,
  intent,
  isUpdate,
  error,
});

const setTagSuccess = ({
  projectId, hashList, intent, isUpdate,
}) => ({
  type: types.SET_TAG_SUCCESS,
  projectId,
  hashList,
  intent,
  isUpdate,
});

export const setTag = ({
  projectId, hashList, intent, isUpdate,
}) => (dispatch, getState) => new Promise((resolve, reject) => {
  dispatch(setTagRequest({
    projectId, hashList, intent, isUpdate,
  }));
  const state = getState();
  const { userId, csrfToken, userName } = state.app;
  const clientId = state.header.client.id;

  let fetchUrl = getUrl(pathKey.addManualTag, { projectId, clientId });
  if (isUpdate) {
    fetchUrl = getUrl(pathKey.updateManualTag, { projectId, clientId });
  }

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': csrfToken,

    },
    body: JSON.stringify({ intent, username: userName, transcriptionHashList: hashList }),
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(setTagSuccess({
        projectId, hashList, intent, isUpdate,
      }));
      resolve();
    })
    .catch((error) => {
      dispatch(setTagError({
        projectId, hashList, intent, isUpdate, error,
      }));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

const removeTagRequest = ({ projectId, hashList }) => ({
  type: types.REMOVE_TAG_REQUEST,
  projectId,
  hashList,
});

const removeTagError = ({ projectId, hashList, error }) => ({
  type: types.REMOVE_TAG_ERROR,
  projectId,
  hashList,
  error,
});

const removeTagSuccess = ({ projectId, hashList }) => ({
  type: types.REMOVE_TAG_SUCCESS,
  projectId,
  hashList,
});

export const removeTag = ({ projectId, hashList }) => (dispatch, getState) => new Promise((resolve, reject) => {
  dispatch(removeTagRequest({ projectId, hashList }));
  const state = getState();
  const { userId, csrfToken } = state.app;
  const { username } = state.header;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.deleteTag, { projectId, clientId });
  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': csrfToken,

    },
    body: JSON.stringify({ username, transcriptionHashList: hashList }),
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(removeTagSuccess({ projectId, hashList }));
      resolve();
    })
    .catch((error) => {
      dispatch(removeTagError({ projectId, hashList, error }));
      reject(error);
    });
});

const setCommentRequest = ({ projectId, hashList, comment }) => ({
  type: types.SET_COMMENT_REQUEST,
  projectId,
  hashList,
  comment,
});

const setCommentError = ({
  projectId, hashList, comment, error,
}) => ({
  type: types.SET_COMMENT_ERROR,
  projectId,
  hashList,
  comment,
  error,
});

const setCommentSuccess = ({ projectId, hashList, comment }) => ({
  type: types.SET_COMMENT_SUCCESS,
  projectId,
  hashList,
  comment,
});

export const setComment = ({ projectId, hashList, comment }) => (dispatch, getState) => new Promise((resolve, reject) => {
  dispatch(setCommentRequest({ projectId, hashList, comment }));
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const { username } = state.header;

  const fetchUrl = getUrl(pathKey.datasetComment, { projectId, clientId });

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': csrfToken,

    },
    body: JSON.stringify({ comment, username, transcriptionHashList: hashList }),
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(setCommentSuccess({ projectId, hashList, comment }));
      resolve();
    })
    .catch((error) => {
      dispatch(setCommentError({
        projectId, hashList, comment, error,
      }));
      reject(error);
    });
});

const removeCommentRequest = ({ projectId, hashList }) => ({
  type: types.REMOVE_COMMENT_REQUEST,
  projectId,
  hashList,
});

const removeCommentError = ({ projectId, hashList, error }) => ({
  type: types.REMOVE_COMMENT_ERROR,
  projectId,
  hashList,
  error,
});

const removeCommentSuccess = ({ projectId, hashList }) => ({
  type: types.REMOVE_COMMENT_SUCCESS,
  projectId,
  hashList,
});

// //////////////////////////////////////////////////////////////////////////////////// BULK TAG

export const showBulkTagCell = () => ({
  type: types.SHOW_BULK_TAG_CELL,
});

export const hideBulkTagCell = () => ({
  type: types.HIDE_BULK_TAG_CELL,
});

export const updateUserEnteredBulkTag = userEnteredBulkTag => ({
  type: types.UPDATE_BULK_TAG_USER_ENTERED,
  userEnteredBulkTag,
});

export const selectedBulkTag = bulkTagIntent => ({
  type: types.SELECTED_BULK_TAG,
  bulkTagIntent,
});

export const fetchManualBulkTag = ({
  projectId, intent, username, hashlist, newSearchResults,
}) => (dispatch, getState) => {
  dispatch(requestManualBulkTag());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ intent, username, transcriptionHashList: hashlist }),
  };

  const fetchUrl = getUrl(pathKey.addManualTag, { projectId, clientId });

  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      Promise.all([
        dispatch(receiveManualBulkTagSuccess({ status: 1, message: '' })),
        dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.bulkTagAdded(intent))),
        dispatch(updateSearchResultsAfterBulkTag(newSearchResults)),
        dispatch(uncheckAllSelectedRows()),
      ]);
    })
    .catch((error) => {
      const manualBulkTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveManualBulkTagFail(manualBulkTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

export const fetchManualBulkTagUpdate = ({
  projectId, intent, username, hashlist, newSearchResults,
}) => (dispatch, getState) => {
  dispatch(requestManualBulkTagUpdate());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ intent, username, transcriptionHashList: hashlist }),
  };

  const fetchUrl = getUrl(pathKey.updateManualTag, { projectId, clientId });

  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      Promise.all([
        dispatch(receiveManualBulkTagUpdateSuccess({ status: 1, message: '' })),
        dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.bulkTagUpdated(intent))),
        dispatch(updateSearchResultsAfterBulkTag(newSearchResults)),
        dispatch(uncheckAllSelectedRows()),
      ]);
    })
    .catch((error) => {
      const manualBulkTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveManualBulkTagUpdateFail(manualBulkTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

export const fetchManualBulkTagMixed = ({
  projectId, intent, username, hashObj, newSearchResults,
}) => (dispatch, getState) => {
  dispatch(requestManualBulkTagMixed());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payloadNew = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ intent, username, transcriptionHashList: hashObj.new }),
  };

  const payloadUpdated = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ intent, username, transcriptionHashList: hashObj.updated }),
  };

  const fetchUrl = getUrl(pathKey.addManualTag, { projectId, clientId });
  const fetchNew = () => fetch(fetchUrl, payloadNew)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(receiveManualBulkTagSuccess({ status: 1, message: '' }));
    })
    .catch((error) => {
      const manualBulkTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveManualBulkTagFail(manualBulkTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });

  const fetchUrlUpdate = getUrl(pathKey.updateManualTag, { projectId });
  const fetchUpdate = () => fetch(fetchUrlUpdate, payloadUpdated)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(receiveManualBulkTagUpdateSuccess({ status: 1, message: '' }));
    })
    .catch((error) => {
      const manualBulkTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveManualBulkTagUpdateFail(manualBulkTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });

  return Promise.all([
    fetchNew(),
    fetchUpdate(),
  ])
    .then(() => {
      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.bulkTagAddNUpdate(intent)));
    })
    .then(() => {
      dispatch(updateSearchResultsAfterBulkTag(newSearchResults));
    })
    .then(() => {
      dispatch(uncheckAllSelectedRows());
    });
};


export const fetchDeleteManualBulkTag = ({
  projectId, username, hashlist, newSearchResults,
}) => (dispatch, getState) => {
  dispatch(requestDeleteManualBulkTag());
  const state = getState();
  const { userId, csrfToken } = state.app;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'post',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',

    },
    body: JSON.stringify({ username, transcriptionHashList: hashlist }),
  };

  const fetchUrl = getUrl(pathKey.deleteTag, { projectId, clientId });

  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      Promise.all([
        dispatch(receiveDeleteManualBulkTagSuccess({ status: 5, suggestedTags: [] })),
        dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.bulkTagRemoved)),
        dispatch(updateSearchResultsAfterBulkTag(newSearchResults)),
      ]);
    })
    .catch((error) => {
      const manualBulkTagResult = { status: 5, suggestedTags: [] };
      dispatch(receiveDeleteManualBulkTagFail(manualBulkTagResult));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
};

export const propagateInvalidIntentMessage = intent => (dispatch) => {
  dispatch(receiveManualTagFail({ status: 2, suggestedTags: [] }));
  dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.invalidIntent(intent)));
};
