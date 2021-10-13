import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Select from 'react-select';
import Upload from 'rc-upload';
import MultipleCheckboxFilter from 'components/controls/MultipleCheckboxFilter';
import Constants from 'constants/Constants';
import { getDatasetOptions, getSelectedDatasets } from 'components/models/modelUtils';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import * as actionsConfigs from 'state/actions/actions_configs';
import * as actionsStats from 'state/actions/actions_datasets_transformed_stats';
import Model from 'model';
import { getLanguage } from 'state/constants/getLanguage';
import {
  Button, Textarea, PrimaryDivider, Checkbox, RadioGroup,
} from '@tfs/ui-components';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

const isUploadedConfigSelected = (selectedConfig = {}) => {
  const index = selectedConfig.name.indexOf(Constants.UPLOADED_CONFIG_NAME);
  if (index === 0) {
    return true;
  }
  return false;
};

// The config list consists of
// ~ System default (1)
// ~ Model name | Version (0..n)
// ~ Upload config (0, 1)
// ~ Selected config (0, 1)
//
// Entry points
// ~ Tune
// ~ Create a new model
// ~ From a step in the wizard
const getConfigDropdownList = (dropdownData) => {
  let {
    configs, models, tuneModelId,
    viewModelId, selectedConfig,
    existingListItems = [],
  } = dropdownData;

  const defaultConfig = Model.ModelConfigManager.getDefaultConfig(configs);

  let modelsArray = !_.isNil(models) ? models.toArray() : [];
  modelsArray = _.orderBy(modelsArray, ['updated'], ['desc']);

  const configsArray = [...existingListItems];

  // populate state.configsArray for the first time
  if (!configsArray.length) {
    if (defaultConfig) {
      configsArray.push(Object.assign({}, defaultConfig, { modelType: Constants.DIGITAL_MODEL }));
    }
    // Convert models to config items
    modelsArray.forEach((modelItem) => {
      const configItem = {
        key: '-1',
        id: modelItem.configId,
        name: `Version ${modelItem.version}-cfg`,
        description: '',
        modelType: modelItem.modelType,
      };
      configsArray.push(configItem);
    });
  }

  let newSelectedConfig = defaultConfig;

  if (tuneModelId) {
    const modelItem = _.find(modelsArray, { id: tuneModelId });
    newSelectedConfig = _.find(configsArray, { id: modelItem.configId });
  } else if (viewModelId) {
    const modelItem = _.find(modelsArray, { id: viewModelId });
    newSelectedConfig = _.find(configsArray, { id: modelItem.configId });
  }
  // if config has been changed, that config should be retained over tune/view model config
  if (!_.isNil(selectedConfig)) {
    newSelectedConfig = selectedConfig;
  }

  return ({
    configsArray,
    selectedConfig: newSelectedConfig,
  });
};

