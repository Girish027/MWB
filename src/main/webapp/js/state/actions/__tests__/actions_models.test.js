jest.mock('utils/amplitudeUtils');
jest.mock('utils/api');

import * as amplitudeUtils from 'utils/amplitudeUtils';
import api from 'utils/api';
import AmplitudeConstants from 'constants/AmplitudeConstants';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import Constants from 'constants/Constants';
import * as actionsModels from 'state/actions/actions_models';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_models', () => {
  let store;
  let model = {
    id: '123',
    projectId: '661',
    configId: '321',
    name: 'newMode',
    description: 'newDescription',
  };
  const {
    id: modelId, name, clientId, projectId,
    description, configId,
  } = model;
  const selecteDatasets = ['1232'];
  let data = {
    userId: 'testuser',
    csrfToken: 'sadsads',
    projectId,
    clientId,
    history: {},
    startBuild: true,
    datasets: ['2323'],
    name,
    description,
    configId,
    selecteDatasets,
    modelTestJobId: '3122',
    utterances: 'book a room',
    testModelType: Constants.DIGITAL_MODEL,
    batchTestName: 'BatchTest',
    speechModelId: '1234',
  };

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
        csrfToken: 'jskjfk22',
      },
      header: {
        client: {
          id: '2',
        },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  describe('onTestDigitalModelSuccess:', () => {
    let modelTestResults = {
      projectId: '661',
      modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
      evaluations: [{
        utterance: 'how are you',
      }],
    };
    const modelType = Constants.DIGITAL_MODEL;
    const { projectId, modelId } = modelTestResults;
    const clientDBId = '2';

    test('should dispatch actions to test digital model and store current results', () => {
      store.dispatch(actionsModels.onTestDigitalModelSuccess(modelTestResults, projectId, clientDBId, modelId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to close modal and display bad request message when results are not available', () => {
      store.dispatch(actionsModels.onTestDigitalModelSuccess());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_COMPLETED_EVENT event', () => {
      store.dispatch(actionsModels.onTestDigitalModelSuccess(modelTestResults, projectId, clientDBId, modelId));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_DIGITAL_MODEL_COMPLETED_EVENT, store.getState(), {
        projectId, clientDBId, modelId, modelType,
      });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });

    test('should call logAmplitudeEvent with DIGITAL_MODEL_TEST_FAILED_EVENT event', () => {
      store.dispatch(actionsModels.onTestDigitalModelSuccess());
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.DIGITAL_MODEL_TEST_FAILED_EVENT, store.getState(), { modelType });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('onTestDigitalModelFailure:', () => {
    let error = {
      message: 'Unable to test digital model',
    };
    const modelType = Constants.DIGITAL_MODEL;

    test('should dispatch actions and display bad request message when test have failed ', () => {
      store.dispatch(actionsModels.onTestDigitalModelFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions and display bad request message when results are not available', () => {
      store.dispatch(actionsModels.onTestDigitalModelFailure());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with DIGITAL_MODEL_TEST_FAILED_EVENT event', () => {
      store.dispatch(actionsModels.onTestDigitalModelFailure(error));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.DIGITAL_MODEL_TEST_FAILED_EVENT, store.getState(), { modelType });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('testDigitalModel:', () => {
    const utterance = 'testing';
    beforeAll(() => {
      data = {
        clientId: '2',
        modelId: 'abcd-efgh-',
        projectId: '3',
        testModelType: Constants.DIGITAL_MODEL,
        utterances: [
          utterance,
        ],
      };
    });
    test('should dispatch initiateSingleUtteranceTest action and clear Model Test Results', () => {
      store.dispatch(actionsModels.testModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsModels.testModel(data));
      expect(api.post).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        data: expect.any(Array),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsModels.testModel(data));
      const url = api.post.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_EVENT event', () => {
      const { projectId, modelId } = data;
      const modelType = Constants.DIGITAL_MODEL;
      store.dispatch(actionsModels.testModel(data));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_DIGITAL_MODEL_EVENT, store.getState(), {
        projectId, clientDBId: store.getState().header.client.id, modelId, modelType,
      });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('onTestSpeechModelSuccess:', () => {
    let modelTestResults = {
      projectId: '661',
      modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
      evaluations: [{
        utterance: 'how are you',
      }],
    };
    const modelType = Constants.DIGITAL_SPEECH_MODEL;
    const { projectId, modelId } = modelTestResults;

    test('should dispatch actions to test digital model and store current results', () => {
      store.dispatch(actionsModels.onTestSpeechModelSuccess(modelTestResults));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to close modal and display bad request message when results are not available', () => {
      store.dispatch(actionsModels.onTestSpeechModelSuccess());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_COMPLETED_EVENT event', () => {
      store.dispatch(actionsModels.onTestSpeechModelSuccess(modelTestResults));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(2);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_SPEECH_MODEL_COMPLETED_EVENT, store.getState(), {
        projectId, clientDBId: store.getState().header.client.id, modelId, modelType,
      });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });

    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_FAILED_EVENT event', () => {
      store.dispatch(actionsModels.onTestSpeechModelSuccess());
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_SPEECH_MODEL_FAILED_EVENT, store.getState(), { modelType });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('onTestSpeechModelFailure:', () => {
    let error = {
      message: 'Unable to transcribe audio',
    };
    const modelType = Constants.DIGITAL_SPEECH_MODEL;

    test('should dispatch actions to close modal and display bad request message when test have failed ', () => {
      store.dispatch(actionsModels.onTestSpeechModelFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to close modal and display bad request message when results are not available', () => {
      store.dispatch(actionsModels.onTestSpeechModelFailure());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_FAILED_EVENT event', () => {
      store.dispatch(actionsModels.onTestSpeechModelFailure(error));
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_SPEECH_MODEL_FAILED_EVENT, store.getState(), { modelType });
      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    });
  });

  describe('submitModel:', () => {
    beforeAll(() => {
      const data = {
        csrfToken: 'sadsads',
        projectId: '1',
        clientId: '27',
        id: '123',
        startBuild: true,
        history: {},
        name: 'testRoot',
        description: 'My description',
        configId: '1',
        selecteDatasets: '1',
        modelType: Constants.DIGITAL_SPEECH_MODEL,
        speechConfigId: '123456',
        isUnbundled: true,
        digitalHostedUrl: 'digital.com/123',
        modelToken: 'ghdfjhr',
        speechModelId: '1234',
      };
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsModels.submitModel(data));
      expect(api.post).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        data: expect.any(Object),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });
  });

  describe('testSpeechModel:', () => {
    beforeAll(() => {
      data = {
        fileName: 'fileName',
        userId: 'abc@247.ai',
        clientId: '2',
        modelId: 'abcd-efgh-',
        projectId: '3',
        fileType: 'upload',
        audioFile: new Blob(),
        audioURL: 'http://...abc.wav',
      };
    });
    test('should dispatch initiateSingleUtteranceTest action and clear Model Test Results', () => {
      store.dispatch(actionsModels.testSpeechModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsModels.testSpeechModel(data));
      expect(api.post).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        data: expect.any(FormData),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsModels.testSpeechModel(data));
      const url = api.post.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });

    //    Need to debug this case and resolved.
    //    test('should call logAmplitudeEvent with TEST_SPEECH_MODEL_EVENT event', () => {
    //      const { projectId, modelId, fileType } = data;
    //      const modelType = Constants.DIGITAL_SPEECH_MODEL;
    //      store.dispatch(actionsModels.testSpeechModel(data));
    //      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
    //      expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.TEST_SPEECH_MODEL_EVENT, store.getState(), {
    //        projectId, clientDBId: store.getState().header.client.id, modelId, modelType, fileType,
    //      });
    //      expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
    //    });
  });

  describe('updateByModel:', () => {
    beforeAll(() => {
      data = {
        clientId: '2',
        model: {
          id: '221',
          projectId: '3',
          description: 'New Description',
        },
      };
    });
    test('should dispatch updateModel action', () => {
      store.dispatch(actionsModels.updateByModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch updateModel action without data', () => {
      const dataDes = {
        clientId: '2',
        model: {
          id: '221',
          projectId: '3',
        },
      };
      store.dispatch(actionsModels.updateByModel(dataDes));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should do patch call with required data', () => {
      store.dispatch(actionsModels.updateByModel(data));
      expect(api.patch).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        headers: expect.any(Object),
        data: expect.any(Array),
        onApiSuccess: expect.any(Function),
        onApiError: actionsModels.onUpdateModelFailure,
      });
    });

    test('should do patch call with required data', () => {
      store.dispatch(actionsModels.updateByModel(data));
      const url = api.patch.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('createNewModel:', () => {
    test('should dispatch actions call createNewModel', () => {
      store.dispatch(actionsModels.createNewModel(projectId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onUpdateModelSuccess:', () => {
    const modelUpdateResults = {
      projectId: '661',
      modelId: 'b61693f6-fa65-4308-af09-b32872dfb5f6',
      id: '123',
      description: 'New Description',
    };
    const { projectId, id } = modelUpdateResults;

    test('should dispatch actions display good request message when model is updated', () => {
      store.dispatch(actionsModels.onUpdateModelSuccess(modelUpdateResults, projectId, id));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions display good request message when no message is not available', () => {
      store.dispatch(actionsModels.onUpdateModelSuccess(undefined, projectId, id));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onTestSubmitModelSuccess:', () => {
    const data = {
      clientId: '1',
      speechModelId: '1234',
      id: '123',
      history: [],
      startBuild: true,
    };

    const newModel = {
      id: '123',
      status: 'NULL',
      created: 'undefined',
      createdAt: '20200630',
      modelType: Constants.DIGITAL_MODEL,
      version: '111',
      projectId: '2',
      datasetIds: ['1'],
    };

    test('should dispatch onTestSubmitModelSuccess when model is submitted', () => {
      store.dispatch(actionsModels.onTestSubmitModelSuccess(data, newModel));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onTestSubmitModelFailure:', () => {
    let modelTestResults = {
      clientId: '11',
      speechModelId: '1234',
      id: '123',
      history: [],
      startBuild: 'true',
      datasetIds: '12',
    };
    let error = {
      message: 'Model version already exists.',
      status: '409',
    };

    test('should dispatch actions to display bad request message when submit has failed', () => {
      store.dispatch(actionsModels.onTestSubmitModelFailure(modelTestResults, error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to display bad request message when no message is available', () => {
      let error = {
        message: 'Error',
        status: '400',
      };
      store.dispatch(actionsModels.onTestSubmitModelFailure(modelTestResults, error));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onUpdateModelFailure:', () => {
    let error = {
      message: 'Unable to update the model',
    };

    test('should dispatch actions to display bad request message when update have failed ', () => {
      store.dispatch(actionsModels.onUpdateModelFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to display bad request message when no message is available ', () => {
      store.dispatch(actionsModels.onUpdateModelFailure());
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('deleteModelFromProject:', () => {
    beforeAll(() => {
      data = {
        clientId: '2',
        projectId: '3',
        modelId: '221',
      };
    });
    test('should dispatch deleteModel action', () => {
      store.dispatch(actionsModels.deleteModelFromProject(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should do delete call with required data', () => {
      store.dispatch(actionsModels.deleteModelFromProject(data));
      expect(api.delete).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        onApiSuccess: expect.any(Function),
        onApiError: actionsModels.onFetchFailureDeleteModel,
      });
    });

    test('should do delete call with required data', () => {
      store.dispatch(actionsModels.deleteModelFromProject(data));
      const url = api.delete.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onFetchSuccessDeleteModel:', () => {
    const data = {
      clientId: '1',
      projectId: '661',
      modelId: '123',
    };

    test('should dispatch modelDeletedFromProject actions display good request message when model is deleted', () => {
      store.dispatch(actionsModels.onFetchSuccessDeleteModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onFetchFailureDeleteModel:', () => {
    test('should dispatch modelDeleteFailed actions to display bad request message when delete have failed ', () => {
      let error = {
        message: 'Unable to update the model',
        code: 400,
      };
      store.dispatch(actionsModels.onFetchFailureDeleteModel(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch modelAlreadyDeletedFromProject actions to display bad request message when delete have failed ', () => {
      let error = {
        message: 'Unable to update the model',
        code: 500,
      };
      store.dispatch(actionsModels.onFetchFailureDeleteModel(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch modelAlreadyDeletedFromProject actions to display bad request message when no result is available', () => {
      store.dispatch(actionsModels.onFetchFailureDeleteModel());
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('<Function call />', () => {
    test('should call viewSelectedModel ', () => {
      store.dispatch(actionsModels.viewSelectedModel(model));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call createSpeechModel', () => {
      store.dispatch(actionsModels.createSpeechModel(model, ['1232']));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call startModelBuild', () => {
      store.dispatch(actionsModels.startModelBuild({ projectId, modelId }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call submitModel', () => {
      store.dispatch(actionsModels.submitModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call testModel', () => {
      store.dispatch(actionsModels.testModel(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call listBatchTests', () => {
      store.dispatch(actionsModels.listBatchTests(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call modelBatchTest', () => {
      store.dispatch(actionsModels.modelBatchTest(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call modelCheckBatchTest', () => {
      store.dispatch(actionsModels.modelCheckBatchTest(data));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call recieveModelTestResults', () => {
      store.dispatch(actionsModels.recieveModelTestResults({ modelTestResults: [] }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call modelCreated', () => {
      store.dispatch(actionsModels.modelCreated({ model }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call tuneModel', () => {
      store.dispatch(actionsModels.tuneModel('123'));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call clearViewModel', () => {
      store.dispatch(actionsModels.clearViewModel());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call showModelNavigationConfirmationDialog', () => {
      store.dispatch(actionsModels.showModelNavigationConfirmationDialog({}));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call updateSpeechModelIdForDigitalModel', () => {
      store.dispatch(actionsModels.updateSpeechModelIdForDigitalModel('12', '4', '13'));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call modelBatchTestResults', () => {
      store.dispatch(actionsModels.modelBatchTestResults({}));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call modelBatchJobRequest', () => {
      store.dispatch(actionsModels.modelBatchJobRequest({ modelBatchJobRequest: '' }));
      expect(store.getActions()).toMatchSnapshot();
    });
  });
});
