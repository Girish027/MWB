import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Constants from 'constants/Constants';
import * as actionsModels from 'state/actions/actions_models';
import * as appActions from 'state/actions/actions_app';
import * as modelUtils from 'components/models/modelUtils';
import Upload from 'rc-upload';
import axios from 'axios';
import { getLanguage } from 'state/constants/getLanguage';
import {
  LegacyRow,
  RadioGroup,
  ContextualActionItem,
  InfoTooltip,
  Button,
  Record,
  UploadIcon,
  Link,
  PrimaryDivider,
} from '@tfs/ui-components';

const { DISPLAY_MESSAGES } = getLanguage();

export class UtteranceInputBar extends Component {
  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.onClickRunTest = this.onClickRunTest.bind(this);
    this.onClickRunSpeechTest = this.onClickRunSpeechTest.bind(this);
    this.onClickUpload = this.onClickUpload.bind(this);
    this.onClickRecord = this.onClickRecord.bind(this);
    this.onClickLink = this.onClickLink.bind(this);
    this.onModelTypeChange = this.onModelTypeChange.bind(this);

    this.renderRadioButtons = this.renderRadioButtons.bind(this);

    this.actionItemStyle = {
      marginTop: '0px',
      paddingLeft: '0px',
      paddingRight: '20px',
      fontWeight: 'bold',
      icon: {
        width: '15px',
        height: '22px',
        paddingRight: '1px',
      },
    };

    this.state = {
      fileType: '',
      audioSrc: '',
      utterance: '',
      selectedRadioGroupValue: Constants.DIGITAL_SPEECH_MODEL,
    };

