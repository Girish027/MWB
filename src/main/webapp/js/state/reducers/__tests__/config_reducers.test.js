
import * as types from 'state/actions/types';
import { configReducer, defaultState } from 'state/reducers/config_reducers';

describe('configReducer', () => {
  test('should return the initial state', () => {
    const results = configReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
  test('PROJECT_CLOSE', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.PROJECT_CLOSE,
    });
    expect(results).toEqual(defaultState);
  });
  test('CLIENT_CHANGE', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.CLIENT_CHANGE,
    });
    expect(results).toEqual(defaultState);
  });
  test('CLEAR_SELECTED_CLIENT', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.CLEAR_SELECTED_CLIENT,
    });
    expect(results).toEqual(defaultState);
  });
  test('MODEL_CREATED', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.MODEL_CREATED,
    });
    expect(results).toEqual(defaultState);
  });
  test('CLEAR_MODEL_DATA', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.CLEAR_MODEL_DATA,
    });
    expect(results).toEqual(defaultState);
  });
  test('REQUEST_CONFIG', () => {
    const expectedConfig = {
      name: 'test config',
    };
    let results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    results = configReducer(undefined, {
      type: types.REQUEST_CONFIG,
    });
    expect(results).toEqual(defaultState);
  });
  test('RECEIVE_CONFIG', () => {
    const expectedConfig = {
      name: 'test config',
    };
    const results = configReducer(undefined, {
      type: types.RECEIVE_CONFIG,
      config: expectedConfig,
    });
    expect(results.config).toEqual(expectedConfig);
  });

  test('CONFIG_EDIT_UPDATE', () => {
    const expectedConfig = {
      name: 'test config',
    };
    const results = configReducer(undefined, {
      type: types.CONFIG_EDIT_UPDATE,
      config: expectedConfig,
    });
    expect(results.config).toEqual(expectedConfig);
  });

  test('CONVERT_TO_SPEECH_CONFIG', () => {
    const expectedConfig = {
      speechConfigs: {},
    };
    const results = configReducer({ config: {} }, {
      type: types.CONVERT_TO_SPEECH_CONFIG,
    });
    expect(results.config).toEqual(expectedConfig);
  });

  test('CONVERT_TO_DIGITAL_CONFIG', () => {
    const initialConfig = {
      config: {
        existingData: {},
        speechConfigs: {},
      },
    };
    const expectedConfig = {
      existingData: {},
    };
    const results = configReducer(initialConfig, {
      type: types.CONVERT_TO_DIGITAL_CONFIG,
    });
    expect(results.config).toEqual(expectedConfig);
  });

  test('UPDATE_TRAINING_CONFIG_VALIDITY', () => {
    const initialConfig = {
      isTransformationValid: true,
    };
    const results = configReducer(initialConfig, {
      type: types.UPDATE_TRAINING_CONFIG_VALIDITY,
      isTrainingConfigsValid: true,
    });
    expect(results.isTrainingConfigsValid).toEqual(true);
    expect(results.isConfigsValid).toEqual(initialConfig.isTransformationValid && true);
  });

  test('UPDATE_TRANSFORMATION_VALIDITY', () => {
    const initialConfig = {
      isTrainingConfigsValid: true,
    };
    const expectedConfig = {
      existingData: {},
    };
    const results = configReducer(initialConfig, {
      type: types.UPDATE_TRANSFORMATION_VALIDITY,
      isTransformationValid: true,
    });
    expect(results.isTransformationValid).toEqual(true);
    expect(results.isConfigsValid).toEqual(initialConfig.isTrainingConfigsValid && true);
  });

  test('CONFIG_SHOW_TRANSFORMATION_ADD_DIALOG', () => {
    const results = configReducer(undefined, {
      type: types.CONFIG_SHOW_TRANSFORMATION_ADD_DIALOG,
      show: true,
    });
    expect(results.showTransformationAddDialog).toEqual(true);
  });
  test('CONFIG_SHOW_TRANSFORMATION_DELETE_DIALOG', () => {
    const results = configReducer(undefined, {
      type: types.CONFIG_SHOW_TRANSFORMATION_DELETE_DIALOG,
      show: true,
    });
    expect(results.showTransformationDeleteDialog).toEqual(true);
  });
  test('CONFIG_SHOW_TRANSFORMATION_PREDEFINED_DIALOG', () => {
    const results = configReducer(undefined, {
      type: types.CONFIG_SHOW_TRANSFORMATION_PREDEFINED_DIALOG,
      show: true,
    });
    expect(results.showTransformationPredefinedDialog).toEqual(true);
  });
});
