import React, { Component } from 'react';
import { Field/* , Fields */, reduxForm, initialize } from 'redux-form';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Box from 'grommet/components/Box';
import Request from 'react-http-request';
import _ from 'lodash';
import * as appActions from 'state/actions/actions_app.js';
import errorMessageUtil from 'utils/ErrorMessageUtil';
import { RouteNames } from 'utils/routeHelpers';
import { Button } from '@tfs/ui-components';
import * as projectActions from 'state/actions/actions_projects';
import Constants from 'constants/Constants';
import LocaleSelect from '../common/LocaleSelect';

const validate = (values) => {
  const errors = {};
  const { name = '', vertical = '' } = values;
  if (!name.trim()) {
    errors.name = 'Please provide a name';
  }

  if (!vertical.trim()) {
    errors.vertical = 'Please provide a vertical';
  }
  return errors;
};

const renderField = ({
  input, label, type, meta: { touched, error },
}) => (
  <div>
    <label>{label}</label>
    <div>
      <input {...input} placeholder={label} type={type} />
      {touched && error && <div className="error" style={{ paddingBottom: '5px' }}>{error}</div>}
    </div>
  </div>
);

const renderSelect = ({
  input, label/* , type */, url, please, dispatch, meta: { touched, error },
}) => (
  <div>
    <label>{label}</label>
    <select {...input}>
      <Request
        url={url}
        method="get"
        accept="application/json"
        verbose
      >
        {
          ({ error, result, loading }) => {
            if (error) {
              errorMessageUtil.dispatchError(error, dispatch, appActions.displayBadRequestMessage);
              return (<option value="">Error retrieving verticals...</option>);
            }
            if (loading) {
              return (<option value="">loading...</option>);
            }
            const verticals = !_.isNil(result) && !_.isNil(result.body) ? result.body.sort() : [];
            return (<optgroup>{verticals.map(vertical => <option value={vertical} key={vertical}>{vertical}</option>)}</optgroup>);
          }
        }
      </Request>
    </select>
    {touched && error && <div className="error">{error}</div>}
  </div>
);

class ProjectForm extends Component {
  constructor(props, context) {
    super(props, context);
    this.props = props;
    this.appState = this.props.app;
    this.selectStyle = { width: '200px' };
    this.submitBtnStyle = { float: 'right', width: '130px' };
    this.submitLabel = Constants.UPDATE;
    this.handleSubmit = this.updateProject.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
  }

  componentDidMount() {
    this.handleInitialize();
  }

  handleInitialize() {
    const {
      projectId, projectById, clientId,
    } = this.props;
    this.initData = projectById[projectId];
    this.props.initialize(this.initData);
  }

  updateProject(values) {
    const {
      clientId, projectId, history, dispatch,
    } = this.props;
    let data = {
      clientId,
      projectId,
      values,
    };
    dispatch(projectActions.updateByProject(data));
    dispatch(projectActions.updateProjectStatus(projectId, Constants.STATUS.RUNNING));
    dispatch(appActions.changeRoute(RouteNames.PROJECTID, { clientId, projectId }, history));
  }

  onClickCancel() {
    const {
      clientId, projectId, history, dispatch,
    } = this.props;
    dispatch(appActions.changeRoute(RouteNames.PROJECTID, { clientId, projectId }, history));
  }

  render() {
    const {
      error, handleSubmit/* , pristine, reset */, submitting, invalid,
    } = this.props;

    return (
      <div className="pageCentered">
        <div className="page">
          <Box>
            <form id="ProjectForm">
              <Field name="name" type="text" component={renderField} label={Constants.PROJECT_NAME} />
              <Field name="description" type="text" component={renderField} label={Constants.PROJECT_DESCRIPTION} />
              <Field
                name="vertical"
                type="text"
                component={renderSelect}
                label={Constants.VERTICAL_MARKET}
                url="nltools/private/v1/resources/verticals"
                style={this.selectStyle}
                dispatch={this.props.dispatch}
              />
              <Field
                name="locale"
                type="text"
                component={LocaleSelect}
                label={Constants.Locale}
                style={this.selectStyle}
              />
              <Field name="clientId" component={renderField} type="hidden" />
              {error && <strong>{error}</strong>}
              <Button
                type="flat"
                name="project-form-cancel-button"
                onClick={this.onClickCancel}
              >
                {Constants.CANCEL}
              </Button>
              <Button
                type="primary"
                name="project-form-submit-button"
                onClick={handleSubmit(this.handleSubmit)}
                disabled={invalid || submitting}
                styleOverride={this.submitBtnStyle}
              >
                {this.submitLabel}
              </Button>
            </form>
          </Box>
        </div>
      </div>
    );
  }
}

// Decorate the form component
const createProjectForm = reduxForm({
  form: 'ProjectForm', // a unique name for this form
  validate,
})(ProjectForm);

const mapStateToProps = (state, ownProps) => ({
  projectId: state.projectListSidebar.selectedProjectId,
  projectById: state.projectListSidebar.projectById,
  clientId: state.header.client.id,
  app: state.app,
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(createProjectForm));
