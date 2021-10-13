import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import {
  Table, AlertIcon, DashedSpinner, TrashIcon,
} from '@tfs/ui-components';
import * as actionsTaggingGuide from 'state/actions/actions_taggingguide';
import _ from 'lodash';
import Constants from 'constants/Constants';
import IconButton from 'components/IconButton';
import tableUtils from 'utils/TableUtils';
import Placeholder from 'components/controls/Placeholder';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import { colors } from 'styles';

const { TAGGING_GUIDE_TABLE } = Constants;

export class TaggingGuideGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.getEditorMultiline = this.getEditorMultiline.bind(this);
    this.getStatusIndicator = this.getStatusIndicator.bind(this);
    this.updateStatusForCell = this.updateStatusForCell.bind(this);

    this.handleDeleteIntent = this.handleDeleteIntent.bind(this);
    this.handleCellValueChange = this.handleCellValueChange.bind(this);

    this.timeoutCellStatus = null;

    this.isModified = false;
    this.state = {
      statusDataCell: {},
      resizedData: {},
    };

    this.status = {
      saving: 'Saving',
      failed: 'Failed to save',
      invalid: 'Invalid entry',
    };

    this.statusIcon = {
      [this.status.saving]: (<DashedSpinner height="10px" width="10px" />),
      [this.status.failed]: (<AlertIcon width="10px" height="10px" style={{ marginTop: '-3px' }} />),
      [this.status.invalid]: (<AlertIcon width="10px" height="10px" style={{ marginTop: '-3px' }} />),
    };

    this.localStorage_key = Constants.LOCALSTORAGE_KEY.INTENTS_TABLE;
  }


  componentDidMount() {
    let storedSettings = {};
    try {
      storedSettings = JSON.parse(localStorage.getItem(this.localStorage_key)) || {};
    } catch (e) { /* do nothing */ }
    storedSettings = Object.assign({}, storedSettings);
    this.isModified = true;
    this.setState({
      resizedData: storedSettings,
    });
  }

  handleCellValueChange(newValue, cellInfo) {
    const {
      dispatch, userId, csrfToken, clientId, projectId, validateData,
    } = this.props;
    const { statusDataCell } = this.state;

    const updatedData = _.cloneDeep(this.props.intents);

    const columnId = cellInfo.column.id;
    const rowId = cellInfo.row.id;
    const { index } = cellInfo;

    const oldValue = updatedData[index][columnId];

    if (oldValue !== newValue) {
      const isValid = validateData(newValue, cellInfo.column.id);
      if (isValid) {
        updatedData[cellInfo.index][columnId] = newValue;
        this.updateStatusForCell(rowId, columnId, this.status.saving);

        dispatch(actionsTaggingGuide.requestUpdateTag({
          userId, csrfToken, projectId, objectId: rowId, values: updatedData[index], clientId,
        })).then(() => {
          // clear status when response is successful
          this.updateStatusForCell(rowId, columnId, null);
        }).catch((error) => {
          this.updateStatusForCell(rowId, columnId, this.status.failed, false);
        });
      } else {
        this.updateStatusForCell(rowId, columnId, this.status.invalid, false);
      }
    } else if (statusDataCell[rowId]
        && statusDataCell[rowId][columnId]
        && statusDataCell[rowId][columnId].indexOf('not saved')) {
      this.updateStatusForCell(rowId, columnId, null);
    }
  }

  handleDeleteIntent(cellInfo) {
    const {
      dispatch, userId, csrfToken, clientId, projectId,
    } = this.props;
    dispatch(actionsTaggingGuide.requestRemoveTag({
      userId, csrfToken, projectId, objectId: cellInfo.row.id, index: cellInfo.index, clientId,
    }));
  }

  updateStatusForCell(row, column, status, reset = true) {
    const updateStatusData = _.cloneDeep(this.state.statusDataCell);
    if (!updateStatusData[row]) {
      updateStatusData[row] = {};
    }
    updateStatusData[row][column] = status;
    this.setState({ statusDataCell: updateStatusData });
  }

  componentWillUnmount() {
    clearTimeout(this.timeoutCellStatus);
  }

  getStatusIndicator(rowId, columnId) {
    const { statusDataCell } = this.state;
    const status = statusDataCell[rowId] ? statusDataCell[rowId][columnId] : undefined;
    if (status) {
      let className;
      if (status === this.status.saving) {
        className = 'greenText';
      } else {
        className = 'redText';
      }
      return (
        <span>
          {this.statusIcon[status]}
          <span className={className} style={{ paddingLeft: '5px' }}>
            {status}
          </span>
        </span>

      );
    }
    return null;
  }

  getEditorMultiline(cellInfo) {
    const {
      intents,
    } = this.props;
    const columnId = cellInfo.column.id;
    const rowId = cellInfo.row.id;

    let className = 'editable-cell';
    switch (columnId) {
    case TAGGING_GUIDE_TABLE.granularIntent.id:
    case TAGGING_GUIDE_TABLE.rollupIntent.id:
      className += ' single-line';
      break;
    default:
      break;
    }

    return (
      <div>
        <div
          className={className}
          contentEditable
          suppressContentEditableWarning

          onBlur={(event) => {
            this.handleCellValueChange(event.target.innerText, cellInfo);
          }}
          // eslint-disable-next-line react/no-danger
          dangerouslySetInnerHTML={{
            __html: (() => (intents.length
              ? intents[cellInfo.index][columnId]
              : '')
            )(),
          }}
        />
        {this.getStatusIndicator(rowId, columnId)}
      </div>
    );
  }

  get columns() {
    const { userFeatureConfiguration } = this.props;
    const { names } = featureFlagDefinitions;
    const { resizedData } = this.state;

    const colCount = {
      Header: TAGGING_GUIDE_TABLE.count.header,
      accessor: TAGGING_GUIDE_TABLE.count.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.count.id, 70),
    };
    let colGranularIntent = {
      Header: TAGGING_GUIDE_TABLE.granularIntent.header,
      accessor: TAGGING_GUIDE_TABLE.granularIntent.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.granularIntent.id, 100),
    };
    let colRollupIntent = {
      Header: TAGGING_GUIDE_TABLE.rollupIntent.header,
      accessor: TAGGING_GUIDE_TABLE.rollupIntent.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.rollupIntent.id, 100),
    };
    let colDescription = {
      Header: TAGGING_GUIDE_TABLE.description.header,
      accessor: TAGGING_GUIDE_TABLE.description.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.description.id, 100),
    };
    let colKeywords = {
      Header: TAGGING_GUIDE_TABLE.keywords.header,
      accessor: TAGGING_GUIDE_TABLE.keywords.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.keywords.id, 100),
    };
    let colExamples = {
      Header: TAGGING_GUIDE_TABLE.examples.header,
      accessor: TAGGING_GUIDE_TABLE.examples.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.examples.id, 100),
    };
    let colComments = {
      Header: TAGGING_GUIDE_TABLE.comments.header,
      accessor: TAGGING_GUIDE_TABLE.comments.id,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.comments.id, 100),
    };
    let colID = {
      Header: 'id',
      accessor: 'id',
      show: false,
    };
    const colDelete = {
      Header: TAGGING_GUIDE_TABLE.delete.header,
      accessor: TAGGING_GUIDE_TABLE.delete.id,
      sortable: false,
      minWidth: tableUtils.getColumnWidth(resizedData, TAGGING_GUIDE_TABLE.delete.id, 70),
      Cell: cellInfo => (
        <span className="tooltip-general">
          <IconButton
            onClick={() => this.handleDeleteIntent(cellInfo)}
            icon={TrashIcon}
            data-qa="delete-intents"
            title="Delete"
            width={16}
            height={16}
          />
        </span>
      ),
      show: !!(isFeatureEnabled(names.intentGuideDelete, userFeatureConfiguration)),
    };

    if (isFeatureEnabled(names.intentGuideUpdate, userFeatureConfiguration)) {
      colGranularIntent.Cell = this.getEditorMultiline;
      colRollupIntent.Cell = this.getEditorMultiline;
      colDescription.Cell = this.getEditorMultiline;
      colKeywords.Cell = this.getEditorMultiline;
      colExamples.Cell = this.getEditorMultiline;
      colComments.Cell = this.getEditorMultiline;
    }

    const columns = [
      colCount,
      colGranularIntent,
      colRollupIntent,
      colDescription,
      colKeywords,
      colExamples,
      colComments,
      colID,
      colDelete,
    ];

    return columns;
  }

  render() {
    const { userFeatureConfiguration } = this.props;
    const { names } = featureFlagDefinitions;
    // This check is added to prevent the table from unnecesary rendering before ComponentDidMount.
    if (this.isModified === false) return <Placeholder message={Constants.SEARCHING_IN_PROGRESS} />;

    const { intents } = this.props;
    let tableStyle = {
      maxHeight: 'calc(95vh - 225px)',
      borderLeft: 'unset',
    };
    if (!isFeatureEnabled(names.intentGuideContextualActionBar, userFeatureConfiguration)) {
      tableStyle.borderTop = 'unset';
    }

    return (
      <Table
        id="TaggingGuideGrid"
        style={tableStyle}
        resizable
        data={intents}
        columns={this.columns}
        showPaginaton
        defaultPageSize={10}
        onSortedChange={() => {
          this.setState({
            statusDataCell: {},
          });
        }}
        onResizedChange={(newResized) => {
          tableUtils.handleTableResizeChange(newResized, this.localStorage_key);
        }}
        defaultSorted={[
          {
            id: TAGGING_GUIDE_TABLE.count.id,
            desc: true,
          },
        ]}
      />
    );
  }
}

TaggingGuideGrid.propTypes = {
  clientId: PropTypes.string.isRequired,
  projectId: PropTypes.string.isRequired,
  csrfToken: PropTypes.string.isRequired,
  userId: PropTypes.string,
  validateData: PropTypes.func,
  dispatch: PropTypes.func,
};

TaggingGuideGrid.defaultProps = {
  intents: [],
  validateData: () => true,
  dispatch: () => {},
};

const mapStateToProps = (state) => {
  const props = {
    csrfToken: state.app.csrfToken,
    userId: state.app.userId,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
  };
  return props;
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(TaggingGuideGrid);
