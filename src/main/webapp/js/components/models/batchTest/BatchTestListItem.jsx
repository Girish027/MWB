import React, { Component } from 'react';
import PropTypes from 'prop-types';
import getIcon, { IconNames } from 'utils/iconHelpers';
import ReactSVG from 'react-svg';

import {
  LegacyGrid, LegacyRow, LegacyColumn, DashedSpinner,
} from '@tfs/ui-components';
import { getDateFromTimestamp } from 'utils/DateUtils';

class BatchTestListItem extends Component {
  constructor() {
    super();

    this.state = {
      focus: false,
      showMisclassified: false,
      showDeleteHover: false,
    };

    this.toggleFocus = this.toggleFocus.bind(this);
    this.onMouseUp = this.onMouseUp.bind(this);
    this.getStatusIcon = this.getStatusIcon.bind(this);
    this.getDeleteIcon = this.getDeleteIcon.bind(this);
    this.onClickBatchTestItem = this.onClickBatchTestItem.bind(this);
    this.spinnerProps = { height: '20px', width: '20px' };
  }

  static getDerivedStateFromProps(props, state) {
    if (props.batchtest.misclassified) {
      return ({
        showMisclassified: true,
      });
    }
    return null;
  }

  getStatusIcon(status) {
    if (status === 'IN_PROGRESS') {
      return (
        <DashedSpinner {...this.spinnerProps} />
      );
    }
    const iconMap = {
      SUCCESS: IconNames.COMPLETED_BATCH_TEST,
      FAILED: IconNames.FAILED_BATCH_TEST,
      QUEUED: IconNames.QUEUED_BATCH_TEST,
    };
    return (
      <ReactSVG src={getIcon(iconMap[status])} />
    );
  }

  getDeleteIcon() {
    // TODO Implement Delete API before enabling the button
    return (
      <ReactSVG src={getIcon(IconNames.DELETE_BATCH_TEST)} />
    );
  }

  toggleFocus(event) {
    this.setState({
      focus: !this.state.focus,
    }, () => this.props.onFocusChange(this.props.index, this.state.focus));
  }

  onMouseUp(event) {
    this.setState({
      focus: false,
    }, () => this.props.onFocusChange(this.props.index, this.state.focus));
  }

  onClickBatchTestItem(event) {
    const { testId } = this.props.batchtest;
    this.props.onClickBatchTestItem(this.props.index, testId);
  }

  render() {
    return (
      <div
        key={`batchtest-${this.props.index}`}
        className="batchtest-list-item"
        onClick={this.onClickBatchTestItem}
        onFocus={this.toggleFocus}
        onBlur={this.toggleFocus}
        onMouseOver={this.toggleFocus}
        onMouseOut={this.toggleFocus}
        onMouseUp={this.onMouseUp}
        style={{
          backgroundColor: this.props.focused ? '#eee' : (this.props.selected ? '#f3f4f5' : ''),
        }}
      >
        <LegacyGrid>
          <LegacyRow>
            <LegacyColumn size={12}>
              <LegacyRow>
                <LegacyColumn size={12}>
                  <div
                    style={{
                      display: 'inline-block',
                    }}
                  >
                    <div className="batchtest-name">
                      {this.props.batchtest.batchTestName}
                    </div>
                    <span
                      style={{
                        float: 'left',
                        height: '16px',
                        width: '16px',
                      }}
                    >
                      {this.getStatusIcon(this.props.batchtest.status)}
                    </span>
                    <div className="float-clear" />
                  </div>
                </LegacyColumn>
              </LegacyRow>
              <LegacyRow>
                <div className="batchtest-meta">
                  Datasets:
                  {' '}
                  {this.props.batchtest.requestPayload.split(',').length}
                </div>
                {this.state.showMisclassified
                  ? (
                    <div className="batchtest-meta">
                    Misclassified:
                      {' '}
                      {this.props.batchtest.misclassified}
                    </div>
                  )
                  : null
                }
                <div className="batchtest-meta">
                  Date:
                  {' '}
                  {getDateFromTimestamp(this.props.batchtest.createdAt)}
                </div>
              </LegacyRow>
            </LegacyColumn>
            {/* <LegacyColumn size={2}>
              <div
                data-qa={`batchTest-delete-${this.props.index}`}
                style={{
                  visibility: 'hidden',
                }}
              >
                <span
                  style={{
                    padding: '42px 30px 42px 30px',
                    height: '16px',
                    width: '16px',
                    display: 'block',
                    marginLeft: 'auto',
                    marginRight: 'auto',
                  }}
                >
                  {this.getDeleteIcon()}
                </span>
              </div>
            </LegacyColumn> */}
          </LegacyRow>
        </LegacyGrid>
      </div>
    );
  }
}

BatchTestListItem.propTypes = {
  index: PropTypes.number,
  batchtest: PropTypes.shape({
    testId: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
    requestPayload: PropTypes.string.isRequired,
    createdAt: PropTypes.string.isRequired,
    batchTestName: PropTypes.string.isRequired,
    misclassified: PropTypes.number,
  }),
  onClickBatchTestItem: PropTypes.func.isRequired,
  focused: PropTypes.bool,
  selected: PropTypes.bool,
  onFocusChange: PropTypes.func,
};

BatchTestListItem.defaultProps = {
  focused: false,
  selected: false,
};

export default BatchTestListItem;
