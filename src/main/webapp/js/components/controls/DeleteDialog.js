import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog } from '@tfs/ui-components';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { headerIcon } from '../../styles';

export default class DeleteDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickCancel = this.onClickCancel.bind(this);
    this.styleOverride = {
      content: {
        top: '20%',
      },
      ...headerIcon,
    };
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange(null));
  }

  render() {
    const {
      onOk, header, message, ok, size,
    } = this.props;

    return (
      <div>
        <Dialog
          isOpen
          onClickOk={onOk}
          okVisible
          okChildren={ok}
          closeIconVisible
          onClickClose={this.onClickCancel}
          onClickCancel={this.onClickCancel}
          cancelChildren={Constants.CANCEL}
          styleOverride={this.styleOverride}
          headerChildren={header}
          centerContent={false}
          size={size}
        >
          {message}
        </Dialog>
      </div>
    );
  }
}

DeleteDialog.defaultProps = {
  message: '',
  dispatch: () => {},
  ok: Constants.DELETE,
  size: 'small',
};

DeleteDialog.propTypes = {
  dispatch: PropTypes.func,
  onOk: PropTypes.func.isRequired,
  header: PropTypes.node.isRequired,
  message: PropTypes.node,
  ok: PropTypes.string,
  size: PropTypes.string,
};
