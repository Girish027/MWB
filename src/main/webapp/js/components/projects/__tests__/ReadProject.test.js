import React from 'react';
import { shallow, mount } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import * as actionsApp from 'state/actions/actions_app';
import toJSON from 'enzyme-to-json';
import { ReadProject } from 'components/projects/ReadProject';
import ReduxReadProject from 'components/projects/ReadProject';
import ConnectedReadProject from 'components/projects/ReadProject';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';

const middlewares = [];
const mockStore = configureStore(middlewares);

// Initialize mockstore with empty state
const initialState = {
  header: {
    client: {
      id: 5,
    },
  },
};

const { featureFlags } = global.uiConfig;

const testProps = {
  project: {},
  clientId: '12',
  projectId: '23',
  dispatch: jest.fn(),
  history: {},
  taggingguide: [],
  userFeatureConfiguration: featureFlags.DEFAULT,
  datasets: {
    toArray: jest.fn().mockReturnValue([{ id: '123', status: Constants.STATUS.COMPLETED }]),
  },
  models: {
    toArray: jest.fn().mockReturnValue([{ id: '123', status: Constants.STATUS.COMPLETED }]),
  },
  environment: 'dev',
  match: {
    path: '/models',
  },
};
const store = mockStore(initialState);
let wrapper;

const getShallowWrapperWithRedux = (propsObj) => shallow(<Provider store={store}>
  <ReduxReadProject {...propsObj} />
</Provider>);

afterAll(() => {
  jest.clearAllMocks();
});

const getShallowWrapper = (propsObj) => shallow(<ReadProject
  {...propsObj}
/>);

describe('<ReadProject />', () => {
  const match = {
    params: {
      projectId: '7',
      modelId: '3',
    },
  };
  const getMountedWrapper = () => mount(
    <Provider store={store}>
      <ConnectedReadProject
        {...testProps}
      />
    </Provider>,
  );

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = shallow(<Provider store={store}>
        <ReadProject {...testProps} />
      </Provider>);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly when status complete', () => {
      wrapper = getShallowWrapperWithRedux(testProps);
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
        loadingProject: false,
        loadingDatasets: false,
        loadingModels: false,
        componentMounted: false,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props on shallow copy', () => {
      wrapper = getShallowWrapper(testProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with state loadingProject set', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.setState({ loadingProject: true });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props on shallow copy', () => {
      const defaultProps = {
        ...testProps,
        projectId: undefined,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with datasets and models undefined', () => {
      const defaultProps = {
        ...testProps,
        datasets: undefined,
        models: undefined,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with userFeatureConfiguration undefined', () => {
      const defaultProps = {
        ...testProps,
        userFeatureConfiguration: undefined,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with path set as /manage-intents', () => {
      const defaultProps = {
        ...testProps,
        match: {
          path: '/manage-intents',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with path set as /datasets', () => {
      const defaultProps = {
        ...testProps,
        match: {
          path: '/datasets',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with path set as /', () => {
      const defaultProps = {
        ...testProps,
        match: {
          path: '/',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props without any project', () => {
      const defaultProps = {
        ...testProps,
        project: undefined,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(testProps);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      actionsApp.changeRoute = jest.fn(() => 'called changeRoute');
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    describe('onClickTag', () => {
      test('should call onClickTag', () => {
        const { clientId, projectId, history } = testProps;
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickTag();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.TAG_DATASETS, { clientId, projectId }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onClickTagBeta', () => {
      test('should call onClickTag', () => {
        const { clientId, projectId, history } = testProps;
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickTagBeta();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.TAG_DATASETS_BETA, { clientId, projectId }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onClickDatasetTemplate', () => {
      test('should call onClickDatasetTemplate', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickDatasetTemplate();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickExportAllDatasets', () => {
      test('should call onClickExportAllDatasets', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickExportAllDatasets();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickUpload', () => {
      test('should call onClickUpload', () => {
        const { project } = testProps;
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickUpload();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({ type: Constants.DIALOGS.CREATE_DATASET, project });
        expect(testProps.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });

    describe('componentWillUnmount', () => {
      test('should call componentWillUnmount', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().componentWillUnmount();
        expect(wrapper.state().loadingProject).toBe(false);
        expect(wrapper.state().loadingDatasets).toBe(false);
        expect(wrapper.state().loadingModels).toBe(false);
      });
    });

    describe('onClickResolveInconsistencies', () => {
      const { projectId, clientId, history } = testProps;
      test('should call onClickResolveInconsistencies', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onClickResolveInconsistencies();
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.RESOLVE_INCONSISTENCY, { clientId, projectId }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });

    describe('onTabSelected', () => {
      const { projectId, clientId, history } = testProps;
      test('should call onTabSelected with overview tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('overview', 0);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'overview' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with node-analytics tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('node-analytics', 1);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'node-analytics' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with models tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('models', 2);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'models' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with datasets tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('datasets', 3);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'datasets' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with manage-intents tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('manage-intents', 4);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'manage-intents' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with default tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('default', 0);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'overview' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });

      test('should call onTabSelected with manage-settings tabs selected', () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().onTabSelected('manage-settings', 5);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'manage-settings' }, history);
        expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
      });
    });
  });
});
