import React, { Component } from 'react';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import * as actionsTag from 'state/actions/actions_datasets_transformed_tag';
import * as actionsApp from 'state/actions/actions_app';
import { Actions as GridActions } from 'react-redux-grid';
import validationUtil from 'utils/ValidationUtil';
import * as _ from 'lodash';
import { Button } from '@tfs/ui-components';
import BulkTagCellEditable from './BulkTagCellEditable';

class GridBulkTag extends Component {
  constructor(props, context) {
    super(props, context);

    this.handleBulkTagClick = this.handleBulkTagClick.bind(this);
    this.handleBulkUnTagClick = this.handleBulkUnTagClick.bind(this);
    this.manualBulkTag = this.manualBulkTag.bind(this);
    this.getAllIds = this.getAllIds.bind(this);

    this.state = {
      bulkTagIntent: '',
      tagCellDisplay: 'none',
      btnDisplay: 'block',
      mode: 'choose',
    };
  }

  componentWillReceiveProps(nextProps/* , nextState */) {
    this.props = nextProps;
    const newState = {};
    newState.bulkTagIntent = this.props.tagDatasets.bulkTagIntent;

    if (this.props.tagDatasets.activeRowData !== null) {
      newState.mode = 'choose';
    }

    if (this.props.tagDatasets.activeRowData !== null && this.state.mode === 'choose') {
      this.showTagUntagButtons();
    } else if (this.state.mode === 'tag' && this.state.bulkTagIntent !== '' && this.state.tagCellDisplay === 'block') {
      this.manualBulkTag(this.state.bulkTagIntent);
      this.toggleTagCellDisplay();
    }

    const isBulkTagMode = this.props.tagDatasets.bulkTagMode;
    if (!isBulkTagMode) {
      newState.mode = 'choose';
    } else {
      newState.mode = 'tag';
    }
    this.setState(newState);
  }

  handleBulkTagClick(event) {
    event.preventDefault();
    this.setState({
      mode: 'tag',
      bulkTagIntent: '', // clear the intent each time
    });
    this.props.showBulkTagCell();
    this.toggleTagCellDisplay();
  }

  handleBulkUnTagClick(event) {
    event.preventDefault();
    this.setState({
      mode: 'untag',
    });
    this.manualBulkUnTag();
  }

  toggleTagCellDisplay() {
    const newTagCellDisplay = (this.state.tagCellDisplay === 'block') ? 'none' : 'block';
    const newBtnDisplay = (this.state.btnDisplay === 'block') ? 'none' : 'block';
    this.setState({
      tagCellDisplay: newTagCellDisplay,
      btnDisplay: newBtnDisplay,
    });
  }

  showTagUntagButtons() {
    const newTagCellDisplay = 'none';
    const newBtnDisplay = 'block';
    this.setState({
      mode: 'choose', tagCellDisplay: newTagCellDisplay, btnDisplay: newBtnDisplay,
    });
  }

  handleSort(event) {
    event.preventDefault();
  }

  getAllIds() {
    const allIds = [];
    _.times(
      this.props.tagDatasets.limit,
      (rowIndex) => { allIds.push(rowIndex); },
    );

    return allIds;
  }

  getSelectedIds() {
    const selection = this.props.selection.get('workbench');
    const selectionData = selection.toJS();

    /*
         issue with selection.get("indexes")
         https://github.com/bencripps/react-redux-grid/issues/145
         */

    // const selectedIds = (this.props.tagDatasets.allRowsSelected) ? this.getAllIds() : selection.get("indexes");
    const selectedIds = [];
    for (const key in selectionData) {
      if (selectionData[key]) {
        const selectedId = key.split('row-')[1];
        if (String(Number(selectedId)) !== 'NaN') selectedIds.push(Number(selectedId));
      }
    }

    if (undefined !== selectedIds) {
      return selectedIds;
    }
    this.props.displayWarningRequestMessage('No rows selected');
    this.props.removeTagSuggest();

    return [];
  }

