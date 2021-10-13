import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import {
  Play, Download, Stop, Tabs, LegacyRow,
} from '@tfs/ui-components';
import Constants from 'constants/Constants';
import Placeholder from 'components/controls/Placeholder';
import AudioPlayer from 'components/audio/AudioPlayer';
import Downloader from 'components/controls/Downloader';
import ModelTestIntentsGrid from './ModelTestIntentsGrid';
import ModelTestTransformsGrid from './ModelTestTransformsGrid';
import ModelTestEntitiesGrid from './ModelTestEntitiesGrid';

class UtteranceTestResults extends PureComponent {
  constructor(props) {
    super(props);
    this.renderModelTestResults = this.renderModelTestResults.bind(this);
    this.renderResultsGrid = this.renderResultsGrid.bind(this);
    this.renderMessage = this.renderMessage.bind(this);
    this.renderEvaluationsResult = this.renderEvaluationsResult.bind(this);

    this.tabs = Constants.TEST_RESULTS_TABS;

    this.state = {
      selectedTabIndex: 0,
    };
  }

  renderResultsGrid(tabName, data) {
    let results = '';
    const { intentsTab, transformationsTab, entitiesTab } = this.tabs;
    if (data.length) {
      switch (tabName) {
      case intentsTab:
        results = <ModelTestIntentsGrid data={data} />;
        break;
      case transformationsTab:
        results = <ModelTestTransformsGrid data={data} />;
        break;
      case entitiesTab:
        results = <ModelTestEntitiesGrid data={data} />;
        break;
      }
    } else {
      results = this.renderMessage(`No ${tabName} Results`);
    }
    return (
      <LegacyRow styleOverride={{ paddingTop: '30px', paddingBottom: '30px' }}>
        {results}
      </LegacyRow>
    );
  }

  onTabSelected = (selectedTab, selectedTabIndex) => {
    this.setState({
      selectedTabIndex,
    });
  }

  renderMessage(message) {
    const { testModelType } = this.props;
    if (_.isNil(message)) {
      message = Constants.DIGITAL_TEST_MESSAGE;
      if (testModelType === Constants.DIGITAL_SPEECH_MODEL || testModelType === Constants.SPEECH_MODEL) {
        message = Constants.SPEECH_TEST_MESSAGE;
      }
    }
    return (<Placeholder message={message} />);
  }

  getFileName() {
    return `audio_${Date.now()}.wav`;
  }

  renderEvaluationsResult() {
    const {
      speechModelTestResults, dispatch, audioSrc, fileType,
    } = this.props;
    const { evaluations = [{}] } = speechModelTestResults;
    const { utterance = '' } = evaluations[0];
    if (utterance) {
      return (
        <div>
          <span className="utterance-result">
            {utterance}
          </span>
          <AudioPlayer
            audioSrc={audioSrc}
            playIcon={Play}
            stopIcon={Stop}
            iconProps={{
              common: {
                width: '20px',
                height: '20px',
                pathFill: '#004C97',
                circleStroke: '#004C97',
              },
            }}
            dispatch={dispatch}
          />
          <span
            style={{
              paddingLeft: '0px',
              paddingRight: '20px',
            }}
          >
            <Downloader
              file={audioSrc}
              fileName={this.getFileName()}
              fileType={fileType}
              dispatch={dispatch}
            >
              <Download
                width="12px"
                height="16px"
                fill="#004C97"
              />
            </Downloader>
          </span>
        </div>
      );
    }
    return '';
  }

  renderModelTestResults() {
    const { digitalModelTestResults, speechModelTestResults, testModelType } = this.props;
    const { evaluations: digitalEvaluationsArray = [] } = digitalModelTestResults;
    const { evaluations: speechEvaluationsArray = [] } = speechModelTestResults;
    const { selectedTabIndex } = this.state;

    // if digital model has been tested and results are available,
    // we have the complete results
    if (digitalEvaluationsArray.length || speechEvaluationsArray.length) {
      let speechResultsRow = '';
      let modelTestResults = _.cloneDeep(digitalEvaluationsArray[0]);

      // If we are testing Speech Model, results of the WebRecognizer takes precedence.
      if (testModelType === Constants.DIGITAL_SPEECH_MODEL || testModelType === Constants.SPEECH_MODEL) {
        const { speechModelTestResults } = this.props;
        const { evaluations: speechEvaluationsArray = [{}] } = speechModelTestResults;
        modelTestResults = Object.assign({}, modelTestResults, speechEvaluationsArray[0]);
      }

      // Recognition Score and Utterance with Word Class

      let {
        utterance = '',
        utteranceWithWordClass = '',
        recognitionScore = '',
      } = modelTestResults;

      if (utteranceWithWordClass || utterance && testModelType === Constants.SPEECH_MODEL) {
        recognitionScore = (parseFloat(recognitionScore) * 100).toFixed(2);

        speechResultsRow = (
          <LegacyRow styleOverride={{
            borderBottom: Constants.BORDER,
          }}
          >
            <ul className="utterance-reco-score-ul">
              <li className="center-item-li recognition-score" key="recognition-score">
                Recognition Score
                <span className="recognition-score-data">
                  {' '}
                  {`${recognitionScore} %`}
                  {' '}
                </span>
              </li>
              <li className="center-item-li word-class">
                {this.renderEvaluationsResult()}
                <span> Utterance with word class:</span>
                {(utteranceWithWordClass !== '') && (<span style={{ fontWeight: 'normal', paddingLeft: '5px' }}>{utteranceWithWordClass}</span>)}
              </li>
            </ul>
          </LegacyRow>
        );
      }

      // Predicted Intents, Transformations and Entities tab
      let tabs = '';
      if (testModelType !== Constants.SPEECH_MODEL) {
        const {
          intents = [],
          transformations = [],
          entities = [],
        } = modelTestResults;

        const { intentsTab, transformationsTab, entitiesTab } = this.tabs;
        let tabsTitle = [`${intentsTab} (${intents.length})`, `${transformationsTab} (${transformations.length})`, `${entitiesTab} (${entities.length})`];
        let tabPanels = [this.renderResultsGrid(intentsTab, intents),
          this.renderResultsGrid(transformationsTab, transformations),
          this.renderResultsGrid(entitiesTab, entities)];

        // if it is a tensorflow model (not an n-gram model) remove the transformations and entities tab
        const { model } = this.props;
        if (model && model.vectorizerTechnology != Constants.MODEL_TECHNOLOGY.N_GRAM) {
          tabsTitle = tabsTitle.slice(0, 1);
          tabPanels = tabPanels.slice(0, 1);
        }
        tabs = (
          <Tabs
            tabs={tabsTitle}
            onTabSelected={this.onTabSelected}
            selectedIndex={selectedTabIndex}
            tabPanels={tabPanels}
            align="left"
          />
        );
      }

      return (
        <span>
          {speechResultsRow}
          {tabs}
        </span>
      );
    }

    return this.renderMessage();
  }

  render() {
    return (
      <LegacyRow styleOverride={{
        borderTop: Constants.BORDER,
      }}
      >
        {this.renderModelTestResults()}
      </LegacyRow>
    );
  }
}

UtteranceTestResults.propTypes = {
  speechModelTestResults: PropTypes.object,
  digitalModelTestResults: PropTypes.object,
  testModelType: PropTypes.string,
  audioSrc: PropTypes.string,
  fileType: PropTypes.string,
};

UtteranceTestResults.defaultProps = {
  speechModelTestResults: {},
  digitalModelTestResults: {},
  testModelType: Constants.DIGITAL_MODEL,
  audioSrc: '',
  fileType: '',
};

export default UtteranceTestResults;
