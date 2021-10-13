import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';

import { DataSetTable } from 'components/tagging/grid/DataSetTable';

describe('<DataSetTable />', () => {
  const match = {
    params: {
      projectId: '7',
      datasetId: '3',
    },
  };

  const testProps = {
    dispatch: () => {},
    clientId: '151',
    projectId: '7',
    tagDatasets: {
      project: {},
      filter: {},
      isLoading: false,
      cancelUpdateRender: true,
      datasetsLoaded: true,
      projectId: '7',
      projectLoaded: true,
      isControlsCollapsed: false,
      searchResults: {
        comment: '',
        count: 2,
        datasetIds: [],
        fullResult: {},
        id: 0,
        index: 0,
        manualTag: '',
        rutag: '',
        suggestedTag: 'reservation',
        transcriptionHash: '855b3aad9e13dcb704aae8eaf82ae1e3b135800d',
        uniqueTextString: 'reservation',
      },
    },
  };

  let wrapper;
  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Snapshots', () => {
    beforeAll(() => {
      testProps.dispatch = jest.fn();
    });

    test('should match snapshots', () => {
      wrapper = shallow(<DataSetTable
        {...testProps}
      />);

      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
