import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import store from 'state/configureStore';
import * as actionsCellEditable from 'state/actions/actions_cellEditable';
import * as actionsCellEditableManualTagSuggest from 'state/actions/actions_cellEditableManualTagSuggest';

export class CellEditable extends Component {
  constructor(props, context) {
    super(props, context);

    this.onClick = this.onClick.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onChange = this.onChange.bind(this);

    this.isEscaped = false;

    this.state = {
      value: this.props.value,
      isFocused: false,
      isValid: true,
      isTouched: false,
    };
  }

  componentWillReceiveProps(nextProps) {
    const prevValue = this.props.value;
    this.props = nextProps;

    const {
      stateKey, rowIndex, columnIndex, value, tableState,
    } = this.props;
    const { activeRowIndex, activeColumnIndex } = tableState;

    const newState = {};

    if (prevValue != value) {
      newState.value = value;
      this.validate(newState.value);
    }

    if (
      activeRowIndex === rowIndex
            && activeColumnIndex === columnIndex
            && this.props.tableState.value
            && newState.value != this.props.tableState.value
    ) {
      newState.value = this.props.tableState.value;
      this.validate(newState.value);
      store.dispatch(actionsCellEditable.updateValue({
        stateKey,
        value: null,
      }));
    }

    this.checkFocus();
    this.setState(newState);
  }

  componentWillMount() {

  }

  componentDidMount() {
    this.checkFocus();
  }

  onClick(event) {
    const { stopClickPropagation } = this.props;
    if (stopClickPropagation) {
      event.stopPropagation();
    }
  }

  onFocus() {
    this.isEscaped = false;
    this.setState({
      isFocused: true,
      value: this.props.value,
      isValid: true,
    });
    this.dispatchSetActiveCell(true);
  }

  onBlur() {
    let { isValid, value } = this.state;
    if (this.state.value !== this.props.value && !this.isEscaped && this.props.onChange) {
      this.isEscaped = false;
      isValid = this.validate(this.state.value);
      if (isValid) {
        this.props.onChange(value);
      } else {
        value = this.props.value;
      }
    }
    this.setState({
      isFocused: false,
      isTouched: false,
      value,
      isValid,
    });
    this.dispatchSetActiveCell(false);
  }

  onKeyDown(event) {
    const {
      tableState, stateKey, rowIndex, columnIndex, maxRowIndex,
      maxColumnIndex, suggested, activateNextCellOnEnter,
    } = this.props;
    const { activeRowIndex, activeColumnIndex } = tableState;
    const { keyCode, shiftKey, ctrlKey } = event;
    const { input } = this.refs;

    if (event.key === 'Enter') {
      if (activateNextCellOnEnter) {
        let newRowIndex = rowIndex;
        const newColumnIndex = columnIndex;
        newRowIndex++;
        if (newRowIndex > maxRowIndex) {
          newRowIndex = maxRowIndex;
        }
        if (activeRowIndex !== newRowIndex || activeColumnIndex !== columnIndex) {
          if (input) {
            input.blur();
            // this.onBlur();
          }
          store.dispatch(actionsCellEditable.setActiveCell({
            stateKey,
            activeRowIndex: newRowIndex,
            activeColumnIndex: newColumnIndex,
          }));
        }
      } else {
        if (input) {
          input.blur();
          // this.onBlur();
        }
        store.dispatch(actionsCellEditable.setActiveCell({
          stateKey,
          activeRowIndex: null,
          activeColumnIndex: null,
        }));
      }
    } else if (
      keyCode === 9 // Tab key
            || keyCode === 40 // Down Arrow
            || keyCode === 38 // Up Arrow
            || keyCode === 37 // Left Arrow
            || keyCode === 39 // Right Arrow
    ) {
      if (
        suggested
                && (event.keyCode == 40 || event.keyCode == 38)
                && suggested.suggestedTags.length
                && suggested.intent.length
      ) {
        if (event.keyCode == 40) {
          store.dispatch(actionsCellEditableManualTagSuggest.cursorDown({ stateKey }));
        } else if (event.keyCode == 38) {
          store.dispatch(actionsCellEditableManualTagSuggest.cursorUp({ stateKey }));
        }
        return;
      }

      if (typeof rowIndex !== 'undefined' && typeof columnIndex !== 'undefined') {
        let newRowIndex = rowIndex;
        let newColumnIndex = columnIndex;

        const left = (ctrlKey) => {
          if (ctrlKey) {
            newColumnIndex = 0;
          } else {
            newColumnIndex--;
            if (newColumnIndex < 0) {
              newColumnIndex = maxColumnIndex;
              newRowIndex--;
            }
            if (newRowIndex < 0) {
              newRowIndex = 0;
              newColumnIndex = 0;
            }
          }
        };
        const down = (ctrlKey) => {
          if (ctrlKey) {
            newRowIndex = maxRowIndex;
          } else {
            newRowIndex++;
            if (newRowIndex > maxRowIndex) {
              newRowIndex = maxRowIndex;
            }
          }
        };
        const up = (ctrlKey) => {
          if (ctrlKey) {
            newRowIndex = 0;
          } else {
            newRowIndex--;
            if (newRowIndex < 0) {
              newRowIndex = 0;
            }
          }
        };
        const right = (ctrlKey) => {
          if (ctrlKey) {
            newColumnIndex = maxColumnIndex;
          } else {
            newColumnIndex++;
            if (newColumnIndex > maxColumnIndex) {
              newColumnIndex = 0;
              newRowIndex++;
            }
            if (newRowIndex > maxRowIndex) {
              newRowIndex = maxRowIndex;
              newColumnIndex = maxColumnIndex;
            }
          }
        };

        switch (keyCode) {
        case 9: // Tab key
          if (shiftKey) {
            left(false);
          } else {
            right(false);
          }
          break;

        case 40: // Down Arrow
          down(ctrlKey);
          break;

        case 38: // Up Arrow
          up(ctrlKey);
          break;

        case 37: // Left Arrow
          left(ctrlKey);
          break;

        case 39: // Right Arrow
          right(ctrlKey);
          break;
        }

        if (activeRowIndex !== newRowIndex || activeColumnIndex !== newColumnIndex) {
          event.preventDefault();
          if (input) {
            input.blur();
            // this.onBlur();
          }
          store.dispatch(actionsCellEditable.setActiveCell({
            stateKey,
            activeRowIndex: newRowIndex,
            activeColumnIndex: newColumnIndex,
          }));
        }
      }
    } else if (event.keyCode === 27) { // Esc
      this.isEscaped = true;
      if (input) {
        this.setState({
          value: this.props.value,
        });
        input.blur();
        // this.onBlur();
      }
      store.dispatch(actionsCellEditable.setActiveCell({
        stateKey,
        activeRowIndex: null,
        activeColumnIndex: null,
      }));
    }
  }

