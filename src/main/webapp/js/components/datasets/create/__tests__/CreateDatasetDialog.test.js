import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import store from 'state/configureStore';
import { Provider } from 'react-redux';
import CreateDatasetDialog from 'components/datasets/create/CreateDatasetDialog';

describe('<CreateDatasetDialog />', () => {
  let wrapper;
  const props = {
    dispatch: jest.fn(),
  };

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
        <CreateDatasetDialog
          {...props}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('onClickCancel', () => {
    beforeEach(() => {
      wrapper = shallow(<Provider store={store}>
        <CreateDatasetDialog
          {...props}
        />
      </Provider>);
      wrapper.instance().onClickCancel = jest.fn()
        .mockImplementation(() => ({ called: 'onClickCancel' }));
    });

    test('should render onClickCancel correctly', () => {
      wrapper.instance().onClickCancel();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
