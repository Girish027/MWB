import fetch from 'isomorphic-fetch';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import { addDatasetToProject, fetchDatasetTransform } from 'state/actions/actions_datasets';
import * as apiUtils from 'utils/apiUtils';
import getUrl, { pathKey } from 'utils/apiUrls';
import { getLanguage } from 'state/constants/getLanguage';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import Constants from 'constants/Constants';
import api from 'utils/api';
import * as projectsActions from 'state/actions/actions_projects';
import { escapeRegExp } from 'utils/StringUtils';
import * as appActions from './actions_app';
import * as types from './types';
import { colors } from '../../styles';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

export const reset = () => ({
  type: types.CREATE_DATASET_DIALOG_RESET,
});

export const fileUploadSuccess = ({ token, columns, previewData }) => ({
  type: types.CREATE_DATASET_DIALOG_FILE_UPLOAD_SUCCESS,
  token,
  columns,
  previewData,
});

export const changeStep = ({ step }) => ({
  type: types.CREATE_DATASET_DIALOG_CHANGE_STEP,
  step,
});

export const columnsBind = ({
  bindingArray, isBindingValid, columnsBinding, isPreSelected,
}) => ({
  type: types.CREATE_DATASET_DIALOG_COLUMNS_BIND,
  bindingArray,
  isBindingValid,
  columnsBinding,
  isPreSelected,
});

export const changeAutoTag = ({ autoTag }) => ({
  type: types.CREATE_DATASET_DIALOG_AUTO_TAG_CHANGE,
  autoTag,
});

export const changesStartTransform = ({ startTransform }) => ({
  type: types.CREATE_DATASET_DIALOG_START_TRANSFORM_CHANGE,
  startTransform,
});

export const changeFirstRowSkip = ({ skipFirstRow }) => ({
  type: types.CREATE_DATASET_DIALOG_FISRT_ROW_SKIP_CHANGE,
  skipFirstRow,
});

const columnsBindRequest = ({ token, bindingArray, skipFirstRow }) => ({
  type: types.CREATE_DATASET_DIALOG_COLUMNS_BIND_REQUEST,
  token,
  bindingArray,
  skipFirstRow,
});

const columnsBindError = ({
  token, bindingArray, skipFirstRow, error,
}) => ({
  type: types.CREATE_DATASET_DIALOG_COLUMNS_BIND_ERROR,
  token,
  bindingArray,
  skipFirstRow,
  error,
});

const columnsBindSuccess = ({
  token, bindingArray, skipFirstRow, fileId,
}) => ({
  type: types.CREATE_DATASET_DIALOG_COLUMNS_BIND_SUCCESS,
  token,
  bindingArray,
  skipFirstRow,
  fileId,
});

export const requestColumnsBind = ({ token, bindingArray, skipFirstRow }) => (dispatch, getState) => new Promise((resolve, reject) => {
  dispatch(columnsBindRequest({ token, bindingArray, skipFirstRow }));

  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.importDatasetColumnsBind, { token, skipFirstRow, clientId });

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
      /* fileId, name, systemName */
      const bindData = Object.assign({}, {
        token,
        bindingArray,
        skipFirstRow,
        fileId: response.fileId,
      });
      dispatch(columnsBindSuccess(bindData));
      resolve(bindData);
    })
    .catch((error) => {
      dispatch(columnsBindError({
        token, bindingArray, skipFirstRow, error,
      }));
      dispatch(appActions.modalDialogChange(null));
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      reject(error);
      dispatch(reset());
    });
});

const createDatasetRequest = () => ({
  type: types.CREATE_DATASET_REQUEST,
});

const createDatasetError = () => ({
  type: types.CREATE_DATASET_ERROR,
});

const createDatasetSuccess = () => ({
  type: types.CREATE_DATASET_SUCCESS,
});

