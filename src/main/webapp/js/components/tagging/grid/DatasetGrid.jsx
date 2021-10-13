import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Actions } from 'react-redux-grid';
import { Grid } from 'react-redux-grid';
import * as _ from 'lodash';
import * as actionsTag from '../../../state/actions/actions_datasets_transformed_tag';
import GridBulkTag from './GridBulkTag';

class DatasetGrid extends Component {
  constructor(props) {
    super(props);

    this.handleResize = this.handleResize.bind(this);

    this.state = {
      uncheckingRows: false,
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
  }

  componentWillMount() {
    this.addDocumentWindowListeners();
  }

  componentWillUnmount() {
    this.removeDocumentWindowListeners();
  }

  componentDidUpdate() {
    if (undefined !== document
            && undefined !== document.getElementsByClassName('react-grid-row')
            && undefined !== document.getElementsByClassName('react-grid-row')[0]) {
      const row = document.getElementsByClassName('react-grid-row')[0];
      const column = row.getElementsByTagName('TD')[0];
      const hasTabIndexSet = column.getAttribute('tabindex');

      if (this.props.tagDatasets.searchResults.length > 0 && Number(hasTabIndexSet) !== -1) {
        this.updateTabIndex();
      }
    }

    // if any rows have been checked, uncheck them after a bulk tag
    if (this.props.tagDatasets.uncheckAllRows && this.props.tagDatasets.bulkTagReceived) {
      this.uncheckAllSelectedRows();
    }
  }

  handleResize() {
    if (this.props.tagDatasets.searchResults !== null && this.props.tagDatasets.searchResults.length > 0) {
      this.forceUpdate();
    }
  }

  addDocumentWindowListeners() {
    if (window.addEventListener) {
      window.addEventListener('resize', this.handleResize, false);
    }
  }

  removeDocumentWindowListeners() {
    if (window.removeEventListener) {
      window.removeEventListener('resize', this.handleResize);
    }
  }

  handleBulkTagClick(event) {
    event.preventDefault();
    this.bulkManualTag();
  }

  updateTabIndex(/* columnIndex */) {
    const rows = document.getElementsByClassName('react-grid-row');
    _.each(rows, (row) => {
      const checkboxColumn = row.getElementsByTagName('TD')[0];
      if (undefined !== checkboxColumn) {
        checkboxColumn.setAttribute('tabindex', '-1');
        const input = checkboxColumn.getElementsByTagName('INPUT');
        if (typeof input[0] !== 'undefined') input[0].setAttribute('tabindex', '-1');
      }
    });
  }

  uncheckAllSelectedRows() {
    const rows = document.getElementsByClassName('react-grid-row');

    /*

         there is a bug in react-redux-grid where indexes isn"t updated
         https://github.com/bencripps/react-redux-grid/issues/145

         const gridMap = this.props.selection.get("workbench");

         const getAllIds = () => {

         let allIds = [];
         _.times( this.props.tagDatasets.limit, rowIndex => { allIds.push(rowIndex) })

         return allIds;
         }

         const selectedIds = (this.props.tagDatasets.allRowsSelected) ? getAllIds() : gridMap.get("indexes");
         */

    const selection = this.props.selection.get('workbench');
    const selectionData = selection.toJS();

    const selectedIds = [];
    for (const key in selectionData) {
      if (selectionData[key]) {
        const selectedId = key.split('row-')[1];
        if (String(Number(selectedId)) !== 'NaN') selectedIds.push(Number(selectedId));
      }
    }

    // only click the checkbox of selected rows
    if (selectedIds && !this.state.uncheckingRows) {
      this.setState({
        uncheckingRows: true,
      });

      _.each(selectedIds, (selectedId) => {
        const selectedRow = rows[selectedId];
        if (typeof selectedRow !== 'undefined') {
          const checkboxColumn = selectedRow.getElementsByTagName('TD')[0];
          if (typeof checkboxColumn !== 'undefined') {
            const input = checkboxColumn.getElementsByTagName('INPUT');
            input[0].checked = false;
          }
        }
      });

      // give it 2 seconds to ensure all the rows are unchecked
      setTimeout(() => {
        this.setState({
          uncheckingRows: false,
        });
        this.props.allRowsUnchecked();
        const stateKey = 'workbench';
        this.props.deselectAll(stateKey);
      }, 2000);
    }
  }


  render() {
    const { paddingTop } = this.props;

    if (!this.props.tagDatasets.searchResults) {
      const isLoading = this.props.tagDatasets.isLoading;
      return (
        <div className="datagridContainer" style={{ paddingTop: `${paddingTop}px` }}>
          {isLoading ? (
            <div className="TagDatasetLoading">
                        Searching...
            </div>
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
      if (this.props.tagDatasets.searchResults && this.props.tagDatasets.searchResults.length > 0) {
        return (
          <div className="actionsTableContainer">
            <div className="resultsMessageContainer">
              { getResultsMessage() }
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
      const { searchResults, isLoading } = this.props.tagDatasets;
      if (isLoading) {
        return (
          <div className="TagDatasetLoading">
                        Searching...
          </div>
        );
      } if (
        searchResults
                && !this.props.tagDatasets.dropTable && searchResults.length > 0
                && !(searchResults.length === 1 && searchResults[0].fullResult.transcriptionHash === '-1')
      ) {
        return (
          <Grid
            id="DatasetGridPagerSelect"
            data={this.props.tagDatasets.searchResults}
            {...this.props.config}
          />
        );
      }
      return (
        <div />
      );
    };

    return (
      <div className="datagridContainer" style={{ paddingTop: `${paddingTop}px`, top: '-1px' }}>
        {getActions()}
        <div className="tableContainer">
          {getTable()}
        </div>
        <div className="gridFooterForSuggestList" />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  app: state.app,
  tagDatasets: state.tagDatasets,
  header: state.header,
  grid: state.grid,
  dataSource: state.dataSource,
  data: state.data,
  selection: state.selection,
  state,
});

const mapDispatchToProps = dispatch => ({
  updateColumnSort: (sort) => {
    dispatch(actionsTag.updateColumnSort(sort));
  },
  allRowsUnchecked: () => {
    dispatch(actionsTag.allRowsUnchecked());
  },
  deselectAll: (stateKey) => {
    dispatch(Actions.SelectionActions.deselectAll({ stateKey }));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(DatasetGrid);
