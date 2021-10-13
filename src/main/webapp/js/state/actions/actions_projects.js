import fetch from 'isomorphic-fetch';
import _ from 'lodash';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import store from 'state/configureStore';
import * as apiUtils from 'utils/apiUtils';
import getUrl, { pathKey } from 'utils/apiUrls';
import api from 'utils/api';
import { getLanguage } from 'state/constants/getLanguage';
import { RouteNames } from 'utils/routeHelpers';
import Constants from 'constants/Constants';
import Model from 'model';
import * as appActions from './actions_app';
import * as types from './types';

const lang = getLanguage();
const { DISPLAY_MESSAGES } = lang;
// PROJECT

export const requestProject = projectId => ({
  type: types.REQUEST_PROJECT,
  projectId,
});

export const receiveProject = (project, datasets) => ({
  type: types.RECEIVE_PROJECT,
  project,
  datasets,
  receivedAt: Date.now(),
});
// PROJECT LIST

export const toggleSidebarVisible = visible => ({
  type: types.PROJECT_LIST_TOGGLE_VISIBLE,
  visible,
});

export const requestAllProjects = () => ({
  type: types.REQUEST_PROJECTS_ALL,
});

export const receiveAllProjects = projectList => ({
  type: types.RECEIVE_PROJECTS_ALL,
  projectList,
  receivedAt: Date.now(),
});

export const requestProjectsByClient = ({ clientId, startIndex, limit }) => ({
  type: types.REQUEST_PROJECTS_BY_CLIENT,
  clientId,
  startIndex,
  limit,
});

export const receiveProjectsByClient = ({
  projects, clientId, startIndex, limit,
}) => ({
  type: types.RECEIVE_PROJECTS_BY_CLIENT,
  projects,
  clientId,
  startIndex,
  limit,
});

export const sidebarSelectProject = ({ projectId }) => ({
  type: types.SIDEBAR_SELECT_PROJECT,
  projectId,
});

export const collapsibleSidebarSelect = isCollapsibleModelOpen => ({
  type: types.COLLAPSIBLE_SIDEBAR_SELECT,
  isCollapsibleModelOpen,
});

export const projectListLoadedSuccess = newProjectList => ({
  type: types.PROJECT_LIST_LOADED_SUCCESS,
  newProjectList,
});

/* TODO: remove */
export const addDatasetToProjectSuccess = (serverMessage, dataset, projectId) => ({
  type: types.ADD_DATASET_TO_PROJECT_SUCCESS,
  serverMessage,
  dataset,
  projectId,
});

/*
 export const requestDatasetsTransfromationStatuses = (datasetsIds) => {
 return {
 type: types.REQUEST_DATASETS_TRANSFORMATION_STATUSES,
 datasetsIds: datasetsIds
 }
 };
 */
/*
 export const receiveDatasetsTransfromationStatuses = (response) => {
 return {
 type: types.RECEIVE_DATASETS_TRANSFORMATION_STATUSES,
 response: response
 }
 };
 */

export const addNewProject = project => ({
  type: types.ADD_NEW_PROJECT,
  project,
});

export const updateProject = projects => ({
  type: types.PROJECT_UPDATE,
  projects,
});

export const updateProjectStatus = (projectId, status) => ({
  type: types.PROJECT_STATUS_UPDATE,
  projectId,
  status,
});

export const deleteProject = id => ({
  type: types.PROJECT_DELETE,
  id,
});

export const onUpdateProjectSuccess = (response = {}, projectId, message = DISPLAY_MESSAGES.projectUpdated) => (dispatch, getState) => {
  const projects = apiUtils.normalizeIds(response);
  dispatch(updateProject([projects]));
  dispatch(updateProjectStatus(projectId, Constants.STATUS.COMPLETED));
  dispatch(appActions.displayGoodRequestMessage(message));
};

export const onPromoteProjectSuccess = (response = [], projectId) => (dispatch, getState) => {
  const projects = response;
  dispatch(updateProject(projects));
  dispatch(updateProjectStatus(projectId, Constants.STATUS.COMPLETED));
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.projectPromoted));
};

