import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

import { AlertIcon } from '@tfs/ui-components';
import { validationErrorStyle } from '../../styles/customComponentsStyles';

export default class ValidationPlaceholder extends PureComponent {
  render() {
    const { validationMessage, styleOverride } = this.props;

    let validationStyle = validationErrorStyle;
    if (styleOverride) {
      validationStyle = { ...validationErrorStyle, ...styleOverride };
    }
    return (
      <div style={validationStyle}>
        <AlertIcon style={validationErrorStyle.icon} />
        <span
          style={validationErrorStyle.field}
        >
          {' '}
          {validationMessage}
          {' '}
        </span>
      </div>
    );
  }
}

ValidationPlaceholder.propTypes = {
  validationMessage: PropTypes.string,
};

ValidationPlaceholder.defaultProps = {
  validationMessage: '',
};
