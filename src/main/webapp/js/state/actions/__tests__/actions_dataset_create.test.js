jest.mock('utils/api');

import api from 'utils/api';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import * as datasetCreateActions from 'state/actions/actions_dataset_create';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_dataset_create', () => {
  let store;
  let file = new Blob([
    JSON.stringify({ name: 'dropzone' }),
  ], { type: 'application/json' });
  let importData = {
    dropZone: file[0],
    name: 'dataset',
    description: 'description',
    projectId: '123',
    clientId: '14',
    locale: 'en-US',
    dataType: 'Text',
  };
  let props = {
    handleSuccess: jest.fn(),
    handleError: jest.fn(),
  };
  let data = {
    token: 'token',
    columns: [
      {
        id: '1',
        name: 'transcription',
        required: true,
        displayName: 'Transcription',
      },
      {
        id: '3',
        name: 'filename',
        required: false,
        displayName: 'Filename',
      },
      {
        id: '5',
        name: 'inheritedIntent',
        required: false,
        displayName: 'Granular Intent',
      },
      {
        id: '7',
        name: 'rutag',
        required: false,
        displayName: 'Rollup Intent',
      },
      {
        id: '9',
        name: 'filesize',
        required: false,
        displayName: 'Filesize',
      },
      {
        id: '11',
        name: 'date',
        required: false,
        displayName: 'Date',
      },
      {
        id: '13',
        name: 'dataset',
        required: false,
        displayName: 'Dataset',
      },
    ],
    previewData: [
      [
        'Transcription',
        'Rollup Intent',
      ],
      [
        'What are store hours',
        'Store_Hours',
      ],
      [
        'store hours',
        'Store_Hours',
      ],
      [
        'store opening time',
        'Store_Hours',
      ],
      [
        'store closing time',
        'Store_Hours',
      ],
      [
        'store times',
        'Store_Hours',
      ],
      [
        'When does this shop open',
        'Store_Hours',
      ],
      [
        'store timings',
        'Store_Hours',
      ],
      [
        'What are store timings',
        'Store_Hours',
      ],
      [
        'Tell me the store hours',
        'Store_Hours',
      ],
      [
        'I would like to change password',
        'Password_Change',
      ],
    ],
  };

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
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
      createDatasetDialog: {
        step: 'upload',
        columns: data.columns,
        previewData: data.previewData,
        bindingArray: [],
        columnsBinding: {},
        isPreSelected: false,
        isBindingValid: false,
        skipFirstRow: true,
        isMappingRequestLoading: false,
        isCommitRequestLoading: false,
        token: '8182f040-8c4c-40c5-b864-3ffb2d71d330',
        fileId: null,
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  describe('onDatasetImportSuccess', () => {
    test('should dispatch onDatasetImportSuccess action with data', () => {
      store.dispatch(datasetCreateActions.onDatasetImportSuccess(data, {}));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onDatasetImportFailure', () => {
    test('should dispatch onDatasetImportFailure action', () => {
      store.dispatch(datasetCreateActions.onDatasetImportFailure({}));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('importFile', () => {
    test('should dispatch importFile action', () => {
      store.dispatch(datasetCreateActions.importFile(importData));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should make a GET call to get the config properties', () => {
      store.dispatch(datasetCreateActions.importFile(importData));
      expect(api.post).toHaveBeenCalledTimes(1);
      expect(api.post.mock.calls[0][0].url).toMatchSnapshot();
    });

    test('should dispatch fileUpload action', () => {
      store.dispatch(datasetCreateActions.fileUpload(importData.dropZone, props.handleSuccess, props.handleError));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch columnBinding action', () => {
      store.dispatch(datasetCreateActions.columnBinding(importData));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch createDataset action', () => {
      const {
        projectId, clientId, dataType,
        locale, name, description,
      } = importData;
      const uri = 'https://tagging.247-inc.com:8443/nltools/private/v1/files';
      store.dispatch(datasetCreateActions.createDataset({
        projectId,
        clientId,
        dataType,
        locale,
        name,
        uri,
        description,
        autoTagDataset: true,
        mapToProjectId: true,
      }));
      expect(store.getActions()).toMatchSnapshot();
    });
  });
});
