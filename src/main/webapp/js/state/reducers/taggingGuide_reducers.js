import * as types from 'state/actions/types';

export const defaultState = {
  initialized: false,
  projectId: null,
  project: null,
  searching: false,
  searchResults: [],
  sort: { property: 'count', direction: 'desc' },
  lastImportInfo: null,
  usedForTaggingRatio: 0,
};

function taggingGuideReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.TAGGING_GUIDE_RECEIVE_PROJECT:
    return Object.assign({}, state, {
      projectId: action.project.id,
      project: action.project,
      initialized: true,
    });

  case types.TAGGING_GUIDE_REQUEST_SEARCH:
    return Object.assign({}, state, {
      sort: action.sort,
      searching: true,
      searchResults: [],
    });

  case types.TAGGING_GUIDE_RECEIVE_SEARCH:
    return Object.assign({}, state, {
      searchResults: action.searchResults,
      searching: false,
      usedForTaggingRatio: action.usedForTaggingRatio,
    });

  case types.TAGGING_GUIDE_RECEIVE_TAG_REMOVE: {
    let searchResults = [...state.searchResults];
    let { usedForTaggingRatio } = state;
    // remove the selected tag
    const tagData = searchResults[action.index];
    searchResults.splice(action.index, 1);
    if (usedForTaggingRatio > 0) {
      usedForTaggingRatio = (usedForTaggingRatio * searchResults.length - (tagData.count > 0 ? 1 : 0)) / (searchResults.length - 1);
    }
    return Object.assign({}, state, {
      searchResults,
      usedForTaggingRatio,
    });
  }

  case types.TAGGING_GUIDE_RECEIVE_TAG_UPDATE: {
    let existingTag = state.searchResults.findIndex(item => item.id === action.values.id);
    let searchResults = [...state.searchResults];
    searchResults[existingTag] = Object.assign({}, searchResults[existingTag], action.values);

    let { usedForTaggingRatio } = state;
    if (usedForTaggingRatio > 0) {
      usedForTaggingRatio = usedForTaggingRatio * searchResults.length / (searchResults.length + 1);
    }
    return Object.assign({}, state, {
      searchResults,
    });
  }
  case types.TAGGING_GUIDE_RECEIVE_TAG_CREATE: {
    let searchResults = [action.values, ...state.searchResults];
    let { usedForTaggingRatio } = state;
    if (usedForTaggingRatio > 0) {
      usedForTaggingRatio = usedForTaggingRatio * searchResults.length / (searchResults.length + 1);
    }
    return Object.assign({}, state, {
      searchResults,
      usedForTaggingRatio,
    });
  }
  case types.PROJECT_CLOSE:
  case types.CLIENT_CHANGE:
  case types.CLEAR_SELECTED_CLIENT:
  case types.TAGGING_GUIDE_RESET:
    return Object.assign({}, state, defaultState);

  case types.TAGGING_GUIDE_RECEIVE_LAST_IMPORT_INFO:
    return Object.assign({}, state, {
      lastImportInfo: action.info,
    });

  default:
    return state;
  }
}

export { taggingGuideReducer };
