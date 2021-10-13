import fetch from 'isomorphic-fetch';
import store from 'state/configureStore';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import _ from 'lodash';
import getUrl, { pathKey } from 'utils/apiUrls';
import { modelUpdated, loadProjectModels } from 'state/actions/actions_projectsmanager';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import * as apiUtils from 'utils/apiUtils';
import * as configActions from 'state/actions/actions_configs';
import Model from 'model';
import { getLanguage } from 'state/constants/getLanguage';
import Constants from 'constants/Constants';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import api from 'utils/api';
import * as projectsActions from 'state/actions/actions_projects';
import * as appActions from './actions_app';
import * as types from './types';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

export const modelCreated = ({ model }) => ({
  type: types.MODEL_CREATED,
  model,
});

export const clearModelData = () => ({
  type: types.CLEAR_MODEL_DATA,
});

export const tuneModel = tuneModelId => ({
  type: types.TUNE_MODEL_ID,
  tuneModelId,
});

export const viewModel = viewModelId => ({
  type: types.VIEW_MODEL_ID,
  viewModelId,
});

export const clearViewModel = () => ({
  type: types.CLEAR_VIEW_MODEL_VIEW,
});

export const updateModel = model => ({
  type: types.MODEL_UPDATE,
  model,
});


export const showModelNavigationConfirmationDialog = showModelNavigationConfirmationDialog => ({
  type: types.SHOW_MODEL_NAVIGATION_CONFIRMATION_DIALOG,
  showModelNavigationConfirmationDialog,
});

export const recieveModelTestResults = ({ modelTestResults, speechResults = false }) => ({
  type: types.RECIEVE_MODEL_TEST_RESULTS,
  modelTestResults,
  speechResults,
});

export const modelBatchTestResults = modelBatchTestResults => ({
  type: types.MODEL_BATCH_TEST_RESULTS,
  ...modelBatchTestResults,
});

export const modelBatchJobRequest = ({ modelBatchJobRequest }) => ({
  type: types.MODEL_BATCH_JOB_REQUEST,
  modelBatchJobRequest,
});

export const listBatchTestsInfo = listOfBatchTests => ({
  type: types.LIST_BATCH_TESTS_INFO,
  listOfBatchTests,
});

export const clearListOfBatchTests = () => ({
  type: types.CLEAR_LIST_BATCH_TESTS_INFO,
});

export const clearModelTestResults = () => ({
  type: types.CLEAR_MODEL_TEST_RESULTS,
});

export const checkModelTestResultsFailed = ({ projectId, modelId, modelTestJobId }) => ({
  type: types.CHECK_MODEL_TEST_RESULTS_FAILED,
  projectId,
  modelTestJobId,
  modelId,
});

export const clearModelBatchTestResults = () => ({
  type: types.CLEAR_MODEL_BATCH_TEST_RESULTS,
});

export const newModel = modelType => ({
  type: types.NEW_MODEL,
  modelType,
});

export const modelEditUpdate = model => ({
  type: types.MODEL_EDIT_UPDATE,
  model,
});

export const initiateSingleUtteranceTest = (modelType, fileType) => ({
  type: types.INITIATE_UTTERANCE_TEST,
  modelType,
  fileType,
});

export const audioUpload = () => ({
  type: types.BEGIN_MODEL_TEST_AUDIO_UPLOAD,
});

export const audioUploadFail = () => ({
  type: types.MODEL_TEST_AUDIO_UPLOAD_FAIL,
});

export const audioUploadSuccess = () => ({
  type: types.MODEL_TEST_AUDIO_UPLOAD_SUCCESS,
});

export const updateSpeechModelIdForDigitalModel = (modelId, projectId, speechModelId) => ({
  type: types.UPDATE_SPEECH_MODEL_ID_FOR_DIGITAL_MODEL,
  modelId,
  projectId,
  speechModelId,
});

export const onFetchFailureDeleteModel = (error = {}) => (dispatch) => {
  if (error.code === 500) {
    dispatch(modelAlreadyDeletedFromProject());
    dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.modelAlreadyDeleted));
  } else {
    dispatch(modelDeleteFailed());
    dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.modelDeleteFailed));
  }
};

