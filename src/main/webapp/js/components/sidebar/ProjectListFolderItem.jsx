import React, { Component } from 'react';
import PropTypes from 'prop-types';
import ListItem from 'grommet/components/ListItem';
import Box from 'grommet/components/Box';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsProjects from 'state/actions/actions_projects';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from 'constants/Constants';
import { RouteNames } from 'utils/routeHelpers';
import {
  DashedSpinner,
  SolidTriangle,
  MessageIcon,
  DropDown,
  Tooltip,
} from '@tfs/ui-components';
import MenuIcon from 'components/Icons/MenuIcon';
import IconButton from 'components/IconButton';

export default class ProjectListFolderItem extends Component {
  constructor(props, context) {
    super(props, context);

    this.onMouseEnter = this.onMouseEnter.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);
    this.onClickDeleteProject = this.onClickDeleteProject.bind(this);
    this.onClickPromoteProject = this.onClickPromoteProject.bind(this);
    this.onClickDemoteProject = this.onClickDemoteProject.bind(this);
    this.onClickEditProject = this.onClickEditProject.bind(this);
    this.spinnerProps = { height: '20px', width: '20px', fill: '#004C97' };
    this.props = props;

    this.state = {
      showAction: false,
      showDropDown: false,
    };
  }

  onClickPromoteOk(projectId, globalProjectName) {
    const { dispatch, globalProjectsByName } = this.props;
    const globalProjectId = globalProjectsByName[globalProjectName.toLowerCase()];
    dispatch(actionsProjects.promoteProjectById({ projectId, globalProjectId, globalProjectName }));
    dispatch(actionsProjects.updateProjectStatus(projectId, Constants.STATUS.RUNNING));
    dispatch(actionsApp.modalDialogChange(null));
  }

  onClickPromoteProject() {
    const {
      project, dispatch,
    } = this.props;

    const { id, name } = project;
    const header = `Promote '${name}' model?`;

    dispatch(actionsApp.modalDialogChange({
      header,
      dispatch,
      type: Constants.DIALOGS.PROMOTE_DIALOG,
      onClickPromote: (globalProjectName) => this.onClickPromoteOk(id, globalProjectName),
    }));
  }

  onClickDemoteOk(projectId) {
    const { dispatch } = this.props;
    dispatch(actionsProjects.demoteProjectById(projectId));
    dispatch(actionsProjects.updateProjectStatus(projectId, Constants.STATUS.RUNNING));
    dispatch(actionsApp.modalDialogChange(null));
  }

  onClickDemoteProject() {
    const {
      project, dispatch,
    } = this.props;

    const { id, name } = project;
    const header = `Demote '${name}' model?`;
    const message = 'Are you sure you want to demote this model?';

    dispatch(actionsApp.modalDialogChange({
      header,
      dispatch,
      message,
      type: Constants.DIALOGS.DELETE_DIALOG,
      ok: Constants.DEMOTE,
      onOk: () => this.onClickDemoteOk(id),
    }));
  }

  onClickDeleteOk() {
    const {
      project, selectedProjectId, dispatch, history,
    } = this.props;
    const { id, clientId } = project;
    dispatch(actionsProjects.deleteProjectById(id));
    dispatch(actionsProjects.updateProjectStatus(id, Constants.STATUS.RUNNING));
    dispatch(actionsApp.modalDialogChange(null));
    if (id === selectedProjectId) {
      dispatch(actionsApp.changeRoute(RouteNames.PROJECTS, { clientId }, history));
    }
  }

  onClickDeleteProject() {
    const {
      project, dispatch,
    } = this.props;

    const { name } = project;
    const header = `Delete '${name}' model?`;
    const message = (
      <div>
        <p>
         All your datasets and versions will be permanently deleted.
         Are you sure you want to delete this model?
        </p>
        <p>
         This cannot be reversed.
        </p>
      </div>
    );

    dispatch(actionsApp.modalDialogChange({
      header,
      dispatch,
      message,
      type: Constants.DIALOGS.DELETE_DIALOG,
      onOk: () => this.onClickDeleteOk(),
    }));
  }

  onClickEditProject() {
    const { project, dispatch, history } = this.props;
    const { id, clientId } = project;
    dispatch(actionsProjects.sidebarSelectProject({ projectId: id }));
    dispatch(actionsApp.changeRoute(RouteNames.UPDATEPROJECT, { clientId, projectId: id }, history));
  }

  onMouseEnter(event) {
    event.preventDefault();
    this.setState({
      showAction: true,
    });
  }

  onMouseLeave(event) {
    event.preventDefault();
    this.setState({
      showAction: false,
      showDropDown: false,
    });
  }

  getSpinner() {
    const { status = '' } = this.props;
    if (status === Constants.STATUS.RUNNING) {
      return (
        <DashedSpinner {...this.spinnerProps} />
      );
    }
    return null;
  }

  onDropDownSelected = (selectedValue) => {
    let result;
    switch (selectedValue) {
    case Constants.DROPDOWN_PROMOTE:
      result = this.onClickPromoteProject();
      break;
    case Constants.DROPDOWN_DEMOTE:
      result = this.onClickDemoteProject();
      break;
    case Constants.DROPDOWN_EDIT:
      result = this.onClickEditProject();
      break;
    case Constants.DROPDOWN_DELETE:
      result = this.onClickDeleteProject();
      break;
    }
    this.setState({
      showDropDown: false,
    });
    return result;
  }

  getMenuIcon(showIcon, showDemote) {
    const { userFeatureConfiguration } = this.props;
    const { names } = featureFlagDefinitions;
    const { showDropDown } = this.state;
    const dropDownList = [];
    if (showIcon) {
      dropDownList.push(Constants.DROPDOWN_PROMOTE);
      dropDownList.push(Constants.DROPDOWN_EDIT);
      if (isFeatureEnabled(names.projectDelete, userFeatureConfiguration)) {
        dropDownList.push(Constants.DROPDOWN_DELETE);
      }
    } else if (showDemote) {
      dropDownList.push(Constants.DROPDOWN_DEMOTE);
    }
    return (
      <span style={{ overflow: 'visible' }}>
        <IconButton
          onClick={() => {
            const { showDropDown: isOpen } = this.state;
            this.setState({
              showDropDown: !isOpen,
            });
          }}
          icon={MenuIcon}
          styleOverride={{
            outline: 'none',
          }}
          width={70}
          height={70}
        />
        <span onMouseLeave={() => { this.setState({ showDropDown: false }); }} style={{ overflow: 'visible' }}>
          {showDropDown
          && (
            <DropDown
              onItemSelected={(selectedValue) => { this.onDropDownSelected(selectedValue); }}
              itemList={dropDownList}
              styleOverride={{
                menuList: {
                  top: 10,
                  right: 55,
                },
              }}
            />
          )
          }
        </span>
      </span>
    );
  }

  truncateCount(number) {
    if (number.toString().length > 8) {
      return `${number.toString().substring(0, 7)}...`;
    }
    return number;
  }

  render() {
    const {
      project = {}, onClick, status = '',
    } = this.props;
    const { showAction } = this.state;
    const {
      id,
      name,
      type,
    } = project;
    const showIcon = (type === Constants.PROJECT_TYPE.NODE.NAME && showAction && status !== Constants.STATUS.RUNNING);
    const showDemote = (showAction && status !== Constants.STATUS.RUNNING);
    const versionNumber = 65;
    const volumeCount = 8000;
    const escalationCount = 2850;
    const volumePercentange = 1.6;
    const escalationPercentange = 1.8;
    const redEscalationTheme = false;

    return (
      <ListItem
        justify="between"
        key={id}
        onClick={onClick}
        selected={this.props.selected}
        className="ProjectListFolderItem"
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      >
        <Box direction="row" flex>
          <Box
            direction="row"
            align="center"
            className="ProjectListFolderIcon"
            style={redEscalationTheme ? { backgroundColor: '#e03c31' } : { backgroundColor: '#d7d8d9' }}
          >
            <MessageIcon stroke={redEscalationTheme ? '#f8e8e7' : '#7f7f7f'} />
          </Box>
          <Box
            flex
            className="ProjectListFolderItemName"
          >
            <Box direction="row">
              <Box>
                <span className="project-list-menu-icon">
                  { showAction && this.getMenuIcon(showIcon, showDemote)}
                  { this.getSpinner() }
                </span>
              </Box>
              <div className="project-list-item-name">
                {name}
              </div>
            </Box>
            <div className="project-list-item-meta">
              {Constants.VERSION(versionNumber)}
            </div>
            <div className="project-list-item-duration">
              {'LAST TWO WEEKS'}
            </div>
            <hr />
            <Box direction="row">
              <Box style={{ width: '130px' }}>
                <div>
                  <span
                    className="project-list-volume-count"
                  >
                    <Tooltip
                      content={volumeCount}
                      direction="right"
                      enabled={volumeCount.length > 8}
                    >
                      <span>
                        {this.truncateCount(volumeCount)}
                      </span>
                    </Tooltip>
                  </span>
                  <span className="grey-theme-volume">
                    <SolidTriangle
                      fill="#6E7887"
                      direction="down"
                    />
                    {Constants.PERCENTAGE(volumePercentange)}
                  </span>
                  <div className="project-list-volume">
                    {Constants.VOLUME}
                  </div>
                </div>
              </Box>
              <Box style={{ width: '130px' }}>
                <div>
                  <span
                    className="project-list-escalation-count"
                  >
                    <Tooltip
                      content={escalationCount}
                      direction="right"
                      enabled={escalationCount.length > 8}
                    >
                      <span>
                        {this.truncateCount(escalationCount)}
                      </span>
                    </Tooltip>
                  </span>
                  <span className={redEscalationTheme ? 'red-theme-escalation' : 'grey-theme-escalation'}>
                    <SolidTriangle
                      fill={redEscalationTheme ? '#e03c31' : '#6E7887'}
                      direction="up"
                    />
                    {Constants.PERCENTAGE(escalationPercentange)}
                  </span>
                  <div className="project-list-escalation">
                    {Constants.ESCALATION}
                  </div>
                </div>
              </Box>
            </Box>
          </Box>
        </Box>
      </ListItem>
    );
  }
}

ProjectListFolderItem.propTypes = {
  index: PropTypes.number,
  project: PropTypes.object.isRequired,
  status: PropTypes.string,
  onClick: PropTypes.func,
  selected: PropTypes.bool,
  dispatch: PropTypes.func.isRequired,
  history: PropTypes.object.isRequired,
};
