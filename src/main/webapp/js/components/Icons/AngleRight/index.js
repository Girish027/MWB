import React from 'react';
import PropTypes from 'prop-types';
import { FaAngleRight } from 'react-icons/fa';
import { IconContext } from 'react-icons';

function AngleRight(props) {
  const { fill, width, height } = props;
  return (
    <IconContext.Provider
      value={{
        color: fill,
        width,
        height,
      }}
    >
      <div>
        <FaAngleRight />
      </div>
    </IconContext.Provider>
  );
}

AngleRight.propTypes = {
  fill: PropTypes.string,
  width: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  height: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
};

AngleRight.defaultProps = {
  fill: '#313f54',
  width: '2.5em',
  height: '2.5em',
};

export default AngleRight;
