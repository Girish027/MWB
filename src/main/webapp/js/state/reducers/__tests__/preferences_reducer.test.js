import * as types from 'state/actions/types';
import { preferencesReducer, defaultState } from '../preferences_reducer';

describe('preferencesReducer', () => {
  test('should return the initial state', () => {
    const results = preferencesReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
  test('CLIENT_CHANGE', () => {
    const testAction = {
      type: types.CLIENT_CHANGE,
      client: { id: '123' },
    };
    let results = preferencesReducer(undefined, testAction);
    expect(results.clientId).toEqual(testAction.client.id);
  });
  test('DEFAULT_TECHNOLOGY', () => {
    const testAction = {
      type: types.DEFAULT_TECHNOLOGY,
      isDataAvailable: true,
      clientLevelPreference: { id: '1', type: 'USE' },
    };
    let results = preferencesReducer(undefined, testAction);
    expect(results.clientLevelPreference).toEqual(testAction.clientLevelPreference);
  });
});
