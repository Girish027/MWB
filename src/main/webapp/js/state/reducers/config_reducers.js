import * as types from 'state/actions/types';

export const defaultState = {
  config: null,
  isConfigsValid: true,
  isTrainingConfigsValid: true,
  isTransformationValid: true,
  showTransformationAddDialog: false,
  showTransformationDeleteDialog: false,
  showTransformationPredefinedDialog: false,
};

function configReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.PROJECT_CLOSE:
  case types.CLIENT_CHANGE:
  case types.CLEAR_SELECTED_CLIENT:
  case types.MODEL_UPDATE:
  case types.MODEL_CREATED:
  case types.CLEAR_MODEL_DATA:
  case types.REQUEST_CONFIG:
    return Object.assign({}, state, defaultState);

  case types.RECEIVE_CONFIG:
    return Object.assign({}, state, {
      config: action.config,
    });
  case types.CONFIG_EDIT_UPDATE:
    return Object.assign({}, state, {
      config: action.config,
    });

  case types.CONVERT_TO_SPEECH_CONFIG: {
    const speechConfig = Object.assign({}, state.config);
    speechConfig.speechConfigs = {};
    return Object.assign({}, state, {
      config: speechConfig,
    });
  }

  case types.CONVERT_TO_DIGITAL_CONFIG: {
    // remove modelType: 'normalization' from any config to avoid confusion
    const { speechConfigs, modelType, ...digitalConfig } = state.config;
    return Object.assign({}, state, {
      config: digitalConfig,
    });
  }

  case types.UPDATE_TRAINING_CONFIG_VALIDITY: {
    const { isTrainingConfigsValid } = action;
    const { isTransformationValid } = state;
    const isConfigsValid = isTrainingConfigsValid && isTransformationValid;
    return Object.assign({}, state, {
      isTrainingConfigsValid,
      isConfigsValid,
    });
  }

  case types.UPDATE_TRANSFORMATION_VALIDITY: {
    const { isTransformationValid } = action;
    const { isTrainingConfigsValid } = state;
    const isConfigsValid = isTrainingConfigsValid && isTransformationValid;
    return Object.assign({}, state, {
      isTransformationValid,
      isConfigsValid,
    });
  }

  case types.CONFIG_SHOW_TRANSFORMATION_ADD_DIALOG:
    return Object.assign({}, state, {
      showTransformationAddDialog: action.show,
    });
  case types.CONFIG_SHOW_TRANSFORMATION_DELETE_DIALOG:
    return Object.assign({}, state, {
      showTransformationDeleteDialog: action.show,
    });
  case types.CONFIG_SHOW_TRANSFORMATION_PREDEFINED_DIALOG:
    return Object.assign({}, state, {
      showTransformationPredefinedDialog: action.show,
    });
  default:
    return state;
  }
}

export { configReducer };
