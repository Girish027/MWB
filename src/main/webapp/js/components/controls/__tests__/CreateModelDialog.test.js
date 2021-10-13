import React from 'react';
import { shallow, mount } from 'enzyme';
import toJSON from 'enzyme-to-json';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import * as projectActions from 'state/actions/actions_projects';
import CreateModelDialog from 'components/controls/CreateModelDialog';

describe('<CreateModelDialog />', () => {
  let wrapper;
  let mockPush = jest.fn();
  let history = {};
  const props = {
    header: Constants.CREATE_MODEL,
    onCancel: () => {},
    dispatch: () => {},
    history,
    clientId: '14',
  };

  const getShallowWrapper = (propsObj) => shallow(<CreateModelDialog
    {...props}
  />);

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = getShallowWrapper(props);
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
      props.header = Constants.CREATE_MODEL,
      props.dispatch = jest.fn();
      props.onCancel = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      projectActions.createProject = jest.fn(() => 'createProject');
    });

    beforeEach(() => {
      wrapper = shallow(<CreateModelDialog
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
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
      });
    });

    describe('onSubmit:', () => {
      test('should call create project', () => {
        const { dispatch, clientId, history } = props;
        const formData = {
          name: 'testProject',
          description: 'testProject',
          vertical: 'FINANCIAL',
          locale: 'en-US',
        };
        wrapper.setState({
          formData,
        });
        wrapper.instance().onSubmit();
        expect(projectActions.createProject).toHaveBeenCalledWith({
          clientId,
          history,
          values: { clientId, ...formData },
        });
        expect(dispatch).toHaveBeenCalledWith('createProject');
      });
    });

    describe('onFormChange:', () => {
      test('should change the state when there are no error', () => {
        const data = {
          name: 'abc',
          description: 'form',
        };
        wrapper.instance().onFormChange(data, []);
        expect(wrapper.state().formData).toBe(data);
        expect(wrapper.state().isValid).toBe(true);
      });

      test('should change the state when there are no error', () => {
        const data = {
          name: 'abc',
          description: 'form',
        };
        wrapper.instance().onFormChange(data, [{ name: 'is required' }]);
        expect(wrapper.state().isValid).toBe(false);
      });
    });
  });
});
