import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import CustomDropDown from 'components/fields/CustomDropDown';

describe('<CustomDropDown />', () => {
  let wrapper;

  const props = {
    name: 'testDropDown',
    schema: {
      maxLength: 150,
      minLength: 5,
      title: 'DropDown',
      type: 'string',
      isRequired: true,
    },
    uiSchema: {},
    onChange: jest.fn(),
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<CustomDropDown
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = shallow(<CustomDropDown
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly when list/result is passed', () => {
      wrapper = shallow(<CustomDropDown
        {...props}
      />);

      wrapper.instance().processResults(['Audia', 'Chat']);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.onChange = jest.fn();
    });

    describe('handleChange', () => {
      test('should change the state when target value is empty', () => {
        wrapper = shallow(<CustomDropDown
          {...props}
        />);

        wrapper.instance().handleChange({
          target: {
            value: '',
          },
        });

        expect(wrapper.state().value).toBe('');
        expect(wrapper.state().validationMessage).toBe(`Please provide a ${props.schema.title.toLowerCase()} `);
        expect(wrapper.state().showError).toBe(true);
      });

      test('should change the state when selected value is not null', () => {
        wrapper = shallow(<CustomDropDown
          {...props}
        />);

        wrapper.instance().handleChange({
          target: {
            value: 'Sample Dropdown',
          },
        });

        expect(wrapper.state().value).toBe('Sample Dropdown');
        expect(wrapper.state().validationMessage).toBe('');
        expect(wrapper.state().showError).toBe(false);
      });
    });
  });
});
