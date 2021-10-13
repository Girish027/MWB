import React from 'react';
import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import toJSON from 'enzyme-to-json';
import ReactDOM from 'react-dom';
import EditableLabel from 'components/common/EditableLabel';


describe('<EditableLabel />', () => {
  const match = {
    params: {
      clientId: '1',
      projectId: '2',
      modelId: '3',
    },
  };

  const props = {
    defaultValue: 'test value',
    validateAndUpdateValue: () => {},
    customClassName: '',
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<EditableLabel
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = mount(<EditableLabel
        {...props}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<EditableLabel
        {...props}
      />);
    });

    test('renders correctly for a model without any batch tests', () => {
      wrapper = shallow(<EditableLabel
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly when custom class is provided', () => {
      wrapper = shallow(<EditableLabel
        defaultValue="test value"
        validateAndUpdateValue={() => {}}
        customClassName="test class"
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should call enableEditMode', () => {
      wrapper = getShallowWrapper(props);
      wrapper.instance().enableEditMode();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
