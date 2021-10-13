import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Form from 'components/Form/Form';
import createModelUiSchema from 'components/schema/createModel/uiSchema.json';
import createModelJsonSchema from 'components/schema/createModel/jsonSchema.json';

describe('<Form />', () => {
  let wrapper;


  const props = {
    uiSchema: createModelUiSchema,
    jsonSchema: createModelJsonSchema,
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<Form
    {...propsObj}
  />);

  const getMountWrapper = (propsObj) => mount(<Form
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getMountWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with default props', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.onChange = jest.fn();
    });

    describe('onChange', () => {
      test('should change the state when onChange is called', () => {
        wrapper = shallow(<Form
          {...props}
        />);

        const formData = {
          name: 'abc',
        };

        wrapper.setState({
          componentMounted: true,
        });
        wrapper.instance().onChange({ formData, errors: [] });
        expect(props.onChange).toHaveBeenCalledWith(formData, []);
      });
    });

    describe('transformErrors', () => {
      test('should properly transform the errors', () => {
        wrapper = shallow(<Form
          {...props}
        />);

        const errors = [
          {
            message: 'new Meassage',
            type: 'error',
          },
        ];

        expect(wrapper.instance().transformErrors(errors)).toStrictEqual([{ type: 'error' }]);
      });
    });
  });
});
