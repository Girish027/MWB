import React from 'react';
import { mount, shallow } from 'enzyme';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router';
import store from 'state/configureStore';
import toJSON from 'enzyme-to-json';
import MainLayout from 'layouts/MainLayout';

describe('<MainLayout />', () => {
  const { featureFlags } = global.uiConfig;
  const clientList = [{
    cid: 'cltBEOPMGPPLOMXCUI47',
    name: '247ai Referencebot',
    itsClientId: '247ai',
    itsAppId: 'referencebot',
    itsAccountId: '247aireferencebot',
    isVertical: false,
    deploymentModule: 'clients--247ai--applications--referencebot--models',
    state: 'ENABLED',
    id: '151',
    createdAt: 1524158751174,
    modifiedAt: 1591175798246,
    offset: 0,
    standardClientName: 'tfsai',
    clientDisplayName: '247 ai',
    totalCount: 432,
  }, {
    cid: 'cltBEOPMGPPLOMXCUI48',
    name: '247ai RetailBot',
    itsClientId: '247ai',
    itsAppId: 'retailbot',
    itsAccountId: '247airetailbot',
    isVertical: false,
    deploymentModule: 'clients--247ai--applications--retailbot--models',
    state: 'ENABLED',
    id: '152',
    createdAt: 1524158751174,
    modifiedAt: 1591175798246,
    offset: 0,
    standardClientName: 'tfsai',
    clientDisplayName: '247 ai',
    totalCount: 432,
  }];
  let props = {
    dispatch: jest.fn(),
    projects: {
      16: {
        id: '16',
        clientId: '151',
        name: 'New Model',
        created: 1593161543648,
        locale: 'en-US',
        vertical: 'FINANCIAL',
      },
      17: {
        id: '17',
        clientId: '151',
        name: 'New Model2',
        created: 1593579120820,
        locale: 'en-US',
        vertical: 'FINANCIAL',
      },
    },
    routeClientId: 'tfsai',
    routeAppId: 'referencebot',
    project: {
      id: '17',
      clientId: '151',
      name: 'New Model2',
      created: 1593579120820,
      locale: 'en-US',
      vertical: 'FINANCIAL',
    },
    clientList,
    selectedClient: clientList[0],
    userFeatureConfiguration: featureFlags.DEFAULT,
    showSidebar: true,
  };

  const getShallowWrapper = (propsObj) => shallow(<Provider store={store}>
    <MainLayout
      {...propsObj}
    />
  </Provider>);

  describe('Creating an instance', () => {
    let wrapper;

    beforeAll(() => {
      wrapper = mount(<Provider store={store}>
        <MemoryRouter
          initialEntries={['/']}
          initialIndex={0}
        >
          <MainLayout
            match={{
              path: '/',
              url: '/',
              params: {
                projectId: 5,
              },
            }}
            location={{
              search: '?clientid=247ai&projectid=15&appid=undefined',
            }}
          >
            <div>Hello World</div>
          </MainLayout>
        </MemoryRouter>
      </Provider>);
    });

    test('should exist', () => {
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Snapshots:', () => {
    test('should render correctly', () => {
      const wrapper = getShallowWrapper(props);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });

    test('should render correctly with empty clientList', () => {
      const wrapper = shallow(<Provider store={store}>
        <MainLayout
          {...props}
          clientList={[]}
          showSidebar={false}
        />
      </Provider>);
      expect(toJSON(wrapper)).toMatchSnapshot();
    });
  });
});
