jest.mock('utils/api');

import React from 'react';
import * as configActions from 'state/actions/actions_configs';
import Constants from 'constants/Constants';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import api from 'utils/api';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);

describe('<actions_configs />', () => {
  let store;
  const clientId = '2';

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
        csrfToken: 'jskjfk22',
      },
      header: {
        client: {
          id: '2',
        },
      },
    });
  });

  describe('Snapshots', () => {
    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should do POST call with required data', () => {
      const modelTypeToBeCreated = Constants.DIGITAL_SPEECH_MODEL;
      const data = { wordclassFile: { abc: 'test' }, model: { name: 'test' }, config: {} };
      store.dispatch(configActions.submitConfigAndModel(data, modelTypeToBeCreated));
      expect(api.post).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        data: expect.any(Object),
        onApiSuccess: expect.any(Function),
        onApiError: configActions.onSubmitConfigAndModelFailure,
      });
    });

    test('should call submit model on creating digital model ', () => {
      const modelTypeToBeCreated = Constants.DIGITAL_MODEL;
      const model = { configId: '101', modelType: modelTypeToBeCreated, speechConfigId: null };
      const data = { config: {}, model, wordclassFile: { abc: 'test' } };
      const response = {};
      store.dispatch(configActions.onSubmitConfigAndModelSuccess(modelTypeToBeCreated, data, clientId, response));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call importSpeechFile function on creating speech model', () => {
      const modelTypeToBeCreated = Constants.DIGITAL_SPEECH_MODEL;
      const model = { configId: '101', modelType: modelTypeToBeCreated, speechConfigId: null };
      const wordclassFile = new Blob([
        JSON.stringify({ abc: 'test', name: 'word_classes.txt' }),
      ], { type: 'application/json' });
      const data = { config: {}, model, wordclassFile };
      const response = {};
      store.dispatch(configActions.onSubmitConfigAndModelSuccess(modelTypeToBeCreated, data, clientId, response));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should display config create fail on 400 status', () => {
      const error = { response: { body: { errors: [] } }, status: 400 };
      store.dispatch(configActions.onSubmitConfigAndModelFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should display configAlreadyExists message on 409 status', () => {
      const error = { status: 409 };
      store.dispatch(configActions.onSubmitConfigAndModelFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call addSpeechWithWordClassFile function while user uploads wordclass file', () => {
      const model = { configId: '101', speechConfigId: null };
      const wordclassFile = new Blob([
        JSON.stringify({ abc: 'test', name: 'word_classes.txt' }),
      ], { type: 'application/json' });
      const data = { config: {}, model, wordclassFile };
      store.dispatch(configActions.addSpeechWithWordClassFile(data, clientId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call addSpeechWithoutWordClassFile function while user does not upload wordclass file', () => {
      const model = { configId: '101', speechConfigId: null };
      const data = { config: {}, model, wordclassFile: {} };
      store.dispatch(configActions.addSpeechWithoutWordClassFile(data, clientId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });
});
