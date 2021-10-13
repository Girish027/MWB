import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import PromoteDialog from 'components/controls/PromoteDialog';

describe('<PromoteDialog />', () => {
  let wrapper;
  const props = {
    header: 'Promote Dialog',
    onClickCancel: () => {},
    dispatch: () => {},
    onClickPromote: () => {},
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<PromoteDialog
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
      props.header = '';
      props.dispatch = jest.fn();
      props.onClickCancel = jest.fn();
      props.onClickPromote = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    beforeEach(() => {
      wrapper = shallow(<PromoteDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.onClickCancel).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });

    describe('onClickOk:', () => {
      test('should promote the project for the selected global model name', () => {
        wrapper.setState({
          selectedValue: 'Root_Intent',
        });
        wrapper.instance().onClickOk();
        expect(props.onClickPromote).toHaveBeenCalledWith(wrapper.state().selectedValue);
      });
    });

    describe('onChange:', () => {
      test('should prevent default behaviour of triggered event and update state', () => {
        const value = 'Root_Intent';
        wrapper.instance().onChange(value);
        expect(wrapper.state().selectedValue).toEqual(value);
      });
    });
  });
});
