
import * as types from 'state/actions/types';
import { tagDatasetsReducer, defaultState } from 'state/reducers/tag_datasets_reducers';

describe('tagDatasetsReducer', () => {
  test('should return the initial state', () => {
    const results = tagDatasetsReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
});
