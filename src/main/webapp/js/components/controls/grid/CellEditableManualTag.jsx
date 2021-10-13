import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import validationUtil from 'utils/ValidationUtil';
import CellEditable from 'components/controls/grid/CellEditable';
import CellEditableManualTagSuggest from 'components/controls/grid/CellEditableManualTagSuggest';

export class CellEditableManualTag extends Component {
  constructor(props, context) {
    super(props, context);
    this.onEdit = this.onEdit.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onValidChange = this.onValidChange.bind(this);

    this.state = {
      editedValue: this.props.value,
      isEditedValueValid: true,
    };
  }

  componentWillReceiveProps(nextProps) {
    const prevValue = this.props.value;
    this.props = nextProps;

    if (prevValue !== this.props.value) {
      this.setState({
        editedValue: this.props.value,
        isEditedValueValid: true,
      });
    }
  }

  onEdit(newValue, isValid) {
    this.setState({
      editedValue: newValue,
    });
  }

  onChange(newValue) {
    const { onChange } = this.props;
    if (onChange) {
      onChange(newValue);
    }
  }

  onValidChange(isValid) {
  }

  render() {
    const {
      id, className, stateKey, value, rowIndex, columnIndex,
      maxRowIndex, maxColumnIndex, stopClickPropagation,
      projectId, editable, highlightTags, activateNextCellOnEnter,
    } = this.props;
    const { editedValue } = this.state;

    const isActive = typeof rowIndex !== 'undefined'
            && typeof columnIndex !== 'undefined'
            && editable.activeRowIndex === rowIndex
            && editable.activeColumnIndex === columnIndex;

    return (
      <div
        id={id}
        className={`CellEditableManualTag${className ? ` ${className}` : ''}`}
      >
        <CellEditable
          stateKey={stateKey}
          value={value}
          rowIndex={rowIndex}
          columnIndex={columnIndex}
          maxRowIndex={maxRowIndex}
          maxColumnIndex={maxColumnIndex}
          stopClickPropagation={stopClickPropagation}
          validation={validationUtil.validateTaggingGuideTag}
          suggestedStateKey="cellEditableManualTagSuggest"
          activateNextCellOnEnter={activateNextCellOnEnter}
          onValidChange={this.onValidChange}
          onChange={this.onChange}
          onEdit={this.onEdit}
        />
        {isActive && editedValue.length ? (
          <CellEditableManualTagSuggest
            stateKey={stateKey}
            intent={editedValue}
            projectId={projectId}
            highlightTags={highlightTags}
          />
        ) : null}
      </div>
    );
  }
}

CellEditableManualTag.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  stateKey: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  projectId: PropTypes.string.isRequired,
  rowIndex: PropTypes.number,
  columnIndex: PropTypes.number,
  maxRowIndex: PropTypes.number,
  maxColumnIndex: PropTypes.number,
  stopClickPropagation: PropTypes.bool,
  highlightTags: PropTypes.array,
  onChange: PropTypes.func,
  activateNextCellOnEnter: PropTypes.bool,
};

CellEditableManualTag.defaultProps = {
  value: '',
  rowIndex: -1,
  columnIndex: -1,
  highlightTags: [],
  activateNextCellOnEnter: true,
};

const mapStateToProps = (state, ownProps) => ({
  editable: state.cellEditable.get(ownProps.stateKey),
  suggest: state.cellEditableManualTagSuggest.get(ownProps.stateKey),
});

export default connect(mapStateToProps)(CellEditableManualTag);
