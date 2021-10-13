import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

import { AlertIcon } from '@tfs/ui-components';

export default class ErrorLayout extends PureComponent {
  render() {
    const { errorMsg, styleOverride } = this.props;
    const modalErrorLayout = {
      border: '1px solid #C81919',
      padding: '10px',
      fontSize: '15px',
      color: '#C81919',
      borderRadius: '3px',
    };
    const layoutStyle = Object.assign({}, modalErrorLayout, styleOverride);

    return (
      <div
        style={layoutStyle}
      >
        <AlertIcon />
        {' '}
        {errorMsg}
      </div>
    );
  }
}

ErrorLayout.propTypes = {
  errorMsg: PropTypes.string.isRequired,
  styleOverride: PropTypes.object,
};

ErrorLayout.defaultProps = {
  styleOverride: {},
};
