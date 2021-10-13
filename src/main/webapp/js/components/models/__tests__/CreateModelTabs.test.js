import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Constants from 'constants/Constants';
import * as actionsTagDatasets from 'state/actions/actions_tag_datasets';
import * as actionsApp from 'state/actions/actions_app';
import { RouteNames } from 'utils/routeHelpers';
import * as headerActions from 'state/actions/actions_header';
import { CreateModelTabsComponent } from 'components/models/CreateModelTabs';

describe('<CreateModelTabsComponent />', () => {
  const initialState = {
    activeTab: 'Basic Info*',
    lastTab: 'Basic Info*',
  };

  const actionItems = [{
    label: Constants.BUILD_MODEL,
    name: 'build-model',
    onClick: jest.fn(),
    disabled: true,
  },
  ];

  const testProps = {
    dispatch: () => {},
    history: {},
    actionItems,
    client: {
      cid: 'cltBEOPMGPPLOMXCUI47',
      createdAt: 1524158751174,
      id: '151',
    },
    clientDataLoaded: true,
    clientId: '151',
    config: {},
    model: {
      description: 'myDesc',
    },
    isDatasetsTagged: true,
    isDatasetsValid: true,
    isConfigsValid: true,
    createNewModel: {
      description: 'myNewDesc',
    },
    projectId: '1109',
    viewModelId: null,
    tuneModelId: '123',
  };

  let wrapper;

  afterAll(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    test('should exist', () => {
      wrapper = shallow(<CreateModelTabsComponent
        {...testProps}
      />);
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots', () => {
    beforeEach(() => {
      wrapper = shallow(<CreateModelTabsComponent
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
    beforeAll(() => {
      actionsTagDatasets.setIncomingFilter = jest.fn(() => 'called setIncomingFilter');
      actionsApp.changeRoute = jest.fn(() => 'called changeRoute');
      headerActions.setActionItems = jest.fn(() => 'called setActionItems');
    });

    beforeEach(() => {
      wrapper = shallow(<CreateModelTabsComponent
        {...testProps}
      />);
      wrapper.setState({
        ...initialState,
        validModel: true,
      });
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('onClickNext', () => {
      test('should render the next tab', () => {
        wrapper.instance().onClickNext();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('buildModelWithConfig', () => {
      test('should render buildModelWithConfig on clicking CREATE VERSION button', () => {
        wrapper.instance().buildModelWithConfig();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('renderActionItem', () => {
      test('should render renderActionItem correctly', () => {
        wrapper.instance().renderActionItem();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('confirmModelWithConfig', () => {
      test('should render confirmModelWithConfig while clicking on CREATE VERSION button', () => {
        wrapper.instance().confirmModelWithConfig();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('handleCancel', () => {
      test('should render handleCancel while clicking on CANCEL button', () => {
        wrapper.instance().handleCancel();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('tuneModel', () => {
      test('should render tuneModel function while tuning model', () => {
        wrapper.instance().tuneModel();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onTabSelected', () => {
      test('should render the selected tab', () => {
        wrapper.instance().onTabSelected(wrapper.instance().tabIds.overview, 4);
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('onClickNext', () => {
      test('should render the Preprocessing Tab if N-Gram is selected in Basic Info Tab', () => {
        wrapper.setState({ hideModelTransformationsTab: false });
        wrapper.instance().onClickNext();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
      test('should hide the Preprocessing Tab if Tensorflow is selected in Basic Info Tab', () => {
        wrapper.setState({ hideModelTransformationsTab: true });
        wrapper.instance().onClickNext();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getTabs', () => {
      test('should render the Preprocessing Tab if N-Gram is selected in Basic Info Tab', () => {
        const props = { ...testProps, model: { technology: 'N-GRAM' } };
        wrapper = shallow(<CreateModelTabsComponent
          {...props}
        />);
        wrapper.instance().getTabs();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
      test('should hide the Preprocessing Tab if Tensorflow is selected in Basic Info Tab', () => {
        const props = { ...testProps, model: { technology: 'USE' } };
        wrapper = shallow(<CreateModelTabsComponent
          {...props}
        />);
        wrapper.instance().getTabs();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });
  });
});
