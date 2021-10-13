import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import { Button, Dialog, Checkbox } from '@tfs/ui-components';
import Model from 'model';
import {
  modalDialogChange, removeFile, displayBadRequestMessage,
  displayGoodRequestMessage, stopShowingServerMessage,
} from 'state/actions/actions_app';
import {
  reset, fileUploadSuccess, changeStep, columnsBind,
  changeFirstRowSkip, requestColumnsBind, createDataset, fileUpload,
} from 'state/actions/actions_dataset_create';
import { fetchDatasetTransform } from 'state/actions/actions_datasets';
import TaggerModal from 'components/controls/TaggerModal';
import Box from 'grommet/components/Box';
import DropzoneComponent from 'react-dropzone-component';
import CreateDatasetForm from 'components/datasets/create/CreateDatasetForm';
import BindColumnsPreviewGrid from 'components/controls/BindColumnsPreviewGrid';
import getUrl, { pathKey } from 'utils/apiUrls';
import Constants from 'constants/Constants';
import { getLanguage } from 'state/constants/getLanguage';
import Upload from 'rc-upload';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';

const languageConstants = getLanguage();
const { DISPLAY_MESSAGES } = languageConstants;

class CreateDatasetDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickCancel = this.onClickCancel.bind(this);
    this.onColumnBindChange = this.onColumnBindChange.bind(this);
    this.uploaderProps = {};
  }

  componentWillReceiveProps(nextProps) {
    this.props = nextProps;
  }

  componentDidMount() {
  }

  componentWillUnmount() {
    const { state, dispatch } = this.props;
    const { token } = state;

    if (token) {
      // TODO: remove file?
    }
    dispatch(reset());
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(stopShowingServerMessage());
    dispatch(modalDialogChange(null));
  }

  get setpUplaodContent() {
    const {
      csrfToken, userName, clientId, dispatch, environment,
    } = this.props;

    const postUrl = getUrl(pathKey.importDataset, { clientId });

    const componentConfig = {
      postUrl,
      iconFiletypes: ['.csv'],
      showFiletypeIcon: true,
    };

    const tfsDjsConfig = {
      ...Constants.DROPZONEJS_COMMON_CONFIG,
      timeout: Constants.DATASET_UPLOAD_TIMEOUT, // in milliseconds = 10 min
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

    const onFileDroppedConfig = {
      success: (file, response) => {
        dispatch(stopShowingServerMessage());
        let { token, columns, previewData } = response;
        if (previewData && previewData.length > 10) {
          previewData = previewData.slice(0, 10);
        }
        file.token = token;
        dispatch(fileUploadSuccess({ token, columns, previewData }));
      },
      error: (file, errorMessage/* , response */) => {
        const { code } = errorMessage;
        const message = (errorMessage.message) ? errorMessage.message : errorMessage;
        if (code == 400) {
          dispatch(displayBadRequestMessage(message));
        } if (code == 401) {
          dispatch(displayBadRequestMessage(DISPLAY_MESSAGES.sessionExpired));
        } else {
          dispatch(displayBadRequestMessage(DISPLAY_MESSAGES.fileParseFail));
        }
        // reset all dropzone error messages to proper message
        const _ref = file.previewElement.querySelectorAll('[data-dz-errormessage]');
        const _results = [];
        for (let _i = 0, _len = _ref.length; _i < _len; _i++) {
          const node = _ref[_i];
          _results.push(node.textContent = message);
        }
        return _results;
      },
      sending: (/* file, xhr, formData */) => {
        // TODO: need to send datatype with file upload?
        // formData.append("datatype", datatype);
      },
      removedfile: (file) => {
        dispatch(stopShowingServerMessage());
        if (file.token) {
          dispatch(removeFile({
            fileId: file.token,
          }));
        }
      },
    };

    this.uploaderProps = {
      multiple: false,
      accept: 'text/csv',

      customRequest: (input) => {
        const { file } = input;
        dispatch(fileUpload(
          file,
          (response) => {
            const res = JSON.parse(response);
            onFileDroppedConfig.success(file, res);
          },
          (error) => {
            dispatch(displayBadRequestMessage(DISPLAY_MESSAGES.fileImportFail));
          },
        ));
      },
    };

    return (
      <div id="CreateDatasetUpload" className="TaggerForm">
        <div className="Field">
          <div>
            <label htmlFor="dataset-filepicker">Upload File*</label>
            <div className="confidential-text">Do not upload any data that contains personally identifiable information</div>
          </div>
          <div id="dataset-filepicker">
            <DropzoneComponent
              config={componentConfig}
              djsConfig={tfsDjsConfig}
              eventHandlers={onFileDroppedConfig}
            />
          </div>
          { (environment === Constants.ENVIRONMENTS.DEV || environment === Constants.ENVIRONMENTS.QA || environment === Constants.ENVIRONMENTS.PSR)
            ? (
              <div className="upload-configuration">
                <Upload
                  {...this.uploaderProps}
                  component="a"
                >
                  <span> Upload</span>
                </Upload>
              </div>
            ) : null}
        </div>
      </div>
    );
  }

  get setpMappingContent() {
    const { state, dispatch } = this.props;
    const {
      previewData,
      columns,
      skipFirstRow,
      columnsBinding,
      isPreSelected,
      isMappingRequestLoading,
      isBindingValid,
      bindingArray,
      token,
    } = state;

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
      <Dialog
        isOpen
        okVisible
        okChildren="NEXT"
        onClickOk={isBindingValid && !isMappingRequestLoading ? function () {
          dispatch(requestColumnsBind({
            token,
            bindingArray,
            skipFirstRow,
          }));
        } : null}
        closeIconVisible
        onClickClose={this.onClickCancel}
        cancelVisible
        onClickCancel={isMappingRequestLoading ? null : this.onClickCancel}
        cancelChildren={Constants.CANCEL}
        headerChildren={Constants.COLUMNS_MAPPER}
        centerContent={false}
        styleOverride={{
          container: {
            width: '800px',
            height: '575px',
            display: 'grid',
            gridTemplateRows: '60px auto 60px',
          },
          ok: {
            marginLeft: '10px',
            paddingLeft: '25px',
            paddingRight: '25px',
          },
          overlay: {
            zIndex: 9000,
          },
          header: {
            width: '100%',
            height: '100%',
            borderBottom: '1px solid',
            borderBottomColor: '#DADADA',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            title: {
              marginLeft: '23px',
              marginRight: '20px',
              fontSize: '20px',
              fontWeight: 'bold',
            },
            titleIcon: {
              marginRight: '20px',
            },
          },
        }}
      >
        <Box
          id="CreateDatasetMapping"
          className="TaggerMapping"
          flex
          direction="column"
        >
          <Box>
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
              onChange={() => {
                dispatch(changeFirstRowSkip({ skipFirstRow: !skipFirstRow }));
              }}
            />
          </Box>
        </Box>
      </Dialog>
    );
  }

  onColumnBindChange({
    bindingArray, isBindingValid, columnsBinding, isPreSelected,
  }) {
    const { dispatch } = this.props;
    dispatch(columnsBind({
      bindingArray, isBindingValid, columnsBinding, isPreSelected,
    }));
  }

  get setpConfirmContent() {
    const { project } = this.props;

    const models = Model.ProjectsManager.getModelsByProjectId(project.id);

    let suggestIntentDisabled = (_.isNil(models)) || models.size === 0;

    return (
      <Box
        id="CreateDatasetConfirm"
        direction="column"
        flex
      >
        <CreateDatasetForm
          suggestIntentDisabled={suggestIntentDisabled}
        />
      </Box>
    );
  }


  get content() {
    const { state } = this.props;
    const { step } = state;
    switch (step) {
    case 'mapping':
      return this.setpMappingContent;

    case 'confirm':
      return this.setpConfirmContent;

    case 'upload':
    default:
      return this.setpUplaodContent;
    }
  }

  get actions() {
    const { state, dispatch, userFeatureConfiguration } = this.props;
    const {
      step, token,
      isCommitRequestLoading, fileId,
    } = state;
    const { names } = featureFlagDefinitions;

    switch (step) {
    case 'upload': {
      return [
        <Button
          name="cancel"
          type="flat"
          key="Cancel"
          onClick={this.onClickCancel}
          data-qa="cancel"
          styleOverride={{
            ':focus': {
              outline: 'none',
            },
          }}
        >
        CANCEL
        </Button>,
        <Button
          name="next"
          key="Next"
          onClick={!token ? null : () => {
            dispatch(changeStep({
              step: 'mapping',
            }));
          }}
          data-qa="next"
        >
        NEXT
        </Button>,
      ];
    }

    case 'confirm': {
      const { form, userId, csrfToken } = this.props;
      const syncErrors = form && form.syncErrors ? form.syncErrors : {};

      return [
        <Button
          name="cancel"
          type="flat"
          key="Cancel"
          onClick={isCommitRequestLoading ? null : this.onClickCancel}
          styleOverride={{
            ':focus': {
              outline: 'none',
            },
          }}
        >
        CANCEL
        </Button>,
        <Button
          name="create"
          key="Create"
          onClick={isCommitRequestLoading || (syncErrors && Object.keys(syncErrors).length) ? null : () => {
            const { project, clientId } = this.props;
            const values = this.props.form && this.props.form.values ? this.props.form.values : {};
            const {
              name, description, autoTag, startTransform,
            } = values;

            let { dataType, suggestIntent } = values;

            if (!isFeatureEnabled(names.datasetType, userFeatureConfiguration)) {
              dataType = Constants.DATASET_TYPE_DEFAULT;
            }

            if (!isFeatureEnabled(names.suggestedIntent, userFeatureConfiguration)) {
              suggestIntent = Constants.SUGGESTED_INTENT_DEFAULT;
            }

            dispatch(createDataset({
              projectId: project.id,
              clientId,
              dataType,
              locale: project.locale,
              name,
              uri: `https://tagging.247-inc.com:8443/nltools/private/v1/files/${fileId}`,
              description,
              autoTagDataset: autoTag,
              mapToProjectId: project.id,
            })).then((dataset) => {
              if (startTransform) {
                setTimeout(() => {
                  dispatch(fetchDatasetTransform(userId, dataset.id, clientId, project.id, csrfToken, false, suggestIntent));
                }, 2000);
              }
              dispatch(displayGoodRequestMessage(DISPLAY_MESSAGES.datasetCreated(dataset.name)));
              dispatch(modalDialogChange(null));
            });
          }}
        >
        CREATE
        </Button>,
      ];
    }

    default:
      return [
        <Button
          name="cancel"
          type="flat"
          key="Cancel"
          onClick={this.onClickCancel}
          styleOverride={{
            ':focus': {
              outline: 'none',
            },
          }}
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
    const { state } = this.props;
    const { step } = state;
    switch (step) {
    case 'confirm':
      return 'Transformation options';

    case 'upload':
    default:
      return 'Upload Dataset';
    }
  }

  render() {
    // TODO: Change all the different dialogs to tfs-ui dialog
    const { state } = this.props;
    const { step } = state;
    if (step === 'mapping') {
      return this.content;
    }
    return (
      <TaggerModal
        id="CreateDatasetDialog"
        className={`CreateDatasetDialog ${step}`}
        header={this.header}
        content={this.content}
        actions={this.actions}
      />
    );
  }
}

CreateDatasetDialog.propTypes = {
  project: PropTypes.object.isRequired,
};

const mapStateToProps = state => ({
  userId: state.app.userId,
  csrfToken: state.app.csrfToken,
  environment: state.app.environment,
  clientId: state.header.client.id,
  state: state.createDatasetDialog,
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  form: state.form.CreateDataset,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(CreateDatasetDialog);
