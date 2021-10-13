import React from 'react';
import { shallow, mount } from 'enzyme';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import toJSON from 'enzyme-to-json';
import * as actionsModels from 'state/actions/actions_models';
import * as projectActions from 'state/actions/actions_projects';
import * as appActions from 'state/actions/actions_app';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import Model from 'model';
import Constants from 'constants/Constants';
import { ReadProjectModelsGrid } from 'components/projects/models/ReadProjectModelsGrid';
import ConnectedReadProjectModelsGrid from 'components/projects/models/ReadProjectModelsGrid';

describe('<ReadProjectModelsGrid />', () => {
  const match = {
    params: {
      projectId: '7',
      modelId: '3',
    },
  };

  const data = [{
    description: 't1',
    id: '3',
    modelType: 'DIGITAL',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe49',
    name: 'table',
    projectId: '7',
    status: 'COMPLETE',
    userId: 'xyz@247.ai',
    version: 1,
    configId: '1113',
    created: 1569593066484,
  }, {
    description: 't2',
    id: '4',
    modelType: 'DIGITAL',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe50',
    name: 'table',
    projectId: '7',
    status: 'FAILED',
    userId: 'xyz@247.ai',
    version: 2,
    configId: '1114',
  }, {
    description: 't3',
    id: '5',
    modelType: 'DIGITAL',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe51',
    name: 'table',
    projectId: '7',
    status: 'RUNNING',
    userId: 'xyz@247.ai',
    version: 3,
    configId: '1115',
  }];

  const props = {
    row: '1',
    column: '2',
    status: 'COMPLETED',
    reset: true,
    modelId: '101',
    isDeployedEnabled: true,
    version: '1',
    clientId: '002',
    data,
    project: {
      clientId: '5',
      created: 1573027214432,
      deployableModelId: 3,
      description: 'Sample Project for Status Badge',
      id: '7',
      locale: 'en-US',
      name: 'Sample Project for Status Badge',
      vertical: 'FINANCIAL',
      previewModelId: '101',
      liveModelId: '105',
    },
    roles: ['admin'],
    dispatch: () => {},
  };

  const propsWithEmptyProject = {
    ...props,
    project: undefined,
  };


  const statusComplete = [{
    description: 't1',
    id: '3',
    modelType: 'DIGITAL',
    name: 'table',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe49',
    projectId: '7',
    status: 'COMPLETE',
    userId: 'xyz@247.ai',
    version: 1,
    configId: '1113',
    created: 1569593066484,
  }];

  const statusFailed = [{
    description: 't2',
    id: '4',
    modelType: 'DIGITAL',
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe50',
    name: 'table',
    datasetIds: ['2477'],
    projectId: '7',
    status: 'FAILED',
    userId: 'xyz@247.ai',
    version: 2,
    configId: '1114',
  }];

  const statusRunning = [{
    description: 't3',
    id: '5',
    modelType: 'DIGITAL',
    name: 'table',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe51',
    projectId: '7',
    status: 'RUNNING',
    userId: 'xyz@247.ai',
    version: 3,
    configId: '1115',
  }];

  const statusPreview = [{
    description: 't4',
    id: '8',
    modelType: 'DIGITAL',
    name: 'table',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe52',
    projectId: '7',
    status: 'PREVIEW',
    userId: 'xyz@247.ai',
    version: 4,
    configId: '1116',
  }];

  const statusLive = [{
    description: 't5',
    id: '9',
    modelType: 'DIGITAL',
    name: 'table',
    datasetIds: ['2477'],
    modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe53',
    projectId: '7',
    status: 'LIVE',
    userId: 'xyz@247.ai',
    version: 5,
    configId: '1117',
  }];

  const testProps = {
    clientId: '002',
    projectId: '7',
    project: {
      clientId: '5',
      created: 1573027214432,
      deployableModelId: 4,
      description: 'Sample Project for Status Badge',
      id: '7',
      locale: 'en-US',
      name: 'Sample Project for Status Badge',
      vertical: 'FINANCIAL',
      previewModelId: '2',
      liveModelId: '3',
    },
    userFeatureConfiguration: {
      appHelp: 'show',
      kibanaLogs: 'show',
      modelDownload: 'show',
      modelTrainingOutputs: 'show',
    },
    dispatch: () => { },
    history: {},
    roles: ['admin'],
    filtered: { id: 'userId', value: 'abc.xyz@247.ai' },
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<ReadProjectModelsGrid
    {...propsObj}
  />);

  const getMountedWrapper = () => mount(
    <Provider store={store}>
      <MemoryRouter
        initialEntries={['/']}
        initialIndex={0}
      >
        <ConnectedReadProjectModelsGrid
          match={match}
          location={{
            search: '?clientid=247ai&appid=aisha&projectid=7',
          }}
          {...testProps}
          data={statusComplete}
        />
      </MemoryRouter>
    </Provider>,
  );

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = getMountedWrapper();
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly when status complete', () => {
      wrapper = shallow(<ReadProjectModelsGrid
        {...testProps}
        data={statusComplete}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '5',
          },
        },
        app: {
          kibanaLogIndex: 'AAAAAAAAAAA',
          kibanaLogURL: 'abc.com',
        },
        userFeatureConfiguration: {},
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when status failed', () => {
      wrapper = shallow(<ReadProjectModelsGrid
        {...testProps}
        data={statusFailed}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '5',
          },
        },
        app: {
          kibanaLogIndex: 'AAAAAAAAAAA',
          kibanaLogURL: 'abc.com',
        },
        userFeatureConfiguration: {},
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when status running', () => {
      wrapper = shallow(<ReadProjectModelsGrid
        {...testProps}
        data={statusRunning}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '5',
          },
        },
        app: {
          kibanaLogIndex: 'AAAAAAAAAAA',
          kibanaLogURL: 'abc.com',
        },
        userFeatureConfiguration: {},
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when status preview', () => {
      wrapper = shallow(<ReadProjectModelsGrid
        {...testProps}
        data={statusPreview}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '105',
          },
        },
        app: {
          kibanaLogIndex: 'AAAAAAAAAAA',
          kibanaLogURL: 'test.com',
        },
        userFeatureConfiguration: {},
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when status live', () => {
      wrapper = shallow(<ReadProjectModelsGrid
        {...testProps}
        data={statusLive}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '106',
          },
        },
        app: {
          kibanaLogIndex: 'BBBBBBBBB',
          kibanaLogURL: 'abc.com',
        },
        userFeatureConfiguration: {},
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      appActions.displayGoodRequestMessage = jest.fn(() => 'displayGoodRequestMessage');
      actionsModels.viewSelectedModel = jest.fn(() => 'viewSelectedModel');
      actionsModels.updateByModel = jest.fn(() => 'updateModel');
      actionsCellEditable.stateRemove = jest.fn(() => 'stateRemove');
      projectActions.markDeployableModel = jest.fn(() => 'markDeployableModel');
      Model.ProjectsManager.getDataset = jest.fn(() => { 'sample dataset'; });
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('handleDeployableModel', () => {
      test('should mark the model for deployment', () => {
        wrapper = getShallowWrapper(testProps);
        const model = { version: '1' };
        const modelId = '1';
        const clientId = testProps.clientId;
        const projectId = testProps.projectId;

        wrapper.instance().handleDeployableModel(model, modelId);
        expect(projectActions.markDeployableModel).toHaveBeenCalledWith({
          clientId, modelId, projectId, model,
        });
      });
    });

    describe('onClickModelVersion', () => {
      test('should call an action to view the model', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().onClickModelVersion(statusComplete);
        expect(actionsModels.viewSelectedModel).toHaveBeenCalledWith(statusComplete);
        expect(props.dispatch).toHaveBeenCalledWith('viewSelectedModel');
      });
    });

    describe('componentWillUnmount', () => {
      test('should call componentWillUnmount', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().componentWillUnmount();
        expect(actionsCellEditable.stateRemove).toHaveBeenCalledWith({ stateKey: 'ModelDescription' });
        expect(props.dispatch).toHaveBeenCalledWith('stateRemove');
      });
    });

    describe('<Function call />', () => {
      const newValue = 'New Description';
      const cellInfo = {
        value: {
          description: 't1',
          id: '3',
          modelType: 'DIGITAL',
          datasetIds: ['2477'],
          modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe49',
          name: 'table',
          projectId: '7',
          userId: 'xyz@247.ai',
          version: 1,
          configId: '1113',
          created: 1569593066484,
        },
        original: {
          description: 't1',
          id: '3',
          modelType: 'DIGITAL',
          datasetIds: ['2477'],
          modelToken: 'cfe27b7c-ef7a-4e8b-acc9-0bec3aa9fe49',
          name: 'table',
          projectId: '7',
          status: 'COMPLETE',
          statusMessage: 'Process',
          userId: 'xyz@247.ai',
          version: 1,
          configId: '1113',
          created: 1569593066484,
        },
        index: 0,
        column: {
          id: '1',
        },
        row: {
          id: '1',
        },
      };
      const model = Object.assign({}, cellInfo.original, { description: newValue });

      test('should call onChange', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChange(newValue, cellInfo);
        expect(actionsModels.updateByModel).toHaveBeenCalledWith({ clientId: props.clientId, model });
        expect(props.dispatch).toHaveBeenCalledWith('updateModel');
      });

      test('should call getEditorMultiline', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().getEditorMultiline(cellInfo);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderDescription', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderDescription(cellInfo);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderModelId', () => {
        const value = { modelToken: '12345' };
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderModelId(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call getStatusBadge', () => {
        const value = 'COMPLETED';
        wrapper = getShallowWrapper(props);
        wrapper.instance().getStatusBadge(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call updateStatusForCell', () => {
        const {
          row, column, status, reset,
        } = props;
        wrapper = getShallowWrapper(props);
        wrapper.instance().updateStatusForCell(row, column, status, reset);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call handleCellValueChange', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().handleCellValueChange(newValue, cellInfo);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderStatus', () => {
        const value = 'COMPLETED';
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderStatus(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderStatus with Empty project', () => {
        const value = 'COMPLETED';
        wrapper = getShallowWrapper(propsWithEmptyProject);
        wrapper.instance().renderStatus(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderStatus correctly for PREVIEW status', () => {
        const value = { id: '101', status: 'PREVIEW' };
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderStatus(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderStatus correctly for LIVE status', () => {
        const value = { id: '105', status: 'LIVE' };
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderStatus(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call renderStar', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().renderStar(cellInfo.original);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      test('should call onClickMarkForDeploy', () => {
        const { modelId, isDeployedEnabled } = props;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onClickMarkForDeploy(modelId, isDeployedEnabled);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });

      Object.keys(Constants.STATUS).forEach((status) => {
        const value = Object.assign({}, cellInfo.original, { status });
        test(`should renders correct status - ${status} with statusMessage`, () => {
          wrapper = getShallowWrapper(props);
          wrapper.instance().getStatus(value);
          expect(toJSON(wrapper)).toMatchSnapshot();
        });
      });

      Object.keys(Constants.STATUS).forEach((status) => {
        const value = Object.assign({}, cellInfo.value, { status });
        test(`should renders correct status - ${status} without statusMessage`, () => {
          wrapper = getShallowWrapper(props);
          wrapper.instance().getStatus(value);
          expect(toJSON(wrapper)).toMatchSnapshot();
        });
      });
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      appActions.displayGoodRequestMessage = jest.fn(() => 'displayGoodRequestMessage');
      actionsModels.viewSelectedModel = jest.fn(() => 'viewSelectedModel');
      projectActions.unmarkDeployableModel = jest.fn(() => 'unmarkDeployableModel');
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('handleUndeployableModel', () => {
      test('should mark the model for undeployable', () => {
        wrapper = getShallowWrapper(testProps);
        const model = { version: '1' };
        const modelId = null;
        const clientId = testProps.clientId;
        const projectId = testProps.projectId;

        wrapper.instance().handleUndeployableModel(model, modelId);
        expect(projectActions.unmarkDeployableModel).toHaveBeenCalledWith({
          clientId, projectId, model,
        });
      });
    });

    describe('onClickModelVersion', () => {
      test('should call an action to view the model', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().onClickModelVersion(statusComplete);
        expect(actionsModels.viewSelectedModel).toHaveBeenCalledWith(statusComplete);
        expect(props.dispatch).toHaveBeenCalledWith('viewSelectedModel');
      });

      test('should render renderModelVersion correctly', () => {
        wrapper = getShallowWrapper(props);
        const { version } = props;
        wrapper.instance().renderModelVersion(version);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('renderDataset', () => {
      test('should render renderDataset correctly', () => {
        wrapper = getShallowWrapper(props);
        const { version } = props;
        wrapper.instance().renderDataset(version);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });
  });
});
