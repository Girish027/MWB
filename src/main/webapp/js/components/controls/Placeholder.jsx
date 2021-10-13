import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

class Placeholder extends PureComponent {
  render() {
    const {
      message, styleOverride, children, ...otherProps
    } = this.props;
    if (children) {
      return (
        <div
          className="vertical-center horizontal-center"
          style={{
            paddingBottom: '20px',
            ...styleOverride,
          }}
          {...otherProps}
        >
          {children}
        </div>
      );
    }
    return (
      <div
        className="vertical-center horizontal-center message-default"
        style={styleOverride}
        {...otherProps}
      >
        {message}
      </div>
    );
  }
}

Placeholder.defaultProps = {
  styleOverride: {},
  message: '',
};

Placeholder.propTypes = {
  children: PropTypes.node,
  styleOverride: PropTypes.object,
  message: PropTypes.string,
};

export default Placeholder;
