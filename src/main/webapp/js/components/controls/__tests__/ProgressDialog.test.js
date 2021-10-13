import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import ProgressDialog from 'components/controls/ProgressDialog';
import * as actionsPreferences from 'state/actions/actions_preferences';

describe('<ProgressDialog />', () => {
  let wrapper;

  describe('Creating an instance with no props', () => {
    beforeEach(() => {
      wrapper = shallow(<ProgressDialog />);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance with test props', () => {
    const props = {
      message: 'Loading',
      clientId: '123',
      projectId: '123',
      closeIconVisible: true,
      dispatch: () => {},
    };

    beforeAll(() => {
      props.dispatch = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      actionsPreferences.getTechnologyByClientModel = jest.fn(() => 'getTechnologyByClientModel');
    });

    beforeEach(() => {
      wrapper = shallow(<ProgressDialog
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should dispatch an action when closeIcon is clicked', () => {
      wrapper.instance().onClickClose();
      expect(actionsApp.modalDialogChange).toHaveBeenCalled;
    });

    test('should dispatch action to save vectorizer', () => {
      wrapper.instance().componentDidMount();
      expect(actionsPreferences.getTechnologyByClientModel).toHaveBeenCalled();
      expect(props.dispatch).toHaveBeenCalledWith('getTechnologyByClientModel');
    });
  });
});
