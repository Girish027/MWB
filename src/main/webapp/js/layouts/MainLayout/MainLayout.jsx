
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as ramda from 'ramda';
import URLSearchParams from '@ungap/url-search-params';

import Model from 'model';
import ProjectListSidebar from 'components/sidebar/ProjectListSidebar';
import SettingsPageHeader from 'components/settings/SettingsPageHeader';
import AppHeader from 'components/AppHeader';
import * as headerActions from 'state/actions/actions_header';
import { sidebarSelectProject } from 'state/actions/actions_projects';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import Navigation from 'components/Navigation';
import ErrorBoundary from 'components/ErrorBoundary/ErrorBoundary';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';

import {
  loadProjectDatasets,
  loadProjectModels,
  loadProjectConfigs,
} from 'state/actions/actions_projectsmanager';

import {
  requestSearch, requestLastImportInfo,
} from 'state/actions/actions_taggingguide';

import {
  getUpdateClient,
  getUpdateProjectId,
  filterClientsList,
  getClientsByStandardClientId,
} from 'layouts/MainLayout/mainLayoutUtils';

class MainLayoutComponent extends Component {
  constructor(props, context) {
    super(props, context);

    this.props = props;
    this.state = {
      isSidebarOpen: true,
      lastActiveClient: undefined,
    };
    this.onSelectClient = this.onSelectClient.bind(this);

    this.loadingProject = false;
    this.loadingModels = false;
    this.loadingDatasets = false;
    this.loadingConfigs = false;
    this.loadingIntents = false;
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    const {
      project,
      dispatch,
      projects,
      datasets,
      models,
      configs,
      intents,
      clientList,
      selectedClient,
      selectedProjectId,
      noMoreProjects,
      routeClientId,
      routeAppId,
      routeProjectId,
      isSettingsPage,
    } = this.props;

    const {
      lastActiveClient,
    } = this.state;

    let updateProjectId;
    let projectId;
    let activeClient;

    // Select the client (entry point via URL)
    if (!_.isNil(clientList) && clientList.length > 0) {
      activeClient = getUpdateClient({
        clientList,
        routeClientId,
        routeAppId,
      });
      if (!_.isNil(activeClient) && !_.isEmpty(activeClient) && !_.isEqual(lastActiveClient, activeClient)) {
        this.onSelectClient(activeClient.standardClientName, activeClient.itsAppId, true);
      }
    }

    // If there is a selected client and all projects have been received
    if (selectedClient.id !== '0' && noMoreProjects) {
      updateProjectId = getUpdateProjectId({
        projects,
        routeProjectId,
        selectedProjectId,
      });

      if (!_.isNil(updateProjectId) && selectedProjectId !== updateProjectId) {
        dispatch(sidebarSelectProject({
          projectId: updateProjectId,
        }));
        projectId = updateProjectId;
      }
      if (!_.isNil(project)) {
        /**
         * project has loaded. Now load datasets, configs, models and intents.
         */
        if (_.isNil(projectId)) {
          projectId = project.id;
        }

        if (!this.loadingDatasets && _.isNil(datasets)) {
          this.loadingDatasets = true;
          dispatch(loadProjectDatasets(projectId))
            .then(() => {
              this.loadingDatasets = false;
            })
            .catch(() => {
              this.loadingDatasets = false;
            });
        }
        if (!this.loadingConfigs && _.isNil(configs)) {
          this.loadingConfigs = true;
          dispatch(loadProjectConfigs(projectId))
            .then(() => {
              this.loadingConfigs = false;
            })
            .catch(() => {
              this.loadingConfigs = false;
            });
        }

        if (!this.loadingModels && _.isNil(models)) {
          this.loadingModels = true;
          dispatch(loadProjectModels(projectId))
            .then(() => {
              this.loadingModels = false;
            })
            .catch(() => {
              this.loadingModels = false;
            });
        }

        if (!this.loadingIntents && _.isNil(intents)) {
          this.loadingIntents = true;
          dispatch(requestLastImportInfo(projectId));
          dispatch(requestSearch(projectId, {
            property: 'count',
            direction: 'desc',
          }))
            .then(() => {
              this.loadingIntents = false;
            })
            .catch(() => {
              this.loadingIntents = false;
            });
        }
      }
    }
  }

  onSelectClient(newClientId, newAppId, direct = false) {
    const {
      clientList,
      selectedClient,
      history,
      dispatch,
      location,
    } = this.props;

    let newClient;

    if (newClientId && newAppId) {
      newClient = filterClientsList(clientList, newClientId, newAppId);
    }

    if (!newClient || (selectedClient && newClient.id === selectedClient.id)) {
      return;
    }

    if (localStorage.clientId !== newClient.id) {
      localStorage.removeItem('projectId');
      localStorage.removeItem('itsAppId');
    }
    localStorage.clientId = newClient.id;
    localStorage.itsClientId = newClient.itsClientId;
    localStorage.standardClientId = newClient.standardClientName;
    localStorage.itsAppId = newClient.itsAppId;
    this.setState({
      lastActiveClient: newClient,
    });
    dispatch(headerActions.onClientChange(newClient));


    // handles the route change when a client is changed via clientPicker
    const index = location.search.indexOf(`clientid=${newClientId}&appid=${newAppId}`);
    if (index === -1) {
      dispatch(changeRoute(RouteNames.PROJECTS, {
        client: newClient,
      }, history));
    }
  }

