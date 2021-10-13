import * as _ from 'lodash';
import * as types from 'state/actions/types';
import { Map } from 'immutable';
import { initialStatsResults, initialColumnSort, initialValidDatasetsStats } from '../initialSearchData';

export const defaultState = {
  projectId: null,
  project: null,
  projectLoaded: false,
  datasets: null,
  datasetsLoaded: false,
  incomingFilter: {
    projectId: null,
    datasets: [],
  },
  refreshStats: false,
  isLoading: false,
  isControlsCollapsed: false,

  bulkTagMode: false,
  selectedSuggestTag: '',
  updatedSuggestTag: '',
  suggestTagResult: { status: 4, suggestedTags: [] },
  selectedSuggestBulkTag: '',
  updatedSuggestBulkTag: '',
  bulkTagReceived: false,
  userEnteredBulkTag: '',

  focusRowIndex: 0,
  focusColumnIndex: 0,

  presentationType: 'table', // visualize | table
  requestDatasetId: null,
  datasetOpen: false,
  dataset: {
    name: 'Default Dataset',
    description: 'All about this default dataset...',
    vertical: 'FINANCIAL',
    dataType: '0',
  },
  status: 0,
  query: 'query word or phrase',
  filter: {
    untagged: true,
    tagged: false,
    onlyMultipleTags: false,
    wordCount: 8,
    hasComment: false,
    datasets: [],
  },
  intent: '',
  bulkTagIntent: '',
  activeCell: null, // the <td> element
  mousePosition: { x: 0, y: 360 },
  activeRowData: null, // the object containing the properties for the row
  lastRowIdFocussed: -1,
  lastRowIdTagged: -1,
  manualTagAddedSuccess: false,
  isAfterInit: false,
  statsResults: initialStatsResults,
  validDatasetsStats: initialValidDatasetsStats,
  manualTagResult: { status: 1, message: '' },
  manualBulkTagResult: { status: 1, message: '' },
  userEnteredTag: '',
  userEnteredComment: '',
  fetching: false,
  dropTable: false,
  error: null,
  cancelUpdateRender: true,
  startIndex: 0, //
  currentPage: 1,
  limit: 50, // total records in a page ,
  total: 0, // total pages calculated from search results
  pageSearch: false,
  pageActiveButtonLabel: 'next',
  totalPagingButtons: 5, // PREV 1,2,3,4,5 NEXT - used in GridPager
  searchResultsTotal: 0, //
  searchResults: null, // the total results would reflect the limit
  cursor: null,
  newRowsSelected: false,
  uncheckAllRows: false,
  allRowsSelected: false,
  sort: {
    columns: initialColumnSort, sortKey: 'count', direction: 'desc', sortDirection: 'desc',
  },
};

function tagDatasetsReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.TAG_DATASETS_CLEANUP: {
    return Object.assign({}, state, {
      projectId: null,
      datasetsId: null,
      project: null,
      projectLoaded: false,
      datasets: null,
      datasetsLoaded: false,
      incomingFilter: {
        datasets: [],
      },
      filter: {
        untagged: true,
        tagged: false,
        onlyMultipleTags: false,
        wordCount: 8,
        hasComment: false,
        datasets: [],
      },
      statsResults: initialStatsResults,
      searchResultsTotal: 0,
      searchResults: null,
      isLoading: false,
      focusRowIndex: 0,
      focusColumnIndex: 0,
      isControlsCollapsed: false,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.TAG_DATASETS_SET_PROJECT_ID: {
    const { projectId } = action;
    const { filter, incomingFilter } = state;
    if (incomingFilter.projectId == projectId) {
      filter.datasets = incomingFilter.datasets;
    }
    return Object.assign({}, state, {
      projectId: projectId || null,
      project: null,
      projectLoaded: false,
      datasets: null,
      datasetsLoaded: false,
      statsResults: initialStatsResults,
      searchResultsTotal: 0,
      searchResults: null,
      refreshStats: true,
      filter,
      incomingFilter: {
        projectId: null,
        datasets: [],
      },
      isLoading: false,
      focusRowIndex: 0,
      focusColumnIndex: 0,
      isControlsCollapsed: false,
    });
  }

  case types.PROJECTS_MANAGER_PROJECT_LOAD_ERROR: {
    const { projectId } = action;
    if (projectId === state.projectId) {
      return Object.assign({}, state, {
        project: null,
        projectLoaded: true,
      });
    }
    return state;
  }

  case types.PROJECTS_MANAGER_PROJECT_LOAD_SUCCESS: {
    const { projectId, project } = action;
    if (projectId === state.projectId) {
      return Object.assign({}, state, {
        project,
        projectLoaded: true,
      });
    }
    return state;
  }

  case types.TAG_DATASETS_SET_PROJECT: {
    const { projectId, project } = action;
    if (projectId === state.projectId) {
      return Object.assign({}, state, {
        project,
        projectLoaded: true,
      });
    }
    return state;
  }

  case types.PROJECTS_MANAGER_PROJECT_UPDATED: {
    const { projectId, project } = action;
    if (projectId === state.projectId) {
      return Object.assign({}, state, {
        project,
        projectLoaded: true,
      });
    }
    return state;
  }

  case types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_ERROR: {
    const { projectId } = action;
    if (projectId === state.projectId) {
      return Object.assign({}, state, {
        datasets: null,
        datasetsLoaded: true,
      });
    }
    return state;
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS: {
    const { projectId, datasets } = action;
    if (projectId === state.projectId) {
      const { filter } = state;
      const datasetsFilter = filter.datasets;
      if (!datasetsFilter.length) {
        datasets.forEach((d) => {
          if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
            datasetsFilter.push(d.id);
          }
        });
        filter.datasets = datasetsFilter;
      }
      return Object.assign({}, state, {
        datasets,
        datasetsLoaded: true,
        filter,
      });
    }
    return state;
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.TAG_DATASETS_SET_DATASETS: {
    const { projectId, datasets } = action;
    if (projectId === state.projectId) {
      const { filter } = state;
      const datasetsFilter = filter.datasets;
      if (!datasetsFilter.length) {
        datasets.forEach((d) => {
          if (d.id && d.status && d.status == 'COMPLETED' && d.name && d.isClickable) {
            datasetsFilter.push(d.id);
          }
        });
        filter.datasets = datasetsFilter;
      }

      return Object.assign({}, state, {
        datasets,
        datasetsLoaded: true,
        filter,
      });
    }
    return state;
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.TAG_DATASETS_SET_DATASETS_FILTER: {
    const { datasets } = action;
    const { filter } = state;
    filter.datasets = datasets;
    return Object.assign({}, state, {
      filter,
    });
  }

  // TODO: https://247inc.atlassian.net/browse/NT-3246
  case types.TAG_DATASETS_SET_INCOMING_FILTER: {
    const { projectId, datasets } = action;
    const { filter } = state;
    if (projectId == state.projectId) {
      filter.datasets = datasets || [];
      return Object.assign({}, state, {
        incomingFilter: {
          projectId: null,
          datasets: [],
        },
        filter,
        statsResults: initialStatsResults,
        searchResultsTotal: 0,
        searchResults: null,
        refreshStats: true,
        focusRowIndex: 0,
        focusColumnIndex: 0,
      });
    }
    return Object.assign({}, state, {
      incomingFilter: {
        projectId,
        datasets,
      },
    });
  }

  case types.TAG_DATASETS_SET_FOCUS: {
    const { rowIndex, columnIndex } = action;
    return Object.assign({}, state, {
      focusRowIndex: rowIndex,
      focusColumnIndex: columnIndex,
    });
  }

  case types.TAG_DATASETS_SET_CONTROLS_COLLAPSE: {
    const { isCollapsed } = action;
    return Object.assign({}, state, {
      isControlsCollapsed: isCollapsed,
    });
  }


  /* ToDo: review following actions */

  case types.DATASET_OPEN:
    return Object.assign({}, state, {
      dataset: action.dataset,
      datasetOpen: true,
      searchResults: null,
      focusRowIndex: 0,
      focusColumnIndex: 0,
    });
  case types.DATASET_CLOSE:
    return Object.assign({}, state, {
      dataset: null,
      datasetOpen: false,
    });
  case types.CHANGE_PRESENTATION_TYPE:
    return Object.assign({}, state, {
      presentationType: action.presentationType,
    });
  case types.RECEIVE_DATASET:
    return Object.assign({}, state, {
      requestDatasetId: null,
      dataset: action.dataset,
      datasetOpen: true,
    });
  case types.REQUEST_DATASET_SEARCH:
    return Object.assign({}, state, {
      fetching: true,
      activeCell: null,
      activeRowData: null,
      userEnteredTag: '',
      dropTable: true,
      pageSearch: false,
      query: action.query,
      filter: action.filter,
      allRowsSelected: false,
      sort: action.sort,
      isLoading: true,
    });
  case types.RECEIVE_DATASET_SEARCH_SUCCESS:
    return Object.assign({}, state, {
      searchResults: action.searchResults,
      limit: action.limit,
      startIndex: action.startIndex,
      total: action.total,
      fetching: false,
      dropTable: false,
      pageSearch: false,
      isLoading: false,
      focusRowIndex: 0,
      focusColumnIndex: 0,
    });
  case types.REQUEST_DATASET_STATS:
    return Object.assign({}, state, {
      fetching: true,
      refreshStats: false,
    });
  case types.RECEIVE_DATASET_STATS_SUCCESS:
    return Object.assign({}, state, {
      statsResults: action.statsResults,
      fetching: false,
      refreshStats: false,
    });
  case types.RECEIVE_DATASET_VALIDATION_STATS: {
    const {
      isDatasetsValid, isDatasetsTagged,
      isDatasetValid, isFullyTagged,
      uniqueRollupValue, datasetId,
      datasetsIntents, datasetIntents,
    } = action;

    // update local dataset stats
    const updatedDataset = Object.assign({}, {
      isDatasetValid, isFullyTagged, uniqueRollupValue, datasetIntents,
    });
    const { validDatasetMap } = state.validDatasetsStats;
    let clonedValidDatasetMap = new Map(validDatasetMap);
    clonedValidDatasetMap = clonedValidDatasetMap.set(datasetId, updatedDataset);

    const updatedValidDatasetsStats = Object.assign({}, state.validDatasetsStats, {
      isDatasetsTagged,
      isDatasetsValid,
      datasetsIntents,
      validDatasetMap: clonedValidDatasetMap,
    });

    return Object.assign({}, state, {
      validDatasetsStats: updatedValidDatasetsStats,
    });
  }

  case types.DELETE_DATASET_VALIDATION_STATS: {
    const { datasetId } = action;
    const { validDatasetMap } = state.validDatasetsStats;
    let clonedValidDatasetMap = new Map(validDatasetMap);
    let isDatasetsTagged = true;
    let isDatasetsValid = false;
    let ruValue = null;
    let datasetsIntents = [];

    if (clonedValidDatasetMap.get(datasetId)) {
      clonedValidDatasetMap = clonedValidDatasetMap.delete(datasetId);
    }

    clonedValidDatasetMap.forEach((value) => {
      isDatasetsTagged = isDatasetsTagged && value.isFullyTagged;
      isDatasetsValid = isDatasetsValid || value.isDatasetValid || (ruValue !== null && value.uniqueRollupValue !== ruValue);
      ruValue = value.uniqueRollupValue;
      datasetsIntents = [...datasetsIntents, ...value.datasetIntents];
    });

    const updatedValidDatasetsStats = Object.assign({}, state.validDatasetsStats, {
      isDatasetsTagged,
      isDatasetsValid,
      datasetsIntents,
      validDatasetMap: clonedValidDatasetMap,
    });

    return Object.assign({}, state, {
      validDatasetsStats: updatedValidDatasetsStats,
    });
  }
  case types.RESET_DATASET_VALIDATION_STATS: {
    return Object.assign({}, state, {
      validDatasetsStats: initialValidDatasetsStats,
    });
  }
  case types.UPDATE_QUERY:
    return Object.assign({}, state, {
      query: action.query,
    });
  case types.UPDATE_FILTERS:
    return Object.assign({}, state, {
      filter: action.filter,
    });
  case types.REQUEST_TAG_MANUAL:
    return Object.assign({}, state, {
      fetching: true,
      manualTagAddedSuccess: false,
    });
  case types.RECEIVE_TAG_MANUAL_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      manualTagResult: action.manualTagResult,
      suggestTagResult: action.suggestTagResult,
      lastRowIdTagged: action.activeRowData.id,
      refreshStats: true,
      activeCell: null,
      selectedSuggestTag: '',
      intent: '',
      manualTagAddedSuccess: true,
    });
  case types.RECEIVE_TAG_MANUAL_DELETE_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      manualTagResult: action.manualTagResult,
      suggestTagResult: action.suggestTagResult,
      lastRowIdTagged: action.activeRowData.id,
      refreshStats: true,
      activeCell: null,
      selectedSuggestTag: '',
      intent: '',
    });
  case types.RECEIVE_TAG_MANUAL_FAIL:
    return Object.assign({}, state, {
      fetching: false,
      manualTagResult: action.manualTagResult,
      suggestTagResult: action.suggestTagResult,
    });
  case types.REQUEST_BULK_TAG_MANUAL:
    return Object.assign({}, state, {
      fetching: true,
    });
  case types.REQUEST_BULK_TAG_MANUAL_UPDATE:
    return Object.assign({}, state, {
      fetching: true,
    });
  case types.REQUEST_BULK_TAG_MANUAL_MIXED:
    return Object.assign({}, state, {
      fetching: true,
    });
  case types.RECEIVE_BULK_TAG_MANUAL_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      manualBulkTagResult: action.manualBulkTagResult,
      suggestTagResult: action.suggestTagResult,
      refreshStats: true,
      bulkTagIntent: '',
      bulkTagReceived: true,
      selectedSuggestTag: '',
      intent: '',
      uncheckAllRows: true,
    });
  case types.RECEIVE_BULK_TAG_MANUAL_FAIL:
    return Object.assign({}, state, {
      fetching: false,
      manualBulkTagResult: action.manualBulkTagResult,
      suggestTagResult: action.suggestTagResult,
    });
  case types.RECEIVE_BULK_TAG_MANUAL_UPDATE_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      manualBulkTagResult: action.manualBulkTagResult,
      suggestTagResult: action.suggestTagResult,
      refreshStats: true,
      bulkTagIntent: '',
      bulkTagReceived: true,
      selectedSuggestTag: '',
      intent: '',
      uncheckAllRows: true,
    });
  case types.RECEIVE_BULK_TAG_MANUAL_UPDATE_FAIL:
    return Object.assign({}, state, {
      fetching: false,
      manualBulkTagResult: action.manualBulkTagResult,
      suggestTagResult: action.suggestTagResult,
    });
  case types.RECEIVE_BULK_TAG_MANUAL_DELETE_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      manualBulkTagResult: action.manualBulkTagResult,
      suggestTagResult: action.suggestTagResult,
      refreshStats: true,
      bulkTagIntent: '',
      bulkTagReceived: true,
      selectedSuggestTag: '',
      intent: '',
      uncheckAllRows: true,
    });
  case types.REQUEST_TAG_SUGGEST:
    return Object.assign({}, state, {
      fetching: true,
    });
  case types.RECEIVE_TAG_SUGGEST_SUCCESS:
    return Object.assign({}, state, {
      fetching: false,
      suggestTagResult: action.suggestTagResult,
    });
  case types.RECEIVE_TAG_SUGGEST_FAIL:
    return Object.assign({}, state, {
      fetching: false,
      suggestTagResult: action.suggestTagResult,
    });
  case types.UPDATE_INTENT:
    return Object.assign({}, state, {
      intent: action.intent,
    });
  case types.UPDATE_CELL_ACTIVE:
    return Object.assign({}, state, {
      activeCell: action.activeCell,
      activeRowData: action.activeRowData,
      lastRowIdFocussed: action.activeRowData.id,
      suggestTagResult: { status: 5, suggestedTags: [] },
      isAfterInit: true,
      selectedSuggestTag: '',
      updatedSuggestTag: '',
      bulkTagReceived: false,
      manualTagAddedSuccess: false,
      mousePosition: action.mousePosition,
      bulkTagMode: false,
    });
  case types.UPDATE_BULK_TAG_USER_ENTERED:
    return Object.assign({}, state, {
      userEnteredBulkTag: action.userEnteredBulkTag,
      cancelUpdateRender: false,
      bulkTagReceived: false,
      selectedSuggestTag: '',
      updatedSuggestTag: '',
    });
  case types.SELECTED_BULK_TAG:
    return Object.assign({}, state, {
      bulkTagIntent: action.bulkTagIntent,
      cancelUpdateRender: false,
      bulkTagReceived: true,
    });
  case types.SHOW_BULK_TAG_CELL:
    return Object.assign({}, state, {
      intent: '',
      bulkTagIntent: '',
      activeRowData: null,
    });
  case types.HIDE_BULK_TAG_CELL:
    return Object.assign({}, state, {
      intent: '',
      bulkTagIntent: '',
      bulkTagMode: false,
      bulkTagReceived: false,
      activeCell: {}, //  activeCell & activeRowData can"t be null but they don"t need properties either to hide bulk tag cell
      activeRowData: {},
    });
  case types.UPDATE_BULK_CELL_ACTIVE:
    return Object.assign({}, state, {
      activeCell: action.activeCell,
      activeRowData: null,
      suggestTagResult: { status: 5, suggestedTags: [] },
      intent: '',
      selectedSuggestTag: '',
      updatedSuggestTag: '',
      bulkTagReceived: false,
      bulkTagMode: true,
    });
  case types.UPDATE_TAG_USER_ENTERED:
    return Object.assign({}, state, {
      userEnteredTag: action.userEnteredTag,
      cancelUpdateRender: false,
      bulkTagReceived: false,
    });
  case types.SELECTED_TAG_SUGGEST:
    return Object.assign({}, state, {
      selectedSuggestTag: action.selectedSuggestTag,
      updatedSuggestTag: '',
      bulkTagIntent: action.selectedSuggestTag,
      suggestTagResult: { status: 5, suggestedTags: [] }, // status 5 means they updated the cell with a suggested tag
      cancelUpdateRender: false,
    });
  case types.FORGET_SELECTED_SUGGESTED_TAG:
    return Object.assign({}, state, {
      selectedSuggestTag: '',
      updatedSuggestTag: '',
      bulkTagIntent: '',
      cancelUpdateRender: false,
    });
  case types.FORGET_SELECTED_SUGGESTED_BULK_TAG:
    return Object.assign({}, state, {
      selectedSuggestBulkTag: '',
      updatedSuggestBulkTag: '',
      bulkTagIntent: '',
      cancelUpdateRender: false,
    });
  case types.REMOVE_TAG_SUGGEST:
    return Object.assign({}, state, {
      selectedSuggestTag: '',
      updatedSuggestTag: '',
      bulkTagIntent: '',
      suggestTagResult: action.suggestTagResult, // status 5 means they updated the cell with a suggested tag
      cancelUpdateRender: false,
    });
  case types.UPDATE_WITH_TAG_SUGGEST:
    return Object.assign({}, state, {
      updatedSuggestTag: action.updatedSuggestTag,
      selectedSuggestTag: '',
      cursor: null,
    });
  case types.PROJECT_TABLE_CANCEL_RENDER:
    return Object.assign({}, state, {
      cancelUpdateRender: true,
    });
  case types.TABLE_SEARCH_RESULTS_UPDATE:
    return Object.assign({}, state, {
      searchResults: action.searchResults,
      focusRowIndex: 0,
      focusColumnIndex: 0,
    });
  case types.DATASET_CHANGE_PAGE:
    return Object.assign({}, state, {
      currentPage: action.newPage,
      startIndex: action.newStartIndex,
      pageSearch: true,
      pageActiveButtonLabel: action.label,
      bulkTagIntent: '',
      intent: '',
      activeCell: null,
      activeRowData: null,
    });
  case types.DATASET_CHANGE_LIMIT:
    return Object.assign({}, state, {
      limit: action.newLimit,
      pageSearch: typeof action.pageSearch !== 'undefined' ? action.pageSearch : true,
    });
  case types.BULK_TAG_INTENT_DELETED:
    return Object.assign({}, state, {
      bulkTagIntent: '',
    });
  case types.CURSOR_UP:
    return Object.assign({}, state, {
      cursor: { timestamp: Number(new Date()), direction: 'up' },
    });
  case types.CURSOR_DOWN:
    return Object.assign({}, state, {
      cursor: { timestamp: Number(new Date()), direction: 'down' },
    });
  case types.UNCHECK_ALL_SELECTED_ROWS:
    return Object.assign({}, state, {
      uncheckAllRows: true,
    });
  case types.ALL_ROWS_UNCHECKED:
    return Object.assign({}, state, {
      uncheckAllRows: false,
    });
  case types.COLUMN_SORT_CHANGE:
    return Object.assign({}, state, {
      sort: action.sort,
      startIndex: 0,
      currentPage: 1,
    });
  case '@@react-redux-grid/SET_COLUMNS': {
    const setColumn = _.find(action.columns, column => column.sortDirection !== null && undefined !== column.sortDirection);
    const finalSetColumn = (undefined !== setColumn) ? setColumn : { dataIndex: 'count', sortDirection: 'desc' };
    return Object.assign({}, state, {
      sort: {
        columns: action.columns,
        sortKey: finalSetColumn.dataIndex,
        direction: finalSetColumn.sortDirection,
        sortDirection: finalSetColumn.sortDirection,
      },
    });
  }
  case '@@react-redux-grid/SET_SORT_DIRECTION': {
    const sortColumn = _.find(action.columns, column => column.sortDirection !== null && undefined !== column.sortDirection);
    const finalSortColumn = (undefined !== sortColumn) ? sortColumn : {
      dataIndex: 'count',
      sortDirection: 'desc',
    };
    return Object.assign({}, state, {
      sort: {
        columns: action.columns,
        sortKey: finalSortColumn.dataIndex,
        direction: finalSortColumn.sortDirection,
        sortDirection: finalSortColumn.sortDirection,
      },
    });
  }
  case '@@react-redux-grid/SET_SELECTION':
    return Object.assign({}, state, {
      newRowsSelected: true,
      allRowsSelected: false, // while all the rows could be manually selected, this is false because it relates to th bulk check all box in the header
    });
  case '@@react-redux-grid/SELECT_ALL':
    return Object.assign({}, state, {
      newRowsSelected: true,
      allRowsSelected: true,
      bulkTagReceived: false,
    });
  case '@@react-redux-grid/DESELECT_ALL':
    return Object.assign({}, state, {
      newRowsSelected: false,
      uncheckAllRows: true,
      allRowsSelected: false,
      bulkTagReceived: false,
    });
  case '@@react-redux-grid/RESIZE_COLUMNS ':
    return Object.assign({}, state, {

    });
  default:
    return state;
  }
}

export { tagDatasetsReducer };
