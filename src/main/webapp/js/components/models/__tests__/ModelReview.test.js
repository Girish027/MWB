import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';

const ModelReview = require('components/models/ModelReview').default;

let wrapper;

describe('<ModelReview />', () => {
  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/projects/-1/models/createconfig']}
          initialIndex={0}
        >
          <ModelReview />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<ModelReview />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });
    test('should match with basic props', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
