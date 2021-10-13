import breadcrumbSegments from 'components/AppHeader/breadcrumbSegments';
import store from 'state/configureStore';
import * as appActions from 'state/actions/actions_app.js';

describe('breadcrumbSegments', () => {
  const testClient = {
    id: '82',
    name: 'Air Canada',
    itsAppId: 'chatbot',
  };

  const testProject = {
    clientId: '82',
    created: 1533920233825,
    description: 'week 1 model',
    id: '2',
    locale: 'en-US',
    name: 'week 1',
    vertical: 'TRAVEL',
  };

  const testModel = {
    configId: '8',
    configName: 'week 1-cfg',
    created: 1533920956600,
    datasetIds: ['2'],
    description: 'week 1 model',
    id: '6',
    modelToken: '5bd94388-224d-40bd-8ec0-dcebf57f8d2c',
    name: 'week 1',
    projectId: '2',
    status: 'COMPLETED',
    updated: 1533920956600,
    version: 5,
    _key: '6',
  };

  const mockPush = jest.fn();

  const testHistory = {
    push: mockPush,
  };
  const itsURLPath = 'https://stable.developer.sv2.247-inc.net/integratedtoolsuite';

  const testInfo = {
    selectedClient: testClient,
    selectedProject: testProject,
    selectedAppId: testClient.itsAppId,
    model: testModel,
  };

  const createTestMatch = (path, url) => ({
    path, // '/clients/:clientId/projects/:projectId/models/:modelId/test',
    url, // '/clients/82/projects/2/models/6/test',
    isExact: true,
  });

  beforeAll(() => {
    global.localStorage.itsClientId = 'dish';
    global.localStorage.standardClientId = 'dish';
    global.localStorage.itsAppId = 'referencebot';
    store.dispatch(appActions.updateUserGroups({ IAT_INTERNAL: 'IAT_INTERNAL' }));
  });

  afterAll(() => {
    delete global.localStorage.itsClientId;
    delete global.localStorage.itsAppId;
  });

  test('returned segments match length and label for model', () => {
    const path = '/models/test';
    const url = '/models/test';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments.length).toEqual(4);
    expect(segments[3].label).toEqual('Version 5');
    expect(segments).toMatchSnapshot();
  });

  test('returned default urlItems when url is null', () => {
    const path = '/models/test';
    const url = null;
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returned default project id and name when selectedProject is null', () => {
    const path = '/models/test';
    const url = '/models/test';
    const segments = breadcrumbSegments(createTestMatch(path, url), { ...testInfo, selectedProject: null }, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returned empty string if match is null', () => {
    const segments = breadcrumbSegments(null, testInfo, testHistory, itsURLPath);
    expect(segments).toEqual('');
  });

  test('returns expected segments for model create', () => {
    const path = '/models/create';
    const url = '/models/create';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returns expected segments for model tune', () => {
    const path = '/models/tune';
    const url = '/models/tune';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returns expected segments for model view', () => {
    const path = '/models/view';
    const url = '/models/view';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returns expected segments for project create', () => {
    const path = '/projects/create';
    const url = '/projects/create';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments).toMatchSnapshot();
  });

  test('returned segments with length 1 when user having access to ITS', () => {
    const path = '/';
    const url = '/';
    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments.length).toEqual(1);

    const state = store.getState();
    const userGroup = state.app.userGroups;
    expect(userGroup).toEqual(['IAT_INTERNAL']);
  });

  test('returned segments with length 0 when user having access to ITS', () => {
    const path = '/';
    const url = '/';

    store.dispatch(appActions.updateUserGroups({ MWB_CLIENT_ADMIN: 'MWB_CLIENT_ADMIN' }));
    const state = store.getState();
    const userGroup = state.app.userGroups;
    expect(userGroup).toEqual(['MWB_CLIENT_ADMIN']);

    const segments = breadcrumbSegments(createTestMatch(path, url), testInfo, testHistory, itsURLPath);
    expect(segments.length).toEqual(1);
  });
});
