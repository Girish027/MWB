import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import SectionTitle from 'components/FormTitle/SectionTitle';

describe('<SectionTitle />', () => {
  let wrapper;


  const props = {
    formContext: {
      abc: 'asdasd',
    },
    properties: [{
      name: 'abc',
    }, {
      name: 'bcd',
    }],
    title: 'Title',
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<SectionTitle
    {...propsObj}
  />);

  const getMountWrapper = (propsObj) => mount(<SectionTitle
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getMountWrapper();
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly with default props', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
