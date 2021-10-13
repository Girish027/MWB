import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import DeleteDialog from 'components/controls/DeleteDialog';

describe('<DeleteDialog />', () => {
  let wrapper;
  const props = {
    header: 'Delete Dialog',
    onOk: () => {},
    dispatch: () => {},
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<DeleteDialog
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Function Call Handler:', () => {
    beforeAll(() => {
      props.header = 'Delete';
      props.dispatch = jest.fn();
      props.onOk = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    beforeEach(() => {
      wrapper = shallow(<DeleteDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });
  });
});
