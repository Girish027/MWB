import React from 'react';
import { mount, shallow } from 'enzyme';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';

// Require the component to test (HomePage) through proxyquire
// and pass it the stubbed dependencies
const ModelsView = require('components/projects/models/ModelsView').default;

describe('ModelsView', () => {
  const props = {
    models: null,
    loadingModels: false,
    projectId: '5',
    isAnyDatasetTransformed: true,
    clientId: '101',
    history: [],
    dispatch: jest.fn(),
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Initializing with a basic data set', () => {
    beforeAll(() => {
      wrapper = mount(
        <Provider store={store}>
          <MemoryRouter
            initialEntries={['/']}
            initialIndex={0}
          >
            <ModelsView {...props} />
          </MemoryRouter>
        </Provider>,
      );
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('should render modelView component correctly with given props', () => {
      wrapper = shallow(<ModelsView
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
