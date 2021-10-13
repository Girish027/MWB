import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  StartRecordingIcon,
  StopRecordingIcon,
  PlayRecordingIcon,
  PrimaryDivider,
} from '@tfs/ui-components';
import * as appActions from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import AudioPlayer from 'components/audio/AudioPlayer';
import AudioRecorder from 'components/audio/AudioRecorder';
import { getLanguage } from 'state/constants/getLanguage';
import Downloader from 'components/controls/Downloader';

const { DISPLAY_MESSAGES } = getLanguage();

export default class RecordSpeechDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.audioStates = {
      beforeRecording: 'beforeRecording',
      recording: 'recording',
      beforePlaying: 'beforePlaying',
      playing: 'playing',
    };

    this.subtileMap = {
      beforeRecording: 'START',
      recording: 'STOP',
      beforePlaying: 'PLAY',
      playing: 'STOP',
    };

    this.initialState = {
      isRunTestDisable: true,
      isPlayButtonEnabled: false,
      seconds: Constants.RECORDING_MAX_TIME_LIMIT_SEC,
      minutes: Constants.RECORDING_MAX_TIME_LIMIT_MIN,
      audioFile: '',
      audioSrc: '',
      audioState: this.audioStates.beforeRecording,
    };

    this.state = {
      ...this.initialState,
    };

    this.registerCallbacks = this.registerCallbacks.bind(this);
    this.onClickClose = this.onClickClose.bind(this);
    this.onClickStartRecord = this.onClickStartRecord.bind(this);
    this.onClickStopRecord = this.onClickStopRecord.bind(this);
    this.onClickRecordNew = this.onClickRecordNew.bind(this);
    this.onClickRunTest = this.onClickRunTest.bind(this);
    this.setAudioState = this.setAudioState.bind(this);
    this.renderChildren = this.renderChildren.bind(this);
    this.tick = this.tick.bind(this);

    this.timer = null;
    this.callbacks = {};
    this.styleOverride = {
      container: {
        width: '500px',
        height: '340px',
        display: 'grid',
        gridTemplateRows: '60px auto 60px',
      },
      childContainer: {
        marginTop: '10px',
        marginBottom: '10px',
      },
      cancel: {
        backgroundColor: 'unset',
        paddingLeft: '10px',
        paddingRight: '10px',
      },
      ok: {
        marginLeft: '10px',
        paddingLeft: '25px',
        paddingRight: '25px',
      },
    };
  }

  registerCallbacks(callbackItem) {
    this.callbacks = {
      ...this.callbacks,
      ...callbackItem,
    };
  }

  tick() {
    let { seconds } = this.state;
    if (seconds === 0) {
      clearInterval(this.timer);
      const {
        stopMicrophone = Constants.noop,
      } = this.callbacks;
      stopMicrophone();
    } else {
      this.setState({
        seconds: --seconds,
      });
    }
  }

  onClickClose() {
    const { dispatch } = this.props;
    // reset state
    this.setState(this.initialState);
    dispatch(appActions.modalDialogChange(null));
  }

  onClickStartRecord() {
    this.timer = setInterval(this.tick, 1000);
    this.setState({
      audioState: this.audioStates.recording,
    });
  }

  onClickStopRecord(audioFile) {
    clearInterval(this.timer);
    this.setState({
      isPlayButtonEnabled: true,
      isRunTestDisable: false,
      seconds: Constants.RECORDING_MAX_TIME_LIMIT_SEC,
      audioState: this.audioStates.beforePlaying,
      audioFile,
      audioSrc: URL.createObjectURL(audioFile),
    });
  }

  onClickRecordNew() {
    // reset state
    this.setState(this.initialState);
  }

  onClickRunTest() {
    const { dispatch, onClickRunTest } = this.props;
    const { audioFile } = this.state;
    if (audioFile) {
      //       dispatch(appActions.modalDialogChange({
      //         dispatch,
      //         type: Constants.DIALOGS.PROGRESS_DIALOG,
      //         message: Constants.TEST_IN_PROGRESS,
      //       }));
      dispatch(appActions.modalDialogChange(null));
      onClickRunTest(audioFile);
    } else {
      dispatch(appActions.displayWarningRequestMessage(DISPLAY_MESSAGES.audioFileNotAvailable));
    }
  }

  setAudioState(audioState) {
    this.setState({
      audioState,
    });
  }

  getFileName() {
    return `recording_${Date.now()}.wav`;
  }

  getActionSubtitle(addClass = 'blueText') {
    const { audioState } = this.state;
    let download = '';
    switch (audioState) {
    case this.audioStates.recording:
      addClass = 'blueText';
      break;
    case this.audioStates.beforePlaying:
    case this.audioStates.playing: {
      const { dispatch } = this.props;
      const { audioSrc } = this.state;
      download = (
        <span
          style={{
            paddingLeft: '10px',
          }}
        >
          <PrimaryDivider fill="#004C97" />
          <Downloader
            file={audioSrc}
            fileName={this.getFileName()}
            fileType={Constants.FILE_TYPE.RECORDING}
            dispatch={dispatch}
          >
            DOWNLOAD
          </Downloader>
        </span>
      );
    }
      break;
    default:
      break;
    }
    return (
      <span className={`speech-recording-span ${addClass}`}>
        {this.subtileMap[audioState]}
        {download}
      </span>
    );
  }

  renderChildren() {
    const { dispatch } = this.props;
    const {
      minutes,
      seconds,
      audioState,
      audioSrc,
    } = this.state;

    switch (audioState) {
    case this.audioStates.beforeRecording:
    case this.audioStates.recording:
      return (
        <span>
          {audioState === this.audioStates.recording
            ? (
              <span className="speech-recording-timer">
                {minutes}
:
                {seconds}
              </span>
            )
            : null}
          <div className="record-icon">
            <AudioRecorder
              recordIcon={StartRecordingIcon}
              stopIcon={StopRecordingIcon}
              iconProps={{
                stopProps: {
                  fill: '#004C97',
                },
                recordProps: {
                  fill: '#004C97',
                },
              }}
              styleOverride={{
                button: {
                  height: '70px',
                  ':focus': {
                    outline: 'none',
                  },
                  ':hover': {
                    boxShadow: 'none',
                  },
                },
              }}
              buttonHeight="70px"
              onRecord={this.onClickStartRecord}
              onStop={this.onClickStopRecord}
              registerCallbacks={this.registerCallbacks}
              dispatch={dispatch}
            />
          </div>
        </span>
      );
    case this.audioStates.beforePlaying:
    case this.audioStates.playing:
      return (
        <span>
          <div className="record-icon">
            <AudioPlayer
              audioSrc={audioSrc}
              playIcon={PlayRecordingIcon}
              stopIcon={StopRecordingIcon}
              iconProps={{
                stopProps: {
                  fill: '#004C97',
                },
                playProps: {
                  fill: '#004C97',
                },
              }}
              styleOverride={{
                button: {
                  height: '70px',
                  ':focus': {
                    outline: 'none',
                  },
                  ':hover': {
                    boxShadow: 'none',
                  },
                },
              }}
              onPlay={this.setAudioState}
              onStop={this.setAudioState}
              audioStates={this.audioStates}
              dispatch={dispatch}
            />
          </div>
        </span>
      );
    default:
      return '';
    }
  }

  render() {
    const { header } = this.props;
    const {
      isPlayButtonEnabled,
      isRunTestDisable,
    } = this.state;
    return (
      <div>
        <Dialog
          isOpen
          closeIconVisible
          onClickClose={this.onClickClose}
          okVisible
          okChildren={Constants.RUN_TEST}
          onClickOk={this.onClickRunTest}
          okDisabled={isRunTestDisable}
          cancelVisible={isPlayButtonEnabled}
          cancelChildren="RECORD AGAIN"
          onClickCancel={this.onClickRecordNew}
          headerChildren={header}
          centerContent
          styleOverride={this.styleOverride}
          showBodySeperator={false}
        >
          {this.renderChildren()}
          {this.getActionSubtitle()}
        </Dialog>
      </div>
    );
  }
}

RecordSpeechDialog.defaultProps = {
  header: '',
  onClickRunTest: () => {},
  dispatch: () => {},
};

RecordSpeechDialog.propTypes = {
  dispatch: PropTypes.func,
  header: PropTypes.node,
  onClickRunTest: PropTypes.func,
};
