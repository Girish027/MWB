import * as types from 'state/actions/types';
import { OrderedMap } from 'immutable';

export const defaultState = {
  activeRowIndex: null,
  activeColumnIndex: null,
  value: null,
};

function cellEditableReducer(state = new OrderedMap(), action) {
  switch (action.type) {
  case types.CELL_EDITABLE_STATE_CREATE: {
    const { stateKey } = action;
    if (!state.has(stateKey)) {
      return state.set(stateKey, { ...defaultState });
    }
    return state;
  }

  case types.CELL_EDITABLE_STATE_REMOVE: {
    const { stateKey } = action;
    return state.delete(stateKey);
  }

  case types.CELL_EDITABLE_STATE_RESET: {
    const { stateKey } = action;
    return state.set(stateKey, { ...defaultState });
  }

  case types.CELL_EDITABLE_SET_ACTIVE_CELL: {
    const { stateKey, activeRowIndex, activeColumnIndex } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        activeRowIndex,
        activeColumnIndex,
      }));
    }
    return state;
  }

  case types.CELL_EDITABLE_UPDATE_VALUE: {
    const { stateKey, value } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        value,
      }));
    }
    return state;
  }


  default:
    return state;
  }
}

export { cellEditableReducer };
