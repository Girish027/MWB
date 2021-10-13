import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';
import Constants from 'constants/Constants';
import { getDatasetOptions, getSelectedDatasets } from 'components/models/modelUtils';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import * as actionsStats from 'state/actions/actions_datasets_transformed_stats';
import {
  Textarea, Download, Doc, RadioGroup,
} from '@tfs/ui-components';
import { getDataUrl, pathKey } from 'utils/apiUrls';
import validationUtil from 'utils/ValidationUtil';
import Dropzone from '../Form/Dropzone';
import { dropzoneContainer, seperators } from '../../styles';
import ValidationPlaceholder from '../fields/ValidationPlaceholder';
import TextIcon from '../Icons/TextIcon';

const getModel = (model, models, project) => {
  if (!_.isNil(model) && (!_.isNil(model.name) || !_.isNil(model.description))) {
    return model;
  }
  const modelId = null;
  if (!_.isNil(modelId)) {
    const modelsArray = !_.isNil(models) ? models.toArray() : [];
    const modelItem = _.find(modelsArray, { id: modelId });
    return modelItem;
  }
  if (!_.isNil(project)) {
    const { name = '' } = project;
    const newModel = Object.assign({}, model, { name });
    return newModel;
  }
  return {};
};

class NewSpeechModel extends Component {
  constructor(props) {
    super(props);
    this.onDescriptionChange = this.onDescriptionChange.bind(this);
    this.onDescriptionBlur = this.onDescriptionBlur.bind(this);
    this.getDatasetsLabel = this.getDatasetsLabel.bind(this);
    this.saveFile = this.saveFile.bind(this);
    this.rejectFile = this.rejectFile.bind(this);
    this.handleFileChoosen = this.handleFileChoosen.bind(this);
    this.handleFileRead = this.handleFileRead.bind(this);
    this.onClickDownload = this.onClickDownload.bind(this);
    this.onChangeRadioButton = this.onChangeRadioButton.bind(this);
    this.isValidated = this.isValidated.bind(this);
    this.isDatasetValid = this.isDatasetValid.bind(this);
    this.isNameValid = this.isNameValid.bind(this);
    this.saveModelChanges = this.saveModelChanges.bind(this);

    const {
      model, models,
      datasets, project,
    } = this.props;

    const datasetsOptions = getDatasetOptions(datasets);
    let newModel = getModel(model, models, project);

    const selectedDatasets = getSelectedDatasets(datasetsOptions, newModel, null);

    this.isActive = true;

    this.state = {
      selectedDatasets,
      datasetsOptions,
      model: newModel,
      showNameErrorMessage: false,
      showDatasetErrorMessage: false,
      isValid: true,
      selectedRadioGroupValue: Constants.SPEECH_DIALOG.UNBUNDLED,
    };

    this.fileReader = null;
    this.datasetFile = null;
  }

  componentDidMount() {
    this.isValidated();
  }

