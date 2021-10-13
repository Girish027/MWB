import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';
import SimpleDialog from 'components/controls/SimpleDialog';


describe('<SimpleDialog />', () => {
  let wrapper;

  const props = {
    header: 'Apply Suggested Intents',
  };

  describe('Creating an instance with no props', () => {
    beforeEach(() => {
      wrapper = shallow(<SimpleDialog />);
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });

    test('Snapshots should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = mount(<SimpleDialog
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

  describe('Functionality', () => {
    beforeAll(() => {
      props.dispatch = jest.fn();
      props.onCancel = jest.fn();
      props.onOk = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });

    beforeEach(() => {
      wrapper = shallow(<SimpleDialog
        {...props}
      />);
    });

    describe('onClickCancel:', () => {
      test('should call onClickCancel click handler', () => {
        const props = {
          header: 'Simple Dialog',
          onCancel: () => {},
          dispatch: () => {},
        };

        wrapper.instance().onClickCancel();
        wrapper = shallow(<SimpleDialog
          {...props}
        />);
        expect(props.onCancel).toHaveBeenCalled;
      });
    });

    describe('onClickOk:', () => {
      test('should call onClickOk click handler', () => {
        const props = {
          header: 'Simple Dialog',
          onOk: () => {},
          dispatch: () => {},
        };

        wrapper.instance().onClickOk();
        wrapper = shallow(<SimpleDialog
          {...props}
        />);
        expect(props.onOk).toHaveBeenCalled;
      });
    });
  });
});
