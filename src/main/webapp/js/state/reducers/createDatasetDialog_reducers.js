import * as types from 'state/actions/types';

export const defaultState = {
  step: 'upload', // upload | mapping | confirm_ok | confirm_table | confirm_delete
  columns: null,
  previewData: null,
  bindingArray: [],
  columnsBinding: {},
  isPreSelected: false,
  isBindingValid: false,
  skipFirstRow: true,
  isMappingRequestLoading: false,
  isCommitRequestLoading: false,
  token: null,
  fileId: null,
};

function createDatasetDialogReducer(state = { ...defaultState }, action) {
  switch (action.type) {
  case types.FILE_REMOVE_REQUEST: {
    const { fileId } = action;
    if (fileId === state.token) {
      return Object.assign({}, state, {
        step: 'upload',
        columns: null,
        previewData: null,
        bindingArray: [],
        columnsBinding: {},
        isPreSelected: false,
        isBindingValid: false,
        skipFirstRow: true,
        isMappingRequestLoading: false,
        isCommitRequestLoading: false,
        token: null,
        fileId: null,
      });
    }
    return state;
  }

  case types.CREATE_DATASET_DIALOG_FILE_UPLOAD_SUCCESS: {
    const { token, columns, previewData } = action;
    return Object.assign({}, state, {
      token,
      columns,
      previewData,
      bindingArray: [],
      columnsBinding: {},
      isPreSelected: false,
      isBindingValid: false,
      skipFirstRow: true,
      isMappingRequestLoading: false,
      isCommitRequestLoading: false,
    });
  }

  case types.CREATE_DATASET_DIALOG_RESET: {
    return Object.assign({}, state, {
      step: 'upload',
      columns: null,
      previewData: null,
      bindingArray: [],
      columnsBinding: {},
      isPreSelected: false,
      isBindingValid: false,
      skipFirstRow: true,
      isMappingRequestLoading: false,
      isCommitRequestLoading: false,
      token: null,
      fileId: null,
    });
  }

  case types.CREATE_DATASET_DIALOG_CHANGE_STEP: {
    const { step } = action;
    return Object.assign({}, state, {
      step,
    });
  }

  case types.CREATE_DATASET_DIALOG_COLUMNS_BIND: {
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

  case types.CREATE_DATASET_DIALOG_FISRT_ROW_SKIP_CHANGE: {
    const { skipFirstRow } = action;
    return Object.assign({}, state, {
      skipFirstRow,
    });
  }

  case types.CREATE_DATASET_DIALOG_COLUMNS_BIND_REQUEST: {
    return Object.assign({}, state, {
      isMappingRequestLoading: true,
    });
  }

  case types.CREATE_DATASET_DIALOG_COLUMNS_BIND_ERROR: {
    return Object.assign({}, state, {
      isMappingRequestLoading: false,
    });
  }

  case types.CREATE_DATASET_DIALOG_COLUMNS_BIND_SUCCESS: {
    return Object.assign({}, state, {
      fileId: action.fileId,
      isMappingRequestLoading: false,
      step: 'confirm',
    });
  }

  case types.CREATE_DATASET_REQUEST: {
    return Object.assign({}, state, {
      isCommitRequestLoading: true,
    });
  }

  case types.CREATE_DATASET_ERROR: {
    return Object.assign({}, state, {
      isCommitRequestLoading: false,
    });
  }

  case types.CREATE_DATASET_SUCCESS: {
    return Object.assign({}, state, {
      isCommitRequestLoading: true,
    });
  }

  default:
    return state;
  }
}

export { createDatasetDialogReducer };
