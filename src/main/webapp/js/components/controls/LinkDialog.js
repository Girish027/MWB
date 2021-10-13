import React, { Component } from 'react';
import { Dialog, TextField } from '@tfs/ui-components';
import PropTypes from 'prop-types';
import * as appActions from 'state/actions/actions_app';
import _ from 'lodash';
import Constants from 'constants/Constants';
import { headerIcon } from 'styles';

export default class LinkDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);

    this.state = {
      linkTextField: '',
    };

    this.styleOverride = {
      childContainer: {
        marginTop: '10px',
        marginBottom: '10px',
      },
      cancel: {
        backgroundColor: 'unset',
        paddingLeft: '10px',
        paddingRight: '10px',
      },
      ok: {
        marginLeft: '10px',
        paddingLeft: '25px',
        paddingRight: '25px',
      },
      ...headerIcon,
    };
  }

  onChange(event) {
    event.preventDefault();
    const { value } = event.target;
    this.setState({
      linkTextField: value,
    });
  }

  onKeyPress(event) {
    // TODO : write KeyboardUtils APIs
    if (event.key === 'Enter'
        || event.key === 'Tab') {
      event.preventDefault();
      this.onClickOk();
    }
  }

  onClickCancel() {
    const { dispatch, onClickCancel } = this.props;
    onClickCancel();
    dispatch(appActions.modalDialogChange(null));
  }

  onClickOk() {
    const { dispatch, onClickRunTest } = this.props;
    const { linkTextField } = this.state;
    dispatch(appActions.modalDialogChange({
      dispatch,
      type: Constants.DIALOGS.PROGRESS_DIALOG,
      message: Constants.TEST_IN_PROGRESS,
    }));
    onClickRunTest(linkTextField);
  }

  render() {
    const { header } = this.props;
    const { linkTextField } = this.state;

    return (
      <div>
        <Dialog
          size="small"
          headerChildren={header}
          onClickCancel={this.onClickCancel}
          onClickOk={this.onClickOk}
          onClickClose={this.onClickCancel}
          isOpen
          okDisabled={_.isNil(linkTextField) || linkTextField.length === 0}
          okChildren={Constants.RUN_TEST}
          styleOverride={this.styleOverride}
        >
          <div style={{
            textAlign: 'left',
            paddingTop: '40px',
          }}
          >
            <TextField
              type="text"
              name="link"
              placeholder="Enter URL"
              onChange={this.onChange}
              onKeyDown={this.onKeyPress}
              style={{ width: '435px' }}
            />
          </div>
        </Dialog>
      </div>
    );
  }
}

LinkDialog.propTypes = {
  header: PropTypes.string.isRequired,
  onClickCancel: PropTypes.func,
  onClickRunTest: PropTypes.func.isRequired,
  dispatch: PropTypes.func,
};

LinkDialog.defaultProps = {
  onClickCancel: () => {},
};
