jest.mock('utils/amplitudeUtils');
jest.mock('utils/api');

import mockStore from 'state/configureStore';
import * as amplitudeUtils from 'utils/amplitudeUtils';
import api from 'utils/api';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import * as appConfigActions from 'state/actions/actions_appConfig';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_appConfig', () => {
  const data = { value: 'test' };
  let store;

  beforeAll(() => {
    store = reduxMockStore({});
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  describe('onAppConfigSuccess', () => {
    test('should dispatch appConfigSuccess action with data', () => {
      store.dispatch(appConfigActions.onAppConfigSuccess(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with LoginUser event', () => {
      mockStore.dispatch(appConfigActions.onAppConfigSuccess(data));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.LOGIN_EVENT, mockStore.getState());
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('onAppConfigFailure', () => {
    test('should dispatch appConfigRequest action', () => {
      store.dispatch(appConfigActions.onAppConfigFailure(data));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('fetchAppConfig', () => {
    test('should dispatch appConfigRequest action', () => {
      store.dispatch(appConfigActions.fetchAppConfig());
      expect(store.getActions()).toMatchSnapshot();
    });
    test('should make a GET call to get the config properties', () => {
      store.dispatch(appConfigActions.fetchAppConfig());
      expect(api.get).toHaveBeenCalledTimes(1);
      expect(api.get.mock.calls[0][0].url).toMatchSnapshot();
    });
  });
});
