import React, { Component } from 'react';
import { connect } from 'react-redux';
import Model from 'model';
import ReadProjectDatasetsGrid from 'components/projects/datasets/ReadProjectDatasetsGrid';
import ModelsView from 'components/projects/models/ModelsView';
import TaggingGuide from 'components/taggingguide/TaggingGuide';
import NodeAnalytics from 'components/projects/nodeAnalytics_tab/NodeAnalytics';
import Overview from 'components/projects/overview_tab/Overview';
import * as actionsApp from 'state/actions/actions_app';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import { changeRoute, modalDialogChange } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import getUrl, { getDataUrl, pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import {
  ContextualActionsBar,
  ContextualActionItem,
  Tag,
  Plus,
  Doc,
  Download,
  Button,
  Tabs,
  ArrowRight,
} from '@tfs/ui-components';
import Placeholder from 'components/controls/Placeholder';
import SettingsView from 'components/projects/settings/SettingsView';
import { actionItems, actionBar } from '../../styles';

export class ReadProject extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;

    this.onClickTag = this.onClickTag.bind(this);
    this.onClickTagBeta = this.onClickTagBeta.bind(this);
    this.onClickUpload = this.onClickUpload.bind(this);
    this.onClickDatasetTemplate = this.onClickDatasetTemplate.bind(this);
    this.onClickResolveInconsistencies = this.onClickResolveInconsistencies.bind(this);
    this.onClickExportAllDatasets = this.onClickExportAllDatasets.bind(this);
    this.onClickAddVersion = this.onClickAddVersion.bind(this);

    this.state = {
      loadingProject: false,
      loadingDatasets: false,
      loadingModels: false,
      componentMounted: false,
    };
  }

  componentDidMount() {
    this.setState({
      componentMounted: true,
    });
  }

  componentWillUnmount() {
    this.setState({
      loadingProject: false,
      loadingDatasets: false,
      loadingModels: false,
    });
  }

  onClickAddVersion(isAnyDatasetTransformed) {
    const {
      dispatch,
      clientId,
      projectId,
      history,
    } = this.props;
    dispatch(modalDialogChange({
      header: Constants.CREATE_NEW_MODEL_HEADER,
      type: Constants.DIALOGS.CREATE_VERSION_DIALOG,
      projectId,
      history,
      isAnyDatasetTransformed,
      clientId,
      dispatch,
    }));
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    const {
      dispatch, history, clientId, projectId,
    } = this.props;
    let routeTag = '';
    switch (selectedTabIndex) {
    case 0:
      routeTag = 'overview';
      break;
    case 1:
      routeTag = 'node-analytics';
      break;
    case 2:
      routeTag = 'models';
      break;
    case 3:
      routeTag = 'datasets';
      break;
    case 4:
      routeTag = 'manage-intents';
      break;
    case 5:
      routeTag = 'manage-settings';
      break;
    default:
      routeTag = 'overview';
      break;
    }
    dispatch(changeRoute(RouteNames.READPROJECT, { clientId, projectId, routeTag }, history));
  }

  render() {
    // Patch put in to delay rendering.  This fixes the crash when
    // navigating to projects view from Create or Tune model. Was getting
    // crash because Model Creation uses react-tag-input which uses
    // react-dnd and the Project View uses react-dnd also.   If ReadProject
    // renders before the componend using react-tag-input is unmounted
    // we got a crash 'Cannot have two HTML5 backends at the same time
    if (!this.state.componentMounted) {
      return null;
    }
    const { loadingProject } = this.state;
    const {
      project, datasets, projectId, match,
    } = this.props;
    const {
      ALL_VERSIONS, DATASETS, INTENT_GUIDE, SETTINGS, OVERVIEW, NODE_ANALYTICS,
    } = Constants;

    if (!projectId) {
      /* project is not selected */
      return (
        <Placeholder message={Constants.CREATE_NEW_MODEL_MESSAGE} />
      );
    }

    if (loadingProject) {
      /* loading data */
      return (
        <div id="ReadProject" />
      );
    }

    if (!project) {
      /* selected project not found */
      return (
        <Placeholder message={Constants.PROJECT_NOT_FOUND} />
      );
    }

    let activeTab = match.path.substr(1);

    let selectedTabIndex = 0;
    switch (activeTab) {
    case 'manage-settings':
      selectedTabIndex = 5;
      break;
    case 'manage-intents':
      selectedTabIndex = 4;
      break;
    case 'datasets':
      selectedTabIndex = 3;
      break;
    case 'models':
      selectedTabIndex = 2;
      break;
    case 'node-analytics':
      selectedTabIndex = 1;
      break;
    case 'overview':
      selectedTabIndex = 0;
      break;
    default:
      selectedTabIndex = 0;
      break;
    }

    const tabs = [OVERVIEW, NODE_ANALYTICS, ALL_VERSIONS, DATASETS, INTENT_GUIDE, SETTINGS];
    const tabPanels = [this.renderOverviewDetails(), this.renderNodeAnalyticsDetails(), this.renderModelDetails(), this.renderDatasetsDetails(), this.renderIntentDetails(), this.renderSettingsDetails()];
    const datasetsArray = datasets ? datasets.toArray() : [];
    const isAnyDatasetTransformed = datasetsArray.some(d => d.status && d.status == 'COMPLETED');
    return (
      <div id="ReadProject">
        <Tabs
          tabs={tabs}
          tabPanels={tabPanels}
          onTabSelected={this.onTabSelected}
          align="left"
          selectedIndex={selectedTabIndex}
          styleOverride={{
            tabItem: {
              padding: '23px 20px 21px',
            },
            tabContainer: {
              borderTop: 'none',
            },
          }}
        />
        <span className="add-version">
          <Button
            onClick={() => { this.onClickAddVersion(isAnyDatasetTransformed); }}
            disabled={!isAnyDatasetTransformed}
          >
            ADD VERSION
          </Button>
        </span>
      </div>
    );
  }

  renderSettingsDetails() {
    return (
      <SettingsView />
    );
  }

  renderIntentDetails() {
    const { intents } = this.props;
    const intentsArray = intents || [];
    return (
      <TaggingGuide
        intents={intentsArray}
      />
    );
  }

  onClickResolveInconsistencies() {
    const { clientId, projectId, dispatch } = this.props;
    dispatch(changeRoute(RouteNames.RESOLVE_INCONSISTENCY, { clientId, projectId }, this.props.history));
  }

  renderDatasetsDetails() {
    const {
      datasets, environment, userFeatureConfiguration,
    } = this.props;
    const { names } = featureFlagDefinitions;
    // if (!loadingDatasets && !datasets) {
    //   /* TODO: show some error message to user? */
    // }

    const datasetsArray = datasets ? datasets.toArray() : [];
    const isAnyDatasetTransformed = datasetsArray.some(d => d.status && d.status == 'COMPLETED');

    return (
      <div id="ReadProjectDatasetsDetails">
        <div style={actionBar}>
          <ContextualActionsBar styleOverride={actionBar.contextualBar}>
            <ContextualActionItem
              icon={Tag}
              onClickAction={isAnyDatasetTransformed ? this.onClickTag : () => {}}
              data-qa="dataset-tag-button"
              disabled={!isAnyDatasetTransformed}
              styleOverride={actionItems}
            >
                TAG
            </ContextualActionItem>
            <ContextualActionItem
              onClickAction={this.onClickUpload}
              icon={Plus}
              data-qa="dataset-upload-button"
              styleOverride={actionItems}
            >
              UPLOAD
            </ContextualActionItem>
            { (isFeatureEnabled(names.resolveInconsistencies, userFeatureConfiguration)) ? (
              <ContextualActionItem
                onClickAction={isAnyDatasetTransformed ? this.onClickResolveInconsistencies : () => {}}
                icon={Doc}
                data-qa="dataset-inconsistencies-button"
                disabled={!isAnyDatasetTransformed}
                styleOverride={actionItems}
              >
                RESOLVE INCONSISTENCIES
              </ContextualActionItem>
            ) : null }
            { (isFeatureEnabled(names.datasetExport, userFeatureConfiguration)) ? (
              <ContextualActionItem
                onClickAction={isAnyDatasetTransformed ? this.onClickExportAllDatasets : () => {}}
                icon={ArrowRight}
                data-qa="dataset-export-all-datasets"
                disabled={!isAnyDatasetTransformed}
              >
                  EXPORT
              </ContextualActionItem>
            ) : null }
            { (isFeatureEnabled(names.datasetTemplate, userFeatureConfiguration)) ? (
              <ContextualActionItem
                onClickAction={this.onClickDatasetTemplate}
                icon={Download}
              >
                  DATASET TEMPLATE
              </ContextualActionItem>
            ) : null }
          </ContextualActionsBar>
        </div>
        { datasetsArray.length
          ? (
            <ReadProjectDatasetsGrid
              data={datasetsArray}
              history={this.props.history}
              clientId={this.props.clientId}
            />
          )
          : (
            <Placeholder>
              <div className="message-default">{Constants.UPLOAD_DATASET_MESSAGE}</div>
              <Button
                type="flat"
                name="upload-2"
                onClick={this.onClickUpload}
              >
                <ContextualActionItem
                  icon={Plus}
                >
                    UPLOAD NEW DATASET
                </ContextualActionItem>
              </Button>
            </Placeholder>
          )}
      </div>
    );
  }

  renderModelDetails() {
    const {
      models, configs, router, projectId, project, clientId, datasets,
      kibanaLogIndex, kibanaLogURL, userFeatureConfiguration,
      featureFlags, history, dispatch, modelId, roles,
    } = this.props;
    const { loadingModels } = this.state;
    const datasetsArray = datasets ? datasets.toArray() : [];
    const isAnyDatasetTransformed = datasetsArray.some(d => d.status && d.status == Constants.STATUS.COMPLETED);

    return (
      <ModelsView
        models={models}
        configs={configs}
        router={router}
        loadingModels={loadingModels}
        projectId={projectId}
        clientId={clientId}
        roles={roles}
        isAnyDatasetTransformed={isAnyDatasetTransformed}
        kibanaLogIndex={kibanaLogIndex}
        kibanaLogURL={kibanaLogURL}
        userFeatureConfiguration={userFeatureConfiguration}
        featureFlags={featureFlags}
        modelId={modelId}
        history={history}
        dispatch={dispatch}
      />
    );
  }


  renderOverviewDetails() {
    const {
      models, configs, router, projectId, project, clientId, datasets,
      history, modelId, roles, dispatch,
    } = this.props;
    const { loadingModels } = this.state;
    const datasetsArray = datasets ? datasets.toArray() : [];
    const isAnyDatasetTransformed = datasetsArray.some(d => d.status && d.status == Constants.STATUS.COMPLETED);
    return (
      <Overview
        models={models}
        configs={configs}
        router={router}
        loadingModels={loadingModels}
        projectId={projectId}
        clientId={clientId}
        isAnyDatasetTransformed={isAnyDatasetTransformed}
        roles={roles}
        modelId={modelId}
        history={history}
        dispatch={dispatch}
      />
    );
  }

  renderNodeAnalyticsDetails() {
    const {
      models, configs, router, projectId, project, clientId, datasets,
      history, modelId, roles, dispatch,
    } = this.props;
    const { loadingModels } = this.state;
    const datasetsArray = datasets ? datasets.toArray() : [];
    const isAnyDatasetTransformed = datasetsArray.some(d => d.status && d.status == Constants.STATUS.COMPLETED);
    return (
      <NodeAnalytics
        models={models}
        configs={configs}
        router={router}
        loadingModels={loadingModels}
        projectId={projectId}
        clientId={clientId}
        roles={roles}
        isAnyDatasetTransformed={isAnyDatasetTransformed}
        modelId={modelId}
        history={history}
        dispatch={dispatch}
      />
    );
  }

  onClickTag() {
    const { clientId, projectId, dispatch } = this.props;
    dispatch(changeRoute(RouteNames.TAG_DATASETS, { clientId, projectId }, this.props.history));
  }

  onClickTagBeta() {
    const { clientId, projectId, dispatch } = this.props;
    dispatch(changeRoute(RouteNames.TAG_DATASETS_BETA, { clientId, projectId }, this.props.history));
  }

  onClickDatasetTemplate() {
    const locationUrl = getDataUrl(pathKey.datasetTemplate);
    document.location = locationUrl;
  }

  onClickUpload() {
    const { project, dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange({
      type: Constants.DIALOGS.CREATE_DATASET,
      project,
    }));
  }

  onClickExportAllDatasets() {
    const { clientId, projectId } = this.props;
    const locationUrl = getUrl(pathKey.datasetExport, { projectId, clientId });
    document.location = locationUrl;
  }
}

const mapStateToProps = (state, ownProps) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  return {
    environment: state.app.environment,
    project: Model.ProjectsManager.getProject(projectId) || null,
    datasets: Model.ProjectsManager.getDatasetsByProjectId(projectId, true) || null,
    models: Model.ProjectsManager.getModelsByProjectId(projectId) || null,
    configs: Model.ProjectsManager.getConfigsByProjectId(projectId) || null,
    intents: Model.ProjectsManager.getIntentsByProjectId(projectId) || null,
    clientId: state.header.client.id,
    clientName: state.header.client.name,
    roles: state.header.client.roles || [],
    csrfToken: state.app.csrfToken,
    kibanaLogIndex: state.app.kibanaLogIndex,
    kibanaLogURL: state.app.kibanaLogURL,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
    featureFlags: state.app.featureFlags,
    projectId,
    clientDataLoaded: state.projectsManager.clientDataLoaded,
  };
};
const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ReadProject);
