import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Menu from 'grommet/components/Menu';
import Anchor from 'grommet/components/Anchor';
import * as actionsModels from 'state/actions/actions_models';
import * as preferencesActions from 'state/actions/actions_preferences';
import Model from 'model';
import { changeRoute, modalDialogChange } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import getUrl, { pathKey } from 'utils/apiUrls';
import constructKibanaUrl from 'utils/kibanaUtils';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import * as appActions from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import { logAmplitudeEvent } from 'utils/amplitudeUtils';
import { headerIcon } from 'styles';

export class ModelRowCascadeMenu extends Component {
  constructor(props) {
    super(props);

    this.onClickBuildModel = this.onClickBuildModel.bind(this);
    this.onClickDownload = this.onClickDownload.bind(this);
    this.onClickViewDigitalUrl = this.onClickViewDigitalUrl.bind(this);
    this.onClickStatistics = this.onClickStatistics.bind(this);
    this.onClickConfiguration = this.onClickConfiguration.bind(this);
    this.onClickTrainingOutputs = this.onClickTrainingOutputs.bind(this);
    this.onClickTestModel = this.onClickTestModel.bind(this);
    this.onClickTuneModel = this.onClickTuneModel.bind(this);
    this.onClickViewModel = this.onClickViewModel.bind(this);
    this.onClickViewLogs = this.onClickViewLogs.bind(this);
    this.onClickAddSpeech = this.onClickAddSpeech.bind(this);
    this.getMenuItem = this.getMenuItem.bind(this);
    this.onClickBuildCombinedModel = this.onClickBuildCombinedModel.bind(this);
    this.downloadFile = this.downloadFile.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);
    this.onClickDeleteModel = this.onClickDeleteModel.bind(this);
    this.activeItems = {};

    this.props = props;

    this.state = {
      items: this.getItems(),
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
    this.setState({
      items: this.getItems(),
    });
  }

  onMouseLeave() {
    this.activeItems = {};
    this.setState({ items: this.getItems() });
  }

  changeActiveItems(item) {
    this.activeItems = {
      [item]: true,
    };
    this.setState({ items: this.getItems() });
  }

  getMenuItem(name, disabled, onClickHandler, displayText) {
    const text = displayText || name;
    return {
      name,
      text,
      onMouseEnter: () => { this.changeActiveItems(name); },
      onMouseLeave: this.onMouseLeave,
      className: this.activeItems[name] ? 'hover' : null,
      disabled,
      onClick: !disabled ? onClickHandler : null,
    };
  }

