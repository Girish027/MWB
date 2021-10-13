/* jest.mock('amplitude-js/amplitude.nocompat');
import amplitude from 'amplitude-js/amplitude.nocompat';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { initializeAnalytics, logAmplitudeEvent, getCommonData } from 'utils/amplitudeUtils';

describe('amplitude', () => {
  const MOCK_STATE = {
      header: {
        client: {
          id: '2',
          itsAppId: 'default',
          itsClientId: '247ai',
          name: '247 ai',
        },
      },
      app: {
        amplitudeApiKey: 'hd383d3893',
        environment: 'test',
        userDetails: {
          userType: 'Internal',
          name: 'PICK ANY',
        },
      },
    },
    eventSpecificData = {
      projectId: '453', modelDBId: '12', modelId: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
    };
  describe('amplitudeUtils', () => {
    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('initializeAnalytics', () => {
      test('should getInstance amplitude called', () => {
        initializeAnalytics(MOCK_STATE);
        expect(amplitude.getInstance).toHaveBeenCalledTimes(1);
      });

      test('should init amplitude called', () => {
        initializeAnalytics(MOCK_STATE);
        expect(amplitude.getInstance().init).toHaveBeenCalledTimes(1);
        expect(amplitude.getInstance().init).toHaveBeenCalledWith(MOCK_STATE.app.amplitudeApiKey, MOCK_STATE.app.userDetails.name);
        expect(amplitude.getInstance().init).toHaveReturnedWith('called amplitude init');
      });
    });

    describe('logAmplitudeEvent', () => {
      test('should logEvent called', () => {
        logAmplitudeEvent(AmplitudeConstants.LOGIN_EVENT, MOCK_STATE);
        expect(amplitude.getInstance().logEvent).toHaveBeenCalledTimes(1);
        expect(amplitude.getInstance().logEvent).toHaveReturnedWith('called amplitude logEvent');
      });

      Object.keys(AmplitudeConstants).forEach((event) => {
        test(`should renders correctly for each events - ${event}`, () => {
          logAmplitudeEvent(event, MOCK_STATE, eventSpecificData);
          expect(amplitude.getInstance().logEvent).toHaveBeenCalledWith(event, {
            appId: 'default', clientId: '247ai', environment: 'test', modelDBId: '12', modelId: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451', projectId: '453', toolId: 'MWB', userType: 'Internal',
          });
          expect(amplitude.getInstance().logEvent).toHaveReturnedWith('called amplitude logEvent');
        });
      });
    });

    describe('snapshots', () => {
      test('snapshot getCommonData Object', () => {
        const commonObj = getCommonData(MOCK_STATE);
        expect(commonObj).toMatchSnapshot();
      });
    });
  });
});
*/
