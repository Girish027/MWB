import React from 'react';
import { shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import ProjectListFolderItem from 'components/sidebar/ProjectListFolderItem';
import * as actionsApp from 'state/actions/actions_app';

describe('<ProjectListFolderItem />', () => {
  const initialState = {
    showAction: false,
    showDropDown: false,
  };

  const testProps = {
    project: {
      clientId: '151',
      id: '2358',
      name: 'Root_Intent',
      type: 'GLOBAL',
    },
    index: 0,
    dispatch: jest.fn(),
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (props) => shallow(<ProjectListFolderItem
    {...props}
  />);

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<ProjectListFolderItem
        {...testProps}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<ProjectListFolderItem
        {...testProps}
      />);
      wrapper.setState({
        ...initialState,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('should match with basic props', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      actionsApp.changeRoute = jest.fn(() => 'called changeRoute');
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
    });
    test('should call onDropDownSelected with value Promote', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickPromoteProject = jest.fn();
      wrapper.instance().onDropDownSelected('Promote');
      expect(wrapper.instance().onClickPromoteProject).toHaveBeenCalled();
      expect(wrapper.state().showDropDown).toBe(false);
    });

    test('should call onDropDownSelected with value Demote', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickDemoteProject = jest.fn();
      wrapper.instance().onDropDownSelected('Demote');
      expect(wrapper.instance().onClickDemoteProject).toHaveBeenCalled();
    });

    test('should call onDropDownSelected with value Delete', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickDeleteProject = jest.fn();
      wrapper.instance().onDropDownSelected('Delete');
      expect(wrapper.instance().onClickDeleteProject).toHaveBeenCalled();
    });

    test('should call onDropDownSelected with value Edit', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickEditProject = jest.fn();
      wrapper.instance().onDropDownSelected('Edit');
      expect(wrapper.instance().onClickEditProject).toHaveBeenCalled();
    });

    test('should call onClickPromoteProject', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickPromoteProject();
      expect(actionsApp.modalDialogChange).toHaveBeenCalled();
      expect(testProps.dispatch).toHaveBeenCalledWith('called modalDialogChange');
    });

    test('should call onClickDeleteProject', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickDeleteProject();
      expect(actionsApp.modalDialogChange).toHaveBeenCalled();
      expect(testProps.dispatch).toHaveBeenCalledWith('called modalDialogChange');
    });

    test('should call onClickDemoteProject', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickDemoteProject();
      expect(actionsApp.modalDialogChange).toHaveBeenCalled();
      expect(testProps.dispatch).toHaveBeenCalledWith('called modalDialogChange');
    });

    test('should call onClickEditProject', () => {
      wrapper = getShallowWrapper(testProps);
      wrapper.instance().onClickEditProject();
      expect(actionsApp.changeRoute).toHaveBeenCalled();
      expect(testProps.dispatch).toHaveBeenCalledWith('called changeRoute');
    });
  });
});
