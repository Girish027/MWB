import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import fileDownload from 'js-file-download';

import {
  modelCheckBatchTest,
  clearModelBatchTestResults,
  clearModelTestResults,
} from 'state/actions/actions_models';

import {
  batchResultsDataToJson,
  getBatchResultsData,
} from 'components/models/modelUtils';

import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import CreateBatchTest from 'components/models/batchTest/CreateBatchTest';
import BatchTestSidebar from 'components/models/batchTest/BatchTestSidebar';
import ModelTestBatchResultsGrid from 'components/models/ModelTestBatchResultsGrid';

import {
  LegacyGrid,
  LegacyRow,
  Button,
  PrimaryDivider,
  DropDown,
} from '@tfs/ui-components';
import Placeholder from 'components/controls/Placeholder';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import Model from 'model';
import Constants from 'constants/Constants';


class BatchTestContainer extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isSidebarOpen: true,
      batchTestInfo: undefined,
      lastBatchTestResults: undefined,
      selectedIndex: 0,
      selectedTestId: '',
      createBatchTestMode: false,
      inputDatasets: [],
    };
    this.sideBarClassName = 'main-sidebar-container sidebar-open';
    this.getLoadingBatchTestScreen = this.getLoadingBatchTestScreen.bind(this);
    this.onClickBatchTestItem = this.onClickBatchTestItem.bind(this);
    this.onRunBatchTestClick = this.onRunBatchTestClick.bind(this);
    this.onCancelBatchTest = this.onCancelBatchTest.bind(this);
    this.onCheckBatchTest = this.onCheckBatchTest.bind(this);
    this.onDownloadResults = this.onDownloadResults.bind(this);
    this.showBatchTestResults = this.showBatchTestResults.bind(this);
    this.getBatchTestResultsContent = this.getBatchTestResultsContent.bind(this);
    this.toggleSidebar = this.toggleSidebar.bind(this);
  }

  static getDerivedStateFromProps(props, state) {
    let { batchTestInfo } = props.listOfBatchTests;
    let { selectedTestId, selectedIndex, lastBatchTestResults } = state;
    let { modelBatchTestResults } = props;

    if (selectedTestId === '' && modelBatchTestResults && modelBatchTestResults.modelTestJobId) {
      selectedTestId = modelBatchTestResults.modelTestJobId;
    }
    const statusOfSelectedTest = batchTestInfo && batchTestInfo[selectedIndex] ? batchTestInfo[selectedIndex].status : '';
    if (statusOfSelectedTest !== 'SUCCESS') {
      lastBatchTestResults = undefined;
    } else if (!_.isNil(modelBatchTestResults)) {
      if (modelBatchTestResults.modelTestJobId === selectedTestId) {
        lastBatchTestResults = props.modelBatchTestResults;
      }
    }
    return ({
      userId: props.app.userId,
      csrfToken: props.app.csrfToken,
      batchTestInfo,
      lastBatchTestResults,
      selectedTestId,
    });
  }

  componentDidMount() {
    this.props.dispatch(clearModelBatchTestResults());
  }

  showBatchTestResults() {
    this.setState({
      createBatchTestMode: false,
    });
  }

  onClickDatasetName = (datasetName) => {
    const {
      dispatch, history, projectId, clientId,
    } = this.props;
    const { inputDatasets } = this.state;
    const dataset = inputDatasets.find(({ name } = {}) => datasetName === name) || {};
    const { id: datasetId } = dataset;
    dispatch(actionsTagDatasets.setIncomingFilter({
      projectId,
      datasets: [datasetId],
    }));
    dispatch(changeRoute(RouteNames.TAG_DATASETS, {
      clientId,
      projectId,
    }, history));
  }

  getRequestPayload = () => {
    const { inputDatasets } = this.state;
    const datasetList = [];
    const dropdownLabels = [];
    inputDatasets.forEach(({ id: datasetId, name: datasetName } = {}, index) => {
      if (index < 3) {
        datasetList.push(<PrimaryDivider
          style={{ padding: '0px 5px' }}
          fill="#9E9E9E"
          key={`divider-${index}`}
        />);
        // check whether the dataset from payload has not been deleted
        if (datasetId) {
          datasetList.push(<Button
            name={`dataset-${datasetId}`}
            key={`button-${index}`}
            type="flat"
            onClick={() => this.onClickDatasetName(datasetName)}
            styleOverride={{
              color: '#313f54',
            }}
          >
            {datasetName}
          </Button>);
        } else {
          datasetList.push(<span
            style={{
              color: '#ddd',
              cursor: 'not-allowed',
            }}
            key={`button-${index}`}
          >
                      deleted
          </span>);
        }
      } else if (datasetName) {
        dropdownLabels.push(datasetName);
      }
    });
    // remove the first '|'
    datasetList.shift();
    return (
      <div className="dataset-list-container">
        <span className="float-left" style={{ color: '#9E9E9E', padding: '10px 0px' }}> Datasets: </span>
        <span className="float-left dataset-list">
          {datasetList}
        </span>
        <span className="float-left" style={{ padding: '10px 0px' }}>
          { dropdownLabels.length > 0 && (
            <DropDown
              key="more"
              itemList={dropdownLabels}
              labelName={`MORE (${dropdownLabels.length})`}
              onItemSelected={this.onClickDatasetName}
            />
          )}
        </span>
      </div>
    );
  }

  getInputDatasets = (testId) => {
    // get all the datasets used for the batch test
    const { listOfBatchTests = {}, projectId } = this.props;
    const { batchTestInfo = [] } = listOfBatchTests;
    const selectedBatchTest = batchTestInfo.find((batchtest) => batchtest.testId === testId) || {};
    const { requestPayload = '' } = selectedBatchTest;
    const selectedDatasets = requestPayload.split(Constants.COMMA_SEPARATOR).map((datasetId) => Model.ProjectsManager.getDataset(projectId, datasetId));
    return selectedDatasets;
  }

  onClickBatchTestItem(selectedIndex, testId) {
    this.setState({
      createBatchTestMode: false,
      selectedIndex,
      selectedTestId: testId,
      inputDatasets: this.getInputDatasets(testId),
    });
    this.props.dispatch(clearModelBatchTestResults());
    this.onCheckBatchTest(testId);
  }

  toggleSidebar(event) {
    this.sideBarClassName = 'main-sidebar-container';
    // toggles the classname
    if (!this.state.isSidebarOpen) {
      this.sideBarClassName += ' sidebar-open';
    } else {
      this.sideBarClassName += ' sidebar-closed';
    }

    this.setState({
      isSidebarOpen: !this.state.isSidebarOpen,
    });
  }

  onRunBatchTestClick() {
    this.setState({
      createBatchTestMode: true,
      selectedIndex: 0,
      selectedTestId: '',
    }, () => {
      this.props.dispatch(clearModelTestResults());
      this.props.dispatch(clearModelBatchTestResults());
    });
  }

  onCancelBatchTest() {
    // TODO - we need deletion for cancelling batch test as per design (page 10 of design spec)
  }

  onCheckBatchTest(jobId) {
    // retrieves the batch test results for the JobId
    const { dispatch } = this.props;
    const data = {
      userId: this.state.userId,
      csrfToken: this.state.csrfToken,
      projectId: this.props.projectId,
      modelId: this.props.model.modelToken,
      clientId: this.props.clientId,
      modelTestJobId: jobId,
    };
    dispatch(modelCheckBatchTest(data));
  }

  onDownloadResults() {
    const { model } = this.props;
    const { lastBatchTestResults } = this.state;

    const date = new Date();

    const headerData = {
      date,
      model,
      jobId: lastBatchTestResults.modelTestJobId,
    };

    const data = getBatchResultsData(lastBatchTestResults.modelBatchTestResults, headerData);
    const fileName = `${model.name}_v${model.version}.csv`;

    fileDownload(data, fileName);
  }

  getLoadingBatchTestScreen() {
    let loadingMessage = 'Create new batch test';
    let recentBatchTestStatus;
    const { listOfBatchTests = {} } = this.props;
    const { batchTestInfo = [] } = listOfBatchTests;
    if (batchTestInfo.length > 0) {
      const { status } = listOfBatchTests.batchTestInfo[this.state.selectedIndex];
      recentBatchTestStatus = status;
    }

    switch (recentBatchTestStatus) {
    case 'SUCCESS':
      loadingMessage = Constants.RETRIEVE_RESULTS_MESSAGE;
      break;
    case 'QUEUED':
    case 'IN_PROGRESS':
      loadingMessage = Constants.PREPARE_RESULTS_MESSAGE;
      break;
    case 'FAILED':
      loadingMessage = Constants.BATCH_TEST_FAILED_MESSAGE;
      break;
    }

    return (<Placeholder message={loadingMessage} />);
  }

  getBatchResultsGrid() {
    const { lastBatchTestResults } = this.state;
    const { names } = featureFlagDefinitions;
    const { userFeatureConfiguration } = this.props;

    if (!_.isNil(lastBatchTestResults)) {
      const parsedBatchResults = batchResultsDataToJson(lastBatchTestResults.modelBatchTestResults);
      if (parsedBatchResults && parsedBatchResults.data.length > 0) {
        return (
          <LegacyRow data-qa="batchtests-results-grid">
            <LegacyRow>
              <div style={{
                margin: '0px 30px',
                display: 'block',
                fontWeight: 'bold',
              }}
              >
                <div
                  className="float-left"
                  style={{
                    color: '#313f54',
                    padding: '10px 0px',
                  }}
                >
                  Results
                </div>
                { (isFeatureEnabled(names.downloadResults, userFeatureConfiguration)) ? (
                  <div className="float-right">
                    <Button
                      type="flat"
                      onClick={this.onDownloadResults}
                    >
                      DOWNLOAD
                    </Button>
                  </div>
                ) : null }
              </div>
            </LegacyRow>
            <LegacyRow
              styleOverride={{
                backgroundColor: '#f6f7f8',
                borderTop: '1px solid #e6e6e6',
                borderBottom: '1px solid #e6e6e6',
              }}
            >
              {this.getRequestPayload()}
            </LegacyRow>
            <LegacyRow>
              <div className="float-clear">
                <ModelTestBatchResultsGrid
                  data={parsedBatchResults.data}
                  onRunSingleUtterance={this.props.onRunSingleUtterance}
                />
              </div>
            </LegacyRow>
          </LegacyRow>
        );
      }
    }
    return this.getLoadingBatchTestScreen();
  }

  getBatchTestResultsContent() {
    if (this.state.createBatchTestMode) {
      return (
        <CreateBatchTest
          {...this.props}
          testModelType={this.props.testModelType}
          dispatch={this.props.dispatch}
          showBatchTestResults={this.showBatchTestResults}
        />
      );
    }
    return (
      <LegacyGrid>
        {this.getBatchResultsGrid()}
      </LegacyGrid>
    );
  }

  render() {
    return (
      <div id="batchtest-container">
        <div className={this.sideBarClassName}>
          <BatchTestSidebar
            {...this.props}
            isOpen={this.state.isSidebarOpen}
            toggleSidebar={this.toggleSidebar}
            onClickBatchTestItem={this.onClickBatchTestItem}
            onRunBatchTestClick={this.onRunBatchTestClick}
          />
        </div>
        <div className="main-separator" />
        <div id="main-content-container">
          {this.getBatchTestResultsContent()}
        </div>
      </div>
    );
  }
}

BatchTestContainer.propTypes = {
  testModelType: PropTypes.string,
  onTuneModelClick: PropTypes.func,
  onReviewDatasetsClick: PropTypes.func,
  onRunSingleUtterance: PropTypes.func,
  dispatch: PropTypes.func,
  history: PropTypes.object,
  projectId: PropTypes.string,
  clientId: PropTypes.string,
  listOfBatchTests: PropTypes.object,
  model: PropTypes.object,
};

BatchTestContainer.defaultProps = {

};

export default BatchTestContainer;
