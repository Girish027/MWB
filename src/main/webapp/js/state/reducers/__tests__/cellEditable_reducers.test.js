
import * as types from 'state/actions/types';
import { OrderedMap } from 'immutable';
import { cellEditableReducer, defaultState } from 'state/reducers/cellEditable_reducers';

describe('cellEditableReducer', () => {
  test('should return the initial state', () => {
    const results = cellEditableReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_EDITABLE_STATE_CREATE the default state', () => {
    const results = cellEditableReducer(undefined, {
      type: types.CELL_EDITABLE_STATE_CREATE,
      stateKey: 'stateKey',
    });
    expect(results).toEqual(new OrderedMap({ stateKey: { ...defaultState } }));
  });

  test('should handle default CELL_EDITABLE_STATE_CREATE the default state', () => {
    const results = cellEditableReducer(new OrderedMap({ stateKey: 'ff' }), {
      type: types.CELL_EDITABLE_STATE_CREATE,
      stateKey: 'stateKey',
    });
    expect(results).toEqual(new OrderedMap({ stateKey: 'ff' }));
  });

  test('should handle default CELL_EDITABLE_STATE_REMOVE state', () => {
    const results = cellEditableReducer(new OrderedMap({ stateKey: 'ff' }), {
      type: types.CELL_EDITABLE_STATE_REMOVE,
      stateKey: 'stateKey',
    });
    expect(results).toEqual(new OrderedMap({}));
  });

  test('should handle CELL_EDITABLE_STATE_RESET the default state', () => {
    const results = cellEditableReducer(new OrderedMap({ stateKey: 'ff' }), {
      type: types.CELL_EDITABLE_STATE_RESET,
      stateKey: 'stateKey',
    });
    expect(results).toEqual(new OrderedMap({ stateKey: { ...defaultState } }));
  });

  test('should handle CELL_EDITABLE_SET_ACTIVE_CELL state', () => {
    const results = cellEditableReducer(new OrderedMap({ stateKey: 'ff' }), {
      type: types.CELL_EDITABLE_SET_ACTIVE_CELL,
      stateKey: 'stateKey',
      activeRowIndex: 3,
      activeColumnIndex: 5,
    });
    expect(results).toEqual(new OrderedMap({
      stateKey: {
        0: 'f', 1: 'f', activeRowIndex: 3, activeColumnIndex: 5,
      },
    }));
  });

  test('should handle default CELL_EDITABLE_SET_ACTIVE_CELL state', () => {
    const results = cellEditableReducer(undefined, {
      type: types.CELL_EDITABLE_SET_ACTIVE_CELL,
      stateKey: 'stateKey',
      activeRowIndex: 3,
      activeColumnIndex: 5,
    });
    expect(results).toEqual(new OrderedMap({}));
  });

  test('should handle CELL_EDITABLE_UPDATE_VALUE state', () => {
    const results = cellEditableReducer(new OrderedMap({ stateKey: 'ff' }), {
      type: types.CELL_EDITABLE_UPDATE_VALUE,
      stateKey: 'stateKey',
      value: 3,
    });
    expect(results).toEqual(new OrderedMap({ stateKey: { 0: 'f', 1: 'f', value: 3 } }));
  });

  test('should handle default CELL_EDITABLE_UPDATE_VALUE state', () => {
    const results = cellEditableReducer(undefined, {
      type: types.CELL_EDITABLE_UPDATE_VALUE,
      stateKey: 'stateKey',
      value: 3,
    });
    expect(results).toEqual(new OrderedMap({}));
  });
});
