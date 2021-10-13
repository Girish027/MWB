
import * as types from 'state/actions/types';
import { headerReducer, defaultState } from 'state/reducers/header_reducers';

describe('headerReducer', () => {
  test('should return the initial state', () => {
    const results = headerReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });

  test('CLEAR_SELECTED_CLIENT', () => {
    const actionItems = [{
      label: 'action label',
    }, {
      label: 'action label 2',
    }];
    const results = headerReducer(undefined, {
      type: types.SET_APP_HEADER_ACTION_ITEMS,
      actionItems,
    });
    expect(results.actionItems).toEqual(actionItems);
  });

  test('CLEAR_SELECTED_CLIENT', () => {
    const results = headerReducer(undefined, {
      type: types.CLEAR_SELECTED_CLIENT,
    });
    expect(results).toEqual(defaultState);
  });
  test('CLIENT_CHANGE', () => {
    const expectedClient = 'Test Client';
    const results = headerReducer(undefined, {
      type: types.CLIENT_CHANGE,
      client: expectedClient,
    });
    expect(results.client).toEqual(expectedClient);
  });
  test('RECEIVE_CLIENT_LIST', () => {
    const expectedClientList = [
      {
        name: 'MWB',
        id: '32',
      },
      {
        name: 'Test Client',
        id: '100',
      },
      {
        name: 'Internal Client',
        id: '56',
      },
    ];
    const results = headerReducer(undefined, {
      type: types.RECEIVE_CLIENT_LIST,
      clientList: expectedClientList,
    });
    expect(results.clientList).toEqual(expectedClientList);
  });
  test('VERSION_INFO', () => {
    const expectedVersionInfo = {
      ui: {
        version: '2.3',
        name: 'ui',
      },
    };
    const results = headerReducer(undefined, {
      type: types.VERSION_INFO,
      versionInfo: expectedVersionInfo,
    });
    expect(results.versionInfo).toEqual(expectedVersionInfo);
  });
});
