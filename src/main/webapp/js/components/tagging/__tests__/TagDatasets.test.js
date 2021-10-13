jest.mock('utils/api');

import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TaggerApp from 'components/app/TaggerApp';
import AppHeader from 'components/AppHeader';
import TagDatasets from 'components/tagging/TagDatasets';

describe('<TagDatasets />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/tag-datasets']}
          initialIndex={0}
        >
          <TaggerApp
            location={{
              search: '',
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
      expect(wrapper.find(AppHeader).length).toBe(0);
    });

    test('should contain TagDatasets', () => {
      expect(wrapper.find(TagDatasets).length).toBe(0);
    });
  });
});
