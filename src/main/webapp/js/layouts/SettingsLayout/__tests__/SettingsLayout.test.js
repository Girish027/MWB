import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import SettingsLayout from 'layouts/SettingsLayout';

describe('<SettingsLayout />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <SettingsLayout
            match={{
              path: '/',
              url: '/',
              params: {
                projectId: 5,
              },
            }}
          >
            <div>Hello World</div>
          </SettingsLayout>
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
