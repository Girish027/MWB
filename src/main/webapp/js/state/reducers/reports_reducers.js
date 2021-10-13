import * as types from 'state/actions/types';
import { Map } from 'immutable';

export const defaultState = {
  projectId: null,
  incomingFilter: {
    projectId: null,
    datasets: [],
  },
  reportTypes: Map(),
  filter: {
    datasets: [],
    reportType: null,
    entries: 50,
    interval: '3h',
    timeFrom: null,
    timeTo: null,
  },
};

function reportsReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.REPORTS_RESET: {
    return Object.assign({}, state, {
      projectId: null,
      incomingFilter: {
        projectId: null,
        datasets: [],
      },
      reportTypes: Map(),
      filter: {
        datasets: [],
        reportType: null,
        entries: 50,
        interval: '3h',
        timeFrom: null,
        timeTo: null,
      },
    });
  }

  case types.REPORTS_SET_PROJECT_ID: {
    const { projectId } = action;
    const { incomingFilter } = state;
    const filter = {
      datasets: [],
      reportType: null,
      entries: 50,
      interval: '3h',
      timeFrom: null,
      timeTo: null,
    };
    if (incomingFilter.projectId == projectId) {
      filter.datasets = incomingFilter.datasets;
    }
    return Object.assign({}, state, {
      projectId,
      filter: Object.assign({}, filter),
      reportTypes: Map(),
      incomingFilter: {
        projectId: null,
        datasets: [],
      },
    });
  }

  case types.REPORTS_SET_INCOMING_FILTER: {
    const { projectId, datasets } = action;
    const { filter } = state;
    if (projectId == state.projectId) {
      filter.datasets = datasets || [];
      return Object.assign({}, state, {
        incomingFilter: {
          projectId: null,
          datasets: [],
        },
        filter: Object.assign({}, filter),
      });
    }
    return Object.assign({}, state, {
      incomingFilter: {
        projectId,
        datasets,
      },
    });
  }

  case types.REPORTS_SET_FILTER: {
    const {
      datasets, reportType, entries, interval, timeFrom, timeTo,
    } = action;
    const { filter } = state;
    if (typeof datasets !== 'undefined') {
      filter.datasets = datasets;
    }
    if (typeof reportType !== 'undefined') {
      filter.reportType = reportType;
    }
    if (typeof entries !== 'undefined') {
      filter.entries = entries;
    }
    if (typeof interval !== 'undefined') {
      filter.interval = interval;
    }
    if (typeof timeFrom !== 'undefined') {
      filter.timeFrom = timeFrom;
    }
    if (typeof timeTo !== 'undefined') {
      filter.timeTo = timeTo;
    }
    return Object.assign({}, state, {
      filter: Object.assign({}, filter),
    });
  }

  case types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS: {
    const { projectId, datasets } = action;
    const { filter } = state;
    const datasetsFilter = filter.datasets;
    if (projectId === state.projectId && !datasetsFilter.length) {
      datasets.forEach((d) => {
        if (d.id && d.status && d.status == 'COMPLETED' && d.name) {
          datasetsFilter.push(d.id);
        }
      });
      filter.datasets = datasetsFilter;
      return Object.assign({}, state, {
        filter: Object.assign({}, filter),
      });
    }
    return state;
  }

  case types.REPORTS_GET_REPORT_TYPES_SUCCESS: {
    const { reportTypes } = action;
    const { filter } = state;
    let reportTypesMap = Map();
    if (!filter.reportType) {
      filter.reportType = reportTypes.length ? reportTypes[0].name : null;
    }
    reportTypes.forEach((r) => {
      reportTypesMap = reportTypesMap.set(r.name, r);
    });
    return Object.assign({}, state, {
      reportTypes: reportTypesMap,
      filter: Object.assign({}, filter),
    });
  }

  default:
    return state;
  }
}

export { reportsReducer };