  onChange(event) {
    const { onValidChange, onEdit } = this.props;
    const { isValid } = this.state;
    const newValue = event.target.value;
    const newIsValid = this.validate(newValue);
    const isValueChanged = newValue !== this.state.value;

    this.setState({
      value: newValue,
      isTouched: true,
      isValid: newIsValid,
    });

    if (isValueChanged && onEdit) {
      onEdit(newValue, newIsValid);
    }

    if (newIsValid != isValid && onValidChange) {
      onValidChange(newIsValid);
    }
  }

  validate(value) {
    const { validation } = this.props;
    if (!validation) {
      return true;
    }
    return validation(value);
  }

  checkFocus() {
    const { input } = this.refs;
    const { tableState, rowIndex, columnIndex } = this.props;
    const { activeRowIndex, activeColumnIndex } = tableState;

    if (!input || typeof rowIndex === 'undefined' || typeof columnIndex === 'undefined') {
      return;
    }

    if (!this.state.isFocused) {
      if (rowIndex === activeRowIndex && columnIndex === activeColumnIndex) {
        input.focus();
        this.onFocus();
      }
    } else if (rowIndex !== activeRowIndex || columnIndex !== activeColumnIndex) {
      input.blur();
      // this.onBlur();
    }
  }

  dispatchSetActiveCell(isActive) {
    const {
      stateKey, tableState, rowIndex, columnIndex,
    } = this.props;
    const { activeRowIndex, activeColumnIndex } = tableState;

    if (typeof rowIndex === 'undefined' || typeof columnIndex === 'undefined') {
      return;
    }

    if (isActive) {
      if (activeRowIndex !== rowIndex || activeColumnIndex !== columnIndex) {
        store.dispatch(actionsCellEditable.setActiveCell({
          stateKey,
          activeRowIndex: rowIndex,
          activeColumnIndex: columnIndex,
        }));
      }
    } else if (activeRowIndex === rowIndex && activeColumnIndex === columnIndex) {
      store.dispatch(actionsCellEditable.setActiveCell({
        stateKey,
        activeRowIndex: null,
        activeColumnIndex: null,
      }));
    }
  }

  render() {
    const { id, className } = this.props;
    const { value, isValid, isTouched } = this.state;

    const classNameAttr = `CellEditable${
      isTouched ? ' touched' : ''
    }${isValid ? '' : ' error'
    }${className ? ` ${className}` : ''}`;

    return (
      <div
        id={id}
        className={classNameAttr}
      >
        <input
          type="text"
          value={value}
          onClick={this.onClick}
          onFocus={this.onFocus}
          onBlur={this.onBlur}
          onKeyDown={this.onKeyDown}
          onChange={this.onChange}
          ref="input"
        />
      </div>
    );
  }
}

CellEditable.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  stateKey: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func,
  onEdit: PropTypes.func,
  rowIndex: PropTypes.number,
  columnIndex: PropTypes.number,
  maxRowIndex: PropTypes.number,
  maxColumnIndex: PropTypes.number,
  stopClickPropagation: PropTypes.bool,
  validation: PropTypes.func,
  onValidChange: PropTypes.func,
  suggestedStateKey: PropTypes.string,
  activateNextCellOnEnter: PropTypes.bool,
};

CellEditable.defaultProps = {
  value: '',
  rowIndex: -1,
  columnIndex: -1,
  activateNextCellOnEnter: true,
};

const mapStateToProps = (state, ownProps) => ({
  tableState: state.cellEditable ? state.cellEditable.get(ownProps.stateKey) : {},
  suggested: ownProps.suggestedStateKey && typeof state[ownProps.suggestedStateKey] !== 'undefined' ? state[ownProps.suggestedStateKey].get(ownProps.stateKey) : null,
});

export default connect(mapStateToProps)(CellEditable);
