
import fetch from 'isomorphic-fetch';
import store from 'state/configureStore';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import * as actionsTaggingGuide from 'state/actions/actions_taggingguide';
import * as types from './types';
import * as appActions from './actions_app';

export const fileUploadSuccess = ({ token, columns, previewData }) => ({
  type: types.TAGGING_GUIDE_IMPORT_FILE_UPLOAD_SUCCESS,
  token,
  columns,
  previewData,
});

export const reset = () => ({
  type: types.TAGGING_GUIDE_IMPORT_RESET,
});

const abortRequest = ({ projectId, token }) => ({
  type: types.TAGGING_GUIDE_IMPORT_ABORT_REQUEST,
  projectId,
  token,
});

const abortError = ({ projectId, token, error }) => ({
  type: types.TAGGING_GUIDE_IMPORT_ABORT_ERROR,
  projectId,
  token,
  error,
});

const abortSuccess = ({ projectId, token }) => ({
  type: types.TAGGING_GUIDE_IMPORT_ABORT_SUCCESS,
  projectId,
  token,
});

const commitRequest = ({ projectId, token }) => ({
  type: types.TAGGING_GUIDE_IMPORT_COMMIT_REQUEST,
  projectId,
  token,
});

const commitError = ({ projectId, token, error }) => ({
  type: types.TAGGING_GUIDE_IMPORT_COMMIT_ERROR,
  projectId,
  token,
  error,
});

const commitSuccess = ({ projectId, token }) => ({
  type: types.TAGGING_GUIDE_IMPORT_COMMIT_SUCCESS,
  projectId,
  token,
});

export const abort = ({ projectId, token, clientId }) => dispatch => new Promise((resolve, reject) => {
  dispatch(abortRequest({ projectId, token }));

  const state = store.getState();
  const { userId, csrfToken } = state.app;

  const fetchUrl = getUrl(pathKey.intentGuideImportCancel, { projectId, token, clientId });

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(abortSuccess({ projectId, token }));
      resolve();
    })
    .catch((error) => {
      dispatch(abortError({ projectId, token, error }));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const commit = ({ projectId, token, clientId }) => dispatch => new Promise((resolve, reject) => {
  dispatch(commitRequest({ projectId, token }));

  const state = store.getState();
  const { userId, csrfToken } = state.app;
  const fetchUrl = getUrl(pathKey.intentGuideImportCommit, { projectId, token, clientId });

  logAmplitudeEvent(AmplitudeConstants.IMPORT_TAGGING_GUIDE_EVENT, state, { projectId, clientDBId: clientId });
  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(commitSuccess({ projectId, token }));
      logAmplitudeEvent(AmplitudeConstants.IMPORT_TAGGING_GUIDE_COMPLETED_EVENT, state, { projectId, clientDBId: clientId });
      // get the tagging guide data after classification indices are created in backend.
      // fix for bug - https://247inc.atlassian.net/browse/NT-2756
      setTimeout(() => {
        dispatch(actionsTaggingGuide.requestSearch(
          projectId, {
            property: 'count',
            direction: 'desc',
          },
        ));
      }, 3000);
      resolve();
    })
    .catch((error) => {
      dispatch(commitError({ projectId, token, error }));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      logAmplitudeEvent(AmplitudeConstants.IMPORT_TAGGING_GUIDE_FAILED_EVENT, state, { projectId, clientDBId: clientId });
      reject(error);
    });
});

export const columnsBind = ({
  bindingArray, isBindingValid, columnsBinding, isPreSelected,
}) => ({
  type: types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND,
  bindingArray,
  isBindingValid,
  columnsBinding,
  isPreSelected,
});

export const changeFirstRowSkip = ({ skipFirstRow }) => ({
  type: types.TAGGING_GUIDE_IMPORT_FISRT_ROW_SKIP_CHANGE,
  skipFirstRow,
});

const columnsBindRequest = ({
  projectId, token, bindingArray, skipFirstRow,
}) => ({
  type: types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_REQUEST,
  projectId,
  token,
  bindingArray,
  skipFirstRow,
});

const columnsBindError = ({
  projectId, token, bindingArray, skipFirstRow, error,
}) => ({
  type: types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_ERROR,
  projectId,
  token,
  bindingArray,
  skipFirstRow,
  error,
});

const columnsBindSuccess = ({
  projectId, token, bindingArray, skipFirstRow, validTagCount, missingTags, invalidTags,
}) => ({
  type: types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_SUCCESS,
  projectId,
  token,
  bindingArray,
  skipFirstRow,
  validTagCount,
  missingTags,
  invalidTags,
});

export const requestColumnsBind = ({
  projectId, token, bindingArray, skipFirstRow, clientId,
}) => dispatch => new Promise((resolve, reject) => {
  dispatch(columnsBindRequest({
    projectId, token, bindingArray, skipFirstRow,
  }));

  const state = store.getState();
  const { userId, csrfToken } = state.app;

  const fetchUrl = getUrl(pathKey.intentGuideImportMapping, {
    projectId,
    token,
    skipFirstRow,
    clientId,
  });

  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

      'Content-Type': 'application/json',
    },
    body: JSON.stringify(bindingArray),
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      dispatch(columnsBindSuccess({
        projectId,
        token,
        bindingArray,
        skipFirstRow,
        validTagCount: response.validTagCount,
        missingTags: response.missingTags,
        invalidTags: response.invalidTags,
      }));
      resolve({
        validTagCount: response.validTagCount,
        missingTags: response.missingTags,
        invalidTags: response.invalidTags,
      });
    })
    .catch((error) => {
      dispatch(columnsBindError({
        projectId, token, bindingArray, skipFirstRow, error,
      }));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
    });
});

export const changeStep = ({ step }) => ({
  type: types.TAGGING_GUIDE_IMPORT_CHANGE_STEP,
  step,
});
