import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import CustomTextArea from 'components/fields/CustomTextArea';

describe('<CustomTextArea />', () => {
  let wrapper;

  const props = {
    name: 'testDescription',
    schema: {
      maxLength: 150,
      minLength: 5,
      placeholder: 'Add Description',
      title: 'Description',
      type: 'string',
    },
    uiSchema: {},
    onChange: jest.fn(),
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<CustomTextArea
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = shallow(<CustomTextArea
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.onChange = jest.fn();
    });

    describe('onChange', () => {
      test('should pass the props and change the state with new value in the form', () => {
        wrapper = shallow(<CustomTextArea
          {...props}
        />);

        wrapper.instance().handleChange({
          target: {
            name: 'runDescription',
            value: 'Run Description',
          },
        });
        expect(props.onChange).toHaveBeenCalledWith('Run Description');
        expect(wrapper.state().value).toEqual('Run Description');
      });

      test('should chamge the state in case of validation failure', () => {
        wrapper = shallow(<CustomTextArea
          {...props}
        />);

        expect(wrapper.state().validationMessage).toEqual('');
        wrapper.instance().handleChange({
          target: {
            name: 'runDescription',
            value: 'Run Description',
            validationMessage: 'Description should not be less than 4 char',
          },
        });
        expect(wrapper.state().validationMessage).toEqual('Description should not be less than 4 char');
      });
    });
  });
});
