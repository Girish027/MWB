import base64 from 'base-64';
import * as types from 'state/actions/types';
import Constants from 'constants/Constants';
import { appReducer, defaultState, getUserDetails } from 'state/reducers/app_reducers';

describe('app_reducer', () => {
  test('should return the initial state', () => {
    const results = appReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });

  test('should handle LOGIN_INFO', () => {
    const testAction = {
      type: types.LOGIN_INFO,
      value: {
        userId: '123',
        userName: 'testuser@gmail.com',
        userDetails: base64.encode('{name=Test User, otherinfo=moredetails}'),
      },
    };
    const results = appReducer(undefined, testAction);
    expect(results.userId).toEqual(testAction.value.userId);
    expect(results.userDetails.name).toEqual('Test User');
  });

  test('should handle LOGIN_SUCCESS', () => {
    const testAction = {
      type: types.LOGIN_SUCCESS,
      value: '85a755cb-c0ee-4a9c-ace4-b83ac997c301',
    };
    const results = appReducer(undefined, testAction);
    expect(results.csrfToken).toEqual(testAction.value);
  });
  test('should handle UPDATE_USER_GROUPS', () => {
    const testAction = {
      type: types.UPDATE_USER_GROUPS,
      data: {
        MWB_ROLE_EXTERNAL: '/v1/models/download',
      },
    };
    const results = appReducer(undefined, testAction);
    expect(Object.keys(results.featureFlags)).toEqual(['MWB_ROLE_EXTERNAL', 'DEFAULT']);
  });
  test('should handle GOOD_REQUEST', () => {
    const testAction = {
      type: types.GOOD_REQUEST,
      value: 'Good message',
    };
    const results = appReducer(undefined, testAction);
    expect(results.serverMessage).toEqual(testAction.value);
    expect(results.notificationType).toEqual(Constants.NOTIFICATION.types.success);
  });
  test('should handle WARNING_REQUEST', () => {
    const testAction = {
      type: types.WARNING_REQUEST,
      value: 'Warning message',
    };
    const results = appReducer(undefined, testAction);
    expect(results.serverMessage).toEqual(testAction.value);
    expect(results.notificationType).toEqual(Constants.NOTIFICATION.types.error);
  });
  test('should handle PERSISTENT_REQUEST', () => {
    const testAction = {
      type: types.PERSISTENT_REQUEST,
      value: 'Persistent message',
    };
    const results = appReducer(undefined, testAction);
    expect(results.serverMessage).toEqual(testAction.value);
    expect(results.notificationType).toEqual(Constants.NOTIFICATION.types.default);
  });
  test('should handle BAD_REQUEST', () => {
    const testAction = {
      type: types.BAD_REQUEST,
      value: 'Bad message',
    };
    const results = appReducer(undefined, testAction);
    expect(results.serverMessage).toEqual(testAction.value);
    expect(results.notificationType).toEqual(Constants.NOTIFICATION.types.error);
  });
  test('should handle APP_STOP_SHOWING_SERVER_MESSAGE', () => {
    const testAction = {
      type: types.APP_STOP_SHOWING_SERVER_MESSAGE,
    };
    const results = appReducer(undefined, testAction);
    expect(results.notificationType).toEqual('');
    expect(results.serverMessage).toEqual('');
  });
  test('should handle CONTEXT_MENU_CHANGE', () => {
    const testAction = {
      type: types.CONTEXT_MENU_CHANGE,
      contextMenuState: false,
    };
    const results = appReducer(undefined, testAction);
    expect(results.contextMenuState).toEqual(testAction.contextMenuState);
  });
  test('should handle MODAL_DIALOG_CHANGE', () => {
    const testAction = {
      type: types.MODAL_DIALOG_CHANGE,
      modalDialogState: {
        type: Constants.DIALOGS.SIMPLE_DIALOG,
        header: 'Apply Suggested Intents',
      },
    };
    const results = appReducer(undefined, testAction);
    expect(results.modalDialogState.type).toEqual(testAction.modalDialogState.type);
  });
  test('should handle APP_CONFIG_REQUEST', () => {
    const testAction = {
      type: types.APP_CONFIG_REQUEST,
    };
    const results = appReducer(undefined, testAction);
    expect(results).toEqual(defaultState);
  });
  test('should handle APP_CONFIG_SUCCESS', () => {
    const testAction = {
      type: types.APP_CONFIG_SUCCESS,
      data: {
        kibanaLogIndex: 'AWP65OUFE0CbGZa_3U_o',
        kibanaLogURL: 'https://stable-logview01.app.shared.int.sv2.247-inc.net/app/kibana',
      },
    };
    const results = appReducer(undefined, testAction);
    expect(results.kibanaLogURL).toEqual(testAction.data.kibanaLogURL);
  });
  test('should handle APP_CONFIG_FAILURE', () => {
    const testAction = {
      type: types.APP_CONFIG_FAILURE,
      data: {
        message: 'failure',
      },
    };
    const results = appReducer(undefined, testAction);
    expect(results.message).toEqual(testAction.data.message);
  });
});

describe('getUserDetails', () => {
  test('should return the userDetails object with internal user type', () => {
    const results = getUserDetails('e2VtYWlsPUFiaGlzaGVrLkdvc2hAMjQ3LmFpLCB0ZnNVc2VyVHlwZT1JTlRFUk5BTH0=');
    expect(results).toEqual({ email: 'Abhishek.Gosh@247.ai', tfsUserType: 'INTERNAL', userType: 'Internal' });
  });

  test('should return the userDetails object with external user type', () => {
    const results = getUserDetails('e2VtYWlsPUFiaGlzaGVrLkdvc2hAMjQ3LmFpLCB0ZnNVc2VyVHlwZT1FWFRFUk5BTH0=');
    expect(results).toEqual({ email: 'Abhishek.Gosh@247.ai', tfsUserType: 'EXTERNAL', userType: 'External' });
  });
  test('should return the userDetails object with external user type', () => {
    const results = getUserDetails();
    expect(results).toEqual({ '': undefined, userType: 'Internal' });
  });
});
