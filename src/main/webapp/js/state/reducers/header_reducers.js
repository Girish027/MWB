import * as types from '../actions/types';

export const defaultState = {
  client: { id: '0', itsClientId: '', itsAppId: '' },
  clientList: [],
  versionInfo: null,
  actionItems: [],
};

function headerReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.SET_APP_HEADER_ACTION_ITEMS: {
    return Object.assign({}, state, {
      actionItems: action.actionItems,
    });
  }
  case types.CLEAR_SELECTED_CLIENT:
    return Object.assign({}, state, {
      client: defaultState.client,
    });
  case types.CLIENT_CHANGE:
    return Object.assign({}, state, {
      client: action.client,
    });
  case types.RECEIVE_CLIENT_LIST:
    return Object.assign({}, state, {
      clientList: action.clientList,
    });
  case types.VERSION_INFO:
    return Object.assign({}, state, {
      versionInfo: action.versionInfo,
    });
  default:
    return state;
  }
}

export { headerReducer };