export const onFetchSuccessDeleteModel = ({ clientId, projectId, modelId }) => (dispatch) => {
  dispatch(projectsActions.refreshProjectsByClient(clientId));
  const deletedModel = Model.ProjectsManager.getModel(projectId, modelId) || {};
  const { version = '', speechModelId = '' } = deletedModel;
  dispatch(modelDeletedFromProject({ projectId, modelId }));
  if (speechModelId) {
    dispatch(modelDeletedFromProject({ projectId, modelId: speechModelId }));
  }
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.modelDeleted(`${version}`)));
};

const modelDeletedFromProject = ({ projectId, modelId }) => ({
  type: types.MODEL_DELETED_FROM_PROJECT,
  projectId,
  modelId,
});

const modelDeleteFailed = () => ({
  type: types.MODEL_DELETE_FAILED,
});

const modelAlreadyDeletedFromProject = () => ({
  type: types.MODEL_ALREADY_DELETED,
});

export const deleteModelFromProject = ({ clientId, projectId, modelId }) => (dispatch, getState) => new Promise((resolve, reject) => {
  let fetchUrl = getUrl(pathKey.modelDelete, { clientId, projectId, modelId });

  api.delete({
    url: fetchUrl,
    onApiSuccess: () => onFetchSuccessDeleteModel({ clientId, projectId, modelId }),
    onApiError: onFetchFailureDeleteModel,
    dispatch,
    getState,
  });
});

export const updateOutOfDomainInConfig = projectId => (dispatch, getState) => {
  const state = getState();
  const { config } = state.config;
  const { projectConfigs } = state.projectsManager;
  const configs = projectConfigs.get(projectId);
  const [...configsKeys] = configs.keys();
  const lastInsertedKey = configsKeys.sort((a, b) => parseInt(a) - parseInt(b))[configs.size - 1];
  const getLastConfig = configs.get(lastInsertedKey);
  const outOfDomainIntent = getLastConfig.out_of_domain_intent
    ? getLastConfig.out_of_domain_intent
    : JSON.parse(getLastConfig.configFile).trainingConfigs.out_of_domain_intent;
  const trainingConfigs = Object.assign({}, config.trainingConfigs, { out_of_domain_intent: outOfDomainIntent });
  const newConfig = Object.assign({}, config, { trainingConfigs });
  dispatch(configActions.configEditUpdate(newConfig));
};

export const createNewModel = projectId => (dispatch, getState) => {
  const configs = Model.ProjectsManager.getConfigsByProjectId(projectId) || null;
  const defaultConfig = Model.ModelConfigManager.getDefaultConfig(configs);

  dispatch(clearModelData());
  dispatch(newModel(Constants.DIGITAL_MODEL));
  if (defaultConfig && defaultConfig.id) {
    dispatch(configActions.fetchConfigById(defaultConfig.id))
      .then(() => {
        dispatch(updateOutOfDomainInConfig(projectId));
        dispatch(configActions.convertToDigitalConfig());
      });
  }
};

export const tuneSelectedModel = model => (dispatch, getState) => {
  const { id, configId } = model;
  dispatch(tuneModel(id));
  dispatch(configActions.fetchConfigById(configId))
    .then(() => {
      dispatch(configActions.convertToDigitalConfig());
    });
};

export const viewSelectedModel = model => (dispatch, getState) => {
  const { id, configId } = model;
  dispatch(viewModel(id));
  dispatch(configActions.fetchConfigById(configId));
};

// Build Digital or Speech Model

export const createSpeechModel = (model, wordclassFile = {}) => (dispatch, getState) => {
  const { configId } = model;

  dispatch(newModel(Constants.DIGITAL_SPEECH_MODEL));
  dispatch(configActions.fetchConfigById(configId))
    .then(() => {
      dispatch(configActions.convertToSpeechConfig());
    })
    .then(() => {
    // TODO: implement selectors.
      const state = getState();
      const {
        config, header, app, projectListSidebar,
      } = state;
      const { userId, csrfToken } = app;
      const { projectId } = projectListSidebar.selectedProjectId;
      const clientId = header.client.id;

      const data = {
        config: config.config,
        model: {
          userId,
          csrfToken,
          projectId,
          ...model,
          clientId,
        },
        wordclassFile,
      };
      dispatch(configActions.importSpeechFile(data, clientId));
    });
};

