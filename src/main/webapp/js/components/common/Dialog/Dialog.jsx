import React, { Component } from 'react';
import PropTypes from 'prop-types';
// eslint-disable-next-line import/no-named-default
import ReactSVG from 'react-svg';
import {
  Modal, ModalHeader, ModalBody, ModalFooter,
} from 'reactstrap';
import { Button } from '@tfs/ui-components';
// TODO: remove all instances of this component. Use TFSUI Dialog.

export default class Dialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickOk = this.onClickOk.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.getIcon = this.getIcon.bind(this);
  }

  onClickCancel() {
    this.props.onCancel();
  }

  onClickOk() {
    this.props.onOk();
  }

  getIcon(icon) {
    return (
      <ReactSVG
        src={icon}
      />
    );
  }

  render() {
    const {
      icon, title, children, okString, visible,
      onOk, cancelString, onCancel, okDisabled, size,
    } = this.props;

    const modalSize = (typeof (size) === 'undefined') ? null : size;

    return (
      <Modal isOpen={visible} toggle={this.toggle} size={modalSize}>
        <ModalHeader toggle={this.toggle}>
          {
            icon
              ? <span className="dialog-title-icon" data-qa="dialog-title">{this.getIcon(icon)}</span>
              : ''
          }
          <span className="dialog-title-text">{title}</span>
        </ModalHeader>
        <ModalBody>
          {children}
        </ModalBody>
        <ModalFooter>
          <div className="dialog-cancel-container">
            {this.props.showCancel
              && (
                <Button
                  name="cancel-dialog"
                  type="flat"
                  onClick={onCancel}
                  styleOverride={{
                    ':focus': {
                      outline: 'none',
                    },
                  }}
                >
                  {cancelString || 'CANCEL'}
                </Button>
              )
            }
          </div>
          <div className="dialog-ok-container">
            {this.props.showOk
              && (
                <Button
                  name="ok-dialog"
                  onClick={onOk}
                  disabled={okDisabled}
                >
                  {okString.toUpperCase()}
                </Button>
              )
            }
          </div>
        </ModalFooter>
      </Modal>
    );
  }
}

Dialog.defaultProps = {
  showCancel: true,
  showOk: true,
  okString: 'OK',
};

Dialog.propTypes = {
  icon: PropTypes.string,
  visible: PropTypes.bool,
  title: PropTypes.string,
  children: PropTypes.element.isRequired,
  okString: PropTypes.string,
  cancelString: PropTypes.string,
  onOk: PropTypes.func,
  onCancel: PropTypes.func,
  className: PropTypes.string,
  showCancel: PropTypes.bool,
  showOk: PropTypes.bool,
  okDisabled: PropTypes.bool,
  customClassName: PropTypes.string,
};
