import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Actions } from 'react-redux-grid';
import store from 'state/configureStore';
import _ from 'lodash';

import validationUtil from 'utils/ValidationUtil';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsSearch from 'state/actions/actions_datasets_transformed_search';
import * as actionsTag from 'state/actions/actions_datasets_transformed_tag';
import { setTag, removeTag, setComment } from 'state/actions/actions_datasets_transformed_tag';
import { Actions as GridActions } from 'react-redux-grid';
import GridPager from 'components/tagging/grid/GridPager';
import CellEditable from 'components/controls/grid/CellEditable';
import CellEditableManualTag from 'components/controls/grid/CellEditableManualTag';
import * as appActions from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { IconNames } from 'utils/iconHelpers';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import { pathKey } from 'utils/apiUrls';
import { featureFlagDefinitions } from 'utils/FeatureFlags';
import { getLanguage } from 'state/constants/getLanguage';
import { Table, Button, InvertedTriangle } from '@tfs/ui-components';
import GridBulkTag from './GridBulkTag';
import Placeholder from '../../controls/Placeholder';
import tableUtils from '../../../utils/TableUtils';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

// TODO:
// 1. Enable granular intents editing and saving
// 2. editing comments
// 3. checkbox for every row
// 4. bulk tag and bulk untag - link to the selected rows
// 5. Suggestions List - style changes


