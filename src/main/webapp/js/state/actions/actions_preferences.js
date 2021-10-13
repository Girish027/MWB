import store from 'state/configureStore';
import getUrl, { pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import api from 'utils/api';
import * as appActions from './actions_app';
import * as types from './types';

export const technologySelected = ({
  allPreferences = [], clientLevelPreference = {}, isClientAvailable = false, vectorizer, clientType,
}) => ({
  type: types.DEFAULT_TECHNOLOGY,
  allPreferences,
  clientType,
  clientLevelPreference,
  isClientAvailable,
  vectorizer,
});

export const projectPreference = ({
  response,
}) => ({
  type: types.RECEIVE_PROJECT_PREFERENCE,
  response,
});

export const latestVersion = ({
  response,
}) => ({
  type: types.RECEIVE_LATEST_TECHNOLOGY,
  response,
});

export const addOrUpdateTechnology = data => (dispatch, getState) => {
  const state = store.getState();
  const {
    isClientAvailable = false, clientLevelPreference = {}, allPreferences = [], technology,
  } = state.preferences;
  const { projectById } = state.projectListSidebar;
  const projectIdList = Object.keys(projectById);
  const { technologyValue = '', updateExistingModels = false } = data;
  const { id = '' } = clientLevelPreference;
  let isModelsAvailable = false;
  let modelsResponse = allPreferences.filter((record) => record.level === Constants.LEVEL.MODEL);
  let preferenceList = [];
  if (modelsResponse.length > 0) {
    isModelsAvailable = true;
    projectIdList.forEach((project) => {
      const response = modelsResponse.find((record) => record.attribute === project);
      if (!response) {
        preferenceList.push(project);
      }
    });
  } else {
    preferenceList = projectIdList;
  }

  if (isClientAvailable) {
    if (technology === technologyValue) {
      if (updateExistingModels && isModelsAvailable) {
        if (preferenceList.length > 0) {
          dispatch(createModelPreferences({ modelsResponse: preferenceList, technologyValue }));
        }
        dispatch(updateModelTechnology({ technologyValue, modelsResponse }));
      } else if (updateExistingModels) {
        if (preferenceList.length > 0) {
          dispatch(createModelPreferences({ modelsResponse: preferenceList, technologyValue }));
        }
      }
      const technologyType = technology === Constants.MODEL_TECHNOLOGY.USE ? Constants.TENSORFLOW : Constants.N_GRAM;
      const technologyMessage = technologyType === Constants.TENSORFLOW ? Constants.TECHNOLOGY.DIGITAL_TENSORFLOW : Constants.TECHNOLOGY.DIGITAL_NGRAM;
      dispatch(appActions.displayGoodRequestMessage(Constants.SETTINGS_NOTIFICATION_MESSAGE(technologyMessage)));
    } else if (updateExistingModels && isModelsAvailable) {
      dispatch(updateModelTechnology({ technologyValue, modelsResponse }));
      dispatch(updateClientTechnology({ id, technologyValue }));
      if (preferenceList.length > 0) {
        dispatch(createModelPreferences({ modelsResponse: preferenceList, technologyValue }));
      }
    } else {
      if (updateExistingModels && (preferenceList.length > 0)) {
        dispatch(createModelPreferences({ modelsResponse: preferenceList, technologyValue }));
      }
      dispatch(updateClientTechnology({ id, technologyValue }));
    }
  } else if (technologyValue !== '') {
    if (isModelsAvailable) {
      dispatch(updateModelTechnology({ technologyValue, modelsResponse }));
    }
    if ((updateExistingModels) && (preferenceList.length > 0)) {
      dispatch(createModelPreferences({ modelsResponse: preferenceList, technologyValue }));
    }
    dispatch(addClientTechnology({ technologyValue }));
  }
};

export const onUpdateModelTechnologySuccess = (apiResponse, id) => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientRecord = {};
  let clientType;
  let isClientAvailable = false;
  if (allPreferences && allPreferences.length === 0) {
    allPreferences.push(apiResponse);
  } else {
    let index = allPreferences.findIndex((record) => record.id === id);
    allPreferences.splice(index, 1, apiResponse);
    clientRecord = allPreferences.find((record) => record.level === Constants.LEVEL.CLIENT);
    clientRecord = (clientRecord && (Object.keys(clientRecord).length > 0)) ? clientRecord : {};
  }
  if (clientRecord && (Object.keys(clientRecord).length > 0)) {
    isClientAvailable = true;
    if (Object.keys(vectorizer).length !== 0) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientRecord.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
  }
  dispatch(technologySelected({
    allPreferences, clientLevelPreference: clientRecord, clientType, isClientAvailable: true, vectorizer,
  }));
};

