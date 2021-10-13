import React, { Component } from 'react';
import PropTypes from 'prop-types';
import * as actionsApp from 'state/actions/actions_app';
import { connect } from 'react-redux';

class TaggerContextMenuContainer extends Component {
  static getPageXY(event) {
    // If pageX/Y aren"t available and clientX/Y are,
    // calculate pageX/Y - logic taken from jQuery.
    // (This is to support old IE)
    let doc,
      eventDoc,
      body,
      pageX = event.pageX || 0,
      pageY = event.pageY || 0;
    eventDoc = (event.target && event.target.ownerDocument) || document;
    doc = eventDoc.documentElement;
    body = eventDoc.body;
    if (event.pageX == null && event.clientX != null) {
      pageX = event.clientX
                /* + (doc && doc.scrollLeft || body && body.scrollLeft || 0) */
                - (doc && doc.clientLeft || body && body.clientLeft || 0);
      pageY = event.clientY
                /* + (doc && doc.scrollTop  || body && body.scrollTop  || 0) */
                - (doc && doc.clientTop || body && body.clientTop || 0);
    } else {
      pageX -= (doc && doc.scrollLeft || body && body.scrollLeft || 0);
      pageY -= (doc && doc.scrollTop || body && body.scrollTop || 0);
    }

    return {
      pageX, pageY, doc, body,
    };
  }

  constructor(props) {
    super(props);
    this.props = props;
    this.documentClickHandler = this.documentClickHandler.bind(this);
  }

  componentDidMount() {
    document.addEventListener('click', this.documentClickHandler);
    // document.addEventListener("contextmenu", this.documentClickHandler);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.documentClickHandler);
    // document.removeEventListener("contextmenu", this.documentClickHandler);
  }

  documentClickHandler() {
    this.props.contextMenuChange(false);
  }

  render() {
    const { top, left } = this.props;
    const style = {
      top: `${(top || 0) + 10}px`,
      left: `${(left || 0) + 10}px`,
      position: 'fixed',
      zIndex: 55555,
    };

    return (
      <div
        className="TaggerContextMenu"
        style={style}
      >
        {this.props.children}
      </div>
    );
  }
}

TaggerContextMenuContainer.propTypes = {
  top: PropTypes.number,
  left: PropTypes.number,
};

TaggerContextMenuContainer.defaultProps = {
  top: 0,
  left: 0,
};


const mapStateToProps = (/* state */) => ({

});

const mapDispatchToProps = dispatch => ({
  contextMenuChange: (contextMenuState) => {
    dispatch(actionsApp.contextMenuChange(contextMenuState));
  },
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(TaggerContextMenuContainer);
