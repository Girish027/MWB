import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from '@tfs/ui-components';
import * as appActions from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';
import Constants from 'constants/Constants';

class AudioPlayer extends Component {
  constructor(props) {
    super(props);

    this.onClick = this.onClick.bind(this);

    this.state = {
      isPlaying: false,
    };
  }

  onClick(event) {
    event.preventDefault();
    const {
      onStop,
      onPlay,
      audioStates,
      audioSrc,
      dispatch,
    } = this.props;

    const { beforePlaying, playing } = audioStates;

    const { isPlaying } = this.state;

    if (isPlaying) {
      this.setState({
        isPlaying: false,
      }, () => {
        if (audioSrc) {
          this.audioRef.pause();
          this.audioRef.currentTime = 0;
        }
        onStop(beforePlaying);
      });
    } else {
      this.setState({
        isPlaying: true,
      }, () => {
        // reset state on audio has completed playing
        if (audioSrc) {
          this.audioRef.onended = () => {
            this.setState({
              isPlaying: false,
            }, () => {
              onStop(beforePlaying);
            });
          };
          try {
            this.audioRef.play();
          } catch (e) {
            // happens when Error: NetworkError when attempting to fetch resource
            const { DISPLAY_MESSAGES } = getLanguage();
            dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.audioNotPlayable));
          }
        }
        onPlay(playing);
      });
    }
  }


  render() {
    const {
      playIcon, stopIcon, style, audioSrc, iconProps, styleOverride,
    } = this.props;
    const { playProps = {}, stopProps = {}, common = {} } = iconProps;
    const { isPlaying } = this.state;

    let VisualComponent = playIcon;
    let visualComponentProps = playProps;
    if (isPlaying) {
      VisualComponent = stopIcon;
      visualComponentProps = stopProps;
    }
    let {
      button = {
        ':focus': {
          outline: 'none',
        },
        ':hover': {
          color: '#003467',
          boxShadow: 'none',
        },
      },
    } = styleOverride;

    return (
      <Button
        type="flat"
        name="audioplayer-button"
        styleOverride={button}
        onClick={this.onClick}
      >
        <VisualComponent
          {...common}
          {...visualComponentProps}
        />
        <audio
          ref={ref => this.audioRef = ref}
          src={audioSrc}
          style={{ display: 'none' }}
          type={Constants.AUDIO_TYPE_WAV}
        >
          <track src="" kind="" srcLang="en" label="English" />
        </audio>
      </Button>
    );
  }
}

AudioPlayer.propTypes = {
  audioSrc: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.instanceOf(Blob),
    PropTypes.instanceOf(File),
  ]),
  playIcon: PropTypes.func.isRequired,
  stopIcon: PropTypes.func.isRequired,
  iconProps: PropTypes.object,
  onPlay: PropTypes.func,
  onStop: PropTypes.func,
  audioStates: PropTypes.object,
  styleOverride: PropTypes.object,
  dispatch: PropTypes.func,
};

AudioPlayer.defaultProps = {
  audioSrc: '',
  iconProps: {},
  onPlay: () => {},
  onStop: () => {},
  audioStates: {},
  styleOverride: {},
  dispatch: () => {},
};

export default AudioPlayer;
