import React, { Component } from 'react';
import PropTypes from 'prop-types';
import EditableLabel from 'components/common/EditableLabel';
import { Button, LegacyGrid, LegacyRow } from '@tfs/ui-components';

import DatasetSelectorTable from 'components/models/batchTest/DatasetSelectorTable';

import { modelBatchTest } from 'state/actions/actions_models';

class CreateBatchTest extends Component {
  constructor(props) {
    super(props);

    this.defaultBatchTestName = 'Create new batch test';

    this.state = {
      checked: false,
      selectedDatasets: [],
      batchTestName: this.defaultBatchTestName,
    };

    this.onRunBatchTest = this.onRunBatchTest.bind(this);
    this.updateSelectedDatasets = this.updateSelectedDatasets.bind(this);
    this.getRunTestButton = this.getRunTestButton.bind(this);
    this.validateAndUpdateValue = this.validateAndUpdateValue.bind(this);
  }

  updateSelectedDatasets(selectedDatasets) {
    this.setState({
      selectedDatasets,
      checked: selectedDatasets.length > 0,
    });
  }

  onRunBatchTest() {
    const { dispatch, showBatchTestResults, testModelType } = this.props;
    const data = {
      userId: this.props.app.userId,
      csrfToken: this.props.app.csrfToken,
      projectId: this.props.projectId,
      modelId: this.props.model.modelToken,
      clientId: this.props.client.id,
      datasets: this.state.selectedDatasets,
      testModelType,
      batchTestName: this.state.batchTestName,
    };

    dispatch(modelBatchTest(data));
    showBatchTestResults();
  }

  getRunTestButton() {
    return (
      <Button
        onClick={this.onRunBatchTest}
        name="run-batch-test-button"
        type="primary"
        tabIndex={0}
        disabled={!this.state.checked || this.state.validatedName}
        styleOverride={{
          padding: '0px 25px',
        }}
      >
        RUN TEST
      </Button>
    );
  }

  validateAndUpdateValue(batchTestName) {
    // TODO - validation logic-
    this.setState({
      validatedName: true,
    }, () => {
      if (this.state.validatedName) {
        this.setState({
          batchTestName,
        });
      }
    });
  }

  render() {
    // TODO - remove display:none on Editable Label on implementing Naming of BT
    return (
      <LegacyGrid>
        <LegacyRow>
          <div className="batchtest-name-editor">
            {this.defaultBatchTestName}
          </div>
        </LegacyRow>
        <LegacyRow styleOverride={{
          display: 'none',
        }}
        >
          <EditableLabel
            customClassName="batchtest-name-editor"
            data-qa="batchtest-name-editor"
            validateAndUpdateValue={this.validateAndUpdateValue}
            defaultValue={this.defaultBatchTestName}
          />
        </LegacyRow>
        <LegacyRow>
          <div className="batchtest-info">
            Select one or more data sets from the project list to run a batch test.
          </div>
        </LegacyRow>
        <LegacyRow>
          <DatasetSelectorTable
            history={this.props.history}
            client={this.props.client}
            projectId={this.props.projectId}
            datasets={this.props.datasets}
            updateSelectedDatasets={this.updateSelectedDatasets}
          />
        </LegacyRow>
        <LegacyRow>
          <div className="float-right runtest-button">
            {this.getRunTestButton()}
          </div>
          <div className="float-clear" />
        </LegacyRow>
      </LegacyGrid>
    );
  }
}

CreateBatchTest.propTypes = {
  history: PropTypes.object.isRequired,
  client: PropTypes.object.isRequired,
  projectId: PropTypes.string.isRequired,
  datasets: PropTypes.object,
  showBatchTestResults: PropTypes.func,
  dispatch: PropTypes.func,
  testModelType: PropTypes.string,
};

CreateBatchTest.defaultProps = {
  datasets: {},
  showBatchTestResults: () => {},
};

export default CreateBatchTest;
