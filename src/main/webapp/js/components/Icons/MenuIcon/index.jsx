import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

class MenuIcon extends PureComponent {
  render() {
    const {
      width, height, color, ...otherProps
    } = this.props;

    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width={width}
        height={height}
        viewBox="0 0 100 100"
      >
        <circle
          cx="11"
          cy="12"
          r="10"
          fill="none"
          stroke={color}
          strokeWidth="2"
        />
        <circle
          cx="11"
          cy="8.9"
          r="2"
          fill={color}
          transform="rotate(-9.3 56.946 14.608)"
        />
        <circle
          cx="11.46"
          cy="8.502"
          r="2"
          fill={color}
          transform="rotate(-6.5 33.473 18.847)"
        />
        <circle
          cx="10.202"
          cy="6.202"
          r="2"
          fill={color}
        />
      </svg>
    );
  }
}

MenuIcon.defaultProps = {
  width: 100,
  height: 100,
  color: '#989FA9',
};

MenuIcon.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  color: PropTypes.string,
};

export default MenuIcon;
