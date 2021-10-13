import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import ReactForm from 'react-jsonschema-form';
import FormPageTitle from '../FormTitle/PageTitle';
import FormSectionTitle from '../FormTitle/SectionTitle';
import CustomComponents from '../fields/CustomComponents';

export default class Form extends PureComponent {
  constructor(props) {
    super(props);
    this.submitButton = React.createRef();
    this.props = props;

    this.state = {
      componentMounted: false,
      fields: CustomComponents,
    };

    this.onSubmit = this.onSubmit.bind(this);
    this.submit = this.submit.bind(this);
    this.onChange = this.onChange.bind(this);
    this.objectFieldTemplate = this.objectFieldTemplate.bind(this);
  }

  componentDidMount() {
    this.setState({
      componentMounted: true,
    });
  }

  onSubmit({ formData }) {
    this.props.onSubmit(formData);
  }

  onChange({ formData, errors }) {
    if (this.state.componentMounted) {
      this.props.onChange(formData, errors);
    }
  }

    transformErrors = (errors) => errors.map((error) => {
      const _error = error;
      delete _error.message;
      return _error;
    });

    objectFieldTemplate(props) {
      const {
        idSchema, title, description, properties, formContext,
      } = props;
      let retVal;
      if (idSchema.$id === 'root') {
        retVal = <FormPageTitle title={title} description={description} properties={properties} formContext={formContext} />;
      } else {
        retVal = <FormSectionTitle title={title} properties={properties} formContext={formContext} />;
      }
      return retVal;
    }

    // This is basically to click the submit button in react-jsonschema-form which is in disabled state
    submit() {
      this.submitButton.current.click();
    }

    render() {
      const {
        uiSchema, jsonSchema, liveValidate, formContext, showErrorList, formData,
      } = this.props;
      return (
        <ReactForm
          schema={jsonSchema}
          uiSchema={uiSchema}
          className="form-group"
          onChange={this.onChange}
          onSubmit={this.onSubmit}
          fields={this.props.fields}
          ObjectFieldTemplate={this.objectFieldTemplate}
          formContext={formContext}
          formData={formData}
          showErrorList={showErrorList}
          liveValidate={liveValidate}
          transformErrors={this.transformErrors}
        >
          {/* As the React json schema form have submit button in default, the below code is to disable that submit button. */}
          <div>
            <button
              type="submit"
              ref={this.submitButton}
              style={{ display: 'none' }}
            >
                        Submit
            </button>
          </div>
        </ReactForm>
      );
    }
}

Form.propTypes = {
  formData: PropTypes.object,
  formContext: PropTypes.object,
  showErrorList: PropTypes.bool,
  onSubmit: PropTypes.func,
  uiSchema: PropTypes.object.isRequired,
  jsonSchema: PropTypes.object.isRequired,
  onChange: PropTypes.func,
  liveValidate: PropTypes.bool,
};

Form.defaultProps = {
  onChange: () => {},
  formData: {},
  formContext: {},
  onSubmit: () => {},
  showErrorList: false,
  liveValidate: false,
};
