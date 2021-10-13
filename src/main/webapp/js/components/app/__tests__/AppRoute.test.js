import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import AppRoute from 'components/app/AppRoute';

describe('<AppRoute />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    const testComponent = props => (
      <div> Component </div>
    );
    const testLayout = props => (
      <div> Layout </div>
    );

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <AppRoute
            component={testComponent}
            layout={testLayout}
            show
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('should contain testLayout', () => {
      expect(wrapper.find(testLayout).length).toBe(1);
    });

    // test('should contain testComponent', () => {
    //   expect(wrapper.find(testComponent).length).toBe(1);
    // });
  });
});