export const startModelBuild = ({ projectId, modelId, modelType }) => (dispatch, getState) => new Promise((resolve, reject) => {
  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  let fetchUrl = getUrl(pathKey.buildModel, { modelId, clientId });
  if (modelType === 'SPEECH') {
    fetchUrl = getUrl(pathKey.buildModelSpeech, { modelId, clientId });
  }
  fetch(fetchUrl, {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(() => {
      dispatch(modelUpdated({ projectId, modelId, model: { status: 'QUEUED' } }));
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
    });
});

export const onTestSubmitModelSuccess = (data, response = {}) => (dispatch, getState) => {
  const {
    clientId, speechModelId, id, history, startBuild,
  } = data;
  const state = getState();
  dispatch(projectsActions.refreshProjectsByClient(clientId));
  const newModel = apiUtils.normalizeModel(response);
  newModel._key = newModel.id;
  newModel.status = 'NULL';
  const projectId = newModel.projectId;
  let existingModel;
  const projectModels = Model.ProjectsManager.getModelsByProjectId(projectId);
  const transformedList = !_.isNil(projectModels) ? projectModels.toArray() : [];
  if (transformedList.length > 0) {
    existingModel = transformedList.find((record) => record.modelToken === newModel.modelId);
  }
  if (existingModel) {
    newModel.vectorizerTechnology = existingModel.vectorizerTechnology;
    newModel.vectorizer_technology_version = existingModel.vectorizer_technology_version;
  }
  if (typeof newModel.created === 'undefined' && newModel.createdAt) {
    newModel.created = newModel.createdAt;
  }
  dispatch(modelCreated({ model: newModel }));
  if (startBuild) {
    setTimeout(() => {
      dispatch(startModelBuild({
        projectId,
        modelId: newModel.id,
        modelType: newModel.modelType,
      }));
    }, 2000);
  }
  if (newModel.modelType == Constants.DIGITAL_SPEECH_MODEL) {
    if (speechModelId) {
      dispatch(modelDeletedFromProject({ projectId, modelId: speechModelId }));
    }
    dispatch(updateSpeechModelIdForDigitalModel(id, projectId, newModel.id));
  }
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.modelCreated(`${newModel.version}`)));
  logAmplitudeEvent(AmplitudeConstants.CREATE_MODEL_EVENT, state, {
    projectId, modelDBId: newModel.id, clientDBId: clientId,
  });
  if (history) {
    dispatch(loadProjectModels(newModel.projectId));
    dispatch(changeRoute(RouteNames.MODELS, { clientId, projectId: newModel.projectId }, history));
    // dispatch(loadProjectModels(newModel.projectId));
  }
};

export const onTestSubmitModelFailure = (data, error = {}) => (dispatch, getState) => {
  const { projectId, clientId, modelType } = data;
  const state = getState();
  if (error.code === 409) {
    dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.modelAlreadyExists));
    logAmplitudeEvent(AmplitudeConstants.CREATE_MODEL_FAILED_EVENT, state, {
      projectId, clientDBId: clientId, reason: DISPLAY_MESSAGES.modelAlreadyExists, modelType,
    });
  } else {
    errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
  }
};

export const createSpeechOnlyModel = (model, wordclassFile = {}) => (dispatch, getState) => {
  dispatch(newModel(Constants.SPEECH_MODEL));
  const state = getState();
  const {
    config, header, app, projectListSidebar,
  } = state;
  const { userId, csrfToken } = app;
  const { projectId } = projectListSidebar.selectedProjectId;
  const clientId = header.client.id;

  const data = {
    model: {
      userId,
      csrfToken,
      projectId,
      ...model,
      clientId,
    },
    wordclassFile,
  };
  dispatch(configActions.importSpeechFile(data, clientId));
};

