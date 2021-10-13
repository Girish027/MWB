import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  Table, DropDown, LegacyGrid, LegacyRow, ContextualActionsBar, ContextualActionItem,
} from '@tfs/ui-components';
import _ from 'lodash';
import BatchTestConstants from 'constants/BatchTestConstants';

export const stateKey = 'ModelTestBatchResultsGrid-v3';

export default class ModelTestBatchResultsGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.state = {
      tableData: [],
      filteredData: [],
      selectedIntent: BatchTestConstants.FILTER_SELECTED_ALL,
      selectedType: BatchTestConstants.FILTER_SELECTED_ALL,
      listOfIntents: [BatchTestConstants.FILTER_SELECTED_ALL],
    };

    this.onClickTranscription = this.onClickTranscription.bind(this);
    this.getScore = this.getScore.bind(this);
    this.setIntentsFilter = this.setIntentsFilter.bind(this);
    this.setTranscriptionFilter = this.setTranscriptionFilter.bind(this);
  }

  static getDerivedStateFromProps(props, state) {
    const tableData = [...props.data];
    let {
      listOfIntents, filteredData, selectedIntent, selectedType,
    } = state;

    switch (selectedType) {
    case BatchTestConstants.TRANSCRIPTIONS_FILTER_LIST[1]:
      filteredData = tableData.filter((entry) => {
        const intentValue = entry[BatchTestConstants.ROLLUP_INTENT_ROW_ID] || entry.intent;
        const predictedRUI1Value = entry[BatchTestConstants.PREDICTED_RUI_1] || entry.predict1;
        return intentValue !== predictedRUI1Value;
      });
      break;
    case BatchTestConstants.FILTER_SELECTED_ALL:
    default:
      filteredData = [...tableData];
      break;
    }

    switch (selectedIntent) {
    case BatchTestConstants.FILTER_SELECTED_ALL:
      break;
    default:
      filteredData = filteredData.filter((entry) => {
        const intentValue = entry[BatchTestConstants.ROLLUP_INTENT_ROW_ID] || entry.intent;
        return intentValue === selectedIntent;
      });
      break;
    }

    return ({
      tableData,
      filteredData,
      listOfIntents,
    });
  }

  componentDidMount() {
    let { listOfIntents, tableData } = this.state;
    // one time operation - get unique intents from table
    if (listOfIntents.length === 1) {
      const intents = tableData.map(entry => entry[BatchTestConstants.ROLLUP_INTENT_ROW_ID] || entry.intent)
        .filter((intent, index, intentArray) => intent && intentArray.indexOf(intent) === index);
      listOfIntents = [BatchTestConstants.FILTER_SELECTED_ALL, ...intents];
      this.setState({
        listOfIntents,
      });
    }
  }

  onClickTranscription(e) {
    this.props.onRunSingleUtterance(e.target.innerText);
  }

  getScore(value) {
    if (!_.isNil(value) && value.length > 0) {
      return `${parseFloat(value).toFixed(4)}`;
    }

    if (_.isNil(value)) {
      return '';
    }
    return value;
  }

  setTranscriptionFilter(selectedType) {
    this.setState({
      selectedType,
    });
  }

  setIntentsFilter(selectedIntent) {
    this.setState({
      selectedIntent,
    });
  }

  render() {
    return (
      <LegacyGrid>
        <LegacyRow>
          <ContextualActionsBar>
            <ContextualActionItem>
              <DropDown
                key="transcriptions"
                itemList={BatchTestConstants.TRANSCRIPTIONS_FILTER_LIST}
                labelName={BatchTestConstants.TRANSCRIPTIONS_FILTER_NAME}
                selectedIndex={0}
                onItemSelected={this.setTranscriptionFilter}
              />
            </ContextualActionItem>
            <ContextualActionItem>
              <DropDown
                key="intents"
                itemList={this.state.listOfIntents}
                labelName={BatchTestConstants.INTENT_FILTER_NAME}
                selectedIndex={0}
                onItemSelected={this.setIntentsFilter}
              />
            </ContextualActionItem>
          </ContextualActionsBar>
        </LegacyRow>
        <LegacyRow>
          <Table
            style={{ maxHeight: 'calc(95vh - 260px)' }}
            resizable
            data={this.state.filteredData}
            columns={this.tableColumns}
            showPagination
            defaultPageSize={BatchTestConstants.DEFAULT_PAGE_SIZE}
          />
        </LegacyRow>
      </LegacyGrid>
    );
  }

  get tableColumns() {
    const ruRowId = BatchTestConstants.ROLLUP_INTENT_ROW_ID;
    const ruPredict1Id = BatchTestConstants.PREDICTED_RUI_1;
    const ruPredict2Id = BatchTestConstants.PREDICTED_RUI_2;
    const ruPredict3Id = BatchTestConstants.PREDICTED_RUI_3;
    const ruPredict4Id = BatchTestConstants.PREDICTED_RUI_4;
    const ruPredict5Id = BatchTestConstants.PREDICTED_RUI_5;
    const columnDefs = [
      {
        Header: 'Transcription',
        id: 'transcription',
        width: 150,
        accessor: '',
        Cell: (row) => {
          let className = 'test-result-batch-results-transcription';
          return (
            <span
              className={className}
              onClick={this.onClickTranscription}
            >
              {row.value.transcription}
            </span>
          );
        },
      },
      {
        Header: ruRowId,
        id: 'rollupIntent',
        width: 180,
        accessor: '',
        Cell: (row) => {
          let className = '';
          const ruIntentValue = row.value[ruRowId] || row.value.intent;
          const ruPredict1Value = row.value[ruPredict1Id] || row.value.predict1;
          if (ruIntentValue !== ruPredict1Value) {
            className = `${className} test-result-mismatch`;
          }
          return (
            <span className={className}>
              {ruIntentValue}
            </span>
          );
        },
      },
      {
        Header: 'Count',
        id: 'count',
        accessor: '',
        Cell: row => row.value.count || '',
      },
      {
        Header: ruPredict1Id,
        id: 'Predicted_RUI1',
        width: 150,
        accessor: '',
        Cell: (row) => {
          const ruIntentValue = row.value[ruRowId] || row.value.intent;
          const ruPredict1Value = row.value[ruPredict1Id] || row.value.predict1;
          let className = '';
          if (ruIntentValue !== ruPredict1Value) {
            className = `${className} test-result-mismatch`;
          }
          return (
            <span className={className}>
              {ruPredict1Value}
            </span>
          );
        },
      },
      {
        Header: 'Score 1',
        id: 'score1',
        accessor: '',
        Cell: (row) => {
          const ruIntentValue = row.value[ruRowId] || row.value.intent;
          const ruPredict1Value = row.value[ruPredict1Id] || row.value.predict1;
          let score = row.value.score1;
          if (!_.isNil(score)) {
            score = `${parseFloat(score).toFixed(4)}`;
          }
          let className = '';
          if (ruIntentValue !== ruPredict1Value) {
            className = `${className} test-result-mismatch`;
          }
          return (
            <span className={className}>
              {score}
            </span>
          );
        },
      },
      {
        Header: ruPredict2Id,
        id: 'Predicted_RUI2',
        width: 150,
        accessor: '',
        Cell: row => row.value[ruPredict2Id] || row.value.predict2 || '',
      },
      {
        Header: 'Score 2',
        id: 'score2',
        accessor: '',
        Cell: row => this.getScore(row.value.score2) || '',
      },
      {
        Header: ruPredict3Id,
        id: 'Predicted_RUI3',
        width: 150,
        accessor: '',
        Cell: row => row.value[ruPredict3Id] || row.value.predict3 || '',
      },
      {
        Header: 'Score 3',
        id: 'score3',
        accessor: '',
        Cell: row => this.getScore(row.value.score3) || '',
      },
      {
        Header: ruPredict4Id,
        id: 'Predicted_RUI4',
        width: 150,
        accessor: '',
        Cell: row => row.value[ruPredict4Id] || row.value.predict4 || '',
      },
      {
        Header: 'Score 4',
        id: 'score4',
        accessor: '',
        Cell: row => this.getScore(row.value.score4) || '',
      },
      {
        Header: ruPredict5Id,
        id: 'Predicted_RUI5',
        width: 150,
        accessor: '',
        Cell: row => row.value[ruPredict5Id] || row.value.predict5 || '',
      },
      {
        Header: 'Score 5',
        id: 'score5',
        accessor: '',
        Cell: row => this.getScore(row.value.score5) || '',
      },
    ];

    return columnDefs;
  }
}

ModelTestBatchResultsGrid.propTypes = {
  data: PropTypes.array.isRequired,
  onRunSingleUtterance: PropTypes.func,
};
