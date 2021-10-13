import React from 'react';
import { mount, shallow } from 'enzyme';
import toJSON from 'enzyme-to-json';
import Constants from 'constants/Constants';
import * as appActions from 'state/actions/actions_app';
import {
  Pencil,
  Tag,
} from '@tfs/ui-components';

import AppHeader from 'components/AppHeader/AppHeader';

describe('<AppHeader />', () => {
  let wrapper;
  let props = {
    dispatch: () => {},
    selectedClient: {
      id: 1,
      itsClientId: '247ai',
      itsAppId: 'aisha',
    },
    match: {
      path: '/projects',
      url: '/projects',
      params: {
        projectId: 5,
      },
    },
    itsURL: 'https://its/homepage',
  };

  const propsWithModelId = {
    ...props,
    app: {
      notificationType: 'error',
      serverMessage: 'server down',
    },
    selectedProject: {
      id: '3',
      name: 'test project',
    },
    modelId: '3',
    match: {
      path: '/models/test',
      url: '/models/test',
      params: {
        projectId: 5,
      },
    },
  };

  const propsWithActionItems = {
    ...propsWithModelId,
    app: {
      notificationType: 'success',
      serverMessage: 'Model is tuned',
    },
    actionItems: [{
      label: Constants.REVIEW_DATASETS,
      icon: Tag,
      onClick: jest.fn(),
    }, {
      label: Constants.TUNE_MODEL,
      icon: Pencil,
      onClick: jest.fn(),
    },
    ],
  };

  // props with breadcrumps and action item
  const breadcrumb = [
    {
      value: 'app-overview',
      label: 'Bot Overview',
      onClick: () => {},
    },
    {
      value: 'manage-models',
      label: 'Manage Models',
    },
    {
      value: 'utterance-test',
      label: 'Utterance test',
      onClick: () => {},
    },
  ];
  const testProps = {
    ...propsWithModelId,
    actionItems: [{
      label: Constants.BUILD_MODEL,
      name: 'build-model',
      onClick: jest.fn(),
      disabled: true,
    }],
    breadcrumb,
    title: 'Create a New Version',
    showBackButton: false,
  };

  const testPropsForCancel = {
    ...propsWithModelId,
    actionItems: [{
      label: Constants.CANCEL,
      name: 'CANCEL',
      onClick: jest.fn(),
    }],
  };

  const testPropsForCreateVersion = {
    ...propsWithModelId,
    actionItems: [{
      label: Constants.BUILD_VERSION,
      name: 'build-version',
      onClick: jest.fn(),
      disabled: true,
      type: 'primary',
    }],
    breadcrumb,
    title: 'Create a New Version',
    showBackButton: false,
  };

  beforeEach(() => {
    wrapper = mount(<AppHeader {...props} />);
  });

  describe('Creating an instance:', () => {
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    test('should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('verify Header Subtitle ', () => {
      expect(wrapper.instance().subtitleMap).toMatchSnapshot();
    });

    test('should render correctly on test page', () => {
      wrapper = mount(<AppHeader {...propsWithModelId} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly on with action items', () => {
      wrapper = shallow(<AppHeader {...propsWithActionItems} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly on with action items and breadcrumps', () => {
      wrapper = shallow(<AppHeader {...testProps} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render Cancel Button correctly along with action items', () => {
      wrapper = shallow(<AppHeader {...testPropsForCancel} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render Create Version Button correctly along with action items', () => {
      wrapper = shallow(<AppHeader {...testPropsForCreateVersion} />);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeEach(() => {
      props.dispatch = jest.fn();
      appActions.stopShowingServerMessage = jest.fn(() => 'stopShowingServerMessage');
    });

    describe('closeNotification:', () => {
      test('should close the notification on closeNotification click', () => {
        wrapper = shallow(<AppHeader {...props} />);
        wrapper.instance().closeNotification();
        expect(appActions.stopShowingServerMessage).toHaveBeenCalledWith();
        expect(props.dispatch).toHaveBeenCalledWith('stopShowingServerMessage');
      });
    });

    describe('getSubtitle:', () => {
      test('should return the header subtitle for Tag Datasets page', () => {
        const sub = wrapper.instance().getSubtitle({
          url: '/tag-datasets',
        });
        expect(sub).toEqual('Tag Datasets');
      });

      test('should return the header subtitle Projects/Datasets/Models/Landing pages as \'Models & Datasets\'', () => {
        const modelsAndDatasets = Constants.MANAGE_MODELS_DATASETS;
        let sub;
        sub = wrapper.instance().getSubtitle({
          url: '/datasets',
        });
        expect(sub).toEqual(modelsAndDatasets);

        sub = wrapper.instance().getSubtitle({
          url: '/manage-intents',
        });
        expect(sub).toEqual(modelsAndDatasets);

        sub = wrapper.instance().getSubtitle({
          url: '/models',
        });
        expect(sub).toEqual(modelsAndDatasets);

        sub = wrapper.instance().getSubtitle({
          url: '/projects',
        });
        expect(sub).toEqual(modelsAndDatasets);

        sub = wrapper.instance().getSubtitle({
          url: '/',
        });
        expect(sub).toEqual(modelsAndDatasets);
      });

      test('should return the header subtitle for Update Model page', () => {
        const sub = wrapper.instance().getSubtitle({
          url: '/projects/update',
        });
        expect(sub).toEqual('Update Model');
      });
    });
  });
});
