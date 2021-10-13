import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import * as projectsActions from 'state/actions/actions_projects.js';
import * as actionsTaggingGuide from 'state/actions/actions_taggingguide';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import List from 'grommet/components/List';
import { changeRoute } from 'state/actions/actions_app';
import * as appActions from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import * as projectActions from 'state/actions/actions_projects';
import * as preferencesActions from 'state/actions/actions_preferences';
import Collapsible from 'react-collapsible';
import {
  Plus, Tabs, Filter, Tooltip, InfoIcon,
} from '@tfs/ui-components';
import IconButton from 'components/IconButton';
import Placeholder from 'components/controls/Placeholder';
import ProjectListFolderItem from 'components/sidebar/ProjectListFolderItem';

class ProjectListSidebar extends Component {
  constructor(props, context) {
    super(props, context);

    this.handleProjectSelect = this.handleProjectSelect.bind(this);
    this.loadChunk = this.loadChunk.bind(this);
    this.onClickCreateProject = this.onClickCreateProject.bind(this);

    this.localStorage_key = Constants.LOCALSTORAGE_KEY.COLLAPSIBLE_SIDEBAR_OPEN;
    this.renderTabs = this.renderTabs.bind(this);
    this.renderGlobalProjectItems = this.renderGlobalProjectItems.bind(this);
    this.renderNodeProjectItems = this.renderNodeProjectItems.bind(this);

    this.props = props;
    // TODO: revert limit back to 100 after fixing MD-153
    this.limit = 500;
    this.state = {
      activeTab: Constants.PROJECT_TYPE.GLOBAL.NAME,
      showGlobalInfoTooltip: false,
      showNodeInfoTooltip: false,
    };
  }

  componentDidMount() {
    const { projectListSidebar, dispatch } = this.props;
    if (!_.isNil(projectListSidebar.clientId)
         && projectListSidebar.clientId !== 0) {
      this.loadChunk();
    }
    // TODO: Add project fetch at regular intervals
  }

  componentDidUpdate(prevProps) {
    const { dispatch } = this.props;
    const prevClientId = prevProps.projectListSidebar.clientId;
    const clientId = this.props.projectListSidebar.clientId;
    if (prevClientId !== clientId) {
      this.loadChunk();
    }
    dispatch(preferencesActions.getVectorizerByTechnology({ technology: 'use_large' }));
  }

  loadChunk() {
    const {
      loading, noMoreProjects, clientId, startIndex,
    } = this.props.projectListSidebar;
    if (loading || noMoreProjects) {
      return;
    }
    this.props.fetchProjectsByClient({
      clientId: clientId || this.props.header.client.id,
      limit: this.limit,
      startIndex,
    });
  }

  handleProjectSelect(selectionIndex, projects) {
    const selectedProject = projects[selectionIndex];
    const { selectedProjectId } = this.props.projectListSidebar;
    if (!selectedProject || selectedProject.id == selectedProjectId) {
      return;
    }

    const clientId = this.props.header.client.id;

    localStorage.projectId = selectedProject.id;
    localStorage.setItem('projectId', selectedProject.id);
    this.props.sidebarSelectProject({ projectId: selectedProject.id });
    this.props.requestProject(selectedProject.id);
    this.props.requestLastImportInfo(selectedProject.id);
    this.props.requestSearch(selectedProject.id);
    this.props.dispatch(changeRoute(RouteNames.PROJECTID, { clientId, projectId: selectedProject.id }, this.props.history));
  }

  selectedIndex(projects) {
    const { selectedProjectId } = this.props.projectListSidebar;
    if (!selectedProjectId || !projects || !projects.length) {
      return null;
    }
    for (let index = 0; index < projects.length; index++) {
      if (projects[index].id == selectedProjectId) {
        localStorage.setItem('projectId', selectedProjectId);
        return index;
      }
    }
    return null;
  }

  onClickCreateProject(event) {
    event.stopPropagation();
    const { dispatch, history, projectListSidebar } = this.props;
    const { clientId } = projectListSidebar;
    dispatch(appActions.modalDialogChange({
      type: Constants.DIALOGS.CREATE_MODEL_DIALOG,
      header: Constants.CREATE_MODEL,
      dispatch,
      clientId,
      history,
    }));
  }

  getInfoTooltip(tooltipText) {
    return (
      <span className="tooltip-adjust">
        <Tooltip
          content={tooltipText}
          direction="bottom"
        >
          <span>
            <InfoIcon width="15px" height="13px" />
          </span>
        </Tooltip>
      </span>
    );
  }

