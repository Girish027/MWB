import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import ValidationPlaceholder from 'components/fields/ValidationPlaceholder';

describe('<ValidationPlaceholder />', () => {
  let wrapper;

  const props = {
    validationMessage: 'Name should be greater than 4 char',
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<ValidationPlaceholder
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = shallow(<ValidationPlaceholder
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
