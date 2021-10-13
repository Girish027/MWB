
import * as types from 'state/actions/types';
import _ from 'lodash';
import { projectsManagerReducer, defaultState } from 'state/reducers/projectsManager_reducers';

describe('projectsManagerReducer', () => {
  describe('defaultState', () => {
    test('should return the initial state', () => {
      const results = projectsManagerReducer(undefined, {
        type: types.NOOP,
      });
      expect(results).toEqual(defaultState);
    });
  });

  describe('Project Operation', () => {
    let projects = new Map(defaultState.projects);
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
      status: 'RUNNING',
    };

    beforeEach(() => {
      projects.set(project.id, project);
      newState = Object.assign({}, defaultState, {
        projects,
      });
    });

    afterEach(() => {
      newState.projects.delete(project.id);
    });

    test('should add the project', () => {
      const testAction = {
        type: types.ADD_NEW_PROJECT,
        project,
      };
      const results = projectsManagerReducer(newState, testAction);
      expect(results.projects.get(project.id)).toEqual(testAction.project);
    });

    test('should return the updated project', () => {
      const testAction = {
        type: types.PROJECT_UPDATE,
        projects: [{
          id: '1049',
          description: 'eemm',
          locale: 'en-US',
          name: 'Updated Project',
          vertical: 'TECHNICAL',
          deployableModelId: '12345',
        }],
      };
      const results = projectsManagerReducer(newState, testAction);
      expect(results.projects.get(testAction.projects[0].id)).toMatchObject(testAction.projects[0]);
    });

    test('should update the project status', () => {
      const testAction = {
        type: types.PROJECT_STATUS_UPDATE,
        projectId: '1049',
        status: 'COMPLETED',
      };
      const results = projectsManagerReducer(newState, testAction);
      expect(results.projects.get(project.id).status).toEqual(testAction.status);
    });

    test('should delete the project', () => {
      const testAction = {
        type: types.PROJECT_DELETE,
        id: '1049',
      };
      const results = projectsManagerReducer(newState, testAction);
      expect(results.projects.size).toEqual(0);
    });
  });
});
