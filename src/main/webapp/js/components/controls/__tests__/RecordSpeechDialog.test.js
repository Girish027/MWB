import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';
import RecordSpeechDialog from 'components/controls/RecordSpeechDialog';

jest.useFakeTimers();

describe('<RecordSpeechDialog />', () => {
  let wrapper;

  const props = {
    header: 'Record',
  };

  describe('Creating an instance with no props', () => {
    beforeEach(() => {
      wrapper = shallow(<RecordSpeechDialog />);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<RecordSpeechDialog
        {...props}
      />);
      wrapper.setState({
        isRunTestDisable: true,
        isPlayButtonEnabled: false,
        seconds: 40,
        minutes: '00',
      });
      wrapper.update();
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Various state of the Record Speech Dialog', () => {
    beforeEach(() => {
      wrapper = shallow(<RecordSpeechDialog
        {...props}
      />);
    });

    test('renders correctly when user has not recorded audio', () => {
      wrapper.setState({
        ...wrapper.instance().initialState,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when user is recording audio', () => {
      wrapper.setState({
        audioState: wrapper.instance().audioStates.recording,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when user has stopped recording audio', () => {
      wrapper.instance().getFileName = jest.fn(() => 'recording_1557955235157.wav');
      wrapper.instance().onClickStopRecord('audio data');
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when user has playing the recorded audio', () => {
      wrapper.instance().getFileName = jest.fn(() => 'recording_1557955235157.wav');

      wrapper.instance().onClickStopRecord('audio data');
      wrapper.instance().setAudioState(wrapper.instance().audioStates.playing);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when user has stopped playing the recorded audio', () => {
      wrapper.instance().getFileName = jest.fn(() => 'recording_1557955235157.wav');
      wrapper.instance().onClickStopRecord('audio data');
      wrapper.instance().setAudioState(wrapper.instance().audioStates.playing);
      wrapper.instance().setAudioState(wrapper.instance().audioStates.beforePlaying);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });


  describe('Functionality', () => {
    beforeAll(() => {
      props.dispatch = jest.fn();
      props.onClickRunTest = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      actionsApp.displayWarningRequestMessage = jest.fn(() => 'called displayWarningRequestMessage');
    });

    beforeEach(() => {
      wrapper = shallow(<RecordSpeechDialog
        {...props}
      />);
    });

    describe('registerCallbacks', () => {
      test('should register the provided callback', () => {
        const callback = jest.fn();
        wrapper.instance().registerCallbacks({ callback });
        wrapper.instance().callbacks.callback();
        expect(callback).toHaveBeenCalled();
      });
    });

    describe('tick', () => {
      test('should decrement countdown seconds each time it is called', () => {
        wrapper.setState({
          seconds: 10,
        });
        wrapper.instance().tick();
        expect(wrapper.state().seconds).toEqual(9);
        wrapper.instance().tick();
        expect(wrapper.state().seconds).toEqual(8);
      });

      test('should clearInterval for the timer when countdown reaches zero', () => {
        wrapper.instance().timer = 20;
        wrapper.setState({
          seconds: 0,
        });
        wrapper.instance().tick();
        expect(clearInterval).toHaveBeenCalledWith(wrapper.instance().timer);
      });

      test('should call the stopMicrophone callback to stop currently active recording', () => {
        const stopMicrophone = jest.fn();
        wrapper.instance().registerCallbacks({ stopMicrophone });
        wrapper.setState({
          seconds: 0,
        });
        wrapper.instance().tick();
        expect(stopMicrophone).toHaveBeenCalled();
      });
    });

    test('should change the state when onClickStartRecord is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickStartRecord();
      expect(wrapperInstance.state.audioState).toBe(wrapper.instance().audioStates.recording);
    });

    test('should start the timer when onClickStartRecord is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickStartRecord();
      expect(setInterval).toHaveBeenLastCalledWith(wrapperInstance.tick, 1000);
    });

    test('should change the state when onClickStopRecord is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickStopRecord();
      expect(wrapperInstance.state.isPlayButtonEnabled).toEqual(true);
      expect(wrapperInstance.state.isRunTestDisable).toEqual(false);
      expect(wrapperInstance.state.seconds).toEqual(Constants.RECORDING_MAX_TIME_LIMIT_SEC);
      expect(wrapperInstance.state.audioState).toBe(wrapper.instance().audioStates.beforePlaying);
    });

    test('should rest the state when onClickRecordNew is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickRecordNew();
      expect(wrapperInstance.state).toEqual(wrapper.instance().initialState);
    });

    test('should decrement the counter', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.tick();
      expect(wrapperInstance.state.seconds).toBe(39);
    });

    test('should dispatch an action when onClickClose is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickClose();
      expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
    });
    test('should reset state when onClickClose is clicked', () => {
      const wrapperInstance = wrapper.instance();
      wrapperInstance.onClickClose();
      expect(wrapperInstance.state).toEqual(wrapperInstance.initialState);
    });

    //    Need to debug this case and resolved.
    //    test('should show Progress Dialog when onClickRunTest is clicked', () => {
    //      const { dispatch } = props;
    //
    //      const wrapperInstance = wrapper.instance();
    //      wrapper.setState({
    //        audioFile: 'some File',
    //      });
    //      wrapperInstance.onClickRunTest();
    //
    //      expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
    //        dispatch,
    //        type: Constants.DIALOGS.PROGRESS_DIALOG,
    //        message: Constants.TEST_IN_PROGRESS,
    //      });
    //    });

    test('should call onClickRunTest of parent when onClickRunTest is clicked', () => {
      const { dispatch } = props;

      const wrapperInstance = wrapper.instance();
      wrapper.setState({
        audioFile: 'some audio file',
      });
      wrapperInstance.onClickRunTest();
      expect(props.onClickRunTest).toHaveBeenCalledWith(wrapper.state().audioFile);
    });

    test('should show warning if audio file is missing', () => {
      const { dispatch } = props;

      const wrapperInstance = wrapper.instance();
      wrapper.setState({
        audioFile: '',
      });
      wrapperInstance.onClickRunTest();
      const { DISPLAY_MESSAGES } = getLanguage();

      expect(actionsApp.displayWarningRequestMessage).toHaveBeenCalledWith(DISPLAY_MESSAGES.audioFileNotAvailable);
      expect(props.dispatch).toHaveBeenCalledWith('called displayWarningRequestMessage');
    });

    describe('getFileName:', () => {
      test('should get the name of the file with current timestamp', () => {
        expect(wrapper.instance().getFileName().match(/recording_\d+\.wav/)).not.toEqual(null);
      });
    });
  });
});
