import _ from 'lodash';

const isFeatureEnabled = (feature, userFeatureConfiguration, enabledCheck = 'show') => {
  if (!_.isNil(userFeatureConfiguration) && !_.isNil(feature) && !_.isNil(enabledCheck)) {
    const userFeature = userFeatureConfiguration[feature];

    if (!_.isNil(userFeature)) {
      return userFeature === enabledCheck;
    }
  }

  return false;
};

// These need to match the flags used in conf/ui.config.js

export const featureFlagDefinitions = {
  names: {
    models: 'models',
    modelTrainingOutputs: 'modelTrainingOutputs',
    modelDownload: 'modelDownload',
    kibanaLogs: 'kibanaLogs',
    appHelp: 'appHelp',
    transformationHelp: 'transformationHelp',
    tagSearchHelp: 'tagSearchHelp',
    modelConfigDownload: 'modelConfigDownload',
    modelStatistics: 'modelStatistics',
    modelDelete: 'modelDelete',
    modelConfigTemplate: 'modelConfigTemplate',
    datasetTemplate: 'datasetTemplate',
    intentGuideTemplate: 'intentGuideTemplate',
    datasetExport: 'datasetExport',
    datasetDelete: 'datasetDelete',
    intentGuideExport: 'intentGuideExport',
    datasetExportById: 'datasetExportById',
    projectDelete: 'projectDelete',
    downloadResults: 'downloadResults',
    createNewVersionTrainingConfigTab: 'createNewVersionTrainingConfigTab',
    createNewVersionModelReviewTab: 'createNewVersionModelReviewTab',
    granularMessage: 'granularMessage',
    intentGuideDelete: 'intentGuideDelete',
    intentGuideUpdate: 'intentGuideUpdate',
    resolveInconsistencies: 'resolveInconsistencies',
    intentGuideContextualActionBar: 'intentGuideContextualActionBar',
    speechBundledUnbundled: 'speechBundledUnbundled',
    datasetType: 'datasetType',
    suggestedIntent: 'suggestedIntent',
    supportLink: 'supportLink',
  },
  options: {
    hide: 'hide',
    show: 'show',
  },
};

export default isFeatureEnabled;
