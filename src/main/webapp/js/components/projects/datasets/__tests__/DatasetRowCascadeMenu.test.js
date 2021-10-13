import React from 'react';
import { mount } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import DatasetRowCascadeMenu from 'components/projects/datasets/DatasetRowCascadeMenu';

const middlewares = [];
const mockStore = configureStore(middlewares);

// Initialize mockstore with empty state
const initialState = {
  header: {
    client: {
      id: 5,
    },
  },
  app: {
    userFeatureConfiguration: '',
  },
};
const store = mockStore(initialState);

describe('<DatasetRowCascadeMenu />', () => {
  let wrapper;
  describe('Creating an instance', () => {
    beforeAll(() => {
      const testProps = {
        datasetId: '5',
        projectId: '6',
      };
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <DatasetRowCascadeMenu
            projectId={testProps.projectId}
            datasetId={testProps.datasetId}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('should contain DatasetRowCascadeMenu', () => {
      expect(wrapper.find(DatasetRowCascadeMenu).length).toBe(1);
    });
  });
});
