
import * as types from 'state/actions/types';
import { taggingGuideReducer, defaultState } from 'state/reducers/taggingGuide_reducers';

describe('taggingGuideReducer', () => {
  test('should return the initial state', () => {
    const results = taggingGuideReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });
});