  manualBulkTag(bulkTagIntent) {
    const invalid = validationUtil.validateTopicGoal(bulkTagIntent);
    if (invalid) {
      this.props.propagateInvalidIntentMessage(bulkTagIntent);
      return false;
    }

    const selectedIds = this.getSelectedIds();
    const { userName = 'unknown' } = this.props.app;

    const oldSearchResults = this.props.tagDatasets.searchResults;

    // get selected rows and tag them with this intent
    // some rows may already have an intent
    const bulkTaggedRowsNew = [];
    const bulkTaggedRowsUpdated = [];

    _.each(oldSearchResults, (row) => {
      _.each(selectedIds, (selectedRowId) => {
        if (selectedRowId === row.id) {
          const alreadyTagged = (row.fullResult.intent !== '');

          row.manualTag = bulkTagIntent;
          row.fullResult.intent = bulkTagIntent;
          row.fullResult.taggedAt = Number(new Date());
          row.fullResult.taggedBy = userName;

          if (alreadyTagged) {
            bulkTaggedRowsUpdated.push({ ...row });
          } else {
            bulkTaggedRowsNew.push({ ...row });
          }
        }
      });
    });

    // now get the hash lists for the new and updated rows
    const transcriptionHashListForNewlyTaggedRows = [];
    _.each(bulkTaggedRowsNew, (row) => {
      const transcriptionHash = row.fullResult.transcriptionHash;
      transcriptionHashListForNewlyTaggedRows.push(transcriptionHash);
    });

    const transcriptionHashListForUpdatedTaggedRows = [];
    _.each(bulkTaggedRowsUpdated, (row) => {
      const transcriptionHash = row.fullResult.transcriptionHash;
      transcriptionHashListForUpdatedTaggedRows.push(transcriptionHash);
    });

    // ensure manual tags are properly set and match the intent on the fullResult
    const newSearchResults = _.map(oldSearchResults, (row) => {
      row.manualTag = row.fullResult.intent;
      return { ...row };
    });

    let newResultsUpdate = false;

    if (
      transcriptionHashListForUpdatedTaggedRows.length === 0
            && transcriptionHashListForNewlyTaggedRows.length > 0
            && typeof bulkTagIntent !== 'undefined'
            && !this.props.tagDatasets.fetching
    ) {
      this.props.fetchManualBulkTag({
        projectId: this.props.tagDatasets.projectId,
        intent: bulkTagIntent,
        userName,
        hashlist: transcriptionHashListForNewlyTaggedRows,
        newSearchResults,
      });
      newResultsUpdate = true;
    }

    if (
      transcriptionHashListForNewlyTaggedRows.length === 0
            && transcriptionHashListForUpdatedTaggedRows.length > 0
            && typeof bulkTagIntent !== 'undefined'
            && !this.props.tagDatasets.fetching
    ) {
      this.props.fetchManualBulkTagUpdate({
        projectId: this.props.tagDatasets.projectId,
        intent: bulkTagIntent,
        userName,
        hashlist: transcriptionHashListForUpdatedTaggedRows,
        newSearchResults,
      });
      newResultsUpdate = true;
    }

    if (
      transcriptionHashListForNewlyTaggedRows.length > 0
            && transcriptionHashListForUpdatedTaggedRows.length > 0
            && typeof bulkTagIntent !== 'undefined'
            && !this.props.tagDatasets.fetching
    ) {
      const hashObj = { new: transcriptionHashListForNewlyTaggedRows, updated: transcriptionHashListForUpdatedTaggedRows };
      this.props.fetchManualBulkTagMixed({
        projectId: this.props.tagDatasets.projectId,
        intent: bulkTagIntent,
        userName,
        hashObj,
        newSearchResults,
      });
      newResultsUpdate = true;
    }

    if (newResultsUpdate) {
      store.dispatch(GridActions.GridActions.setData({ data: newSearchResults, stateKey: 'workbench' }));
    }
  }

