
import * as types from 'state/actions/types';
import { OrderedMap } from 'immutable';
import { cellEditableManualTagSuggestReducer, defaultState } from 'state/reducers/cellEditableManualTagSuggest_reducers';

describe('cellEditableManualTagSuggest', () => {
  test('should return the initial state', () => {
    const results = cellEditableManualTagSuggestReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE', () => {
    const testAction = {
      type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE,
      stateKey: 'stateKey',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { ...defaultState } }));
  });

  test('should handle default CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE', () => {
    const testAction = {
      type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_CREATE,
      stateKey: 'stateKey',
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: 'ff' }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: 'ff' }));
  });

  test('should handle CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_REMOVE', () => {
    const testAction = {
      type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_REMOVE,
      stateKey: 'stateKey',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_RESET', () => {
    const testAction = {
      type: types.CELL_EDITABLE_MANUAL_TAG_SUGGEST_STATE_RESET,
      stateKey: 'stateKey',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { ...defaultState } }));
  });

  test('should handle GET_SUGGESTED_TAGS_SUCCESS', () => {
    const testAction = {
      type: types.GET_SUGGESTED_TAGS_SUCCESS,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: 'f' }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { 0: 'f', intent: 'reservation_query', suggestedTags: 'reservation_query' } }));
  });

  test('should handle default GET_SUGGESTED_TAGS_SUCCESS', () => {
    const testAction = {
      type: types.GET_SUGGESTED_TAGS_SUCCESS,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_SUGGEST_CURSOR_DOWN', () => {
    const testAction = {
      type: types.CELL_SUGGEST_CURSOR_DOWN,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: { cursor: 1 } }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { cursor: 2 } }));
  });

  test('should handle default CELL_SUGGEST_CURSOR_DOWN', () => {
    const testAction = {
      type: types.CELL_SUGGEST_CURSOR_DOWN,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_SUGGEST_CURSOR_UP', () => {
    const testAction = {
      type: types.CELL_SUGGEST_CURSOR_UP,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: { cursor: 1 } }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { cursor: 0 } }));
  });

  test('should handle default CELL_SUGGEST_CURSOR_UP', () => {
    const testAction = {
      type: types.CELL_SUGGEST_CURSOR_UP,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_EDITABLE_STATE_RESET', () => {
    const testAction = {
      type: types.CELL_EDITABLE_STATE_RESET,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: { intent: 'reservation' } }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { intent: '', suggestedTags: [] } }));
  });

  test('should handle default CELL_EDITABLE_STATE_RESET', () => {
    const testAction = {
      type: types.CELL_EDITABLE_STATE_RESET,
      stateKey: 'stateKey',
      intent: 'reservation_query',
      suggestedTags: 'reservation_query',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });

  test('should handle CELL_EDITABLE_SET_ACTIVE_CELL', () => {
    const testAction = {
      type: types.CELL_EDITABLE_SET_ACTIVE_CELL,
      stateKey: 'stateKey',
      activeRowIndex: null,
      activeColumnIndex: null,
    };
    const results = cellEditableManualTagSuggestReducer(new OrderedMap({ stateKey: { intent: 'reservation' } }), testAction);
    expect(results).toEqual(new OrderedMap({ stateKey: { intent: '', suggestedTags: [] } }));
  });

  test('should handle default CELL_EDITABLE_SET_ACTIVE_CELL', () => {
    const testAction = {
      type: types.CELL_EDITABLE_SET_ACTIVE_CELL,
      stateKey: 'stateKey',
    };
    const results = cellEditableManualTagSuggestReducer(undefined, testAction);
    expect(results).toEqual(new OrderedMap());
  });
});
