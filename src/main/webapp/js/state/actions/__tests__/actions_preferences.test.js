jest.mock('utils/api');

import api from 'utils/api';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import getUrl, { pathKey } from 'utils/apiUrls';
import * as actionsPreferences from 'state/actions/actions_preferences';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_preferences', () => {
  let store;

  const clientLevelPreference = {
    id: 3,
    client_id: 151,
    level: 'client',
    type: 'use_large',
    attribute: '151',
    value: 2,
  };
  const allPreferences = [{
    id: 3,
    client_id: 151,
    level: 'client',
    type: 'use_large',
    attribute: '151',
    value: 1,
  }, {
    id: 4,
    client_id: 151,
    level: 'model',
    type: 'n-gram',
    attribute: '151',
    value: 1,
  }];
  const vectorizer = {
    N_GRAM: 1,
    USE: 2,
  };
  const currentType = {
    id: 2,
    type: 'use_large',
    version: 1.2,
    isLatest: 1,
  };

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
      },
      header: {
        client: {
          id: '123',
        },
      },
      preferences: {
        technology: '',
        clientId: '123',
        isClientAvailable: true,
        clientLevelPreference,
        allPreferences,
        vectorizer,
        currentType,
      },
      projectListSidebar: {
        projectById: {
          111: {
            name: 'TestName',
          },
        },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  test('should call getTechnology', () => {
    store.dispatch(actionsPreferences.getTechnology({ clientId: '123' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onGetTechnologySuccess', () => {
    store.dispatch(actionsPreferences.onGetTechnologySuccess(undefined, allPreferences));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onGetTechnologyFailure', () => {
    store.dispatch(actionsPreferences.onGetTechnologyFailure());
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call technologySelected', () => {
    store.dispatch(actionsPreferences.technologySelected({}));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call addOrUpdateTechnology', () => {
    store.dispatch(actionsPreferences.addOrUpdateTechnology({ technologyValue: 'n-gram', updateExistingModels: false }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onUpdateModelTechnologySuccess', () => {
    const apiResponse = [{
      id: 4,
      client_id: 151,
      level: 'model',
      type: 'use_large',
      attribute: '151',
      value: 1,
    }];
    const id = apiResponse[0].id;
    store.dispatch(actionsPreferences.onUpdateModelTechnologySuccess(apiResponse, id, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onUpdateModelTechnologySuccess with empty allPreferences', () => {
    const apiResponse = [{
      id: 4,
      client_id: 151,
      level: 'model',
      type: 'use_large',
      attribute: '151',
      value: 1,
    }];
    const id = apiResponse[0].id;
    const allPreferences = [];
    store.dispatch(actionsPreferences.onUpdateModelTechnologySuccess(apiResponse, id, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onUpdateModelTechnologyFailure', () => {
    store.dispatch(actionsPreferences.onUpdateModelTechnologyFailure(undefined, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call updateModelTechnology with use_large', () => {
    const modelsResponse = [{
      id: 3,
      client_id: 151,
      level: 'model',
      type: 'use_large',
      attribute: '151',
      value: 2,
    }];
    store.dispatch(actionsPreferences.updateModelTechnology({ technologyValue: 'use_large', modelsResponse }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call updateModelTechnology with N-Gram', () => {
    const modelsResponse = [{
      id: 3,
      client_id: 151,
      level: 'model',
      type: 'n-gram',
      attribute: '151',
      value: 2,
    }];
    store.dispatch(actionsPreferences.updateModelTechnology({ technologyValue: 'n-gram', modelsResponse }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call updateClientTechnology with use_large', () => {
    store.dispatch(actionsPreferences.updateClientTechnology({ technologyValue: 'use_large', id: '3' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call updateClientTechnology with n-gram', () => {
    store.dispatch(actionsPreferences.updateClientTechnology({ technologyValue: 'n-gram', id: '3' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call updateClientTechnology with empty id', () => {
    store.dispatch(actionsPreferences.updateClientTechnology({ technologyValue: 'use_large', id: '' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call addClientTechnology', () => {
    store.dispatch(actionsPreferences.addClientTechnology({ technologyValue: 'use_large' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onAddOrUpdateClientTechnologySuccess', () => {
    const apiResponse = clientLevelPreference;
    store.dispatch(actionsPreferences.onAddOrUpdateClientTechnologySuccess(
      apiResponse, allPreferences, vectorizer,
    ));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onAddOrUpdateClientTechnologySuccess with no data in db', () => {
    const allPreferences = [];
    const apiResponse = clientLevelPreference;
    store.dispatch(actionsPreferences.onAddOrUpdateClientTechnologySuccess(
      apiResponse, allPreferences, vectorizer,
    ));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onAddOrUpdateClientTechnologyFailure', () => {
    store.dispatch(actionsPreferences.onAddOrUpdateClientTechnologyFailure(undefined, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call getVectorizer', () => {
    store.dispatch(actionsPreferences.getVectorizer());
    expect(store.getActions()).toMatchSnapshot();
  });


  test('should call onGetVectorizerSuccess', () => {
    const response = [{
      id: 1,
      type: 'n-gram',
    },
    {
      id: 2,
      type: 'use_large',
      isLatest: 1,
    },
    ];
    store.dispatch(actionsPreferences.onGetVectorizerSuccess(response));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call onGetVectorizerFailure', () => {
    store.dispatch(actionsPreferences.onGetVectorizerFailure());
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call getTechnologyByClientModel', () => {
    store.dispatch(actionsPreferences.getTechnologyByClientModel({ clientId: '123', projectId: '123' }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call getTechnologyByClientModelResult', () => {
    const response = {
      id: 2,
      type: 'use_large',
      version: 1.2,
      isLatest: 1,
    };
    store.dispatch(actionsPreferences.getVectorizerByClientProjectResult(response));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call createModelPreferences', () => {
    store.dispatch(actionsPreferences.createModelPreferences({ technologyValue: 'use_large', modelsResponse: ['111'] }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call createModelPreferencesSuccess', () => {
    const apiResponse = {
      id: 5,
      client_id: 151,
      level: 'model',
      type: 'use_large',
      attribute: '151',
      value: 2,
    };
    store.dispatch(actionsPreferences.onCreateModelPreferencesSuccess(apiResponse, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call createModelPreferencesFailure', () => {
    store.dispatch(actionsPreferences.onCreateModelPreferencesFailure(undefined, allPreferences, vectorizer));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call addOrUpdateTechnology for technology with use', () => {
    store.dispatch(actionsPreferences.addOrUpdateTechnology({ technologyValue: 'use_large', updateExistingModels: true }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call addOrUpdateTechnology for technology with ngram', () => {
    store.dispatch(actionsPreferences.addOrUpdateTechnology({ technologyValue: 'n-gram', updateExistingModels: true }));
    expect(store.getActions()).toMatchSnapshot();
  });

  test('should call createModelPreferences', () => {
    store.dispatch(actionsPreferences.createModelPreferences({ technologyValue: 'use_large', modelsResponse: ['111'] }));
    const testUrl = '/nltools/private/v1/preference?clientId=151&setDefault=true';
    expect(api.post).toHaveBeenCalledTimes(1);
    expect(api.post.mock.calls[0][0].testUrl).toMatchSnapshot();
  });
});
