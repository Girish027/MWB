import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';

const Overview = require('components/projects/overview_tab/Overview').default;

describe('Overview', () => {
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
  test('should render overview component correctly with given props', () => {
    wrapper = shallow(<Overview
      {...props}
    />);
    expect(toJSON(wrapper)).toMatchSnapshot();
  });
});
