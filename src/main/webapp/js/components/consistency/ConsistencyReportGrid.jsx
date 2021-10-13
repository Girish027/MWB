import React, { Component } from 'react';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import * as actionsConsistencyReport from 'state/actions/actions_consistency_report';
import * as actionsApp from 'state/actions/actions_app';
import AlertIcon from 'grommet/components/icons/base/Alert';
import FormCheckmarkIcon from 'grommet/components/icons/base/FormCheckmark';
import CellEditableManualTag from 'components/controls/grid/CellEditableManualTag';
import {
  setTag, removeTag, showBulkTagCell, hideBulkTagCell,
} from 'state/actions/actions_datasets_transformed_tag';
import { modalDialogChange } from 'state/actions/actions_app';
import TaggerGrid from 'components/controls/grid/TaggerGrid';
import BulkTagCellEditable from 'components/tagging/grid/BulkTagCellEditable';
import SimpleDialog from 'components/controls/SimpleDialog';
import { getLanguage } from 'state/constants/getLanguage';
import Constants from 'constants/Constants';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;
import {
  Xmark,
  Tag,
  ContextualActionsBar,
  ContextualActionItem,
} from '@tfs/ui-components';

export const stateKey = 'ConsistencyReportGridState-v1';

export class ConsistencyReportGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.handleApplySuggestedClick = this.handleApplySuggestedClick.bind(this);
    this.handleBulkTagClick = this.handleBulkTagClick.bind(this);
    this.handleBulkUnTagClick = this.handleBulkUnTagClick.bind(this);
    this.handleOnClickOk = this.handleOnClickOk.bind(this);

    this.state = {
      selectedIndexes: [],
      selectedTranscriptionHashes: [],
      selectedConflictRowsWithSuggestedIntent: [],
      selectedSuggestedIntents: [],
      bulkTagFormVisible: false,
    };
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
    const { onTagsChange, dispatch } = this.props;
    const {
      projectId, searchResults, showBulkTag, updateBulkTag,
    } = this.props.consistencyReport;

    if (!searchResults.length) {
      this.setState({
        selectedIndexes: [],
        selectedTranscriptionHashes: [],
        selectedConflictRowsWithSuggestedIntent: [],
        selectedSuggestedIntents: [],
        bulkTagFormVisible: false,
      });
      return;
    }

    const selectedIndexes = this.getSelectedIndexes();
    const selectedTranscriptionHashes = [];
    const selectedConflictRowsWithSuggestedIntent = [];
    const selectedSuggestedIntentsObj = {};

    selectedIndexes.forEach((index) => {
      const row = typeof searchResults[index] !== 'undefined' ? searchResults[index] : {};
      if (!row.transcriptionHash) {
        return;
      }
      selectedTranscriptionHashes.push(row.transcriptionHash);
      if (row.intentConflict && row.suggestedIntent) {
        selectedConflictRowsWithSuggestedIntent.push(row);
        selectedSuggestedIntentsObj[row.suggestedIntent] = true;
      }
    });

    this.setState({
      selectedIndexes,
      selectedTranscriptionHashes,
      selectedConflictRowsWithSuggestedIntent,
      selectedSuggestedIntents: Object.keys(selectedSuggestedIntentsObj),
      bulkTagFormVisible: showBulkTag && selectedTranscriptionHashes.length,
    });

    if (showBulkTag && !selectedTranscriptionHashes.length) {
      dispatch(hideBulkTagCell());
    }
    if (showBulkTag && selectedTranscriptionHashes.length && updateBulkTag) {
      dispatch(setTag({
        projectId,
        hashList: selectedTranscriptionHashes,
        intent: updateBulkTag,
        isUpdate: true,
      })).then(() => {
        dispatch(hideBulkTagCell());
        if (onTagsChange) {
          onTagsChange();
        }
      });
    }
  }

  handleOnClickOk(transcriptionHashesBySuggestedIntent) {
    const { dispatch, onTagsChange, consistencyReport } = this.props;
    const { projectId } = consistencyReport;

    let promises = [],
      suggestedIntent;
    for (suggestedIntent in transcriptionHashesBySuggestedIntent) {
      if (!transcriptionHashesBySuggestedIntent.hasOwnProperty(suggestedIntent)) {
        continue;
      }
      promises.push(dispatch(setTag({
        projectId,
        hashList: transcriptionHashesBySuggestedIntent[suggestedIntent],
        intent: suggestedIntent,
        isUpdate: true,
      })));
    }

    Promise.all(promises)
      .then(() => {
        if (onTagsChange) {
          onTagsChange();
        }
      })
      .catch(() => {
      });

    dispatch(modalDialogChange(null));
  }

  getSelectedIndexes() {
    const { selection } = this.props;
    const currentSelection = selection.get(this.config.stateKey);
    if (currentSelection) {
      const selectionData = currentSelection.toJS();
      if (selectionData.indexes) {
        return [...selectionData.indexes];
      }
    }
    return [];
  }

  handleApplySuggestedClick() {
    const { dispatch } = this.props;
    const { selectedConflictRowsWithSuggestedIntent } = this.state;

    let transcriptionHashesBySuggestedIntent = {},
      differentSuggestedCount = 0,
      lastSuggested = '';

    if (selectedConflictRowsWithSuggestedIntent.length) {
      selectedConflictRowsWithSuggestedIntent.forEach((row) => {
        if (!row.suggestedIntent || !row.transcriptionHash) {
          return;
        }
        if (!transcriptionHashesBySuggestedIntent[row.suggestedIntent]) {
          transcriptionHashesBySuggestedIntent[row.suggestedIntent] = [];
          differentSuggestedCount++;
          lastSuggested = row.suggestedIntent;
        }
        transcriptionHashesBySuggestedIntent[row.suggestedIntent].push(row.transcriptionHash);
      });

      dispatch(modalDialogChange({
        type: Constants.DIALOGS.SIMPLE_DIALOG,
        header: 'Apply Suggested Intents',
        message: (
          <div>
            <div>
              You are about to tag
              {' '}
              <b>{selectedConflictRowsWithSuggestedIntent.length}</b>
              {' '}
              of transcriptions with
              {differentSuggestedCount < 2 ? (
                <span>
                  {' '}
                  tag "
                  {lastSuggested}
                  ".
                </span>
              ) : (
                <span>
                  <b>
                    {' '}
                    {differentSuggestedCount}
                  </b>
                  {' '}
                  corresponding suggested tags.
                </span>
              )}
            </div>
            <p>
              Are you sure?
            </p>
          </div>
        ),
        actions: [SimpleDialog.ACTION_OK, SimpleDialog.ACTION_CANCEL],
        className: 'ConsistencyReportConfirmApplySuggested',
        onOk: () => this.handleOnClickOk(transcriptionHashesBySuggestedIntent),
      }));
    }
  }

  handleBulkTagClick() {
    const { selectedIndexes } = this.state;

    if (selectedIndexes.length) {
      this.props.dispatch(showBulkTagCell());
    }
  }

  handleBulkUnTagClick() {
    const { consistencyReport, onTagsChange, dispatch } = this.props;
    const { selectedTranscriptionHashes, selectedIndexes } = this.state;
    const { projectId } = consistencyReport;

    if (selectedIndexes.length) {
      dispatch(removeTag({ projectId, hashlist: selectedTranscriptionHashes }))
        .then(() => {
          if (onTagsChange) {
            onTagsChange();
          }
        })
        .catch(() => {
        });
    }
  }

  render() {
    const { paddingTop } = this.props;
    const {
      selectedIndexes, selectedConflictRowsWithSuggestedIntent,
      selectedSuggestedIntents, bulkTagFormVisible,
    } = this.state;
    const { isSearching, isError, projectId } = this.props.consistencyReport;
    const style = {};
    if (isSearching || isError) {
      style.display = 'none';
    }
    if (paddingTop) {
      style.paddingTop = `${paddingTop}px`;
    }

    const bulkCellData = {
      column: null,
      value: 0,
      row: { manualTag: '' },
      key: 0,
      index: 0,
    };

    return (
      <div
        id="ConsistencyReportGridContainer"
        style={style}
      >
        <div
          id="ConsistencyReportGridActions"
          direction="row"
          style={{
          }}
        >
          {/* <Box flex>
            <div className="PageInfo">
                            Page {currentPage} <span className="GrayText">of {maxPage} [{total} Search Result{total == 1 ? '' : 's'}]</span>
                            &nbsp;
              {selectedIndexes.length ? <span>
                {selectedIndexes.length} Row{selectedIndexes.length == 1 ? '' : 's'} Selected
              </span> : null}
            </div>
          </Box> */}
          <div className="ActionsContainer" direction="row">
            {bulkTagFormVisible
              ? (
                <div className="bulkTagLabelCellContainer">
                  <span className="bulkTagLabel">Bulk Tag:&nbsp;</span>
                  <BulkTagCellEditable cellData={bulkCellData} projectId={projectId} suggestedIntents={selectedSuggestedIntents} />
                </div>
              )
              : (
                <ContextualActionsBar>
                  <ContextualActionItem
                    onClickAction={this.handleApplySuggestedClick}
                    icon={Tag}
                    disabled={!selectedConflictRowsWithSuggestedIntent.length}
                    styleOverride={{ paddingLeft: '20px' }}
                  >
                    APPLY SUGGESTED
                  </ContextualActionItem>
                  <ContextualActionItem
                    disabled={!selectedIndexes.length}
                    onClickAction={this.handleBulkTagClick}
                    icon={Tag}
                  >
                    BULK TAG
                  </ContextualActionItem>
                  <ContextualActionItem
                    disabled={!selectedIndexes.length}
                    onClickAction={this.handleBulkUnTagClick}
                    icon={Xmark}
                  >
                    BULK UNTAG
                  </ContextualActionItem>
                </ContextualActionsBar>
              )
            }
          </div>
        </div>

        <div>
          <TaggerGrid
            id="ConsistencyReportGrid"
            className="ConsistencyReportGrid"
            config={this.config}
          />
        </div>
      </div>
    );
  }

  get config() {
    const { dispatch } = this.props;
    const height = '';

    const stateful = true;

    const emptyDataMessage = 'No Search Results';

    const columns = [
      {
        name: 'Count',
        width: '5%',
        dataIndex: 'documentCount',
        sortable: true,
        hideable: true,
        resizable: false,
        hidden: true,
      },
      {
        name: 'Has Conflict',
        width: '3%',
        dataIndex: 'intentConflict',
        sortable: true,
        hideable: true,
        resizable: false,
        renderer: ({ column, value, row }) => (value
          ? <span className="HasConflictIconContainer"><AlertIcon /></span>
          : <span className="NoConflictIconContainer"><FormCheckmarkIcon /></span>),
      },
      {
        name: 'Text String',
        width: '20%',
        dataIndex: 'textStringForTagging',
        sortable: true,
        hideable: false,
      },
      {
        name: 'Normalized Form',
        width: '15%',
        dataIndex: 'normalizedForm',
        sortable: true,
        hideable: true,
      },
      {
        name: 'Group',
        width: '10%',
        dataIndex: 'normalizedFormGroup',
        sortable: true,
        hideable: true,
      },
      {
        name: 'Original Intent',
        width: '15%',
        dataIndex: 'intent',
        sortable: true,
        hideable: true,
        renderer: ({ column, value, row }) => {
          const state = store.getState();
          const { consistencyReport } = state;
          const { projectId } = consistencyReport;

          return (
            <CellEditableManualTag
              stateKey="ConsistencyReport"
              value={row.intent}
              rowIndex={row._rowIndex}
              columnIndex={0}
              maxRowIndex={consistencyReport.searchResults ? consistencyReport.searchResults.length - 1 : 0}
              maxColumnIndex={0}
              stopClickPropagation
              projectId={consistencyReport.projectId}
              highlightTags={row.suggestedIntent ? [row.suggestedIntent] : []}
              activateNextCellOnEnter={false}
              onChange={(newValue) => {
                if (newValue.length) {
                  dispatch(setTag({
                    projectId,
                    hashList: [row.transcriptionHash],
                    intent: newValue,
                    isUpdate: true,
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagUpdated));
                    });
                } else {
                  dispatch(removeTag({
                    projectId,
                    hashList: [row.transcriptionHash],
                  }))
                    .then(() => {
                      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagRemoved));
                    });
                }
              }}
            />
          );
        },
      },
      {
        name: 'Suggested Intent',
        width: '15%',
        dataIndex: 'suggestedIntent',
        sortable: true,
        hideable: true,
      },
      {
        name: 'Other Intents With Same Form',
        width: '20%',
        dataIndex: 'intents',
        sortable: true,
        hideable: true,
        renderer: ({ column, value, row }) => (value && value.length ? value.join(', ') : ''),
      },
    ];

    const dataSource = function getData(arg1, arg2, arg3) {
      const state = store.getState();
      const sort = arg3 ? arg3.sort : state.consistencyReport.sort;
      const {
        projectId, query, filter, startIndex, limit,
      } = state.consistencyReport;

      return new Promise((resolve, reject) => {
        dispatch(actionsConsistencyReport.requestSearch({
          projectId,
          query,
          filter,
          sort,
          startIndex,
          limit,
        }))
          .then(() => {
            const state = store.getState();
            resolve({
              data: state.consistencyReport.searchResults,
              total: state.consistencyReport.total,
            });
          })
          .catch((error) => {
            reject(error);
          });
      });
    };

    const plugins = {
      COLUMN_MANAGER: {
        resizable: true,
        moveable: true,
        minColumnWidth: 8,
        sortable: {
          enabled: true,
          method: 'remote',
          sortingSource: dataSource,
        },
      },
      GRID_ACTIONS: {
        iconCls: 'action-icon',
        menu: [],
      },
      SELECTION_MODEL: {
        enabled: true,
        mode: 'checkbox-multi',
        allowDeselect: true,
      },
    };

    const events = {

    };

    return {
      stateKey,
      height,
      stateful,
      columns,
      plugins,
      dataSource,
      emptyDataMessage,
      events,
    };
  }
}

const mapStateToProps = state => ({
  app: state.app,
  consistencyReport: state.consistencyReport,
  selection: state.selection,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ConsistencyReportGrid);
