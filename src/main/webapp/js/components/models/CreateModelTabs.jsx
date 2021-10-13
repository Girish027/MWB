import React, { Component } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import store from 'state/configureStore';
import * as preferencesActions from 'state/actions/actions_preferences';
import NavigationPrompt from 'react-router-navigation-prompt';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import { RouteNames } from 'utils/routeHelpers';
import { displayBadRequestMessage, displayGoodRequestMessage, changeRoute } from 'state/actions/actions_app';
import { convertTransformationTypeIfNeeded } from 'components/modelConfigs/transformations/transformationTypes';
import Dialog from 'components/common/Dialog';
import * as appActions from 'state/actions/actions_app';
import * as actionsStats from 'state/actions/actions_datasets_transformed_stats';
import Model from 'model';
import {
  createModelConfig, configEditUpdate,
  submitConfigAndModel, fetchConfigById,
  convertToDigitalConfig, updateTrainingConfigValidity,
} from 'state/actions/actions_configs';
import {
  modelEditUpdate, clearModelData, tuneSelectedModel,
  showModelNavigationConfirmationDialog,
} from 'state/actions/actions_models';
import NewModel from 'components/models/NewModel';
import ModelTransformations from 'components/modelConfigs/transformations/ModelTransformations';
import PostProcessingTransformations from 'components/modelConfigs/PostProcessingTransformations';
import ModelReview from 'components/models/ModelReview';
import TrainingConfigs from 'components/modelConfigs/TrainingConfigs';
import Constants from 'constants/Constants';
import * as headerActions from 'state/actions/actions_header';
import { Button, Next, Tabs } from '@tfs/ui-components';
import { getLanguage } from 'state/constants/getLanguage';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

const downloadFile = (text, name, type) => {
  const file = new Blob([text], { type });
  const isIE = /* @cc_on!@ */false || !!document.documentMode;
  if (isIE) {
    window.navigator.msSaveOrOpenBlob(file, name);
  } else {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(file);
    a.download = name;
    a.click();
  }
};

