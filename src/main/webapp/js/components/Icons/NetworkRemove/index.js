import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

class NetworkRemove extends PureComponent {
  render() {
    let {
      fill, innerStrokeColor, outerStrokeColor, ...otherProps
    } = this.props;

    if (fill !== 'none') {
      innerStrokeColor = fill;
      outerStrokeColor = fill;
    }

    return (
      <svg viewBox="0 0 16 16" {...otherProps}>
        <g
          transform="translate(2 2)"
          fill="none"
          fillRule="evenodd"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <circle stroke={innerStrokeColor} cx="8.625" cy="8.625" r="3" />
          <path d="M9.75 7.5L7.5 9.75M7.5 7.5l2.25 2.25" stroke={innerStrokeColor} />
          <path
            d="M5.245 11.575a5.626 5.626 0 1 1 6.33-6.325M4.644 11.46C3.885 10.345 3.375 8.316 3.375 6c0-2.317.509-4.344 1.27-5.46M.388 5.625H5.25M1.5 2.625h9M1.024 8.625h2.583M7.356.54a8.505 8.505 0 0 1 1.153 3.579"
            stroke={outerStrokeColor}
          />
        </g>
      </svg>
    );
  }
}

NetworkRemove.defaultProps = {
  width: 16,
  height: 16,
  fill: 'none',
  innerStrokeColor: '#D0021B',
  outerStrokeColor: '#313F54',
};

NetworkRemove.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  fill: PropTypes.string,
  innerStrokeColor: PropTypes.string,
  outerStrokeColor: PropTypes.string,
};

export default NetworkRemove;
