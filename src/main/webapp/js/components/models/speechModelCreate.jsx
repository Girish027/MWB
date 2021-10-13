import React, { Component } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import { featureFlagDefinitions } from 'utils/FeatureFlags';
import { RouteNames } from 'utils/routeHelpers';
import { displayBadRequestMessage, displayGoodRequestMessage, changeRoute } from 'state/actions/actions_app';
import { convertTransformationTypeIfNeeded } from 'components/modelConfigs/transformations/transformationTypes';
import * as actionsStats from 'state/actions/actions_datasets_transformed_stats';
import Model from 'model';
import {
  modelEditUpdate, clearModelData, tuneSelectedModel,
  showModelNavigationConfirmationDialog, submitModel,
} from 'state/actions/actions_models';
import {
  createModelConfig, configEditUpdate,
  submitConfigAndModelForSpeech, fetchConfigById,
  convertToDigitalConfig, updateTrainingConfigValidity,
} from 'state/actions/actions_configs';
import NewModel from 'components/models/NewSpeechModel';
import Constants from 'constants/Constants';
import * as appActions from 'state/actions/actions_app';
import * as headerActions from 'state/actions/actions_header';
import { getLanguage } from 'state/constants/getLanguage';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

export class speechModelCreate extends Component {
  constructor(props) {
    super(props);

    this.getWordUpload = this.getWordUpload.bind(this);
    this.saveModelChanges = this.saveModelChanges.bind(this);
    this.cleanup = this.cleanup.bind(this);
    this.reportError = this.reportError.bind(this);
    this.buildModelWithConfig = this.buildModelWithConfig.bind(this);
    this.confirmModelWithConfig = this.confirmModelWithConfig.bind(this);
    this.reportSuccess = this.reportSuccess.bind(this);
    this.onFileLoaded = this.onFileLoaded.bind(this);
    this.setValidModel = this.setValidModel.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
    this.tuneModel = this.tuneModel.bind(this);
    this.renderActionItem = this.renderActionItem.bind(this);
    this.loadingProject = false;
    this.loadingDatasets = false;
    this.loadingModels = false;
    this.doNotSave = false;
    this.newActionItem = [];


    const { model } = props;

    this.state = {
      showTestRegex: false,
      datasets: Model.ProjectsManager.getDatasetsByProjectId(props.projectId, true) || null,
      models: Model.ProjectsManager.getModelsByProjectId(props.projectId) || null,
      validModel: false || (model && model.name && model.datasetIds),
      versionActionEnabled: true,
      wordClassFile: {},
      isUnbundled: true,
    };

    this.buttonStyle = {
      marginTop: '-10px',
      paddingLeft: '25px',
      paddingRight: '25px',
    };
  }

  componentWillUnmount() {
    const { dispatch } = this.props;
    dispatch(headerActions.setActionItems([]));
    dispatch(actionsStats.resetDatasetValidationStats());
  }

  setValidModel(validModel) {
    this.setState({
      validModel,
    });
  }

  handleCancel() {
    const {
      dispatch, history, clientId,
    } = this.props;
    dispatch(changeRoute(RouteNames.PROJECTS, { clientId }, history));
  }

  static getDerivedStateFromProps(props, state) {
    const { projectId } = props;
    return {
      datasets: Model.ProjectsManager.getDatasetsByProjectId(projectId, true) || null,
      models: Model.ProjectsManager.getModelsByProjectId(projectId) || null,
    };
  }

  reportError(err) {
    const { dispatch } = this.props;
    dispatch(displayBadRequestMessage(err.message));
  }

  reportSuccess(message) {
    const { dispatch } = this.props;
    dispatch(displayGoodRequestMessage(message));
  }

  cleanup() {
    const { dispatch } = this.props;
    dispatch(clearModelData());
    dispatch(showModelNavigationConfirmationDialog(false));
  }

  saveModelChanges(model) {
    const { dispatch } = this.props;
    if (!this.doNotSave) {
      dispatch(modelEditUpdate(model));
    }
  }

  onFileLoaded(wordClassFile = {}, isValidFile) {
    this.setState({
      wordClassFile,
      isValidFile,
    });
  }

  tuneModel() {
    const {
      model, projectId, history, client, dispatch,
    } = this.props;

    dispatch(tuneSelectedModel(model));
    dispatch(changeRoute(RouteNames.TUNESPEECHMODEL, {
      client, projectId,
    }, history));
  }


