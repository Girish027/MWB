import _ from 'lodash';
import * as types from 'state/actions/types';
import Constants from 'constants/Constants';

export const defaultState = {
  clientId: null,
  projects: [],
  projectById: {},
  globalProjectsByName: {},
  selectedProjectId: null,
  loading: false,
  noMoreProjects: false,
  startIndex: 0,
  isGlobalModelOpen: true,
  isNodeModelOpen: false,
};

const validateGlobalName = (name) => {
  const modelNameLowerCase = _.mapValues(Constants.PROJECT_TYPE.GLOBAL.MODELS_NAME, _.method('toLowerCase'));
  return name && Object.values(modelNameLowerCase).includes(name.toLowerCase());
};

function projectListSidebarReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.REQUEST_PROJECTS_BY_CLIENT:
    return Object.assign({}, state, {
      clientId: action.clientId,
      loading: true,
      selectedProjectId: null,
    });

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.RECEIVE_PROJECTS_BY_CLIENT: {
    const { clientId, projects, limit } = action;
    if (clientId === state.clientId) {
      let index;
      let globalProjectsByName = {};
      let projectById = {};
      let clonedProjects = [];
      let selectedProjectId = null;
      projects.forEach(project => {
        projectById[project.id] = project;
        clonedProjects.push(project);
        if (validateGlobalName(project.name)) {
          globalProjectsByName[project.name.toLowerCase()] = project.id;
          if (project.name.toLowerCase() == Constants.ROOT_INTENT) {
            selectedProjectId = project.id;
          }
        }
      });
      let sortedProjects = _.orderBy(clonedProjects, ['createdAt'], ['desc']);
      if (sortedProjects.length > 0 && selectedProjectId == null
        && (!state.selectedProjectId || typeof state.selectedProjectId === 'undefined'
        || state.selectedProjectId === 'undefined')) {
        if (Object.keys(globalProjectsByName).length !== 0) {
          for (let i = 0; i < sortedProjects.length; i++) {
            if (validateGlobalName(sortedProjects[i].name)) {
              selectedProjectId = sortedProjects[i].id;
              break;
            }
          }
        }

        if (selectedProjectId == null) {
          selectedProjectId = sortedProjects[0].id;
        }
      }
      return Object.assign({}, state, {
        projects: sortedProjects,
        projectById,
        globalProjectsByName,
        noMoreProjects: projects.length < limit,
        loading: false,
        startIndex: state.startIndex + limit,
        selectedProjectId,
      });
    }
    return state;
  }

  case types.COLLAPSIBLE_SIDEBAR_SELECT: {
    const { isCollapsibleModelOpen } = action;
    const { isGlobalModelOpen, isNodeModelOpen } = isCollapsibleModelOpen;
    return Object.assign({}, state, {
      isGlobalModelOpen,
      isNodeModelOpen,
    });
  }

  case types.SIDEBAR_SELECT_PROJECT: {
    const { projectId } = action;
    return Object.assign({}, state, {
      selectedProjectId: projectId,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.ADD_NEW_PROJECT: {
    const { project } = action;
    if (!state.projectById[project.id]) {
      state.projects.unshift(action.project);
      state.projectById[project.id] = project;
      return Object.assign({}, state, {
        projects: state.projects,
        projectById: state.projectById,
      });
    }
    return state;
  }

  case types.PROJECT_UPDATE: {
    const { projects } = action;

    if (projects.length > 0) {
      let index,
        i;
      let globalProjectsByName = {};
      let clonedProjectById = _.cloneDeep(state.projectById);
      let clonedProjects = _.cloneDeep(state.projects);

      projects.forEach(project => {
        index = clonedProjects.findIndex(clonedProject => clonedProject.id === project.id);
        if (index > -1) {
          const stateProjectById = clonedProjectById[project.id];
          const stateProject = clonedProjects[index];
          const updatedProjectById = Object.assign({}, stateProjectById, project);
          const updatedProject = Object.assign({}, stateProject, project);
          clonedProjectById[project.id] = updatedProjectById;
          clonedProjects[index] = updatedProject;
        }
      });

      clonedProjects.forEach(project => {
        if (validateGlobalName(project.name)) {
          globalProjectsByName[project.name.toLowerCase()] = project.id;
        }
      });

      return Object.assign({}, state, {
        projects: clonedProjects,
        projectById: clonedProjectById,
        globalProjectsByName,
      });
    }
    return state;
  }
  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECT_DELETE: {
    const { id } = action;
    let clonedProjectById = _.cloneDeep(state.projectById);
    let clonedProjects = _.cloneDeep(state.projects);
    const index = clonedProjects.findIndex(project => project.id === id);
    if (index > -1) {
      clonedProjects.splice(index, 1);
      delete clonedProjectById[id];
      let projectId = null;
      if (clonedProjects.length > 0) {
        projectId = clonedProjects[0].id;
      }
      return Object.assign({}, state, {
        projects: clonedProjects,
        projectById: clonedProjectById,
        selectedProjectId: projectId,
      });
    }
    return state;
  }

  case types.CLEAR_SELECTED_CLIENT:
    return Object.assign({}, defaultState);

  case types.CLIENT_CHANGE:
    return Object.assign({}, state, {
      clientId: action.client.id,
      projects: [],
      projectById: {},
      noMoreProjects: false,
      loading: false,
      startIndex: 0,
    });

  case types.PROJECTS_MANAGER_PROJECT_LOAD_SUCCESS: {
    const { projectId } = action;
    return Object.assign({}, state, {
      selectedProjectId: projectId,
    });
  }

  default:
    return state;
  }
}

export { projectListSidebarReducer };
