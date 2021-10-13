import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { TextField } from '@tfs/ui-components';
import EditorTags from 'components/modelConfigs/EditorTags';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from 'constants/Constants';
import AceEditor from 'react-ace';
import 'brace/mode/json';
import 'brace/theme/textmate';
import 'brace/theme/kuroir';

class TrainingConfigs extends Component {
  constructor(props) {
    super(props);
    this.onNumEpochsBlur = this.onNumEpochsBlur.bind(this);
    this.onValidationSplitBlur = this.onValidationSplitBlur.bind(this);
    this.onValidationSplitChange = this.onValidationSplitChange.bind(this);
    this.onTrainingConfigChange = this.onTrainingConfigChange.bind(this);
    this.onUpdateTags = this.onUpdateTags.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onChange = this.onChange.bind(this);

    const { names } = featureFlagDefinitions;
    const { userFeatureConfiguration } = props;
    const isInternal = isFeatureEnabled(names.createNewVersionTrainingConfigTab, userFeatureConfiguration);
    this.state = {
      data: props.config ? props.config : { trainingConfigs: { stemmingExceptions: [] } },
      tooltipText: '',
      invalid: false,
      isInternal,
    };
  }

  static getDerivedStateFromProps(nextProps, state) {
    const data = nextProps.config ? nextProps.config : state.data;
    return ({
      data,
    });
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.isCurrentTab) {
      return true;
    }