export const submitModel = data => (dispatch, getState) => {
  const {
    csrfToken, projectId, clientId, id,
    startBuild, history, name, description, configId, selectedDatasets,
    modelType, speechConfigId, isUnbundled,
    digitalHostedUrl, modelToken = '', speechModelId, modelTechnology, toDefault,
  } = data;
  const state = getState();
  let url = getUrl(pathKey.modelsDigital, {
    clientId, projectId, modelTechnology, toDefault,
  });
  logAmplitudeEvent(AmplitudeConstants.CREATE_MODEL_EVENT, state, {
    projectId, clientDBId: clientId, modelType,
  });

  let modelData = {
    projectId,
    name,
    description,
    configId,
    datasetIds: selectedDatasets,
    modelType,
    speechConfigId,
  };

  if (modelType === Constants.DIGITAL_SPEECH_MODEL) {
    url = getUrl(pathKey.models, { clientId, projectId });
    modelData = {
      ...modelData,
      isUnbundled,
      digitalHostedUrl,
      modelId: modelToken,
    };
  }

  if (modelType === Constants.SPEECH_MODEL) {
    const speechId = (speechConfigId === '0') ? null : speechConfigId;
    modelData = {
      modelType,
      name,
      datasetIds: selectedDatasets,
      speechConfigId: speechId || null,
      isUnbundled,
      description,
    };

    url = getUrl(pathKey.speechModel, {
      clientId,
      projectId,
      trainNow: false,
      modelType: 'speech',
    });
  }

  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: modelData,
      onApiSuccess: response => onTestSubmitModelSuccess(data, response),
      onApiError: error => onTestSubmitModelFailure(data, error),
    });
  });
};

export const combineModel = data => (dispatch, getState) => {
  const {
    projectId, clientId, digitalHostedUrl, modelId,
  } = data;
  const state = getState();
  const urlData = {
    clientId,
    projectId,
    modelId,
    digitalHostedUrl,
  };
  const url = getUrl(pathKey.patchCombinedModel, urlData);

  return new Promise((resolve, reject) => {
    api.patch({
      dispatch,
      getState,
      url,
      headers: { 'Content-Type': 'application/json-patch+json' },
      onApiSuccess: response => onUpdateModelSuccess(projectId, modelId, response),
      onApiError: error => onUpdateModelFailure(error),
    });
  });
};

export const onUpdateModelSuccess = (projectId, modelId, response = {}) => (dispatch, getState) => {
  const updatedModel = Model.ProjectsManager.getModel(projectId, modelId) || {};
  const { version = '' } = updatedModel;
  const model = apiUtils.normalizeIds(response);
  dispatch(updateModel(model));
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.modelUpdated(`${version}`)));
  dispatch(loadProjectModels(projectId));
};

export const onUpdateModelFailure = (error = {}) => (dispatch, getState) => {
  const { message } = error;
  dispatch(appActions.displayBadRequestMessage(message));
};

export const updateByModel = data => (dispatch, getState) => {
  const { clientId, model } = data;
  const { description, projectId, id } = model;

  let formData = [{
    op: Constants.OP.REPLACE,
    path: '/description',
    value: description || '',
  }];

  const url = getUrl(pathKey.modelUpdate, { clientId, projectId, modelId: id });
  return new Promise((resolve, reject) => {
    api.patch({
      dispatch,
      getState,
      url,
      headers: {
        'Content-Type': 'application/json-patch+json',
      },
      data: formData,
      onApiSuccess: response => onUpdateModelSuccess(projectId, id, response),
      onApiError: onUpdateModelFailure,
    });
  });
};

// Single Utterance Test for Speech Model

