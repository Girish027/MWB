// import amplitude from 'amplitude-js/amplitude.nocompat';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import Constants from 'constants/Constants';

export const initializeAnalytics = (state = Constants.OBJECT_DEFAULT_VALUE) => {
  // const analytics = amplitude.getInstance(),
  const { userDetails = '', amplitudeApiKey } = state.app,
    { name = '' } = userDetails;
  // analytics.init(amplitudeApiKey, name);
};

export const logAmplitudeEvent = (eventName, state = Constants.OBJECT_DEFAULT_VALUE, eventSpecificData = Constants.OBJECT_DEFAULT_VALUE) => {
  /* amplitude.getInstance().logEvent(eventName, {
    ...getCommonData(state),
    ...eventSpecificData,
  }); */
};

// TODO Add user sessionId in the common list for amplitude events
export const getCommonData = (state) => {
  const { userDetails = Constants.OBJECT_DEFAULT_VALUE, environment } = state.app,
    { itsClientId = Constants.DEFAULT_VALUE, itsAppId = Constants.DEFAULT_VALUE } = state.header.client,
    { userType = Constants.DEFAULT_VALUE } = userDetails,
    obj = {
      toolId: AmplitudeConstants.TOOL_ID, userType, clientId: itsClientId, appId: itsAppId, environment,
    };
  return obj;
};
