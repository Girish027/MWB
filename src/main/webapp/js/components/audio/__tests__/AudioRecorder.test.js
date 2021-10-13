import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import AudioRecorder from 'components/audio/AudioRecorder';
import Constants from 'constants/Constants';
import { StartRecordingIcon, StopRecordingIcon } from '@tfs/ui-components';
import * as appActions from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';

describe('<AudioRecorder />', () => {
  const basicProps = {
    audioSrc: 'some link',
    recordIcon: StartRecordingIcon,
    stopIcon: StopRecordingIcon,
    audioStates: {
      beforeRecording: 'beforeRecording',
      recording: 'recording',
      beforePlaying: 'beforePlaying',
      playing: 'playing',
    },
  };
  const props = {
    ...basicProps,
    iconProps: {
      common: {
        height: '20px',
      },
      recordProps: {
        width: '20px',
      },
      button: {
        height: '70px',
      },
    },
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<AudioRecorder
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for the basic props (audio is not being recorded)', () => {
      wrapper = shallow(<AudioRecorder
        {...basicProps}
      />);
      expect(wrapper.state().active).toEqual(false);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for the enhanced props (audio is not being recorded)', () => {
      wrapper = shallow(<AudioRecorder
        {...props}
      />);
      expect(wrapper.state().active).toEqual(false);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for when audio is being recorded', () => {
      wrapper = shallow(<AudioRecorder
        {...props}
      />);
      wrapper.setState({
        active: true,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for when permission is denied', () => {
      wrapper = shallow(<AudioRecorder
        {...props}
      />);
      wrapper.setState({
        disabled: true,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let event;
    let mockAudioStream;
    let mockMediaRecorder;
    let audioTracks;
    let audioTrackStop = jest.fn();
    let getUserMedia = jest.fn();

    beforeAll(() => {
      props.onRecord = jest.fn();
      props.onStop = jest.fn();
      props.dispatch = jest.fn();
      appActions.displayWarningRequestMessage = jest.fn(() => 'called displayWarningRequestMessage');
      event = {
        preventDefault: jest.fn(),
      };

      audioTracks = [{
        stop: audioTrackStop,
      }];
      mockAudioStream = {
        getAudioTracks: jest.fn(() => audioTracks),
      };

      global.window.navigator.mediaDevices = {
        getUserMedia,
      };

      mockMediaRecorder = {
        stop: jest.fn(),
        start: jest.fn(),
        addEventListener: jest.fn(),
      };
    });

    beforeEach(() => {
      wrapper = shallow(<AudioRecorder
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentWillUnmount:', () => {
      test('should stop microphone', () => {
        wrapper.instance().stopMicrophone = jest.fn();
        wrapper.instance().componentWillUnmount();
        expect(wrapper.instance().stopMicrophone).toHaveBeenCalled();
      });
    });

    describe('onClick:', () => {
      test('should prevent default behaviour of triggered event and update state', () => {
        wrapper.instance().getMicrophone = jest.fn();
        wrapper.instance().stopMicrophone = jest.fn();
        wrapper.instance().onClick(event);
        expect(event.preventDefault).toHaveBeenCalled();
      });

      test('should stop recording if currently active', () => {
        wrapper.setState({
          active: true,
        });
        wrapper.instance().stopMicrophone = jest.fn();
        wrapper.instance().onClick(event);
        expect(wrapper.instance().stopMicrophone).toHaveBeenCalled();
      });

      test('should get audiStream for recording if not active', () => {
        wrapper.setState({
          active: false,
        });
        wrapper.instance().getMicrophone = jest.fn();
        wrapper.instance().onClick(event);
        expect(wrapper.instance().getMicrophone).toHaveBeenCalled();
      });
    });

    describe('stopMicrophone:', () => {
      beforeEach(() => {
        wrapper.instance().mediaRecorder = mockMediaRecorder;
      });

      test('should not try to stop recording if audio is not available', () => {
        wrapper.setState({
          audio: null,
        });
        wrapper.instance().stopMicrophone();
        expect(wrapper.instance().mediaRecorder.stop).not.toHaveBeenCalled();
      });

      test('should stop audio tracks, media recorder on stop ', () => {
        wrapper.setState({
          audio: mockAudioStream,
        });
        wrapper.instance().stopMicrophone();
        expect(mockAudioStream.getAudioTracks).toHaveBeenCalled();
        expect(audioTrackStop).toHaveBeenCalled();
        expect(wrapper.instance().mediaRecorder.stop).toHaveBeenCalled();
      });

      test('should clear state on stop', () => {
        wrapper.setState({
          audio: mockAudioStream,
          active: true,
        });
        wrapper.instance().stopMicrophone();
        expect(wrapper.state().audio).toEqual(null);
        expect(wrapper.state().active).toEqual(false);
      });
    });
  });
});