    const { dispatch } = this.props;
    this.uploaderProps = {
      multiple: false,
      accept: Constants.AUDIO_TYPE_WAV,
      beforeUpload: () => {
        dispatch(actionsModels.audioUpload());
      },
      customRequest: this.onClickUpload,
      onStart: () => {
        dispatch(appActions.modalDialogChange({
          dispatch,
          type: Constants.DIALOGS.PROGRESS_DIALOG,
          message: Constants.TEST_IN_PROGRESS,
        }));
      },
      onError: () => {
        dispatch(actionsModels.audioUploadFail());
      },
    };
  }

  static getDerivedStateFromProps(props, state) {
    const { speechModelTestResults } = props;
    const { evaluations = [{}] } = speechModelTestResults;
    const { utteranceFileData = '' } = evaluations[0];
    if (utteranceFileData) {
      const { fileType } = state;
      if (fileType === Constants.FILE_TYPE.LINK) {
        const file = modelUtils.base64ToBlob(utteranceFileData, Constants.AUDIO_TYPE_WAV);
        return {
          audioSrc: URL.createObjectURL(file),
        };
      }
    }
    return null;
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(actionsModels.clearModelTestResults());
  }

  onChange(event) {
    event.preventDefault();
    const { value } = event.target;
    this.setState({
      utterance: value,
    });
  }

  onKeyPress(event) {
    // TODO : write KeyboardUtils APIs
    if (event.key === 'Enter'
      || event.key === 'Tab') {
      event.preventDefault();
      this.onClickRunTest();
    }
  }

  onClickRunTest() {
    const { submitTest } = this.props;
    const { utterance } = this.state;
    submitTest(utterance);
  }

  onClickRunSpeechTest({ audioFile, audioURL, fileType }) {
    const {
      client, projectId, dispatch, model, speechTestTimeout,
    } = this.props;

    const signal = axios.CancelToken.source();

    const data = {
      clientId: client.id,
      projectId,
      fileType,
      audioURL,
      audioFile,
      modelId: model.modelToken,
      model,
      cancelToken: signal.token,
    };
    dispatch(actionsModels.testSpeechModel(data));

    setTimeout(() => {
      if (signal) {
        signal.cancel(DISPLAY_MESSAGES.speechTestTimeoutMsg);
      }
      dispatch(appActions.modalDialogChange(null));
    }, speechTestTimeout);
  }

  onClickUpload(input) {
    const { dispatch } = this.props;
    const { file: audioFile } = input;
    const fileType = Constants.FILE_TYPE.UPLOADED;
    const reader = new FileReader();
    reader.onload = () => {
      this.setState({
        audioSrc: URL.createObjectURL(audioFile),
        fileType,
      });
      dispatch(actionsModels.audioUploadSuccess(audioFile));
      this.onClickRunSpeechTest({
        audioFile,
        fileType,
      });
    };
    reader.readAsBinaryString(audioFile);
  }

  onClickRecord() {
    const { dispatch } = this.props;
    const fileType = Constants.FILE_TYPE.RECORDING;
    dispatch(appActions.modalDialogChange({
      header: 'Record and Test',
      dispatch,
      type: Constants.DIALOGS.RECORD_SPEECH,
      onClickRunTest: (audioFile) => {
        this.setState({
          audioSrc: URL.createObjectURL(audioFile),
          fileType,
        });
        this.onClickRunSpeechTest({
          audioFile,
          fileType,
        });
      },
    }));
  }

  onClickLink() {
    const { dispatch } = this.props;
    const fileType = Constants.FILE_TYPE.LINK;

    dispatch(appActions.modalDialogChange({
      header: 'Link audio file URL to test',
      dispatch,
      type: Constants.DIALOGS.LINK_DIALOG,
      onClickRunTest: (audioURL) => {
        this.setState({
          audioSrc: audioURL,
          fileType,
        });
        this.onClickRunSpeechTest({
          audioURL,
          fileType,
        });
      },
    }));
  }

  onModelTypeChange(value) {
    const { testModelType, onModelTypeChange } = this.props;
    if (value !== testModelType) {
      this.setState({
        utterance: '',
        audioSrc: '',
        fileType: '',
        selectedRadioGroupValue: value,
      }, () => {
        onModelTypeChange(value);
      });
    }
  }

  renderRadioButtons() {
    // TODO: https://247inc.atlassian.net/browse/CUC-69
    // Bug: Label styles to be made configurable for Radio
    // TODO: https://247inc.atlassian.net/browse/CUC-65, https://247inc.atlassian.net/browse/NT-3072
    // Bug: Parent should be able to control value of RadioGroup
    const { model } = this.props;
    const modelType = model ? model.modelType : '';

    if (modelType === Constants.DIGITAL_SPEECH_MODEL) {
      return (
        <span>
          <li className="center-item-li radio-group">
            <RadioGroup
              displayType="horizontal"
              onChange={this.onModelTypeChange}
              styleOverride={{
                paddingLeft: '20px',
                container: {},
                label: {
                  color: '#575757',
                },
              }}
              values={(modelType === Constants.SPEECH_MODEL) ? [Constants.DIGITAL_SPEECH_MODEL] : [Constants.DIGITAL_SPEECH_MODEL, Constants.DIGITAL_MODEL]}
              labels={Constants.TEST_MODEL_RADIO_LABELS}
              value={this.state.selectedRadioGroupValue}
            />
          </li>
          <li className="center-item-li">
            <PrimaryDivider />
          </li>
        </span>
      );
    }
    return '';
  }

  render() {
    const { utterance, fileType, audioSrc } = this.state;
    const {
      model, testModelType, handleAudio, utterance: utteranceText,
    } = this.props;

    const modelType = model ? model.modelType : '';
    if ((modelType === Constants.DIGITAL_SPEECH_MODEL) || (modelType === Constants.SPEECH_MODEL)) {
      handleAudio(fileType, audioSrc);
    }

    return (
      <LegacyRow styleOverride={{
        borderTop: '1px solid #fafbfc',
        backgroundColor: '#fafbfc',
      }}
      >
        <ul className="utterance-input-area-ul">
          {this.renderRadioButtons()}
          {(testModelType === Constants.DIGITAL_SPEECH_MODEL || testModelType === Constants.SPEECH_MODEL)
            ? (
              <span>
                <li className="center-item-li" key="input-bar-speech" style={{ paddingLeft: '30px' }}>
                  <ContextualActionItem
                    styleOverride={this.actionItemStyle}
                    icon={Record}
                    onClickAction={this.onClickRecord}
                  >
                  RECORD
                  </ContextualActionItem>
                  <ContextualActionItem
                    styleOverride={this.actionItemStyle}
                    icon={UploadIcon}
                    left
                  >
                    <Upload
                      {...this.uploaderProps}
                    >
                    UPLOAD
                    </Upload>
                  </ContextualActionItem>
                  <ContextualActionItem
                    styleOverride={{
                      ...this.actionItemStyle,
                      paddingRight: '0px',
                    }}
                    icon={Link}
                    onClickAction={this.onClickLink}
                    left
                  >
                  LINK
                  </ContextualActionItem>

                </li>
              </span>
            )
            : (testModelType === Constants.DIGITAL_SPEECH_MODEL || testModelType === Constants.DIGITAL_MODEL)
              ? (
                <li className="center-item-li" key="input-bar-digital">
                  <textarea
                    name="utterance"
                    placeholder={Constants.UTTERANCE_PLACEHOLDER_TEXT(utteranceText)}
                    onChange={this.onChange}
                    onKeyDown={this.onKeyPress}
                    className="utterance-input-area-input"
                    rows="1"
                    cols="100"
                  />
                  <div className="model-test-utterance-instructions">Count: 200</div>
                  <InfoTooltip
                    tooltipText="Do not include any Personally Identifiable information in the utterance string"
                    direction="right"
                  />
                  {utterance.length ? (
                    <Button
                      className="model-test-submit"
                      onClick={this.onClickRunTest}
                    >
                      {Constants.RUN_TEST}
                    </Button>
                  ) : ''}
                </li>
              ) : ''
          }
        </ul>
      </LegacyRow>
    );
  }
}

UtteranceInputBar.propTypes = {
  testModelType: PropTypes.string,
  speechModelTestResults: PropTypes.object,
  model: PropTypes.object,
  onModelTypeChange: PropTypes.func.isRequired,
  submitTest: PropTypes.func.isRequired,
  utterance: PropTypes.string,
};

UtteranceInputBar.defaultProps = {
  speechModelTestResults: {},
  testModelType: Constants.DIGITAL_MODEL,
  model: {},
  utterance: '',
};

const mapStateToProps = (state, ownProps) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  const { userId, csrfToken, speechTestTimeout } = state.app;
  return {
    userId,
    csrfToken,
    speechTestTimeout,
    projectId,
    client: state.header.client,
    speechModelTestResults: state.projectsManager.speechModelTestResults,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(UtteranceInputBar);
