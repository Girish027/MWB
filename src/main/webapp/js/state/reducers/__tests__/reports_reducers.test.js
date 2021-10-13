
import * as types from 'state/actions/types';
import { reportsReducer, defaultState } from 'state/reducers/reports_reducers';

describe('reportsReducer', () => {
  test('should return the initial state', () => {
    const results = reportsReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
});
