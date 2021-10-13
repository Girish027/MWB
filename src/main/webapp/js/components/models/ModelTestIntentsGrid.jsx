import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { Table } from '@tfs/ui-components';
import Constants from 'constants/Constants';

export default class ModelTestIntentsGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  get columns() {
    const columns = [
      {
        Header: Constants.MODEL_TEST_INTENTS_TABLE.rank.header,
        id: Constants.MODEL_TEST_INTENTS_TABLE.rank.header,
        accessor: Constants.MODEL_TEST_INTENTS_TABLE.rank.id,
        maxWidth: 200,
        Cell: (row) => <div>{row.index + 1}</div>,
      },
      {
        Header: Constants.MODEL_TEST_INTENTS_TABLE.intent.header,
        accessor: Constants.MODEL_TEST_INTENTS_TABLE.intent.id,
      },
      {
        Header: Constants.MODEL_TEST_INTENTS_TABLE.score.header,
        accessor: Constants.MODEL_TEST_INTENTS_TABLE.score.id,
        Cell: ({ value: score }) => {
          if (!_.isNil(score)) {
            score = `${score.toFixed(4)}`;
          }
          return score;
        },
      },
    ];
    return columns;
  }

  render() {
    return (
      <Table
        id="ModelTestIntentsGrid"
        resizable
        data={this.props.data}
        columns={this.columns}
        showPagination={false}
        defaultSorted={[
          {
            id: Constants.MODEL_TEST_INTENTS_TABLE.rank.header,
            desc: false,
          },
        ]}
      />
    );
  }
}

ModelTestIntentsGrid.propTypes = {
  data: PropTypes.array.isRequired,
};
