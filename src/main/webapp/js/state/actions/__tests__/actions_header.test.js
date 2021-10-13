jest.mock('utils/amplitudeUtils');
import mockStore from 'state/configureStore';
import * as amplitudeUtils from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { onClientChange } from 'state/actions/actions_header';

describe('actions_header', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('amplitude', () => {
    test('should call logAmplitudeEvent with SelectClientApp event', () => {
      mockStore.dispatch(onClientChange('Test Client'));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.SELECT_CLIENT_EVENT, mockStore.getState());
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });
});
