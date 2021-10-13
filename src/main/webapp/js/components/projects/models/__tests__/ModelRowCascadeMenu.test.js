jest.mock('utils/amplitudeUtils');

import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import ConnectedModelRowCascadeMenu, { ModelRowCascadeMenu } from 'components/projects/models/ModelRowCascadeMenu';
import Constants from 'constants/Constants';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsModels from 'state/actions/actions_models';
import { RouteNames } from 'utils/routeHelpers';
import { pathKey } from 'utils/apiUrls';
import constructKibanaUrl from 'utils/kibanaUtils';
import * as amplitudeUtils from 'utils/amplitudeUtils';
import AmplitudeConstants from 'constants/AmplitudeConstants';

const middlewares = [];
const mockStore = configureStore(middlewares);

// Initialize mockstore with empty state
const initialState = {
  header: {
    client: {
      id: '5',
    },
  },
  app: {
    kibanaLogIndex: 'kibana_index',
    kibanaLogURL: 'kibana url',
    userFeatureConfiguration: {},
  },
};
const store = mockStore(initialState);

describe('<ModelRowCascadeMenu />', () => {
  const createDataset = (id, name, createdAt, status = 'COMPLETED') => ({
    _key: id,
    id,
    clientId: 1,
    projectId: 2,
    name,
    type: 'Audio/Voice (Data Collection)',
    description: 'test dataset',
    locale: 'en_US',
    createdAt,
    status,
    task: 'INDEX',
  });

  const model = {
    _key: 1,
    id: 12,
    modelToken: '15e17bdf-f38e-42f3-a8f0-7b595dd3df40',
    projectId: '2',
    name: 'Test Model',
    description: 'Test Description',
    version: 5,
    modelType: Constants.DIGITAL_MODEL,
    created: 1536777405845,
    updated: 1536777405845,
    datasetIds: ['1', '2'],
    configId: '12',
    userId: 'mwbuser@247.ai',
    status: Constants.STATUS.COMPLETED,
  };

  const newModel = {
    _key: 1,
    id: 13,
    modelToken: '14e17bdf-f38e-42f3-a8f0-7b595dd3df40',
    projectId: 2,
    name: 'New Test Model',
    description: 'Test Description',
    version: 5,
    modelType: Constants.DIGITAL_MODEL,
    created: new Date(),
    updated: new Date(),
    datasetIds: ['1', '2'],
    configId: 12,
    userId: 'mwbuser@247.ai',
    status: Constants.STATUS.COMPLETED,
  };

  let wrapper;
  let props = {};
  let datasets;
  const { featureFlags } = global.uiConfig;

  beforeAll(() => {
    datasets = new Map();
    datasets.set(1, createDataset(1, 'dataset 1', 1536777405845));
    datasets.set(2, createDataset(2, 'dataset 2', 1536777805845));
    props = {
      clientId: '12',
      projectId: '6',
      configId: '7',
      modelId: '10',
      modelType: Constants.DIGITAL_MODEL,
      kibanaLogIndex: 'kibana_index',
      kibanaLogURL: 'kibana url',
      userFeatureConfiguration: featureFlags.DEFAULT,
      datasets,
      model,
      dispatch: () => {},
      history: {},
      state: initialState,
    };
  });

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Connected ModelRowCascadeMenu', () => {
    beforeAll(() => {
      const testProps = {
        projectId: '6',
        configId: '7',
        modelId: '10',
      };
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ConnectedModelRowCascadeMenu
            projectId={testProps.projectId}
            modelId={testProps.modelId}
            {...testProps}
            modelType={Constants.DIGITAL_MODEL}
          />
        </MemoryRouter>
      </Provider>);
    });

    it('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    it('should contain ModelRowCascadeMenu', () => {
      expect(wrapper.find('ModelRowCascadeMenu').length).toEqual(1);
    });
  });

  describe('Snapshots', () => {
    Object.keys(Constants.STATUS).forEach((status) => {
      test(`renders correctly for user with DEFAULT permissions when status is ${status}`, () => {
        const testModel = {
          ...model,
          status,
        };
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          model={testModel}
        />);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test(`renders correctly for user with MWB_ROLE_EXTERNAL permissions when status is ${status}`, () => {
        const testModel = {
          ...model,
          status,
        };
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          model={testModel}
          userFeatureConfiguration={{
            ...featureFlags.DEFAULT,
            ...featureFlags.MWB_ROLE_EXTERNAL,
          }}
        />);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    test('renders correctly for Digital Model - should contain Add Speech', () => {
      wrapper = shallow(<ModelRowCascadeMenu
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for Digital + Speech Model - should not contain Add Speech', () => {
      const testModel = {
        ...model,
        modelType: Constants.DIGITAL_SPEECH_MODEL,
      };
      wrapper = shallow(<ModelRowCascadeMenu
        {...props}
        model={testModel}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      actionsApp.modalDialogChange = jest.fn()
        .mockImplementation(() => ({ called: 'modalDialogChange' }));
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('changeActiveItems:', () => {
      beforeEach(() => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should mark the current item in focus/hover and render correctly', () => {
        wrapper.instance().changeActiveItems(Constants.MODEL_ACTION_MENU.TEST);
        expect(wrapper.instance().activeItems).toEqual({
          [Constants.MODEL_ACTION_MENU.TEST]: true,
        });
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onMouseLeave:', () => {
      beforeEach(() => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
        wrapper.instance().changeActiveItems(Constants.MODEL_ACTION_MENU.TEST);
        expect(wrapper.instance().activeItems).toEqual({
          [Constants.MODEL_ACTION_MENU.TEST]: true,
        });
      });

      test('should remove focus on all items and render correctly', () => {
        wrapper.instance().onMouseLeave();
        expect(wrapper.instance().activeItems).toEqual({});
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickBuildModel:', () => {
      beforeEach(() => {
        actionsModels.startModelBuild = jest.fn()
          .mockImplementation(() => ({ called: 'startModelBuild' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should dispatch startModelBuild action', () => {
        const { projectId, modelId } = props;
        wrapper.instance().onClickBuildModel();
        expect(actionsModels.startModelBuild).toHaveBeenCalledWith({ projectId, modelId });
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'startModelBuild' });
      });
    });

    describe('onClickTestModel:', () => {
      beforeEach(() => {
        actionsModels.clearListOfBatchTests = jest.fn()
          .mockImplementation(() => ({ called: 'clearListOfBatchTests' }));
        actionsApp.changeRoute = jest.fn()
          .mockImplementation(() => ({ called: 'changeRoute' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should dispatch clearListOfBatchTests action', () => {
        wrapper.instance().onClickTestModel();
        expect(actionsModels.clearListOfBatchTests).toHaveBeenCalled();
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'clearListOfBatchTests' });
      });

      test('should dispatch changeRoute with correct route and params', () => {
        const {
          clientId, projectId, modelId, history,
        } = props;
        wrapper.instance().onClickTestModel();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.TESTMODEL, { clientId, projectId, modelId }, history);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'changeRoute' });
      });
    });

    describe('Download handlers:', () => {
      beforeEach(() => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
        wrapper.instance().downloadFile = jest.fn();
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(() => ({ called: 'modalDialogChange' }));
      });

      test('onClickDownload: should download model file', () => {
        wrapper.instance().onClickDownload();
        expect(wrapper.instance().downloadFile).toHaveBeenCalledWith(pathKey.modelDownload);
      });

      test('onClickDownload: should call logAmplitudeEvent with DOWNLOAD_MODEL_EVENT event', () => {
        const {
          projectId, modelId, clientId,
        } = props;
        const { modelType } = model;
        wrapper.instance().onClickDownload();
        expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledTimes(1);
        expect(amplitudeUtils.logAmplitudeEvent).toHaveBeenCalledWith(AmplitudeConstants.DOWNLOAD_MODEL_EVENT, store.getState(), {
          projectId, clientDBId: clientId, modelDBId: modelId, modelType,
        });
        expect(amplitudeUtils.logAmplitudeEvent).toHaveReturnedWith('called logAmplitudeEvent');
      });

      test('onClickStatistics: should download accuracy report', () => {
        wrapper.instance().onClickStatistics();
        expect(wrapper.instance().downloadFile).toHaveBeenCalledWith(pathKey.modelStatistics);
      });

      test('onClickTrainingOutputs: should open modal dialog ', () => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          {...model}
        />);

        wrapper.instance().onClickTrainingOutputs();
        expect(actionsApp.modalDialogChange).toHaveBeenCalled();
      });

      test('onClickTrainingOutputs: should download training outputs ', () => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          model={newModel}
        />);

        wrapper.instance().downloadFile = jest.fn();
        wrapper.instance().onClickTrainingOutputs();
        expect(actionsApp.modalDialogChange).not.toHaveBeenCalled();
        expect(wrapper.instance().downloadFile).toHaveBeenCalledWith(pathKey.modelTrainingOutputs);
      });

      test('onClickConfiguration: should download configuration file', () => {
        wrapper.instance().onClickConfiguration();
        expect(wrapper.instance().downloadFile).toHaveBeenCalledWith(pathKey.modelConfigDownload);
      });
    });

    describe('onClickTuneModel:', () => {
      beforeEach(() => {
        actionsModels.tuneSelectedModel = jest.fn()
          .mockImplementation(() => ({ called: 'tuneSelectedModel' }));
        actionsApp.changeRoute = jest.fn()
          .mockImplementation(() => ({ called: 'changeRoute' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should dispatch tuneSelectedModel action', () => {
        wrapper.instance().onClickTuneModel();
        expect(actionsModels.tuneSelectedModel).toHaveBeenCalledWith(props.model);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'tuneSelectedModel' });
      });

      test('should dispatch changeRoute with correct route and params', () => {
        const { clientId, projectId, history } = props;
        wrapper.instance().onClickTuneModel();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.TUNEMODEL, { clientId, projectId }, history);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'changeRoute' });
      });
    });

    describe('onClickViewModel:', () => {
      beforeEach(() => {
        actionsModels.viewSelectedModel = jest.fn()
          .mockImplementation(() => ({ called: 'viewSelectedModel' }));
        actionsApp.changeRoute = jest.fn()
          .mockImplementation(() => ({ called: 'changeRoute' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should dispatch viewSelectedModel action', () => {
        wrapper.instance().onClickViewModel();
        expect(actionsModels.viewSelectedModel).toHaveBeenCalledWith(props.model);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'viewSelectedModel' });
      });

      test('should dispatch changeRoute with correct route and params', () => {
        const { clientId, projectId, history } = props;
        wrapper.instance().onClickViewModel();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.VIEWMODEL, { clientId, projectId }, history);
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'changeRoute' });
      });
    });

    describe('onClickAddSpeech:', () => {
      beforeEach(() => {
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(() => ({ called: 'modalDialogChange' }));
        actionsApp.changeRoute = jest.fn()
          .mockImplementation(() => ({ called: 'changeRoute' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should dispatch onClickAddSpeech action', () => {
        const {
          dispatch,
          userFeatureConfiguration,
          datasets,
        } = props;
        wrapper.instance().onClickAddSpeech();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
          type: 'SpeechSelectDatasetDialog',
          datasets,
          dispatch,
          model,
          userFeatureConfiguration,
        });
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'modalDialogChange' });
      });
    });

    describe('onClickViewDigitalUrl:', () => {
      beforeEach(() => {
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(() => ({ called: 'modalDialogChange' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          model
        />);
      });

      test('should dispatch onClickViewDigitalUrl action', () => {
        wrapper.instance().onClickViewDigitalUrl();
        expect(actionsApp.modalDialogChange).toHaveBeenCalled();
        expect(props.dispatch).toHaveBeenCalledWith({ called: 'modalDialogChange' });
      });
    });


    describe('onClickDeleteModel:', () => {
      beforeEach(() => {
        actionsApp.modalDialogChange = jest.fn()
          .mockImplementation(() => ({ called: 'modalDialogChange' }));
        actionsModels.deleteModelFromProject = jest.fn()
          .mockImplementation(() => ({ called: 'deleteModelFromProject' }));
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('renders correctly for Digital Model - should contain Delete action', () => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('renders correctly for Digital + Speech Model - should not contain Delete action', () => {
        const testModel = {
          ...model,
          modelType: Constants.DIGITAL_SPEECH_MODEL,
        };
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
          model={testModel}
        />);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should dispatch onClickDelete action', () => {
        const {
          dispatch, modelId, projectId, clientId,
        } = props;

        wrapper.instance().onClickDeleteModel();
        expect(actionsApp.modalDialogChange).toHaveBeenCalled();

        dispatch(actionsModels.deleteModelFromProject(clientId, projectId, modelId));
        expect(actionsModels.deleteModelFromProject).toHaveBeenCalledWith(clientId, projectId, modelId);
        expect(dispatch).toHaveBeenCalledWith({ called: 'deleteModelFromProject' });
      });
    });

    describe('onClickViewLogs:', () => {
      let windowFocus = jest.fn();

      beforeEach(() => {
        wrapper = shallow(<ModelRowCascadeMenu
          {...props}
        />);
      });

      test('should create kibana url and open in new tab', () => {
        const expectedKibanaUrl = constructKibanaUrl({
          kibanaLogIndex: props.kibanaLogIndex,
          kibanaLogURL: props.kibanaLogURL,
          modelToken: props.model.modelToken,
        });
        wrapper.instance().onClickViewLogs();
        expect(global.window.open).toHaveBeenCalledWith(expectedKibanaUrl, '_blank');
      });

      test('should create kibana url and open in new tab', () => {
        wrapper.instance().onClickViewLogs();
        expect(global.windowOpenFocus).toHaveBeenCalled();
      });
    });
  });
});
