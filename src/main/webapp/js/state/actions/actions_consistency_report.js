import errorMessageUtil from 'utils/ErrorMessageUtil';
import store from 'state/configureStore';

import getUrl, { pathKey } from 'utils/apiUrls';
import * as appActions from './actions_app';
import * as types from './types';

export const reset = () => ({
  type: types.CONSISTENCY_REPORT_RESET,
});

export const setProjectId = projectId => ({
  type: types.CONSISTENCY_REPORT_SET_PROJECT_ID,
  projectId,
});

export const setProject = ({ projectId, project }) => ({
  type: types.CONSISTENCY_REPORT_SET_PROJECT,
  projectId,
  project,
});

export const setDatasets = ({ projectId, datasets }) => ({
  type: types.CONSISTENCY_REPORT_SET_DATASETS,
  projectId,
  datasets,
});


export const setFilter = ({ datasets, onlyConflicts }) => ({
  type: types.CONSISTENCY_REPORT_SET_FILTER,
  datasets,
  onlyConflicts,
});

const requestSearchSuccess = ({ results, startIndex }) => ({
  type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS,
  results,
  startIndex,
});

const requestSearchRequest = ({
  promise, projectId, query, filter, sort, startIndex, limit,
}) => ({
  type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_REQUEST,
  promise,
  projectId,
  query,
  filter,
  sort,
  startIndex,
  limit,
});

const requestSearchError = ({ error }) => ({
  type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_ERROR,
  error,
});

export const setPagerSettings = ({ startIndex, limit, showControls }) => ({
  type: types.CONSISTENCY_REPORT_SET_PAGER_SETTINGS,
  startIndex,
  limit,
  showControls,
});

export const requestSearch = ({
  projectId, query, filter, sort, startIndex, limit, delay,
}, isNested) => (dispatch, getState) => {
  const promise = new Promise((resolve, reject) => {
    const state = getState();
    const { userId, csrfToken } = state.app;
    const clientId = state.header.client.id;

    const fetchUrl = getUrl(pathKey.consistency, {
      projectId,
      startIndex,
      limit,
      sortProperty: sort.property,
      sortDirection: sort.direction,
      clientId,
    });

    const filterBodyObj = {};
    filterBodyObj.onlyTagged = true;
    filterBodyObj.onlyConflicts = filter.onlyConflicts;
    if (filter.datasets && filter.datasets.length) {
      filterBodyObj.datasetIds = filter.datasets;
    }

    setTimeout(() => {
      fetch(fetchUrl, {
        credentials: 'same-origin',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-TOKEN': csrfToken,
        },
        body: JSON.stringify({
          configId: '0',
          filter: filterBodyObj,
        }),
      })
        .then(errorMessageUtil.handleErrors)
        .then(response => response.json())
        .then((results) => {
          const searchResults = results.transcriptionList || [];

          searchResults.forEach((res, index) => {
            res._rowIndex = index;
          });

          if (!searchResults.length && startIndex > 0) {
            const total = results.total;
            const maxPage = Math.max(Math.floor(total / limit) + (total % limit ? 1 : 0), 1);
            const newStartIndex = (maxPage - 1) * limit;
            dispatch(requestSearch({
              projectId,
              query,
              filter,
              sort,
              startIndex: newStartIndex,
              limit,
            }, true))
              .then((results) => {
                dispatch(requestSearchSuccess({ results, startIndex: newStartIndex }));
                resolve(results);
              })
              .catch((error) => {
                reject(error);
              });
          } else {
            if (!isNested) {
              dispatch(requestSearchSuccess({ results, startIndex }));
            }
            resolve(results);
          }
        })
        .catch((error) => {
          dispatch(requestSearchError({ error }));
          errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
          reject(error);
        });
    }, delay || 0);
  });

  if (!isNested) {
    dispatch(requestSearchRequest({
      promise, projectId, query, filter, sort, startIndex, limit,
    }));
  }

  return promise;
};

export const refresh = ({ delay }) => ({
  type: types.CONSISTENCY_REPORT_REFRESH,
  delay,
});
