import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Constants from 'constants/Constants';
import CustomTextField from 'components/fields/CustomTextField';

describe('<CustomTextField />', () => {
  let wrapper;

  const props = {
    name: 'testName',
    schema: {
      maxLength: 150,
      minLength: 5,
      placeholder: 'Name this iteration',
      title: 'Name',
      type: 'string',
    },
    uiSchema: {},
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<CustomTextField
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = shallow(<CustomTextField
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
        wrapper = shallow(<CustomTextField
          {...props}
        />);

        wrapper.instance().handleChange({
          target: {
            name: 'runName',
            value: 'Run Name',
          },
        });
        expect(props.onChange).toHaveBeenCalledWith('Run Name');
        expect(wrapper.state().value).toEqual('Run Name');
      });

      test('Dataset name should allow only alphanumeric characters and spaces', () => {
        wrapper = shallow(<CustomTextField {...props} />);
        wrapper.instance().handleChange({
          target: {
            value: 'My First-Dataset_Name',
            validationMessage: '',
          },
        });
        expect(wrapper.state().validationMessage).toEqual('');
      });

      test('Dataset name should not allow any special characters', () => {
        wrapper.instance().handleChange({
          target: {
            value: 'MyFirst#$%^GSf',
            validationMessage: 'Only alphanumeric characters are allowed..!!',
          },
        });
        expect(wrapper.state().validationMessage).toEqual(Constants.INVALID_ENTERED_NAME);
      });

      test('Dataset name should not exceed to 64 characters ', () => {
        wrapper.instance().handleChange({
          target: {
            value: 'llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll',
            validationMessage: 'Oops, Exceeded maximum size of 64 characters',
          },
        });
        expect(wrapper.state().validationMessage).toEqual(Constants.VALIDATION_NAME_SIZE_MSG);
      });
    });
  });
});
