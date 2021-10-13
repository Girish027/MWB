import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as ramda from 'ramda';
import URLSearchParams from '@ungap/url-search-params';

import { displayWarningRequestMessage } from 'state/actions/actions_app';
import {
  clearModelTestResults,
  testModel,
} from 'state/actions/actions_models';

import Model from 'model';
import Constants from 'constants/Constants';

import {
  LegacyGrid,
} from '@tfs/ui-components';
import ConnectedUtteranceInputBar from './UtteranceInputBar';
import UtteranceTestResults from './UtteranceTestResults';

export class UtteranceTestContainer extends Component {
  constructor(props) {
    super(props);
    this.submitTest = this.submitTest.bind(this);
    this.onRunSingleUtterance = this.onRunSingleUtterance.bind(this);
    this.onModelTypeChange = this.onModelTypeChange.bind(this);
    this.handleAudio = this.handleAudio.bind(this);

    const { model } = this.props;

    this.state = {
      utterance: '',
      testModelType: model ? model.modelType : '',
      fileType: '',
      audioSrc: '',
    };
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    const { runSingleUtterance, model } = this.props;
    if (runSingleUtterance.length) {
      this.onRunSingleUtterance(runSingleUtterance);
    }
    if (!prevState.testModelType && model) {
      this.onModelTypeChange(model.modelType);
    }
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(clearModelTestResults());
  }

  onRunSingleUtterance(transcription) {
    const { clearRunSingleUtterance } = this.props;
    this.setState({
      utterance: transcription,
      testModelType: Constants.DIGITAL_MODEL,
    }, () => {
      const textElement = document.querySelectorAll('[name="utterance"]');
      if (!_.isNil(textElement) && !_.isNil(textElement[0])) {
        textElement[0].value = transcription;
      }
      clearRunSingleUtterance();
      this.submitTest(transcription);
    });
  }

  submitTest(utterance) {
    const {
      dispatch, model, client, projectId,
    } = this.props;
    const { testModelType } = this.state;

    dispatch(clearModelTestResults());
    if (_.isNil(model)) {
      dispatch(displayWarningRequestMessage(Constants.UNKNOWN_MODEL));
      return;
    }
    const data = {
      projectId,
      modelId: model.modelToken,
      testModelType,
      clientId: client.id,
      utterances: [
        utterance,
      ],
    };
    dispatch(testModel(data));
  }

  onModelTypeChange(value) {
    const { testModelType } = this.state;
    const { dispatch } = this.props;
    if (value !== testModelType) {
      this.setState({
        testModelType: value,
      });
      dispatch(clearModelTestResults());
    }
  }

  handleAudio(fileType, audioSrc) {
    if (fileType && audioSrc) {
      this.setState({
        fileType,
        audioSrc,
      });
    }
  }

  render() {
    const { testModelType, fileType, audioSrc } = this.state;
    const {
      model, speechModelTestResults, digitalModelTestResults, utterance,
    } = this.props;
    return (
      <div id="ModelTest">
        <LegacyGrid>
          <ConnectedUtteranceInputBar
            model={model}
            utterance={utterance}
            testModelType={testModelType}
            onModelTypeChange={this.onModelTypeChange}
            submitTest={this.submitTest}
            speechModelTestResults={speechModelTestResults}
            handleAudio={this.handleAudio}
          />

          <UtteranceTestResults
            model={model}
            speechModelTestResults={speechModelTestResults}
            digitalModelTestResults={digitalModelTestResults}
            testModelType={testModelType}
            fileType={fileType}
            audioSrc={audioSrc}
          />
        </LegacyGrid>
      </div>
    );
  }
}

UtteranceTestContainer.propTypes = {
  userId: PropTypes.string,
  csrfToken: PropTypes.string,
  projectId: PropTypes.string,
  client: PropTypes.object,
  speechModelTestResults: PropTypes.object,
  digitalModelTestResults: PropTypes.object,
  runSingleUtterance: PropTypes.string,
  model: PropTypes.object,
  clearRunSingleUtterance: PropTypes.func,
  utterance: PropTypes.string,
};

UtteranceTestContainer.defaultProps = {
  speechModelTestResults: {},
  digitalModelTestResults: {},
  runSingleUtterance: '',
  utterance: '',
};

const mapStateToProps = (state, ownProps) => {
  // TODO: URI Utils.js
  const searchString = ramda.path(['location', 'search'], ownProps) || '';
  const query = new URLSearchParams(searchString);
  const modelId = query.get('modelid');
  const projectId = state.projectListSidebar.selectedProjectId;
  const { userId, csrfToken } = state.app;

  return {
    userId,
    csrfToken,
    projectId,
    client: state.header.client,
    model: Model.ProjectsManager.getModel(projectId, modelId) || null,
    speechModelTestResults: state.projectsManager.speechModelTestResults,
    digitalModelTestResults: state.projectsManager.digitalModelTestResults,
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(UtteranceTestContainer);
