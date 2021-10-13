import React from 'react';
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TransformationEditorContainer from 'components/modelConfigs/transformations/TransformationEditorContainer';

describe('<TransformationEditorContainer />', () => {
  const data = {
    modelName: '',
    name: '',
  };

  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/projects/-1/models/createconfig']}
          initialIndex={0}
        >
          <TransformationEditorContainer
            data={data}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
