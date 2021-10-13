import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog } from '@tfs/ui-components';
import { importFile } from 'state/actions/actions_dataset_create';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from '../../constants/Constants';
import Form from '../Form/Form';
import * as appActions from '../../state/actions/actions_app';
import { pathKey } from '../../utils/apiUrls';

import createDatasetUiSchema from '../schema/createDataset/uiSchema.json';
import createDatasetUiSchemaExt from '../schema/createDataset/uiSchemaExternal.json';
import createDatasetJsonSchema from '../schema/createDataset/jsonSchema.json';
import createDatasetJsonSchemaExt from '../schema/createDataset/jsonSchemaExternal.json';
import CustomTextField from '../fields/CustomTextField';
import CustomDropDown from '../fields/CustomDropDown';
import CustomDropZone from '../fields/CustomDropZone';

export default class DatasetDialog extends Component {
  constructor(props) {
    super(props);
    this.form = React.createRef();
    this.props = props;

    this.onClickNext = this.onClickNext.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
    this.onFormChange = this.onFormChange.bind(this);

    this.styleOverride = Object.assign({}, {
      childContainer: {
        marginTop: '0px',
        marginBottom: '0px',
      },
      container: {
        width: '446px',
        height: '460px',
        display: 'grid',
        gridTemplateRows: '60px auto 60px',
      },
    },
    this.props.styleOverride);

    this.state = {
      formData: {},
      isValid: false,
      datasetFile: null,
    };

    this.fields = {
      text: CustomTextField,
      select: (props, url = pathKey.datatypes) => <CustomDropDown {...props} url={pathKey.datatypes} defaultValue={Constants.CHOOSE_TYPE} />,
      dropZone: (props) => <CustomDropZone {...props} />,
    };
  }

  onClickNext() {
    this.form.current.submit();
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(appActions.modalDialogChange(null));
  }

  onSubmit() {
    const { formData } = this.state;
    const {
      dispatch, project, userFeatureConfiguration,
    } = this.props;
    const { id: projectId, clientId, locale } = project;

    const { names } = featureFlagDefinitions;

    let importData = {
      ...formData,
      projectId,
      clientId,
      locale,
    };

    if (!isFeatureEnabled(names.datasetType, userFeatureConfiguration)) {
      importData = { ...importData, dataType: 'Chat/Text' };
    }

    dispatch(importFile(importData));
  }

  onFormChange(data, errors) {
    if (errors.length === 0 && Object.entries(data.dropZone).length !== 0) {
      this.setState({
        formData: data,
        isValid: true,
      });
    } else if (errors.length > 0) {
      this.setState({
        isValid: false,
      });
    }
  }

  render() {
    const {
      header, size, formData, errorType, userFeatureConfiguration, ...otherProps
    } = this.props;

    const { isValid } = this.state;
    const { names } = featureFlagDefinitions;

    return (
      <div>
        <Dialog
          size={size}
          headerChildren={header}
          {...otherProps}
          isOpen
          showBodySeperator={false}
          okVisible
          okDisabled={!isValid}
          okChildren={Constants.NEXT}
          cancelVisible
          cancelChildren={Constants.CANCEL}
          closeIconVisible
          styleOverride={this.styleOverride}
          onClickOk={this.onClickNext}
          onClickCancel={this.onClickCancel}
          onClickClose={this.onClickCancel}
          centerContent={false}
        >
          <div>
            <div>
              <Form
                uiSchema={isFeatureEnabled(names.datasetType, userFeatureConfiguration) ? createDatasetUiSchema : createDatasetUiSchemaExt}
                jsonSchema={isFeatureEnabled(names.datasetType, userFeatureConfiguration) ? createDatasetJsonSchema : createDatasetJsonSchemaExt}
                fields={this.fields}
                onSubmit={this.onSubmit}
                onChange={this.onFormChange}
                formData={formData}
                ref={this.form}
                liveValidate
              />
            </div>
          </div>
        </Dialog>
      </div>
    );
  }
}

DatasetDialog.propTypes = {
  dispatch: PropTypes.func,
  project: PropTypes.object,
  header: PropTypes.string,
  closeIconVisible: PropTypes.bool,
  onClickOk: PropTypes.func,
  onClickClose: PropTypes.func,
  styleOverride: PropTypes.object,
  size: PropTypes.string,
  formData: PropTypes.object,
  errorType: PropTypes.string,
  userFeatureConfiguration: PropTypes.object,
};

DatasetDialog.defaultProps = {
  dispatch: () => {},
  project: {},
  closeIconVisible: true,
  onClickOk: () => {},
  onClickClose: () => {},
  styleOverride: {},
  size: 'large',
  formData: {},
  errorType: '',
  header: Constants.CREATE_MODEL,
  userFeatureConfiguration: {},
};
