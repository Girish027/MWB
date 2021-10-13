import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import BulkTagCellEditable from 'components/tagging/grid/BulkTagCellEditable';

describe('<BulkTagCellEditable />', () => {
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
        <BulkTagCellEditable
          {...props}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
