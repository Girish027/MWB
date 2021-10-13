import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import { Checkbox } from '@tfs/ui-components';
import Request from 'react-http-request';
import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';
import Constants from 'constants/Constants';
import validationUtil from 'utils/ValidationUtil';

// TODO: redux-form is not recommended. To be changed to use react-json-schema.
// Remove dependency of redux-form and react-http-request.

export const validate = (values, props) => {
  const errors = {};
  const { names } = featureFlagDefinitions;

  const { name } = values;

  if (!name) {
    errors.name = Constants.VALIDATION_NO_DATA_MSG;
  } else if (!(validationUtil.checkField(name)) && name.length <= Constants.DATASET_NAME_SIZE_LIMIT && name !== '') {
    errors.name = Constants.INVALID_ENTERED_NAME;
  } else if (name.length >= Constants.DATASET_NAME_SIZE_LIMIT) {
    errors.name = Constants.VALIDATION_NAME_SIZE_MSG;
  }
  if (isFeatureEnabled(names.datasetType, props.userFeatureConfiguration)) {
    if (!values.dataType || values.dataType === Constants.TYPE_MSG) {
      errors.dataType = Constants.VALIDATION_MSG('type');
    }
  }
  return errors;
};

const renderField = ({
  input, label, type, meta: { touched, error },
}) => {
  if (type != 'hidden') {
    return (
      <div className="Field">
        <label>{label}</label>
        <div>
          <input {...input} placeholder={label} type={type} />
          {touched && error && <div className="error">{error}</div>}
        </div>
      </div>
    );
  }
  return (
    <input {...input} placeholder={label} type={type} />
  );
};

const renderSelect = ({
  input, label, /* type, */ please, meta: { touched, error },
}) => (
  <div className="Field">
    <label>{label}</label>
    <select {...input}>
      <option value={please}>{please}</option>
      <Request
        url="nltools/private/v1/resources/datatypes"
        method="get"
        accept="application/json"
        verbose
      >
        {
          ({/* error, */ result, loading }) => {
            if (loading) {
              return (<option value="">loading...</option>);
            }
            const dataTypes = result.body.sort();
            return (<optgroup>{dataTypes.map(dataType => <option value={dataType} key={dataType}>{dataType}</option>)}</optgroup>);
          }
        }
      </Request>
    </select>
    {touched && error && <div className="error">{error}</div>}
  </div>
);

class CreateDatasetForm extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  componentDidMount() {
  }

  render() {
    const {
      handleSubmit, userFeatureConfiguration/* , pristine, reset, submitting, invalid */, error,
    } = this.props;
    const state = this.props.state || {};
    // let { autoTag, startTransform } = (state && state.values) ? state.values : {};
    const { suggestIntent } = (state && state.values) ? state.values : {};

    const { names } = featureFlagDefinitions;

    return (
      <form
        id="CreateDatasetForm"
        className="TaggerForm"
        onSubmit={handleSubmit(() => { /* do nothing */ })}
      >
        <Field name="name" component={renderField} type="text" label="Dataset Name*" />
        <Field name="description" component={renderField} type="text" label="Dataset Description" />
        {isFeatureEnabled(names.datasetType, userFeatureConfiguration)
        && (
          <Field
            name="dataType"
            label="Dataset Type*"
            component={renderSelect}
            please="Please select a type"
          />
        )
        }
        <div className="Field TransformationOptions">
          {/* <label>Transformation options</label> */}
          {isFeatureEnabled(names.suggestedIntent, userFeatureConfiguration)
          && (
            <div>
              <Checkbox
                checked={suggestIntent}
                value="Suggest-Intent"
                label="Suggest Intent"
                onChange={() => {
                  this.props.change('suggestIntent', !suggestIntent);
                }}
              />
              {this.props.suggestIntentDisabled
                ? <span>(No models available)</span> : ''}
            </div>
          )
          }
        </div>
        <div className="Field">
          {
            // this is the generic error, passed through as { _error: "something wrong" }
            error && <div className="error">{ error }</div>
          }
        </div>
      </form>
    );
  }
}

// Decorate the form component
const createDatasetForm = reduxForm({
  form: 'CreateDataset', // a unique name for this form
  validate,
})(CreateDatasetForm);

const mapStateToProps = state => ({
  state: state.form.CreateDataset,
  userFeatureConfiguration: state.app.userFeatureConfiguration,
  initialValues: {
    autoTag: true,
    startTransform: true,
    suggestIntent: false,
  },
});

const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(createDatasetForm);
