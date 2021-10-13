import React from 'react';
import { shallow, mount } from 'enzyme';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import toJSON from 'enzyme-to-json';

import ConnectedTagDatasetsBeta from 'components/tagging/TagDatasetsBeta';
import { TagDatasetsBeta } from 'components/tagging/TagDatasetsBeta';

describe('<TagDatasetsBeta />', () => {
  const match = {
    params: {
      projectId: '7',
      datasetId: '3',
    },
  };

  const testProps = {
    dispatch: () => {},
    clientDataLoaded: true,
    clientId: '151',
    projectId: '7',
    tagDatasets: {
      project: {},
      filter: {},
      cancelUpdateRender: true,
      datasetsLoaded: true,
      projectId: '7',
      projectLoaded: true,
      isControlsCollapsed: false,
    },
  };

  let wrapper;
  afterAll(() => {
    jest.clearAllMocks();
  });


  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <ConnectedTagDatasetsBeta
            match={match}
            location={{
              pathname: '/tag-datasets-beta',
              search: '?clientid=247ai&appid=aisha&projectid=7',
            }}
          />
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });


  describe('Snapshots', () => {
    beforeAll(() => {
      testProps.dispatch = jest.fn();
    });

    test('should match snapshots', () => {
      wrapper = shallow(<TagDatasetsBeta
        {...testProps}
      />);

      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
