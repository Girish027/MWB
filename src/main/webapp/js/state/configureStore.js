import { createStore, applyMiddleware, combineReducers } from 'redux';
import thunkMiddleware from 'redux-thunk';
import { createLogger } from 'redux-logger';
import { reducer as formReducer } from 'redux-form';
import { appReducer, defaultState as defaultAppState } from 'state/reducers/app_reducers';
import { headerReducer, defaultState as defaultHeaderState } from 'state/reducers/header_reducers';
import { projectsManagerReducer } from 'state/reducers/projectsManager_reducers';
import { consistencyReportReducer, defaultState as defaultConsistencyReportState } from 'state/reducers/consistencyReport_reducers';
import { Reducers as gridReducers } from 'react-redux-grid';
import config from 'config';
import { Map } from 'immutable';
import { projectListSidebarReducer, defaultState as defaultProjectListSidebarState } from './reducers/projectListSidebar_reducers';
import { taggingGuideReducer, defaultState as defaultTaggingGuideState } from './reducers/taggingGuide_reducers';
import { taggingGuideImportReducer } from './reducers/taggingGuideImport_reducers';
import { tagDatasetsReducer, defaultState as defaultTagDatasetsState } from './reducers/tag_datasets_reducers';
import { createDatasetDialogReducer } from './reducers/createDatasetDialog_reducers';
import { cellEditableReducer } from './reducers/cellEditable_reducers';
import { cellEditableManualTagSuggestReducer } from './reducers/cellEditableManualTagSuggestReducer_reducers';
import { reportsReducer } from './reducers/reports_reducers';
import { configReducer } from './reducers/config_reducers';
import { preferencesReducer } from './reducers/preferences_reducer';

let store;

if (config.env != 'test') {
  const transformObject = (obj) => {
    try {
      return JSON.parse(JSON.stringify(obj));
    } catch (err) {
      return obj;
    }
  };
  const loggerMiddleware = createLogger({
    stateTransformer: (state) => transformObject(state),
    actionTransformer: (action) => transformObject(action),
  });

  const rootReducer = combineReducers({
    app: appReducer,
    form: formReducer,
    ...gridReducers,
    config: configReducer,
    projectListSidebar: projectListSidebarReducer,
    header: headerReducer,
    taggingGuide: taggingGuideReducer,
    taggingGuideImport: taggingGuideImportReducer,
    projectsManager: projectsManagerReducer,
    tagDatasets: tagDatasetsReducer,
    consistencyReport: consistencyReportReducer,
    createDatasetDialog: createDatasetDialogReducer,
    cellEditable: cellEditableReducer,
    cellEditableManualTagSuggest: cellEditableManualTagSuggestReducer,
    reports: reportsReducer,
    preferences: preferencesReducer,
  });

  const configureStore = preloadedState => createStore(
    rootReducer,
    preloadedState,
    (config.env != 'production')
      ? applyMiddleware(thunkMiddleware, loggerMiddleware)
      : applyMiddleware(thunkMiddleware),
  );

  store = configureStore();
} else {
  /* store for tests */
  /* ToDo: move to separate file */

  let testProjects = Map();
  testProjects = testProjects.set('-1', {
    id: '-1',
    clientId: '-1',
    name: 'Test Project',
    created: 1498754327022,
    locale: 'en-US',
    description: 'Test Project Description',
    vertical: 'OTHER',
  });
  let testProjectDatasets = Map();
  const datasetsMap = Map({
    '-10': {
      _key: '-10',
      id: '-10',
      clientId: '-1',
      projectId: '-1',
      name: 'Dataset One',
      type: 'Audio/Voice (Live)',
      description: 'Test Dataset One',
      locale: 'en-US',
      created: 1503012662726,
      status: 'COMPLETED',
      task: 'INDEX',
      percentComplete: undefined,
      errorCode: undefined,
    },
    '-11': {
      _key: '-11',
      id: '-11',
      clientId: '-1',
      projectId: '-1',
      name: 'Dataset Two',
      type: 'Audio/Voice (Live)',
      description: 'Test Dataset Two',
      locale: 'en-US',
      created: 1503052662727,
      status: 'COMPLETED',
      task: 'INDEX',
      percentComplete: undefined,
      errorCode: undefined,
    },
    '-12': {
      _key: '-12',
      id: '-12',
      clientId: '-1',
      projectId: '-1',
      name: 'Dataset Three',
      type: 'Chat/Text',
      description: 'Test Dataset Three',
      locale: 'en-US',
      created: 1503032662727,
      status: 'NULL',
    },
    '-13': {
      _key: '-13',
      id: '-13',
      clientId: '-1',
      projectId: '-1',
      name: 'Dataset Four',
      type: 'Chat/Text',
      description: 'Test Dataset Four',
      locale: 'en-US',
      created: 1503031662727,
      status: 'COMPLETED',
      task: 'INDEX',
      percentComplete: undefined,
      errorCode: undefined,
    },
  });
  testProjectDatasets = testProjectDatasets.set('-1', datasetsMap);


  const mockStore = {
    app: {
      ...defaultAppState,
      userId: '-1',
      csrfToken: 'test',
    },
    header: {
      ...defaultHeaderState,
      client: { id: '-1', name: 'Test Client' },
      username: 'TestUser@247-inc.com',
    },
    consistencyReport: {
      ...defaultConsistencyReportState,
    },
    projectsManager: {
      projects: testProjects,
      projectDatasets: testProjectDatasets,
      trackDatasetsIds: {},
      projectModels: Map(),
      projectConfigs: Map(),
      projectIntents: Map(),
      trackModelsIds: {},
      trackModelsBatchTestIds: {},

    },
    projectListSidebar: {
      ...defaultProjectListSidebarState,
    },
    taggingGuide: {
      ...defaultTaggingGuideState,
    },
    tagDatasets: {
      ...defaultTagDatasetsState,
    },
    config: { },
    preferences: { },
  };

  const rootReducer = combineReducers({
    app: appReducer,
    header: headerReducer,
    consistencyReport: consistencyReportReducer,
    projectsManager: projectsManagerReducer,
    projectListSidebar: projectListSidebarReducer,
    taggingGuide: taggingGuideReducer,
    tagDatasets: tagDatasetsReducer,
    config: configReducer,
    preferences: preferencesReducer,
  });

  store = createStore(rootReducer, mockStore, applyMiddleware(thunkMiddleware));
}

export default store;
