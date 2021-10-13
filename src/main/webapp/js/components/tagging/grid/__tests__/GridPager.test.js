import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import GridPager from 'components/tagging/grid/GridPager';

describe('<GridPager />', () => {
  let wrapper;

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = mount(<Provider store={store} />);
    });
  });

  describe('Snapshots', () => {
    const props = {
      dispatch: () => {},
    };
    beforeAll(() => {
      props.dispatch = jest.fn();
    });

    test('should match snapshots', () => {
      wrapper = shallow(<Provider store={store}>
        <GridPager
          {...props}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
