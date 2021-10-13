/* eslint-disable react/no-unused-state */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Model from 'model';

import TransformationItems from 'components/modelConfigs/transformations/TransformationItems';
import TransformationEditorContainer from 'components/modelConfigs/transformations/TransformationEditorContainer';
import { saveTransformationToList } from 'model/ModelConfigManager';

class ModelTransformations extends Component {
  constructor(props) {
    super(props);

    this.onDeleteTransformation = this.onDeleteTransformation.bind(this);
    this.onCreateTransformation = this.onCreateTransformation.bind(this);
    this.onUpdateTransformation = this.onUpdateTransformation.bind(this);
    this.onUpdateTransformationItems = this.onUpdateTransformationItems.bind(this);
    this.onSelectItem = this.onSelectItem.bind(this);
    this.onAddPredefined = this.onAddPredefined.bind(this);

    let transformations = [];
    if (props.config && props.config.transformations) {
      transformations = props.config.transformations;
    }
    const currentItem = transformations.length > 0 ? transformations[0] : null;
    this.state = {
      config: props.config || {},
      transformations,
      currentItem,
      resetCurrentItem: false,
      currentIndex: 0,
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.isCurrentTab) {
      return true;
    }
    return false;
  }

  static getDerivedStateFromProps(props, state) {
    let {
      transformations, resetCurrentItem, currentItem, currentIndex,
    } = state;
    const { newConfigSelected, resetNewConfigSelected, config } = props;
    if (config && config.transformations) {
      transformations = config.transformations; // TODO: NT-2969
    }

    if (!currentItem || resetCurrentItem || newConfigSelected) {
      currentItem = transformations[transformations.length - 1];
      currentIndex = transformations.length - 1;
      resetCurrentItem = false;

      if (newConfigSelected) {
        resetNewConfigSelected();
      }
    }

    return {
      transformations,
      config: props.config,
      currentItem,
      currentIndex,
      resetCurrentItem,
    };
  }

  onAddPredefined(itemList) {
    const { transformations } = this.state;
    itemList.forEach((item) => {
      transformations.push(item); // TODO: NT-2969
    });
    this.setState({
      resetCurrentItem: true,
    }, () => {
      this.onUpdateTransformationItems(transformations);
    });
  }

  onCreateTransformation(item) {
    const { transformations } = this.state;
    transformations.push(item); // TODO: NT-2969
    this.setState({
      resetCurrentItem: true,
    }, () => {
      this.onUpdateTransformationItems(transformations);
    });
  }

  onDeleteTransformation(idx) {
    const { transformations } = this.state;
    transformations.splice(idx, 1); // TODO: NT-2969
    this.setState({
      resetCurrentItem: true,
    }, () => {
      this.onUpdateTransformationItems(transformations);
    });
  }

  onSelectItem(currentItem, currentIndex) {
    this.setState({
      currentItem,
      resetCurrentItem: false,
      currentIndex,
    });
  }

  onUpdateTransformation(transformation) {
    const transformations = saveTransformationToList(this.state.transformations, transformation);

    const updatedConfig = {
      ...this.props.config,
      transformations,
    };
    this.props.saveConfigChanges(updatedConfig);
  }

  onUpdateTransformationItems(transformationItems) {
    const updatedConfig = {
      ...this.props.config,
      transformations: transformationItems,
    };
    this.props.saveConfigChanges(updatedConfig);
  }

  render() {
    const {
      modelViewReadOnly, isCurrentTab, userFeatureConfiguration,
      showTransformationAddDialog, showTransformationDeleteDialog,
      showTransformationPredefinedDialog, dispatch, isTransformationValid,
    } = this.props;

    const { config = {}, currentItem, currentIndex } = this.state;
    const transformations = config && config.transformations ? config.transformations : [];

    if (!isCurrentTab) {
      return null;
    }

    return (
      <div id="ModelTransformationContainer">
        <div id="modelTransformationItems">
          <TransformationItems
            transformations={transformations}
            currentItem={currentItem}
            currentIndex={currentIndex}
            onCreateTransformation={this.onCreateTransformation}
            onDeleteTransformation={this.onDeleteTransformation}
            onSelectItem={this.onSelectItem}
            onUpdateTransformationItems={this.onUpdateTransformationItems}
            onAddPredefined={this.onAddPredefined}
            showTransformationPredefinedDialog={showTransformationPredefinedDialog}
            showTransformationAddDialog={showTransformationAddDialog}
            showTransformationDeleteDialog={showTransformationDeleteDialog}
            dispatch={dispatch}
            modelViewReadOnly={modelViewReadOnly}
          />
        </div>
        <div id="modelTransformationEditor">
          <TransformationEditorContainer
            currentItem={currentItem}
            onUpdateTransformation={this.onUpdateTransformation}
            dispatch={dispatch}
            userFeatureConfiguration={userFeatureConfiguration}
            modelViewReadOnly={modelViewReadOnly}
            isTransformationValid={isTransformationValid}
          />
        </div>
        <div id="nextButtonArea" />
      </div>
    );
  }
}

ModelTransformations.propTypes = {
  model: PropTypes.object,
  config: PropTypes.object,
  saveConfigChanges: PropTypes.func,
  showTransformationDeleteDialog: PropTypes.bool,
  showTransformationAddDialog: PropTypes.bool,
  showTransformationPredefinedDialog: PropTypes.bool,
  dispatch: PropTypes.func,
  isCurrentTab: PropTypes.bool,
  userFeatureConfiguration: PropTypes.object,
  modelViewReadOnly: PropTypes.bool,
};

const mapStateToProps = (state, ownProps) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  const { tuneModelId, viewModelId } = state.projectsManager;
  const modelId = tuneModelId || viewModelId;
  const model = modelId
    ? Model.ProjectsManager.getModel(projectId, modelId)
    : state.projectsManager.model;
  const createNewModel = state.projectsManager.model;
  return {
    config: state.config.config,
    modelViewReadOnly: state.projectsManager.modelViewReadOnly,
    showModelNavigationConfirmationDialog: state.projectsManager.showModelNavigationConfirmationDialog,
    showTransformationDeleteDialog: state.config.showTransformationDeleteDialog,
    showTransformationAddDialog: state.config.showTransformationAddDialog,
    showTransformationPredefinedDialog: state.config.showTransformationPredefinedDialog,
    isTransformationValid: state.config.isTransformationValid,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ModelTransformations);
