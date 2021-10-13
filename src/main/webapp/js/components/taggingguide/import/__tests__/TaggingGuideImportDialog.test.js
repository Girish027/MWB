import React from 'react';
import { shallow, mount } from 'enzyme';
import { Provider } from 'react-redux';
import store from 'state/configureStore';
import * as actionsApp from 'state/actions/actions_app';
import * as actionsImport from 'state/actions/actions_taggingguideimport';
import ReduxTaggingGuideImportDialog from 'components/taggingguide/import/TaggingGuideImportDialog';
import { TaggingGuideImportDialog } from 'components/taggingguide/import/TaggingGuideImportDialog';
import toJSON from 'enzyme-to-json';

describe('<TaggingGuideImportDialog />', () => {
  let wrapper;
  const props = {
    dispatch: jest.fn(),
    csrfToken: 'dffs',
    userName: 'testUser',
    project: {
      id: '12',
      name: 'project1',
    },
    clientId: '123',
    taggingGuideImport: {
      step: 'mapping',
      done: true,
      token: 'dasda',
      validTagCount: 12,
      isBindingValid: true,
      bindingArray: [],
      skipFirstRow: true,
      previewData: [],
      columnsBinding: { column1: 0 },
      columns: [{ name: 'column1', required: true }, { name: 'column2', required: true }],
      isMappingRequestLoading: false,
      missingTags: ['dsfsdf'],
      isCommitRequestLoading: false,
      isPreSelected: true,
      invalidTags: ['sadsd'],
    },
  };

  afterAll(() => {
    jest.clearAllMocks();
  });

  const getShallowWrapper = (propsObj) => shallow(<TaggingGuideImportDialog
    {...propsObj}
  />);

  describe('Creating an instance', () => {
    beforeAll(() => {
      wrapper = shallow(<Provider store={store}>
        <ReduxTaggingGuideImportDialog
          match={{
            params: {
              clientId: 1,
              projectId: 2,
            },
          }}
        />
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = getShallowWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    test('renders correctly for default props', () => {
      wrapper = mount(<TaggingGuideImportDialog
        {...props}
      />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as mapping', () => {
      wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as confirm_ok', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'confirm_ok',
          isPreSelected: false,
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as confirm_table with missingTags', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'confirm_table',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as confirm_table without missingTags', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'confirm_table',
          missingTags: '',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as confirm_delete', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'confirm_delete',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as confirm', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'confirm',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('renders correctly for default props with step as upload', () => {
      const defaultProps = {
        ...props,
        taggingGuideImport: {
          ...props.taggingGuideImport,
          step: 'upload',
        },
      };
      wrapper = getShallowWrapper(defaultProps);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      actionsApp.modalDialogChange = jest.fn(() => 'called modalDialogChange');
      actionsApp.removeFile = jest.fn(() => 'called removeFile');
      actionsImport.columnsBind = jest.fn(() => 'called columnsBind');
      actionsImport.reset = jest.fn(() => 'called reset');
      actionsImport.abort = jest.fn(() => 'called abort');
      actionsImport.changeStep = jest.fn(() => 'called changeStep');
      actionsImport.commit = jest.fn(() => 'called commit');
      actionsImport.requestColumnsBind = jest.fn(() => 'called requestColumnsBind');
      actionsImport.changeFirstRowSkip = jest.fn(() => 'called changeFirstRowSkip');
    });

    describe('onClickCancel', () => {
      test('should call onClickCancel', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().onClickCancel();
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });

    describe('onColumnBindChange', () => {
      test('should call onColumnBindChange', () => {
        const {
          bindingArray, isBindingValid, columnsBinding, isPreSelected,
        } = props.taggingGuideImport;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onColumnBindChange({
          bindingArray, isBindingValid, columnsBinding, isPreSelected,
        });
        expect(actionsImport.columnsBind).toHaveBeenCalledWith({
          bindingArray, isBindingValid, columnsBinding, isPreSelected,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called columnsBind');
      });
    });

    describe('componentWillReceiveProps', () => {
      test('should call componentWillReceiveProps', () => {
        const nextProps = {
          ...props,
        };
        wrapper = getShallowWrapper(props);
        wrapper.instance().componentWillReceiveProps(nextProps);
        expect(actionsApp.modalDialogChange).toHaveBeenCalledWith(null);
        expect(props.dispatch).toHaveBeenCalledWith('called modalDialogChange');
      });
    });

    describe('onChangeStep', () => {
      test('should call onChangeStep', () => {
        const step = 'mapping';
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChangeStep(step);
        expect(actionsImport.changeStep).toHaveBeenCalledWith({ step });
        expect(props.dispatch).toHaveBeenCalledWith('called changeStep');
      });
    });

    describe('onCommit', () => {
      test('should call onCommit', () => {
        const {
          project, clientId, taggingGuideImport,
        } = props;
        const { token } = taggingGuideImport;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onCommit();
        expect(actionsImport.commit).toHaveBeenCalledWith({ projectId: project.id, token, clientId });
        expect(props.dispatch).toHaveBeenCalledWith('called commit');
      });
    });

    describe('onRequestColumnsBind', () => {
      test('should call onRequestColumnsBind', () => {
        const {
          project, taggingGuideImport,
        } = props;
        const { token, bindingArray, skipFirstRow } = taggingGuideImport;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onRequestColumnsBind();
        expect(actionsImport.requestColumnsBind).toHaveBeenCalledWith({
          projectId: project.id,
          token,
          bindingArray,
          skipFirstRow,
          clientId: project.clientId,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called requestColumnsBind');
      });
    });

    describe('onChangeFirstRowSkip', () => {
      test('should call onChangeFirstRowSkip', () => {
        const { taggingGuideImport } = props;
        const { skipFirstRow } = taggingGuideImport;
        wrapper = getShallowWrapper(props);
        wrapper.instance().onChangeFirstRowSkip();
        expect(actionsImport.changeFirstRowSkip).toHaveBeenCalledWith({
          skipFirstRow: !skipFirstRow,
        });
        expect(props.dispatch).toHaveBeenCalledWith('called changeFirstRowSkip');
      });
    });

    describe('componentWillUnmount', () => {
      test('should call componentWillUnmount with done as true', () => {
        wrapper = getShallowWrapper(props);
        wrapper.instance().componentWillUnmount();
        expect(actionsImport.reset).toHaveBeenCalledWith();
        expect(props.dispatch).toHaveBeenCalledWith('called reset');
      });

      test('should call componentWillUnmount woth done as false', () => {
        const defaultProps = {
          ...props,
          taggingGuideImport: {
            ...props.taggingGuideImport,
            done: false,
          },
        };
        wrapper = getShallowWrapper(defaultProps);
        wrapper.instance().componentWillUnmount();
        expect(actionsImport.abort).toHaveBeenCalledWith({ projectId: props.project.id, token: props.taggingGuideImport.token, clientId: props.clientId });
        expect(actionsApp.removeFile).toHaveBeenCalledWith({ fileId: props.taggingGuideImport.token });
        expect(props.dispatch).toHaveBeenCalledWith('called abort');
        expect(props.dispatch).toHaveBeenCalledWith('called removeFile');
      });
    });
  });
});