export class CreateModelTabsComponent extends Component {
  constructor(props) {
    super(props);

    this.getTabs = this.getTabs.bind(this);
    this.onClickNext = this.onClickNext.bind(this);
    this.newModelConfig = this.newModelConfig.bind(this);
    this.getNextNavControl = this.getNextNavControl.bind(this);
    this.saveConfigChanges = this.saveConfigChanges.bind(this);
    this.isTrainingConfigsValid = this.isTrainingConfigsValid.bind(this);
    this.saveModelChanges = this.saveModelChanges.bind(this);
    this.onClickDownloadConfig = this.onClickDownloadConfig.bind(this);
    this.fetchConfigById = this.fetchConfigById.bind(this);
    this.buildModelWithConfig = this.buildModelWithConfig.bind(this);
    this.confirmModelWithConfig = this.confirmModelWithConfig.bind(this);
    this.shouldShowNavigationConfirmationDialog = this.shouldShowNavigationConfirmationDialog.bind(this);
    this.cleanup = this.cleanup.bind(this);
    this.reportError = this.reportError.bind(this);
    this.reportSuccess = this.reportSuccess.bind(this);
    this.registerCallbacks = this.registerCallbacks.bind(this);
    this.convertTransformTypes = this.convertTransformTypes.bind(this);
    this.tuneModel = this.tuneModel.bind(this);
    this.setValidModel = this.setValidModel.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
    this.renderActionItem = this.renderActionItem.bind(this);
    this.loadingProject = false;
    this.loadingDatasets = false;
    this.loadingConfigs = false;
    this.loadingModels = false;
    this.doNotSave = false;

    // Dialog should appear in the following cases
    // ~ User is past New Model Step
    // ~ User returns to the New Model step after visiting other steps
    //     note:  Not checking if there were changes made
    // Dialog should not appear in the following cases
    // ~ User hasn't gone past the New Model step
    // ~ User has requested Build Model and the app is automatically
    //    redirecting the user to the Models view
    this.shouldShowNavConfirmDialog = false;

    this.tabIds = Constants.CREATE_MODEL_TABS;

    this.callbacks = {};

    this.newActionItem = [];
    this.newcreateNewModel = '';
    this.newConfig = '';

    const { model } = props;

    this.state = {
      showDownloadConfig: false,
      showTestRegex: false,
      activeTab: this.tabIds.basicInfo,
      lastTab: this.tabIds.basicInfo,
      datasets: Model.ProjectsManager.getDatasetsByProjectId(props.projectId, true) || null,
      models: Model.ProjectsManager.getModelsByProjectId(props.projectId) || null,
      configs: Model.ProjectsManager.getConfigsByProjectId(props.projectId) || null,
      newConfigSelected: true,
      validModel: false || (model && model.name && model.datasetIds),
      versionActionEnabled: true,
      hideModelTransformationsTab: false,
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

  registerCallbacks(callbackData) {
    this.callbacks[callbackData.tabName] = callbackData;
  }

  handleCancel() {
    const {
      dispatch, history, clientId,
    } = this.props;

    // routing back to project view page
    // method cleanup() and navigationPrompt logic takes care of clearing model data
    dispatch(clearModelData());
    dispatch(changeRoute(RouteNames.PROJECTS, { clientId }, history));
  }

  static getDerivedStateFromProps(props, state) {
    const { projectId } = props;
    return {
      datasets: Model.ProjectsManager.getDatasetsByProjectId(projectId, true) || null,
      models: Model.ProjectsManager.getModelsByProjectId(projectId) || null,
      configs: Model.ProjectsManager.getConfigsByProjectId(projectId) || null,
    };
  }

  onTabSelected = (tab, selectedTabIndex) => {
    let showDownloadConfig = false;
    let showTestRegex = false;
    let shouldShowNavConfirmDialog = false;

    if (!_.isNil(this.callbacks[this.state.activeTab])
      && !_.isNil(this.callbacks[this.state.activeTab].onLeave)) {
      this.callbacks[this.state.activeTab].onLeave();
    }

    if (tab !== this.tabIds.overview) {
      showDownloadConfig = true;
      if (!this.props.modelViewReadOnly) {
        shouldShowNavConfirmDialog = true;
      }
    } else if (this.props.config) {
      // User has returned to the first step
      // As there might have been changes to the configuration,
      // show warning if they navigate away and show the download option
      if (!this.props.modelViewReadOnly) {
        shouldShowNavConfirmDialog = true;
      }
      showDownloadConfig = true;
    }

    if (tab !== this.tabIds.overview && tab !== this.tabIds.basicInfo) {
      showTestRegex = true;
    }

    this.shouldShowNavConfirmDialog = shouldShowNavConfirmDialog;

    this.setState({
      showDownloadConfig,
      showTestRegex,
      activeTab: tab,
      lastTab: this.state.activeTab,
    });
  }

  onClickNext() {
    let nextTab = '';

    const { hideModelTransformationsTab } = this.state;

    switch (this.state.activeTab) {
    case this.tabIds.basicInfo:
      nextTab = hideModelTransformationsTab ? this.tabIds.postProcessing : this.tabIds.transformations;
      break;
    case this.tabIds.transformations:
      nextTab = this.tabIds.postProcessing;
      break;
    case this.tabIds.postProcessing:
      nextTab = this.tabIds.trainingConfigs;
      break;
    case this.tabIds.trainingConfigs:
      nextTab = this.tabIds.overview;
      break;
    default:
      nextTab = this.tabIds.overview;
    }

    this.onTabSelected(nextTab);
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

  fetchConfigById(config) {
    const { dispatch } = this.props;
    if (!this.doNotSave && !_.isNil(config)) {
      dispatch(fetchConfigById(config.id));
    }
  }

  tuneModel() {
    const {
      model, projectId, history, client, dispatch, clientId,
    } = this.props;

    this.setState({
      activeTab: this.tabIds.basicInfo,
      lastTab: this.tabIds.basicInfo,
    }, () => {
      dispatch(tuneSelectedModel(model));
      dispatch(changeRoute(RouteNames.TUNEMODEL, {
        client, projectId,
      }, history));
    });
  }

  convertTransformTypes(config) {
    const transformations = !_.isNil(config) ? config.transformations : null;

    if (!_.isNil(transformations)) {
      transformations.forEach((item) => {
        if (typeof item !== 'string') {
          const transformName = (Object.keys(item))[0];
          item[transformName].type = convertTransformationTypeIfNeeded(item[transformName].type);
        }
      });
    }

    return config;
  }

  onClickDownloadConfig() {
    const { selectedConfig } = this.state;
    downloadFile(JSON.stringify(this.props.config, null, 2), `${selectedConfig.name}.json`, 'text/json');
  }

  saveConfigChanges(config) {
    const { dispatch } = this.props;
    if (!this.doNotSave) {
      const modelName = this.props.createNewModel ? this.props.createNewModel.name : '';
      if (!_.isEmpty(modelName)) {
        config.name = `${modelName}-cfg`;
      }
      dispatch(configEditUpdate(config));
    }
  }

  isTrainingConfigsValid(isTrainingConfigsValid) {
    const { dispatch } = this.props;
    dispatch(updateTrainingConfigValidity(isTrainingConfigsValid));
  }

  onNewConfigSelect(selectedConfig) {
    this.setState({
      newConfigSelected: true,
      selectedConfig,
    });
  }

  resetNewConfigSelected() {
    this.setState({
      newConfigSelected: false,
    });
  }

  newModelConfig(config) {
    createModelConfig(
      config,
      this.props.projectId,
      this.props.csrfToken,
      this.props.client.id,
    );
  }

  buildModelWithConfig() {
    const {
      dispatch, model, createNewModel, config: inputConfig, projectId,
    } = this.props;
    const config = this.convertTransformTypes(inputConfig);
    this.shouldShowNavConfirmDialog = false;

    if (model && config) {
      let state = store.getState();
      let updatedModel = state.projectsManager.model;
      if ((updatedModel) && (updatedModel.id == model.id)) {
        model.modelTechnology = updatedModel.modelTechnology;
        model.toDefault = updatedModel.toDefault;
        model.datasetIds = updatedModel.datasetIds;
      }
      model.startBuild = true;
      config.name = `${model.name}-cfg`;

      const data = {
        config,
        model: {
          userId: this.props.userId,
          csrfToken: this.props.csrfToken,
          projectId: this.props.projectId,
          ...model,
          selectedDatasets: model.datasetIds,
          store: this.props.store,
          history: this.props.history,
          clientId: this.props.client.id,
          description: createNewModel.description,
        },
      };

      // currently we support Speech Model only through ADD SPEECH flow.
      // convert all configs to Digital(uploaded or otherwise)
      dispatch(appActions.modalDialogChange(null));
      dispatch(convertToDigitalConfig());
      dispatch(submitConfigAndModel(data));
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
        showHeader: false,
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

  shouldShowNavigationConfirmationDialog(currLocation, nextLocation) {
    let shouldShow = false;
    const { dispatch, modelViewReadOnly } = this.props;
    if (modelViewReadOnly) {
      return shouldShow;
    }
    if (this.shouldShowNavConfirmDialog
      && (!nextLocation.pathname.startsWith(currLocation.pathname))) {
      shouldShow = true;
    }
    if (!shouldShow) {
      this.doNotSave = true;
      dispatch(clearModelData());
    }

    dispatch(showModelNavigationConfirmationDialog(shouldShow));
    return shouldShow;
  }


  getNextNavControl() {
    return (
      <div
        style={{
          position: 'fixed',
          right: '30px',
          bottom: '30px',
        }}
        onClick={this.onClickNext}
      >
        <Next />
      </div>
    );
  }

  getTabs(data) {
    const {
      basicInfo, transformations, postProcessing, trainingConfigs, overview,
    } = this.tabIds;
    const {
      activeTab, models, configs, datasets, newConfigSelected, hideModelTransformationsTab,
    } = this.state;
    const {
      dispatch, userFeatureConfiguration, tuneModelId, viewModelId,
      modelViewReadOnly, config, model, options, currentType, latestTensorflowVersion,
    } = this.props;
    const { names } = featureFlagDefinitions;
    let technology;

    const newModelTab = (
      <NewModel
        key={basicInfo}
        currentType={currentType}
        config={config}
        model={model}
        models={models}
        configs={configs}
        datasets={datasets}
        newModelConfig={this.newModelConfig}
        saveConfigChanges={this.saveConfigChanges}
        saveModelChanges={this.saveModelChanges}
        onClickDownloadConfig={this.onClickDownloadConfig}
        setValidModel={this.setValidModel}
        fetchConfigById={this.fetchConfigById}
        dispatch={dispatch}
        tuneModelId={tuneModelId}
        viewModelId={viewModelId}
        modelViewReadOnly={modelViewReadOnly}
        project={this.props.project}
        reportError={this.reportError}
        reportSuccess={this.reportSuccess}
        tabName={basicInfo}
        registerCallbacks={this.registerCallbacks}
        isCurrentTab={activeTab === basicInfo}
        onNewConfigSelect={this.onNewConfigSelect.bind(this)}
        userFeatureConfiguration={userFeatureConfiguration}
        latestTensorflowVersion={latestTensorflowVersion}
      />
    );

    const ModelTransformationsTab = (
      <ModelTransformations
        key={transformations}
        model={model}
        saveConfigChanges={this.saveConfigChanges}
        tabName={transformations}
        registerCallbacks={this.registerCallbacks}
        isCurrentTab={activeTab === transformations}
        userFeatureConfiguration={userFeatureConfiguration}
        modelViewReadOnly={modelViewReadOnly}
        newConfigSelected={newConfigSelected}
        resetNewConfigSelected={this.resetNewConfigSelected.bind(this)}
      />
    );

    const PostProcessingTransformationsTab = (
      <PostProcessingTransformations
        key={postProcessing}
        config={config}
        model={model}
        saveConfigChanges={this.saveConfigChanges}
        tabName={postProcessing}
        registerCallbacks={this.registerCallbacks}
        isCurrentTab={activeTab === postProcessing}
        modelViewReadOnly={modelViewReadOnly}
        options={options}
      />
    );

    const trainingConfigsTab = (
      <TrainingConfigs
        key={trainingConfigs}
        config={config}
        model={model}
        saveConfigChanges={this.saveConfigChanges}
        isTrainingConfigsValid={this.isTrainingConfigsValid}
        tabName={trainingConfigs}
        userFeatureConfiguration={userFeatureConfiguration}
        registerCallbacks={this.registerCallbacks}
        isCurrentTab={activeTab === trainingConfigs}
        modelViewReadOnly={modelViewReadOnly}
      />
    );

    const ModelReviewTab = (
      <ModelReview
        key={overview}
        config={config}
        model={model}
        datasets={datasets}
        saveConfigChanges={this.saveConfigChanges}
        tabName={overview}
        registerCallbacks={this.registerCallbacks}
        convertTransformTypes={this.convertTransformTypes}
        isCurrentTab={activeTab === overview}
        modelViewReadOnly={modelViewReadOnly}
      />
    );

    let tabPanels = [
      newModelTab,
      ModelTransformationsTab,
      PostProcessingTransformationsTab,
      trainingConfigsTab,
    ];

    let tabs = Object.values(Constants.CREATE_INIT_MODEL_TABS);

    if (isFeatureEnabled(names.createNewVersionModelReviewTab, userFeatureConfiguration)) {
      tabPanels.push(ModelReviewTab);
      tabs.push(Constants.CREATE_MODEL_TABS_OVERVIEW_TAB);
    }

    if (model) {
      let state = store.getState();
      let updatedModel = state.projectsManager.model;
      const { modelTechnology, vectorizerTechnology } = model;
      if (modelViewReadOnly) {
        technology = vectorizerTechnology || modelTechnology;
      } else {
        technology = (updatedModel && updatedModel.modelTechnology) || vectorizerTechnology || modelTechnology;
      }
    }
    if (technology === Constants.MODEL_TECHNOLOGY.USE) {
      tabPanels.splice(1, 1);
      tabs.splice(1, 1);
      if (!hideModelTransformationsTab) {
        this.setState({
          hideModelTransformationsTab: true,
        });
      }
    } else if (technology === Constants.MODEL_TECHNOLOGY.N_GRAM) {
      if (hideModelTransformationsTab) {
        this.setState({
          hideModelTransformationsTab: false,
        });
      }
    }

    return (
      <Tabs
        align="center"
        tabs={tabs}
        onTabSelected={this.onTabSelected}
        selectedIndex={tabs.findIndex((tab) => tab === activeTab)}
        forceRenderTabPanel={false}
        tabPanels={tabPanels}
        styleOverride={{
          tabItem: {
            padding: '23px 50px 21px',
          },
          tabContainer: {
            borderTop: 'none',
          },
        }}
      />
    );
  }

  renderActionItem() {
    const {
      dispatch, viewModelId, tuneModelId, actionItems,
      createNewModel, config, isDatasetsValid, isConfigsValid, model,
    } = this.props;
    let modelType = model ? model.modelType : undefined;
    const { validModel, versionActionEnabled } = this.state;
    let validModelNew = validModel && isDatasetsValid && isConfigsValid && versionActionEnabled;

    if (viewModelId) {
      validModelNew = validModel && isConfigsValid;
    }

    this.newActionItem = [{
      type: 'flat',
      label: Constants.CANCEL.toUpperCase(),
      onClick: this.handleCancel,
      styleOverride: {
        button: {
          padding: '0px 10px',
          ':focus': {
            outline: 'none',
          },
        },
      },
    }];

    if (viewModelId) {
      if (modelType !== Constants.DIGITAL_SPEECH_MODEL) {
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
      }
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

    if (JSON.stringify(actionItems) !== JSON.stringify(this.newActionItem) || createNewModel !== this.newcreateNewModel || this.newConfig !== config) {
      dispatch(headerActions.setActionItems(this.newActionItem));
    }
    this.newcreateNewModel = createNewModel;
    this.newConfig = config;
  }

  render() {
    const { activeTab } = this.state;
    const { names } = featureFlagDefinitions;
    const { userFeatureConfiguration } = this.props;
    this.renderActionItem();

    return (
      <div id="CreateModelWizardPage">
        <div id="modelCreateContent">
          {this.getTabs()}
        </div>
        {activeTab === this.tabIds.overview
          ? (null)
          : (
            <div id="modelCreateFooter">
              {this.getNextNavControl()}
            </div>
          )
        }
        <NavigationPrompt
          afterConfirm={this.cleanup}
          // Children will be rendered even if props.when is falsey and isActive is false:
          renderIfNotActive
          // Confirm navigation if going to a path that does not start with current path:
          when={this.shouldShowNavigationConfirmationDialog}
        >
          {({ isActive, onCancel, onConfirm }) => {
            if (isActive) {
              return (
                <Dialog
                  title="Unsaved Changes"
                  visible={this.props.showModelNavigationConfirmationDialog}
                  okString="Continue"
                  onCancel={onCancel}
                  onOk={onConfirm}
                  icon="images/icons/alertIcon.svg"
                >
                  <div className="navigate-warning-content">
                    <div>
                      <p>Changes will be lost.  Continue?</p>
                    </div>
                    <div>
                      {(isFeatureEnabled(names.modelConfigDownload, userFeatureConfiguration)) ? (
                        <Button
                          name="download-config"
                          type="flat"
                          onClick={this.onClickDownloadConfig}
                          styleOverride={{
                            ':focus': {
                              outline: 'none',
                            },
                          }}
                        >
                          Download Configuration
                        </Button>
                      ) : null}
                    </div>
                  </div>
                </Dialog>
              );
            }
          }}
        </NavigationPrompt>
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
    isDatasetsValid,
    currentType: state.preferences.currentType,
    latestTensorflowVersion: state.preferences.latestTechnology,
    project: Model.ProjectsManager.getProject(projectId) || {},
    client: state.header.client,
    config: state.config.config,
    clientId: state.header.client.id,
    model,
    createNewModel,
    tuneModelId,
    viewModelId,
    actionItems: state.header.actionItems,
    options: datasetsIntents,
    modelViewReadOnly: state.projectsManager.modelViewReadOnly,
    showModelNavigationConfirmationDialog: state.projectsManager.showModelNavigationConfirmationDialog,
    showTransformationDeleteDialog: state.config.showTransformationDeleteDialog,
    showTransformationAddDialog: state.config.showTransformationAddDialog,
    showTransformationPredefinedDialog: state.config.showTransformationPredefinedDialog,
    isConfigsValid: state.config.isConfigsValid,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
    clientDataLoaded: state.projectsManager.clientDataLoaded,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(CreateModelTabsComponent);
