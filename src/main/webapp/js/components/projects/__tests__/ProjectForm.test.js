import React from 'react';
import { shallow } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import ProjectForm from 'components/projects/ProjectForm';

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
const store = mockStore(initialState);

describe('ProjectForm', () => {
  let wrapper;
  describe('Creating an instance', () => {
    beforeAll(() => {
      const testProps = {
        datasets: {},
        project: {},
      };
      wrapper = shallow(<Provider store={store}>
        <ProjectForm {...testProps} />
      </Provider>);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
