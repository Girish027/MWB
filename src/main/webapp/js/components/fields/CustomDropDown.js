import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Request from 'react-http-request';
import _ from 'lodash';
import getUrl, { pathKey } from 'utils/apiUrls';

import {
  DropDown,
} from '@tfs/ui-components';
import Constants from '../../constants/Constants';
import { customDropDown } from '../../styles/customComponentsStyles';
import ObjectUtils from '../../utils/ObjectUtils';
import ValidationPlaceholder from './ValidationPlaceholder';

export default class CustomDropDown extends Component {
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
    this.processResults = this.processResults.bind(this);
    this.getDropDownStyle = this.getDropDownStyle.bind(this);
  }

  getDropDownStyle() {
    const { showError } = this.state;

    let selectStyle = { ...customDropDown.select };
    if (showError) {
      selectStyle = { ...selectStyle, border: '1px solid #FF0000' };
    }
    return selectStyle;
  }

  processResults(result) {
    const dropDownList = !_.isNil(result) && !_.isNil(result.body) ? result.body.sort() : [];

    return (
      <select
        name={this.props.name}
        onChange={this.handleChange}
        onFocus={this.handleChange}
        value={this.state.value}
        style={this.getDropDownStyle()}
      >
        {this.props.defaultValue && <option value={this.props.defaultValue}>{this.props.defaultValue}</option> }
        {dropDownList.map((item, index) => (
          <option key={item} value={item}>
            {item}
          </option>
        ))}
      </select>
    );
  }

  getLabel() {
    return (
      <div
        style={customDropDown.label}
      >
        {this.schema.title}
      </div>
    );
  }

  getField() {
    const { validationMessage } = this.state;
    const { url } = this.props;
    const fetchUrl = getUrl(url);

    let validationMsg = '';
    if (validationMessage) {
      validationMsg = validationMessage.slice(0, validationMessage.indexOf('('));
    }

    return (
      <div style={{ paddingBottom: '5px' }}>
        <Request
          url={fetchUrl}
          method="get"
          accept="application/json"
          verbose
        >
          {
            ({ result, loading }) => {
              if (loading) {
                return (
                  <select
                    style={this.getDropDownStyle()}
                    onFocus={this.handleChange}
                  >
                    {this.props.defaultValue
                      ? <option value={this.props.defaultValue}>{this.props.defaultValue}</option>
                      : <option>loading..</option> }
                  </select>
                );
              }
              return this.processResults(result);
            }
          }
        </Request>
        {validationMessage && (
          <ValidationPlaceholder validationMessage={validationMsg} />
        )}
      </div>
    );
  }

  handleChange(event) {
    const { value } = event.target;
    const { schema, name } = this.props;

    if ((value === '' || value === Constants.CHOOSE_TYPE) && schema.hasOwnProperty('isRequired')) {
      const name = schema.title.toLowerCase();
      const validationMessage = Constants.VALIDATION_MSG(name);
      this.setState({
        value,
        validationMessage,
        showError: true,
      });
      this.props.onChange();
    } else if ((value !== '' && value !== Constants.CHOOSE_TYPE)) {
      this.setState({
        value,
        validationMessage: '',
        showError: false,
      });
      this.props.onChange(value);
    }
  }

  render() {
    return (
      <div
        style={customDropDown}
      >
        {this.getLabel()}
        {this.getField()}
      </div>
    );
  }
}

CustomDropDown.propTypes = {
  name: PropTypes.string,
  schema: PropTypes.object.isRequired,
  formData: PropTypes.string,
  onChange: PropTypes.func,
};
