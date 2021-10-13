import goToRoute, { RouteNames } from 'utils/routeHelpers';

describe('routeHelpers', () => {
  let testHistory;
  let mockPush;

  beforeEach(() => {
    mockPush = jest.fn();

    testHistory = {
      push: mockPush,
    };
  });

  const projectId = '14';
  const modelId = '7';
  const client = {
    id: '2',
    itsAppId: 'default',
    itsClientId: '247ai',
    standardClientName: 'tfsai',
    name: '247 ai',
  };

  const paramData = {
    client,
    projectId,
    modelId,
  };

  test('returns expected route for selected route name', () => {
    const expectedRoute = `/projects?clientid=${client.standardClientName}&appid=${client.itsAppId}&projectid=${projectId}`;
    goToRoute(RouteNames.PROJECTID, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
    expect(mockPush.mock.calls[0][0]).toBe(expectedRoute);
  });

  test('history is called for PROJECTS', () => {
    goToRoute(RouteNames.PROJECTS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for TAG_DATASETS', () => {
    goToRoute(RouteNames.TAG_DATASETS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for MODELS', () => {
    goToRoute(RouteNames.MODELS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for CREATEMODEL', () => {
    goToRoute(RouteNames.CREATEMODEL, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for TUNEMODEL', () => {
    goToRoute(RouteNames.TUNEMODEL, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for DATASETS', () => {
    goToRoute(RouteNames.DATASETS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for TESTMODEL', () => {
    goToRoute(RouteNames.TESTMODEL, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for REPORTS', () => {
    goToRoute(RouteNames.REPORTS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for UPDATEPROJECT', () => {
    goToRoute(RouteNames.UPDATEPROJECT, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for READPROJECT', () => {
    goToRoute(RouteNames.READPROJECT, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for MANAGE_INTENTS', () => {
    goToRoute(RouteNames.MANAGE_INTENTS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for SETTINGS PAGE', () => {
    goToRoute(RouteNames.SETTINGS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
  test('history is called for SETTINGS TAB', () => {
    goToRoute(RouteNames.MANAGE_SETTINGS, paramData, testHistory);
    expect(mockPush.mock.calls.length).toBe(1);
  });
});
