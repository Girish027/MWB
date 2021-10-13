import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import { getLanguage } from 'state/constants/getLanguage';
import TaggerModal from 'components/controls/TaggerModal';


describe('<TaggerModal />', () => {
  let wrapper;

  const props = {
    header: 'Apply Suggested Intents',
  };

  describe('Creating an instance with no props', () => {
    beforeEach(() => {
      wrapper = shallow(<TaggerModal />);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = mount(<TaggerModal
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
