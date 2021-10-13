import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import AudioPlayer from 'components/audio/AudioPlayer';
import Constants from 'constants/Constants';
import { Play, Stop } from '@tfs/ui-components';

describe('<AudioPlayer />', () => {
  const basicProps = {
    audioSrc: 'some link',
    playIcon: Play,
    stopIcon: Stop,
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
      playProps: {
        width: '20px',
      },
      stopProps: {
        fill: 'grey',
      },
    },
    onPlay: () => {},
    onStop: () => {},
    styleOverride: {
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
      wrapper = mount(<AudioPlayer
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for the basic props (audio is not playing)', () => {
      wrapper = mount(<AudioPlayer
        {...basicProps}
      />);
      expect(wrapper.state().isPlaying).toEqual(false);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for the enhanced props (audio is not playing)', () => {
      wrapper = mount(<AudioPlayer
        {...props}
      />);
      expect(wrapper.state().isPlaying).toEqual(false);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for basic props (audio is playing)', () => {
      wrapper = mount(<AudioPlayer
        {...basicProps}
      />);
      wrapper.setState({
        isPlaying: true,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for the enhanced props (audio is playing)', () => {
      wrapper = mount(<AudioPlayer
        {...props}
      />);
      wrapper.setState({
        isPlaying: true,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    let event;
    beforeAll(() => {
      props.onPlay = jest.fn();
      props.onStop = jest.fn();
      event = {
        preventDefault: jest.fn(),
      };
    });

    beforeEach(() => {
      wrapper = mount(<AudioPlayer
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClick:', () => {
      test('should prevent default behaviour of triggered event and update state', () => {
        wrapper.instance().onClick(event);
        expect(event.preventDefault).toHaveBeenCalled;
      });

      test('should stop playing audio if currently it is being played', () => {
        wrapper.setState({
          isPlaying: true,
        });
        wrapper.instance().onClick(event);
        expect(wrapper.state().isPlaying).toBe(false);
      });

      test('should indicate to parent that it has reset to beforePlaying', () => {
        wrapper.setState({
          isPlaying: true,
        });
        wrapper.instance().onClick(event);
        expect(props.onStop).toHaveBeenCalledWith(props.audioStates.beforePlaying);
      });

      test('should play audio if currently it is not being played', () => {
        wrapper.setState({
          isPlaying: false,
        });
        wrapper.instance().onClick(event);
        expect(wrapper.state().isPlaying).toBe(true);
      });

      test('should indicate to parent that is now playing', () => {
        wrapper.setState({
          isPlaying: false,
        });
        wrapper.instance().onClick(event);
        expect(props.onPlay).toHaveBeenCalledWith(props.audioStates.playing);
      });
    });
  });
});
