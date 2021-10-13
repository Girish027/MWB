/* react */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsDatasets from 'state/actions/actions_datasets';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Menu from 'grommet/components/Menu';
import Anchor from 'grommet/components/Anchor';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import store from 'state/configureStore';
import Model from 'model';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import getUrl, { pathKey } from 'utils/apiUrls';
import { getLanguage } from 'state/constants/getLanguage';
import Constants from 'constants/Constants';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

class DatasetRowCascadeMenu extends Component {
  constructor(props) {
    super(props);

    this.onClickTransform = this.onClickTransform.bind(this);
    this.onClickCancelTransform = this.onClickCancelTransform.bind(this);
    this.onClickTagDataset = this.onClickTagDataset.bind(this);
    this.onClickRetry = this.onClickRetry.bind(this);
    this.onClickRemoveJob = this.onClickRemoveJob.bind(this);
    this.onClickRemoveDataset = this.onClickRemoveDataset.bind(this);
    this.onClickExportDataset = this.onClickExportDataset.bind(this);

    this.onMouseLeave = this.onMouseLeave.bind(this);
    this.activeItems = {};

    this.props = props;

    this.dataset = Model.ProjectsManager.getDataset(this.props.projectId, this.props.datasetId);
    this.project = Model.ProjectsManager.getProject(this.props.projectId);
    this.state = {
      items: this.getItems(),
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
    this.dataset = Model.ProjectsManager.getDataset(this.props.projectId, this.props.datasetId);
    this.project = Model.ProjectsManager.getProject(this.props.projectId);

    this.setState({
      items: this.getItems(),
    });
  }

  onMouseLeave() {
    this.activeItems = {};
    this.setState({ items: this.getItems() });
  }

  changeActiveItems() {
    this.activeItems = {};
    for (let i = 0; i < arguments.length; i++) {
      this.activeItems[arguments[i]] = true;
    }
    this.setState({ items: this.getItems() });
  }

  getItems() {
    if (!this.dataset) {
      return [];
    }
    const { names } = featureFlagDefinitions;
    const { userFeatureConfiguration } = this.props;
    const items = [];
    switch (this.dataset.status) {
    case 'NULL':
      items.push({
        name: 'Transform',
        text: 'Transform Dataset',
        onMouseEnter: () => { this.changeActiveItems('Transform'); },
        onMouseLeave: this.onMouseLeave,
        className: this.activeItems.Transform ? 'hover' : null,
        onClick: this.onClickTransform,
      });
      break;

    case 'QUEUED':
    case 'RUNNING':
    case 'STARTED':
      items.push({
        name: 'Tag',
        text: 'Tag',
        disabled: true,
        onMouseEnter: () => { this.changeActiveItems('Tag'); },
        onMouseLeave: this.onMouseLeave,
        className: this.activeItems.Tag ? 'hover' : null,
        onClick: this.onClickTagDataset,
      });
      if (isFeatureEnabled(names.datasetDelete, userFeatureConfiguration)) {
        items.push({
          name: 'RemoveDataset',
          text: 'Delete',
          disabled: true,
          onMouseEnter: () => { this.changeActiveItems('RemoveDataset'); },
          onMouseLeave: this.onMouseLeave,
          className: this.activeItems.RemoveDataset ? 'hover' : null,
          onClick: this.onClickRemoveDataset,
        });
      }
      if (isFeatureEnabled(names.datasetExportById, userFeatureConfiguration)) {
        items.push({
          name: 'ExportDataset',
          text: 'Export',
          disabled: true,
          onMouseEnter: () => { this.changeActiveItems('ExportDataset'); },
          onMouseLeave: this.onMouseLeave,
          className: this.activeItems.ExportDataset ? 'hover' : null,
          onClick: this.onClickExportDataset,
        });
      }
      break;

    case 'COMPLETED':
      items.push({
        name: 'Tag',
        text: 'Tag',
        onMouseEnter: () => { this.changeActiveItems('Tag'); },
        onMouseLeave: this.onMouseLeave,
        className: this.activeItems.Tag ? 'hover' : null,
        onClick: this.onClickTagDataset,
      });
      if (isFeatureEnabled(names.datasetDelete, userFeatureConfiguration)) {
        items.push({
          name: 'RemoveDataset',
          text: 'Delete',
          onMouseEnter: () => { this.changeActiveItems('RemoveDataset'); },
          onMouseLeave: this.onMouseLeave,
          className: this.activeItems.RemoveDataset ? 'hover' : null,
          onClick: this.onClickRemoveDataset,
        });
      }
      if (isFeatureEnabled(names.datasetExportById, userFeatureConfiguration)) {
        items.push({
          name: 'ExportDataset',
          text: 'Export',
          onMouseEnter: () => { this.changeActiveItems('ExportDataset'); },
          onMouseLeave: this.onMouseLeave,
          className: this.activeItems.ExportDataset ? 'hover' : null,
          onClick: this.onClickExportDataset,
        });
      }
      break;

    case 'FAILED':
    case 'CANCELLED':
      if (isFeatureEnabled(names.datasetDelete, userFeatureConfiguration)) {
        items.push({
          name: 'RemoveDataset',
          text: 'Delete',
          onMouseEnter: () => { this.changeActiveItems('RemoveDataset'); },
          onMouseLeave: this.onMouseLeave,
          className: this.activeItems.RemoveDataset ? 'hover' : null,
          onClick: this.onClickRemoveDataset,
        });
      }
      // TODO: Renable after fixing retry code in backend
      /*
      items.push({
        name: 'Retry',
        text: 'Retry Transform Dataset',
        onMouseEnter: () => { this.changeActiveItems('Retry'); },
        onMouseLeave: this.onMouseLeave,
        className: this.activeItems.Retry ? 'hover' : null,
        onClick: this.onClickRetry,
      });
      items.push({
        name: 'RemoveJob',
        text: 'Remove Transformation',
        onMouseEnter: () => { this.changeActiveItems('RemoveJob'); },
        onMouseLeave: this.onMouseLeave,
        className: this.activeItems.RemoveJob ? 'hover' : null,
        onClick: this.onClickRemoveJob,
      }); */
      break;
    }
    return items;
  }

  onClickTransform() {
    const {
      projectId, datasetId, app, client, fetchDatasetTransform,
    } = this.props;
    fetchDatasetTransform(app.userId, datasetId, client.id, projectId, app.csrfToken, false);
  }

  onClickCancelTransform() {
    const {
      projectId, datasetId, app, client, fetchCancelTransformJob,
    } = this.props;
    fetchCancelTransformJob(app.userId, datasetId, projectId, app.csrfToken, client.id);
  }

  onClickTagDataset() {
    const {
      projectId, client, datasetId, history, dispatch,
    } = this.props;
    dispatch(actionsTagDatasets.setIncomingFilter({ projectId, datasets: [datasetId] }));
    dispatch(changeRoute(RouteNames.TAG_DATASETS, { client, projectId }, history));
  }

  onClickRetry() {
    const {
      projectId, datasetId, app, client, fetchDatasetTransform,
    } = this.props;
    fetchDatasetTransform(app.userId, datasetId, client.id, projectId, app.csrfToken, true);
  }

  onClickRemoveJob() {
    const {
      projectId, datasetId, app, client, fetchDeleteTransformJob,
    } = this.props;
    fetchDeleteTransformJob(app.userId, datasetId, projectId, app.csrfToken, client.id);
  }

  onClickDeleteOk() {
    const {
      projectId, datasetId, client, removeDatasetFromProject, dispatch,
    } = this.props;
    removeDatasetFromProject(projectId, datasetId, client.id);
    dispatch(actionsApp.modalDialogChange(null));
  }

  onClickRemoveDataset() {
    const { dispatch, projectId, datasetId } = this.props;
    const isDatasetUsedinModel = Model.ProjectsManager.isDatasetUsedInModel(projectId, datasetId);
    const header = `Delete '${this.dataset.name}' dataset?`;
    const message = (
      <div>
        <p>
          Are you sure you want to delete this dataset from project
          {' '}
          {this.project.name}
?
        </p>
        <p>This cannot be reversed.</p>
      </div>
    );

    if (isDatasetUsedinModel) {
      dispatch(actionsApp.displayWarningRequestMessage(DISPLAY_MESSAGES.datasetUsed));
      return;
    }

    dispatch(actionsApp.modalDialogChange({
      header,
      dispatch,
      message,
      type: Constants.DIALOGS.DELETE_DIALOG,
      onOk: () => this.onClickDeleteOk(),
    }));
  }

  onClickExportDataset() {
    const { client, datasetId } = this.props;
    const locationUrl = getUrl(pathKey.datasetExportById, { projectId: this.project.id, datasetId, clientId: client.id });
    document.location = locationUrl;
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

DatasetRowCascadeMenu.propTypes = {
  projectId: PropTypes.string.isRequired,
  datasetId: PropTypes.string.isRequired,
  hideItems: PropTypes.object,
};

const mapStateToProps = state => ({
  app: state.app,
  client: state.header.client,
  projectsManager: state.projectsManager, // TODO: improvements
  userFeatureConfiguration: state.app.userFeatureConfiguration,
});

const mapDispatchToProps = dispatch => ({
  fetchDatasetTransform: (userId, datasetId, clientId, projectId, csrfToken, isRetry) => {
    dispatch(actionsDatasets.fetchDatasetTransform(userId, datasetId, clientId, projectId, csrfToken, isRetry));
  },
  fetchDeleteTransformJob: (userId, datasetId, projectId, csrfToken, clientId) => {
    dispatch(actionsDatasets.fetchDeleteTransformJob(userId, datasetId, projectId, csrfToken, clientId));
  },
  fetchCancelTransformJob: (userId, datasetId, projectId, csrfToken, clientId) => {
    dispatch(actionsDatasets.fetchCancelTransformJob(userId, datasetId, projectId, csrfToken, clientId));
  },
  removeDatasetFromProject: (projectId, datasetId, clientId) => {
    dispatch(actionsDatasets.removeDatasetFromProject({ projectId, datasetId, clientId }));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(DatasetRowCascadeMenu));
