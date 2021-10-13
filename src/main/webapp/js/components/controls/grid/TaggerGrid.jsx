import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Grid } from 'react-redux-grid';

export default class TaggerGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  render() {
    const {
      id, className, config, style,
    } = this.props;

    return (
      <div
        id={id}
        className={`TaggerGrid${className ? ` ${className}` : ''}`}
        style={style}
      >
        <Grid {...config} data={this.props.data} />
      </div>
    );
  }
}

TaggerGrid.propTypes = {
  config: PropTypes.object.isRequired,
  id: PropTypes.string,
  className: PropTypes.string,
  data: PropTypes.array,
  style: PropTypes.object,
};

TaggerGrid.defaultProps = {

};
