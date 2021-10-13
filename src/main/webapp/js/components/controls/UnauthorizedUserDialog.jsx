import React, { Component } from 'react';
import PropTypes from 'prop-types';
import * as actionsApp from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { Dialog } from '@tfs/ui-components';

export default class UnauthorizedUserDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickCancel = this.onClickCancel.bind(this);
  }

  onClickCancel() {
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange(null));
  }

  render() {
    const { header, message } = this.props;
    const styleOverride = {
      content: {
        top: '315px',
        maxWidth: '500px',
        maxHeight: '271px',
        left: 'calc((100vw - 515px) / 2)',
        boxShadow: 'rgba(0, 0, 0, 0.2) 1px 3px 3px 0px, rgba(0, 0, 0, 0.2) 1px 3px 15px 2px',
      },
    };
    return (
      <div>
        <Dialog
          isOpen
          closeIconVisible={false}
          okVisible={false}
          onClickCancel={this.onClickCancel}
          cancelChildren={Constants.CLOSE}
          headerChildren={header}
          styleOverride={styleOverride}
        >
          <p>
            {' '}
            {message}
            {' '}
          </p>
        </Dialog>
      </div>
    );
  }
}

UnauthorizedUserDialog.propTypes = {
  message: PropTypes.string,
  header: PropTypes.string,
};

UnauthorizedUserDialog.defaultProps = {
  message: 'User is not authorized to access any workspace. Please contact your admin.',
  header: 'Unauthorized User',
};
