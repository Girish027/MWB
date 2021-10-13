import React from 'react';
import { shallow, mount } from 'enzyme';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import toJSON from 'enzyme-to-json';
import Constants from 'constants/Constants';
import * as actionsDatasets from 'state/actions/actions_datasets';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import { changeRoute } from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import { ReadProjectDatasetsGrid } from 'components/projects/datasets/ReadProjectDatasetsGrid';
import ConnectedReadProjectDatasetsGrid from 'components/projects/datasets/ReadProjectDatasetsGrid';

describe('<ReadProjectDatasetsGrid />', () => {
  const match = {
    params: {
      projectId: '7',
      datasetId: '3',
    },
  };

  const testProps = {
    clientId: '123',
    project: {},
    dispatch: () => {},
    history: {},
    app: {
      userDetails: { userType: 'Internal' },
      csrfToken: 'wewqw',
      userId: '12',
    },
    header: {
      client: {
        id: '123',
      },
    },
  };

  const testValue = {
    _key: '3',
    clientId: '001',
    description: 'ftyfriyttg',
    id: '3',
    locale: 'en-US',
    modifiedBy: 'abc@247.ai',
    createdAt: 1573029950062,
    name: 'y1',
    projectId: '7',
    status: 'COMPLETED',
    task: 'INDEX',
    type: 'Social/Text',
  };

  const statusComplete = [{
    _key: '3',
    clientId: '001',
    description: 'ftyfriyttg',
    id: '3',
    locale: 'en-US',
    modifiedBy: 'abc@247.ai',
    name: 'y1',
    projectId: '7',
    status: 'COMPLETED',
    task: 'INDEX',
    type: 'Social/Text',
  }];

  const statusFailed = [{
    _key: '3',
    clientId: '001',
    description: 'ftyfriyttg',
    id: '3',
    locale: 'en-US',
    modifiedBy: 'abc@247.ai',
    name: 'y1',
    projectId: '7',
    status: 'FAILED',
    task: 'INDEX',
    type: 'Social/Text',
  }];

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<ReadProjectDatasetsGrid
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ConnectedReadProjectDatasetsGrid
            match={match}
            location={{
              search: '?clientid=247ai&appid=aisha&projectid=7',
            }}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });


  describe('Snapshots', () => {
    test('renders correctly when status completed', () => {
      wrapper = shallow(<ReadProjectDatasetsGrid
        {...testProps}
        data={statusComplete}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '5',
          },
        },
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when status failed', () => {
      wrapper = shallow(<ReadProjectDatasetsGrid
        {...testProps}
        data={statusFailed}
      />);
      wrapper.setState({
        header: {
          client: {
            id: '5',
          },
        },
      });

      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderName with status completed', () => {
      const value = Object.assign({}, testValue);
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderName(value);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderName with default status', () => {
      const value = Object.assign({}, testValue, { status: 'NULL' });
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderName(value);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderStatus with status null', () => {
      const value = Object.assign({}, testValue, { status: 'NULL' });
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderStatus(value);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderStatus with status null and percentage', () => {
      const value = Object.assign({}, testValue, { percentComplete: 45, status: 'NULL' });
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderStatus(value);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderCreatedAt', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderCreatedAt(testValue.createdAt);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call getStatusBadge', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().getStatusBadge(testValue.status, testValue.status);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call renderStatus with status other than null', () => {
      const value = Object.assign({}, testValue, { status: 'COMPLETED' });
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().renderStatus(value);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    Object.keys(Constants.STATUS).forEach((status) => {
      const value = Object.assign({}, testValue, { status });
      test(`should renders correct status - ${status}`, () => {
        wrapper = getShallowWrapper(testProps);
        wrapper.instance().getStatus(value);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });
  });

  describe('<Function call />', () => {
    beforeEach(() => {
      testProps.dispatch = jest.fn();
      actionsDatasets.fetchDatasetTransform = jest.fn(() => 'fetchDatasetTransform');
      actionsTagDatasets.setIncomingFilter = jest.fn(() => 'setIncomingFilter');
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should call onClickStatus', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickStatus(testValue);
      expect(actionsDatasets.fetchDatasetTransform).toHaveBeenCalledWith(testProps.app.userId, testValue.id, testProps.header.client.id, testValue.projectId, testProps.app.csrfToken, false);
      expect(testProps.dispatch).toHaveBeenCalledWith('fetchDatasetTransform');
    });

    test('should call onClickName', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickName(testValue);
      expect(actionsTagDatasets.setIncomingFilter).toHaveBeenCalledWith({ projectId: testValue.projectId, datasets: [testValue.id] });
      expect(testProps.dispatch).toHaveBeenCalledWith('setIncomingFilter');
    });
  });
});
