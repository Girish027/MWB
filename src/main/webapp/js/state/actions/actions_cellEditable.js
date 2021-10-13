import * as types from './types';

export const stateCreate = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_STATE_CREATE,
  stateKey,
});

export const stateReset = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_STATE_RESET,
  stateKey,
});

export const stateRemove = ({ stateKey }) => ({
  type: types.CELL_EDITABLE_STATE_REMOVE,
  stateKey,
});

export const setActiveCell = ({ stateKey, activeRowIndex, activeColumnIndex }) => ({
  type: types.CELL_EDITABLE_SET_ACTIVE_CELL,
  stateKey,
  activeRowIndex,
  activeColumnIndex,
});

export const updateValue = ({ stateKey, value }) => ({
  type: types.CELL_EDITABLE_UPDATE_VALUE,
  stateKey,
  value,
});