export const onProjectFailure = (error = {}, projectId) => (dispatch, getState) => {
  const { message } = error;
  dispatch(updateProjectStatus(projectId, Constants.STATUS.FAILED));
  if (error.code === 409) {
    dispatch(appActions.displayWarningRequestMessage(message));
  } else {
    dispatch(appActions.displayBadRequestMessage(message));
  }
};

export const onDeleteProjectSuccess = (projectId) => (dispatch, getState) => {
  const deletedProject = Model.ProjectsManager.getProject(projectId) || {};
  const { name = '' } = deletedProject;
  dispatch(deleteProject(projectId));
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.projectDeleted(name)));
};

export const onCreateProjectSuccess = (response = {}, clientId, history) => (dispatch, getState) => {
  const project = apiUtils.normalizeIds(response);
  const { name, id: projectId } = project;
  dispatch(addNewProject(project));
  dispatch(appActions.changeRoute(RouteNames.READPROJECT, { clientId, projectId, routeTag: 'datasets' }, history));
  dispatch(appActions.modalDialogChange({
    type: Constants.DIALOGS.DATASET_DIALOG,
    header: Constants.UPLOAD_DATASET,
    dispatch,
    projectId,
    history,
    project,
  }));
  dispatch(appActions.displayGoodRequestMessage(DISPLAY_MESSAGES.projectCreated(name)));
};

export const onCreateProjectFailure = (error = {}) => (dispatch, getState) => {
  const { message } = error;
  dispatch(appActions.modalDialogChange(null));
  if (error.code === 409) {
    dispatch(appActions.displayWarningRequestMessage(message));
  } else {
    dispatch(appActions.displayBadRequestMessage(message));
  }
};

export const updateByProject = data => (dispatch, getState) => {
  const {
    name, description, locale, vertical,
  } = data.values;
  const { clientId, projectId } = data;
  const oldName = getState().projectListSidebar.projectById[projectId].name;
  let formData = [{
    op: Constants.OP.REPLACE,
    path: '/description',
    value: description || '',
  },
  {
    op: Constants.OP.REPLACE,
    path: '/locale',
    value: locale || '',
  },
  {
    op: Constants.OP.REPLACE,
    path: '/vertical',
    value: vertical || 'NULL',
  }];

  if (name !== oldName) {
    formData.push({
      op: Constants.OP.REPLACE,
      path: '/name',
      value: name || '',
    });
  }
  const url = getUrl(pathKey.projectById, { clientId, projectId });
  return new Promise((resolve, reject) => {
    api.patch({
      dispatch,
      getState,
      url,
      headers: {
        'Content-Type': 'application/json-patch+json',
      },
      data: formData,
      onApiSuccess: response => onUpdateProjectSuccess(response, projectId),
      onApiError: error => onProjectFailure(error, projectId),
    });
  });
};

export const deleteProjectById = projectId => (dispatch, getState) => {
  const state = getState();
  const clientId = state.header.client.id;
  const url = getUrl(pathKey.projectById, { projectId, clientId });

  return new Promise((resolve, reject) => {
    api.delete({
      dispatch,
      getState,
      url,
      onApiSuccess: () => onDeleteProjectSuccess(projectId),
      onApiError: error => onProjectFailure(error, projectId),
    });
  });
};

export const promoteProjectById = data => (dispatch, getState) => {
  const state = getState();
  const clientId = state.header.client.id;
  const { projectId } = data;
  const url = getUrl(pathKey.promoteProject, { clientId, ...data });

  return new Promise((resolve, reject) => {
    api.put({
      dispatch,
      getState,
      url,
      onApiSuccess: response => onPromoteProjectSuccess(response, projectId),
      onApiError: error => onProjectFailure(error, projectId),
    });
  });
};

export const demoteProjectById = projectId => (dispatch, getState) => {
  const state = getState();
  const clientId = state.header.client.id;
  const url = getUrl(pathKey.demoteProject, { projectId, clientId });

  return new Promise((resolve, reject) => {
    api.put({
      dispatch,
      getState,
      url,
      onApiSuccess: response => onUpdateProjectSuccess(response, projectId, DISPLAY_MESSAGES.projectDemoted),
      onApiError: error => onProjectFailure(error, projectId),
    });
  });
};