  getHomeLink = () => {
    let homeLink = '#';
    const { app = {}, selectedClient = {} } = this.props;
    const { standardClientName: standardClientId = '' } = selectedClient;
    const { ufpURL = '' } = app;
    homeLink = `${ufpURL}?clientid=${standardClientId}`;
    return homeLink;
  };

  render() {
    const {
      showSidebar,
      clientList,
      children,
      selectedClient,
      match,
      app,
      location,
      history,
      routeModelId,
      project,
      actionItems,
      userGroup,
      routeClientId,
      userFeatureConfiguration,
      isSettingsPage,
      dispatch,
    } = this.props;

    const {
      itsURL, userDetails = {}, userAccountLink, ufpURL,
      internalSupportLink, externalSupportLink, documentationLink,
    } = app;
    const { names } = featureFlagDefinitions;
    const { name: userName = 'unknown', email = '' } = userDetails;
    const { isSidebarOpen } = this.state;
    let sidebarClassName = 'main-sidebar-container';
    let supportLink = internalSupportLink;
    let clientId;
    let client;

    if (!isFeatureEnabled(names.supportLink, userFeatureConfiguration)) {
      supportLink = externalSupportLink;
    }

    if (routeClientId) {
      client = getClientsByStandardClientId(clientList, routeClientId);
    }

    if (!_.isNil(client) && !_.isEmpty(client)) {
      clientId = client[0].itsClientId;
    }


    if (showSidebar) {
      if (isSidebarOpen) {
        sidebarClassName += ' sidebar-open';
      } else {
        sidebarClassName += ' sidebar-closed';
      }
    } else {
      sidebarClassName += ' sidebar-hidden';
    }

    return clientList.length ? (
      <main id="app-main">
        <div id="main-navigation-container" className="navigation-container">
          <Navigation
            clientList={clientList}
            selectedClient={selectedClient}
            onSelectClient={this.onSelectClient}
            clientId={clientId}
            userName={userName}
            email={email}
            userGroup={userGroup}
            ufpURL={ufpURL}
            userAccountLink={userAccountLink}
            homeLink={this.getHomeLink()}
            supportLink={supportLink}
            documentationLink={documentationLink}
            history={history}
            dispatch={dispatch}
          />
        </div>
        {isSettingsPage === true ? (
          <div id="main-header-container">
            <SettingsPageHeader
              selectedProject={project}
              selectedClient={selectedClient}
              match={match}
              itsURL={itsURL}
              actionItems={actionItems}
              history={history}
              dispatch={this.props.dispatch}
              clientId={clientId}
              app={this.props.app}
            />
          </div>
        ) : (
          <div id="main-header-container">
            <AppHeader
              clientList={clientList}
              selectedClient={selectedClient}
              modelId={routeModelId}
              match={match}
              location={location}
              selectedProject={project}
              history={history}
              itsURL={itsURL}
              actionItems={actionItems}
              app={this.props.app}
              dispatch={this.props.dispatch}
            />
          </div>
        )}
        <div className={sidebarClassName}>
          <ProjectListSidebar
            isOpen={this.state.isSidebarOpen}
            match={match}
          />
        </div>
        <div className="main-separator" />
        <div id="main-content-container">
          <ErrorBoundary>
            {children}
          </ErrorBoundary>
        </div>
        <footer id="main-footer-container">
          <div className="footer-inner-container" />
        </footer>
      </main>
    ) : null;
  }
}

MainLayoutComponent.defaultProps = {
  showSidebar: true,
  isSettingsPage: false,
};

MainLayoutComponent.propTypes = {
  showSidebar: PropTypes.bool,
  isSettingsPage: PropTypes.bool,
};

const mapStateToProps = (state, ownProps) => {
  const searchString = ramda.path(['location', 'search'], ownProps) || '';
  const query = new URLSearchParams(searchString);

  const routeClientId = query.get('clientid');
  const routeProjectId = query.get('projectid') || undefined;
  const routeAppId = query.get('appid');
  const selectedProjectId = state.projectListSidebar.selectedProjectId;
  const routeModelId = query.get('modelid');
  return {
    app: state.app,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
    clientList: state.header.clientList,
    actionItems: state.header.actionItems,
    selectedClient: state.header.client,
    selectedProjectId,
    projects: state.projectListSidebar.projects,
    noMoreProjects: state.projectListSidebar.noMoreProjects,
    project: Model.ProjectsManager.getProject(selectedProjectId) || null,
    datasets: Model.ProjectsManager.getDatasetsByProjectId(selectedProjectId, true) || null,
    models: Model.ProjectsManager.getModelsByProjectId(selectedProjectId) || null,
    configs: Model.ProjectsManager.getConfigsByProjectId(selectedProjectId) || null,
    intents: Model.ProjectsManager.getIntentsByProjectId(selectedProjectId) || null,
    routeClientId,
    routeAppId,
    routeProjectId,
    routeModelId,
    csrfToken: state.app.csrfToken,
    userGroup: state.app.userGroups,
    ...ownProps,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(MainLayoutComponent);
