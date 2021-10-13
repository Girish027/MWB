import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog, Button,
} from '@tfs/ui-components';
import Constants from 'constants/Constants';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsModels from 'state/actions/actions_models';
import { getLanguage } from 'state/constants/getLanguage';
import { getDatasetOptions, getSelectedDatasets } from 'components/models/modelUtils';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';
import validationUtil from 'utils/ValidationUtil';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import SpeechDropZone from 'components/models/speech/SpeechDropzone';
import {
  speechDialog, seperator, speechModal, moreOptionBtn, headerIcon,
} from 'styles';

const lang = getLanguage();
const { DISPLAY_MESSAGES } = lang;

export default class SpeechSelectDatasetDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickAddSpeech = this.onClickAddSpeech.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onChangeDatasets = this.onChangeDatasets.bind(this);
    this.onClickMore = this.onClickMore.bind(this);
    this.onFileLoaded = this.onFileLoaded.bind(this);
    this.onRadioChange = this.onRadioChange.bind(this);
    this.onChangeDigitalHostedUrl = this.onChangeDigitalHostedUrl.bind(this);

    const { model, datasets } = this.props;
    const datasetsOptions = getDatasetOptions(datasets);
    const selectedDatasets = getSelectedDatasets(datasetsOptions, model, null);

    this.state = {
      datasetsOptions,
      selectedDatasets,
      size: datasetsOptions.length > 5 ? 'large' : 'medium',
      showDropbox: false,
      isFileLoaded: true,
      wordclassFile: {},
      isUnbundled: true,
      digitalHostedUrl: '',
      isValidFile: true,
      isValidUrl: true,
    };
  }

  onChangeDatasets(newValue) {
    this.setState({
      selectedDatasets: newValue,
    });
  }

  onClickMore() {
    this.setState({
      showDropbox: true,
    });
  }

  onClickAddSpeech() {
    const { dispatch, model } = this.props;
    const {
      selectedDatasets, digitalHostedUrl,
      isUnbundled, wordClassFile = {},
    } = this.state;

    const speechModel = {
      ...model,
      modelType: Constants.DIGITAL_SPEECH_MODEL,
      startBuild: true,
      selectedDatasets,
      isUnbundled,
      digitalHostedUrl,
    };
    dispatch(actionsModels.createSpeechModel(speechModel, wordClassFile));
    dispatch(actionsApp.modalDialogChange(null));
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange(null));
  }

  onFileLoaded(wordClassFile = {}, isValidFile) {
    this.setState({
      wordClassFile,
      isValidFile,
    });
  }

  onRadioChange({ isUnbundled = true }) {
    this.setState({
      isUnbundled,
    });
  }

  onChangeDigitalHostedUrl(digitalHostedUrl) {
    const isValidUrl = validationUtil.validateUrl(digitalHostedUrl);
    this.setState({
      digitalHostedUrl,
      isValidUrl,
    });
  }

  render() {
    const {
      dispatch,
      userFeatureConfiguration,
      model,
      speechOnly,
    } = this.props;
    const { names } = featureFlagDefinitions;
    const {
      size, isValidFile, isValidUrl, showDropbox, datasetsOptions, selectedDatasets,
    } = this.state;
    const isValid = !isValidUrl || validationUtil.isEmpty(selectedDatasets);
    const { MODEL_ACTION_MENU } = Constants;
    const { speechModelId = '' } = model;
    let speechLabel = MODEL_ACTION_MENU.ADD_SPEECH;
    const isSpeechBundledUnbundledEnabled = isFeatureEnabled(names.speechBundledUnbundled, userFeatureConfiguration);
    let maxHeight = '615px';

    if (speechModelId != '') {
      speechLabel = MODEL_ACTION_MENU.REBUILD_SPEECH;
    }

    if (!isSpeechBundledUnbundledEnabled) {
      maxHeight = '515px';
    } else if (!isValidFile) {
      maxHeight = '655px';
    }

    let dialogStyle = isValidFile && !showDropbox
      ? speechDialog : {
        ...speechDialog,
        container: {
          height: '480px',
          width: '720px',
        },
      };


    return (
      <div>
        <Dialog
          isOpen
          onClickOk={this.onClickAddSpeech}
          okVisible
          okChildren={speechLabel.toUpperCase()}
          okDisabled={isValid}
          closeIconVisible
          onClickClose={this.onClickCancel}
          cancelVisible
          onClickCancel={this.onClickCancel}
          cancelChildren={Constants.CANCEL}
          headerChildren={speechLabel}
          centerContent={false}
          styleOverride={dialogStyle}
          size={size}
        >
          <div style={showDropbox ? speechModal.container : null}>

            <div
              id="speech-dataset"
              style={showDropbox ? { ...seperator, ...speechModal.datasetContainer } : { marginTop: '30px' }}
            >
              <div style={speechModal.datasetContainer.label}>{Constants.ADD_SPEECH_DIALOG_INST}</div>
              <MultipleCheckboxFilter
                options={datasetsOptions}
                value={selectedDatasets}
                onChange={this.onChangeDatasets}
                showAsGrid
                noOfColumns={1}
                className="transparent-bkg"
                type="add-speech"
                showDropbox={showDropbox}
                isSpeechBundledUnbundledEnabled={isSpeechBundledUnbundledEnabled}
              />

              { !showDropbox
                        && (
                          <Button
                            type="flat"
                            name="stop-words"
                            onClick={this.onClickMore}
                            styleOverride={moreOptionBtn}
                          >
                            {Constants.SPEECH_DIALOG.MORE_OPTION}
                          </Button>
                        )}
            </div>

            <div style={{
              ...speechModal.container, display: 'unset', width: '50%', paddingRight: '30px',
            }}
            >
              <SpeechDropZone
                onRadioChange={this.onRadioChange}
                onChange={this.onFileLoaded}
                onChangeDigitalHostedUrl={this.onChangeDigitalHostedUrl}
                dispatch={dispatch}
                speechOnly={speechOnly}
                showDropbox={showDropbox}
                modelToken={model.modelToken}
              />
            </div>
          </div>
        </Dialog>
      </div>
    );
  }
}

SpeechSelectDatasetDialog.defaultProps = {

};

SpeechSelectDatasetDialog.propTypes = {
  dispatch: PropTypes.func.isRequired,
  model: PropTypes.object,
};
