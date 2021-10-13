import * as types from './types';

export const setProjectId = projectId => ({
  type: types.TAG_DATASETS_SET_PROJECT_ID,
  projectId,
});

export const setProject = ({ projectId, project }) => ({
  type: types.TAG_DATASETS_SET_PROJECT,
  projectId,
  project,
});

export const setDatasets = ({ projectId, datasets }) => ({
  type: types.TAG_DATASETS_SET_DATASETS,
  projectId,
  datasets,
});

export const cleanUp = () => ({
  type: types.TAG_DATASETS_CLEANUP,
});

export const setDatasetsFilter = ({ datasets }) => ({
  type: types.TAG_DATASETS_SET_DATASETS_FILTER,
  datasets,
});

export const setIncomingFilter = ({ projectId, datasets }) => ({
  type: types.TAG_DATASETS_SET_INCOMING_FILTER,
  projectId,
  datasets,
});

export const setFocus = ({ rowIndex, columnIndex }) => ({
  type: types.TAG_DATASETS_SET_FOCUS,
  rowIndex,
  columnIndex,
});

export const setIsControlsCollapsed = ({ isCollapsed }) => ({
  type: types.TAG_DATASETS_SET_CONTROLS_COLLAPSE,
  isCollapsed,
});
