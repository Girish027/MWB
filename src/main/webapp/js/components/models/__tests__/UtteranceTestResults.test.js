import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import UtteranceTestResults from 'components/models/UtteranceTestResults';
import * as actionsModels from 'state/actions/actions_models';
import Constants from 'constants/Constants';

describe('<UtteranceTestResults />', () => {
  const digitalModelTestResults = {
    projectId: '661',
    modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
    type: 'UTTERANCES',
    status: 'SUCCESS',
    evaluations: [{
      utterance: 'i want to cancel for june 13',
      intents: [{
        intent: 'reservation-query',
        score: 0.49544276870123316,
      }, {
        intent: 'agent-query',
        score: 0.42004871402243316,
      }, {
        intent: 'None_None',
        score: 0.14285217997731586,
      }],
      transformations: [{
        id: 'rx-mapper.non-breaking-space-regex',
        result: 'i want to cancel for june 13',
      }, {
        id: 'wsp',
        result: 'i want to cancel for june 13',
      }],
      entities: [{
        name: 'date',
        value: 'june 13',
      }],
    }],
  };

  const speechModelTestResults = {
    projectId: '661',
    modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
    type: 'UTTERANCES',
    status: 'SUCCESS',
    evaluations: [{
      recognitionScore: '0.9312356',
      utterance: 'i want to cancel for june 13',
      utteranceWithWordClass: 'i want to cancel for speech_class_date',
      intents: [{
        intent: 'speech-intent',
        score: 0.49544276870123316,
      }, {
        intent: 'speech-intent2',
        score: 0.42004871402243316,
      }],
    }],
  };

  const propsDigital = {
    digitalModelTestResults,
    testModelType: Constants.DIGITAL_MODEL,
  };

  const propsSpeech = {
    speechModelTestResults,
    digitalModelTestResults,
    testModelType: Constants.DIGITAL_SPEECH_MODEL,
    fileType: Constants.FILE_TYPE.LINK,
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsDigital}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for a digital model when modelTestResults are present', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsDigital}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a speech model when modelTestResults are present', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a digital model when modelTestResults are absent', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsDigital}
        digitalModelTestResults={{}}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a speech model when modelTestResults are absent', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
        digitalModelTestResults={{}}
        speechModelTestResults={{}}
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly modelTestResults are does not have entities', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
        digitalModelTestResults={{
          ...digitalModelTestResults,
          evaluations: [{
            utterance: 'i want to cancel for june 13',
            intents: [{
              intent: 'reservation-query',
              score: 0.49544276870123316,
            }],
            transformations: [{
              id: 'rx-mapper.non-breaking-space-regex',
              result: 'i want to cancel for june 13',
            }],
            entities: [],
          }],
        }
        }
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly modelTestResults are does not have transformations', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
        digitalModelTestResults={{
          ...digitalModelTestResults,
          evaluations: [{
            utterance: 'i want to cancel for june 13',
            intents: [{
              intent: 'reservation-query',
              score: 0.49544276870123316,
            }],
            transformations: [],
            entities: [{
              name: 'date',
              value: 'june 13',
            }],
          }],
        }
        }
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly modelTestResults are does not have intents', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
        digitalModelTestResults={{
          ...digitalModelTestResults,
          evaluations: [{
            utterance: 'i want to cancel for june 13',
            intents: [],
            transformations: [{
              id: 'rx-mapper.non-breaking-space-regex',
              result: 'i want to cancel for june 13',
            }],
            entities: [{
              name: 'date',
              value: 'june 13',
            }],
          }],
        }
        }
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when a tab currently active', () => {
      wrapper = shallow(<UtteranceTestResults
        {...propsSpeech}
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        activeTab: Constants.TEST_RESULTS_TABS.entitiesTab,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('getFileName:', () => {
      test('should get the name of the file with current timestamp', () => {
        expect(wrapper.instance().getFileName().match(/audio_\d+\.wav/)).not.toEqual(null);
      });
    });

    describe('onTabSelected:', () => {
      test('should set the state to match the new selected index', () => {
        wrapper.setState({
          selectedTabIndex: 1,
        });
        wrapper.instance().onTabSelected('Trasnformations Tab', 0);
        expect(wrapper.state().selectedTabIndex).toEqual(0);
      });
    });
  });
});
