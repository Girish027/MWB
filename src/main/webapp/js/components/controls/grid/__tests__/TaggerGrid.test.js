import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import TaggerGrid from 'components/controls/grid/TaggerGrid';

describe('<TaggerGrid />', () => {
  let wrapper;

  const props = {
    dispatch: jest.fn(),
    config: { cursor: 'ssdfd' },
  };

  const getShallowWrapper = (propsObj) => shallow(<TaggerGrid
    {...propsObj}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      const defaultProps = {
        ...props,
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with className', () => {
      const defaultProps = {
        ...props,
        className: 'fff',
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
