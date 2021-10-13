import React from 'react';
import { mount } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import { MemoryRouter } from 'react-router';
import ConnectedProjectListSidebar from 'components/sidebar/ProjectListSidebar';

const middlewares = [thunk];
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
  projectsManager: {
    projects: new Map(),
  },
  projectListSidebar: {
    clientId: '12',
    selectedProjectId: '4',
  },
};
const store = mockStore(initialState);

describe('<ProjectListSidebar />', () => {
  let wrapper;

  describe('Connected ProjectListSidebar', () => {
    beforeAll(() => {
      const testProps = {
        projectId: '6',
      };
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ConnectedProjectListSidebar
            projectId={testProps.projectId}
            {...testProps}
          />
        </MemoryRouter>
      </Provider>);
    });

    it('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    it('should contain ProjectListSidebar', () => {
      expect(wrapper.find('ProjectListSidebar').length).toEqual(1);
    });
  });
});