const getModel = (model, models, tuneModelId, viewModelId, project) => {
  if (!_.isNil(model) && (!_.isNil(model.name) || !_.isNil(model.description))) {
    return model;
  }
  const modelId = tuneModelId || viewModelId || null;
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

class NewModel extends Component {
  constructor(props) {
    super(props);
    this.onDescriptionChange = this.onDescriptionChange.bind(this);
    this.onDescriptionBlur = this.onDescriptionBlur.bind(this);
    this.onConfigSelected = this.onConfigSelected.bind(this);
    this.getConfigSelect = this.getConfigSelect.bind(this);
    this.configUploadCustomRequest = this.configUploadCustomRequest.bind(this);
    this.addUploadedConfig = this.addUploadedConfig.bind(this);
    this.getDatasetsLabel = this.getDatasetsLabel.bind(this);
    this.configUploadSuccess = this.configUploadSuccess.bind(this);
    this.configUploadError = this.configUploadError.bind(this);
    this.onLeave = this.onLeave.bind(this);
    this.isValidated = this.isValidated.bind(this);
    this.isDatasetValid = this.isDatasetValid.bind(this);
    this.isNameValid = this.isNameValid.bind(this);
    this.parseConfigFile = this.parseConfigFile.bind(this);
    this.saveModelChanges = this.saveModelChanges.bind(this);
    this.onClickRadioButton = this.onClickRadioButton.bind(this);
    this.onClickCheckbox = this.onClickCheckbox.bind(this);
    this.getDefaultText = this.getDefaultText.bind(this);
    this.getCheckbox = this.getCheckbox.bind(this);
    this.getDefaultTechnology = this.getDefaultTechnology.bind(this);

    const {
      model, configs, config, models, tuneModelId, viewModelId,
      datasets, dispatch, project,
    } = this.props;

    this.uploaderProps = {
      multiple: false,
      beforeUpload: () => { dispatch(actionsConfigs.configUpload()); },
      onStart: (file) => { },
      customRequest: this.configUploadCustomRequest,
      onSuccess: this.configUploadSuccess,
      onProgress: (step, file) => { },
      onError: this.configUploadError,
    };

    const datasetsOptions = getDatasetOptions(datasets);
    let newModel = getModel(model, models, tuneModelId, viewModelId, project);
    const { configsArray, selectedConfig } = getConfigDropdownList({
      configs,
      models,
      selectedConfig: null,
      config,
      tuneModelId,
      viewModelId,
      existingListItems: [],
    });

    if (!_.isNil(props.onNewConfigSelect)) {
      props.onNewConfigSelect(selectedConfig);
    }

    const selectedDatasets = getSelectedDatasets(datasetsOptions, newModel, null);

    this.isActive = true;

    if (!_.isNil(props.registerCallbacks)) {
      props.registerCallbacks({
        tabName: props.tabName,
        onLeave: this.onLeave,
      });
    }

    this.state = {
      selectedDatasets,
      configsArray,
      selectedConfig,
      datasetsOptions,
      model: newModel,
      showNameErrorMessage: false,
      showDatasetErrorMessage: false,
      config,
      uploadedConfigFile: '',
      showCheckbox: false,
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.isCurrentTab) {
      return true;
    }
    return false;
  }

  componentDidMount() {
    this.isValidated();
  }

  static getDerivedStateFromProps(props, state) {
    const {
      datasets, configs, models, model, tuneModelId, viewModelId, config, project,
    } = props;
    let newModel = !_.isNil(state.model) ? state.model : model;

    newModel = getModel(newModel, models, tuneModelId, viewModelId, project);

    const newConfig = !_.isNil(config) ? config : state.config;
    const datasetsOptions = getDatasetOptions(datasets);
    const selectedDatasets = getSelectedDatasets(datasetsOptions, newModel, state.selectedDatasets);
    const { configsArray, selectedConfig } = getConfigDropdownList({
      configs,
      models,
      selectedConfig: state.selectedConfig,
      config: newConfig,
      tuneModelId,
      viewModelId,
      existingListItems: state.configsArray,
    });

    return {
      configsArray,
      datasetsOptions,
      selectedConfig,
      model: newModel,
      config: newConfig,
      selectedDatasets,
    };
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

  onLeave() {
    this.saveModelChanges();
  }

  addUploadedConfig(uploadedFile) {
    const {
      saveConfigChanges, onNewConfigSelect,
    } = this.props;
    const {
      configsArray, uploadedConfigFile,
    } = this.state;

    // Note -1 : here we are removing the 'speechConfigs' from uploaded config file
    // Reason: Aim is to create a Digital model and Speech capability can be added later
    // Note - 2: removing 'modelType' as ModelBuilder creates 'classifier' models as default
    // eslint-disable-next-line no-unused-vars
    const { speechConfigs, modelType, ...uploadedConfig } = this.parseConfigFile(uploadedFile);
    const UPLOADED_ID = 'uploaded';
    const name = `${Constants.UPLOADED_CONFIG_NAME}`;
    const selectedConfig = {
      key: '-1',
      id: UPLOADED_ID,
      name,
      description: uploadedConfig.description,
    };

    uploadedConfig.name = name;

    // handle re-upload of config
    const newConfigsArray = configsArray.slice(0);
    if (uploadedConfigFile) {
      const existingUploadIndex = _.findLastIndex(newConfigsArray, configItem => configItem.id === UPLOADED_ID);
      newConfigsArray.splice(existingUploadIndex, 1, selectedConfig);
    } else {
      newConfigsArray.push(selectedConfig);
    }

    this.setState({
      configsArray: newConfigsArray,
      selectedConfig,
      config: uploadedConfig,
      uploadedConfigFile: uploadedFile,
    }, () => {
      saveConfigChanges(uploadedConfig);
      onNewConfigSelect(selectedConfig);
    });
  }

  parseConfigFile(uploadedFile) {
    let parsed = {};
    try {
      parsed = JSON.parse(uploadedFile);
    } catch (err) {
      throw new Error(DISPLAY_MESSAGES.invalidConfigFile);
    }
    return parsed;
  }

  configUploadCustomRequest(input) {
    const { reportSuccess, reportError, dispatch } = this.props;
    const reader = new FileReader();
    reader.addEventListener('loadend', () => {
      try {
        const uploadedFile = new TextDecoder('utf-8').decode(new Uint8Array(reader.result));
        this.addUploadedConfig(uploadedFile);
        reportSuccess(DISPLAY_MESSAGES.configFileUploaded(input.file.name));
        dispatch(actionsConfigs.configUploadSuccess());
      } catch (err) {
        reportError(err);
        dispatch(actionsConfigs.configUploadFail());
      }
    });
    reader.readAsArrayBuffer(input.file);
  }

  configUploadSuccess(result, file, xhr) {
    const { reportSuccess, dispatch } = this.props;
    dispatch(actionsConfigs.configUploadSuccess());
    reportSuccess(DISPLAY_MESSAGES.configFileUploaded(file.name));
  }

  configUploadError(err, response, file) {
    const { dispatch, reportError } = this.props;
    dispatch(actionsConfigs.configUploadFail());
    reportError(err);
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
      project, dispatch, tuneModelId, viewModelId,
    } = this.props;
    const { datasetId = -1 } = this.state;
    const { id: projectId } = project;
    let isDataValid = true;

    if ((tuneModelId || viewModelId) && selectedDatasets.length && datasetId == -1) {
      selectedDatasets.map(dId => {
        dispatch(actionsStats.fetchDatasetValidationStatsById({ projectId, datasetId: dId }));
      });
    } else if (datasetId !== -1 && selectedDatasets.indexOf(datasetId) === -1) {
      dispatch(actionsStats.deleteDatasetValidationStats(datasetId));
      if (selectedDatasets.length === 0) {
        isDataValid = false;
      }
    } else if (selectedDatasets.length === 0) {
      isDataValid = false;
    } else if (datasetId !== -1) {
      dispatch(actionsStats.fetchDatasetValidationStatsById({ projectId, datasetId }));
    }
    return isDataValid;
  }

  isValidated() {
    const { setValidModel, saveModelChanges } = this.props;
    const { model = {}, selectedDatasets = [] } = this.state;
    const isModelNameValid = this.isNameValid(model.name);
    const isModelDatasetValid = this.isDatasetValid(selectedDatasets);

    if (isModelNameValid && isModelDatasetValid) {
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
    const { names } = featureFlagDefinitions;
    const { userFeatureConfiguration } = this.props;
    return (
      <div style={{ color: '#727272' }}>
        Datasets*
        {this.state.showDatasetErrorMessage
          ? <div className="validation-error">Please select at least one dataset</div>
          : null
        }
        <div className="help-text overflow-wrap">
          Select datasets with these properties to enable "Create Version":
          <ul className="indentation">
            { (isFeatureEnabled(names.granularMessage, userFeatureConfiguration)) ? (
              <li>Granular intents mapped to Rollup intents in Tagging Guide</li>
            ) : null
            }
            <li>Tagged with at least two Rollup intents</li>
            <li>Contains multiple transcriptions per Rollup intents</li>
          </ul>
        </div>
      </div>
    );
  }

  onConfigSelected(input) {
    const newConfigName = input.label;

    const {
      saveConfigChanges, fetchConfigById, onNewConfigSelect,
    } = this.props;
    const { configsArray, uploadedConfigFile } = this.state;

    const selectedConfig = _.find(configsArray, { name: newConfigName });

    this.setState({
      selectedConfig,
    }, () => {
      if (selectedConfig.name.indexOf(Constants.UPLOADED_CONFIG_NAME) !== 0) {
        fetchConfigById(selectedConfig);
      } else {
        const uploadedConfig = this.addUploadedConfig(uploadedConfigFile);
        saveConfigChanges(uploadedConfig);
      }
      onNewConfigSelect(selectedConfig);
    });
  }

  getConfigSelect(disabled) {
    const { configsArray = [], selectedConfig } = this.state;

    let selectedIndex = 0;
    if (configsArray.length === 0) {
      return null;
    }
    const { modelViewReadOnly } = this.props;

    const options = [];

    configsArray.filter(config => config.modelType == Constants.DIGITAL_MODEL).map(({ name }, index) => {
      options.push({
        label: name,
        value: name,
      });

      if (selectedConfig && selectedConfig.name && selectedConfig.name === name) {
        selectedIndex = index;
      }
    });

    return (
      <Select
        name="model-config-select"
        placeholder="Select model configuration"
        isDisabled={modelViewReadOnly}
        options={options}
        onChange={this.onConfigSelected}
        isSearchable
        clearable={false}
        isMulti={false}
        value={options[selectedIndex]}
        defaultValue={options[selectedIndex]}
      />
    );
  }

  onClickRadioButton(selectedValue) {
    const { modelViewReadOnly } = this.props;
    if (!modelViewReadOnly) {
      const model = Object.assign({}, this.state.model);
      const showCheckbox = true;
      model.toDefault = undefined;
      model.modelTechnology = selectedValue;
      this.setState({
        model,
        showCheckbox,
      }, () => {
        this.saveModelChanges();
      });
    } else {
      const model = Object.assign({}, this.state.model);
      model.modelTechnology = selectedValue;
      this.setState({
        model,
      }, () => {
        this.saveModelChanges();
      });
    }
  }

  onClickCheckbox(event) {
    const model = Object.assign({}, this.state.model);
    model.toDefault = event.checked ? event.value : false;
    this.setState({
      model,
    }, () => {
      this.saveModelChanges();
    });
  }

  getCheckbox(modelViewReadOnly) {
    const defaultTechnology = this.getDefaultTechnology();
    const { model, showCheckbox } = this.state;
    const { modelTechnology = '', toDefault = false } = model;
    let styles = {};
    if (modelTechnology === Constants.MODEL_TECHNOLOGY.USE) {
      styles = { paddingRight: '200px', paddingLeft: '5px' };
    } else if (modelTechnology === Constants.MODEL_TECHNOLOGY.N_GRAM) {
      styles = { paddingLeft: '210px' };
    }
    if (showCheckbox && modelTechnology === defaultTechnology) {
      this.setState({
        showCheckbox: false,
      });
    }
    if (showCheckbox || (modelTechnology !== defaultTechnology && !modelViewReadOnly)) {
      return (
        <Checkbox
          checked={toDefault}
          value
          label={<span style={{ color: '#333333' }}>{Constants.CHECKBOX_LABEL}</span>}
          onChange={(event) => this.onClickCheckbox(event.target)}
          styleOverride={styles}
          disabled={modelViewReadOnly}
        />
      );
    }
  }

  getDefaultTechnology() {
    const { currentType } = this.props;
    if (Object.keys(currentType).length > 0) {
      return currentType.type;
    }
    return Constants.MODEL_TECHNOLOGY.N_GRAM;
  }

  getDefaultText() {
    const defaultTechnology = this.getDefaultTechnology();
    const text = Constants.DEFAULT_TEXT;
    const defaultText = { tensorflow: '', n_gram: '' };
    const { model } = this.state;
    let { modelTechnology = '', vectorizerTechnology } = model;
    if (modelTechnology === '') {
      model.modelTechnology = vectorizerTechnology || defaultTechnology;
      this.setState({
        model,
      }, () => {
        this.saveModelChanges();
      });
    }
    if (defaultTechnology === Constants.MODEL_TECHNOLOGY.USE) {
      defaultText.tensorflow = text;
    } else if (defaultTechnology === Constants.MODEL_TECHNOLOGY.N_GRAM) {
      defaultText.n_gram = text;
    }
    return defaultText;
  }

  render() {
    const {
      isCurrentTab, modelViewReadOnly, onClickDownloadConfig, userFeatureConfiguration, project, currentType, latestTensorflowVersion,
    } = this.props;
    const { names } = featureFlagDefinitions;
    if (!isCurrentTab) {
      return null;
    }
    let radioStyles = {};
    if (modelViewReadOnly === true) {
      radioStyles = { pointerEvents: 'none', opacity: 0.5 };
    }

    const useType = latestTensorflowVersion && latestTensorflowVersion.version || '';
    const { model = {} } = this.state;
    const defaultText = this.getDefaultText();
    let { description = '', modelTechnology, vectorizerTechnology } = model;
    let selectTechnology = modelTechnology || vectorizerTechnology;

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
            <label>Technology</label>
            <div style={{ whiteSpace: 'break-spaces', ...radioStyles }}>
              <RadioGroup
                displayType="horizontal"
                values={[Constants.MODEL_TECHNOLOGY.USE, Constants.MODEL_TECHNOLOGY.N_GRAM]}
                labels={[
                  <span key="tensorflow" style={{ color: '#333333' }}>{Constants.MODEL_TECHNOLOGY.TENSORFLOW_LABEL(useType, defaultText.tensorflow)}</span>,
                  <span key="ngram" style={{ color: '#333333' }}>{Constants.MODEL_TECHNOLOGY.N_GRAM_LABEL(defaultText.n_gram)}</span>,
                ]}
                value={selectTechnology}
                onChange={this.onClickRadioButton}
              />
            </div>
            {this.getCheckbox(modelViewReadOnly)}
          </li>
          <li>
            <label className="configuration-label">Configuration*</label>
            {!modelViewReadOnly
              && (
                <div className="upload-configuration">
                  <Upload
                    {...this.uploaderProps}
                    component="a"
                  >
                    <Button
                      type="flat"
                      name="config-upload-btn"
                      onClick={null}
                      styleOverride={{
                        height: 'auto',
                        color: '#004c97',
                        ':hover': {
                          color: '#003467',
                          boxShadow: 'none',
                        },
                        ':focus': {
                          outline: 'none',
                        },
                      }}
                    >
                        Upload
                    </Button>
                  </Upload>
                  { (isFeatureEnabled(names.modelConfigDownload, userFeatureConfiguration)) ? (
                    <span>
                      <PrimaryDivider fill="#727272" height={12} />
                      <Button
                        type="flat"
                        name="config-download-btn"
                        onClick={onClickDownloadConfig}
                        styleOverride={{
                          height: 'auto',
                          color: '#004c97',
                          ':hover': {
                            color: '#003467',
                            boxShadow: 'none',
                          },
                          ':focus': {
                            outline: 'none',
                          },
                        }}
                      >
                        Download
                      </Button>
                    </span>
                  ) : null }
                </div>
              )
            }
            <div className="config-select">
              {this.getConfigSelect()}
            </div>
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
      </div>
    );
  }
}

NewModel.defaultProps = {
  datasets: {},
  configs: null,
  model: {},
  project: {},
  models: null,
  config: {},
  currentType: {},
  newModelConfig: () => {},
  setValidModel: () => {},
  saveConfigChanges: () => {},
  saveModelChanges: () => {},
  fetchConfigById: () => {},
  tuneModelId: null,
  viewModelId: null,
  modelViewReadOnly: false,
  latestTensorflowVersion: {},
};

NewModel.propTypes = {
  datasets: PropTypes.object,
  configs: PropTypes.object,
  currentType: PropTypes.object,
  model: PropTypes.object,
  models: PropTypes.object,
  project: PropTypes.object,
  config: PropTypes.object,
  newModelConfig: PropTypes.func,
  setValidModel: PropTypes.func,
  saveConfigChanges: PropTypes.func,
  saveModelChanges: PropTypes.func,
  fetchConfigById: PropTypes.func,
  tuneModelId: PropTypes.string,
  viewModelId: PropTypes.string,
  reportError: PropTypes.func,
  reportSuccess: PropTypes.func,
  registerCallbacks: PropTypes.func,
  isCurrentTab: PropTypes.bool,
  modelViewReadOnly: PropTypes.bool,
  latestTensorflowVersion: PropTypes.object,
};

export default NewModel;