  static getDerivedStateFromProps(props, state) {
    const {
      datasets, models, model, project,
    } = props;
    let newModel = !_.isNil(state.model) ? state.model : model;

    newModel = getModel(newModel, models, project);

    const datasetsOptions = getDatasetOptions(datasets);
    const selectedDatasets = getSelectedDatasets(datasetsOptions, newModel, state.selectedDatasets);

    return {
      datasetsOptions,
      model: newModel,
      selectedDatasets,
    };
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

  rejectFile() {
    const { onChange } = this.props;
    const isValid = false;
    this.setState({
      isValid,
    });
    onChange({}, isValid);
  }


  saveModelChanges() {
    const {
      saveModelChanges,
    } = this.props;
    const {
      model, selectedDatasets,
    } = this.state;

    const newModel = Object.assign({}, model);
    newModel.datasetIds = selectedDatasets;
    saveModelChanges(newModel);
  }

  isNameValid(name = '') {
    let isDataValid = true;
    name = name.trim();
    if (_.isNil(name) || _.isEmpty(name)) {
      isDataValid = false;
    }
    return isDataValid;
  }

  isDatasetValid(selectedDatasets = []) {
    const {
      project, dispatch,
    } = this.props;
    const { datasetId = -1 } = this.state;
    const { id: projectId } = project;
    let isDataValid = true;

    if (datasetId !== -1 && selectedDatasets.indexOf(datasetId) === -1) {
      dispatch(actionsStats.deleteDatasetValidationStats(datasetId));
      if (selectedDatasets.length === 0) {
        isDataValid = false;
      }
    } else if (selectedDatasets.length === 0) {
      isDataValid = false;
    }
    return isDataValid;
  }

  isValidated() {
    const { setValidModel, saveModelChanges } = this.props;
    const { model = {}, selectedDatasets = [] } = this.state;
    const isModelNameValid = this.isNameValid(model.name);

    if (isModelNameValid && (selectedDatasets.length > 0)) {
      model.datasetIds = selectedDatasets;
      saveModelChanges(model);
      setValidModel(true);
    } else {
      setValidModel(false);
    }
  }

  onDescriptionChange(e) {
    e.preventDefault();
    this.setState({
      model: Object.assign({}, this.state.model, { description: e.target.value }),
    });
  }

  onDescriptionBlur(e) {
    e.preventDefault();

    const model = Object.assign({}, this.state.model);

    model.description = e.target.value.trim();

    this.setState({
      model,
    }, () => {
      this.saveModelChanges();
    });
  }

  getDatasetsLabel() {
    return (
      <div style={{ color: '#727272' }}>
        Datasets*
        {this.state.showDatasetErrorMessage
          ? <div className="validation-error">Please select at least one dataset</div>
          : null
        }
      </div>
    );
  }

  render() {
    const {
      modelViewReadOnly, userFeatureConfiguration, project,
    } = this.props;
    const { names } = featureFlagDefinitions;

    const { model = {}, isValid, selectedRadioGroupValue } = this.state;
    let { description = '' } = model;

    const modelName = project && project.name || '';
    return (
      <div id="NewModel">
        <ul className="form-fields">
          <li>
            <label>Model</label>
            <div style={{ color: '#313f54' }}>
              {' '}
              {modelName}
            </div>
          </li>
          <li>
            <label>Version Description</label>
            <Textarea
              type="text"
              name="modelDescription"
              placeholder="Enter a description"
              defaultValue={description}
              onBlur={this.onDescriptionBlur}
              onChange={this.onDescriptionChange}
              styleOverride={{
                width: '100%',
              }}
              disabled={modelViewReadOnly}
            />
          </li>
          <li>
            <MultipleCheckboxFilter
              label={this.getDatasetsLabel()}
              className="DatasetsFilter"
              options={this.state.datasetsOptions}
              value={this.state.selectedDatasets}
              onChange={(selectedDatasets, datasetId) => {
                this.setState({
                  selectedDatasets,
                  datasetId,
                }, () => {
                  this.saveModelChanges();
                  this.isValidated();
                });
              }}
              disabled={modelViewReadOnly}
              showAsGrid={false}
            />
          </li>
        </ul>
        <div id="speech-dropbox" style={{ ...seperators }}>
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

          {(isFeatureEnabled(names.speechBundledUnbundled, userFeatureConfiguration)) && (
            <div style={dropzoneContainer.radioGroup}>
              <div style={dropzoneContainer.speechRadioAndTextGroup}>
                <div style={dropzoneContainer.speechRadioAndTextGroup.label}> Output </div>
                <div style={modelViewReadOnly === true ? { pointerEvents: 'none', opacity: 0.5 } : {}}>
                  <RadioGroup
                    displayType="stacked"
                    value={selectedRadioGroupValue}
                    values={[Constants.SPEECH_DIALOG.BUNDLED, Constants.SPEECH_DIALOG.UNBUNDLED]}
                    labels={[Constants.SPEECH_DIALOG.BUNDELED_LABEL, Constants.SPEECH_DIALOG.UNBUNDLED_LABEL]}
                    onChange={this.onChangeRadioButton}
                  />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }
}

NewSpeechModel.defaultProps = {
  datasets: {},
  model: {},
  project: {},
  models: null,
  onChange: () => {},
  newModelConfig: () => {},
  setValidModel: () => {},
  saveModelChanges: () => {},
  modelViewReadOnly: false,
};

NewSpeechModel.propTypes = {
  datasets: PropTypes.object,
  model: PropTypes.object,
  models: PropTypes.object,
  project: PropTypes.object,
  config: PropTypes.object,
  onChange: PropTypes.func,
  setValidModel: PropTypes.func,
  saveModelChanges: PropTypes.func,
  reportError: PropTypes.func,
  reportSuccess: PropTypes.func,
  modelViewReadOnly: PropTypes.bool,
};

export default NewSpeechModel;
