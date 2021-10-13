import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import 'brace/ext/searchbox';

import ModelReviewAce from 'components/models/ModelReviewAce';
import {
  Pencil,
  Checkmark,
  ContextualActionsBar,
  ContextualActionItem,
  ContextualActionInfo,
} from '@tfs/ui-components';
import { actionBarStyles } from 'styles';
import Constants from '../../constants/Constants';

class ModelReview extends Component {
  constructor(props) {
    super(props);
    this.onLoad = this.onLoad.bind(this);
    this.updateDimensions = this.updateDimensions.bind(this);
    this.getSelectedDatasetsList = this.getSelectedDatasetsList.bind(this);
    this.onEnableEdit = this.onEnableEdit.bind(this);
    this.onDisableEdit = this.onDisableEdit.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.getDatasetsLabel = this.getDatasetsLabel.bind(this);
    this.getEditorStatusMessage = this.getEditorStatusMessage.bind(this);

    this.windowHeightOffset = 200;
    this.windowWidthOffset = 340;
    this.textContent = props.config ? JSON.stringify(props.config, null, 2) : '';

    this.state = {
      width: 200,
      height: 200,
      readOnly: true,
      hasError: false,
      unSavedContent: false,
    };
  }

  onLoad(data) { }

  onChange(newValue) {
    this.textContent = newValue ? newValue.trim() : newValue;
    try {
      const updatedConfig = JSON.parse(this.textContent);
      this.setState({
        hasError: false,
        unSavedContent: true,
      });
    } catch (error) {
      this.setState({
        hasError: true,
        unSavedContent: true,
      });
    }
  }

  onBlur(newValue) {
    let updatedConfig;
    try {
      updatedConfig = JSON.parse(this.textContent);
      this.setState({
        hasError: false,
      }, () => {
        this.props.saveConfigChanges(updatedConfig);
      });
    } catch (error) {
      this.setState({
        hasError: true,
      });
    }
  }

  updateDimensions() {
    this.setState({
      width: window.innerWidth - this.windowWidthOffset,
      height: window.innerHeight - this.windowHeightOffset,
    });
  }

  getEditorStatusMessage() {
    let message = '';
    let messageStyle = {};
    if (this.state.hasError) {
      message = 'Please fix errors in the configuration';
      messageStyle.color = '#C81919';
      messageStyle.fontStyle = 'italic';
    }
    if (!this.state.readOnly) {
      message = 'Configuration is in edit mode.  Select "DONE" to return to Read Only view';
      messageStyle = {};
    }
    return message ? (
      <ContextualActionInfo
        label="Message:"
        value={message}
        styleOverride={{
          label: {
            margin: '24px 30px',
          },
          value: messageStyle,
        }}
      />
    ) : null;
  }

  getDatasetsLabel() {
    return (
      <div>
        Datasets
        <div className="help-text overflow-wrap">
          Select datasets that have been tagged with at least two intents and that include multiple transcriptions per intent.
        </div>
      </div>
    );
  }

  /**
       * Add event listener
       */
  componentDidMount() {
    this.updateDimensions();
    window.addEventListener('resize', this.updateDimensions);
  }

  /**
       * Remove event listener
       */
  componentWillUnmount() {
    window.removeEventListener('resize', this.updateDimensions);
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.isCurrentTab) {
      return true;
    }
    return false;
  }

  getSelectedDatasetsList() {
    const { model, datasets } = this.props;
    const datasetIds = model ? model.datasetIds : [];
    const datasetList = [];
    if (datasetIds && datasets) {
      datasetIds.forEach((item) => {
        const datasetItem = datasets.get(item);
        if (datasetItem) {
          const name = datasetItem.name;
          const id = datasetItem.id;
          datasetList.push(
            <li key={id}>
              {name}
            </li>,
          );
        }
      });
    }

    return (
      <ul className="dataset-list">
        {' '}
        {datasetList}
        {' '}
      </ul>
    );
  }

  onEnableEdit() {
    this.setState({
      readOnly: false,
    });
  }

  onDisableEdit() {
    this.setState({
      readOnly: true,
    });
  }

  renderMetaData = (metadata) => {
    const metadataArray = [];
    Object.keys(metadata).forEach((key) => {
      metadataArray.push((
        <li key={key}>
          <label>{key}</label>
          <div style={{ color: '#313f54' }}>
            {' '}
            {metadata[key]}
          </div>
        </li>
      ));
    });
    return metadataArray;
  }

  renderEditorActions = () => (
    <ContextualActionsBar styleOverride={actionBarStyles.bar}>
      <ContextualActionItem
        onClickAction={this.onEnableEdit}
        styleOverride={actionBarStyles.item}
      >
        <Pencil
          width="10px"
          height="10px"
          fill="none"
          stroke="#004C97"
        />
        <span> EDIT</span>
      </ContextualActionItem>
      <ContextualActionItem
        onClickAction={this.onDisableEdit}
        styleOverride={actionBarStyles.item}
      >
        <Checkmark />
        <span> DONE</span>
      </ContextualActionItem>
      {this.getEditorStatusMessage()}
    </ContextualActionsBar>
  );

  render() {
    if (!this.props.isCurrentTab) {
      return null;
    }

    let { config, model, modelViewReadOnly } = this.props;

    if (!this.state.unSavedContent || this.textContent.length === 0) {
      this.textContent = config ? JSON.stringify(config, null, 2) : '';
    }

    let numTransformations = 0;
    let postProcessingRules = '{}';
    let numPostProcessingRules = 0;
    if (!_.isNil(config)) {
      if (!_.isNil(config.transformations)) {
        let configTransforms = this.props.convertTransformTypes(config);
        numTransformations = configTransforms.transformations ? configTransforms.transformations.length : 0;
      }
      if (!_.isNil(config.postProcessingRules)) {
        numPostProcessingRules = config.postProcessingRules ? config.postProcessingRules.length : 0;
      }
    }

    const name = model ? model.name : '';
    const description = model ? model.description : '';

    return (
      <div id="ModelReview">
        <div className="metadata">
          <div className="title">
              Info
          </div>
          <ul>
            {this.renderMetaData({
              'Name*': name,
              Description: description || 'NA',
              'Pre Processing Transformations': numTransformations,
              'Post Processing Rules': numPostProcessingRules,
              Datasets: this.getSelectedDatasetsList(),
            })}
          </ul>
        </div>
        <div className="content">
          <div className="title">
              Configurations
          </div>
          {!modelViewReadOnly && this.renderEditorActions()}
          <ModelReviewAce
            name="model-review"
            onLoad={this.onLoad}
            onChange={this.onChange}
            onBlur={this.onBlur}
            value={this.textContent}
            height={this.state.height}
            width={this.state.width}
            readOnly={this.state.readOnly}
          />
        </div>
      </div>
    );
  }
}

ModelReview.propTypes = {
  datasets: PropTypes.object,
  config: PropTypes.object,
  model: PropTypes.object,
  saveData: PropTypes.func,
  saveConfigChanges: PropTypes.func,
  convertTransformTypes: PropTypes.func,
  isCurrentTab: PropTypes.bool,
  modelViewReadOnly: PropTypes.bool,
};

export default ModelReview;
