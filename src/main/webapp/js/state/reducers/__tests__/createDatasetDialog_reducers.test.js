
import * as types from 'state/actions/types';
import { createDatasetDialogReducer, defaultState } from 'state/reducers/createDatasetDialog_reducers';

describe('createDatasetDialogReducer', () => {
  test('should return the initial state', () => {
    const results = createDatasetDialogReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
});
