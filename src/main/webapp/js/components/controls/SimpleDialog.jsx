import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from '@tfs/ui-components';
import * as actionsApp from 'state/actions/actions_app';
import TaggerModal from 'components/controls/TaggerModal';
import store from 'state/configureStore';

export default class SimpleDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickOk = this.onClickOk.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
  }

  onClickCancel() {
    if (typeof this.props.onCancel === 'function') {
      this.props.onCancel();
    } else {
      store.dispatch(actionsApp.modalDialogChange(null));
    }
  }

  onClickOk() {
    if (typeof this.props.onOk === 'function') {
      this.props.onOk();
    } else {
      store.dispatch(actionsApp.modalDialogChange(null));
    }
  }


  getActions() {
    let { actions, ok, cancel } = this.props;

    if (!actions) {
      actions = [SimpleDialog.ACTION_OK, SimpleDialog.ACTION_CANCEL];
    }

    const ret = [];
    if (actions.indexOf(SimpleDialog.ACTION_CANCEL) != -1) {
      ret.push(
        <Button
          type="flat"
          name="cancel"
          key="Cancel"
          onClick={this.onClickCancel}
          data-qa="cancel"
          styleOverride={{
            ':focus': {
              outline: 'none',
            },
          }}
        >
Cancel
        </Button>,
      );
    }
    if (actions.indexOf(SimpleDialog.ACTION_OK) != -1) {
      ret.push(<Button
        name="yes"
        key="Yes"
        onClick={this.onClickOk}
        data-qa="ok"
      >
OK
      </Button>);
    }
    return ret;
  }

  render() {
    const { header, message, className } = this.props;

    return (
      <TaggerModal
        id="AddDatasetDialog"
        header={header || 'Are you sure?'}
        content={message || ''}
        actions={this.getActions()}
        className={`SimpleDialog${className ? ` ${className}` : ''}`}
      />
    );
  }
}

SimpleDialog.ACTION_OK = 'ACTION_OK';
SimpleDialog.ACTION_CANCEL = 'ACTION_CANCEL';
SimpleDialog.propTypes = {
  header: PropTypes.string,
  message: PropTypes.oneOfType([PropTypes.node, PropTypes.func, PropTypes.string]),
  ok: PropTypes.string,
  onOk: PropTypes.func,
  cancel: PropTypes.string,
  onCancel: PropTypes.func,
  actions: PropTypes.array,
  className: PropTypes.string,
};