export const onUpdateModelTechnologyFailure = (error) => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientLevelPreference = {};
  let isClientAvailable = false;
  let clientType;
  if (allPreferences && allPreferences.length !== 0) {
    const clientRecord = allPreferences.find((record) => record.level === Constants.LEVEL.CLIENT);
    clientLevelPreference = (clientRecord && (Object.keys(clientRecord).length > 0)) ? clientRecord : {};
  }
  if (clientLevelPreference && (Object.keys(clientLevelPreference).length > 0)) {
    isClientAvailable = true;
    if (Object.keys(vectorizer).length !== 0) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientLevelPreference.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
  }
  dispatch(technologySelected({
    allPreferences, clientLevelPreference, isClientAvailable, vectorizer, clientType,
  }));
};

export const onCreateModelPreferencesSuccess = (apiResponse) => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientRecord = {};
  let isClientAvailable = false;
  let clientType;
  allPreferences.push(apiResponse);
  clientRecord = allPreferences.find((record) => record.level === Constants.LEVEL.CLIENT);
  clientRecord = (clientRecord && (Object.keys(clientRecord).length > 0)) ? clientRecord : {};
  isClientAvailable = clientRecord.type !== '';
  if (clientRecord && (Object.keys(clientRecord).length > 0)) {
    isClientAvailable = true;
    if (Object.keys(vectorizer).length !== 0) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientRecord.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
  }
  dispatch(technologySelected({
    allPreferences, clientLevelPreference: clientRecord, clientType, isClientAvailable: true, vectorizer,
  }));
};

export const onCreateModelPreferencesFailure = (error) => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientLevelPreference = {};
  let isClientAvailable = false;
  let clientType;
  if (allPreferences && allPreferences.length !== 0) {
    const clientRecord = allPreferences.find((record) => record.level === Constants.LEVEL.CLIENT);
    clientLevelPreference = (clientRecord && (Object.keys(clientRecord).length > 0)) ? clientRecord : {};
  }
  if (clientLevelPreference && (Object.keys(clientLevelPreference).length > 0)) {
    isClientAvailable = true;
    if (Object.keys(vectorizer).length !== 0) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientLevelPreference.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
  }
  dispatch(technologySelected({
    allPreferences, clientLevelPreference, isClientAvailable, vectorizer, clientType,
  }));
};

export const updateModelTechnology = data => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {}, clientId } = state.preferences;
  const { modelsResponse, technologyValue = '' } = data;
  const vectorizerId = technologyValue === Constants.MODEL_TECHNOLOGY.USE ? vectorizer.USE : vectorizer.N_GRAM;
  modelsResponse.forEach((record) => {
    const { id } = record;
    let url = getUrl(pathKey.updateTechnology, {
      clientId,
      id,
    });
    let formData = [{
      op: Constants.OP.REPLACE,
      path: '/value',
      value: vectorizerId,
    }];

    api.patch({
      dispatch,
      getState,
      url,
      headers: {
        'Content-Type': 'application/json-patch+json',
      },
      data: formData,
      onApiSuccess: response => onUpdateModelTechnologySuccess(response, id),
      onApiError: error => onUpdateModelTechnologyFailure(error),
    });
  });
};

export const onAddOrUpdateClientTechnologySuccess = (apiResponse = {}) => (dispatch, getState) => {
  const state = store.getState();
  let { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientType;
  if (allPreferences && allPreferences.length === 0) {
    allPreferences.push(apiResponse);
  } else {
    allPreferences = allPreferences.filter((record) => record.level === Constants.LEVEL.MODEL);
    allPreferences.push(apiResponse);
  }
  if (Object.keys(vectorizer).length !== 0) {
    const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === apiResponse.value);
    clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
  }
  const type = clientType === Constants.MODEL_TECHNOLOGY.USE ? Constants.TENSORFLOW : Constants.N_GRAM;
  const technologyMessage = type === Constants.TENSORFLOW ? Constants.TECHNOLOGY.DIGITAL_TENSORFLOW : Constants.TECHNOLOGY.DIGITAL_NGRAM;
  dispatch(technologySelected({
    allPreferences, clientLevelPreference: apiResponse, isClientAvailable: true, vectorizer, clientType,
  }));
  dispatch(appActions.displayGoodRequestMessage(Constants.SETTINGS_NOTIFICATION_MESSAGE(technologyMessage)));
};

