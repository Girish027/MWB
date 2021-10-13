import { get, stateGetter } from 'utils/grid/stateGetter';
import expect from 'expect';
import { fromJS, Map, List } from 'immutable';

describe('stateGetter', () => {
  const props = {
    reducerKeys: {
      dsd: 'dsd',
    },
  };
  const state = {
    get: jest.fn(),
    ffd: 'sds',
    dsd: 'dfsdf',
    dfsdf: {
      get: jest.fn(),
      add: 'dds',
    },
  };

  test('It should convert capital letter string to camelize characters', () => {
    const val1 = stateGetter(undefined, props, 'dsd', 'add');
    expect(val1).toEqual(null);
  });

  test('It should convert capital letter string to camelize characters', () => {
    const props = {
      reducerKeys: 'das',
    };
    const val1 = stateGetter(state, props, 'dsd', 'add');
    expect(val1).toEqual(null);
  });

  test('It should convert capital letter string to camelize characters', () => {
    const props = {
      reducerKeys: 'das',
    };
    const val1 = stateGetter(state, undefined, 'dsd', 'add1');
    expect(val1).toEqual(null);
  });

  test('It should convert capital letter string to camelize characters', () => {
    const props = {
      reducerKeys: 'das',
    };
    const state = {
      das: 'sdfdsf',
      ffd: 'dsfds',
    };
    const val1 = stateGetter(state, props, 'ffd', 'add2');
    expect(val1).toEqual(null);
  });

  test('It should convert capital letter string to camelize characters', () => {
    const dfsdf = {
      get: jest.fn(),
      add3: 'dds',
    };
    const state = {
      ffd: 'sds',
      dsd: 'dfsdf',
      dfsdf,
    };
    const val1 = stateGetter(state, props, 'dsd', 'add3');
    expect(val1).toEqual(undefined);
  });

  // sample redux wrapper for state.get
  function getState() {
    return true;
  }

  function getStateWithImmutable() {
    return Map({
      x: 1,
    });
  }

  test('Should return state if its registered', () => {
    const state = { filterState: { get: getState } };
    const props = {};
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toBeTruthy();
  });

  test('Should return an immutable object if state is immutable', () => {
    const state = { filterState: { get: getStateWithImmutable } };
    const props = {};
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(Map({
      x: 1,
    }));
  });

  test('Should return null if test\'s not registered', () => {
    const state = { filterState: { get: getState } };
    const props = {};
    expect(
      stateGetter(state, props, 'unknownState', 'someProp'),
    ).toEqual(null);
  });

  test('Should return state when a dynamic key is used if registerd', () => {
    const state = { someFilterState: { get: getState } };
    const props = {
      reducerKeys: {
        filterState: 'someFilterState',
      },
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toBeTruthy();
  });

  test(['Should return state when a dynamic key ',
    'is used, and has immutable state'].join(''), () => {
    const state = { someFilterState: { get: getStateWithImmutable } };
    const props = {
      reducerKeys: {
        filterState: 'someFilterState',
      },
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(Map({
      x: 1,
    }));
  });

  test('Should return null if a dynamic key is used if not registered', () => {
    const state = {};
    const props = {
      reducerKeys: {
        filterState: 'someFilterState',
      },
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(null);
  });

  test('Should return state when it is nested and registered', () => {
    const state = { nested: { filterState: { get: getState } } };
    const props = {
      reducerKeys: 'nested',
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toBeTruthy();
  });

  test(['Should return state when it is nested ',
    'and has immutable state'].join(''), () => {
    const state = {
      nested: { filterState: { get: getStateWithImmutable } },
    };

    const props = {
      reducerKeys: 'nested',
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(Map({
      x: 1,
    }));
  });

  test(['Should return state when it is nested in an immutable object ',
    'and has immutable state'].join(''), () => {
    const state = Map({
      nested: { filterState: { get: getStateWithImmutable } },
    });

    const props = {
      reducerKeys: 'nested',
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(Map({
      x: 1,
    }));
  });

  test('Should return null if state is nested and not registered', () => {
    const state = { nested: {} };
    const props = {
      reducerKeys: 'nested',
    };
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(null);
  });

  test('Should return null when no keys and no state are provided', () => {
    const state = {};
    const props = {};
    expect(
      stateGetter(state, props, 'filterState', 'someProp'),
    ).toEqual(null);
  });

  test('Should return state when its stored using immutable', () => {
    const state = fromJS({ data: { thing: [1] } });
    const props = {};

    expect(
      stateGetter(state, props, 'data', 'thing'),
    ).toEqual(List([1]));
  });

  test('Should return state when its not stored using immutable', () => {
    const state = { data: { thing: [1] } };
    const props = {};

    expect(
      stateGetter(state, props, 'data', 'thing'),
    ).toEqual([1]);
  });
});