export const onTestSpeechModelSuccess = (modelTestResults = {}, isShow = true, modelType) => (dispatch, getState) => {
  if (isShow) {
    const { evaluations = [{}] } = modelTestResults;
    const { utterance } = evaluations[0];
    const state = getState();

    if (utterance) {
      const { modelId, projectId } = modelTestResults;
      const clientId = state.header.client.id;
      if (Constants.SPEECH_MODEL !== modelType) {
        const digitalModelTestData = {
          clientId,
          modelId,
          projectId,
          testModelType: Constants.DIGITAL_MODEL,
          utterances: [
            utterance,
          ],
        };
        dispatch(testModel(digitalModelTestData));
      }
      dispatch(recieveModelTestResults({ modelTestResults, speechResults: true }));
      logAmplitudeEvent(AmplitudeConstants.TEST_SPEECH_MODEL_COMPLETED_EVENT, state, {
        projectId, clientDBId: clientId, modelId, modelType: Constants.DIGITAL_SPEECH_MODEL,
      });
    } else {
      dispatch(appActions.modalDialogChange(null));
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.evalSpeechFailed));
      logAmplitudeEvent(AmplitudeConstants.TEST_SPEECH_MODEL_FAILED_EVENT, state, { modelType: Constants.DIGITAL_SPEECH_MODEL });
    }
  }
};

export const onTestSpeechModelFailure = (error = {}, isShow = true) => (dispatch, getState) => {
  dispatch(appActions.modalDialogChange(null));
  if (isShow) {
    const { message, code } = error;
    if (code == 504) {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.modelTestConnectionError));
    } else {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.modelTestFailure));
    }
    logAmplitudeEvent(AmplitudeConstants.TEST_SPEECH_MODEL_FAILED_EVENT, getState(), { modelType: Constants.DIGITAL_SPEECH_MODEL });
  }
};

export const testSpeechModel = (data, isShow = true) => (dispatch, getState) => {
  const {
    audioFile, audioURL, fileName, cancelToken, model, ...urlData
  } = data;
  const {
    projectId, clientId, modelId, fileType,
  } = urlData;
  const state = getState();
  const modelType = model ? model.modelType : '';

  dispatch(initiateSingleUtteranceTest(modelType, fileType));
  logAmplitudeEvent(AmplitudeConstants.TEST_SPEECH_MODEL_EVENT, state, {
    projectId, clientDBId: clientId, modelId, modelType, fileType,
  });
  dispatch(clearModelTestResults());
  const url = getUrl(pathKey.testSpeechUtterance, urlData);
  const formData = new FormData();
  if (audioFile) {
    formData.append(Constants.AUDIO_FILE, audioFile, fileName);
  }
  if (audioURL) {
    formData.append(Constants.AUDIO_URL, audioURL);
  }

  return new Promise((resolve, reject) => {
    api.post({
      cancelToken,
      dispatch,
      getState,
      url,
      data: formData,
      onApiSuccess: response => onTestSpeechModelSuccess(response, isShow, modelType),
      onApiError: error => onTestSpeechModelFailure(error, isShow),
    });
  });
};

export const onTestDigitalModelSuccess = (modelTestResults = {}, projectId = '', clientDBId = '', modelId = '', isShow = true) => (dispatch, getState) => {
  if (isShow) {
    const { evaluations = [{}] } = modelTestResults;
    const { utterance } = evaluations[0];
    if (utterance) {
      dispatch(appActions.modalDialogChange(null));
      dispatch(recieveModelTestResults({ modelTestResults }));
      logAmplitudeEvent(AmplitudeConstants.TEST_DIGITAL_MODEL_COMPLETED_EVENT, getState(), {
        projectId, clientDBId, modelId, modelType: Constants.DIGITAL_MODEL,
      });
      dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.testResultsRecieved));
    } else {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.evalSpeechFailed));
      logAmplitudeEvent(AmplitudeConstants.DIGITAL_MODEL_TEST_FAILED_EVENT, getState(), { modelType: Constants.DIGITAL_MODEL });
    }
  }
};

export const onTestDigitalModelFailure = (error = {}, isShow = true) => (dispatch, getState) => {
  if (isShow) {
    const { message, code } = error;
    if (code == 504) {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.modelTestConnectionError));
    } else {
      dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.modelTestFailure));
    }
    logAmplitudeEvent(AmplitudeConstants.DIGITAL_MODEL_TEST_FAILED_EVENT, getState(), { modelType: Constants.DIGITAL_MODEL });
  }
};