export const onAddOrUpdateClientTechnologyFailure = (error) => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {} } = state.preferences;
  let clientLevelPreference = {};
  let clientType;
  let isClientAvailable = false;
  if (allPreferences && allPreferences.length !== 0) {
    const clientRecord = allPreferences.find((record) => record.level === Constants.LEVEL.CLIENT);
    clientLevelPreference = (clientRecord && (Object.keys(clientRecord).length > 0)) ? clientRecord : {};
  }
  if (clientLevelPreference && (Object.keys(clientLevelPreference).length > 0)) {
    isClientAvailable = true;
    if (Object.keys(vectorizer).length !== 0) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientLevelPreference.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
  }
  dispatch(technologySelected({
    allPreferences, clientLevelPreference, isClientAvailable, vectorizer, clientType,
  }));
};

export const updateClientTechnology = data => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], clientId, vectorizer = {} } = state.preferences;
  const { id, technologyValue = '' } = data;
  const vectorizerId = technologyValue === Constants.MODEL_TECHNOLOGY.USE ? vectorizer.USE : vectorizer.N_GRAM;
  if (id === '') {
    dispatch(addClientTechnology({ clientId, technologyValue }));
    return;
  }
  let url = getUrl(pathKey.updateTechnology, {
    clientId,
    id,
  });

  let formData = [{
    op: Constants.OP.REPLACE,
    path: '/value',
    value: vectorizerId,
  }];

  api.patch({
    dispatch,
    getState,
    url,
    headers: {
      'Content-Type': 'application/json-patch+json',
    },
    data: formData,
    onApiSuccess: apiResponse => onAddOrUpdateClientTechnologySuccess(apiResponse),
    onApiError: error => onAddOrUpdateClientTechnologyFailure(error),
  });
};

export const addClientTechnology = data => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {}, clientId } = state.preferences;
  const { technologyValue = '' } = data;
  const vectorizerId = technologyValue === Constants.MODEL_TECHNOLOGY.USE ? vectorizer.USE : vectorizer.N_GRAM;
  let url = getUrl(pathKey.addTechnology, {
    clientId,
    setDefault: true,
  });

  const value = {
    level: Constants.LEVEL.CLIENT,
    type: Constants.VECTORIZER_TYPE,
    value: vectorizerId,
    attribute: clientId,
  };

  api.post({
    dispatch,
    getState,
    url,
    data: value,
    onApiSuccess: apiResponse => onAddOrUpdateClientTechnologySuccess(apiResponse),
    onApiError: error => onAddOrUpdateClientTechnologyFailure(error),
  });
};

export const createModelPreferences = data => (dispatch, getState) => {
  const state = store.getState();
  const { allPreferences = [], vectorizer = {}, clientId } = state.preferences;
  const { modelsResponse, technologyValue = '' } = data;
  const vectorizerId = technologyValue === Constants.MODEL_TECHNOLOGY.USE ? vectorizer.USE : vectorizer.N_GRAM;
  modelsResponse.forEach((record) => {
    let url = getUrl(pathKey.addTechnology, {
      clientId,
      setDefault: true,
    });
    const value = {
      level: Constants.LEVEL.MODEL,
      type: Constants.VECTORIZER_TYPE,
      value: vectorizerId,
      attribute: record,
    };

    api.post({
      dispatch,
      getState,
      url,
      data: value,
      onApiSuccess: response => onCreateModelPreferencesSuccess(response),
      onApiError: error => onCreateModelPreferencesFailure(error),
    });
  });
};

