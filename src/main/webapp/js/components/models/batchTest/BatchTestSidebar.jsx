import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  Plus,
  BackIcon,
  ContextualActionsBar,
  ContextualActionItem,
} from '@tfs/ui-components';
import AngleRight from 'components/Icons/AngleRight';
import IconButton from 'components/IconButton';
import Placeholder from 'components/controls/Placeholder';
import Constants from 'constants/Constants';
import BatchTestListItem from './BatchTestListItem';

class BatchTestSidebar extends Component {
  constructor(props, context) {
    super(props, context);

    this.selectedIndex = 0;
    this.state = {
      toggleDisplay: true,
      focusedIndex: -1,
      selectedIndex: 0,
      loadLatestBatchTestResult: false,
    };

    this.onClickCreateNewBatchTest = this.onClickCreateNewBatchTest.bind(this);
    this.onClickBatchTestItem = this.onClickBatchTestItem.bind(this);
    this.onFocusChange = this.onFocusChange.bind(this);
  }

  onClickBatchTestItem(selectedIndex, testId) {
    this.setState({
      selectedIndex,
    }, () => {
      this.props.onClickBatchTestItem(selectedIndex, testId);
    });
  }

  onClickCreateNewBatchTest(event) {
    this.props.onRunBatchTestClick(event);
  }

  onFocusChange(itemIndex, inFocus) {
    if (inFocus) {
      this.setState({
        focusedIndex: itemIndex,
      });
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    const { listOfBatchTests } = this.props;
    const { batchTestInfo } = listOfBatchTests;

    if (!this.state.loadLatestBatchTestResult && batchTestInfo && batchTestInfo.length > 0) {
      this.props.onClickBatchTestItem(0, batchTestInfo[0].testId);
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        loadLatestBatchTestResult: true,
      });
    }
  }

  renderList() {
    const { batchTestInfo } = this.props.listOfBatchTests;

    if (!batchTestInfo || !batchTestInfo.length) {
      /* empty list */
      return (
        <Placeholder message={Constants.NO_BATCH_TEST} />
      );
    }

    const batchTestItems = batchTestInfo.map((batchtest, index) => (
      <BatchTestListItem
        key={`batchtest-${index}`}
        index={index}
        batchtest={batchtest}
        focused={this.state.focusedIndex === index}
        onFocusChange={this.onFocusChange}
        onClickBatchTestItem={this.onClickBatchTestItem}
        selected={this.state.selectedIndex === index}
      />
    ));

    return (
      <div>
        {batchTestItems}
      </div>
    );
  }

  render() {
    const { isOpen, toggleSidebar } = this.props;
    return (
      <div
        id="batchTestSideBar"
        className="listContainer"
        style={{
          marginTop: '0px',
        }}
      >
        <span
          className="float-right"
          style={{
            marginTop: '30px',
          }}
        >
          {isOpen ? (
            <IconButton
              onClick={toggleSidebar}
              icon={BackIcon}
              data-qa="batchtest-hide-sidebar"
              title="Hide Sidebar"
            />
          ) : (
            <IconButton
              onClick={toggleSidebar}
              icon={AngleRight}
              data-qa="batchtest-show-sidebar"
              title="Show Sidebar"
            />
          )}
        </span>
        {isOpen ? (
          <div>
            <ContextualActionsBar>
              <ContextualActionItem
                onClickAction={this.onClickCreateNewBatchTest}
                icon={Plus}
              >
              CREATE NEW BATCH TEST
              </ContextualActionItem>
            </ContextualActionsBar>
            {this.renderList()}
          </div>
        )
          : null}
      </div>
    );
  }
}

BatchTestSidebar.propTypes = {
  isOpen: PropTypes.bool,
  toggleSidebar: PropTypes.func,
  onClickBatchTestItem: PropTypes.func,
  onRunBatchTestClick: PropTypes.func,
};

export default BatchTestSidebar;