export const createProject = data => (dispatch, getState) => {
  const state = getState();
  const { clientId, history, values } = data;
  const { userId } = state.app;
  const formData = {
    ...values,
    ownerId: userId,
  };

  const url = getUrl(pathKey.projects, { clientId });
  return new Promise((resolve, reject) => {
    api.post({
      dispatch,
      getState,
      url,
      data: formData,
      onApiSuccess: response => onCreateProjectSuccess(response, clientId, history),
      onApiError: onCreateProjectFailure,
    });
  });
};

// Fetch Project & Dataset

const addDatasets = function (project, dispatch) {
  const state = store.getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.projectDatasets, { projectId: project.id, clientId });

  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((datasets) => {
      dispatch(receiveProject(project, datasets));
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const fetchProject = projectId => (dispatch, getState) => {
  dispatch(requestProject(projectId));
  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;

  const fetchUrl = getUrl(pathKey.projectById, { projectId, clientId });

  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,

    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      addDatasets(json, dispatch);
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const refreshProjectsByClient = clientId => (dispatch, getState) => {
  // TODO: revert limit back to 100 after fixing MD-153
  dispatch(fetchProjectsByClient({ clientId, startIndex: 0, limit: 500 }));
};

export const fetchProjectsByClient = ({ clientId, startIndex, limit }) => (dispatch) => {
  if (_.isNil(clientId) || clientId == '0') {
    return;
  }
  dispatch(requestProjectsByClient({ clientId, startIndex, limit }));
  const fetchUrl = getUrl(pathKey.projectsForClient, { clientId, startIndex, limit });
  const state = store.getState();
  const { csrfToken } = state.app;
  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      dispatch(receiveProjectsByClient({
        projects: json, clientId, startIndex, limit,
      }));
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const fetchAllProjects = () => (dispatch, getState) => {
  dispatch(requestAllProjects());
  const state = getState();
  const { csrfToken } = state.app;
  const clientId = state.header.client.id;
  const fetchUrl = getUrl(pathKey.projects, { clientId });
  return fetch(fetchUrl, {
    credentials: 'same-origin',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
  })
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      dispatch(receiveAllProjects(json));
    })
    .catch(error => errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage));
};

export const markDeployableModel = ({
  clientId, projectId, modelId, model,
}) => (dispatch, getState) => new Promise((resolve, reject) => {
  const deployableModelId = parseInt(modelId, 10);

  let formData = [{
    op: Constants.OP.REPLACE,
    path: '/deployableModelId',
    value: deployableModelId || null,
  }];

  const url = getUrl(pathKey.projectById, { clientId, projectId });
  return new Promise((resolve, reject) => {
    api.patch({
      dispatch,
      getState,
      url,
      headers: {
        'Content-Type': 'application/json-patch+json',
      },
      data: formData,
      onApiSuccess: response => onUpdateProjectSuccess(response, projectId, DISPLAY_MESSAGES.markModelFlagSuccess(model.version)),
      onApiError: error => onMarkDeployableModelFailure(error, model.version),
    });
  });
});

export const onMarkDeployableModelFailure = (error = {}, modelVersion) => (dispatch) => {
  dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.markModelFlagFailed(modelVersion)));
};

export const unmarkDeployableModel = ({
  clientId, projectId, model,
}) => (dispatch, getState) => new Promise((resolve, reject) => {
  let formData = [{
    op: Constants.OP.REPLACE,
    path: '/deployableModelId',
    value: null,
  }];

  const url = getUrl(pathKey.projectById, { clientId, projectId });
  return new Promise((resolve, reject) => {
    api.patch({
      dispatch,
      getState,
      url,
      headers: {
        'Content-Type': 'application/json-patch+json',
      },
      data: formData,
      onApiSuccess: response => onUpdateProjectSuccess(response, projectId, DISPLAY_MESSAGES.unmarkModelFlagSuccess(model.version)),
      onApiError: error => onUnmarkDeployableModelFailure(error, model.version),
    });
  });
});

export const onUnmarkDeployableModelFailure = (error = {}, modelVersion) => (dispatch) => {
  dispatch(appActions.displayBadRequestMessage(DISPLAY_MESSAGES.unmarkModelFlagFailed(modelVersion)));
};
