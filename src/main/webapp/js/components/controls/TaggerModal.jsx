import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';
import Layer from 'grommet/components/Layer';


export default class TaggerModal extends Component {
  constructor(props) {
    super(props);
    this.props = props;
  }

  render() {
    const {
      id, resize, header, content, actions, className,
    } = this.props;
    return (
      <Layer
        align="center"
        closer={false}
        id="TaggerModal"
        className={
          `TaggerControl TaggerModal${
            resize ? ' TaggerModalResize' : ''
          }${className ? ` ${className}` : ''}`
        }
      >
        <Box direction="column" id={id} flex>
          <Box className="ModalRow ModalHead">
            <span className="ModalTitle">
              {typeof header === 'function' ? header() : header}
            </span>
            {/* <span className="ModalClose"> X </span> */}
          </Box>
          <Box className="ModalRow ModalContent" flex>
            {typeof content === 'function' ? content() : content}
          </Box>
          <Box className="ModalRow ModalActions" direction="row" justify="end">
            {typeof actions === 'function' ? actions() : actions}
          </Box>
        </Box>
      </Layer>
    );
  }
}

TaggerModal.propTypes = {
  id: PropTypes.string,
  resize: PropTypes.bool,
  header: PropTypes.oneOfType([PropTypes.node, PropTypes.func]),
  content: PropTypes.oneOfType([PropTypes.node, PropTypes.func]),
  actions: PropTypes.oneOfType([PropTypes.node, PropTypes.func]),
  className: PropTypes.string,
};