  getProjectListItems(filteredProjects) {
    const {
      history, dispatch, projectsManager, projectListSidebar, userFeatureConfiguration,
    } = this.props;
    const { globalProjectsByName, selectedProjectId } = projectListSidebar;
    let projectsItems = [];

    if (filteredProjects.length > 0) {
      projectsItems = filteredProjects.map((project, index) => (
        <ProjectListFolderItem
          key={project.id}
          project={project}
          globalProjectsByName={globalProjectsByName}
          index={index}
          selectedProjectId={selectedProjectId}
          status={projectsManager.get(project.id).status}
          history={history}
          dispatch={dispatch}
          userFeatureConfiguration={userFeatureConfiguration}
        />
      ));
    }
    return projectsItems;
  }

  onClickFilter() {
    // TODO
  }

  renderIconButtons() {
    const { activeTab } = this.state;
    const filterClassName = activeTab === Constants.PROJECT_TYPE.GLOBAL.NAME ? 'sidebar-filter-global' : 'sidebar-filter-node';
    return (
      <div>
        <span className={filterClassName}>
          <IconButton
            onClick={this.onClickFilter}
            icon={Filter}
            data-qa="filter-model"
            title="Filter Model"
            height={12}
            width={12}
          />
        </span>
        {activeTab === Constants.PROJECT_TYPE.GLOBAL.NAME ? null
          : (
            <span className="sidebar-create-project">
              <IconButton
                onClick={this.onClickCreateProject}
                icon={Plus}
                strokeColor="#ffffff"
                data-qa="create-model"
                title="Create Model"
                height={12}
                width={12}
                styleOverride={{
                  outline: 'none',
                }}
              />
            </span>
          )
        }
      </div>
    );
  }

  renderGlobalProjectItems(globalFilteredProjects, globalProjectsItems) {
    const { projectListSidebar } = this.props;
    const { noMoreProjects } = projectListSidebar;
    return (
      <List
        selectable
        onSelect={(selectionIndex) => this.handleProjectSelect(selectionIndex, globalFilteredProjects)}
        onMore={noMoreProjects ? null : this.loadChunk}
        selected={this.selectedIndex(globalFilteredProjects)}
      >
        {globalProjectsItems}
      </List>
    );
  }

  renderNodeProjectItems(nodeFilteredProjects, nodeProjectsItems) {
    const { projectListSidebar } = this.props;
    const { noMoreProjects } = projectListSidebar;
    return (
      <List
        selectable
        onSelect={(selectionIndex) => this.handleProjectSelect(selectionIndex, nodeFilteredProjects)}
        onMore={noMoreProjects ? null : this.loadChunk}
        selected={this.selectedIndex(nodeFilteredProjects)}
      >
        {nodeProjectsItems}
      </List>
    );
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    let tab = '';
    const { projects } = this.props.projectListSidebar;
    const globalFilteredProjects = projects.filter(project => project.type === Constants.PROJECT_TYPE.GLOBAL.NAME);
    const nodeFilteredProjects = projects.filter(project => project.type === Constants.PROJECT_TYPE.NODE.NAME);
    switch (selectedTabIndex) {
    case 0:
      tab = Constants.PROJECT_TYPE.GLOBAL.NAME;
      if (globalFilteredProjects && globalFilteredProjects.length > 0) {
        let index = this.selectedIndex(globalFilteredProjects);
        if (index === null) { index = 0; }
        this.handleProjectSelect(index, globalFilteredProjects);
      }
      break;
    case 1:
      tab = Constants.PROJECT_TYPE.NODE.NAME;
      if (nodeFilteredProjects && nodeFilteredProjects.length > 0) {
        let index = this.selectedIndex(nodeFilteredProjects);
        if (index === null) { index = 0; }
        this.handleProjectSelect(index, nodeFilteredProjects);
      }
      break;
    default:
      tab = Constants.PROJECT_TYPE.GLOBAL.NAME;
      break;
    }
    this.setState({
      activeTab: tab,
    });
  }

  getGlobalTabTitle = (title) => (
    <div
      onMouseEnter={() => this.setState({ showGlobalInfoTooltip: true })}
      onMouseLeave={() => this.setState({ showGlobalInfoTooltip: false })}
    >
      {title}
      {this.state.showGlobalInfoTooltip && this.getInfoTooltip(Constants.PROJECT_TYPE.GLOBAL.TOOLTIP)}
    </div>
  )

  getNodeTabTitle = (title) => (
    <div
      onMouseEnter={() => this.setState({ showNodeInfoTooltip: true })}
      onMouseLeave={() => this.setState({ showNodeInfoTooltip: false })}
    >
      {title}
      {this.state.showNodeInfoTooltip && this.getInfoTooltip(Constants.PROJECT_TYPE.NODE.TOOLTIP)}
    </div>
  )

