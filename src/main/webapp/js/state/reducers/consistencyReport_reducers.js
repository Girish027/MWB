import * as types from 'state/actions/types';

export const defaultState = {
  projectId: null,
  query: '',
  filter: {
    datasets: [],
    onlyConflicts: false,
  },
  sort: { property: 'normalizedFormGroup', direction: 'ASC' },
  startIndex: 0,
  limit: 50,
  isSearching: false,
  isError: false,
  showControls: true,
  searchResults: [],
  total: 0,
  doSearch: false,
  showBulkTag: false,
  updateBulkTag: null,
  delay: 0,
  isUpdatingTags: 0,
};

function consistencyReportReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.CONSISTENCY_REPORT_RESET: {
    return Object.assign({}, state, {
      projectId: null,
      query: '',
      filter: {
        datasets: [],
        onlyConflicts: false,
      },
      sort: { property: 'normalizedFormGroup', direction: 'ASC' },
      startIndex: 0,
      limit: 50,
      isSearching: false,
      isError: false,
      showControls: true,
      searchResults: [],
      total: 0,
      doSearch: false,
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.CONSISTENCY_REPORT_SET_PROJECT_ID: {
    const { projectId } = action;
    return Object.assign({}, state, {
      projectId,
      query: '',
      filter: {
        datasets: [],
        onlyConflicts: false,
      },
      sort: { property: 'normalizedFormGroup', direction: 'ASC' },
      startIndex: 0,
      limit: 50,
      isSearching: false,
      isError: false,
      showControls: true,
      searchResults: [],
      total: 0,
      doSearch: false,
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS: {
    const { projectId, datasets } = action;
    const { filter } = state;
    if (projectId === state.projectId && !filter.datasets.length) {
      datasets.forEach((d) => {
        if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
          filter.datasets.push(d.id);
        }
      });
      return Object.assign({}, state, {
        filter,
      });
    }
    return state;
  }

  case types.CONSISTENCY_REPORT_SET_FILTER: {
    const { datasets, onlyConflicts } = action;
    const { filter } = state;
    if (typeof datasets !== 'undefined') {
      filter.datasets = datasets;
    }
    if (typeof onlyConflicts !== 'undefined') {
      filter.onlyConflicts = onlyConflicts;
    }
    return Object.assign({}, state, {
      filter,
    });
  }

  case types.CONSISTENCY_REPORT_REQUEST_SEARCH_REQUEST: {
    const { sort, startIndex, limit } = action;
    return Object.assign({}, state, {
      isSearching: true,
      isError: false,
      sort,
      startIndex,
      limit,
      doSearch: false,
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS: {
    const { results, startIndex } = action;
    const searchResults = results.transcriptionList ? results.transcriptionList : [];
    return Object.assign({}, state, {
      startIndex,
      isSearching: false,
      isError: false,
      searchResults,
      total: results.total || searchResults.length,
      doSearch: false,
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.CONSISTENCY_REPORT_REQUEST_SEARCH_ERROR: {
    return Object.assign({}, state, {
      isSearching: false,
      isError: true,
      doSearch: false,
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.CONSISTENCY_REPORT_SET_PAGER_SETTINGS: {
    const { startIndex, limit, showControls } = action;
    return Object.assign({}, state, {
      startIndex,
      limit,
      showControls,
      doSearch: startIndex != state.startIndex || limit != state.limit,
      delay: 0,
    });
  }

  case types.SET_TAG_REQUEST:
  case types.REMOVE_TAG_REQUEST: {
    const { projectId } = action;
    if (state.projectId === projectId) {
      return Object.assign({}, state, {
        isUpdatingTags: ++state.isUpdatingTags,
      });
    }
    return state;
  }

  case types.SET_TAG_ERROR:
  case types.SET_TAG_SUCCESS:
  case types.REMOVE_TAG_ERROR:
  case types.REMOVE_TAG_SUCCESS: {
    const { projectId } = action;
    if (state.projectId === projectId) {
      return Object.assign({}, state, {
        isUpdatingTags: --state.isUpdatingTags,
        doSearch: state.doSearch || !state.isUpdatingTags,
        delay: 3000,
      });
    }
    return state;
  }

  case types.CONSISTENCY_REPORT_REFRESH: {
    const { delay } = action;
    return Object.assign({}, state, {
      doSearch: true,
      delay,
    });
  }

  case types.SHOW_BULK_TAG_CELL: {
    return Object.assign({}, state, {
      showBulkTag: true,
      updateBulkTag: null,
    });
  }

  case types.HIDE_BULK_TAG_CELL: {
    return Object.assign({}, state, {
      showBulkTag: false,
      updateBulkTag: null,
    });
  }

  case types.SELECTED_TAG_SUGGEST: {
    return Object.assign({}, state, {
      updateBulkTag: action.selectedSuggestTag,
    });
  }

  case types.SELECTED_BULK_TAG: {
    return Object.assign({}, state, {
      updateBulkTag: action.bulkTagIntent,
    });
  }

  default:
    return state;
  }
}

export { consistencyReportReducer };
