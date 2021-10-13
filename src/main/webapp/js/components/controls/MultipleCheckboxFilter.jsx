import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';
import {
  Checkbox, LegacyGrid, LegacyRow, LegacyColumn,
} from '@tfs/ui-components';
import { multipleCheckboxFilter } from '../../styles/index';

export default class MultipleCheckboxFilter extends Component {
  constructor(props) {
    super(props);

    this.createGrid = this.createGrid.bind(this);
    this.getRows = this.getRows.bind(this);
    this.onChange = this.onChange.bind(this);
    this.getStyle = this.getStyle.bind(this);
  }

  onChange(event, option) {
    const { onChange, value, allowEmpty } = this.props;
    const newValue = [...value];
    const index = newValue.indexOf(option.value);
    if (index === -1) {
      newValue.push(option.value);
    } else {
      newValue.splice(index, 1);
    }
    if (!allowEmpty && newValue.length === 0) {
      return;
    }
    onChange(newValue, option.value);
  }

  get checkboxes() {
    const {
      options,
      value,
      allowEmpty,
      onChange,
      disabled,
    } = this.props;
    const checkboxes = [];

    options.forEach((option) => {
      const isChecked = value.indexOf(option.value) !== -1;
      let disabledProp = {};
      if (disabled) {
        disabledProp.disabled = true;
      }
      checkboxes.push(<Checkbox
        data-qa={`checkbox-${option.label}`}
        key={`filter-${option.value}`}
        checked={isChecked}
        label={option.label}
        disabled={disabled}
        onChange={(event) => { this.onChange(event, option); }}
        styleOverride={{
          float: 'left',
          marginLeft: '10px',
          paddingTop: '5px',
        }}
      />);
    });

    return checkboxes;
  }

  getRows(entries, noOfRows, noOfColumns) {
    const rows = [];
    let index = 0;
    const maxColumnSize = 12;
    const size = Math.floor(maxColumnSize / noOfColumns);
    for (let rowNo = 0; rowNo < noOfRows; rowNo++) {
      const columns = [];
      for (let columnNo = 0; columnNo < noOfColumns && index < entries.length; columnNo++) {
        columns.push(
          <LegacyColumn
            size={size}
            key={`row${rowNo}-col${columnNo}`}
          >
            {entries[index]}
          </LegacyColumn>,
        );
        index++;
      }
      rows.push(
        <LegacyRow
          key={`row${rowNo}`}
        >
          {columns}
        </LegacyRow>,
      );
    }
    return rows;
  }

  createGrid(checkboxes, noOfColumns = 3) {
    const length = checkboxes.length;
    const noOfRows = Math.ceil(length / noOfColumns);
    const rows = this.getRows(checkboxes, noOfRows, noOfColumns);
    return (
      <LegacyGrid styleOverride={this.props.type === 'add-speech' ? { marginTop: '15px' } : null}>
        {rows}
      </LegacyGrid>
    );
  }

  getStyle() {
    const { type, showDropbox, isSpeechBundledUnbundledEnabled } = this.props;

    let style = null;

    if (type === 'add-speech' && showDropbox === true && isSpeechBundledUnbundledEnabled) {
      style = { ...multipleCheckboxFilter, height: '360px', boxShadow: 'none' };
    } else if (type === 'add-speech' && showDropbox === true && !isSpeechBundledUnbundledEnabled) {
      style = { ...multipleCheckboxFilter, height: '260px', boxShadow: 'none' };
    } else if (type === 'add-speech') {
      style = { ...multipleCheckboxFilter, marginRight: '30px' };
    }
    return style;
  }

  render() {
    const {
      id, className, label, showAsGrid, noOfColumns,
    } = this.props;

    if (showAsGrid) {
      return (
        <div style={this.getStyle()}>
          {label ? <div className="Label">{label}</div> : null}
          {' '}
          {this.createGrid(this.checkboxes, noOfColumns)}
          {' '}
        </div>
      );
    }

    return (
      <Box
        id={id}
        className={`MultipleCheckboxFilter${className ? ` ${className}` : ''}`}
        flex
      >
        <Box direction="row" flex>
          {label ? <div className="Label">{label}</div> : null}
          <div className="List">
            {this.checkboxes}
          </div>
        </Box>
      </Box>
    );
  }
}

MultipleCheckboxFilter.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  label: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object,
  ]),
  options: PropTypes.array.isRequired,
  value: PropTypes.array,
  onChange: PropTypes.func,
  allowEmpty: PropTypes.bool,
  disabled: PropTypes.bool,
  showAsGrid: PropTypes.bool,
  noOfColumns: PropTypes.number,
  type: PropTypes.string,
  showDropbox: PropTypes.bool,
};

MultipleCheckboxFilter.defaultProps = {
  value: [],
  allowEmpty: true,
  disabled: false,
  showAsGrid: false,
  noOfColumns: 3,
  onChange: () => {},
  type: null,
  showDropbox: false,
};
