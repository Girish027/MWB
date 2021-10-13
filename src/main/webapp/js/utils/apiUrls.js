
import _ from 'lodash';

const root = '/nltools/private';
const dataRoot = '/dist/data';

export const pathKey = {
  config: 'config',
  clients: 'clients',
  csrfToken: 'csrfToken',
  version: 'version',
  files: 'files',
  projects: 'projects',
  testDigitalTranscription: 'testDigitalTranscription',
  patchCombinedModel: 'patchCombinedModel',
  modelBatchTest: 'modelBatchTest',
  speechModel: 'speechModel',
  modelCheckBatchTest: 'modelCheckBatchTest',
  modelsDigital: 'modelsDigital',
  listBatchTests: 'listBatchTests',
  testSpeechUtterance: 'testSpeechUtterance',
  dummySpeechTest: 'dummySpeechTest',
  projectsForClient: 'projectsForClient',
  projectById: 'projectById',
  projectDatasets: 'projectDatasets',
  promoteProject: 'promoteProject',
  demoteProject: 'demoteProject',
  suggestedTags: 'suggestedTags',
  configById: 'configById',
  consistency: 'consistency',
  importDataset: 'importDataset',
  importDatasetColumnsBind: 'importDatasetColumnsBind',
  datasets: 'datasets',
  datasetsByClient: 'datasetsByClient',
  datasetById: 'datasetById',
  datasetSearch: 'datasetSearch',
  datasetStats: 'datasetStats',
  datasetValidationStatsById: 'datasetValidationStatsById',
  addManualTag: 'addManualTag',
  updateManualTag: 'updateManualTag',
  deleteTag: 'deleteTag',
  datasetComment: 'datasetComment',
  datasetExportById: 'datasetExportById',
  datasetExport: 'datasetExport',
  datasetTransform: 'datasetTransform',
  datasetTransformRetry: 'datasetTransformRetry',
  datasetTransformCancel: 'datasetTransformCancel',
  dataset: 'dataset',
  addDatasetAutoTag: 'addDatasetAutoTag',
  buildModel: 'buildModel',
  buildModelSpeech: 'buildModelSpeech',
  modelDelete: 'modelDelete',
  modelUpdate: 'modelUpdate',
  models: 'models',
  reportFields: 'reportFields',
  projectDatasetsTransformStatus: 'projectDatasetsTransformStatus',
  modelState: 'modelState',
  modelDownload: 'modelDownload',
  modelStatistics: 'modelStatistics',
  modelMetadata: 'modelMetadata',
  modelTrainingOutputs: 'modelTrainingOutputs',
  modelConfigDownload: 'modelConfigDownload',
  modelsForProject: 'modelsForProject',
  modelConfigsForProject: 'modelConfigsForProject',
  getSpeechConfig: 'getSpeechConfig',
  intentGuideStats: 'intentGuideStats',
  intentGuideTag: 'intentGuideTag',
  intentGuideExport: 'intentGuideExport',
  intentsForProject: 'intentsForProject',
  intentGuideSearch: 'intentGuideSearch',
  intentGuideImportCancel: 'intentGuideImportCancel',
  intentGuideImportCommit: 'intentGuideImportCommit',
  intentGuideImport: 'intentGuideImport',
  intentGuideImportMapping: 'intentGuideImportMapping',
  intentGuideTemplate: 'intentGuideTemplate',
  modelConfigTemplate: 'modelConfigTemplate',
  datasetTemplate: 'datasetTemplate',
  wordClassDefault: 'wordClassDefault',
  locales: 'locales',
  verticals: 'verticals',
  datatypes: 'datatypes',
  appConfig: 'appConfig',
  userGroups: 'userGroups',
  logIngest: 'logIngest',
  addTechnology: 'addTechnology',
  getTechnology: 'getTechnology',
  updateTechnology: 'updateTechnology',
  getVectorizer: 'getVectorizer',
  getVectorizerByClientProject: 'getVectorizerByClientProject',
  getVectorizerByTechnology: 'getVectorizerByTechnology',
};

