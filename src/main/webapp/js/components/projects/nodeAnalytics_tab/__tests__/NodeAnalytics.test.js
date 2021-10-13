import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';

const NodeAnalytics = require('components/projects/nodeAnalytics_tab/NodeAnalytics').default;

describe('NodeAnalytics', () => {
  const props = {
    models: null,
    loadingModels: false,
    projectId: '5',
    isAnyDatasetTransformed: true,
    clientId: '101',
    history: [],
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });
  test('should render NodeAnalytics component correctly with given props', () => {
    wrapper = shallow(<NodeAnalytics
      {...props}
    />);
    expect(toJSON(wrapper)).toMatchSnapshot();
  });
});
