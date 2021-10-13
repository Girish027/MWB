import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { UtteranceTestContainer } from 'components/models/UtteranceTestContainer';
import * as actionsModels from 'state/actions/actions_models';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';

describe('<UtteranceTestContainer />', () => {
  const modelTestResults = {
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

  const props = {
    dispatch: () => {},
    modelTestResults,
    userId: 'abc@247.ai',
    csrfToken: '123',
    projectId: '3',
    modelId: '4',
    client: {
      id: '2',
    },
    testModelType: Constants.DIGITAL_MODEL,
    clearRunSingleUtterance: () => {},
    runSingleUtterance: '',
    model: {
      modelToken: '8eddf9c8-1aac-4aef-b6a0-8ea3ac36e451',
      modelType: Constants.DIGITAL_MODEL,
    },
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<UtteranceTestContainer
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for a digital model', () => {
      wrapper = shallow(<UtteranceTestContainer
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    const sampleTranscription = 'abc';

    beforeAll(() => {
      props.dispatch = jest.fn();
      props.clearRunSingleUtterance = jest.fn();
      props.submitTest = jest.fn();

      actionsModels.clearModelTestResults = jest.fn(() => 'called clearModelTestResults');
      actionsModels.testModel = jest.fn(() => 'called testModel');
      actionsApp.displayWarningRequestMessage = jest.fn(() => 'called displayWarningRequestMessage');
    });

    beforeEach(() => {
      wrapper = shallow(<UtteranceTestContainer
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentDidUpdate:', () => {
      test('should run single utterance test when provided from parent(case: from batch test)', () => {
        wrapper.instance().onRunSingleUtterance = jest.fn();
        wrapper.setProps({
          ...props,
          runSingleUtterance: 'how are you',
        });
        wrapper.update();
        expect(wrapper.instance().onRunSingleUtterance).toHaveBeenCalledWith('how are you');
      });

      test('should set testModelType if not set', () => {
        wrapper = shallow(<UtteranceTestContainer
          {...props}
          model={null}
        />);
        wrapper.instance().onModelTypeChange = jest.fn();
        wrapper.setState({
          testModelType: '',
        });
        // model is now loaded
        wrapper.setProps({
          ...props,
        });
        wrapper.update();
        expect(wrapper.instance().onModelTypeChange).toHaveBeenCalledWith(props.model.modelType);
      });
    });

    describe('componentDidMount:', () => {
      test('should dispatch action to clear test results', () => {
        wrapper.instance().componentDidMount();
        expect(actionsModels.clearModelTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('called clearModelTestResults');
      });
    });

    describe('onRunSingleUtterance:', () => {
      test('should update state with incoming utterance and set testModelType to DIGITAL', () => {
        wrapper.instance().onRunSingleUtterance(sampleTranscription);
        expect(wrapper.state().utterance).toEqual(sampleTranscription);
        expect(wrapper.state().testModelType).toEqual(Constants.DIGITAL_MODEL);
      });

      test('should update textElement value to be the incoming utterance', () => {
        const textElement = {
          value: '',
        };
        // mock the method to return mocked textElement
        document.querySelectorAll = jest.fn()
          .mockImplementationOnce(selector => [textElement]);

        wrapper.instance().onRunSingleUtterance(sampleTranscription);
        expect(document.querySelectorAll).toHaveBeenCalledWith('[name="utterance"]');
        expect(textElement.value).toEqual(sampleTranscription);
      });

      test('should clear single utterance prop', () => {
        wrapper.instance().onRunSingleUtterance(sampleTranscription);
        expect(props.clearRunSingleUtterance).toHaveBeenCalled;
      });

      test('should submit the incoming utterance for evaluation ', () => {
        wrapper.instance().submitTest = jest.fn();
        wrapper.update();
        wrapper.instance().onRunSingleUtterance(sampleTranscription);
        expect(wrapper.instance().submitTest).toHaveBeenCalled;
      });
    });

    describe('submitTest:', () => {
      test('should dispatch action to clear test results', () => {
        wrapper.instance().submitTest(sampleTranscription);
        expect(actionsModels.clearModelTestResults).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('called clearModelTestResults');
      });

      test('should dispatch action to display warning message, if model is not available', () => {
        wrapper = shallow(<UtteranceTestContainer
          {...props}
          model={null}
        />);
        wrapper.instance().submitTest(sampleTranscription);
        expect(actionsApp.displayWarningRequestMessage).toHaveBeenCalledWith(Constants.UNKNOWN_MODEL);
        expect(props.dispatch).toHaveBeenCalledWith('called displayWarningRequestMessage');
      });

      test('should not dispatch action to test utterance, if model is not available', () => {
        wrapper = shallow(<UtteranceTestContainer
          {...props}
          model={null}
        />);
        wrapper.instance().submitTest(sampleTranscription);
        expect(props.dispatch).not.toHaveBeenCalledWith('called testModel');
      });

      test('should dispatch action to test utterance', () => {
        const {
          model, client, projectId, userId, csrfToken,
        } = props;
        const expectedData = {
          projectId,
          modelId: model.modelToken,
          testModelType: Constants.DIGITAL_MODEL,
          clientId: client.id,
          utterances: [
            sampleTranscription,
          ],
        };
        wrapper.setState({
          testModelType: Constants.DIGITAL_MODEL,
        });
        wrapper.instance().submitTest(sampleTranscription);
        expect(actionsModels.testModel).toHaveBeenCalledWith(expectedData);
        expect(props.dispatch).toHaveBeenCalledWith('called testModel');
      });
    });

    describe('onModelTypeChange:', () => {
      test('should not update state if the new modeltype is same as existing', () => {
        wrapper.setState({
          testModelType: Constants.DIGITAL_MODEL,
        });
        wrapper.instance().setState = jest.fn();
        wrapper.update();
        wrapper.instance().onModelTypeChange(Constants.DIGITAL_MODEL);
        expect(wrapper.instance().setState).not.toHaveBeenCalled;
        expect(wrapper.state().testModelType).toEqual(Constants.DIGITAL_MODEL);
      });

      test('should update state if the new modeltype is different and clear test results', () => {
        wrapper.setState({
          testModelType: Constants.DIGITAL_SPEECH_MODEL,
        });
        wrapper.instance().onModelTypeChange(Constants.DIGITAL_MODEL);
        expect(wrapper.state().testModelType).toEqual(Constants.DIGITAL_MODEL);
        expect(props.dispatch).toHaveBeenCalledWith('called clearModelTestResults');
      });
    });
  });
});
