import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import PageTitle from 'components/FormTitle/PageTitle';

describe('<PageTitle />', () => {
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
    description: 'Title Description',
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<PageTitle
    {...propsObj}
  />);

  const getMountWrapper = (propsObj) => mount(<PageTitle
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
