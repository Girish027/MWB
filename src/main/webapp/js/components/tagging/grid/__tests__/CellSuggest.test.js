import React from 'react';
import { shallow, mount } from 'enzyme';
import store from 'state/configureStore';
import toJSON from 'enzyme-to-json';
import { Provider } from 'react-redux';
import CellSuggest from 'components/tagging/grid/CellSuggest';

describe('<CellSuggest />', () => {
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
        <CellSuggest
          {...props}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
