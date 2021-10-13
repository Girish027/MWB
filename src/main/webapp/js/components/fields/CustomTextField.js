import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  TextField,
} from '@tfs/ui-components';
import Constants from 'constants/Constants';
import validationUtil from 'utils/ValidationUtil';
import ObjectUtils from '../../utils/ObjectUtils';
import { customTextField } from '../../styles/customComponentsStyles';
import ValidationPlaceholder from './ValidationPlaceholder';

export default class CustomTextField extends Component {
  constructor(props) {
    super(props);
    this.schema = props.schema;
    const value = ObjectUtils.isEmptyOrNull(props.formData)
      ? this.schema.default
      : props.formData;
    this.state = {
      value,
      validationMessage: '',
      showError: false,
    };

    this.handleChange = this.handleChange.bind(this);
  }

  getLabel() {
    return (
      <div style={{ paddingBottom: '5px' }}>
        <span
          style={customTextField.label}
        >
          {this.schema.title}
        </span>
        { this.schema.optional
          && <span style={customTextField.label.optionalField}> Optional</span>
        }
      </div>
    );
  }

  getField() {
    const { validationMessage, showError } = this.state;
    let validationMsg = '';

    if (validationMessage) {
      validationMsg = validationMessage.slice(0, validationMessage.indexOf('('));
    }

    let textStyle = { ...customTextField.field };
    if (showError) {
      textStyle = { ...textStyle, input: { border: '1px solid #FF0000' } };
    }

    return (
      <div style={{ paddingBottom: '5px' }}>
        <TextField
          name={this.props.name}
          onChange={this.handleChange}
          onFocus={this.handleChange}
          defaultValue={this.state.value}
          placeholder={this.schema.placeholder}
          styleOverride={textStyle}
          minLength={this.schema.minLength}
          maxLength={this.schema.maxLength}
        />
        {validationMessage && (
          <ValidationPlaceholder validationMessage={validationMsg} />
        )}
      </div>
    );
  }

  handleChange(event) {
    const { value, validationMessage } = event.target;
    const { schema, name, onChange } = this.props;

    if (!(validationUtil.checkField(value)) && value.length <= Constants.DATASET_NAME_SIZE_LIMIT && value) {
      const validationMessage = Constants.INVALID_ENTERED_NAME;
      this.setState({ value, validationMessage, showError: true });
      onChange();
    } else if (value.length >= Constants.DATASET_NAME_SIZE_LIMIT) {
      const validationMessage = Constants.VALIDATION_NAME_SIZE_MSG;
      this.setState({ value, validationMessage, showError: true });
      onChange();
    } else if (value || validationMessage) {
      this.setState({ value, validationMessage, showError: false });
      onChange(value);
    } else if (schema.hasOwnProperty('isRequired') && !value) {
      const validationMessage = Constants.VALIDATION_MSG(name);
      this.setState({ value, validationMessage, showError: true });
      onChange();
    }
  }

  render() {
    return (
      <div
        style={customTextField}
      >
        {this.getLabel()}
        {this.getField()}
      </div>
    );
  }
}

CustomTextField.propTypes = {
  name: PropTypes.string,
  schema: PropTypes.object.isRequired,
  formData: PropTypes.string,
  onChange: PropTypes.func,
};

CustomTextField.defaultProps = {
  onChange: () => {},
  name: 'textfield',
};
