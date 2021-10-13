import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import { Button, Checkbox } from '@tfs/ui-components';
import {
  modalDialogChange, removeFile, displayBadRequestMessage, displayGoodRequestMessage,
} from 'state/actions/actions_app';
import * as actionsImport from 'state/actions/actions_taggingguideimport';
import TaggerModal from 'components/controls/TaggerModal';
import DropzoneComponent from 'react-dropzone-component';
import Box from 'grommet/components/Box';
import BindColumnsPreviewGrid from 'components/controls/BindColumnsPreviewGrid';
import getUrl, { pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

export class TaggingGuideImportDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.state = {
    };

    this.onClickCancel = this.onClickCancel.bind(this);
    this.onColumnBindChange = this.onColumnBindChange.bind(this);
    this.onChangeStep = this.onChangeStep.bind(this);
    this.onCommit = this.onCommit.bind(this);
    this.onRequestColumnsBind = this.onRequestColumnsBind.bind(this);
    this.onChangeFirstRowSkip = this.onChangeFirstRowSkip.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;

    const state = this.props.taggingGuideImport;
    const { done, validTagCount } = state;
    const { dispatch } = this.props;
    if (done) {
      dispatch(modalDialogChange(null));
      dispatch(displayGoodRequestMessage(DISPLAY_MESSAGES.taggingGuideImportSuccess(validTagCount)));
    }
  }

  onChangeStep(step) {
    const { dispatch } = this.props;
    dispatch(actionsImport.changeStep({ step }));
  }

  onCommit() {
    const {
      dispatch, project, clientId, taggingGuideImport,
    } = this.props;
    const { token } = taggingGuideImport;
    dispatch(actionsImport.commit({ projectId: project.id, token, clientId }));
  }

  onRequestColumnsBind() {
    const {
      dispatch, project, taggingGuideImport,
    } = this.props;
    const { bindingArray, token, skipFirstRow } = taggingGuideImport;
    dispatch(actionsImport.requestColumnsBind({
      projectId: project.id,
      token,
      bindingArray,
      skipFirstRow,
      clientId: project.clientId,
    }));
  }

  onChangeFirstRowSkip() {
    const { dispatch, taggingGuideImport } = this.props;
    const { skipFirstRow } = taggingGuideImport;
    dispatch(actionsImport.changeFirstRowSkip({ skipFirstRow: !skipFirstRow }));
  }

  componentWillUnmount() {
    const state = this.props.taggingGuideImport;
    const { token, done } = state;
    const { dispatch } = this.props;
    if (token && !done) {
      const { project, clientId } = this.props;
      dispatch(removeFile({ fileId: token }));
      dispatch(actionsImport.abort({ projectId: project.id, token, clientId }));
    }
    dispatch(actionsImport.reset());
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(modalDialogChange(null));
  }

  get setpUplaodContent() {
    const {
      project, csrfToken, userName, dispatch,
    } = this.props;

    const postUrl = getUrl(pathKey.intentGuideImport, { projectId: project.id, clientId: project.clientId });

    const config = {
      postUrl,
      iconFiletypes: ['.csv'],
      showFiletypeIcon: true,
    };
    const djsConfig = {
      ...Constants.DROPZONEJS_COMMON_CONFIG,
      timeout: Constants.TAGGING_GUIDE_UPLOAD_TIMEOUT, // in milliseconds = 2 min
      dictDefaultMessage: Constants.UPLOAD_DEFAULT_MSG,
      params: {
        username: userName,
      },
      headers: {
        'X-CSRF-Token': csrfToken,
      },
      accept(file, done) {
        file.fileId = '';
        return done();
      },
    };

    const eventHandlers = {
      success(file, response) {
        let { token, columns, previewData } = response;
        if (previewData && previewData.length > 10) {
          previewData = previewData.slice(0, 10);
        }
        file.token = token;
        dispatch(actionsImport.fileUploadSuccess({ token, columns, previewData }));
      },

      error(file, errorMessage/* , response */) {
        dispatch(displayBadRequestMessage(DISPLAY_MESSAGES.fileImportFail));
        // reset all dropzone error messages to proper message
        const message = (errorMessage.message) ? errorMessage.message : errorMessage;
        const _ref = file.previewElement.querySelectorAll('[data-dz-errormessage]');
        const _results = [];
        for (let _i = 0, _len = _ref.length; _i < _len; _i++) {
          const node = _ref[_i];
          _results.push(node.textContent = message);
        }
        return _results;
      },

      sending(/* file, xhr, formData */) {
        // TODO: need to send datatype with file upload?
        // formData.append("datatype", datatype);
      },

      removedfile(file) {
        if (file.token) {
          dispatch(removeFile({
            fileId: file.token,
          }));
        }
      },
    };

    return (
      <div id="TaggingGuideUpload">
        <div>
          <label htmlFor="TaggingGuideUploadFilepicker">Upload File*</label>
        </div>
        <div id="TaggingGuideUploadFilepicker">
          <DropzoneComponent
            config={config}
            djsConfig={djsConfig}
            eventHandlers={eventHandlers}
          />
        </div>
      </div>
    );
  }

  get setpMappingContent() {
    const state = this.props.taggingGuideImport;

    const {
      previewData, columns, skipFirstRow, columnsBinding, isPreSelected,
    } = state;
    const { dispatch } = this.props;
    let columnsRequired = [],
      columnsSelected = [],
      columnsSkipped = [];
    if (columns && columns.length) {
      columns.forEach((c, i) => {
        let isRequired = false;
        if (c.required) {
          isRequired = true;
          columnsRequired.push(<span className="Required" key={i}>{c.displayName}</span>);
        }
        if (typeof columnsBinding[c.name] !== 'undefined') {
          columnsSelected.push(<span className={`Selected${isRequired ? ' Required' : ''}`} key={i}>{c.displayName}</span>);
        } else {
          columnsSkipped.push(<span className={`Skipped${isRequired ? ' Required' : ''}`} key={i}>{c.displayName}</span>);
        }
      });
    }

    return (
      <Box
        id="TaggingGuideMapping"
        className="TaggerMapping"
        flex
        direction="column"
      >
        <Box
          className="ContentRow"
        >
                    Please choose corresponding columns for the file uploaded.
          {isPreSelected ? <div className="notice">Notice: some columns were selected automatically based on first row data.</div> : null}
        </Box>
        <Box
          className="ContentRow BindingInfo"
          direction="column"
        >
          <Box direction="row">
            <Box className="InfoLabel">Required:</Box>
            <Box className={`InfoText${columnsRequired.length ? '' : ' Empty'}`} flex direction="row">
              {columnsRequired.length ? columnsRequired.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
            </Box>
          </Box>
          <Box direction="row">
            <Box className="InfoLabel">Selected:</Box>
            <Box className={`InfoText${columnsSelected.length ? '' : ' Empty'}`} flex direction="row">
              {columnsSelected.length ? columnsSelected.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
            </Box>
          </Box>
          <Box direction="row">
            <Box className="InfoLabel">Skipped:</Box>
            <Box className={`InfoText${columnsSkipped.length ? '' : ' Empty'}`} flex direction="row">
              {columnsSkipped.length ? columnsSkipped.reduce((prev, curr) => [prev, ', ', curr]) : <span>none</span>}
            </Box>
          </Box>
        </Box>
        <BindColumnsPreviewGrid
          data={previewData}
          columns={columns}
          columnsBinding={columnsBinding}
          onColumnBindChange={this.onColumnBindChange}
        />
        <Box
          className="ContentRow SkipFirstRow"
        >
          <Checkbox
            checked={skipFirstRow}
            value="skip-first-row"
            label="Skip First Row"
            onChange={this.onChangeFirstRowSkip}
          />
        </Box>
      </Box>
    );
  }

  onColumnBindChange({
    bindingArray, isBindingValid, columnsBinding, isPreSelected,
  }) {
    const { dispatch } = this.props;
    dispatch(actionsImport.columnsBind({
      bindingArray, isBindingValid, columnsBinding, isPreSelected,
    }));
  }

  get setpConfirmContent() {
    const state = this.props.taggingGuideImport;
    const {
      validTagCount, missingTags, invalidTags, step,
    } = state;

    if (step == 'confirm_table') {
      return (
        <Box
          id="TaggingGuideConfirm"
          direction="column"
          flex
        >
          <Box
            className="ValidTagsCount"
          >
            {validTagCount}
            {' '}
tag
            {validTagCount == 1 ? '' : 's'}
            {' '}
will be added.
          </Box>
          <Box
            direction="column"
            className="TagsSummaryTable"
            flex
            separator="all"
          >
            {
              (invalidTags && invalidTags.length > 0)
                ? (
                  <Box
                    direction="row"
                    className="TagsSummaryTableRow"
                    flex
                    separator={(missingTags && missingTags.length > 0) ? 'bottom' : 'none'}
                  >
                    <Box className="TagsSummaryTableRowName" separator="right">
                                        Invalid Tags
                    </Box>
                    <Box flex className="TagsSummaryTableRowValue">
                      <textarea
                        readOnly
                        value={invalidTags.join(', ')}
                      />
                    </Box>
                  </Box>
                )
                : null
            }
            {
              (missingTags && missingTags.length > 0)
                ? (
                  <Box direction="row" className="TagsSummaryTableRow" flex>
                    <Box className="TagsSummaryTableRowName" separator="right">
                                        Missing Tags
                    </Box>
                    <Box flex className="TagsSummaryTableRowValue">
                      <textarea
                        readOnly
                        value={missingTags.join(', ')}
                      />
                    </Box>
                  </Box>
                )
                : null
            }
          </Box>
        </Box>
      );
    }
    if (step == 'confirm_delete') {
      return (
        <Box
          id="TaggingGuideConfirm"
          direction="column"
          flex
        >
          <Box
            className="ValidTagsCount"
          >
                        Are you sure you want to delete tags?
          </Box>
          <Box>
                        This operation will untag unique strings with these tags:
          </Box>
          <Box
            direction="row"
            className="TagsSummaryTableRow"
            flex
          >
            <Box flex className="TagsSummaryTableRowValue" separator="all">
              <textarea
                readOnly
                value={missingTags.join(', ')}
              />
            </Box>
          </Box>
        </Box>
      );
    } /* confirm_ok */

    return (
      <Box
        id="TaggingGuideConfirm"
        direction="column"
        flex
      >
        <Box
          className="ValidTagsCount"
        >
          {validTagCount}
          {' '}
tag
          {validTagCount == 1 ? '' : 's'}
          {' '}
will be added.
        </Box>
        <Box>
                        Do you want to import?
        </Box>
      </Box>
    );
  }


  get content() {
    const state = this.props.taggingGuideImport;
    const { step } = state;

    switch (step) {
    case 'mapping':
      return this.setpMappingContent;

    case 'confirm_ok':
    case 'confirm_table':
    case 'confirm_delete':
      return this.setpConfirmContent;

    case 'upload':
    default:
      return this.setpUplaodContent;
    }
  }

  get actions() {
    const state = this.props.taggingGuideImport;
    const {
      step,
      isBindingValid,
      bindingArray,
      token,
      skipFirstRow,
      isMappingRequestLoading,
      missingTags,
      isCommitRequestLoading,
    } = state;
    const { project, clientId, dispatch } = this.props;
    switch (step) {
    case 'mapping':
      return [
        <Button
          name="cancel"
          type="flat"
          key="Cancel"
          onClick={isMappingRequestLoading ? null : this.onClickCancel}
        >
          CANCEL
        </Button>,
        <Button
          name="next"
          key="Next"
          onClick={isBindingValid && !isMappingRequestLoading ? this.onRequestColumnsBind : null}
        >
          NEXT
        </Button>,
      ];

    case 'confirm_table':
      if (missingTags && missingTags.length > 0) {
        return [
          <Button
            name="cancel"
            type="flat"
            key="Cancel"
            onClick={this.onClickCancel}
          >
          CANCEL
          </Button>,
          <Button
            name="back"
            key="Back"
            onClick={() => this.onChangeStep('mapping')}
          >
            BACK
          </Button>,
          <Button
            name="next"
            key="Next"
            onClick={() => this.onChangeStep('confirm_delete')}
          >
          NEXT
          </Button>,
        ];
      }
      return [
        <Button
          name="cancel"
          type="flat"
          key="Cancel"
          onClick={isCommitRequestLoading ? null : this.onClickCancel}
        >
        CANCEL
        </Button>,
        <Button
          name="back"
          key="Back"
          onClick={isCommitRequestLoading ? null : () => this.onChangeStep('mapping')}
          styleOverride={{
            margin: '0px 10px',
          }}
        >
          BACK
        </Button>,
        <Button
          name="import"
          key="Import"
          onClick={isCommitRequestLoading ? null : this.onCommit}
        >
        IMPORT
        </Button>,
      ];


    case 'confirm_ok':
    case 'confirm_delete':
      return [
        <Button
          type="flat"
          name="cancel"
          key="Cancel"
          onClick={isCommitRequestLoading ? null : this.onClickCancel}
        >
        CANCEL
        </Button>,
        <Button
          name="back"
          key="Back"
          label="Back"
          onClick={isCommitRequestLoading ? null : () => this.onChangeStep('mapping')}
          styleOverride={{
            margin: '0px 10px',
          }}
        >
        BACK
        </Button>,
        <Button
          name="import"
          key="Import"
          label="Import"
          onClick={isCommitRequestLoading ? null : this.onCommit}
        >
        IMPORT
        </Button>,
      ];

    case 'upload':
    default:
      return [
        <Button
          type="flat"
          name="cancel"
          key="Cancel"
          onClick={this.onClickCancel}
        >
        CANCEL
        </Button>,
        <Button
          name="next"
          key="Next"
          onClick={null}
        >
        NEXT
        </Button>,
      ];
    }
  }

  get header() {
    const state = this.props.taggingGuideImport;
    const { step } = state;
    switch (step) {
    case 'confirm': {
      const { validTagCount } = state;
      return `${validTagCount} tags will be added`;
    }

    case 'mapping':
      return 'Columns mapping';

    case 'upload':
    default: {
      const { project } = this.props;
      return `Import Tagging Guide for ${project.name}`;
    }
    }
  }

  render() {
    const state = this.props.taggingGuideImport;
    const { step } = state;
    return (
      <TaggerModal
        id="TaggingGuideImportDialog"
        className={`TaggingGuideImportDialog ${step}`}
        header={this.header}
        content={this.content}
        actions={this.actions}
        resize={step == 'mapping'}
      />
    );
  }
}

TaggingGuideImportDialog.propTypes = {
  project: PropTypes.object.isRequired,
};

const mapStateToProps = state => ({
  userId: state.app.userId,
  csrfToken: state.app.csrfToken,
  taggingGuideImport: state.taggingGuideImport,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(TaggingGuideImportDialog));
