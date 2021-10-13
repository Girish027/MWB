import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  RadioGroup, Doc, Download, TextField,
} from '@tfs/ui-components';
import { connect } from 'react-redux';
import { getDataUrl, pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import validationUtil from 'utils/ValidationUtil';
import Model from 'model';
import Dropzone from '../../Form/Dropzone';
import TextIcon from '../../Icons/TextIcon';
import { dropzoneContainer, seperator, speechDialog } from '../../../styles';
import ValidationPlaceholder from '../../fields/ValidationPlaceholder';

export class SpeechDropzone extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.saveFile = this.saveFile.bind(this);
    this.rejectFile = this.rejectFile.bind(this);
    this.handleFileChoosen = this.handleFileChoosen.bind(this);
    this.handleFileRead = this.handleFileRead.bind(this);
    this.onClickDownload = this.onClickDownload.bind(this);
    this.onChangeRadioButton = this.onChangeRadioButton.bind(this);
    this.onChangeDigitalUrl = this.onChangeDigitalUrl.bind(this);

    this.state = {
      isValid: true,
      url: '',
      selectedRadioGroupValue: Constants.SPEECH_DIALOG.UNBUNDLED,
    };

    this.fileReader = null;
    this.datasetFile = null;
  }

  saveFile(acceptedFiles) {
    this.wordClassFile = acceptedFiles[0];
    let blob = new Blob(acceptedFiles, { type: 'text/plain;charset=utf-8' });
    this.handleFileChoosen(blob);
  }

  handleFileRead = (e) => {
    const { onChange } = this.props;
    const content = this.fileReader.result;
    let wordClassFile = {};
    const isValid = validationUtil.validateWorldClassFile(content);

    this.setState({
      isValid,
    });

    if (isValid) {
      wordClassFile = this.wordClassFile;
    }
    onChange(wordClassFile, isValid);
  };

  handleFileChoosen(file) {
    this.fileReader = new FileReader();
    this.fileReader.onload = (e) => this.handleFileRead(e);
    this.fileReader.readAsText(file);
  }

  onClickDownload() {
    const locationUrl = getDataUrl(pathKey.wordClassDefault);
    document.location = locationUrl;
  }

  onChangeRadioButton(selectedVal) {
    const { onRadioChange } = this.props;
    this.setState({
      selectedRadioGroupValue: selectedVal,
    });
    if (selectedVal === Constants.SPEECH_DIALOG.BUNDLED) {
      onRadioChange({ isUnbundled: false });
    } else if (selectedVal === Constants.SPEECH_DIALOG.UNBUNDLED) {
      onRadioChange({ isUnbundled: true });
    }
  }

  componentDidMount() {
    const {
      modelToken, orionURL, onChangeDigitalHostedUrl,
    } = this.props;
    const url = `${orionURL}/${modelToken}/digital`;
    this.setState({ url });
    onChangeDigitalHostedUrl(url);
  }

  onChangeDigitalUrl(evt) {
    const { onChangeDigitalHostedUrl } = this.props;
    let digitalUrl = evt.target.value;
    onChangeDigitalHostedUrl(digitalUrl);
  }

  rejectFile() {
    const { onChange } = this.props;
    const isValid = false;
    this.setState({
      isValid,
    });
    onChange({}, isValid);
  }

  render() {
    const {
      showDropbox, userFeatureConfiguration, speechOnly,
    } = this.props;
    const { isValid, url, selectedRadioGroupValue } = this.state;
    const { names } = featureFlagDefinitions;

    return (
      <div id="speech-dropbox" style={!showDropbox ? { display: 'none' } : { ...seperator, paddingRight: '0px', marginTop: '30px' }}>
        <div style={{ paddingBottom: '20px' }}>
          <span style={dropzoneContainer.label}>
            Upload word class file
          </span>
          <div style={dropzoneContainer.link}>
            <span style={dropzoneContainer.link.icon}>
              <Download width="10px" height="13px" fill="#004c97" />
            </span>
            <div style={dropzoneContainer.link.label} onClick={this.onClickDownload}> Download existing </div>
          </div>
        </div>

        <Dropzone
          accept="text/plain"
          icon={TextIcon}
          acceptedIcon={Doc}
          multiple={false}
          saveFile={this.saveFile}
          rejectFile={this.rejectFile}
          removeFile={!isValid}
        />

        {!isValid
          && <ValidationPlaceholder styleOverride={{ marginTop: '10px' }} validationMessage={Constants.SPEECH_DIALOG.INVALID_FILE_CONTENT} />}

        { (isFeatureEnabled(names.speechBundledUnbundled, userFeatureConfiguration)) && (
          <div>
            <div style={dropzoneContainer.speechRadioAndTextGroup}>
              <div style={dropzoneContainer.speechRadioAndTextGroup.label}> Output </div>
              <div>
                <RadioGroup
                  displayType="stacked"
                  value={selectedRadioGroupValue}
                  values={[Constants.SPEECH_DIALOG.BUNDLED, Constants.SPEECH_DIALOG.UNBUNDLED]}
                  labels={[Constants.SPEECH_DIALOG.BUNDELED_LABEL, Constants.SPEECH_DIALOG.UNBUNDLED_LABEL]}
                  onChange={this.onChangeRadioButton}
                />
              </div>
            </div>
            <div style={dropzoneContainer.speechRadioAndTextGroup}>
              {!speechOnly && (
                <React.Fragment>
                  <div style={dropzoneContainer.speechRadioAndTextGroup.label}> Digital Model Url </div>
                  <TextField
                    type="text"
                    defaultValue={url}
                    name="digital-url"
                    title="URL of Digital Model for the Speech recognizer to point to"
                    styleOverride={{
                      width: '100%',
                      paddingBottom: '15px',
                    }}
                    onChange={this.onChangeDigitalUrl}
                  />
                </React.Fragment>
              )}
              <div />
            </div>
          </div>
        )}
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  orionURL: state.app.orionURL,
});

SpeechDropzone.defaultProps = {
  dispatch: () => {},
  showDropbox: false,
};

SpeechDropzone.propTypes = {
  speechOnly: PropTypes.bool,
  showDropbox: PropTypes.bool,
  dispatch: PropTypes.func,
  onChange: PropTypes.func.isRequired,
  onRadioChange: PropTypes.func.isRequired,
  onChangeDigitalHostedUrl: PropTypes.func.isRequired,
  modelToken: PropTypes.string,
};

export default connect(mapStateToProps)(SpeechDropzone);
