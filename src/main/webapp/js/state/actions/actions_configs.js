import fetch from 'isomorphic-fetch';

import errorMessageUtil from 'utils/ErrorMessageUtil';
import getUrl, { pathKey } from 'utils/apiUrls';
import { submitModel } from 'state/actions/actions_models';
import * as apiUtils from 'utils/apiUtils';
import { getLanguage } from 'state/constants/getLanguage';
import Constants from 'constants/Constants';
import api from 'utils/api';
import * as appActions from './actions_app';
import * as types from './types';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

export const configUpload = () => ({
  type: types.CONFIG_UPLOAD,
});

export const configUploadSuccess = () => ({
  type: types.CONFIG_UPLOAD_SUCCESS,
});

export const configUploadFail = () => ({
  type: types.CONFIG_UPLOAD_FAIL,
});

export const configCreated = ({ config }) => ({
  type: types.CONFIG_CREATED,
  config,
});

export const configUpdated = ({ config }) => ({
  type: types.CONFIG_UPDATED,
  config,
});

export const configEditUpdate = config => ({
  type: types.CONFIG_EDIT_UPDATE,
  config,
});

export const convertToSpeechConfig = () => ({
  type: types.CONVERT_TO_SPEECH_CONFIG,
});

export const convertToDigitalConfig = () => ({
  type: types.CONVERT_TO_DIGITAL_CONFIG,
});

export const updateTrainingConfigValidity = isTrainingConfigsValid => ({
  type: types.UPDATE_TRAINING_CONFIG_VALIDITY,
  isTrainingConfigsValid,
});

export const updateTransformationValidity = isTransformationValid => ({
  type: types.UPDATE_TRANSFORMATION_VALIDITY,
  isTransformationValid,
});

export const createModelConfig = (data, projectId, csrfToken, clientId) => (dispatch, getState) => new Promise((resolve, reject) => {
  const configData = {
    name: data.name,
    description: data.description,
    projectId,
    configFile: JSON.stringify(data, null, 2),
  };

  const bodyData = JSON.stringify(configData, null, 2);

  const fetchUrl = getUrl(pathKey.config, { clientId });

  fetch(fetchUrl, {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: bodyData,
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      const newConfig = apiUtils.normalizeIds(response);
      newConfig._key = newConfig.id;
      newConfig.status = 'NULL';
      dispatch(configCreated({ config: newConfig }));
      dispatch(fetchConfigById(newConfig.id));

      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.createdSuccess(newConfig.name)));
    })
    .catch((error) => {
      switch (error.status) {
      case 400: {
        if (error.response.body.errors) {
          error.response.body.errors.forEach((e) => {
            const fieldName = e.field.substring(e.field.lastIndexOf('.') + 1, e.field.length);
            data[fieldName] = `${fieldName} required!`;
          });
          dispatch(appActions.displayBadRequestMessage(data));
        }
        const messageLoggedIn = DISPLAY_MESSAGES.createFailed;
        dispatch(appActions.displayBadRequestMessage(messageLoggedIn));
        break;
      }

      case 409: {
        const messageAlreadyExists = DISPLAY_MESSAGES.configAlreadyExists;
        dispatch(appActions.displayBadRequestMessage(messageAlreadyExists));
        break;
      }

      default:
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      }
    });
});

export const onSubmitConfigAndModelSuccess = (modelTypeToBeCreated, data, clientId, response = {}) => (dispatch, getState) => {
  const { model, wordclassFile } = data;
  const newConfig = apiUtils.normalizeIds(response);
  response._key = newConfig.id;
  response.status = 'NULL';
  dispatch(configCreated({ config: newConfig }));
  model.configId = newConfig.id;
  model.modelType = modelTypeToBeCreated;
  model.speechConfigId = null;

  if (modelTypeToBeCreated === Constants.DIGITAL_SPEECH_MODEL && JSON.stringify(wordclassFile) !== '{}') {
    dispatch(importSpeechFile(data, clientId));
  } else {
    dispatch(submitModel(model));
  }

  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.createdSuccess(newConfig.name)));
};

export const onSubmitConfigAndModelFailure = (error = {}) => (dispatch, getState) => {
  const { status } = error;
  if (status == 400) {
    const data = {};
    if (error.response.body.errors) {
      error.response.body.errors.forEach((e) => {
        const fieldName = e.field.substring(e.field.lastIndexOf('.') + 1, e.field.length);
        data[fieldName] = `${fieldName} required!`;
      });
      dispatch(appActions.displayBadRequestMessage(data));
    }
    const messageLoggedIn = DISPLAY_MESSAGES.createFailed;
    dispatch(appActions.displayBadRequestMessage(messageLoggedIn));
  } else if (status == 409) {
    const messageAlreadyExists = DISPLAY_MESSAGES.configAlreadyExists;
    dispatch(appActions.displayBadRequestMessage(messageAlreadyExists));
  } else {
    errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
  }
};