const apiPath = {
  [pathKey.config]: () => 'configs',
  [pathKey.clients]: data => `clients?itsClientId=${data.itsClientId}&itsAppId=${data.itsAppId}`,
  [pathKey.version]: () => 'version',
  [pathKey.csrfToken]: () => 'csrftoken',
  [pathKey.appConfig]: data => 'configProperties?propertyName=kibanaLogURL,kibanaLogIndex,itsURL,ufpURL,oAuthLogoutURL,environment,amplitudeApiKey,logoutWarningTimeout,logoutTimeout,speechTestTimeout,userAccountLink,internalSupportLink,externalSupportLink,documentationLink,orionURL',

  // Test
  [pathKey.testDigitalTranscription]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/eval_transcriptions?testModelType=${data.testModelType}`,
  [pathKey.modelBatchTest]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/eval_datasets?testModelType=${data.testModelType}`,
  [pathKey.modelCheckBatchTest]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/check_eval/${data.modelTestJobId}`,
  [pathKey.listBatchTests]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/batch_tests`,
  [pathKey.testSpeechUtterance]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/eval_utterance?fileType=${data.fileType}`,
  [pathKey.dummySpeechTest]: () => 'DummySpeechTest.wav',
  // Projects
  [pathKey.projects]: data => `clients/${data.clientId}/projects`,
  [pathKey.projectsForClient]: (data) => {
    let url = `clients/${data.clientId}/projects?clientId=${data.clientId}`;
    if (typeof data.startIndex !== 'undefined') {
      url += `&startIndex=${data.startIndex}`;
    }
    if (typeof data.limit !== 'undefined') {
      url += `&limit=${data.limit}`;
    }
    return url;
  },
  [pathKey.projectById]: data => `clients/${data.clientId}/projects/${data.projectId}`,
  [pathKey.promoteProject]: (data) => {
    let url = `clients/${data.clientId}/projects/${data.projectId}/promote?globalProjectName=${data.globalProjectName}`;
    if (data.globalProjectId) {
      url += `&globalProjectId=${data.globalProjectId}`;
    }
    return url;
  },
  [pathKey.demoteProject]: data => `clients/${data.clientId}/projects/${data.projectId}/demote`,
  [pathKey.projectDatasets]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets`,
  [pathKey.projectDatasetsTransformStatus]: data => `clients/${data.clientId}/projects/transform/status`,
  //
  [pathKey.files]: data => `files/${data.fileId}`,
  // Intent Guide
  [pathKey.intentGuideStats]: data => `search/${data.projectId}/intentguide/importstats`,
  [pathKey.intentGuideTag]: data => `content/${data.projectId}/intents/${data.intentId}`,
  [pathKey.intentsForProject]: data => `content/${data.projectId}/intents`,
  [pathKey.intentGuideSearch]: (data) => {
    const sort = data.sort;

    const uri = `search/${data.projectId}/intentguide?${
      sort ? `sortBy=${sort.property}:${sort.direction}` : ''}`;

    return uri;
  },
  [pathKey.intentGuideExport]: data => `clients/${data.clientId}/projects/${data.projectId}/taggingguide/export`,
  [pathKey.intentGuideImportCancel]: data => `taggingguide/${data.clientId}/${data.projectId}/import/${data.token}/abort`,
  [pathKey.intentGuideImportCommit]: data => `taggingguide/${data.clientId}/${data.projectId}/import/${data.token}/commit`,
  [pathKey.intentGuideImport]: data => `taggingguide/${data.clientId}/${data.projectId}/import`,
  [pathKey.intentGuideImportMapping]: (data) => {
    const uri = `taggingguide/${data.clientId}/${
      data.projectId}/import/${
      data.token}/column/mapping?ignoreFirstRow=${
      data.skipFirstRow ? 'true' : 'false'}`;

    return uri;
  },
  [pathKey.intentGuideTemplate]: () => 'TaggingGuideTemplate.zip',
  // Configs
  [pathKey.configById]: data => `clients/${data.clientId}/configs/${data.configId}/data`,
  [pathKey.getSpeechConfig]: data => `clients/${data.clientId}/configs/${data.configId}`,
  [pathKey.modelConfigsForProject]: data => `clients/${data.clientId}/projects/${data.projectId}/configs`,
  [pathKey.modelConfigTemplate]: () => 'ModelConfigTemplate.zip',
  // Consistency
  [pathKey.consistency]: data => `content/${data.projectId}/verify?startIndex=${data.startIndex
  }&limit=${data.limit}&sortBy=${data.sortProperty}:${data.sortDirection}`,
  // Tags
  [pathKey.suggestedTags]: data => `search/${data.projectId}/intents?q=${data.intent}`,
  [pathKey.addManualTag]: data => `content/${data.projectId}/tag`,
  [pathKey.updateManualTag]: data => `content/${data.projectId}/tag/update`,
  [pathKey.deleteTag]: data => `content/${data.projectId}/tag/delete`,
  // Datasets
  [pathKey.importDataset]: () => 'files/import',
  [pathKey.importDatasetColumnsBind]: data => `files/import/${data.token}/column/mapping?ignoreFirstRow=${data.skipFirstRow ? 'true' : 'false'}`,
  [pathKey.datasets]: data => 'datasets',
  [pathKey.datasetsByClient]: data => `datasets?clientId=${data.clientId}`,
  [pathKey.datasetById]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}`,
  [pathKey.datasetExportById]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}/export`,
  [pathKey.datasetExport]: data => `clients/${data.clientId}/projects/${data.projectId}/export`,
  [pathKey.datasetSearch]: data => `search/${data.projectId}?startIndex=${data.startIndex}&limit=${data.limit}&sortBy=${data.sortKey}:${data.sortDirection}`,
  [pathKey.datasetStats]: data => `search/${data.projectId}/stats`,
  [pathKey.datasetValidationStatsById]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}/validate`,
  [pathKey.datasetComment]: data => `content/${data.projectId}/comment`,
  [pathKey.datasetTransform]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}/transform?${data.queryParams}`,
  [pathKey.datasetTransformRetry]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}/transform/retry?${data.queryParams}`,
  [pathKey.datasetTransformCancel]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}/transform/cancel`,
  [pathKey.dataset]: data => `clients/${data.clientId}/projects/${data.projectId}/datasets/${data.datasetId}`,
  [pathKey.addDatasetAutoTag]: data => `projects/${data.projectId}/datasets/${data.datasetId}?autoTagDataset=true`,
  [pathKey.datasetTemplate]: () => 'sampleDataset.zip',
  // models
  [pathKey.buildModel]: data => `clients/${data.clientId}/models/${data.modelId}/build`,
  [pathKey.buildModelSpeech]: data => `clients/${data.clientId}/models/${data.modelId}/build?modelType=speech`,
  [pathKey.modelState]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/status`,
  [pathKey.modelsForProject]: data => `clients/${data.clientId}/projects/${data.projectId}/models`,
  [pathKey.modelsDigital]: data => `clients/${data.clientId}/projects/${data.projectId}/models?modelTechnology=${data.modelTechnology}&toDefault=${data.toDefault}`,
  [pathKey.models]: data => `clients/${data.clientId}/projects/${data.projectId}/models`,
  [pathKey.speechModel]: data => `clients/${data.clientId}/projects/${data.projectId}/models?trainNow=${data.trainNow}&modelType=${data.modelType}`,
  [pathKey.modelDelete]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}`,
  [pathKey.modelUpdate]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}`,
  [pathKey.wordClassDefault]: () => 'DefaultWordclassFile.zip',
  [pathKey.patchCombinedModel]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/speech?digitalHostedUrl=${data.digitalHostedUrl}`,


  [pathKey.modelConfigDownload]: data => `configs/${data.configId}/download`,
  [pathKey.modelTrainingOutputs]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/training-outputs`,
  [pathKey.modelStatistics]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/statistics`,
  [pathKey.modelDownload]: data => `clients/${data.clientId}/projects/${data.projectId}/models/${data.modelId}/download`,
  // Reports
  [pathKey.reportFields]: data => `clients/${data.clientId}/search/${data.projectId}/datasets/${data.datasetId}/getReportFields`,
  [pathKey.locales]: data => 'resources/locales',
  [pathKey.userGroups]: data => 'userGroups',
  [pathKey.verticals]: data => 'resources/verticals',
  [pathKey.datatypes]: data => 'resources/datatypes',
  [pathKey.logIngest]: data => `ingest/${data.logLevel}/log`,
  [pathKey.addTechnology]: data => `preference?clientId=${data.clientId}&setDefault=${data.setDefault}`,
  [pathKey.getTechnology]: data => `preference?clientId=${data.clientId}`,
  [pathKey.updateTechnology]: data => `preference?clientId=${data.clientId}&id=${data.id}`,
  [pathKey.getVectorizer]: () => 'vectorizer',
  [pathKey.getVectorizerByClientProject]: data => `vectorizer/clients/${data.clientId}/projects/${data.projectId}`,
  [pathKey.getVectorizerByTechnology]: data => `vectorizer/type/${data.technology}`,
};

const versions = {
  v1: 'v1',
};

export const versionKey = {
  v1: 'v1',
};

export const getDataUrl = (reqPath, data = null) => {
  const apiPathFormatter = apiPath[reqPath];
  let url = null;

  if (apiPathFormatter) {
    const apiPathItem = apiPathFormatter(data);

    if (!_.isNil(apiPathItem)) {
      url = `${dataRoot}/${apiPathItem}`;
    }
  }
  return url;
};

export const getItsURL = (data) => {
  const { itsURLPath, itsClientId = '', itsAppId = '' } = data;
  return `${itsURLPath}?clientid=${itsClientId.toLowerCase()}&appid=${itsAppId.toLowerCase()}`;
};

const getUrl = (reqPath, data = null, reqVersion = versionKey.v1) => {
  let url = null;

  const version = versions[reqVersion];
  const apiPathFormatter = apiPath[reqPath];

  if (apiPathFormatter) {
    const apiPathItem = apiPathFormatter(data);

    if (!_.isNil(version) && !_.isNil(apiPathItem)) {
      url = `${root}/${version}/${apiPathItem}`;
    }
  }
  return url;
};

export default getUrl;
