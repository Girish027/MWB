import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

class NetworkAdd extends PureComponent {
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
          <path d="M8.625 7.125v3M7.125 8.625h3" stroke={innerStrokeColor} />
          <path
            d="M5.254 11.576a5.626 5.626 0 1 1 6.322-6.326M4.644 11.46C3.885 10.345 3.375 8.316 3.375 6c0-2.317.509-4.344 1.27-5.46M.388 5.625h4.846M1.5 2.625h9M1.024 8.625h2.583M7.356.54a8.299 8.299 0 0 1 1.2 3.59"
            stroke={outerStrokeColor}
          />
        </g>
      </svg>
    );
  }
}

NetworkAdd.defaultProps = {
  width: 16,
  height: 16,
  fill: 'none',
  innerStrokeColor: '#278903',
  outerStrokeColor: '#313F54',
};

NetworkAdd.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  fill: PropTypes.string,
  innerStrokeColor: PropTypes.string,
  outerStrokeColor: PropTypes.string,
};

export default NetworkAdd;
