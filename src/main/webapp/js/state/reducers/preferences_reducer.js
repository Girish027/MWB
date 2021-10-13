import * as types from 'state/actions/types';

export const defaultState = {
  technology: '',
  clientId: null,
  isClientAvailable: false,
  allPreferences: [],
  clientLevelPreference: {},
  vectorizer: {},
  latestTechnology: {},
};

function preferencesReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.DEFAULT_TECHNOLOGY:
    return Object.assign({}, state, {
      technology: action.clientType,
      isClientAvailable: action.isClientAvailable,
      allPreferences: action.allPreferences,
      clientLevelPreference: action.clientLevelPreference,
      vectorizer: action.vectorizer,
    });
  case types.CLIENT_CHANGE:
    return Object.assign({}, state, {
      clientId: action.client.id,
      technology: '',
    });
  case types.RECEIVE_PROJECT_PREFERENCE:
    return Object.assign({}, state, {
      currentType: action.response,
    });
  case types.RECEIVE_LATEST_TECHNOLOGY:
    return Object.assign({}, state, {
      latestTechnology: action.response,
    });
  default:
    return state;
  }
}

export { preferencesReducer };
