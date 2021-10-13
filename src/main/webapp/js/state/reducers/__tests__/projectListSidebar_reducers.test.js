
import * as types from 'state/actions/types';
import { projectListSidebarReducer, defaultState } from 'state/reducers/projectListSidebar_reducers';

describe('projectListSidebarReducer', () => {
  describe('defaultState', () => {
    test('should return the initial state', () => {
      const results = projectListSidebarReducer(undefined, {
        type: types.NOOP,
      });
      expect(results).toEqual(defaultState);
    });
  });

  describe('ProjectList Operations', () => {
    test('should update the state of collapsible sidebar', () => {
      const testAction = {
        type: types.COLLAPSIBLE_SIDEBAR_SELECT,
        isCollapsibleModelOpen: {
          isGlobalModelOpen: true,
          isNodeModelOpen: true,
        },
      };
      const results = projectListSidebarReducer(undefined, testAction);
      expect(results.isGlobalModelOpen).toEqual(testAction.isCollapsibleModelOpen.isGlobalModelOpen);
      expect(results.isNodeModelOpen).toEqual(testAction.isCollapsibleModelOpen.isNodeModelOpen);
    });

    test('should update the selectedProjectId', () => {
      const testAction = {
        type: types.SIDEBAR_SELECT_PROJECT,
        projectId: '1234',
      };
      const results = projectListSidebarReducer(undefined, testAction);
      expect(results.selectedProjectId).toEqual(testAction.projectId);
    });
  });

  describe('Project Operation', () => {
    let projects = [...defaultState.projects];
    let projectById = { ...defaultState.projectById };
    let newState = {};
    const project = {
      cid: 'cltWDEYCCLMUMNLKJE57',
      clientId: '187',
      createdAt: 1559741969021,
      dbPrefix: 'prj',
      dbid: '00000000000000000000',
      description: 'ee',
      disabled: false,
      groupId: 0,
      id: '1049',
      locale: 'en-US',
      modifiedAt: 1559741969021,
      modifiedBy: 'test@test.com',
      name: 'TestProject',
      state: 'ENABLED',
      vertical: 'FINANCIAL',
      modelCount: 3,
    };
    beforeEach(() => {
      projects[0] = project;
      projectById[project.id] = project;
      newState = Object.assign({}, defaultState, {
        projects,
        projectById,
      });
    });

    afterEach(() => {
      newState.projects.splice(0, 1);
      delete newState.projectById[project.id];
    });

    test('should add the project', () => {
      const testAction = {
        type: types.ADD_NEW_PROJECT,
        project,
      };
      const results = projectListSidebarReducer(undefined, testAction);
      expect(results.projects[0]).toEqual(testAction.project);
      expect(results.projectById[project.id]).toEqual(testAction.project);
    });

    test('should return the updated project', () => {
      const testAction = {
        type: types.PROJECT_UPDATE,
        projects: [{
          id: '1049',
          description: 'dfdkdfnv',
          locale: 'en-US',
          name: 'Updated Project',
          vertical: 'TECHNICAL',
        }],
      };
      const results = projectListSidebarReducer(newState, testAction);
      expect(results.projects[0]).toMatchObject(testAction.projects[0]);
      expect(results.projectById[testAction.projects[0].id]).toMatchObject(testAction.projects[0]);
    });

    test('should delete the project', () => {
      const testAction = {
        type: types.PROJECT_DELETE,
        id: '1049',
      };
      const results = projectListSidebarReducer(newState, testAction);
      expect(results.projects).toEqual([]);
      expect(results.projectById).toEqual({});
    });
  });
});
