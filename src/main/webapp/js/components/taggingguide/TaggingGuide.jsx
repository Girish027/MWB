// 2018, April 30
// Migration to using airbnb eslint config.
// We will be refactoring this with the new ui changes.
// For now, just disable rules.
/* eslint-disable no-script-url */
/* eslint-disable jsx-a11y/anchor-is-valid */
/* eslint-disable react/sort-comp */
/* eslint-disable no-shadow */
/* eslint-disable no-param-reassign */

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import Model from 'model';
import validationUtil from 'utils/ValidationUtil';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import * as actionsTaggingGuide from 'state/actions/actions_taggingguide';
import * as actionsApp from 'state/actions/actions_app';
import TaggingGuideGrid from 'components/taggingguide/TaggingGuideGrid';
import SimpleDialog from 'components/controls/SimpleDialog';
import getUrl, { getDataUrl, pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';
import {
  Plus,
  Download,
  ArrowRight,
  ArrowLeft,
  ContextualActionsBar,
  ContextualActionItem,
  ContextualActionInfo,
} from '@tfs/ui-components';
import Placeholder from 'components/controls/Placeholder';
import { actionBar } from 'styles';

const { DISPLAY_MESSAGES } = getLanguage();

export class TaggingGuide extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;
    this.onClickAddTag = this.onClickAddTag.bind(this);
    this.onClickImport = this.onClickImport.bind(this);
    this.onClickExport = this.onClickExport.bind(this);
    this.onClickTaggingGuideTemplate = this.onClickTaggingGuideTemplate.bind(this);
    this.showMissingTagsDialog = this.showMissingTagsDialog.bind(this);
    this.showInvalidTagsDialog = this.showInvalidTagsDialog.bind(this);
    this.getTotalIntentStats = this.getTotalIntentStats.bind(this);
    this.lastImportInfo = this.lastImportInfo.bind(this);
    this.getTagString = this.getTagString.bind(this);
    this.addNewIntent = this.addNewIntent.bind(this);

    this.validateData = this.validateData.bind(this);
  }

  validateData(value, columnId) {
    // TODO - remove 2nd argument after fixing backend.
    // TODO Bug: https://247inc.atlassian.net/browse/NT-2722
    if (columnId === 'intent') {
      return validationUtil.validateTaggingGuideTag(value, true) && validationUtil.checkField(value);
    }
    return !value || ((`${value}`).length < 51 && validationUtil.checkField(value));
  }

  addNewIntent(values) {
    const {
      dispatch, userId, csrfToken, clientId, selectedProjectId: projectId,
    } = this.props;

    dispatch(actionsTaggingGuide.requestCreateTag({
      userId, csrfToken, projectId, values, clientId,
    })).then((row) => {
      dispatch(actionsApp.displayGoodRequestMessage(DISPLAY_MESSAGES.tagCreated));
    }).catch((error) => {
      dispatch(actionsApp.displayWarningRequestMessage(DISPLAY_MESSAGES.tagCreateFail));
    });
  }


  onClickAddTag() {
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange({
      header: 'Add Intent',
      dispatch,
      type: Constants.DIALOGS.ADD_INTENT,
      onClickAdd: this.addNewIntent,
      validateData: this.validateData,
    }));
  }

  onClickImport(isTaggingGuideEmpty) {
    if (!isTaggingGuideEmpty) {
      return;
    }
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange({
      type: Constants.DIALOGS.IMPORT_TAGGING_GUIDE,
      project: this.props.project,
      onComplete: () => {
        this.forceUpdate();
      },
    }));
  }

  onClickExport() {
    const locationUrl = getUrl(pathKey.intentGuideExport, {
      projectId: this.props.selectedProjectId,
      clientId: this.props.clientId,
    });
    document.location = locationUrl;
  }

  onClickTaggingGuideTemplate() {
    const locationUrl = getDataUrl(pathKey.intentGuideTemplate);
    document.location = locationUrl;
  }

  lastImportInfo() {
    const { taggingGuide } = this.props;
    const { lastImportInfo } = taggingGuide;

    if (!lastImportInfo || typeof lastImportInfo.validTagCount === 'undefined') {
      return (
        <ContextualActionInfo
          label="Last Tags Import:"
          value="none"
        />
      );
    }

    const {
      validTagCount, importedBy, missingTags, invalidTags,
    } = lastImportInfo;

    return (
      <div>
        { missingTags && missingTags.length ? (
          <ContextualActionItem
            data-qa="lastImportInfoMissingTags"
            onClickAction={this.showMissingTagsDialog}
            right
          >
              Missing Tags:
            {' '}
            {missingTags.length}
          </ContextualActionItem>
        ) : null }

        { invalidTags && invalidTags.length ? (
          <ContextualActionItem
            data-qa="lastImportInfoInvalidTags"
            onClickAction={this.showInvalidTagsDialog}
            right
          >
              Invalid Tags:
            {' '}
            {invalidTags.length}
          </ContextualActionItem>
        ) : null}

        <ContextualActionInfo
          data-qa="lastImportInfoValidTagCount"
          label="Last Tags Import:"
          value={validTagCount + this.getTagString(validTagCount)}
        />

        {importedBy ? (
          <ContextualActionInfo
            data-qa="lastImportInfoImportedBy"
            label="Imported By:"
            value={importedBy}
          />
        ) : null}
      </div>
    );
  }

  showMissingTagsDialog() {
    const { taggingGuide, dispatch } = this.props;
    const missingTags = taggingGuide.lastImportInfo ? taggingGuide.lastImportInfo.missingTags : null;
    if (!missingTags || !missingTags.length) {
      return;
    }

    dispatch(actionsApp.modalDialogChange({
      type: Constants.DIALOGS.SIMPLE_DIALOG,
      header: 'Missing Tags',
      message: (
        <div>
          {missingTags.join(', ')}
        </div>
      ),
      actions: [SimpleDialog.ACTION_OK],
      className: 'MissingTags',
    }));
  }

  showInvalidTagsDialog() {
    const { taggingGuide, dispatch } = this.props;
    const invalidTags = taggingGuide.lastImportInfo ? taggingGuide.lastImportInfo.invalidTags : null;
    if (!invalidTags || !invalidTags.length) {
      return;
    }

    dispatch(actionsApp.modalDialogChange({
      type: Constants.DIALOGS.SIMPLE_DIALOG,
      header: 'Invalid Tags',
      message: (
        <div>
          {invalidTags.join(', ')}
        </div>
      ),
      actions: [SimpleDialog.ACTION_OK],
      className: 'InvalidTags',
    }));
  }

  getTagString(length) {
    return length === 1 ? 'tag' : 'tags';
  }

  getTotalIntentStats() {
    const { taggingGuide, intents } = this.props;
    const { usedForTaggingRatio } = taggingGuide;
    const intentsArray = intents || [];
    const percentageTagging = Math.round(usedForTaggingRatio * 1000) / 10;
    const stats = `${intentsArray.length} ${this.getTagString(intentsArray.length)} (${percentageTagging}%)`;
    return stats;
  }

  render() {
    const {
      userFeatureConfiguration, intents, clientId, selectedProjectId,
    } = this.props;
    const { names } = featureFlagDefinitions;
    let isTaggingGuideEmpty = true;
    if (!_.isNil(intents) && intents.length > 0) {
      isTaggingGuideEmpty = false;
    }

    return (
      <div id="TaggingGuide">
        { (isFeatureEnabled(names.intentGuideContextualActionBar, userFeatureConfiguration)) ? (
          <ContextualActionsBar styleOverride={actionBar.contextualBar}>
            <ContextualActionItem
              icon={Plus}
              onClickAction={this.onClickAddTag}
            >
              ADD
            </ContextualActionItem>
            <ContextualActionItem
              icon={ArrowLeft}
              onClickAction={() => this.onClickImport(isTaggingGuideEmpty)} // eslint-disable-line no-console
              disabled={!isTaggingGuideEmpty}
            >
              IMPORT
            </ContextualActionItem>
            <ContextualActionItem
              icon={ArrowRight}
              onClickAction={this.onClickExport}
            >
              EXPORT
            </ContextualActionItem>
            <ContextualActionItem
              icon={Download}
              onClickAction={this.onClickTaggingGuideTemplate}
            >
              INTENTS TEMPLATE
            </ContextualActionItem>
            {this.lastImportInfo()}
            <ContextualActionInfo
              label="Total Intents:"
              value={this.getTotalIntentStats()}
            />
          </ContextualActionsBar>
        ) : null }
        {(_.isNil(intents) || intents.length == 0)
          ? (<Placeholder message={Constants.TAGS_NOT_AVAILABLE} />)
          : (
            <TaggingGuideGrid
              projectId={selectedProjectId}
              clientId={clientId}
              validateData={this.validateData}
              intents={intents}
            />
          )
        }
      </div>
    );
  }
}

TaggingGuide.defaultProps = {
  intents: [],
  dispatch: () => {},
};

TaggingGuide.propTypes = {
  app: PropTypes.object,
  taggingGuide: PropTypes.object,
  intents: PropTypes.array,
  selectedProjectId: PropTypes.string,
  clientName: PropTypes.string,
  clientId: PropTypes.string,
  validateData: PropTypes.func,
  dispatch: PropTypes.func,
  clientDataLoaded: PropTypes.bool,
};

const mapStateToProps = (state, ownProps) => {
  const projectId = state.projectListSidebar.selectedProjectId;
  return {
    app: state.app,
    taggingGuide: state.taggingGuide,
    clientName: state.header.client.name,
    clientId: state.header.client.id,
    selectedProjectId: projectId,
    clientDataLoaded: state.projectsManager.clientDataLoaded,
    userFeatureConfiguration: state.app.userFeatureConfiguration,
    project: Model.ProjectsManager.getProject(projectId) || {},
  };
};

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(TaggingGuide);