    return false;
  }

  componentDidMount() {
    const { validationSplit = '' } = this.state.data.trainingConfigs;
    this.isValidEntry(validationSplit);
  }

  onNumEpochsBlur(evt) {
    this.onTrainingConfigChange('numOfEpochs', evt);
  }

  onValidationSplitBlur(evt) {
    this.onTrainingConfigChange('validationSplit', evt);
  }

  onValidationSplitChange(evt) {
    let validationSplit = evt.target.value;
    this.isValidEntry(validationSplit);
  }

  onUpdateTags(tags) {
    const updatedData = Object.assign({}, this.state.data);

    updatedData.trainingConfigs.stemmingExceptions = tags;

    this.setState(updatedData, () => {
      this.props.saveConfigChanges(updatedData);
    });
  }

  onBlur(newValue) {
    try {
      const newConfigs = JSON.parse(this.textContent);
      const updatedData = Object.assign({}, this.state.data);
      updatedData.trainingConfigs = newConfigs;
      this.setState(updatedData, () => {
        this.props.saveConfigChanges(updatedData);
      });
    } catch (err) {
      // eslint-disable-next-line no-console
      console.log(err);
    }
  }

  onChange(newValue) {
    this.textContent = newValue ? newValue.trim() : newValue;
    this.unSavedContent = true;
  }

  onTrainingConfigChange(stateSlice, evt, saveChangesToState = true) {
    evt.preventDefault();
    const value = evt.target.value;

    const updatedData = Object.assign({}, this.state.data);

    updatedData.trainingConfigs[stateSlice] = value;

    this.setState(updatedData, () => {
      if (saveChangesToState) {
        this.props.saveConfigChanges(updatedData);
      }
    });
  }

  isValidEntry(validationSplit) {
    let tooltipText = '';
    let invalid = false;
    const { isInternal } = this.state;
    const { isTrainingConfigsValid } = this.props;
    if (!isNaN(validationSplit) && validationSplit !== '') {
      validationSplit = parseInt(validationSplit);
      if (isInternal) {
        if (validationSplit < Constants.MIN_VALIDATION_SPLIT_INTERNAL || validationSplit > Constants.MAX_VALIDATION_SPLIT_INTERNAL) {
          tooltipText = Constants.VALIDATION_SPLIT_INTERNAL_MSG;
          invalid = true;
        }
      } else if (validationSplit < Constants.MIN_VALIDATION_SPLIT_INTERNAL || validationSplit > Constants.MAX_VALIDATION_SPLIT_EXTERNAL) {
        tooltipText = Constants.VALIDATION_SPLIT_EXTERNAL_MSG;
        invalid = true;
      }
    } else {
      tooltipText = Constants.VALIDATION_SPLIT_NO_DATA_MSG;
      invalid = true;
    }
    this.setState({
      tooltipText,
      invalid,
    }, () => {
      isTrainingConfigsValid(!invalid);
    });
  }

  render() {
    if (!this.props.isCurrentTab) {
      return null;
    }

    const {
      invalid, tooltipText, data, isInternal,
    } = this.state;
    const { trainingConfigs } = data;
    const { modelViewReadOnly } = this.props;
    const content = trainingConfigs ? JSON.stringify(trainingConfigs, null, 2) : '';
    let stemmingExceptions = [];
    let numOfEpochs = '';
    let validationSplit = '';

    if (!_.isNil(trainingConfigs)) {
      stemmingExceptions = trainingConfigs.stemmingExceptions ? trainingConfigs.stemmingExceptions : [];
      if (trainingConfigs.numOfEpochs) {
        numOfEpochs = trainingConfigs.numOfEpochs;
      }

      if (trainingConfigs.validationSplit) {
        validationSplit = trainingConfigs.validationSplit;
      }
    }


    return (
      <div id="TrainingConfigs">
        <div id="training-input">
          <ul className="form-fields">
            <li>
              <div className="title">
                  My Training Configs
              </div>
              {isInternal && <label>Number of Folds per Sigmoid Training</label>}
              {isInternal && (
                <TextField
                  type="text"
                  name="numEpochs"
                  placeholder="numEpochs"
                  defaultValue={numOfEpochs}
                  onBlur={this.onNumEpochsBlur}
                  disabled={modelViewReadOnly}
                  showValidCheck
                  styleOverride={{
                    width: '100%',
                    helpText: {
                      height: '0px',
                    },
                  }}
                  checkmarkProps={{
                    fill: '#004c97',
                  }}
                />
              )}
            </li>
            <li>
              <label>Number of Folds</label>
              <TextField
                type="text"
                name="validationSplit"
                placeholder="validationSplit"
                defaultValue={validationSplit}
                onBlur={this.onValidationSplitBlur}
                onChange={this.onValidationSplitChange}
                invalid={invalid}
                showValidCheck
                tooltipText={tooltipText}
                disabled={modelViewReadOnly}
                styleOverride={{
                  width: '100%',
                  helpText: {
                    height: '0px',
                  },
                }}
                checkmarkProps={{
                  fill: '#004c97',
                }}
              />
            </li>
            <li>
              <div className="title">
                    Stemming Exceptions
              </div>
              <label>Enter new tag</label>
              <div style={{ marginLeft: '-30px' }}>
                <EditorTags
                  transformationItem={stemmingExceptions}
                  onUpdateTags={this.onUpdateTags}
                  modelViewReadOnly={modelViewReadOnly}
                />
              </div>
            </li>
          </ul>
        </div>
        {isInternal && (
          <div id="Editor">
            <div className="title">
            My Training Configs
            </div>
            <div style={{
              height: 'calc(100% - 80px)',
              width: '98%',
              border: '1px solid #ddd',
            }}
            >
              <AceEditor
                mode="json"
                theme={modelViewReadOnly ? 'kuroir' : 'textmate'}
                onBlur={this.onBlur}
                onChange={this.onChange}
                name="UNIQUE_ID_OF_DIV"
                editorProps={{ $blockScrolling: true }}
                showPrintMargin={false}
                defaultValue={content}
                setOptions={{
                  // showLineNumbers: false
                }}
                width="100%"
                height="100%"
                fontSize={14}
                value={content}
                wrapEnabled
                readOnly={modelViewReadOnly}
              />
            </div>
          </div>
        )}
      </div>

    );
  }
}

TrainingConfigs.propTypes = {
  model: PropTypes.object,
  // Pass in only the training config section, not the entire config
  config: PropTypes.object,
  userFeatureConfiguration: PropTypes.object,
  saveConfigChanges: PropTypes.func,
  isTrainingConfigsValid: PropTypes.func,
  isCurrentTab: PropTypes.bool,
  modelViewReadOnly: PropTypes.bool,
};

export default TrainingConfigs;
