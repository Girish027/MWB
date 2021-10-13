import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import CustomDropZone from 'components/fields/CustomDropZone';

describe('<CustomDropZone />', () => {
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
      wrapper = shallow(<CustomDropZone
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = shallow(<CustomDropZone
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.onChange = jest.fn();
    });

    describe('saveFile', () => {
      test('should call onChange props', () => {
        wrapper = shallow(<CustomDropZone
          {...props}
        />);

        wrapper.instance().saveFile([1, 2]);
        expect(props.onChange).toHaveBeenCalledWith(1);
      });
    });
  });
});
