import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import LinkDialog from 'components/controls/LinkDialog';

describe('<LinkDialog />', () => {
  let wrapper;
  const props = {
    header: 'Link Dialog',
    onCancel: () => {},
    dispatch: () => {},
    onClickRunTest: () => {},
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<LinkDialog
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
      props.header = '',
      props.dispatch = jest.fn();
      props.onOk = jest.fn();
      props.onCancel = jest.fn();
      props.onClickRunTest = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    beforeEach(() => {
      wrapper = shallow(<LinkDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.onCancel).toHaveBeenCalled;
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });

    describe('onClickOk:', () => {
      test('should show the Progress Dialog', () => {
        wrapper.setState({
          linkTextField: '/something/text.wav',
        });
        wrapper.instance().onClickOk();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith({
          dispatch: props.dispatch,
          type: Constants.DIALOGS.PROGRESS_DIALOG,
          message: Constants.TEST_IN_PROGRESS,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });

      test('should run test for the given link', () => {
        wrapper.setState({
          linkTextField: '/something/text.wav',
        });
        wrapper.instance().onClickOk();
        expect(props.onClickRunTest).toHaveBeenCalledWith(wrapper.state().linkTextField);
      });
    });

    describe('onChange:', () => {
      test('should prevent default behaviour of triggered event and update state', () => {
        const event = {
          preventDefault: jest.fn(),
          target: {
            value: '/something/text.wav',
          },
        };
        wrapper.instance().onChange(event);
        expect(event.preventDefault).toHaveBeenCalled;
        expect(wrapper.state().linkTextField).toEqual(event.target.value);
      });
    });

    describe('onKeyPress:', () => {
      test('should prevent default behaviour of triggered event for Enter key and call click handler', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'Enter',
        };
        wrapper.instance().onClickOk = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).toHaveBeenCalled;
        expect(wrapper.instance().onClickOk).toHaveBeenCalled;
      });

      test('should prevent default behaviour of triggered event for Enter key and call click handler', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'Tab',
        };
        wrapper.instance().onClickOk = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).toHaveBeenCalled;
        expect(wrapper.instance().onClickOk).toHaveBeenCalled;
      });

      test('should not handle event if any other key is pressed', () => {
        const event = {
          preventDefault: jest.fn(),
          key: 'A',

        };
        wrapper.instance().onClickOk = jest.fn();
        wrapper.instance().onKeyPress(event);
        expect(event.preventDefault).not.toHaveBeenCalled;
        expect(wrapper.instance().onClickOk).not.toHaveBeenCalled;
      });
    });
  });
});
