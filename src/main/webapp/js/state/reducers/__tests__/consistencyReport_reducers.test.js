
import * as types from 'state/actions/types';
import { consistencyReportReducer, defaultState } from 'state/reducers/consistencyReport_reducers';

describe('consistencyReportReducer', () => {
  test('should return the initial state', () => {
    const results = consistencyReportReducer(undefined, {
      type: types.NOOP,
    });
    expect(results).toEqual(defaultState);
  });

  test('CONSISTENCY_REPORT_RESET', () => {
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_RESET,
    });
    expect(results).toEqual(defaultState);
  });

  test('CONSISTENCY_REPORT_SET_PROJECT_ID', () => {
    const projectId = '23';
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_SET_PROJECT_ID,
      projectId,
    });
    expect(results).toEqual({ ...defaultState, projectId });
  });

  test('PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS with default project', () => {
    const projectId = null;
    const datasets = [{ id: '12', name: 'abc', status: 'COMPLETED' }, { id: '21', name: 'abd', status: 'COMPLETED' }];
    let results = consistencyReportReducer(undefined, {
      type: types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS,
      projectId,
      datasets,
    });
    expect(results).toEqual({ ...defaultState, projectId });
  });

  test('PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS with project', () => {
    const projectId = '123';
    const datasets = [{ id: '12', name: 'abc', status: 'COMPLETED' }, { id: '21', name: 'abd', status: 'COMPLETED' }];
    let results = consistencyReportReducer(undefined, {
      type: types.PROJECTS_MANAGER_PROJECT_DATASETS_LOAD_SUCCESS,
      projectId,
      datasets,
    });
    expect(results).toEqual({ ...defaultState });
  });

  test('CONSISTENCY_REPORT_SET_FILTER with onlyConflicts and datasets', () => {
    const onlyConflicts = '123';
    const datasets = [{ id: '12', name: 'abc', status: 'COMPLETED' }, { id: '21', name: 'abd', status: 'COMPLETED' }];
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_SET_FILTER,
      onlyConflicts,
      datasets,
    });
    expect(results).toEqual({ ...defaultState, filter: { datasets, onlyConflicts } });
  });

  test('CONSISTENCY_REPORT_REQUEST_SEARCH_REQUEST with sort and startIndex', () => {
    const sort = { direction: 'DESC' };
    const startIndex = 0;
    const limit = 100;
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_REQUEST,
      sort,
      startIndex,
      limit,
    });
    expect(results).toEqual({
      ...defaultState, isSearching: true, limit, startIndex, sort,
    });
  });

  test('CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS with transcriptionList undefined', () => {
    const results = {
      transcriptionList: undefined,
    };
    const startIndex = 0;
    let result = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS,
      results,
      startIndex,
    });
    expect(result).toEqual({
      ...defaultState,
    });
  });

  test('CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS with results, startIndex', () => {
    const results = {
      transcriptionList: ['123'],
    };
    const startIndex = 0;
    let result = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_SUCCESS,
      results,
      startIndex,
    });
    expect(result).toEqual({
      ...defaultState, searchResults: results.transcriptionList, total: 1,
    });
  });

  test('CONSISTENCY_REPORT_REQUEST_SEARCH_ERROR with results, startIndex', () => {
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_REQUEST_SEARCH_ERROR,
    });
    expect(results).toEqual({
      ...defaultState, isError: true,
    });
  });

  test('CONSISTENCY_REPORT_SET_PAGER_SETTINGS with results, startIndex', () => {
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_SET_PAGER_SETTINGS,
    });
    expect(results).toEqual({
      ...defaultState, showControls: undefined, limit: undefined, doSearch: true, startIndex: undefined,
    });
  });

  test('CONSISTENCY_REPORT_SET_PAGER_SETTINGS with results, startIndex', () => {
    const limit = 10;
    const showControls = true;
    const startIndex = 0;
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_SET_PAGER_SETTINGS,
      startIndex,
      limit,
      showControls,
    });
    expect(results).toEqual({
      ...defaultState, showControls, limit, doSearch: true, startIndex,
    });
  });

  test('SET_TAG_REQUEST with results, startIndex', () => {
    const projectId = 10;
    let results = consistencyReportReducer({ ...defaultState, projectId }, {
      type: types.SET_TAG_REQUEST,
      projectId,
    });
    expect(results).toEqual({
      ...defaultState, projectId, isUpdatingTags: 1,
    });
  });

  test('SET_TAG_REQUEST with results, startIndex', () => {
    const projectId = 10;
    let results = consistencyReportReducer(defaultState, {
      type: types.SET_TAG_REQUEST,
      projectId,
    });
    expect(results).toEqual({
      ...defaultState,
    });
  });

  test('SET_TAG_ERROR with results, startIndex', () => {
    const projectId = 10;
    let results = consistencyReportReducer(defaultState, {
      type: types.SET_TAG_ERROR,
      projectId,
    });
    expect(results).toEqual({
      ...defaultState,
    });
  });

  test('SET_TAG_ERROR with projectId', () => {
    const projectId = 10;
    let results = consistencyReportReducer({ ...defaultState, projectId }, {
      type: types.SET_TAG_ERROR,
      projectId,
    });
    expect(results).toEqual({
      ...defaultState, projectId, isUpdatingTags: -1, delay: 3000,
    });
  });

  test('CONSISTENCY_REPORT_REFRESH with delay', () => {
    const delay = 200;
    let results = consistencyReportReducer(undefined, {
      type: types.CONSISTENCY_REPORT_REFRESH,
      delay,
    });
    expect(results).toEqual({
      ...defaultState, delay, doSearch: true,
    });
  });

  test('SHOW_BULK_TAG_CELL', () => {
    let results = consistencyReportReducer(undefined, {
      type: types.SHOW_BULK_TAG_CELL,
    });
    expect(results).toEqual({
      ...defaultState, showBulkTag: true, updateBulkTag: null,
    });
  });

  test('HIDE_BULK_TAG_CELL', () => {
    let results = consistencyReportReducer(undefined, {
      type: types.HIDE_BULK_TAG_CELL,
    });
    expect(results).toEqual({
      ...defaultState, showBulkTag: false, updateBulkTag: null,
    });
  });

  test('SELECTED_TAG_SUGGEST with selectedSuggestTag', () => {
    const selectedSuggestTag = 'ffdfdf';
    let results = consistencyReportReducer(undefined, {
      type: types.SELECTED_TAG_SUGGEST,
      selectedSuggestTag,
    });
    expect(results).toEqual({
      ...defaultState, updateBulkTag: selectedSuggestTag,
    });
  });

  test('SELECTED_BULK_TAG with bulkTagIntent', () => {
    const bulkTagIntent = 'dffsfs';
    let results = consistencyReportReducer(undefined, {
      type: types.SELECTED_BULK_TAG,
      bulkTagIntent,
    });
    expect(results).toEqual({
      ...defaultState, updateBulkTag: bulkTagIntent,
    });
  });
});