  renderTabs() {
    const { projectListSidebar } = this.props;
    const { projects } = projectListSidebar;
    const { activeTab } = this.state;
    const globalFilteredProjects = projects.filter(project => project.type === Constants.PROJECT_TYPE.GLOBAL.NAME);
    const nodeFilteredProjects = projects.filter(project => project.type === Constants.PROJECT_TYPE.NODE.NAME);
    const globalProjectsItems = this.getProjectListItems(globalFilteredProjects);
    const nodeProjectsItems = this.getProjectListItems(nodeFilteredProjects);
    const globalTitle = this.getGlobalTabTitle(Constants.PROJECT_TYPE.GLOBAL.TAB_TITLE(globalProjectsItems.length));
    const nodeTitle = this.getNodeTabTitle(Constants.PROJECT_TYPE.NODE.TAB_TITLE(nodeProjectsItems.length));
    const tabs = [Constants.PROJECT_TYPE.GLOBAL.NAME, Constants.PROJECT_TYPE.NODE.NAME];
    const tabNames = [globalTitle, nodeTitle];
    const tabPanels = [
      this.renderGlobalProjectItems(globalFilteredProjects, globalProjectsItems),
      this.renderNodeProjectItems(nodeFilteredProjects, nodeProjectsItems),
    ];

    let selectedTabIndex = 0;
    switch (activeTab) {
    case Constants.PROJECT_TYPE.GLOBAL.NAME:
      selectedTabIndex = 0;
      break;
    case Constants.PROJECT_TYPE.NODE.NAME:
      selectedTabIndex = 1;
      break;
    default:
      selectedTabIndex = 0;
      break;
    }

    return (
      <div id="listsidebar">
        <Tabs
          tabs={tabs}
          tabPanels={tabPanels}
          tabNames={tabNames}
          onTabSelected={this.onTabSelected}
          align="left"
          selectedIndex={selectedTabIndex}
          styleOverride={{
            tabItem: {
              padding: '23px 20px 21px',
            },
            tabContainer: {
              borderTop: 'none',
              overflow: 'visible',
              overflowX: 'visible',
              overflowY: 'visible',
            },
          }}
        />
      </div>
    );
  }

  renderList() {
    const { projectListSidebar, header } = this.props;
    const { projects, noMoreProjects } = projectListSidebar;
    const { itsClientId = '', itsAppId = '' } = header.client;

    if (projects === null) {
      /* not loaded */
      return (
        <Placeholder message={Constants.LOADING_MESSAGE} />
      );
    }

    if (!projects.length && !noMoreProjects) {
      /* empty list */
      return (
        <Placeholder message={Constants.NO_MODELS_AVAILABLE(itsClientId, itsAppId)} />
      );
    }

    return (
      <div>
        {this.renderTabs()}
      </div>
    );
  }

  render() {
    const { isOpen } = this.props;
    return (
      <div
        id="ProjectListSidebar"
        className="listContainer"
      >
        {isOpen && this.renderList()}
        {isOpen && this.renderIconButtons()}
      </div>
    );
  }
}

ProjectListSidebar.propTypes = {
  toggleDisplay: PropTypes.func,
  isOpen: PropTypes.bool,
  match: PropTypes.object,
  isGlobalModelOpen: PropTypes.bool,
  isNodeModelOpen: PropTypes.bool,
};

const mapStateToProps = state => ({
  projectListSidebar: state.projectListSidebar,
  projectsManager: state.projectsManager.projects,
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  header: state.header,
});

const mapDispatchToProps = dispatch => ({
  fetchProjectsByClient: ({ clientId, startIndex, limit }) => {
    dispatch(projectsActions.fetchProjectsByClient({ clientId, startIndex, limit }));
  },
  sidebarSelectProject({ projectId }) {
    dispatch(projectsActions.sidebarSelectProject({ projectId }));
  },
  collapsibleSidebarSelect(isCollapsibleModelOpen) {
    dispatch(projectsActions.collapsibleSidebarSelect(isCollapsibleModelOpen));
  },
  requestProject: (projectId) => {
    dispatch(actionsTaggingGuide.requestProject(projectId));
  },
  requestLastImportInfo: (projectId) => {
    dispatch(actionsTaggingGuide.requestLastImportInfo(projectId));
  },
  requestSearch: (projectId) => {
    dispatch(actionsTaggingGuide.requestSearch(projectId, { property: 'count', direction: 'desc' }));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(ProjectListSidebar));