export const createDataset = ({
  projectId, clientId, dataType, locale, name, uri, description, autoTagDataset, mapToProjectId,
}) => (dispatch, getState) => new Promise((resolve, reject) => {
  dispatch(createDatasetRequest());

  const state = getState();
  const { userDetails = Constants.OBJECT_DEFAULT_VALUE, csrfToken } = state.app;
  const { userType = Constants.DEFAULT_VALUE } = userDetails;
  const fetchUrl = getUrl(pathKey.datasets, { clientId });
  let source = Constants.DATASET_EXTERNAL_FLAG;

  logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_EVENT, state, {
    projectId, clientDBId: clientId,
  });

  if (userType == Constants.USER_TYPE_INTERNAL) {
    source = Constants.DATASET_INTERNAL_FLAG;
  }

  fetch(fetchUrl, {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      dataset: {
        projectId, clientId, dataType, locale, name, source, uri, description,
      },
      projectId,
      autoTagDataset,
    }),
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      const newDataset = apiUtils.normalizeIds(response);
      const dataset = Object.assign({ status: 'NULL', type: newDataset.dataType, description: newDataset.description || '' }, newDataset);
      dispatch(createDatasetSuccess());
      if (!mapToProjectId) {
        resolve(dataset);
      } else {
        dispatch(addDatasetToProject({
          datasetId: dataset.id,
          projectId,
          dataset,
        }))
          .then(() => {
            logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_COMPLETED_EVENT, state, {
              projectId, datasetId: dataset.id, clientDBId: clientId,
            });
            dispatch(projectsActions.refreshProjectsByClient(clientId));
            resolve(dataset);
          })
          .catch((error) => {
            logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_FAILED_EVENT, state, {
              projectId, clientDBId: clientId,
            });
            dispatch(appActions.modalDialogChange(null));
            reject(error);
          });
      }
    })
    .catch((error) => {
      dispatch(appActions.modalDialogChange(null));
      switch (error.status) {
      case 400:
        logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_FAILED_EVENT, state, {
          projectId, clientDBId: clientId, source, reason: DISPLAY_MESSAGES.createFailed,
        });
        dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.createFailed));
        break;
      case 403:
        logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_FAILED_EVENT, state, {
          projectId, clientDBId: clientId, source, reason: DISPLAY_MESSAGES.datasetOperationNotAllowed,
        });
        dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.datasetOperationNotAllowed));
        break;
      case 409:
        logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_FAILED_EVENT, state, {
          projectId, clientDBId: clientId, source, reason: DISPLAY_MESSAGES.datasetAlreadyExists(name),
        });
        dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.datasetAlreadyExists(name)));
        break;
      default:
        logAmplitudeEvent(AmplitudeConstants.CREATE_DATASET_FAILED_EVENT, state, {
          projectId, clientDBId: clientId, source, reason: DISPLAY_MESSAGES.datasetCreateError,
        });
        dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.datasetCreateError));
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      }
      dispatch(createDatasetError());
      reject(error);
    });
});

export const onDatasetImportSuccess = (response = {}, importData) => (dispatch, getState) => {
  let { token, columns, previewData } = response;
  if (previewData && previewData.length > 10) {
    previewData = previewData.slice(0, 10);
  }
  dispatch(fileUploadSuccess({ token, columns, previewData }));
  dispatch(changeStep({ step: 'mapping' }));
  dispatch(columnBinding(importData));
};

export const onDatasetImportFailure = (error = {}) => (dispatch, getState) => {
  const { code } = error;
  if (code == 400) {
    const { message } = error;
    dispatch(appActions.displayBadRequestMessage(message));
  } else if (code == 401) {
    dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.sessionExpired));
  } else {
    dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.fileParseFail));
  }
  dispatch(reset());
  dispatch(appActions.modalDialogChange(null));
};

export const importFile = importData => (dispatch, getState) => {
  const { dropZone } = importData;
  const url = getUrl(pathKey.importDataset);
  const formData = new FormData();
  if (dropZone) {
    formData.append('datatype', null);
    formData.append(Constants.DATASET_FILE, dropZone, dropZone.name);
  }

  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: formData,
      onApiSuccess: response => onDatasetImportSuccess(response, importData),
      onApiError: onDatasetImportFailure,
    });
  });
};

export const columnBinding = importData => (dispatch, getState) => {
  const state = getState();
  const { createDatasetDialog } = state;
  const {
    columns, previewData, token,
    isMappingRequestLoading,
  } = createDatasetDialog;
  const { isSomeColumnsPreSelected, columnsBinding } = tryPreSelectColumns(columns,
    previewData, state.createDatasetDialog.columnsBinding);

  let requiredColumns = [];
  let columnsByName = {};

  columns.forEach(column => {
    columnsByName[column.name] = column;
    if (column.required || column.name === 'rutag') {
      requiredColumns.push(column.name);
    }
  });

  if (isSomeColumnsPreSelected) {
    let isBindingValid = true;
    requiredColumns.forEach(column => {
      if (typeof columnsBinding[column] === 'undefined') {
        isBindingValid = false;
      }
    });

    const bindingArray = [];
    for (const columnName in columnsBinding) {
      if (!columnsBinding.hasOwnProperty(columnName)) {
        continue;
      }
      const column = columnsByName[columnName];
      bindingArray.push({
        id: `${column.id}`,
        columnName: column.name,
        columnIndex: `${columnsBinding[columnName]}`,
        displayName: column.displayName,
      });
    }

    isBindingValid = isBindingValid && bindingArray.length > 0;

    dispatch(columnsBind({
      bindingArray, isBindingValid, columnsBinding, isPreSelected: true,
    }));

    if (isBindingValid && bindingArray.length > 0 && token && !isMappingRequestLoading) {
      dispatch(confirmDatasetUpload(importData));
    } else {
      dispatch(showDatasetMapperDialog(importData));
    }
  } else {
    dispatch(showDatasetMapperDialog(importData));
  }
};

