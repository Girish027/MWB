import Constants from 'constants/Constants';

const amplitudeUtils = jest.genMockFromModule('utils/amplitudeUtils');

amplitudeUtils.initializeAnalytics = jest.fn((state = Constants.OBJECT_DEFAULT_VALUE) => 'called initializeAnalytics');

amplitudeUtils.logAmplitudeEvent = jest.fn((events, state = Constants.OBJECT_DEFAULT_VALUE, eventSpecificData = Constants.OBJECT_DEFAULT_VALUE) => 'called logAmplitudeEvent');

module.exports = amplitudeUtils;
