
import * as types from 'state/actions/types';
import { consistencyReportReducer, defaultState } from 'state/reducers/consistencyReport_reducers';

describe('consistencyReportReducer', () => {
  test('should return the initial state', () => {
    const results = consistencyReportReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
});