export class DataSetTable extends Component {
  constructor(props) {
    super(props);

    this.handleShowHide = this.handleShowHide.bind(this);
    this.handleColumnSelector = this.handleColumnSelector.bind(this);
    this.getInvertedTriangle = this.getInvertedTriangle.bind(this);

    this.state = {
      uncheckingRows: false,
      resizedData: {},
    };

    this.localStorage_key = Constants.LOCALSTORAGE_KEY.TAG_DATASET_TABLE;

    this.tableData = {
      count: {
        header: 'Count',
        id: 'count',
      },
      datasets: {
        header: 'Datasets',
        id: 'datasetIds',
      },
      uniqueTextString: {
        header: 'Unique Text String',
        id: 'uniqueTextString',
      },
      suggestedCategory: {
        header: 'Suggested Category',
        id: 'suggestedTag',
      },
      granularIntents: {
        header: 'Granular Intent',
        id: 'manualTag',
      },
      ruTag: {
        header: 'Rollup Intent',
        id: 'rutag',
      },
      comments: {
        header: 'Comments',
        id: 'comment',
      },
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
  }

  componentDidMount() {
    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {}; } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);
    this.setState({ resizedData: storedSettings });
  }

  shouldComponentUpdate(/* nextProps, nextState */) {
    return true;
  }

  getInvertedTriangle() {
    return <InvertedTriangle />;
  }

  handleColumnSelector() {
    let storedSettings = {};
    try { storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {}; } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);
    this.setState({ resizedData: storedSettings });
  }

  handleShowHide() {
    const { dispatch } = this.props;

    dispatch(appActions.modalDialogChange({
      dispatch,
      type: Constants.DIALOGS.COLUMNSELECTOR_DIALOG,
      header: Constants.SHOW_OR_HIDE,
      columnData: this.tableData,
      localStorageKey: this.localStorage_key,
      handler: this.handleColumnSelector,
      getSelectedColumns: this.getSelectedColumns,
    }));
  }

  get gridConfig() {
    const { resizedData } = this.state;

    const columns = [
      {
        Header: this.tableData.count.header,
        accessor: this.tableData.count.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.count.id, 90),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.count.id, true),
      },
      {
        Header: this.tableData.datasets.header,
        accessor: this.tableData.datasets.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.datasets.id, 110),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.datasets.id, true),
        Cell: ({ row }) => {
          const state = store.getState();
          const { tagDatasets } = state;
          const { datasets } = tagDatasets;

          const datasetNames = [];
          if (row.datasetIds && row.datasetIds.length) {
            row.datasetIds.forEach((id) => {
              const d = datasets.get(id);
              if (d && d.name) {
                datasetNames.push(d.name);
              }
            });
          }

          return (
            <span>{datasetNames.join(', ')}</span>
          );
        },
      },
      {
        Header: this.tableData.uniqueTextString.header,
        accessor: this.tableData.uniqueTextString.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.uniqueTextString.id, 450),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.uniqueTextString.id, true),
      },
      {
        Header: this.tableData.suggestedCategory.header,
        accessor: this.tableData.suggestedCategory.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.suggestedCategory.id, 200),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.suggestedCategory.id, true),
      },
      {
        Header: this.tableData.granularIntents.header,
        accessor: this.tableData.granularIntents.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.granularIntents.id, 150),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.granularIntents.id, true),
        Cell: ({ original, index }) => {
          const { dispatch } = this.props;
          const state = store.getState();
          const { tagDatasets } = state;
          const { projectId } = tagDatasets;
          return (
            <CellEditableManualTag
              stateKey="TagDatasetsBeta"
              value={original.manualTag || ''}
              rowIndex={index}
              columnIndex={0}
              maxRowIndex={tagDatasets.searchResults ? tagDatasets.searchResults.length - 1 : 0}
              maxColumnIndex={1}
              stopClickPropagation
              projectId={tagDatasets.projectId}
              activateNextCellOnEnter
              onChange={(newValue) => {
                const oldValue = original.manualTag || '';
                // dispatch(GridActions.EditorActions.updateRow({
                //   stateKey: 'workbench', rowIndex: row.index, values: { manualTag: newValue },
                // }));

                if (newValue.length) {
                  dispatch(setTag({
                    projectId,
                    hashList: [original.transcriptionHash],
                    intent: newValue,
                    isUpdate: oldValue.length > 0,
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagUpdated));
                    })
                    .catch(() => {
                      // dispatch(GridActions.EditorActions.updateRow({
                      //   stateKey: 'workbench', rowIndex: original.index, values: { manualTag: oldValue },
                      // }));
                    });
                } else {
                  dispatch(removeTag({
                    projectId,
                    hashList: [original.transcriptionHash],
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagRemoved));
                    })
                    .catch(() => {
                      // dispatch(GridActions.EditorActions.updateRow({
                      //   stateKey: 'workbench', rowIndex: row.index, values: { manualTag: oldValue },
                      // }));
                    });
                }
              }}
            />
          );
        },
      }, {
        Header: this.tableData.ruTag.header,
        accessor: this.tableData.ruTag.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.ruTag.id, 150),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.ruTag.id, true),
      },
      {
        Header: this.tableData.comments.header,
        accessor: this.tableData.comments.id,
        sortable: true,
        minWidth: tableUtils.getColumnWidth(resizedData, this.tableData.comments.id, 110),
        show: tableUtils.getColumnVisibility(resizedData, this.tableData.comments.id, true),
      /*  Cell: ({ column, value, row }) => {
          const { dispatch } = this.props;
          const state = store.getState();
          const {tagDatasets} = state;
          const {projectId} = tagDatasets;
          return (
            <CellEditable
              stateKey="TagDatasets"
              value={row.comment || ''}
              rowIndex={row.index}
              columnIndex={1}
              maxRowIndex={tagDatasets.searchResults ? tagDatasets.searchResults.length - 1 : 0}
              maxColumnIndex={1}
              stopClickPropagation
              activateNextCellOnEnter
              validation={validationUtil.validateTranscriptionHashComment}
              onChange={(newValue) => {
                const oldValue = row.comment || '';
                dispatch(GridActions.EditorActions.updateRow({
                  stateKey: 'workbench', rowIndex: row.index, values: {comment: newValue},
                }));

                if (newValue.length) {
                  dispatch(setComment({
                    projectId,
                    hashList: [row.transcriptionHash],
                    comment: newValue,
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.commentUpdated));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: {comment: oldValue},
                      }));
                    });
                } else {
                  dispatch(setComment({
                    comment: '',
                    projectId,
                    hashList: [row.transcriptionHash],
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.commentRemoved));
                    })
                    .catch(() => {
                      dispatch(GridActions.EditorActions.updateRow({
                        stateKey: 'workbench', rowIndex: row.index, values: {comment: oldValue},
                      }));
                    });
                }
              }}
            />
          );
        } */
      },
    ];

    return columns;
  }

  handleBulkTagClick(event) {
    event.preventDefault();
    this.bulkManualTag();
  }

  // noinspection JSAnnotator
  render() {
    const { paddingTop } = this.props;

    if (!this.props.tagDatasets.searchResults) {
      const isLoading = this.props.tagDatasets.isLoading;
      return (
        <div className="datagridContainer transition" style={{ paddingTop: `${paddingTop}px` }}>
          {isLoading ? (
            <Placeholder message={Constants.SEARCHING_IN_PROGRESS} />
          ) : null}
        </div>
      );
    }

    const calcTotalPages = () => {
      const totalPages = Math.ceil(this.props.tagDatasets.total / this.props.tagDatasets.limit);
      return totalPages;
    };

    const getResultsMessage = () => {
      const blackStyle = { color: 'black' };
      if (this.props.tagDatasets.searchResults && this.props.tagDatasets.searchResults.length > 0) {
        const selection = this.props.selection.get('workbench');

        const selectedIds = [];
        if (undefined !== selection) {
          const selectionData = selection.toJS();
          for (const key in selectionData) {
            if (selectionData[key]) {
              const selectedId = key.split('row-')[1];
              if (String(Number(selectedId)) !== 'NaN') selectedIds.push(Number(selectedId));
            }
          }
        }

        // const totalRowsSelected = ( undefined !== this.props.selection.get("workbench") && undefined !== this.props.selection.get("workbench").get("indexes") ) ? this.props.selection.get("workbench").get("indexes").length : 0;
        const totalRowsSelected = (undefined !== selection && undefined !== selectedIds) ? selectedIds.length : 0;

        const rowsSelectedMessage = (totalRowsSelected > 0) ? `${totalRowsSelected} Rows Selected` : '';
        const totalPages = calcTotalPages();
        return (
          <div>
            <span style={blackStyle}>
Page
              {Math.min(this.props.tagDatasets.currentPage, totalPages)}
            </span>
            {' '}
            of
            {' '}
            { totalPages }
            {' '}
            [
            {' '}
            {this.props.tagDatasets.total.toLocaleString()}
            {' '}
            Search Results ]
            {' '}
            <span style={blackStyle}>{rowsSelectedMessage}</span>
          </div>
        );
      }
      return (<div style={blackStyle}>No search results found</div>);
    };

    const getActions = () => {
      const { searchResults } = this.props.tagDatasets;

      if (searchResults && searchResults.length > 0) {
        return (
          <div className="actionsTableContainer">
            <div className="columnSelector">
              <Button
                type="flat"
                className="show-hide-btn"
                name="show-hide"
                onClick={this.handleShowHide}
              >
                Columns
              </Button>
              <InvertedTriangle />
            </div>
            <div className="actionTableBtnContainer">
              <GridBulkTag />
            </div>
          </div>
        );
      }
      return (
        <div className="actionsTableContainer">
          <div className="resultsMessageContainer">
            { getResultsMessage() }
          </div>
        </div>
      );
    };

    const getTable = () => {
      const { searchResults, isLoading, limit } = this.props.tagDatasets;
      const { tableHeight } = this.props;
      let resizedData;

      if (isLoading) {
        return (
          <Placeholder message={Constants.SEARCHING_IN_PROGRESS} />
        );
      } if (
        searchResults
        && !searchResults.dropTable && searchResults.length > 0
        && !(searchResults.length === 1 && searchResults[0].fullResult.transcriptionHash === '-1')
      ) {
        return (
          <Table
            defaultPageSize={limit}
            style={tableHeight}
            data={searchResults}
            columns={this.gridConfig}
            resizable
            onResizedChange={(newResized) => {
              resizedData = tableUtils.handleTableResizeChange(newResized, this.localStorage_key);
              this.setState({
                resizedData,
              });
            }}
          />
        );
      }
      return (
        <div />
      );
    };

    return (
      <div className="datagridContainer transition" style={{ paddingTop: `${paddingTop}px` }}>
        {getActions()}
        <div className="tableContainer">
          {getTable()}
        </div>
        <div className="gridFooterForSuggestList" />
      </div>
    );
  }
}

DataSetTable.propTypes = {
  dispatch: PropTypes.func,
};

const mapStateToProps = state => ({
  app: state.app,
  tagDatasets: state.tagDatasets,
  filtersCollapsed: state.tagDatasets.isControlsCollapsed,
  header: state.header,
  grid: state.grid,
  dataSource: state.dataSource,
  data: state.data,
  selection: state.selection,
  state,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(DataSetTable);
