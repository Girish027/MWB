import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import * as datasetActions from 'state/actions/actions_datasets';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_datasets', () => {
  let store;
  let props = {
    projectId: '123',
    clientId: '14',
    datasetId: '121',
    dataset: {
      id: '121',
      name: 'dataset',
    },
  };
  const userId = 'testUser';
  const csrfToken = 'jskjfk22';
  const {
    projectId, clientId, datasetId, dataset,
  } = props;

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
        csrfToken: 'jskjfk22',
      },
      header: {
        client: {
          id: '14',
        },
      },
      projectListSidebar: {
        projectById: {
          123: {
            name: 'OldName',
          },
        },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  describe('removeDatasetFromProject', () => {
    test('should dispatch removeDatasetFromProject action with data', () => {
      store.dispatch(datasetActions.removeDatasetFromProject({ projectId, clientId, datasetId }));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('addDatasetToProject', () => {
    test('should dispatch addDatasetToProject action', () => {
      store.dispatch(datasetActions.addDatasetToProject({
        projectId, datasetId, dataset, autoTag: true,
      }));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('fetchCancelTransformJob', () => {
    test('should dispatch fetchCancelTransformJob action', () => {
      store.dispatch(datasetActions.fetchCancelTransformJob(userId, datasetId, projectId, csrfToken, clientId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('fetchDeleteTransformJob', () => {
    test('should dispatch fetchDeleteTransformJob action', () => {
      store.dispatch(datasetActions.fetchDeleteTransformJob(userId, datasetId, projectId, csrfToken, clientId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('fetchDatasetTransform', () => {
    test('should dispatch fetchDatasetTransform action', () => {
      store.dispatch(datasetActions.fetchDatasetTransform(userId, datasetId, clientId, projectId, csrfToken, true));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('fetchDataset', () => {
    test('should dispatch fetchDataset action', () => {
      store.dispatch(datasetActions.fetchDataset(datasetId, 0));
      expect(store.getActions()).toMatchSnapshot();
    });
  });
});
