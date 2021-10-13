import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import dateFormat from 'dateformat';
import * as actionsDatasets from 'state/actions/actions_datasets';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import DatasetRowCascadeMenu from 'components/projects/datasets/DatasetRowCascadeMenu';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import { Table, StatusBadge, Tooltip } from '@tfs/ui-components';
import { connect } from 'react-redux';
import tableUtils from 'utils/TableUtils';
import Constants from 'constants/Constants';
import Placeholder from 'components/controls/Placeholder';

export const stateKey = 'ReadProjectDatasetsGridState-v2';

export class ReadProjectDatasetsGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.state = {
      resizedData: {},
    };

    this.isModified = false;

    this.localStorage_key = Constants.LOCALSTORAGE_KEY.READ_PROJECT_DATASET;
  }

  componentDidMount() {
    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {}; } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);
    this.isModified = true;
    this.setState({ resizedData: storedSettings });
  }

  getStatusBadge(status, label) {
    const progress = (status === Constants.STATUS.STARTED
      || status === Constants.STATUS.RUNNING)
      ? {
        type: 'spinner',
      }
      : {};
    const type = (status === Constants.STATUS.COMPLETED) ? 'label' : 'badge';
    return (
      <StatusBadge
        category={Constants.STATUS_CATEGORY_MAPPING[status]}
        label={label}
        onHover={() => {}}
        progress={progress}
        type={type}
        styleOverride={{
          border: '1px solid',
          borderRadius: '2px',
          fontWeight: 'bold',
        }}
      />
    );
  }

  onClickStatus(value) {
    const { app, dispatch, header } = this.props;
    const { userId, csrfToken } = app;
    const clientId = header.client.id;
    dispatch(actionsDatasets.fetchDatasetTransform(userId, value.id, clientId, value.projectId, csrfToken, false));
  }

  renderStatus = (value) => (value.status === Constants.STATUS.NULL
    ? (
      <a
        href="javascript:;"
        className="transformLink"
        onClick={() => this.onClickStatus(value)}
      >
        <span>{this.getStatus(value)}</span>
      </a>
    )
    : (
      <span>{this.getStatus(value)}</span>
    ))

  getStatus(value) {
    const percentComplete = value && value.percentComplete ? value.percentComplete : '';
    let message = '';
    let showTooltip = false;
    const status = value.status;
    let label = status;

    switch (status) {
    case 'STARTED':
      message = Constants.PROCESS_TRANSFORMATION;
      showTooltip = true;
      break;
    case 'RUNNING':
      message = Constants.PROCESS_TRANSFORMATION;
      showTooltip = true;
      break;
    case 'NULL':
      message = Constants.START_TRANSFORMATION;
      label = Constants.NEED_TRANSFORMATION;
      showTooltip = true;
      break;
    }

    return showTooltip ? (
      <Tooltip
        content={message}
        trigger="hover"
        direction="bottom"
        type="info"
      >
        <span>
          {this.getStatusBadge(status, label)}
        </span>
      </Tooltip>
    )
      : (
        <span>
          {this.getStatusBadge(status, label)}
        </span>
      );
  }

  onClickName(value) {
    const { history, dispatch } = this.props;
    dispatch(actionsTagDatasets.setIncomingFilter({
      projectId: value.projectId,
      datasets: [value.id],
    }));
    dispatch(changeRoute(RouteNames.TAG_DATASETS, {
      clientId: value.clientId,
      projectId: value.projectId,
    }, history));
  }

  renderName = (value) => {
    if (!value.isClickable) {
      return value.name;
    }
    switch (value.status) {
    case 'COMPLETED':
      return (
        <a
          href="javascript:;"
          onClick={() => this.onClickName(value)}
        >
          {value.name}
        </a>
      );

    default:
      return value.name;
    }
  }

  renderCreatedAt = (value) => {
    const dateObj = new Date(value);
    return (
      <span>
        {' '}
        {dateFormat(dateObj, 'mmmm dS, yyyy, h:MM:ss TT')}
        {' '}
      </span>
    );
  }

  render() {
    let sortedData = _.sortBy(this.props.data, 'createdAt');
    sortedData = sortedData.reverse();

    // This check is added to prevent the table from unnecesary rendering.
    if (this.isModified === false) return <Placeholder message={Constants.SEARCHING_IN_PROGRESS} />;

    return (
      <Table
        resizable
        style={{ maxHeight: 'calc(95vh - 225px)', borderLeft: 'unset' }}
        data={sortedData}
        columns={this.config}
        onResizedChange={(newResized) => {
          tableUtils.handleTableResizeChange(newResized, this.localStorage_key);
        }}
        defaultSorted={[
          {
            id: Constants.PROJECT_DATASET_TABLE.createdAt.id,
            desc: true,
          },
        ]}
      />
    );
  }

  get config() {
    const { resizedData } = this.state;

    const columns = [{
      Header: Constants.PROJECT_DATASET_TABLE.name.header,
      id: Constants.PROJECT_DATASET_TABLE.name.id,
      accessor: '',
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.name.id, 100),
      Cell: ({ value }) => (this.renderName(value)),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.description.header,
      accessor: Constants.PROJECT_DATASET_TABLE.description.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.description.id, 100),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.type.header,
      accessor: Constants.PROJECT_DATASET_TABLE.type.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.type.id, 100),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.locale.header,
      accessor: Constants.PROJECT_DATASET_TABLE.locale.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.locale.id, 100),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.status.header,
      id: Constants.PROJECT_DATASET_TABLE.status.id,
      accessor: '',
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.status.id, 100),
      Cell: ({ value }) => (this.renderStatus(value)),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.user.header,
      accessor: Constants.PROJECT_DATASET_TABLE.user.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.user.id, 100),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.createdAt.header,
      accessor: Constants.PROJECT_DATASET_TABLE.createdAt.id,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.createdAt.id, 150),
      Cell: ({ value }) => (this.renderCreatedAt(value)),
    }, {
      Header: Constants.PROJECT_DATASET_TABLE.action.header,
      id: Constants.PROJECT_DATASET_TABLE.action.id,
      accessor: '',
      sortable: false,
      minWidth: tableUtils.getColumnWidth(resizedData, Constants.PROJECT_DATASET_TABLE.action.id, 100),
      style: {
        paddingTop: '0px',
        paddingBottom: '0px',
      },
      Cell: ({ value }) => (
        <DatasetRowCascadeMenu
          projectId={value.projectId}
          datasetId={value.id}
        />
      ),
    }];
    return columns;
  }
}

ReadProjectDatasetsGrid.propTypes = {
  data: PropTypes.array.isRequired,
  app: PropTypes.object,
  header: PropTypes.object,
  dispatch: PropTypes.func,
};

const mapStateToProps = (state, ownProps) => ({
  app: state.app,
  header: state.header,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ReadProjectDatasetsGrid);
