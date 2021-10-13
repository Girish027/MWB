import React from 'react';
import { shallow } from 'enzyme';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsPreferences from 'state/actions/actions_preferences';
import Constants from 'constants/Constants';
import { RouteNames } from 'utils/routeHelpers';
import CreateVersionDialog from 'components/controls/CreateVersionDialog';

describe('<CreateVersionDialog />', () => {
  let wrapper;
  const props = {
    header: 'Create Version Dialog',
    onClickCancel: () => {},
    dispatch: () => {},
    projectId: 'testProject',
    modelId: '100',
    isAnyDatasetTransformed: true,
    client: {
      id: '10',
    },
    clientId: '123',
    history: {},
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance with test props', () => {
    beforeEach(() => {
      wrapper = shallow(<CreateVersionDialog
        {...props}
      />);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Function Call Handler:', () => {
    beforeAll(() => {
      props.header = '';
      props.dispatch = jest.fn();
      props.onClickCancel = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'modalDialogChange');
      actionsApp.changeRoute = jest.fn(() => 'changRoute');
      actionsPreferences.getTechnologyByClientModel = jest.fn(() => 'getTechnologyByClientModel');
    });

    beforeEach(() => {
      wrapper = shallow(<CreateVersionDialog
        {...props}
      />);
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('componentDidMount:', () => {
      test('should dispatch action to save vectorizer', () => {
        wrapper.instance().componentDidMount();
        expect(actionsPreferences.getTechnologyByClientModel).toHaveBeenCalled();
        expect(props.dispatch).toHaveBeenCalledWith('getTechnologyByClientModel');
      });
    });

    describe('onClickCancel:', () => {
      test('should onClickCancel call click handler', () => {
        wrapper.instance().onClickCancel();
        expect(props.dispatch).toHaveBeenCalledWith('modalDialogChange');
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
      });
    });

    describe('onDigital:', () => {
      test('set state onDigital', () => {
        wrapper.setState({
          digital: false,
          speech: false,
        });
        wrapper.instance().onDigital();
        expect(wrapper.state().digital).toBe(true);
        expect(wrapper.state().speech).toBe(false);
      });
    });

    describe('onSpeech:', () => {
      test('set state onSpeech', () => {
        wrapper.setState({
          digital: false,
          speech: false,
        });
        wrapper.instance().onSpeech();
        expect(wrapper.state().digital).toBe(false);
        expect(wrapper.state().speech).toBe(true);
      });
    });

    describe('onClickOk:', () => {
      test('should select model name from options for digital', () => {
        const {
          clientId, history, projectId, dispatch,
        } = props;
        wrapper.setState({
          digital: true,
          speech: false,
        });
        wrapper.instance().onClickOk();
        expect(dispatch).toHaveBeenCalledWith('modalDialogChange');
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.CREATEMODEL, { clientId, projectId }, history);
      });

      test('should select model name from options for speech', () => {
        const {
          clientId, history, projectId, dispatch,
        } = props;
        wrapper.setState({
          speech: true,
          digital: false,
        });
        wrapper.instance().onClickOk();
        expect(dispatch).toHaveBeenCalledWith('modalDialogChange');
        expect(actionsApp.changeRoute).toHaveBeenCalledWith(RouteNames.CREATESPEECHMODEL, { clientId, projectId }, history);
      });
    });
  });
});
