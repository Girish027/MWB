import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as ramda from 'ramda';
import URLSearchParams from '@ungap/url-search-params';
import { RouteNames, urlMap } from 'utils/routeHelpers';
import { changeRoute, displayWarningRequestMessage } from 'state/actions/actions_app';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import { getDataUrl, pathKey } from 'utils/apiUrls';
import {
  tuneSelectedModel,
  clearModelTestResults,
  listBatchTests,
  testModel,
  testSpeechModel,
} from 'state/actions/actions_models';
import * as headerActions from 'state/actions/actions_header';
import Constants from 'constants/Constants';
import Model from 'model';
import BatchTestContainer from 'components/models/batchTest/BatchTestContainer';
import UtteranceTestContainer from 'components/models/UtteranceTestContainer';
import {
  LegacyGrid,
  LegacyRow,
  Pencil,
  Tabs,
} from '@tfs/ui-components';


export class ModelTestTabs extends Component {
  constructor(props) {
    super(props);
    this.onReviewDatasetsClick = this.onReviewDatasetsClick.bind(this);
    this.onTuneModelClick = this.onTuneModelClick.bind(this);
    this.onClickBatchTestTab = this.onClickBatchTestTab.bind(this);
    this.onRunSingleUtterance = this.onRunSingleUtterance.bind(this);
    this.clearRunSingleUtterance = this.clearRunSingleUtterance.bind(this);
    this.getTabs = this.getTabs.bind(this);

    this.tabs = Constants.TEST_TABS;
    this.chunks = [];
    this.routes = {
      [this.tabs.utteranceTestTab]: RouteNames.TESTMODEL,
      [this.tabs.batchTestTab]: RouteNames.BATCHTESTMODEL,
    };

    this.actionItemStyle = {
      fontWeight: 'normal',
      ':focus': {
        outline: 'none',
      },
    };

    this.state = {
      runSingleUtterance: '',
    };
  }

  componentDidMount() {
    const {
      dispatch, model, projectId, client, utterance,
    } = this.props;

    const dataCommon = {
      projectId,
      modelId: model.modelToken,
      clientId: client.id,
    };
    const modelType = model ? model.modelType : '';
    const audioFile = getDataUrl(pathKey.dummySpeechTest);

    // this.chunks.push();
    // const recordedData = new Blob(this.chunks, { type: Constants.AUDIO_TYPE_WAV });
    // const reader = new FileReader();
    // reader.readAsBinaryString(recordedData);

    const digitalData = {
      ...dataCommon,
      testModelType: Constants.DIGITAL_MODEL,
      utterances: [
        utterance,
      ],
    };

    const speechData = {
      ...dataCommon,
      fileType: Constants.FILE_TYPE.UPLOADED,
      audioFile,
    };

    const actionItems = [{
      type: 'flat',
      label: Constants.TUNE_VERSION,
      icon: Pencil,
      onClick: this.onTuneModelClick,
      styleOverride: this.actionItemStyle,
    }];
    if (modelType === Constants.DIGITAL_MODEL) {
      dispatch(testModel(digitalData, false));
    } else if (modelType === Constants.DIGITAL_SPEECH_MODEL) {
      // const reader = new FileReader();
      // reader.onload = () => {
      //  dispatch(testSpeechModel(speechData, true));
      // };
      // reader.readAsBinaryString(audioFile);
    }
    dispatch(clearModelTestResults());
    dispatch(headerActions.setActionItems(actionItems));
  }

  componentWillUnmount() {
    const { dispatch } = this.props;
    dispatch(headerActions.setActionItems([]));
  }

  onTuneModelClick() {
    const {
      modelId, client, history, projectId, dispatch, model,
    } = this.props;
    dispatch(tuneSelectedModel(model));
    dispatch(changeRoute(RouteNames.TUNEMODEL, { client, projectId, modelId }, history));
  }

  onReviewDatasetsClick() {
    const {
      projectId, history, client, dispatch,
    } = this.props;
    dispatch(actionsTagDatasets.setIncomingFilter({ projectId, datasets: [] }));
    dispatch(changeRoute(RouteNames.TAG_DATASETS, { client, projectId }, history));
  }

  onClickBatchTestTab() {
    const {
      dispatch, model, client, projectId, userId, csrfToken,
    } = this.props;
    if (_.isNil(model) || _.isEmpty(model)) {
      dispatch(displayWarningRequestMessage(Constants.UNKNOWN_MODEL));
      return;
    }
    const data = {
      userId,
      csrfToken,
      projectId,
      modelId: model.modelToken,
      clientId: client.id,
    };
    dispatch(listBatchTests(data));
  }

