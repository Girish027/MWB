import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Placeholder from 'components/controls/Placeholder';

describe('<Placeholder />', () => {
  let wrapper;
  let props = {
    message: 'Default message to be shown',
  };

  describe('Creating an instance:', () => {
    test('should exist', () => {
      wrapper = shallow(<Placeholder {...props} />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    test('should render correctly when message is provided', () => {
      wrapper = shallow(<Placeholder {...props} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly when children is provided', () => {
      wrapper = shallow(<Placeholder>
        {' '}
        <p> hello </p>
        {' '}
      </Placeholder>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly when message and children are not provided', () => {
      wrapper = shallow(<Placeholder />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
