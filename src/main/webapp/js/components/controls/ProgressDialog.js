import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Dialog, DashedSpinner } from '@tfs/ui-components';
import * as actionsApp from 'state/actions/actions_app';
import * as preferenceActions from 'state/actions/actions_preferences';
import { progressDialogLabel } from '../../styles';

export default class ProgressDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickClose = this.onClickClose.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
  }

  componentDidMount() {
    const {
      clientId, projectId, dispatch,
    } = this.props;
    if (clientId && projectId) {
      dispatch(preferenceActions.getTechnologyByClientModel({ clientId, projectId }));
    }
  }

  onClickClose() {
    const { dispatch } = this.props;
    dispatch(actionsApp.modalDialogChange(null));
  }

  onClickOk() {
    const { history, onOk } = this.props;
    onOk(history);
  }

  render() {
    const {
      message, closeIconVisible, header, showFooter, showHeader,
      okVisible, cancelVisible, okChildren, cancelChildren,
      styleOverride, showSpinner,
    } = this.props;

    return (
      <div>
        <Dialog
          size="small"
          headerChildren={header}
          isOpen
          okVisible={okVisible}
          cancelVisible={cancelVisible}
          closeIconVisible={closeIconVisible}
          onClickClose={this.onClickClose}
          styleOverride={styleOverride}
          okChildren={okChildren}
          cancelChildren={cancelChildren}
          onClickOk={this.onClickOk}
          onClickCancel={this.onClickClose}
          showHeader={showHeader}
          showFooter={showFooter}
        >
          <div>
            <p style={progressDialogLabel}>
              {message}
            </p>
            {showSpinner && <DashedSpinner height="50" width="50" fill="#004C97" />}
          </div>
        </Dialog>
      </div>
    );
  }
}

ProgressDialog.propTypes = {
  message: PropTypes.string,
  closeIconVisible: PropTypes.bool,
  showHeader: PropTypes.bool,
  showFooter: PropTypes.bool,
  okVisible: PropTypes.bool,
  cancelVisible: PropTypes.bool,
  showSpinner: PropTypes.bool,
  header: PropTypes.string,
  okChildren: PropTypes.string,
  cancelChildren: PropTypes.string,
  styleOverride: PropTypes.object,
  onOK: PropTypes.func,
};

ProgressDialog.defaultProps = {
  message: '',
  closeIconVisible: false,
  showHeader: false,
  showFooter: false,
  okVisible: false,
  cancelVisible: false,
  showSpinner: true,
  string: '',
  okChildren: '',
  cancelChildren: '',
  styleOverride: {
    childContainer: {
      margin: 'auto',
    },
  },
  onOk: () => {},
};
