import React from 'react';
import { shallow } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import toJSON from 'enzyme-to-json';
import ModelTestIntentsGrid from 'components/models/ModelTestIntentsGrid';

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

describe('ModelTestIntentsGrid', () => {
  let wrapper;
  describe('Creating an instance', () => {
    beforeAll(() => {
      const testProps = {
        datasets: {},
        project: {},
        data: [],
      };
      wrapper = shallow(<Provider store={store}>
        <ModelTestIntentsGrid {...testProps} />
      </Provider>);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeAll(() => {
      const testProps = {
        datasets: {},
        project: {},
        data: [],
      };
      wrapper = shallow(<ModelTestIntentsGrid
        {...testProps}
      />);
    });
    test('renders correctly for ModelTestIntentsGrid table', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
