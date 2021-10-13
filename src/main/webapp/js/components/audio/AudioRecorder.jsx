import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from '@tfs/ui-components';
import Constants from 'constants/Constants';
import * as appActions from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';

const { DISPLAY_MESSAGES } = getLanguage();

class AudioRecorder extends Component {
  constructor(props) {
    super(props);

    this.onClick = this.onClick.bind(this);
    this.getMicrophone = this.getMicrophone.bind(this);
    this.stopMicrophone = this.stopMicrophone.bind(this);
    this.createMediaRecorder = this.createMediaRecorder.bind(this);

    this.mediaRecorder = null;
    this.chunks = [];

    const { registerCallbacks } = this.props;
    this.state = {
      disabled: false,
      active: false,
      audio: null,
    };

    registerCallbacks({
      stopMicrophone: this.stopMicrophone,
    });
  }

  componentWillUnmount() {
    this.stopMicrophone();
  }

  createMediaRecorder() {
    const {
      onStop,
      onRecord,
    } = this.props;

    const { audio } = this.state;

    this.mediaRecorder = new MediaRecorder(audio);

    this.mediaRecorder.addEventListener('start', (e) => {
      onRecord();
    });

    this.mediaRecorder.addEventListener('stop', (e) => {
      const recordedData = new Blob(this.chunks, { type: Constants.AUDIO_TYPE_WAV });
      this.chunks = [];
      onStop(recordedData);
    });

    this.mediaRecorder.addEventListener('dataavailable', (e) => {
      this.chunks.push(e.data);
    });
  }

  getMicrophone() {
    const {
      dispatch,
    } = this.props;

    navigator.mediaDevices.getUserMedia({
      audio: true,
      video: false,
    }).then((audio) => {
      this.setState({
        audio,
        active: true,
      }, () => {
        try {
          this.createMediaRecorder();
          this.mediaRecorder.start();
        } catch (e) {
          dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.audioRecordFail));
        }
      });
    }).catch((error) => {
      if (error.name === 'NotAllowedError') {
        dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.micPermissionDenied));
        this.setState({
          disabled: true,
        });
      } else {
        dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.audioRecordFail));
      }
    });
  }

  stopMicrophone() {
    const { audio } = this.state;
    if (audio) {
      audio.getAudioTracks().forEach(track => track.stop());
      this.mediaRecorder.stop();
      this.setState({
        audio: null,
        active: false,
      });
    }
  }

  onClick(event) {
    event.preventDefault();
    const { active } = this.state;
    if (active) {
      this.stopMicrophone();
    } else {
      this.getMicrophone();
    }
  }

  render() {
    const {
      recordIcon, stopIcon, iconProps, styleOverride, children,
    } = this.props;

    const { active, disabled } = this.state;
    const { recordProps = {}, stopProps = {} } = iconProps;

    let VisualComponent = recordIcon;
    let visualComponentProps = recordProps;

    if (active) {
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
    if (disabled) {
      visualComponentProps = Object.assign({}, visualComponentProps, { fill: '#bdbdbd' });
    }
    return (
      <Button
        type="flat"
        name="AudioRecorder-button"
        styleOverride={button}
        onClick={this.onClick}
      >
        <VisualComponent
          {...visualComponentProps}
        />
        {children}
      </Button>
    );
  }
}

AudioRecorder.propTypes = {
  children: PropTypes.node,
  recordIcon: PropTypes.func.isRequired,
  stopIcon: PropTypes.func.isRequired,
  iconProps: PropTypes.object,
  onRecord: PropTypes.func,
  onStop: PropTypes.func,
  styleOverride: PropTypes.object,
  dispatch: PropTypes.func,
  registerCallbacks: PropTypes.func,
};

AudioRecorder.defaultProps = {
  children: '',
  audioSrc: '',
  iconProps: {},
  onRecord: () => {},
  onStop: () => {},
  dispatch: () => {},
  styleOverride: {},
  registerCallbacks: () => {},
};

export default AudioRecorder;
