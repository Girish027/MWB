import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  TextField,
  Textarea,
  LegacyGrid,
  LegacyRow,
  LegacyColumn,
} from '@tfs/ui-components';
import * as appActions from 'state/actions/actions_app';
import Constants from 'constants/Constants';
import { headerIcon } from '../../styles';

const { TAGGING_GUIDE_TABLE } = Constants;

export default class AddIntentDialog extends Component {
  constructor(props) {
    super(props);
    this.props = props;

    this.onClickClose = this.onClickClose.bind(this);
    this.onClickAdd = this.onClickAdd.bind(this);
    this.renderForm = this.renderForm.bind(this);
    this.renderItem = this.renderItem.bind(this);
    this.onChange = this.onChange.bind(this);


    this.styleOverride = {
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

    this.initialState = {
      [TAGGING_GUIDE_TABLE.granularIntent.id]: '',
      [TAGGING_GUIDE_TABLE.rollupIntent.id]: '',
      [TAGGING_GUIDE_TABLE.description.id]: '',
      [TAGGING_GUIDE_TABLE.keywords.id]: '',
      [TAGGING_GUIDE_TABLE.examples.id]: '',
      [TAGGING_GUIDE_TABLE.comments.id]: '',
      valid: {},
    };

    this.state = {
      ...this.initialState,
    };
  }

  onChange(event) {
    const { validateData } = this.props;
    const { value, name: columnId } = event.target;

    const isValid = validateData(value, columnId);
    if (isValid) {
      this.setState({
        [columnId]: value,
        valid: {
          ...this.state.valid,
          [columnId]: true,
        },
      });
    } else {
      this.setState({
        valid: {
          ...this.state.valid,
          [columnId]: false,
        },
      });
    }
  }

  onClickAdd() {
    const { onClickAdd } = this.props;
    onClickAdd(this.state);
    this.onClickClose();
  }

  onClickClose() {
    const { dispatch } = this.props;
    this.setState(this.initialState);
    dispatch(appActions.modalDialogChange(null));
  }

  renderItem(item, type = Constants.TEXTFIELD) {
    const { valid } = this.state;
    const itemProps = {
      type: 'text',
      name: item.id,
      placeholder: item.placeholder,
      onChange: this.onChange,
      styleOverride: {
        width: '320px',
      },
      invalid: valid[item.id] === false,
      tooltipText: item.tooltipText,
    };
    return (
      <LegacyRow styleOverride={{ padding: '5px' }}>
        <LegacyColumn
          size={3}
          styleOverride={{ paddingTop: '10px' }}
        >
          <div className="float-left">
            <span>
              {' '}
              {item.header}
            </span>
            <span>
              {' '}
              {item.mandatory ? '*' : ''}
              {' '}
            </span>
          </div>
          <div className="float-right">:</div>
        </LegacyColumn>
        <LegacyColumn
          size={9}
          styleOverride={{ paddingLeft: '10px' }}
        >
          {type === Constants.TEXTFIELD
            ? (
              <TextField
                {...itemProps}
                showValidCheck={valid[item.id] === true}
              />
            )
            : (
              <Textarea
                {...itemProps}
              />
            )
          }
        </LegacyColumn>
      </LegacyRow>
    );
  }

  // commenting below fields
  // Reason: interim design and bug: https://247inc.atlassian.net/browse/NT-2531
  renderForm() {
    return (
      <LegacyGrid>
        {this.renderItem(TAGGING_GUIDE_TABLE.granularIntent)}
        {this.renderItem(TAGGING_GUIDE_TABLE.rollupIntent)}
        {/* <span>
        {this.renderItem(TAGGING_GUIDE_TABLE.description, Constants.TEXTAREA)}
        {this.renderItem(TAGGING_GUIDE_TABLE.keywords, Constants.TEXTAREA)}
        {this.renderItem(TAGGING_GUIDE_TABLE.examples, Constants.TEXTAREA)}
        {this.renderItem(TAGGING_GUIDE_TABLE.comments, Constants.TEXTAREA)}
        </span> */}
      </LegacyGrid>
    );
  }

  render() {
    const { header } = this.props;
    const {
      valid = {},
    } = this.state;
    const { intent: validIntent } = valid;
    return (
      <div>
        <Dialog
          isOpen
          closeIconVisible
          onClickClose={this.onClickClose}
          okVisible
          okChildren={Constants.ADD}
          onClickOk={this.onClickAdd}
          okDisabled={!validIntent}
          cancelChildren={Constants.CANCEL}
          onClickCancel={this.onClickClose}
          headerChildren={header}
          centerContent={false}
          size="medium"
          styleOverride={this.styleOverride}
        >
          {this.renderForm()}
        </Dialog>
      </div>
    );
  }
}

AddIntentDialog.defaultProps = {
  header: '',
  validateData: () => true,
  dispatch: () => {},
  onClickAdd: () => {},
};

AddIntentDialog.propTypes = {
  dispatch: PropTypes.func,
  header: PropTypes.node,
  validateData: PropTypes.func,
  onClickAdd: PropTypes.func,
};
