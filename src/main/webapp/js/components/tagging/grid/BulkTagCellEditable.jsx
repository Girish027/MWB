import React, { Component } from 'react';
import { connect } from 'react-redux';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsTag from 'state/actions/actions_datasets_transformed_tag';
import validationUtil from 'utils/ValidationUtil';
import {
  CELL_ACTIVITY_FOCUSSED,
  CELL_ACTIVITY_TYPING_INTENT,
  CELL_ACTIVITY_BACKSPACING_INTENT,
  CELL_ACTIVITY_SUGGESTED_TAG_SELECTED,
  CELL_ACTIVITY_SUGGESTED_TAG_UPDATED,
  CELL_ACTIVITY_NONE,
  CELL_TYPE_MANUAL_BULK,
  CELL_INTENT_STATUS_ADDED,
  CELL_INTENT_STATUS_REMOVED,
  CELL_INTENT_STATUS_NONE,
} from 'state/actions/types';

import { Button } from '@tfs/ui-components';
import CellSuggest from './CellSuggest';

class BulkTagCellEditable extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    // handlers
    this.handleChange = this.handleChange.bind(this);
    this.handleBlur = this.handleBlur.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleClick = this.handleClick.bind(this);
    this.handleCancelClick = this.handleCancelClick.bind(this);

    this.inputClassName = 'bulklTxtInput';
    this.inputRef = null;
    this.cellRef = null;

    // state
    const intent = props.cellData.row.manualTag;

    this.state = {
      intent,
      activity: CELL_ACTIVITY_NONE,
      type: CELL_TYPE_MANUAL_BULK,
      firstFocus: true,
      intentStatus: CELL_INTENT_STATUS_NONE,
    };
  }

  componentWillReceiveProps(nextProps/* , nextState */) {
    this.props = nextProps;
    let newState = {};
    if (this.state.type === CELL_TYPE_MANUAL_BULK) {
      if (this.state.activity === CELL_ACTIVITY_NONE) {
        newState.intent = this.props.cellData.row.manualTag;
      }
    }

    const isBulkTagMode = this.props.tagDatasets.bulkTagMode;

    // handle suggested tags
    if (this.state.activity !== CELL_ACTIVITY_NONE
              // && this.state.activity !== CELL_ACTIVITY_BACKSPACING_INTENT
              && (this.props.tagDatasets.selectedSuggestTag !== '' || this.props.tagDatasets.updatedSuggestTag !== '')
              && isBulkTagMode
              && this.state.type === CELL_TYPE_MANUAL_BULK
    ) {
      const isSuggestedTagSelected = !!((this.props.tagDatasets.selectedSuggestTag !== ''
              && this.props.tagDatasets.updatedSuggestTag === ''));

      // a suggested tag has been selected
      if (isSuggestedTagSelected && isBulkTagMode) {
        newState.activity = CELL_ACTIVITY_SUGGESTED_TAG_SELECTED;
        newState.intent = this.props.tagDatasets.selectedSuggestTag;
      }

      // a suggested tag has been updated but not selected yet
      if (!isSuggestedTagSelected && isBulkTagMode) {
        newState.activity = CELL_ACTIVITY_SUGGESTED_TAG_UPDATED;
        newState.intent = this.props.tagDatasets.updatedSuggestTag;
      }
    }
    this.setState(newState);
  }

  componentDidMount() {
    if (this.state.firstFocus) {
      // ensure this cell gets focus last after any row cell
      setTimeout(() => {
        this.inputRef.focus();
        this.setState({
          firstFocus: false,
        });
        this.props.updateActiveBulkCell(this.cellRef);
      }, 500);
    }
  }

  componentDidUpdate() {
    // don"t send the tag if it"s bulk but reset it"s state
    if (this.state.type === CELL_TYPE_MANUAL_BULK
      && this.state.activity === CELL_ACTIVITY_SUGGESTED_TAG_SELECTED) {
      setTimeout(() => {
        this.setState({ activity: CELL_ACTIVITY_NONE });
      }, 500);
    }
  }

  handleClick() {
    this.props.updateActiveBulkCell(this.cellRef);
    this.setState({
      activity: CELL_ACTIVITY_FOCUSSED,
    });
  }

  handleCancelClick(event) {
    event.preventDefault();
    this.cancelBulkTag();
  }

  handleBlur(event) {
    event.preventDefault();
    // don"t believe bulk cell should do anything on blur only on enter
  }

  /*
     Mental Note:
     Only set the state for intent onChange - so one does not need to set it again during keyDown or keyPress
     */

  handleChange(event) {
    const intent = event.target.value;
    this.setState({
      intent,
    });
    this.getSuggestedTagsAndUpdate(intent);
  }

  getSuggestedTagsAndUpdate(intent = '') {
    if (intent.length >= 1) {
      this.props.fetchSuggestTag({ projectId: this.props.projectId || this.props.tagDatasets.projectId, intent });
      this.props.updateUserEnteredBulkTag(intent);
    }
  }

  handleKeyDown(event) {
    let activity = '';
    if (event.key === 'Enter') {
      const invalid = validationUtil.validateTopicGoal(this.state.intent);
      if (invalid) {
        this.props.propagateInvalidIntentMessage(this.state.intent);
        return false;
      }
      this.props.selectedBulkTag(this.state.intent);
    } else if (event.keyCode === 8) {
      activity = CELL_ACTIVITY_BACKSPACING_INTENT;

      // if there is selected tag, clear it
      if (this.props.tagDatasets.selectedSuggestBulkTag !== ''
        || this.props.tagDatasets.updatedSuggestBulkTag !== '') {
        this.props.forgetSelectedSuggestedBulkTag();
      }

      this.getSuggestedTagsAndUpdate();

      if (this.state.intent === '') {
        this.props.updateDeletedBulkTagIntent();
      }
    } else if (event.keyCode === 27) { // Esc Key
      this.cancelBulkTag();
    } else if (event.keyCode === 40) {
      this.props.fireCursorDown();
    } else if (event.keyCode === 38) {
      this.props.fireCursorUp();
    } else {
      activity = CELL_ACTIVITY_TYPING_INTENT;
    }

    if (activity) {
      this.setState({
        activity,
      });
    }
  }

  cancelBulkTag() {
    // if there is selected tag, clear it
    if (this.props.tagDatasets.selectedSuggestBulkTag !== ''
      || this.props.tagDatasets.updatedSuggestBulkTag !== '') {
      this.props.forgetSelectedSuggestedBulkTag();
    }
    // remove suggested list
    this.props.removeTagSuggest();
    this.props.hideBulkTagCell();
  }

  sendManualTag() {
    if (this.state.activity === CELL_ACTIVITY_TYPING_INTENT || this.state.activity === CELL_ACTIVITY_BACKSPACING_INTENT) {
      this.props.updateUserEnteredTag(this.state.intent);
    }

    // the action will further validate the topic-goal format
    switch (this.state.activity) {
    case CELL_ACTIVITY_SUGGESTED_TAG_SELECTED:
    case CELL_ACTIVITY_SUGGESTED_TAG_UPDATED:
    case CELL_ACTIVITY_FOCUSSED:
    case CELL_ACTIVITY_TYPING_INTENT:
      return (this.state.intent !== '') ? this.saveManualTag(this.state.intent) : null;
    case CELL_ACTIVITY_BACKSPACING_INTENT:
      return (this.state.intent === '') ? this.saveManualUntag() : null;
    default:
      return null;
    }
  }

  saveManualTag(intent) {
    const invalid = validationUtil.validateTopicGoal(intent);
    if (invalid) {
      this.props.propagateInvalidIntentMessage(intent);
      return false;
    }

    if (this.state.intentStatus === CELL_INTENT_STATUS_ADDED) {
      return false;
    }
  }

  render() {
    switch (this.state.intentStatus) {
    case CELL_INTENT_STATUS_ADDED:
      this.inputClassName = `${this.inputClassName} manualTagStatusAdded`;
      break;
    case CELL_INTENT_STATUS_REMOVED:
      this.inputClassName = `${this.inputClassName} manualTagStatusRemoved`;
      break;
    case CELL_INTENT_STATUS_NONE:
    default:
      this.inputClassName = `${this.inputClassName} manualTagStatusNone`;
      break;
    }

    if (this.state.activity === CELL_ACTIVITY_SUGGESTED_TAG_SELECTED) {
      this.inputClassName = `${this.inputClassName} manualTagSuggestTagAdded`;
    }

    if (this.props.tagDatasets.bulkTagReceived) {
      `${this.inputClassName} manualBulkTagStatus`;
    }
    /*
         Note: controlled input of type text to be uncontrolled warning is removed by this fix:
         <input className={this.inputClassName} />
         vs
         <input className={this.inputClassName || ""} />
         */

    const getCellSuggest = () => {
      const { suggestTagResult = {}, userEnteredBulkTag = '', bulkTagMode } = this.props.tagDatasets;
      if (suggestTagResult.status !== 5
                && suggestTagResult.status !== 4
                && userEnteredBulkTag.length > 0
                && bulkTagMode) {
        return (
          <div className="bulkTagSuggestContainer">
            <CellSuggest suggestedIntents={this.props.suggestedIntents} />
          </div>
        );
      }
      return null;
    };

    return (
      <div className="bulkTagLabelCellContainer">
        <span className="manualTagCell" ref={(ref) => { this.cellRef = ref; }}>
          <input
            type="text"
            ref={(ref) => { this.inputRef = ref; }}
            className={this.inputClassName || ''}
            onChange={this.handleChange}
            onKeyDown={this.handleKeyDown}
            onBlur={this.handleBlur}
            onClick={this.handleClick}
            value={this.state.intent || ''}
          />
          {getCellSuggest()}
        </span>
        <Button
          name="cancel-cell"
          onClick={this.handleCancelClick}
          styleOverride={{
            height: '35px',
          }}
        >
                    CANCEL
        </Button>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  app: state.app,
  tagDatasets: state.tagDatasets,
  header: state.header,
});

