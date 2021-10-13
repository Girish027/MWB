import fetch from 'isomorphic-fetch';
import * as _ from 'lodash';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import store from 'state/configureStore';
import getUrl, { pathKey } from 'utils/apiUrls';
import { normalizeIdArray } from 'utils/apiUtils';
import * as types from './types';
import * as appActions from './actions_app';
import * as sampleData from './sample_search';
import { initialColumnSort } from '../initialSearchData';

const sampleSearch = sampleData.sampleSearch;

export const changePage = (newPage, newStartIndex, label) => ({
  type: types.DATASET_CHANGE_PAGE,
  newStartIndex,
  newPage,
  label,
});

export const changeLimit = (newLimit, pageSearch) => ({
  type: types.DATASET_CHANGE_LIMIT,
  newLimit,
  pageSearch,
});

// QUERY & FILTERS - CHANGE IN THE UI WITHOUT CONTACTING THE SERVER

export const updateFilters = filter => ({
  type: types.UPDATE_FILTERS,
  filter,
});

export const updateQuery = query => ({
  type: types.UPDATE_QUERY,
  query,
});

// SEARCH - APPLY THE QUERY & FILTERS

export const requestSimulateTagSearch = (query, filter) => ({
  type: types.REQUEST_DATASET_SEARCH,
  query,
  filter,
  sort: {
    columns: initialColumnSort, sortKey: 'count', direction: 'desc', sortDirection: 'desc',
  },
});

export const requestTagSearch = (query, filter, sort) => ({
  type: types.REQUEST_DATASET_SEARCH,
  query,
  filter,
  sort,
});

export const receiveTagSearchSuccess = (searchResults, limit, startIndex, total) => ({
  type: types.RECEIVE_DATASET_SEARCH_SUCCESS,
  searchResults,
  limit,
  startIndex,
  total,
});

export const receiveTagSearchFail = error => ({
  type: types.RECEIVE_DATASET_SEARCH_FAIL,
  error,
});


export const fetchSimulateDatasetSearch = (query, filter) => (dispatch) => {
  dispatch(requestSimulateTagSearch(query, filter));

  setTimeout(() => {
    const searchResults = _.map(sampleSearch, (item, index) => ({
      id: index,
      count: index,
      uniqueTextString: item.uniqueTextString,
      suggestedTag: item.suggestedTag,
      manualTag: '',
      comment: '',
      fullResult: item,
    }));

    dispatch(receiveTagSearchSuccess(searchResults));
  }, 200);
};


/*
 paging would be using limit and startIndex. so if you went startIndex= 0 limit = 10
 next page would be start index 10 limit 10. limit is the size of the page, start index is the offset,
 we are also sending the total number of records back, which is what the ui should use to know how
 many pages there are to be, but then update the start index based on what page they are requesting
 */
export const fetchDatasetSearch = ({
  query, filter, projectId, startIndex, limit, sort,
}) => (dispatch, clientId) => new Promise((resolve, reject) => {
  dispatch(requestTagSearch(query, filter, sort));

  const state = store.getState();
  const app = state.app;
  const userId = app.userId;
  const csrfToken = app.csrfToken;
  const clientId = state.header.client.id;

  const payload = {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': csrfToken,
    },
    body: JSON.stringify({ query, filter }),
  };

  const fetchUrl = getUrl(pathKey.datasetSearch, {
    projectId,
    startIndex,
    limit,
    sortKey: sort.sortKey,
    sortDirection: sort.direction,
    clientId,
  });

  return fetch(fetchUrl, payload)
    .then(errorMessageUtil.handleErrors)
    .then(response => response.json())
    .then((json) => {
      const rawSearchResults = (json.transcriptionList) ? json.transcriptionList : [];
      let searchResults = [];
      if (rawSearchResults.length > 0) {
        searchResults = _.map(rawSearchResults, (item, index) => ({
          id: index,
          count: item.documentCount,
          uniqueTextString: item.textStringForTagging,
          suggestedTag: item.autoTagString,
          manualTag: (item.intent) ? item.intent : '', // needs a space to consider "controlled" not empty like ""
          comment: (item.comment) ? item.comment : '',
          fullResult: { ...item, intent: (item.intent) ? item.intent : '' },
          datasetIds: normalizeIdArray(item.datasetIds),
          index,
          transcriptionHash: item.transcriptionHash,
          rutag: (item.rutag) ? item.rutag : '',
        }));
      }
      dispatch(receiveTagSearchSuccess(searchResults, json.limit, json.startIndex, json.total));
      resolve();
    })
    .catch((error) => {
      errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
      dispatch(receiveTagSearchFail(error));
      reject(error);
    });
});
