import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Table } from '@tfs/ui-components';
import Constants from 'constants/Constants';

export default class ModelTestEntitiesGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  get columns() {
    const columns = [
      {
        Header: Constants.MODEL_TEST_ENTITIES_TABLE.name.header,
        accessor: Constants.MODEL_TEST_ENTITIES_TABLE.name.id,
        id: Constants.MODEL_TEST_ENTITIES_TABLE.name.header,
      },
      {
        Header: Constants.MODEL_TEST_ENTITIES_TABLE.value.header,
        accessor: Constants.MODEL_TEST_ENTITIES_TABLE.value.id,
      },
    ];
    return columns;
  }

  render() {
    return (
      <Table
        id="ModelTestEntitiesGrid"
        resizable
        data={this.props.data}
        columns={this.columns}
        showPagination={false}
        defaultSorted={[
          {
            id: Constants.MODEL_TEST_ENTITIES_TABLE.name.header,
            desc: false,
          },
        ]}
      />
    );
  }
}

ModelTestEntitiesGrid.propTypes = {
  data: PropTypes.array.isRequired,
};