  onRunSingleUtterance(transcription) {
    this.setState({
      runSingleUtterance: transcription,
    });
  }

  clearRunSingleUtterance() {
    this.setState({
      runSingleUtterance: '',
    });
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    const {
      client, projectId, modelId, history, dispatch,
    } = this.props;
    const { batchTestTab, utteranceTestTab } = this.tabs;
    let tab = '';
    switch (selectedTabIndex) {
    case 0:
      tab = utteranceTestTab;
      break;
    case 1:
      tab = batchTestTab;
      this.onClickBatchTestTab();
      break;
    default:
      tab = utteranceTestTab;
      break;
    }
    dispatch(changeRoute(this.routes[tab], {
      client, projectId, modelId,
    }, history));
  }

  getTabs() {
    const { runSingleUtterance = '' } = this.state;
    const { batchTestTab, utteranceTestTab } = this.tabs;
    const {
      dispatch,
      match,
      utterance,
      model,
    } = this.props;

    let selectedTabIndex = 0;
    if (match.path === urlMap.BATCHTESTMODEL) {
      selectedTabIndex = 1;
    }

    const tabPanels = [(
      <UtteranceTestContainer
        key={selectedTabIndex}
        {...this.props}
        utterance={utterance}
        runSingleUtterance={runSingleUtterance}
        clearRunSingleUtterance={this.clearRunSingleUtterance}
      />
    ), (
      <BatchTestContainer
        key={selectedTabIndex}
        {...this.props}
        dispatch={dispatch}
        onTuneModelClick={this.onTuneModelClick}
        onReviewDatasetsClick={this.onReviewDatasetsClick}
        onRunSingleUtterance={this.onRunSingleUtterance}
      />
    )];
    let tabsTitle = [utteranceTestTab, batchTestTab];
    if (model.modelType === Constants.SPEECH_MODEL) {
      tabsTitle = [utteranceTestTab];
    }
    return (
      <div id="ReadProject">
        <Tabs
          tabs={tabsTitle}
          tabPanels={tabPanels}
          onTabSelected={this.onTabSelected}
          align="left"
          selectedIndex={selectedTabIndex}
        />
      </div>
    );
  }


  render() {
    return (
      <div id="ModelTest">
        <LegacyGrid fluid>
          <LegacyRow>
            {this.getTabs()}
          </LegacyRow>
        </LegacyGrid>
      </div>
    );
  }
}

ModelTestTabs.propTypes = {
  userId: PropTypes.string,
  csrfToken: PropTypes.string,
  app: PropTypes.object,
  digitalModelTestResults: PropTypes.object,
  projectId: PropTypes.string,
  speechModelTestResults: PropTypes.object,
  modelId: PropTypes.string,
  client: PropTypes.object,
  models: PropTypes.object,
  project: PropTypes.object,
  model: PropTypes.object,
  datasets: PropTypes.object,
};

ModelTestTabs.defaultProps = {
  digitalModelTestResults: {},
  speechModelTestResults: {},
};

const mapStateToProps = (state, ownProps) => {
  // TODO: URI Utils.js
  const searchString = ramda.path(['location', 'search'], ownProps) || '';
  const query = new URLSearchParams(searchString);

  const projectId = state.projectListSidebar.selectedProjectId;
  const modelId = query.get('modelid');

  const { userId, csrfToken } = state.app;
  const { searchResults = [] } = state.taggingGuide;
  const utterance = searchResults.length > 0 ? searchResults[0].examples : 'make a reservation';
  // some of the props are used in the child components.
  // TODO: more optimization for the whole state.
  return {
    userId,
    csrfToken,
    app: state.app,
    digitalModelTestResults: state.projectsManager.digitalModelTestResults,
    speechModelTestResults: state.projectsManager.speechModelTestResults,
    projectId,
    modelId,
    utterance,
    client: state.header.client,
    models: Model.ProjectsManager.getModelsByProjectId(projectId) || null,
    project: Model.ProjectsManager.getProject(projectId) || null,
    model: Model.ProjectsManager.getModel(projectId, modelId) || {},
    datasets: Model.ProjectsManager.getDatasetsByProjectId(projectId, true) || null,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(ModelTestTabs);
