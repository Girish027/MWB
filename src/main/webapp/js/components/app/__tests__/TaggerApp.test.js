jest.mock('utils/api');

import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import TaggerApp from 'components/app/TaggerApp';
import ReadProject from 'components/projects/ReadProject';
import AppHeader from 'components/AppHeader';
import cookie from 'react-cookies';
import Constants from 'constants/Constants';
import toJSON from 'enzyme-to-json';
import * as appActions from 'state/actions/actions_app';
import SimpleDialog from 'components/controls/SimpleDialog';
import DeleteDialog from 'components/controls/DeleteDialog';
import ProgressDialog from 'components/controls/ProgressDialog';
import PromoteDialog from 'components/controls/PromoteDialog';
import RecordSpeechDialog from 'components/controls/RecordSpeechDialog';
import DatasetColumnMapperDialog from 'components/controls/DatasetColumnMapperDialog';
import AddIntentDialog from 'components/controls/AddIntentDialog';
import CreateModelDialog from 'components/controls/CreateModelDialog';
import DatasetDialog from 'components/controls/DatasetDialog';
import ColumnSelectorDialog from 'components/controls/ColumnSelectorDialog';
import SpeechSelectDatasetDialog from 'components/models/speech/SpeechSelectDatasetDialog';
import TaggingGuideImportDialog from 'components/taggingguide/import/TaggingGuideImportDialog';
import LinkDialog from 'components/controls/LinkDialog';
import UnauthorizedUserDialog from 'components/controls/UnauthorizedUserDialog';
import CreateDatasetDialog from 'components/datasets/create/CreateDatasetDialog';

describe('<TaggerApp />', () => {
  let wrapper;

  let props = {
    dispatch: jest.fn(),
    userFeatureConfiguration: {},
    history: [],
    app: {
      modalDialogState: {
        type: '',
      },
      contextMenuState: {
        type: 'default',
      },
    },
    environment: 'test',
    header: '',
    routeClientId: '',
    routeAppId: '',
  };

  const getMountedWrapper = (propsObj, pathName = '/') => mount(<Provider store={store}>
    <MemoryRouter
      initialEntries={['/']}
      initialIndex={0}
    >
      <TaggerApp
        match={{
          params: {
            clientId: 1,
            projectId: 2,
          },
        }}
        location={{
          pathname: '/manage-intents',
          search: '?clientid=247ai&appid=aisha&projectid=7',
        }}
        {...propsObj}
      />
    </MemoryRouter>
  </Provider>);

  const getShallowWrapper = () => shallow(<Provider store={store}>
    <TaggerApp
      {...props}
    />
  </Provider>);

  describe('Creating an instance', () => {
    beforeAll(() => {
      /* cookie.save('ITS_GROUP', Constants.ITS_GROUP); */
    });

    test('should exist', () => {
      wrapper = getMountedWrapper(props);
      expect(wrapper.exists()).toBe(true);
    });

    test('should contain TaggerApp', () => {
      wrapper = getMountedWrapper(props);
      expect(wrapper.find(TaggerApp).length).toBe(1);
    });

    test('should contain AppHeader', () => {
      wrapper = getMountedWrapper(props);
      expect(wrapper.find(AppHeader).length).toBe(0);
    });

    test('should contain ReadProject', () => {
      wrapper = getMountedWrapper(props);
      expect(wrapper.find(ReadProject).length).toBe(0);
    });

    test('should render the correct title when user having access to MWB', () => {
      wrapper = getMountedWrapper(props);
      wrapper.update();
      expect(document.title).toEqual(Constants.DOCUMENT_TITLE_EC);
    });

    test('should render the correct title when user having access to ITS', () => {
      cookie.save('ITS_GROUP', Constants.ITS_GROUP);
      wrapper = getMountedWrapper(props);
      wrapper.update();
      expect(document.title).toEqual(Constants.DOCUMENT_TITLE_EC);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      appActions.modalDialogChange = jest.fn(() => 'modalDialogChange');
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    test('renders correctly when status completed', () => {
      wrapper = getShallowWrapper();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('getContextMenu', () => {
    test('should render getContextMenu correctly', () => {
      wrapper = shallow(<Provider store={store}>
        <TaggerApp
          {...props}
        />
      </Provider>);
      wrapper.instance().getContextMenu = jest.fn()
        .mockImplementation(() => ({ called: 'getContextMenu' }));
      wrapper.instance().getContextMenu();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('getModalDialog', () => {
    test('should render getModalDialog correctly', () => {
      wrapper = shallow(<Provider store={store}>
        <TaggerApp
          {...props}
        />
      </Provider>);
      wrapper.instance().getModalDialog = jest.fn()
        .mockImplementation(() => ({ called: 'getModalDialog' }));
      wrapper.instance().getModalDialog();
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render SimpleDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.SIMPLE_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <SimpleDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render DeleteDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.DELETE_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <DeleteDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render ProgressDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.PROGRESS_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <ProgressDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render TaggingGuideImportDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.IMPORT_TAGGING_GUIDE,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <TaggingGuideImportDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render CreateDatasetDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.CREATE_DATASET,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <CreateDatasetDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render UnauthorizedUserDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.UNAUTHORIZED_USER,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <UnauthorizedUserDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render SpeechSelectDatasetDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.ADD_SPEECH,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <SpeechSelectDatasetDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render LinkDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.LINK_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <LinkDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render PromoteDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.PROMOTE_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <PromoteDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render ColumnSelectorDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.COLUMNSELECTOR_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <ColumnSelectorDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render RecordSpeechDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.RECORD_SPEECH,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <RecordSpeechDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render AddIntentDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.ADD_INTENT,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <AddIntentDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render CreateModelDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.CREATE_MODEL_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <CreateModelDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render DatasetDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.DATASET_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <DatasetDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render DatasetColumnMapperDialog component according to relevant modalDialogState type', () => {
      const testProps = {
        ...props,
        app: {
          modalDialogState: {
            type: Constants.DIALOGS.DATASET_COLUMN_MAPPING_DIALOG,
          },
        },
      };
      wrapper = shallow(<Provider store={store}>
        <DatasetColumnMapperDialog
          {...testProps}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