  buildModelWithConfig() {
    const {
      dispatch, model,
    } = this.props;
    const { wordClassFile, isUnbundled } = this.state;

    if (model) {
      model.startBuild = true;
      model.wordClassFile = wordClassFile;
      model.isUnbundled = isUnbundled;
      model.modelType = 'SPEECH';

      const data = {
        model: {
          userId: this.props.userId,
          csrfToken: this.props.csrfToken,
          projectId: this.props.projectId,
          ...model,
          modelType: 'SPEECH',
          selectedDatasets: model.datasetIds,
          store: this.props.store,
          history: this.props.history,
          clientId: this.props.client.id,
        },
      };

      dispatch(appActions.modalDialogChange(null));
      if (JSON.stringify(wordClassFile) === '{}') {
        dispatch(submitModel(data.model));
      } else {
        dispatch(submitConfigAndModelForSpeech(data, 'SPEECH'));
      }
      dispatch(displayGoodRequestMessage(DISPLAY_MESSAGES.buildModelRequest));
    }
  }

  confirmModelWithConfig() {
    this.setState({ versionActionEnabled: false });
    const { dispatch, isDatasetsTagged } = this.props;

    !isDatasetsTagged
      ? dispatch(appActions.modalDialogChange({
        dispatch,
        type: Constants.DIALOGS.PROGRESS_DIALOG,
        message: Constants.BUILD_MODEL_CONFIRMATION_MESSAGE,
        header: Constants.CREATE_VERSION,
        showHeader: true,
        showFooter: true,
        cancelVisible: true,
        okVisible: true,
        showSpinner: false,
        closeIconVisible: true,
        cancelChildren: Constants.CLOSE,
        okChildren: Constants.BUILD_VERSION,
        onOk: this.buildModelWithConfig,
        styleOverride: {
          childContainer: {
            marginTop: '30px',
            marginBottom: '10px',
          },
          content: {
            top: '160px',
            maxWidth: '380px',
            maxHeight: '260px',
            left: 'calc((100vw - 500px) / 2)',
          },
        },
      }))
      : this.buildModelWithConfig();
  }

  getWordUpload() {
    const {
      models, datasets,
    } = this.state;
    const {
      dispatch, userFeatureConfiguration,
      modelViewReadOnly, model,
    } = this.props;

    return (
      <NewModel
        model={model}
        models={models}
        datasets={datasets}
        onChange={this.onFileLoaded}
        saveModelChanges={this.saveModelChanges}
        setValidModel={this.setValidModel}
        dispatch={dispatch}
        modelViewReadOnly={modelViewReadOnly}
        project={this.props.project}
        reportError={this.reportError}
        reportSuccess={this.reportSuccess}
        userFeatureConfiguration={userFeatureConfiguration}
      />
    );
  }

  renderActionItem() {
    const {
      dispatch, actionItems,
      isDatasetsValid, viewModelId,
    } = this.props;

    const { validModel, versionActionEnabled } = this.state;
    let validModelNew = validModel && versionActionEnabled;

    this.newActionItem = [{
      type: 'flat',
      label: Constants.CANCEL.toUpperCase(),
      onClick: this.handleCancel,
      styleOverride: {
        button: {
          padding: '0px 10px',
        },
      },
    }];
    if (viewModelId) {
      this.newActionItem.push({
        name: 'tune-version',
        type: 'primary',
        label: Constants.TUNE_VERSION,
        onClick: this.tuneModel,
        styleOverride: {
          button: this.buttonStyle,
        },
        isDisabled: !validModelNew,
      });
    } else {
      this.newActionItem.push({
        name: 'build-version',
        type: 'primary',
        label: Constants.BUILD_VERSION,
        onClick: this.confirmModelWithConfig,
        styleOverride: {
          button: this.buttonStyle,
        },
        isDisabled: !validModelNew,
      });
    }

    if (JSON.stringify(actionItems) !== JSON.stringify(this.newActionItem)) {
      dispatch(headerActions.setActionItems(this.newActionItem));
    }
  }

  render() {
    this.renderActionItem();

    return (
      <div id="CreateModelWizardPage">
        <div id="modelCreateContent">
          {this.getWordUpload()}
        </div>
      </div>
    );
  }
}
const mapStateToProps = (state, ownProps) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  const { tuneModelId, viewModelId } = state.projectsManager;
  const { isDatasetsValid, isDatasetsTagged } = state.tagDatasets.validDatasetsStats;
  const modelId = tuneModelId || viewModelId;
  const model = modelId
    ? Model.ProjectsManager.getModel(projectId, modelId)
    : state.projectsManager.model;
  const createNewModel = state.projectsManager.model;
  const { datasetsIntents = [] } = state.tagDatasets.validDatasetsStats;
  return {
    csrfToken: state.app.csrfToken,
    userId: state.app.userId,
    projectId,
    isDatasetsTagged,
    modelViewReadOnly: state.projectsManager.modelViewReadOnly,
    isDatasetsValid,
    project: Model.ProjectsManager.getProject(projectId) || {},
    client: state.header.client,
    clientId: state.header.client.id,
    model,
    viewModelId,
    actionItems: state.header.actionItems,
    options: datasetsIntents,
    showModelNavigationConfirmationDialog: state.projectsManager.showModelNavigationConfirmationDialog,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(speechModelCreate);
