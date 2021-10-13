import * as types from 'state/actions/types';

export const defaultState = {
  step: 'upload', // upload | mapping | confirm_ok | confirm_table | confirm_delete
  token: null,
  columns: null,
  previewData: null,
  bindingArray: [],
  columnsBinding: {},
  isPreSelected: false,
  isBindingValid: false,
  skipFirstRow: true,
  isMappingRequestLoading: false,
  validTagCount: 0,
  missingTags: [],
  invalidTags: [],
  isCommitRequestLoading: false,
  done: false,
};

function taggingGuideImportReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.FILE_REMOVE_REQUEST: {
    const { fileId } = action;
    if (fileId === state.token) {
      return Object.assign({}, state, {
        step: 'upload',
        token: null,
        columns: null,
        previewData: null,
        bindingArray: [],
        columnsBinding: {},
        isPreSelected: false,
        isBindingValid: false,
        skipFirstRow: true,
        isMappingRequestLoading: false,
        validTagCount: 0,
        missingTags: [],
        invalidTags: [],
        isCommitRequestLoading: false,
        done: false,
      });
    }
    return state;
  }

  case types.TAGGING_GUIDE_IMPORT_FILE_UPLOAD_SUCCESS: {
    const { token, columns, previewData } = action;
    return Object.assign({}, state, {
      step: 'mapping',
      token,
      columns,
      previewData,
      bindingArray: [],
      columnsBinding: {},
      isBindingValid: false,
      isPreSelected: false,
      skipFirstRow: true,
      isMappingRequestLoading: false,
      validTagCount: 0,
      missingTags: [],
      invalidTags: [],
      isCommitRequestLoading: false,
      done: false,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_RESET: {
    return Object.assign({}, state, {
      step: 'upload',
      token: null,
      columns: null,
      previewData: null,
      bindingArray: [],
      columnsBinding: {},
      isPreSelected: false,
      isBindingValid: false,
      skipFirstRow: true,
      isMappingRequestLoading: false,
      validTagCount: 0,
      missingTags: [],
      invalidTags: [],
      isCommitRequestLoading: false,
      done: false,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND: {
    const {
      bindingArray, isBindingValid, columnsBinding, isPreSelected,
    } = action;
    return Object.assign({}, state, {
      bindingArray,
      isBindingValid,
      columnsBinding,
      isPreSelected: typeof isPreSelected !== 'undefined' ? isPreSelected : state.isPreSelected,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_FISRT_ROW_SKIP_CHANGE: {
    const { skipFirstRow } = action;
    return Object.assign({}, state, {
      skipFirstRow,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_REQUEST: {
    return Object.assign({}, state, {
      isMappingRequestLoading: true,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_ERROR: {
    return Object.assign({}, state, {
      isMappingRequestLoading: false,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COLUMNS_BIND_SUCCESS: {
    const { validTagCount, missingTags, invalidTags } = action;
    let step = 'confirm_ok';
    if (
      (invalidTags && invalidTags.length > 0)
                || (missingTags && missingTags.length > 0)
    ) {
      step = 'confirm_table';
    }

    return Object.assign({}, state, {
      isMappingRequestLoading: false,
      validTagCount,
      missingTags,
      invalidTags,
      step,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_CHANGE_STEP: {
    const { step } = action;
    return Object.assign({}, state, {
      step,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COMMIT_REQUEST: {
    return Object.assign({}, state, {
      isCommitRequestLoading: true,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COMMIT_ERROR: {
    return Object.assign({}, state, {
      isCommitRequestLoading: false,
    });
  }

  case types.TAGGING_GUIDE_IMPORT_COMMIT_SUCCESS: {
    return Object.assign({}, state, {
      isCommitRequestLoading: false,
      done: true,
    });
  }

  default:
    return state;
  }
}

export { taggingGuideImportReducer };
