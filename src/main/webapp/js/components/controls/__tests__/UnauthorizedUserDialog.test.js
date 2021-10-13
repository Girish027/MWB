import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import UnauthorizedUserDialog from 'components/controls/UnauthorizedUserDialog';
import * as actionsApp from 'state/actions/actions_app';

describe('<UnauthorizedUserDialog />', () => {
  let wrapper;

  const props = {
    dispatch: jest.fn(),
  };

  beforeEach(() => {
    wrapper = shallow(<UnauthorizedUserDialog />);
  });

  describe('Creating an instance:', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    test('should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('onClickCancel', () => {
    beforeEach(() => {
      wrapper = shallow(<UnauthorizedUserDialog {...props} />);
    });

    test('should render onClickCancel correctly', () => {
      const { dispatch } = props;
      wrapper.instance().onClickCancel();
      expect(dispatch(actionsApp.modalDialogChange(null)));
    });
  });
});
