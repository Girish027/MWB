import React, { Component } from 'react';
import PropTypes from 'prop-types';

import {
  Textarea,
} from '@tfs/ui-components';
import { customTextarea } from 'styles/customComponentsStyles';
import ObjectUtils from 'utils/ObjectUtils';
import ValidationPlaceholder from './ValidationPlaceholder';

export default class CustomTextArea extends Component {
  constructor(props) {
    super(props);
    this.schema = props.schema;
    const value = ObjectUtils.isEmptyOrNull(props.formData)
      ? this.schema.default
      : props.formData;
    this.state = {
      value,
      validationMessage: '',
    };
    this.handleChange = this.handleChange.bind(this);
  }

  getLabel() {
    return (
      <div
        style={customTextarea.label}
      >
        {this.schema.title}
      </div>
    );
  }

  getField() {
    const { validationMessage } = this.state;
    let validationMsg = '';

    if (validationMessage) {
      validationMsg = validationMessage.slice(0, validationMessage.indexOf('('));
    }

    return (
      <React.Fragment>
        <Textarea
          value={this.state.value}
          name={this.props.name}
          onChange={this.handleChange}
          placeholder={this.schema.placeholder}
          styleOverride={customTextarea.field}
          minLength={this.schema.minLength}
          maxLength={this.schema.maxLength}
        />
        {validationMessage && (
          <ValidationPlaceholder validationMessage={validationMsg} />
        )}

      </React.Fragment>
    );
  }

  handleChange(event) {
    const { value, validationMessage } = event.target;
    this.setState({ value, validationMessage });
    this.props.onChange(value);
  }

  render() {
    return (
      <div
        style={customTextarea}
      >
        {this.getLabel()}
        {this.getField()}
      </div>
    );
  }
}

CustomTextArea.propTypes = {
  name: PropTypes.string,
  schema: PropTypes.object.isRequired,
  formData: PropTypes.string,
  // formContext: PropTypes.object, //
  onChange: PropTypes.func.isRequired,
};