const mapDispatchToProps = dispatch => ({
  fireCursorUp: () => {
    dispatch(actionsTag.fireCursorUp());
  },
  fireCursorDown: () => {
    dispatch(actionsTag.fireCursorDown());
  },
  updateDeletedBulkTagIntent: () => {
    dispatch(actionsTag.updateDeletedBulkTagIntent());
  },
  selectedBulkTag: (bulkTagIntent) => {
    dispatch(actionsTag.selectedBulkTag(bulkTagIntent));
  },
  updateUserEnteredBulkTag: (userEnteredBulkTag) => {
    dispatch(actionsTag.updateUserEnteredBulkTag(userEnteredBulkTag));
  },
  updateActiveBulkCell: (activeCell) => {
    dispatch(actionsTag.updateActiveBulkCell(activeCell));
  },
  fetchSuggestTag: ({ projectId, intent }) => {
    dispatch(actionsTag.fetchSuggestTag({ projectId, intent }));
  },
  removeTagSuggest: () => {
    dispatch(actionsTag.removeTagSuggest());
  },
  updateIntent: (intent) => {
    dispatch(actionsTag.updateIntent(intent));
  },
  propagateInvalidIntentMessage: (intent) => {
    dispatch(actionsTag.propagateInvalidIntentMessage(intent));
  },
  forgetSelectedSuggestedBulkTag: (intent) => {
    dispatch(actionsTag.forgetSelectedSuggestedBulkTag(intent));
  },
  hideBulkTagCell: () => {
    dispatch(actionsTag.hideBulkTagCell());
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(BulkTagCellEditable);