const showDatasetMapperDialog = importData => (dispatch, getState) => {
  const state = getState();
  const { createDatasetDialog } = state;
  dispatch(appActions.modalDialogChange({
    dispatch,
    createDatasetDialog,
    type: Constants.DIALOGS.DATASET_COLUMN_MAPPING_DIALOG,
    onOk: () => dispatch(confirmDatasetUpload(importData)),
  }));
};

const confirmDatasetUpload = importData => (dispatch, getState) => {
  const state = getState();
  const { createDatasetDialog, app } = state;
  const { userId, csrfToken } = app;
  const { token, bindingArray, skipFirstRow } = createDatasetDialog;
  const {
    name, description, projectId,
    clientId, locale, dataType,
  } = importData;
  dispatch(appActions.modalDialogChange({
    dispatch,
    type: Constants.DIALOGS.PROGRESS_DIALOG,
    message: Constants.UPLOAD_DATASET_PROGRESS,
    header: Constants.UPLOADING_DATASET,
    showHeader: true,
    showFooter: true,
    cancelVisible: true,
    closeIconVisible: true,
    cancelChildren: Constants.CLOSE,
    styleOverride: {
      childContainer: {
        marginTop: '30px',
        marginBottom: '10px',
      },
      content: {
        top: '160px',
        maxWidth: '444px',
        maxHeight: '333px',
        left: 'calc((100vw - 500px) / 2)',
      },
      cancel: {
        color: `${colors.white}`,
        backgroundColor: `${colors.cobalt}`,
        paddingLeft: '25px',
        paddingRight: '25px',
        ':hover': {
          color: `${colors.white}`,
          backgroundColor: `${colors.prussianBlue}`,
        },
      },
    },
  }));
  dispatch(requestColumnsBind({
    token, bindingArray, skipFirstRow,
  })).then((bindData) => {
    const { fileId } = bindData;
    dispatch(createDataset({
      projectId,
      clientId,
      dataType,
      locale,
      name,
      uri: `https://tagging.247-inc.com:8443/nltools/private/v1/files/${fileId}`,
      description,
      autoTagDataset: true,
      mapToProjectId: projectId,
    })).then((dataset) => {
      setTimeout(() => {
        dispatch(fetchDatasetTransform(userId, dataset.id, clientId, projectId, csrfToken, false));
      }, 2000);
      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.datasetCreated(dataset.name)));
      dispatch(reset());
    });
  });
};

const tryPreSelectColumns = (columns, data, columnsBindingInput) => {
  const firstRow = data.length ? data[0] : [];
  const columnsBinding = Object.assign({}, columnsBindingInput);
  if (!firstRow.length || !columns.length || (Object.keys(columnsBinding)).length != 0) {
    return { isSomeColumnsPreSelected: false, columnsBinding };
  }

  columns.forEach((c) => {
    let foundIndexes = [];
    let usedIndexes = {};

    // pass 1 - exact match
    let tests = [
      new RegExp(`^${escapeRegExp(c.name)}$`, 'i'),
      new RegExp(`^${escapeRegExp(c.displayName)}$`, 'i'),
    ];

    firstRow.forEach((newData1, index) => {
      for (let i = 0; i < tests.length; i++) {
        if (tests[i].test(newData1.trim())) {
          foundIndexes.push(index);
          break;
        }
      }
    });

    if (!foundIndexes.length) {
      // pass 2 - partial match
      tests = [
        new RegExp(escapeRegExp(c.name), 'i'),
        new RegExp(escapeRegExp(c.displayName), 'i'),
      ];

      firstRow.forEach((newData2, index) => {
        for (let i = 0; i < tests.length; i++) {
          if (tests[i].test(newData2)) {
            foundIndexes.push(index);
            break;
          }
        }
      });
    }

    if (foundIndexes.length === 1 && !usedIndexes[foundIndexes[0]]) {
      columnsBinding[c.name] = foundIndexes[0];
      usedIndexes[foundIndexes[0]] = true;
    }
  });
  return { isSomeColumnsPreSelected: (Object.keys(columnsBinding)).length != 0, columnsBinding };
};

export const fileUpload = (file, handleSuccess, handleError) => (dispatch, getState) => {
  const fetchUrl = getUrl(pathKey.importDataset);
  const formData = new FormData();
  formData.append('datatype', null);
  formData.append('file', file);
  let req = new XMLHttpRequest();
  req.open('post', fetchUrl, true);
  req.onload = (event) => {
    let { response } = req;
    if (req.status == 200) {
      handleSuccess(response);
    } else {
      handleError(response);
    }
  };
  req.send(formData);
};