export const submitConfigAndModel = (data, modelTypeToBeCreated = Constants.DIGITAL_MODEL) => (dispatch, getState) => {
  const configName = `${data.model.name}-cfg`;
  data.config.name = configName;
  const { model } = data;
  const configData = {
    name: configName,
    description: model.description,
    projectId: model.projectId,
    configFile: JSON.stringify(data.config, null, 2),
  };
  const state = getState();
  const clientId = state.header.client.id;
  const url = getUrl(pathKey.config, { clientId });
  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: configData,
      onApiSuccess: response => onSubmitConfigAndModelSuccess(modelTypeToBeCreated, data, clientId, response),
      onApiError: onSubmitConfigAndModelFailure,
    });
  });
};

export const submitConfigAndModelForSpeech = (data, modelTypeToBeCreated = Constants.SPEECH_MODEL) => (dispatch, getState) => {
  const configName = `${data.model.name}-cfg`;
  const { model } = data;
  const wordclassFile = data.model.wordClassFile;
  const formDataParams = new FormData();
  formDataParams.append('name', configName);
  formDataParams.append('description', '');
  formDataParams.append('projectId', model.projectId);
  formDataParams.append('file', wordclassFile, wordclassFile.name);
  const state = getState();
  const clientId = state.header.client.id;
  const url = getUrl(pathKey.config, { clientId });
  fetch(url, {
    method: 'POST',
    credentials: 'same-origin',
    body: formDataParams,
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      dispatch(addSpeechConfig(response, data));
    })
    .catch((error) => {
      onSubmitConfigAndModelFailure(error);
    });
};

export const addSpeechConfig = (response, data) => (dispatch, getState) => {
  const { model } = data;
  const newConfig = apiUtils.normalizeIds(response);
  response._key = newConfig.id;
  model.speechConfigId = newConfig.id;
  dispatch(submitModel(model));
};

export const addSpeechWithWordClassFile = (data, clientId) => (dispatch, getState) => {
  const { model, wordclassFile = {} } = data;
  const configName = `${data.model.name}-cfg`;

  const formDataParams = new FormData();
  formDataParams.append('name', configName);
  formDataParams.append('description', '');
  formDataParams.append('projectId', model.projectId);
  formDataParams.append('file', wordclassFile, wordclassFile.name);

  const fetchUrl = getUrl(pathKey.config, { clientId });
  fetch(fetchUrl, {
    method: 'POST',
    credentials: 'same-origin',
    body: formDataParams,
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      dispatch(addSpeechConfig(response, data));
    })
    .catch((error) => {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.fileImportFail));
    });
};

export const addSpeechWithoutWordClassFile = (data, clientId) => (dispatch, getState) => {
  const { model } = data;
  const { configId } = model;
  const fetchUrl = getUrl(pathKey.getSpeechConfig, { clientId, configId });
  fetch(fetchUrl, {
    method: 'GET',
    credentials: 'same-origin',
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((response) => {
      dispatch(addSpeechConfig(response, data));
    })
    .catch((error) => {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.fileImportFail));
    });
};

export const importSpeechFile = (data, clientId) => (dispatch, getState) => {
  data = {
    ...data,
    clientId,
  };
  const { wordclassFile = {} } = data;
  if (JSON.stringify(wordclassFile) !== '{}') {
    dispatch(addSpeechWithWordClassFile(data, clientId));
  } else {
    dispatch(addSpeechWithoutWordClassFile(data, clientId));
  }
};

export const requestConfig = configId => ({
  type: types.REQUEST_CONFIG,
  projectId: configId,
});

export const receiveConfig = config => ({
  type: types.RECEIVE_CONFIG,
  config,
  receivedAt: Date.now(),
});

export const fetchConfigById = configId => (dispatch, getState) => {
  dispatch(requestConfig(configId));
  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.configById, { configId, clientId });
  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      let config = null;
      try {
        config = JSON.parse(json.configFile);
      } catch (error) {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      }
      dispatch(receiveConfig(config));
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const showTransformationDeleteDialog = show => ({
  type: types.CONFIG_SHOW_TRANSFORMATION_DELETE_DIALOG,
  show,
});

export const showTransformationPredefinedDialog = show => ({
  type: types.CONFIG_SHOW_TRANSFORMATION_PREDEFINED_DIALOG,
  show,
});

export const showTransformationAddDialog = show => ({
  type: types.CONFIG_SHOW_TRANSFORMATION_ADD_DIALOG,
  show,
});