export const onGetTechnologySuccess = (data, response = []) => (dispatch, getState) => {
  const state = store.getState();
  if (response.length > 0) {
    const vectorizerResponse = response.filter((record) => record.type === Constants.VECTORIZER_TYPE);
    const clientLevelPreference = vectorizerResponse.find((record) => record.level === Constants.LEVEL.CLIENT);
    const vectorizer = state.preferences.vectorizer;
    let clientType;
    if (clientLevelPreference && (Object.keys(clientLevelPreference).length > 0)) {
      if (Object.keys(vectorizer).length !== 0) {
        const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientLevelPreference.value);
        clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
      }
      dispatch(technologySelected({
        allPreferences: vectorizerResponse, clientType, clientLevelPreference, isClientAvailable: true, vectorizer,
      }));
    } else {
      dispatch(technologySelected({
        allPreferences: vectorizerResponse, clientType: Constants.MODEL_TECHNOLOGY.N_GRAM, clientLevelPreference: {}, isClientAvailable: false, vectorizer,
      }));
    }
  }
};

export const onGetTechnologyFailure = (error) => (dispatch, getState) => {
  const state = store.getState();
  const vectorizer = state.preferences.vectorizer;
  dispatch(technologySelected({
    clientLevelPreference: {}, isClientAvailable: false, clientType: Constants.MODEL_TECHNOLOGY.N_GRAM, allPreferences: [], vectorizer,
  }));
};

export const getTechnology = data => (dispatch, getState) => {
  const { clientId } = data;
  let url = getUrl(pathKey.getTechnology, {
    clientId,
  });
  api.get({
    dispatch,
    getState,
    url,
    onApiSuccess: response => onGetTechnologySuccess(data, response),
    onApiError: error => onGetTechnologyFailure(error),
  });
};

export const onGetVectorizerSuccess = (response = []) => (dispatch, getState) => {
  const state = getState();
  let { allPreferences = [], clientLevelPreference, isClientAvailable } = state.preferences;
  let clientType;
  if (response.length > 0) {
    const NGramVectorizer = response.find((record) => record.type === Constants.MODEL_TECHNOLOGY.N_GRAM);
    const USEVectorizer = response.find((record) => record.type === Constants.MODEL_TECHNOLOGY.USE && record.isLatest === 1);
    const vectorizer = {
      N_GRAM: NGramVectorizer.id,
      USE: USEVectorizer.id,
    };
    if (clientLevelPreference && (Object.keys(clientLevelPreference).length > 0)) {
      const currentClientType = Object.keys(vectorizer).find(key => vectorizer[key] === clientLevelPreference.value);
      clientType = Constants.MODEL_TECHNOLOGY[currentClientType];
    }
    dispatch(technologySelected({
      vectorizer, allPreferences, clientType, isClientAvailable, clientLevelPreference,
    }));
  }
};

export const onGetVectorizerFailure = (error) => (dispatch, getState) => {
  const state = getState();
  let { allPreferences = [], vectorizer = {} } = state.preferences;
  dispatch(technologySelected({ vectorizer, allPreferences }));
};

export const getVectorizer = data => (dispatch, getState) => {
  let url = getUrl(pathKey.getVectorizer);
  api.get({
    dispatch,
    getState,
    url,
    onApiSuccess: response => onGetVectorizerSuccess(response),
    onApiError: error => onGetVectorizerFailure(error),
  });
};

export const getTechnologyByClientModel = data => (dispatch, getState) => {
  const { clientId, projectId } = data;
  let url = getUrl(pathKey.getVectorizerByClientProject, {
    clientId,
    projectId,
  });
  api.get({
    dispatch,
    getState,
    url,
    onApiSuccess: response => getVectorizerByClientProjectResult(response),
    onApiError: error => getVectorizerByClientProjectResult(),
  });
};

export const getVectorizerByClientProjectResult = (response = {}) => (dispatch, getState) => {
  dispatch(projectPreference({
    response,
  }));
};

export const getVectorizerByTechnology = data => (dispatch, getState) => {
  const { technology } = data;
  let url = getUrl(pathKey.getVectorizerByTechnology, {
    technology,
  });
  api.get({
    dispatch,
    getState,
    url,
    onApiSuccess: response => getVectorizerByTechnologySuccess(response),
    onApiError: error => getVectorizerByTechnologyFailure(error),
  });
};

export const getVectorizerByTechnologySuccess = (response) => (dispatch, getState) => {
  if (response) {
    dispatch(latestVersion({
      response,
    }));
  }
};

export const getVectorizerByTechnologyFailure = (error = {}) => (dispatch, getState) => {
  const response = {};
  dispatch(latestVersion({
    response,
  }));
};
