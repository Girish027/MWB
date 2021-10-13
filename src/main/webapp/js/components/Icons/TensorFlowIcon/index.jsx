import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

class TensorFlowIcon extends PureComponent {
  render() {
    const {
      height,
      width,
      version,
      ...otherProps
    } = this.props;

    const textContent = `USE-Large ${version}`;

    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        id="icon_tensorflow"
        width={width}
        height={height}
        version="1.1"
        viewBox="-10 -15 50 90"
        {...otherProps}
      >
        <g transform="translate(-77.943 -177)">
          <g fillOpacity="1">
            <path
              fill="#e55b2d"
              d="M360.049 687.873v18.898l32.73 18.899V706.77zm-65.463 18.898v18.899l16.365 9.447v-18.896zm49.096 9.45l-16.366 9.449v56.691l16.366 9.45v-37.795l16.367 9.449v-18.899l-16.367-9.449z"
              transform="scale(.26458)"
            />
            <path
              fill="#ed8e24"
              d="M360.049 687.873l-49.098 28.348v18.896l32.73-18.896v18.896l16.368-9.447zm49.097 9.45l-16.367 9.448v18.899l16.367-9.45zm-32.732 37.794l-16.365 9.45v18.898l16.365-9.45zm-16.365 28.348l-16.367-9.45v37.796l16.367-9.45z"
              transform="scale(.26458)"
            />
            <path
              fill="#f8bf3c"
              d="M360.049 668.977l-65.463 37.794 16.365 9.45 49.098-28.348 32.73 18.898 16.367-9.449zm0 56.693l-16.367 9.447 16.367 9.45 16.365-9.45z"
              transform="scale(.26458)"
            />
          </g>
        </g>
        <text x="1" y="43" fontSize="5.8" fill="#333333" fontWeight="bold">TensorFlow</text>
        <text x="-1" y="56" fontSize="5.8" fill="#6b6b6b" fontWeight="normal">{textContent}</text>
      </svg>
    );
  }
}

TensorFlowIcon.defaultProps = {
  width: '230px',
  height: '250px',
};

TensorFlowIcon.propTypes = {
  width: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  height: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
};

export default TensorFlowIcon;
