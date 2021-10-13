import * as types from 'state/actions/types';
import { OrderedMap } from 'immutable';

export const defaultState = {
  intent: '',
  suggestedTags: [],
  cursor: 0,
};

function cellEditableManualTagSuggestReducer(state = new OrderedMap(), action) {
  switch (action.type) {
  case types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE: {
    const { stateKey } = action;
    if (!state.has(stateKey)) {
      return state.set(stateKey, { ...defaultState });
    }
    return state;
  }

  case types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_REMOVE: {
    const { stateKey } = action;
    return state.delete(stateKey);
  }

  case types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_RESET: {
    const { stateKey } = action;
    return state.set(stateKey, { ...defaultState });
  }

  case types.GET_SUGGESTED_TAGS_SUCCESS: {
    const { stateKey, intent, suggestedTags } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        intent,
        suggestedTags,
      }));
    }
    return state;
  }

  case types.CELL_SUGGEST_CURSOR_DOWN: {
    const { stateKey } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        cursor: ++stateObj.cursor,
      }));
    }
    return state;
  }

  case types.CELL_SUGGEST_CURSOR_UP: {
    const { stateKey } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        cursor: --stateObj.cursor,
      }));
    }
    return state;
  }

  case types.CELL_EDITABLE_STATE_RESET: {
    const { stateKey } = action;
    const stateObj = state.get(stateKey);
    if (stateObj) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        intent: '',
        suggestedTags: [],
      }));
    }
    return state;
  }

  case types.CELL_EDITABLE_SET_ACTIVE_CELL: {
    const { stateKey, activeRowIndex, activeColumnIndex } = action;
    const stateObj = state.get(stateKey);
    if (stateObj && activeRowIndex === null || activeColumnIndex === null) {
      return state.set(stateKey, Object.assign({}, stateObj, {
        intent: '',
        suggestedTags: [],
      }));
    }
    return state;
  }

  default:
    return state;
  }
}

export { cellEditableManualTagSuggestReducer };
