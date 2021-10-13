import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Table, Checkbox } from '@tfs/ui-components';
import { getFormattedDate } from 'utils/DateUtils';
import store from 'state/configureStore';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import { RouteNames } from 'utils/routeHelpers';
import { changeRoute } from 'state/actions/actions_app';

class DatasetSelectorTable extends Component {
  constructor(props, context) {
    super(props, context);

    this.state = {
      tableData: [],
      availableDatasets: [],
      selectedDatasets: [],
    };

    this.onToggleDatasetCheckbox = this.onToggleDatasetCheckbox.bind(this);
    this.toggleSelectAll = this.toggleSelectAll.bind(this);
    this.updateSelectedDatasets = this.updateSelectedDatasets.bind(this);
  }

  static getDerivedStateFromProps(props, state) {
    let tableData = props.datasets ? Array.from(props.datasets.values()) : [];
    // TODO: revisit validation logic for datasets. Move it to Utils to be used commonly.
    tableData = tableData.filter(item => item.id && item.status && item.status == 'COMPLETED' && item.name && item.isClickable);
    if (state.availableDatasets.length === 0) {
      const availableDatasets = tableData.map(dataset => dataset.id);
      return ({
        availableDatasets,
        tableData,
      });
    }
    return ({
      tableData,
    });
  }

  updateSelectedDatasets(selectedDatasets) {
    this.setState({
      selectedDatasets,
    }, () => {
      this.props.updateSelectedDatasets(selectedDatasets);
    });
  }

  onToggleDatasetCheckbox(datasetId, checked) {
    let { selectedDatasets } = this.state;
    if (checked == true) {
      selectedDatasets = [...selectedDatasets, datasetId];
    } else {
      selectedDatasets = selectedDatasets.filter(id => id !== datasetId);
    }
    this.updateSelectedDatasets(selectedDatasets);
  }

  toggleSelectAll(checked) {
    let selectedDatasets = [];
    if (checked) {
      selectedDatasets = this.state.availableDatasets;
    }
    this.updateSelectedDatasets(selectedDatasets);
  }

  render() {
    // data available = {_key, id, client, projectId, name, type, description, locale, createdAt, status, task}
    const { selectedDatasets, availableDatasets } = this.state;
    const columns = [
      {
        Header: row => (
          <Checkbox
            onChange={(event) => {
              this.toggleSelectAll(event.target.checked);
            }}
            checked={(selectedDatasets.length > 0 && selectedDatasets.length === availableDatasets.length)}
          />
        ),
        id: 'checkbox',
        accessor: '',
        Cell: row => (
          <Checkbox
            onChange={(event) => {
              this.onToggleDatasetCheckbox(row.value.id, event.target.checked);
            }}
            checked={this.state.selectedDatasets.includes(row.value.id)}
          />
        ),
        maxWidth: 100,
        sortable: false,
      }, {
        Header: 'Dataset Names',
        accessor: '',
        id: 'name',
        Cell: ({ original }) => (original.isClickable ? (
          <a
            href="javascript:;"
            onClick={() => {
              const { history, client, projectId } = this.props;
              store.dispatch(actionsTagDatasets.setIncomingFilter({ projectId, datasets: [original.id] }));
              store.dispatch(changeRoute(RouteNames.TAG_DATASETS, { client, projectId }, history));
            }}
          >
            {original.name}
          </a>
        ) : (
          <div>
            {' '}
            {original.name}
            {' '}
          </div>
        )),

      }, {
        Header: 'User Id',
        accessor: row => row.modifiedBy,
        id: 'userId',
      }, {
        Header: 'Date Created',
        accessor: row => getFormattedDate(row.createdAt),
        id: 'createdAt',
      },
    ];


    return (
      <div>
        <Table
          data={this.state.tableData}
          columns={columns}
          defaultSorted={[{ id: 'createdAt', desc: true }]}
          showPagination={false}
          defaultPageSize={1000}
        />
      </div>
    );
  }
}

DatasetSelectorTable.propTypes = {
  history: PropTypes.object.isRequired,
  client: PropTypes.object.isRequired,
  projectId: PropTypes.string.isRequired,
  datasets: PropTypes.object,
  updateSelectedDatasets: PropTypes.func,
};

DatasetSelectorTable.defaultProps = {
  datasets: {},
  updateSelectedDatasets: () => {},
};

export default DatasetSelectorTable;
