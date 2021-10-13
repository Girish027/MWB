import React from 'react';
import { mount, shallow } from 'enzyme';
import NavigationBar from 'components/Navigation/NavigationBar';
import Constants from 'constants/Constants';
import toJSON from 'enzyme-to-json';

describe('<NavigationBar />', () => {
  let wrapper;
  let props = {
    userName: 'test user',
    clientList: [{
      id: 1,
      itsClientId: '247ai',
      itsAppId: 'aisha',
      standardClientName: 'tfsai',
    }, {
      id: 2,
      itsClientId: '247ai',
      itsAppId: 'referencebot',
      standardClientName: 'tfsai',
    }, {
      id: 3,
      itsClientId: '247inc',
      itsAppId: 'default',
      standardClientName: 'tfsinc',
    }],
    onSelectClient: jest.fn(),
    selectedClient: {
      id: 1,
      itsClientId: '247ai',
      itsAppId: 'aisha',
      standardClientName: 'tfsai',
    },
    userGroup: ['IAT_INTERNAL', 'MWB_CLIENT_ADMIN'],
  };

  let testProps = {
    userName: 'test user',
    clientList: [{
      id: 1,
      itsClientId: '247ai',
      itsAppId: 'aisha',
      standardClientName: 'tfsai',
    }, {
      id: 2,
      itsClientId: '247ai',
      itsAppId: 'referencebot',
      standardClientName: 'tfsai',
    }, {
      id: 3,
      itsClientId: '247inc',
      itsAppId: 'default',
      standardClientName: 'tfsinc',
    }],
    selectedClient: {},
    onSelectClient: jest.fn(),
    clientId: '23',
    userGroup: ['IAT_INTERNAL', 'MWB_CLIENT_ADMIN'],
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Creating an instance', () => {
    beforeEach(() => {
      wrapper = mount(
        <NavigationBar {...props} />,
      );
    });
    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    beforeEach(() => {
      wrapper = shallow(
        <NavigationBar {...props} />,
      );
    });
    test('should render correctly', () => {
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('Nav bar toolName should render correctly when user having access to ITS.', () => {
      wrapper = mount(
        <NavigationBar {...props} />,
      );
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('Nav bar toolName should render correctly when user having access to MWB.', () => {
      const userGroup = ['MWB_CLIENT_ADMIN'];
      wrapper = mount(
        <NavigationBar {...props} userGroup={userGroup} />,
      );
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('Nav bar toolName should render correctly when user having access to MWB.', () => {
      wrapper = mount(
        <NavigationBar {...testProps} />,
      );
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });

  describe('Functionality:', () => {
    beforeAll(() => {
      props = {
        ...props,
        dispatch: jest.fn(),
        ufpURL: 'http://example.com/home',
      };
    });

    beforeEach(() => {
      wrapper = shallow(
        <NavigationBar {...props} />,
      );
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    describe('constructor:', () => {
      test('should populate the clientsConfig if clientList is available', () => {
        expect(wrapper.instance().clientsConfig).toMatchSnapshot();
      });

      test('should not populate the clientsConfig if clientList is not available', () => {
        wrapper = shallow(
          <NavigationBar {...props} clientList={[]} />,
        );
        expect(wrapper.instance().clientsConfig).toEqual({
          title: Constants.CLIENT_PICK_TITLE,
          items: [],
        });
      });
    });

    describe('getIconLink:', () => {
      test('should return icon link', () => {
        wrapper.instance().getIconLink();
        expect(toJSON(wrapper)).toMatchSnapshot();
      });
    });

    describe('getDerivedStateFromProps:', () => {
      test('should update the state data - clientName and AppName from the current selectedClient', () => {
        const selectedClient = { id: 4, itsClientId: 'modelingworkbench', itsAppId: 'default' };
        wrapper.setState({
          displayClientName: ['247ai', 'referencebot'],
          showLogoutWarning: true,
        });
        const newState = NavigationBar.getDerivedStateFromProps({ selectedClient }, wrapper.state());
        expect(newState.displayClientName).toEqual(['Modelingworkbench', 'Default']);
      });

      test('should return null when itsClientId is not there', () => {
        const selectedClient = {};
        const newState = NavigationBar.getDerivedStateFromProps({ selectedClient });
        expect(newState).toBeNull();
      });
    });

    describe('getClientAppConfigArray:', () => {
      test('should construct the clientsConfig when it is not present and clientList is available', () => {
        const existingClientsConfig = { title: Constants.CLIENT_PICK_TITLE, items: [] };
        const clientsConfig = wrapper.instance().getClientAppConfigArray(existingClientsConfig);
        expect(clientsConfig).toMatchSnapshot();
      });

      test('should not construct the clientsConfig when it is not present and clientList is not available', () => {
        wrapper = shallow(
          <NavigationBar {...props} clientList={[]} />,
        );
        const clientsConfig = wrapper.instance().getClientAppConfigArray({});
        expect(clientsConfig).toEqual({});
      });

      test('should not construct the clientsConfig when it is already present', () => {
        const existingClientsConfig = {
          title: Constants.CLIENT_PICK_TITLE,
          items: [{
            title: '247.ai',
            field: 'clientId',
            data: {
              clientId: '247.ai',
            },
            items: [{
              title: 'referencebot',
              field: 'appId',
              data: {
                appId: 'referencebot',
              },
            }, {
              title: 'aisha',
              field: 'appId',
              data: {
                appId: 'aisha',
              },
            }],
          }, {
            title: '247inc',
            field: 'clientId',
            data: {
              clientId: '247inc',
            },
            items: [{
              title: 'referencebot',
              field: 'appId',
              data: {
                appId: 'referencebot',
              },
            }],
          }],
        };
        const clientsConfig = wrapper.instance().getClientAppConfigArray(existingClientsConfig);
        expect(clientsConfig).toEqual(existingClientsConfig);
      });
    });

    describe('componentDidUpdate:', () => {
      let getClientAppConfigArraySpy;

      afterEach(() => {
        getClientAppConfigArraySpy.mockRestore();
      });

      test('should try and construct the client config', () => {
        getClientAppConfigArraySpy = jest.spyOn(wrapper.instance(), 'getClientAppConfigArray');
        wrapper.instance().componentDidUpdate();
        expect(getClientAppConfigArraySpy).toHaveBeenCalledWith(wrapper.instance().clientsConfig);
      });
    });

    describe('onClientPickerSelect:', () => {
      test('should set the state to match the new client app information', () => {
        wrapper.setState({
          showClientPicker: true,
          selectedClientConfig: { clientId: '247inc', appId: 'referencebot', title: ['247inc', 'referencebot'] },
          displayClientName: ['247inc', 'referencebot'],
        });
        const selectedClientConfig = { clientId: '247ai', appId: 'aisha', title: ['247ai', 'aisha'] };
        wrapper.instance().onClientPickerSelect(selectedClientConfig);
        expect(wrapper.state().showClientPicker).toEqual(false);
        expect(wrapper.state().selectedClientConfig).toEqual({ clientId: '247ai', appId: 'aisha' });
        expect(wrapper.state().displayClientName).toEqual(['247ai', 'Aisha']);
      });

      test('should pass the selected client and app to parent', () => {
        wrapper.setState({
          showClientPicker: true,
          selectedClientConfig: { clientId: '247inc', appId: 'referencebot', title: ['247inc', 'referencebot'] },
          displayClientName: ['247inc', 'referencebot'],
        });
        const selectedClientConfig = {
          clientId: '247ai', appId: 'aisha', standardClientId: 'tfsai', title: ['247ai', 'aisha'],
        };
        wrapper.instance().onClientPickerSelect(selectedClientConfig);
        expect(props.onSelectClient).toHaveBeenCalledWith('tfsai', 'aisha');
      });
    });

    describe('onClickLogoutOkta:', () => {
      test('should dispatch logout action', () => {
        wrapper.instance().onClickLogoutOkta();
        expect(props.dispatch).toHaveBeenCalled;
      });
    });

    describe('onCancel:', () => {
      test('should update showClientPicker state to false', () => {
        wrapper.setState({
          showClientPicker: true,
        });
        wrapper.instance().onCancel();
        expect(wrapper.state().showClientPicker).toEqual(false);
      });
    });

    describe('onClickLogout:', () => {
      test('should update showLogoutWarning state to true on onClickLogout click', () => {
        wrapper.setState({
          showLogoutWarning: false,
        });
        wrapper.instance().onClickLogout();
        expect(wrapper.state().showLogoutWarning).toEqual(true);
      });
    });

    describe('hideLogoutDialog:', () => {
      test('should update showLogoutWarning state to false on hideLogoutDialog click', () => {
        wrapper.setState({
          showLogoutWarning: true,
        });
        wrapper.instance().hideLogoutDialog();
        expect(wrapper.state().showLogoutWarning).toEqual(false);
      });
    });

    describe('onClickClient:', () => {
      test('should update showClientPicker state to true', () => {
        wrapper.setState({
          showClientPicker: false,
        });
        wrapper.instance().onClickClient();
        expect(wrapper.state().showClientPicker).toEqual(true);
      });
    });

    describe('onClickSettings:', () => {
      test('should call dispatch', () => {
        wrapper.instance().onClickSettings();
        expect(props.dispatch).toHaveBeenCalled;
      });
    });
  });
});
