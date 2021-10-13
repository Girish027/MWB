jest.mock('utils/api');

import api from 'utils/api';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import getUrl, { pathKey } from 'utils/apiUrls';
import * as actionsProjects from 'state/actions/actions_projects';

const middlewares = [thunk];
const reduxMockStore = configureMockStore(middlewares);


describe('actions_projects', () => {
  let store;
  let data;
  let clientId = '123';
  let mockPush;
  let testHistory;
  const startIndex = 0;
  const limit = 100;
  let projects = [];
  let projectId = '123';

  beforeAll(() => {
    store = reduxMockStore({
      app: {
        userId: 'testUser',
      },
      header: {
        client: {
          id: '14',
        },
      },
      projectListSidebar: {
        projectById: {
          123: {
            name: 'OldName',
          },
        },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    store.clearActions();
  });

  describe('createProject:', () => {
    beforeAll(() => {
      data = {
        values: {
          name: 'TestProject',
          vertical: 'FINANCIAL',
          description: 'TestProjectUpdated',
          locale: 'en-US',
          inheritTags: 'djkskd',
        },
        clientId: '14',
      };
      mockPush = jest.fn();
      testHistory = {
        push: mockPush,
      };
      clientId = '14';
    });

    test('should do post call to create project with required data', () => {
      store.dispatch(actionsProjects.createProject(data));
      expect(api.post).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        data: expect.any(Object),
        onApiSuccess: expect.any(Function),
        onApiError: actionsProjects.onCreateProjectFailure,
      });
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsProjects.createProject(data));
      const url = api.post.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onCreateProjectSuccess:', () => {
    let response = {
      name: 'TestProject',
    };

    test('should dispatch actions to create project and display good request message on project create success', () => {
      store.dispatch(actionsProjects.onCreateProjectSuccess(response, clientId, testHistory));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to create project and display good request message on project create success without data', () => {
      store.dispatch(actionsProjects.onCreateProjectSuccess(undefined, clientId, testHistory));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onCreateProjectFailure:', () => {
    let error = {
      message: 'Unable to create project',
    };

    let errorMsg = {
      ...error, code: 409,
    };
    test('should dispatch actions to display bad request message when project creation failed', () => {
      store.dispatch(actionsProjects.onCreateProjectFailure(error));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to display warning message when project creation failed', () => {
      store.dispatch(actionsProjects.onCreateProjectFailure(errorMsg));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to display warning message when project creation failed without data', () => {
      store.dispatch(actionsProjects.onCreateProjectFailure());
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('deleteProjectById:', () => {
    test('should do delete call to delete project with required data', () => {
      store.dispatch(actionsProjects.deleteProjectById(projectId));
      expect(api.delete).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do delete call with required data', () => {
      store.dispatch(actionsProjects.deleteProjectById(projectId));
      const url = api.delete.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onDeleteProjectSuccess:', () => {
    test('should dispatch actions to delete project and display good request message on project delete success', () => {
      store.dispatch(actionsProjects.onDeleteProjectSuccess(projectId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('updateByProject:', () => {
    beforeAll(() => {
      data = {
        values: {
          name: 'TestProject',
          vertical: 'FINANCIAL',
          description: 'TestProjectUpdated',
          locale: 'en-US',
        },
        clientId: '14',
        projectId: '123',
      };
    });

    test('should do patch call to update project with required data', () => {
      store.dispatch(actionsProjects.updateByProject(data));
      expect(api.patch).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        headers: expect.any(Object),
        data: expect.any(Array),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do patch call to update project without data', () => {
      const dataMock = {
        values: {},
        clientId: '14',
        projectId: '123',
      };
      store.dispatch(actionsProjects.updateByProject(dataMock));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should do post call with required data', () => {
      store.dispatch(actionsProjects.updateByProject(data));
      const url = api.patch.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onUpdateProjectSuccess:', () => {
    let response = [{
      id: '123',
      name: 'TestProject',
    }];

    test('should dispatch actions to update project status and display good request message on project update success', () => {
      store.dispatch(actionsProjects.onUpdateProjectSuccess(response, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to update project status and display good request message on project update success without response available', () => {
      store.dispatch(actionsProjects.onUpdateProjectSuccess(undefined, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('promoteProjectById:', () => {
    beforeAll(() => {
      data = {
        projectId: '234',
        globalProjectId: '123',
        globalProjectName: 'Root_Intent',
      };
    });

    test('should do put call to promote project with required data', () => {
      store.dispatch(actionsProjects.promoteProjectById(data));
      expect(api.put).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do put call with required data', () => {
      store.dispatch(actionsProjects.promoteProjectById(data));
      const url = api.put.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onPromoteProjectSuccess:', () => {
    let response = [{
      id: '123',
      name: 'TestProject',
      type: 'NODE',
    }, {
      id: '234',
      name: 'Root_Intent',
      type: 'GLOBAL',
    }];

    test('should dispatch actions to promote project to global model and demote already existing project to node level model', () => {
      store.dispatch(actionsProjects.onPromoteProjectSuccess(response, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to promote project to global model and demote already existing project to node level model without data available', () => {
      store.dispatch(actionsProjects.onPromoteProjectSuccess(undefined, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('demoteProjectById:', () => {
    test('should do put call to demote project with projectId', () => {
      store.dispatch(actionsProjects.demoteProjectById(projectId));
      expect(api.put).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url: expect.any(String),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });

    test('should do put call with projectId', () => {
      store.dispatch(actionsProjects.demoteProjectById(projectId));
      const url = api.put.mock.calls[0][0].url;
      expect(url).toMatchSnapshot();
    });
  });

  describe('onProjectFailure:', () => {
    let error = {
      message: 'Unable to perform project action',
    };
    let errorMsg = {
      code: 409,
      message: 'Unable to perform project action',
    };

    test('should dispatch actions to set failed status and display bad request message when project operation failed', () => {
      store.dispatch(actionsProjects.onProjectFailure(error, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to set warning status and display bad request message when project operation failed', () => {
      store.dispatch(actionsProjects.onProjectFailure(errorMsg, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to set failed status and display bad request message when project operation failed without data available', () => {
      store.dispatch(actionsProjects.onProjectFailure(undefined, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('onMarkDeployableModelFailure:', () => {
    let error = {
      message: 'Unable to perform project action',
    };
    let modelVersion = '12';

    test('should dispatch actions to set failed status and display bad request message when mark for deploy operation failed', () => {
      store.dispatch(actionsProjects.onMarkDeployableModelFailure(error, modelVersion));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to set failed status and display bad request message when mark for deploy operation failed without data', () => {
      store.dispatch(actionsProjects.onMarkDeployableModelFailure(undefined, modelVersion));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('markDeployableModel:', () => {
    beforeAll(() => {
      data = {
        clientId: '123',
        projectId: '456',
        modelId: '789',
      };
    });

    test('should do patch call to mark for deploy with required data', () => {
      store.dispatch(actionsProjects.markDeployableModel(data));

      const url = getUrl(pathKey.projectById, { clientId: '123', projectId: '456' });

      expect(api.patch).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url,
        headers: expect.any(Object),
        data: expect.any(Array),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });
  });

  describe('unmarkDeployableModel:', () => {
    beforeAll(() => {
      data = {
        clientId: '123',
        projectId: '456',
        modelId: null,
      };
    });

    test('should do patch call to unmark for deploy', () => {
      store.dispatch(actionsProjects.unmarkDeployableModel(data));

      const url = getUrl(pathKey.projectById, { clientId: '123', projectId: '456' });

      expect(api.patch).toHaveBeenCalledWith({
        dispatch: expect.any(Function),
        getState: expect.any(Function),
        url,
        headers: expect.any(Object),
        data: expect.any(Array),
        onApiSuccess: expect.any(Function),
        onApiError: expect.any(Function),
      });
    });
  });

  describe('onUnmarkDeployableModelFailure:', () => {
    let error = {
      message: 'Unable to perform project action',
    };
    let modelVersion = '12';

    test('should dispatch actions to set failed status and display bad request message when unmark operation failed', () => {
      store.dispatch(actionsProjects.onUnmarkDeployableModelFailure(error, modelVersion));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should dispatch actions to set failed status and display bad request message when unmark operation failed without data', () => {
      store.dispatch(actionsProjects.onUnmarkDeployableModelFailure(undefined, modelVersion));
      expect(store.getActions()).toMatchSnapshot();
    });
  });

  describe('<Function call />', () => {
    test('should call requestProject', () => {
      store.dispatch(actionsProjects.requestProject('123'));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call toggleSidebarVisible', () => {
      store.dispatch(actionsProjects.toggleSidebarVisible(true));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call requestAllProjects', () => {
      store.dispatch(actionsProjects.requestAllProjects());
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call requestProjectsByClient', () => {
      store.dispatch(actionsProjects.requestProjectsByClient({ clientId, startIndex, limit }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call receiveProjectsByClient', () => {
      store.dispatch(actionsProjects.receiveProjectsByClient({
        projects, clientId, startIndex, limit,
      }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call sidebarSelectProject', () => {
      store.dispatch(actionsProjects.sidebarSelectProject({ projectId }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call collapsibleSidebarSelect', () => {
      store.dispatch(actionsProjects.collapsibleSidebarSelect(true));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call projectListLoadedSuccess', () => {
      store.dispatch(actionsProjects.projectListLoadedSuccess([]));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call addDatasetToProjectSuccess', () => {
      store.dispatch(actionsProjects.addDatasetToProjectSuccess('Dataset added', { id: '123' }, projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call fetchProject', () => {
      store.dispatch(actionsProjects.fetchProject(projectId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call refreshProjectsByClient', () => {
      store.dispatch(actionsProjects.refreshProjectsByClient(clientId));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call fetchProjectsByClient', () => {
      store.dispatch(actionsProjects.fetchProjectsByClient({ clientId, startIndex, limit }));
      expect(store.getActions()).toMatchSnapshot();
    });

    test('should call fetchAllProjects', () => {
      store.dispatch(actionsProjects.fetchAllProjects());
      expect(store.getActions()).toMatchSnapshot();
    });
  });
});