  manualBulkUnTag() {
    this.setState({
      bulkTagIntent: '',
    });

    const selectedIds = this.getSelectedIds();
    const { userName = 'unknown' } = this.props.app;

    const oldSearchResults = this.props.tagDatasets.searchResults;

    // get selected rows and untag them only if they have been tagged
    const bulkTaggedRows = [];
    _.each(oldSearchResults, (row) => {
      _.each(selectedIds, (selectedRowId) => {
        if (selectedRowId === row.id && row.fullResult.intent !== '') {
          row.manualTag = '';
          row.fullResult.intent = '';
          row.fullResult.deletedAt = Number(new Date());
          row.fullResult.deletedBy = userName;
          bulkTaggedRows.push({ ...row });
        }
      });
    });

    // now get the hash list
    const transcriptionHashList = [];
    _.each(bulkTaggedRows, (row) => {
      const transcriptionHash = row.fullResult.transcriptionHash;
      transcriptionHashList.push(transcriptionHash);
    });

    // ensure manual tags are properly set and match the intent on the fullResult
    const newSearchResults = _.map(oldSearchResults, (row) => {
      row.manualTag = row.fullResult.intent;
      return { ...row };
    });

    if (transcriptionHashList.length > 0 && !this.props.tagDatasets.fetching) {
      this.props.fetchDeleteManualBulkTag({
        projectId: this.props.tagDatasets.projectId,
        userName,
        hashlist: transcriptionHashList,
        newSearchResults,
      });

      store.dispatch(GridActions.GridActions.setData({ data: newSearchResults, stateKey: 'workbench' }));
    }
  }


  render() {
    const column = null;
    const value = 0;
    const row = { manualTag: '' };
    const key = 0;
    const index = 0;
    const cellData = {
      column, value, row, key, index,
    };

    const tagCellStyle = { display: this.state.tagCellDisplay };
    const btnStyle = { display: this.state.btnDisplay };

    const getBulkEditableCell = () => {
      if (this.state.tagCellDisplay === 'block') {
        return (
          <div className="bulkTagLabelCellContainer">
            <span className="bulkTagLabel">Bulk Tag:&nbsp;</span>
            <BulkTagCellEditable cellData={cellData} />
          </div>
        );
      }
      return null;
    };

    const getBulkTag = () => {
      if (this.props.tagDatasets.searchResults.length > 0) {
        return (
          <div className="bulkTagContainer">
            <div className="bulkSuggestContainer" style={tagCellStyle}>
              {getBulkEditableCell()}
            </div>
            <div className="bulkBtnContainer" style={btnStyle}>
              <Button
                name="bulk-untag"
                onClick={this.handleBulkUnTagClick}
                styleOverride={{
                  width: '150px',
                }}
              >
                BULK UNTAG
              </Button>
            </div>
            <div className="bulkBtnContainer" style={btnStyle}>
              <Button
                name="bulk-tag"
                onClick={this.handleBulkTagClick}
                styleOverride={{
                  width: '150px',
                }}
              >
                BULK TAG
              </Button>
            </div>
          </div>
        );
      }
      return null;
    };

    return (
      <div>
        { getBulkTag() }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  app: state.app,
  header: state.header,
  tagDatasets: state.tagDatasets,
  grid: state.grid,
  dataSource: state.dataSource,
  selection: state.selection,
});

const mapDispatchToProps = dispatch => ({
  displayWarningRequestMessage: (message) => {
    dispatch(actionsApp.displayWarningRequestMessage(message));
  },
  showBulkTagCell: () => {
    dispatch(actionsTag.showBulkTagCell());
  },
  fetchManualBulkTag: ({
    projectId, intent, userName, hashlist, newSearchResults,
  }) => {
    dispatch(actionsTag.fetchManualBulkTag({
      projectId, intent, userName, hashlist, newSearchResults,
    }));
  },
  fetchManualBulkTagUpdate: ({
    projectId, intent, userName, hashlist, newSearchResults,
  }) => {
    dispatch(actionsTag.fetchManualBulkTagUpdate({
      projectId, intent, userName, hashlist, newSearchResults,
    }));
  },
  fetchManualBulkTagMixed: ({
    projectId, intent, userName, hashObj, newSearchResults,
  }) => {
    dispatch(actionsTag.fetchManualBulkTagMixed({
      projectId, intent, userName, hashObj, newSearchResults,
    }));
  },
  fetchDeleteManualBulkTag: ({
    projectId, userName, hashlist, newSearchResults,
  }) => {
    dispatch(actionsTag.fetchDeleteManualBulkTag({
      projectId, userName, hashlist, newSearchResults,
    }));
  },
  propagateInvalidIntentMessage: (intent) => {
    dispatch(actionsTag.propagateInvalidIntentMessage(intent));
  },
  removeTagSuggest: () => {
    dispatch(actionsTag.removeTagSuggest());
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(GridBulkTag);
