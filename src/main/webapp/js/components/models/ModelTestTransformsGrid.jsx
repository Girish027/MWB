import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Table } from '@tfs/ui-components';
import Constants from 'constants/Constants';

export default class ModelTestTransformsGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  render() {
    return (
      <Table
        id="ModelTestTransformsGrid"
        resizable
        data={this.props.data}
        columns={this.columns}
        showPagination={false}
        defaultPageSize={100}
        defaultSorted={[
          {
            id: Constants.MODEL_TEST_TRANSFORMS_TABLE.rank.header,
            desc: false,
          },
        ]}
      />
    );
  }

  get columns() {
    const columns = [
      {
        Header: Constants.MODEL_TEST_TRANSFORMS_TABLE.rank.header,
        accessor: Constants.MODEL_TEST_TRANSFORMS_TABLE.rank.id,
        id: Constants.MODEL_TEST_TRANSFORMS_TABLE.rank.header,
        maxWidth: 200,
        Cell: (row) => <div>{row.index + 1}</div>,
      },
      {
        Header: Constants.MODEL_TEST_TRANSFORMS_TABLE.transformation.header,
        accessor: Constants.MODEL_TEST_TRANSFORMS_TABLE.transformation.id,
      },
      {
        Header: Constants.MODEL_TEST_TRANSFORMS_TABLE.result.header,
        accessor: Constants.MODEL_TEST_TRANSFORMS_TABLE.result.id,
      },
    ];
    return columns;
  }
}

ModelTestTransformsGrid.propTypes = {
  data: PropTypes.array.isRequired,
};
