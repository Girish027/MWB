import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { UtteranceInputBar } from 'components/models/UtteranceInputBar';
import * as actionsModels from 'state/actions/actions_models';
import * as actionsApp from 'state/actions/actions_app';
import * as modelUtils from 'components/models/modelUtils';
import Constants from 'constants/Constants';

describe('<UtteranceInputBar />', () => {
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
    dispatch: () => {},
    testModelType: Constants.DIGITAL_MODEL,
    onModelTypeChange: () => {},
    submitTest: () => {},
    model: {
      modelType: Constants.DIGITAL_MODEL,
    },
    client: {
      id: '12',
    },
  };

  const propsSpeech = {
    ...propsDigital,
    testModelType: Constants.DIGITAL_SPEECH_MODEL,
    speechModelTestResults,
    model: {
      modelType: Constants.DIGITAL_SPEECH_MODEL,
    },
    handleAudio: () => {},
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsDigital}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for a digital model', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsDigital}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders Run Test button when utterance result is provided', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsDigital}
      />);
      wrapper.setState({
        utterance: 'how are you',
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a Speech model with Speech as test model type, with fileType as link', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsSpeech}
        testModelType={Constants.DIGITAL_SPEECH_MODEL}
      />);
      wrapper.instance().getFileName = jest.fn(() => 'audio_1557955235157.wav');
      wrapper.setState({
        fileType: Constants.FILE_TYPE.LINK,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders just the Action elements when modelTestResults are not present', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsSpeech}
        speechModelTestResults={{}}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a Speech model with Digital as test model type', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsSpeech}
        testModelType={Constants.DIGITAL_MODEL}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for a Speech model with Digital as test model type and user entered utterance', () => {
      wrapper = shallow(<UtteranceInputBar
        {...propsSpeech}
        testModelType={Constants.DIGITAL_MODEL}
      />);
      wrapper.setState({
        utterance: 'how are you',
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let dispatch;

    beforeAll(() => {
      propsSpeech.dispatch = jest.fn();
      propsSpeech.onModelTypeChange = jest.fn();
      propsSpeech.submitTest = jest.fn();
      propsSpeech.handleAudio = jest.fn();

      actionsModels.clearModelTestResults = jest.fn(() => 'called clearModelTestResults');
      actionsModels.testSpeechModel = jest.fn(() => 'called testSpeechModel');
      actionsModels.audioUploadSuccess = jest.fn(() => 'called audioUploadSuccess');
      actionsModels.audioUpload = jest.fn(() => 'called audioUpload');
      actionsModels.audioUploadFail = jest.fn(() => 'called audioUploadFail');
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      modelUtils.base64ToBlob = jest.fn(() => 'called base64ToBlob');

      dispatch = propsSpeech.dispatch;
    });

    beforeEach(() => {
      wrapper = shallow(<UtteranceInputBar
        {...propsSpeech}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('React Upload Event Handlers:', () => {
      test('beforeUpload: should dispatch action to indicate audio upload began', () => {
        wrapper.instance().uploaderProps.beforeUpload();
        expect(actionsModels.audioUpload).toHaveBeenCalled();
        expect(propsSpeech.dispatch).toHaveBeenCalledWith('called audioUpload');
      });

      test('customRequest: should call the onClickUpload handler', () => {
        wrapper.instance().onClickUpload = jest.fn();
        expect(() => wrapper.instance().uploaderProps.customRequest({ file: new Blob() })).not.toThrow();
      });

      test('onStart: should dispatch action to show the Progress dialog', () => {
        wrapper.instance().uploaderProps.onStart();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
          dispatch,
          type: Constants.DIALOGS.PROGRESS_DIALOG,
          message: Constants.TEST_IN_PROGRESS,
        });
        expect(propsSpeech.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });

      test('onError: should dispatch action to indicate audio upload failed', () => {
        wrapper.instance().uploaderProps.onError();
        expect(actionsModels.audioUploadFail).toHaveBeenCalled();
        expect(propsSpeech.dispatch).toHaveBeenCalledWith('called audioUploadFail');
      });
    });


    describe('getDerivedStateFromProps:', () => {
      let props;
      let state;

      beforeAll(() => {
        props = {
          ...propsSpeech,
          speechModelTestResults: {
            evaluations: [{
              utteranceFileData: 'file data in base 64',
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
          },
        };
        state = {
          fileType: Constants.FILE_TYPE.LINK,
        };
      });

      test('should return null when utterance file data is not present in model test results', () => {
        const updateState = UtteranceInputBar.getDerivedStateFromProps(propsSpeech, state);
        expect(updateState).toEqual(null);
      });

      test('should return null when utterance file data is present in model test results for fileType not LINK', () => {
        const updateState = UtteranceInputBar.getDerivedStateFromProps(props, { fileType: 'upload' });
        expect(updateState).toEqual(null);
      });

      test('should convert the fileData into blob', () => {
        const updateState = UtteranceInputBar.getDerivedStateFromProps(props, state);
        expect(modelUtils.base64ToBlob)
          .toHaveBeenCalledWith(
            props.speechModelTestResults.evaluations[0].utteranceFileData,
            Constants.AUDIO_TYPE_WAV,
          );
      });

      test('should return filedata when utterance file data is present in model test results for fileType LINK', () => {
        const updateState = UtteranceInputBar.getDerivedStateFromProps(props, state);
        expect(updateState).toEqual({ audioSrc: URL.createObjectURL('file') });
      });
    });

    describe('componentDidMount:', () => {
      test('should dispatch action to clear test results', () => {
        wrapper.instance().componentDidMount();
        expect(actionsModels.clearModelTestResults).toHaveBeenCalled();
        expect(propsSpeech.dispatch).toHaveBeenCalledWith('called clearModelTestResults');
      });
    });

    describe('onChange:', () => {
      test('should prevent default behaviour of triggered event and update state', () => {
        const event = {
          preventDefault: jest.fn(),
          target: {
            value: 'how are you',
          },
        };
        wrapper.instance().onChange(event);
        expect(event.preventDefault).toHaveBeenCalled();
        expect(wrapper.state().utterance).toEqual(event.target.value);
      });
    });

    describe('onKeyPress:', () => {
      test('should prevent default behaviour of triggered event for Enter key and call click handler', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'Enter',
        };
        wrapper.instance().onClickRunTest = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).toHaveBeenCalled();
        expect(wrapper.instance().onClickRunTest).toHaveBeenCalled();
      });

      test('should prevent default behaviour of triggered event for Enter key and call click handler', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'Tab',

        };
        wrapper.instance().onClickRunTest = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).toHaveBeenCalled;
        expect(wrapper.instance().onClickRunTest).toHaveBeenCalled();
      });

      test('should not handle event if any other key is pressed', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'A',

        };
        wrapper.instance().onClickRunTest = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).not.toHaveBeenCalled();
        expect(wrapper.instance().onClickRunTest).not.toHaveBeenCalled();
      });
    });

    describe('onClickRunTest:', () => {
      test('should submit the transcription for digital model testing', () => {
        wrapper.setState({
          utterance: 'how are you',
        });
        wrapper.instance().onClickRunTest();
        expect(propsSpeech.submitTest).toHaveBeenCalled();
      });
    });

    describe('onClickRunSpeechTest:', () => {
      test('should submit the audio for speech model testing', () => {
        const runTestForAudio = {
          audioFile: 'some file',
          audioURL: undefined,
          fileType: Constants.FILE_TYPE.UPLOADED,
        };
        const data = {
          clientId: propsSpeech.client.id,
          projectId: propsSpeech.projectId,
          modelId: propsSpeech.model.modelToken,
          model: propsSpeech.model,
          ...runTestForAudio,
        };
        wrapper.instance().onClickRunSpeechTest(runTestForAudio);
        expect(actionsModels.testSpeechModel).toHaveBeenCalledWith(data);
      });
    });

    describe('onClickUpload', () => {
      let readAsBinaryString;
      const input = { file: new Blob() };
      const audioSrc = URL.createObjectURL(input.file);

      beforeEach(() => {
        readAsBinaryString = jest.fn();
        global.FileReader = jest.fn().mockImplementation(() => ({ readAsBinaryString }));
        wrapper.instance().onClickRunSpeechTest = jest.fn();
      });

      afterAll(() => {
        global.FileReader.mockRestore();
      });

      test('should read uploaded file as binary string', () => {
        wrapper.instance().onClickUpload(input);
        expect(readAsBinaryString).toHaveBeenCalledWith(input.file);
      });
    });

    describe('onClickRecord', () => {
      test('should call the record speech Dialog', () => {
        wrapper.instance().onClickRecord();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
          header: 'Record and Test',
          dispatch,
          type: Constants.DIALOGS.RECORD_SPEECH,
          onClickRunTest: expect.any(Function),
        });
      });

      test('onClickRunTest handler should update state with audioFile', () => {
        const audioFile = 'some file';
        const audioSrc = URL.createObjectURL(audioFile);

        wrapper.instance().onClickRunSpeechTest = jest.fn();
        wrapper.instance().onClickRecord();

        const { onClickRunTest } = actionsApp.modalDialogChange.mock.calls[0][0];
        onClickRunTest(audioFile);
        expect(wrapper.state().audioSrc).toEqual(audioSrc);
        expect(wrapper.state().fileType).toEqual(Constants.FILE_TYPE.RECORDING);
      });

      test('onClickRunTest handler should call the run speech test handler with audioFile and fileType', () => {
        const audioFile = 'some file';
        const audioSrc = URL.createObjectURL(audioFile);

        wrapper.instance().onClickRunSpeechTest = jest.fn();
        wrapper.instance().onClickRecord();

        const { onClickRunTest } = actionsApp.modalDialogChange.mock.calls[0][0];
        onClickRunTest(audioFile);
        expect(wrapper.instance().onClickRunSpeechTest).toHaveBeenCalledWith({ audioFile, fileType: Constants.FILE_TYPE.RECORDING });
      });
    });

    describe('onClickLink', () => {
      test('should call the record speech Dialog', () => {
        wrapper.instance().onClickLink();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
          header: 'Link audio file URL to test',
          dispatch,
          type: Constants.DIALOGS.LINK_DIALOG,
          onClickRunTest: expect.any(Function),
        });
      });

      test('onClickRunTest handler should update state with audioURL', () => {
        const audioURL = 'http://..abc.wav';

        wrapper.instance().onClickRunSpeechTest = jest.fn();
        wrapper.instance().onClickLink();

        const { onClickRunTest } = actionsApp.modalDialogChange.mock.calls[0][0];
        onClickRunTest(audioURL);
        expect(wrapper.state().audioSrc).toEqual(audioURL);
        expect(wrapper.state().fileType).toEqual(Constants.FILE_TYPE.LINK);
      });

      test('onClickRunTest handler should call the run speech test handler with audioURL and fileType', () => {
        const audioURL = 'http://..abc.wav';
        wrapper.instance().onClickRunSpeechTest = jest.fn();
        wrapper.instance().onClickLink();

        const { onClickRunTest } = actionsApp.modalDialogChange.mock.calls[0][0];
        onClickRunTest(audioURL);
        expect(wrapper.instance().onClickRunSpeechTest).toHaveBeenCalledWith({ audioURL, fileType: Constants.FILE_TYPE.LINK });
      });
    });

    describe('onModelTypeChange:', () => {
      test('should not reset state if the new modeltype is same existing', () => {
        const existingState = {
          utterance: 'how are you',
          audioSrc: 'some link',
          fileType: '',
          selectedRadioGroupValue: 'DIGITAL_SPEECH',
        };
        wrapper.setState(existingState);
        wrapper.instance().onModelTypeChange(Constants.DIGITAL_SPEECH_MODEL);
        expect(wrapper.state()).toEqual(existingState);
      });

      test('should reset state if the new modeltype is different', () => {
        const existingState = {
          utterance: 'how are you',
          audioSrc: 'some link',
          fileType: '',
        };
        wrapper.setState(existingState);
        wrapper.instance().onModelTypeChange(Constants.DIGITAL_MODEL);
        expect(wrapper.state()).toEqual({
          utterance: '',
          audioSrc: '',
          fileType: '',
          selectedRadioGroupValue: 'DIGITAL',
        });
      });
    });
  });
});
