import _ from 'lodash';
import Constants from 'constants/Constants';

export const RouteNames = {
  LANDING: 'landing',
  TAG_DATASETS: 'tagDatasets',
  TAG_DATASETS_BETA: 'betatagDatasets',
  PROJECTS: 'projects',
  PROJECTID: 'projectId',
  DATASETS: 'datasets',
  MODELS: 'models',
  CREATEMODEL: 'createModel',
  CREATESPEECHMODEL: 'createSpeechModel',
  TUNEMODEL: 'tuneModel',
  VIEWMODEL: 'viewModel',
  VIEWSPEECHMODEL: 'viewSpeechModel',
  TUNESPEECHMODEL: 'tuneSpeechModel',
  TESTMODEL: 'testModel',
  BATCHTESTMODEL: 'batchtestModel',
  REPORTS: 'reports',
  RESOLVE_INCONSISTENCY: 'resolveInconsistency',
  UPDATEPROJECT: 'updateProject',
  READPROJECT: 'readProject',
  MANAGE_INTENTS: 'manageIntents', // tagging guide
  MANAGE_SETTINGS: 'manageSettings', // Settings Tab
  SETTINGS: 'settings',
  OVERVIEW: 'overview',
  NODEANALYTICS: 'nodeanalytics',
};

export const urlMap = {
  TAG_DATASETS: '/tag-datasets',
  TAG_DATASETS_BETA: '/tag-datasets-beta',
  PROJECTS: '/projects',
  DATASETS: '/datasets',
  MODELS: '/models',
  CREATEMODEL: '/models/create',
  CREATESPEECHMODEL: '/models/speechcreate',
  TUNEMODEL: '/models/tune',
  VIEWMODEL: '/models/view',
  VIEWSPEECHMODEL: '/models/viewspeech',
  TUNESPEECHMODEL: '/models/tunespeech',
  TESTMODEL: '/models/test',
  BATCHTESTMODEL: '/models/batchtest',
  RESOLVE_INCONSISTENCY: '/resolve-inconsistency',
  UPDATEPROJECT: '/projects/update',
  MANAGE_INTENTS: '/manage-intents',
  MANAGE_SETTINGS: '/manage-settings',
  SETTINGS: '/settings',
  OVERVIEW: '/overview',
  NODE_ANALYTICS: '/node-analytics',
};

export const routeRoot = '';

const clientSuffix = (params) => {
  const client = !_.isNil(params.client) ? params.client : {};
  const { standardClientName: standardClientId = '', itsAppId = '' } = client;
  return `?clientid=${standardClientId}&appid=${itsAppId}`;
};

const projectSuffix = params => `${clientSuffix(params)}&projectid=${params.projectId}`;
const modelSuffix = params => `${projectSuffix(params)}&modelid=${params.modelId}`;

const routeURL = {
  [RouteNames.LANDING]: params => `${routeRoot}`,
  [RouteNames.UPDATEPROJECT]: params => `${routeRoot}${urlMap.UPDATEPROJECT}${projectSuffix(params)}`,
  [RouteNames.PROJECTS]: params => `${routeRoot}${urlMap.PROJECTS}${clientSuffix(params)}`,
  [RouteNames.PROJECTID]: params => `${routeRoot}${urlMap.PROJECTS}${projectSuffix(params)}`,
  [RouteNames.READPROJECT]: params => `${routeRoot}/${params.routeTag}${projectSuffix(params)}`,
  [RouteNames.TAG_DATASETS]: params => `${routeRoot}${urlMap.TAG_DATASETS}${projectSuffix(params)}`,
  [RouteNames.TAG_DATASETS_BETA]: params => `${routeRoot}${urlMap.TAG_DATASETS_BETA}${projectSuffix(params)}`,
  [RouteNames.DATASETS]: params => `${routeRoot}${urlMap.DATASETS}${projectSuffix(params)}`,
  [RouteNames.MODELS]: params => `${routeRoot}${urlMap.MODELS}${projectSuffix(params)}`,
  [RouteNames.CREATEMODEL]: params => `${routeRoot}${urlMap.CREATEMODEL}${projectSuffix(params)}`,
  [RouteNames.CREATESPEECHMODEL]: params => `${routeRoot}${urlMap.CREATESPEECHMODEL}${projectSuffix(params)}`,
  [RouteNames.TUNEMODEL]: params => `${routeRoot}${urlMap.TUNEMODEL}${projectSuffix(params)}`,
  [RouteNames.VIEWMODEL]: params => `${routeRoot}${urlMap.VIEWMODEL}${projectSuffix(params)}`,
  [RouteNames.VIEWSPEECHMODEL]: params => `${routeRoot}${urlMap.VIEWSPEECHMODEL}${projectSuffix(params)}`,
  [RouteNames.TUNESPEECHMODEL]: params => `${routeRoot}${urlMap.TUNESPEECHMODEL}${projectSuffix(params)}`,
  [RouteNames.TESTMODEL]: params => `${routeRoot}${urlMap.TESTMODEL}${modelSuffix(params)}`,
  [RouteNames.BATCHTESTMODEL]: params => `${routeRoot}${urlMap.BATCHTESTMODEL}${modelSuffix(params)}`,
  [RouteNames.REPORTS]: params => `${routeRoot}/reports${projectSuffix(params)}`,
  [RouteNames.RESOLVE_INCONSISTENCY]: params => `${routeRoot}${urlMap.RESOLVE_INCONSISTENCY}${projectSuffix(params)}`,
  [RouteNames.MANAGE_INTENTS]: params => `${routeRoot}${urlMap.MANAGE_INTENTS}${projectSuffix(params)}`,
  [RouteNames.MANAGE_SETTINGS]: params => `${routeRoot}${urlMap.MANAGE_SETTINGS}${projectSuffix(params)}`,
  [RouteNames.OVERVIEW]: params => `${routeRoot}${urlMap.OVERVIEW}${projectSuffix(params)}`,
  [RouteNames.NODE_ANALYTICS]: params => `${routeRoot}${urlMap.NODEANALYTICS}${projectSuffix(params)}`,
  [RouteNames.SETTINGS]: params => `${routeRoot}${urlMap.SETTINGS}${clientSuffix(params)}`,
};

export const uniqueURLStringToRouteMap = {
  projects: RouteNames.PROJECTS,
  datasets: RouteNames.DATASETS,
  models: RouteNames.MODELS,
  [Constants.MANAGE_MODELS_DATASETS]: RouteNames.PROJECTS,
};

let lastRoute = '';
const goToRoute = (routeName, params, history) => {
  const newRoute = routeURL[routeName](params);
  const { hash = '' } = location;
  let currentRoute = hash.slice(1);
  if (currentRoute !== newRoute) {
    history.push(newRoute);
  }
};

export const constructRoute = (routeName, params) => routeURL[routeName](params);

export default goToRoute;