  getItems() {
    const { model, userFeatureConfiguration, project = {} } = this.props;

    if (!model) {
      return [];
    }

    const { names } = featureFlagDefinitions;
    const {
      modelType = Constants.DIGITAL_MODEL,
      status,
      id,
      speechModelId = '',
    } = model;

    const { deployableModelId = '' } = project;

    const {
      NULL, FAILED, COMPLETED, RUNNING, PREVIEW, LIVE,
    } = Constants.STATUS;
    const { MODEL_ACTION_MENU } = Constants;

    const isBuildEnabled = !status || status === NULL || status == FAILED;
    const isModelPreviewState = status === PREVIEW;
    const isModelLiveState = status === LIVE;
    const isModelComplete = status && (status === COMPLETED || isModelPreviewState || isModelLiveState);
    const isModelRunning = status && (status === NULL || status === RUNNING);
    const isModelDeployable = deployableModelId == id;
    const isSpeechExistForDigitalModel = speechModelId != '';
    const isDigitalModel = modelType === Constants.DIGITAL_MODEL;
    const isSpeechModel = modelType === Constants.SPEECH_MODEL;

    const menuItems = (isDigitalModel || isSpeechModel) ? [
      this.getMenuItem(MODEL_ACTION_MENU.BUILD, !isBuildEnabled, this.onClickBuildModel),
      this.getMenuItem(MODEL_ACTION_MENU.TEST, !isModelComplete, this.onClickTestModel),
      this.getMenuItem(MODEL_ACTION_MENU.TUNE, false, this.onClickTuneModel),
      this.getMenuItem(MODEL_ACTION_MENU.VIEW, false, this.onClickViewModel),
    ] : [
      this.getMenuItem(MODEL_ACTION_MENU.TEST, !isModelComplete, this.onClickTestModel),
    ];

    if (!(isDigitalModel || isSpeechModel)) {
      if (isFeatureEnabled(names.speechBundledUnbundled, userFeatureConfiguration)) {
        menuItems.push(
          this.getMenuItem(MODEL_ACTION_MENU.VIEW_DIGITAL_URL, !isModelComplete, this.onClickViewDigitalUrl),
        );
      }
    }

    if (isFeatureEnabled(names.modelDownload, userFeatureConfiguration)) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.MODEL_FILE, !isModelComplete, this.onClickDownload),
      );
    }

    if (isDigitalModel && isFeatureEnabled(names.modelTrainingOutputs, userFeatureConfiguration)) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.TRAINING_OUTPUTS, !isModelComplete, this.onClickTrainingOutputs),
      );
    }

    if (isDigitalModel) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.ACCURACY_REPORT, !isModelComplete, this.onClickStatistics),
      );
    }

    if (isDigitalModel && isFeatureEnabled(names.modelConfigDownload, userFeatureConfiguration)) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.CONFIGURATION, false, this.onClickConfiguration, 'Download Configuration'),
      );
    }

    if (isDigitalModel) {
      let addSpeechLabel = MODEL_ACTION_MENU.ADD_SPEECH;
      if (isSpeechExistForDigitalModel) {
        addSpeechLabel = MODEL_ACTION_MENU.REBUILD_SPEECH;
      }
      menuItems.push(
        this.getMenuItem(addSpeechLabel, !isModelComplete, this.onClickAddSpeech),
      );
    }

    if (isFeatureEnabled(names.kibanaLogs, userFeatureConfiguration)) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.VIEW_LOGS, false, this.onClickViewLogs),
      );
    }

    if (isSpeechModel) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.BUILD_COMBINED_MODEL, false, this.onClickBuildCombinedModel),
      );
    }

    if (isDigitalModel && isFeatureEnabled(names.modelDelete, userFeatureConfiguration) && !isModelDeployable && !isModelPreviewState && !isModelLiveState) {
      menuItems.push(
        this.getMenuItem(MODEL_ACTION_MENU.DELETE, isModelRunning, this.onClickDeleteModel),
      );
    }

    return menuItems;
  }

  downloadFile(urlPathKey) {
    const {
      modelId, projectId, clientId, configId,
    } = this.props;
    const locationUrl = getUrl(urlPathKey, {
      modelId, projectId, clientId, configId,
    });
    document.location = locationUrl;
  }

  onClickBuildModel() {
    const { dispatch, projectId, modelId } = this.props;
    dispatch(actionsModels.startModelBuild({ projectId, modelId }));
  }

  onClickTestModel() {
    const {
      projectId, modelId, history, clientId, dispatch,
    } = this.props;
    dispatch(actionsModels.clearListOfBatchTests());
    dispatch(changeRoute(RouteNames.TESTMODEL, { clientId, projectId, modelId }, history));
  }

  onClickBuildCombinedModel() {
    const {
      projectId, modelId, history, clientId, dispatch,
    } = this.props;
    const header = 'Add Digital Url';
    dispatch(modalDialogChange({
      header,
      type: Constants.DIALOGS.ADD_COMBINED_SPEECH,
      projectId,
      history,
      modelId,
      clientId,
      dispatch,
    }));
  }

  onClickDownload() {
    const {
      projectId, modelId, clientId, state, model,
    } = this.props;
    const { modelType } = model;
    logAmplitudeEvent(AmplitudeConstants.DOWNLOAD_MODEL_EVENT, state, {
      projectId, clientDBId: clientId, modelDBId: modelId, modelType,
    });
    this.downloadFile(pathKey.modelDownload);
  }

  onClickStatistics() {
    this.downloadFile(pathKey.modelStatistics);
  }

  onClickTrainingOutputs() {
    const {
      dispatch,
      model,
    } = this.props;
    const date = Date.now();
    const { created } = model;
    if ((date - created) > Constants.TRAINING_OUTPUT_PURGE_TTL_IN_DAYS * 24 * 60 * 60 * 1000) {
      const header = Constants.TRAINING_OUTPUT_PURGE_MESSAGE_HEADER;
      const message = Constants.TRAINING_OUTPUT_PURGE_MESSAGE_BODY;
      dispatch(appActions.modalDialogChange({
        header,
        dispatch,
        message,
        showSpinner: false,
        okVisible: true,
        cancelVisible: false,
        closeIconVisible: false,
        showHeader: true,
        showFooter: true,
        type: Constants.DIALOGS.PROGRESS_DIALOG,
        okChildren: 'OK',
        onOk: () => {
          dispatch(appActions.modalDialogChange(null));
        },
        styleOverride: {
          childContainer: {
            marginLeft: '30px',
            marginRight: '30px',
          },
          content: {
            top: '160px',
          },
        },
      }));
    } else {
      this.downloadFile(pathKey.modelTrainingOutputs);
    }
  }

  onClickConfiguration() {
    this.downloadFile(pathKey.modelConfigDownload);
  }

  onClickTuneModel() {
    const {
      projectId, model, history, clientId, dispatch,
    } = this.props;
    dispatch(actionsModels.tuneSelectedModel(model));
    if (model.modelType === 'SPEECH') {
      dispatch(appActions.changeRoute(RouteNames.TUNESPEECHMODEL, { clientId, projectId }, history));
    } else {
      dispatch(appActions.changeRoute(RouteNames.TUNEMODEL, { clientId, projectId }, history));
    }
  }

  onClickViewModel() {
    const {
      projectId, history, clientId, dispatch, model,
    } = this.props;

    dispatch(actionsModels.viewSelectedModel(model));
    if (model.modelType === 'SPEECH') {
      dispatch(appActions.changeRoute(RouteNames.VIEWSPEECHMODEL, { clientId, projectId }, history));
    } else {
      dispatch(appActions.changeRoute(RouteNames.VIEWMODEL, { clientId, projectId }, history));
    }
  }

  onClickAddSpeech() {
    const {
      dispatch, model, datasets, userFeatureConfiguration,
    } = this.props;
    dispatch(appActions.modalDialogChange({
      type: Constants.DIALOGS.ADD_SPEECH,
      dispatch,
      userFeatureConfiguration,
      model,
      datasets,
    }));
  }

  onClickViewLogs() {
    const { model, kibanaLogIndex, kibanaLogURL } = this.props;
    const url = constructKibanaUrl({
      kibanaLogIndex,
      kibanaLogURL,
      modelToken: model.modelToken,
    });

    let win = window.open(url, '_blank');
    win.focus();
  }

  onClickViewDigitalUrl() {
    const {
      dispatch, model, orionURL,
    } = this.props;

    const { digitalHostedUrl = '' } = model;
    let url = digitalHostedUrl;

    if (!url) {
      url = `${orionURL}/${model.modelToken}/digital`;
    }

    const message = (
      <div>
        <p>
          URL of Digital Model is: "
          {url}
"
        </p>
      </div>
    );

    dispatch(appActions.modalDialogChange({
      dispatch,
      type: Constants.DIALOGS.PROGRESS_DIALOG,
      message,
      header: Constants.DIGITAL_URL,
      showHeader: true,
      showFooter: true,
      cancelVisible: true,
      okVisible: false,
      showSpinner: false,
      closeIconVisible: true,
      cancelChildren: Constants.CLOSE,
      styleOverride: {
        childContainer: {
          margin: '25px 0px 25px 0px',
        },
        content: {
          top: '160px',
        },
        ...headerIcon,
      },
    }));
  }

  onClickDeleteModel() {
    const {
      dispatch, model, modelId, projectId, clientId,
    } = this.props;

    const header = `Delete 'Version ${model.version}' ?`;
    const message = (
      <div>
        <p>
          Please note that dependent speech model also will be deleted along with digital model.
          Your model config file and datasets are still available if you choose to rebuild the model later.
        </p>
      </div>
    );

    dispatch(appActions.modalDialogChange({
      header,
      dispatch,
      message,
      type: Constants.DIALOGS.DELETE_DIALOG,
      onOk: () => {
        dispatch(actionsModels.deleteModelFromProject({ clientId, projectId, modelId }));
        dispatch(appActions.modalDialogChange(null));
      },
    }));
  }

  render() {
    const menuItems = [];
    this.state.items.forEach((item, index) => {
      menuItems.push(
        <Anchor
          onClick={item.onClick}
          className="dataset-menu action-menu"
          disabled={item.disabled}
          key={item.text}
          data-qa={item.text}
        >
          <span className="dataset-menu-item action-menu-item">
            {item.text}
          </span>
        </Anchor>,
      );
    });

    // TODO : Remove the grommet Menu , once dropdown menu is integrated from tfs-ui
    return (
      <Menu
        responsive
        label="..."
        className="ActionItems"
      >
        {menuItems}
      </Menu>
    );
  }
}

ModelRowCascadeMenu.propTypes = {
  projectId: PropTypes.string.isRequired,
  modelId: PropTypes.string.isRequired,
  clientId: PropTypes.string.isRequired,
  configId: PropTypes.string.isRequired,
  kibanaLogIndex: PropTypes.string,
  kibanaLogURL: PropTypes.string,
  userFeatureConfiguration: PropTypes.object,
  model: PropTypes.object,
  datasets: PropTypes.object,
  dispatch: PropTypes.func,
  project: PropTypes.object,
};

const mapStateToProps = (state, ownProps) => ({
  state,
  orionURL: state.app.orionURL,
  datasets: Model.ProjectsManager.getDatasetsByProjectId(ownProps.projectId, true) || null,
  model: Model.ProjectsManager.getModel(ownProps.projectId, ownProps.modelId) || null,
  kibanaLogIndex: state.app.kibanaLogIndex,
  kibanaLogURL: state.app.kibanaLogURL,
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  clientId: state.header.client.id,
  project: Model.ProjectsManager.getProject(ownProps.projectId) || null,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(ModelRowCascadeMenu));
