import React from 'react';
import { shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TaggerApp from 'components/app/TaggerApp';
import AppHeader from 'components/AppHeader';
import TaggingGuide from 'components/taggingguide/TaggingGuide';

describe('<TaggingGuide />', () => {
  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = shallow(<Provider store={store}>
        <TaggingGuide
          match={{
            params: {
              clientId: 1,
              projectId: 2,
            },
          }}
        />
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });
});
