import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import EditorOrderedList from 'components/modelConfigs/transformations/EditorOrderedList';

describe('<EditorOrderedList />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <EditorOrderedList />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
