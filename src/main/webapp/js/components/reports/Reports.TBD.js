import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TaggerApp from 'components/app/TaggerApp';
import AppHeader from 'components/AppHeader';
import Reports from 'components/reports/Reports';

describe('<Reports />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/clients/5/projects/-1/reports']}
          initialIndex={0}
        >
          <TaggerApp
            match={{
              params: {
                clientId: 1,
                projectId: 2,
              },
            }}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('should contain TaggerApp', () => {
      expect(wrapper.find(TaggerApp).length).toBe(1);
    });

    test('should contain AppHeader', () => {
      expect(wrapper.find(AppHeader).length).toBe(1);
    });

    test('should contain Reports', () => {
      expect(wrapper.find(Reports).length).toBe(1);
    });
  });
});
