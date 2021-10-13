import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog, Button } from '@tfs/ui-components';
import * as projectActions from 'state/actions/actions_projects';
import Constants from '../../constants/Constants';
import Form from '../Form/Form';
import * as appActions from '../../state/actions/actions_app';
import { pathKey } from '../../utils/apiUrls';
import createModelUiSchema from '../schema/createModel/uiSchema.json';
import createModelJsonSchema from '../schema/createModel/jsonSchema.json';
import CustomTextField from '../fields/CustomTextField';
import CustomDropDown from '../fields/CustomDropDown';

export default class CreateModelDialog extends Component {
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
    };

    this.fields = {
      text: CustomTextField,
      locale: (props, url = pathKey.locales) => <CustomDropDown {...props} url={pathKey.locales} />,
      vertical: (props, url = pathKey.verticals) => <CustomDropDown {...props} url={pathKey.verticals} />,
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
    const { dispatch, clientId, history } = this.props;
    let data = {
      clientId,
      history,
      values: { clientId, ...formData },
    };
    dispatch(projectActions.createProject(data));
  }

  onFormChange(data, errors) {
    if (errors.length === 0) {
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
      header, size, formData, ...otherProps
    } = this.props;

    const { isValid } = this.state;

    return (
      <div>
        <Dialog
          size={size}
          headerChildren={header}
          {...otherProps}
          isOpen
          okDisabled={!isValid}
          showBodySeperator={false}
          okVisible
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
                uiSchema={createModelUiSchema}
                jsonSchema={createModelJsonSchema}
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

CreateModelDialog.propTypes = {
  dispatch: PropTypes.func,
  history: PropTypes.object,
  header: PropTypes.string,
  closeIconVisible: PropTypes.bool,
  onClickOk: PropTypes.func,
  onClickClose: PropTypes.func,
  styleOverride: PropTypes.object,
  size: PropTypes.string,
  formData: PropTypes.object,
  errorType: PropTypes.string,
};

CreateModelDialog.defaultProps = {
  dispatch: () => {},
  history: {},
  closeIconVisible: true,
  onClickOk: () => {},
  onClickClose: () => {},
  styleOverride: {},
  size: 'large',
  formData: {},
  errorType: '',
  header: Constants.CREATE_MODEL,
};