// Single Utterance test for Digital Model
export const testModel = (data, isShow = true) => (dispatch, getState) => {
  const {
    utterances, ...urlData
  } = data;

  const {
    projectId, clientId, modelId, testModelType: modelType,
  } = urlData;

  dispatch(initiateSingleUtteranceTest(Constants.DIGITAL_MODEL));
  logAmplitudeEvent(AmplitudeConstants.TEST_DIGITAL_MODEL_EVENT, getState(), {
    projectId, clientDBId: clientId, modelId, modelType,
  });
  const url = getUrl(pathKey.testDigitalTranscription, urlData);

  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: utterances,
      onApiSuccess: response => onTestDigitalModelSuccess(response, projectId, clientId, modelId, isShow),
      onApiError: error => onTestDigitalModelFailure(error, isShow),
    });
  });
};

// Batch Test for Digital Model
export const listBatchTests = data => (dispatch, getState) => {
  // eslint-disable-next-line no-unused-vars
  const {
    clientId, modelId, projectId, csrfToken,
  } = data;
  const fetchUrl = getUrl(pathKey.listBatchTests, {
    clientId, modelId, projectId,
  });

  return new Promise((resolve, reject) => {
    fetch(fetchUrl, {
      method: 'get',
      credentials: 'same-origin',
      headers: {

        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((response) => {
        const listOfBatchTests = apiUtils.normalizeIds(response);
        dispatch(
          listBatchTestsInfo(listOfBatchTests),
        );
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      });
  });
};

export const modelBatchTest = data => (dispatch, getState) => {
  const {
    datasets, userId, csrfToken, ...urlData
  } = data;

  const {
    projectId, clientId, modelId, testModelType,
  } = urlData;
  const fetchUrl = getUrl(pathKey.modelBatchTest, urlData);

  logAmplitudeEvent(AmplitudeConstants.RUN_BATCH_TEST_EVENT, getState(), {
    projectId, clientDBId: clientId, modelId, modelType: testModelType,
  });
  return new Promise((resolve, reject) => {
    fetch(fetchUrl, {
      method: 'post',
      credentials: 'same-origin',
      headers: {

        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(datasets),

    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((response) => {
        response.status = 'NULL';
        dispatch(modelBatchJobRequest({ modelBatchJobRequest: response }));
        dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.testRequestSubmitted));
        listBatchTests(data);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      });
  });
};

export const modelCheckBatchTest = data => (dispatch, getState) => {
  // eslint-disable-next-line no-unused-vars
  const {
    clientId, modelId, projectId, csrfToken, modelTestJobId,
  } = data;
  const fetchUrl = getUrl(pathKey.modelCheckBatchTest, {
    clientId, modelId, projectId, modelTestJobId,
  });
  return new Promise((resolve, reject) => {
    fetch(fetchUrl, {
      method: 'get',
      credentials: 'same-origin',
      headers: {

        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((response) => {
        // response.status = 'NULL';
        dispatch(modelBatchTestResults({
          modelId,
          projectId,
          modelTestJobId,
          modelBatchTestResults: response,
        }));
      })
      .catch((error) => {
        dispatch(checkModelTestResultsFailed({
          modelId,
          projectId,
          modelTestJobId,
        }));
        errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      });
  });
};

export const modelCheckBatchTestState = (data) => {
  // eslint-disable-next-line no-unused-vars
  const {
    clientId, modelId, projectId, csrfToken, modelTestJobId,
  } = data;
  const fetchUrl = getUrl(pathKey.modelCheckBatchTest, {
    clientId, modelId, projectId, modelTestJobId,
  });
  return new Promise((resolve, reject) => {
    fetch(fetchUrl, {
      method: 'get',
      credentials: 'same-origin',
      headers: {

        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json',
      },
    })
      .then(errorMessageUtil.handleErrors)
      .then(response => response.json())
      .then((response) => {
        store.dispatch(listBatchTests(data));
        resolve(response);
      })
      .catch((error) => {
        errorMessageUtil.dispatchError(error, store.dispatch, appActions.displayBadRequestMessage);
        store.dispatch(checkModelTestResultsFailed({ modelId, projectId, modelTestJobId }));
        reject(error);
      });
  });
};
