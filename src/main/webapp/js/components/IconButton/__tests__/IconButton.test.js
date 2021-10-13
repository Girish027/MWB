import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import IconButton from 'components/IconButton';
import { Plus } from '@tfs/ui-components';

describe('<IconButton />', () => {
  let wrapper;

  const props = {
    onClick: jest.fn(),
    icon: Plus,
  };

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<IconButton
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly when icon is not in focus', () => {
      wrapper = shallow(<IconButton
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when icon is in focus', () => {
      wrapper = shallow(<IconButton
        {...props}
      />);
      wrapper.setState({
        focus: true,
      });
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when icon is disabled', () => {
      wrapper = shallow(<IconButton
        {...props}
        disabled
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly with title', () => {
      wrapper = shallow(<IconButton
        {...props}
        title="test title"
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when icon with extended props', () => {
      wrapper = shallow(<IconButton
        {...props}
        width={20}
        height={20}
        strokeColor="#ffffff"
        focusedColor="red"
        defaultColor="yellow"
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      wrapper = shallow(<IconButton
        {...props}
      />);
    });

    describe('onFocus', () => {
      test('should set current state of focus to have value true', () => {
        expect(wrapper.state().focus).toBe(false);
        wrapper.instance().onFocus();
        expect(wrapper.state().focus).toBe(true);
      });
    });

    describe('clearFocus', () => {
      test('should set current state of focus to have value false', () => {
        wrapper.setState({
          focus: true,
        });
        wrapper.instance().clearFocus();
        expect(wrapper.state().focus).toBe(false);
      });
    });
  });
});
