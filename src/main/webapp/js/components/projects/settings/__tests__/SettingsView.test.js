import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { SettingsView } from 'components/projects/settings/SettingsView';
import Constants from '../../../../constants/Constants';

describe('<SettingsView />', () => {
  const initialState = {
    selectedNavItem: 'Node 1',
    activeNodeLevelTab: Constants.UNIVERSAL_TAB,
  };

  let wrapper;
  const props = {};

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<SettingsView
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<SettingsView
        {...props}
      />);
      wrapper.setState({
        ...initialState,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should match with basic props', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
