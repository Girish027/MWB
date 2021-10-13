import base64 from 'base-64';
import * as types from 'state/actions/types';
import Constants from 'constants/Constants';

export const defaultState = {
  name: 'Modeling Workbench',
  version: '',
  userId: '',
  userName: '',
  userDetails: '',

  // related to notification
  serverMessage: '',
  notificationType: '',

  csrfToken: null,
  loggedIn: false,
  projectId: null, // after a browser refresh, update projectId & datsetId with the params
  datasetId: null,
  projectTitle: null,
  contextMenuState: null,
  modalDialogState: null,
  featureFlags: {
    // eslint-disable-next-line no-undef
    ...uiConfig.featureFlags,
  },
  userFeatureConfiguration: {},
  // eslint-disable-next-line no-undef
  processENV,
  userGroups: [],
  itsURL: '',
  ufpURL: '',
  oAuthLogoutURL: '',
  environment: '',
  amplitudeApiKey: '',
  logoutWarningTimeout: 3480000,
  logoutTimeout: 120000,
  speechTestTimeout: 5000,
  userAccountLink: '',
  internalSupportLink: '',
  externalSupportLink: '',
  documentationLink: '',
};

export const getUserDetails = (userDetails = Constants.DEFAULT_VALUE) => {
  const _userDetails = base64.decode(userDetails);
  const userDetailsArray = _userDetails.substring(1, _userDetails.length - 1).split(Constants.COMMA_WHITESPACE_SEPARATOR),
    userDetailsObj = {};
  userDetailsArray.forEach((item) => {
    const keyVal = item.split(Constants.EQUALS_SEPARATOR);
    userDetailsObj[keyVal[0]] = keyVal[1];
  });

  let userType = Constants.USER_TYPE_INTERNAL;
  if (userDetailsObj.tfsUserType
    && userDetailsObj.tfsUserType === Constants.USER_TYPE_EXTERNAL.toUpperCase()) {
    userType = Constants.USER_TYPE_EXTERNAL;
  }

  userDetailsObj.userType = userType;
  return userDetailsObj;
};

function appReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.LOGIN_INFO: {
    const {
      userId, userName, userDetails,
    } = action.value;
    return Object.assign({}, state, {
      loggedIn: true,
      userId,
      userName,
      userDetails: getUserDetails(userDetails),
    });
  }
  case types.LOGIN_SUCCESS:
    return Object.assign({}, state, {
      csrfToken: action.value,
    });

  case types.UPDATE_USER_GROUPS: {
    const keys = Object.keys(action.data);
    const userGroups = [];
    let userFeatureConfiguration = state.featureFlags.DEFAULT;

    keys.forEach((key) => {
      userGroups.push(key);
      userFeatureConfiguration = Object.assign(userFeatureConfiguration, state.featureFlags[key]);
    });

    return Object.assign({}, state, {
      userGroups,
      userFeatureConfiguration,
    });
  }
  case types.GOOD_REQUEST:
    return Object.assign({}, state, {
      serverMessage: action.value,
      notificationType: Constants.NOTIFICATION.types.success,
    });
  case types.WARNING_REQUEST:
    return Object.assign({}, state, {
      serverMessage: action.value,
      notificationType: Constants.NOTIFICATION.types.error,
    });
  case types.PERSISTENT_REQUEST:
    return Object.assign({}, state, {
      serverMessage: action.value,
      notificationType: Constants.NOTIFICATION.types.default,
    });
  case types.BAD_REQUEST:
    return Object.assign({}, state, {
      serverMessage: action.value,
      notificationType: Constants.NOTIFICATION.types.error,
    });
  case types.APP_STOP_SHOWING_SERVER_MESSAGE:
    return Object.assign({}, state, {
      serverMessage: '',
      notificationType: '',
    });
  case types.CONTEXT_MENU_CHANGE:
    return Object.assign({}, state, {
      contextMenuState: action.contextMenuState,
    });
  case types.MODAL_DIALOG_CHANGE:
    return Object.assign({}, state, {
      modalDialogState: action.modalDialogState,
    });

  case types.APP_CONFIG_REQUEST:
    return state;
  case types.APP_CONFIG_SUCCESS:
    return Object.assign({}, state, {
      ...action.data,
    });
  case types.APP_CONFIG_FAILURE:
    // ToDo - store error
    return Object.assign({}, state, {
      ...action.data,
    });
  default:
    return state;
  }
}

export { appReducer };
