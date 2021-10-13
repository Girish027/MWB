import React, { Component } from 'react';
import { Dialog, Radio, RadioGroup } from '@tfs/ui-components';
import PropTypes from 'prop-types';
import * as appActions from 'state/actions/actions_app';
import _ from 'lodash';
import Constants from 'constants/Constants';
import { headerIcon } from 'styles';

export default class PromoteDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onClickOk = this.onClickOk.bind(this);
    this.onChange = this.onChange.bind(this);

    this.state = {
      selectedValue: '',
    };

    this.styleOverride = {
      content: {
        top: '20%',
      },
      ok: {
        marginLeft: '10px',
        paddingLeft: '25px',
        paddingRight: '25px',
      },
      ...headerIcon,
    };
  }

  onChange(value) {
    this.setState({
      selectedValue: value,
    });
  }

  onClickCancel() {
    const { dispatch, onClickCancel } = this.props;
    onClickCancel();
    dispatch(appActions.modalDialogChange(null));
  }

  onClickOk() {
    const { onClickPromote } = this.props;
    const { selectedValue } = this.state;
    onClickPromote(selectedValue);
  }

  render() {
    const { header } = this.props;
    const {
      selectedValue,
    } = this.state;
    const {
      SOCIAL, SENTIMENT, YES_NO, ROOT_INTENT,
    } = Constants.PROJECT_TYPE.GLOBAL.MODELS_NAME;

    return (
      <div>
        <Dialog
          size="medium"
          okChildren={Constants.PROMOTE}
          headerChildren={header}
          onClickCancel={this.onClickCancel}
          onClickOk={this.onClickOk}
          onClickClose={this.onClickCancel}
          isOpen
          centerContent={false}
          okDisabled={!selectedValue}
          styleOverride={this.styleOverride}
        >
          <div>
            <p>
              Select global model name:
            </p>
            <RadioGroup
              values={[ROOT_INTENT, SENTIMENT, SOCIAL, YES_NO]}
              labels={[ROOT_INTENT, SENTIMENT, SOCIAL, YES_NO]}
              onChange={this.onChange}
              value={selectedValue}
            />
          </div>
        </Dialog>
      </div>
    );
  }
}

PromoteDialog.propTypes = {
  header: PropTypes.string.isRequired,
  onClickCancel: PropTypes.func,
  onClickPromote: PropTypes.func.isRequired,
  dispatch: PropTypes.func,
};

PromoteDialog.defaultProps = {
  onClickCancel: () => {},
  dispatch: () => {},
};
