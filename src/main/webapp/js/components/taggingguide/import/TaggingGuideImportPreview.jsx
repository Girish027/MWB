// 2018, April 30
// Migration to using airbnb eslint config.
// We will be refactoring this with the new ui changes.
// For now, just disable rules.
/* eslint-disable no-restricted-syntax */
/* eslint-disable react/jsx-no-bind */
/* eslint-disable no-shadow */
/* eslint-disable func-names */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';
import Select from 'grommet/components/Select';

export default class TaggingGuideImportPreview extends Component {
  constructor(props) {
    super(props);
    this.props = props;
    this.onChange = this.onChange.bind(this);

    this.state = {
      scrollTop: 0,
      columnsBinding: {},
      boundByIndex: [],
    };

    this.columnsOptions = [];
    this.columnsNames = {};
    this.requiredColumns = [];
    this.columnsByName = {};

    this.handleScroll = this.handleScroll.bind(this);
  }

  componentDidMount() {
    const { columns, data, columnsBinding } = this.props;
    this.columnsNames = {
      __skip__: 'Skip',
    };
    this.columnsOptions = [{
      value: '__skip__',
      label: 'Skip',
    }];
    this.requiredColumns = [];

    // TODO: use forEach

    let i;
    for (i = 0; i < columns.length; i++) {
      this.columnsByName[columns[i].name] = columns[i];
      this.columnsNames[columns[i].name] = columns[i].displayName;
      this.columnsOptions.push({
        value: columns[i].name,
        label: columns[i].displayName,
      });
      if (columns[i].required) {
        this.requiredColumns.push(columns[i].name);
      }
    }

    const boundByIndex = [];
    const columnsBindingClone = Object.assign({}, columnsBinding);
    if (data.length) {
      const firstRow = data[0];
      let index;
      for (index = 0; index < firstRow.length; index++) {
        let bound = false;
        for (const columnName in columnsBindingClone) {
          if (columnsBindingClone[columnName] == index) {
            boundByIndex.push(columnName);
            bound = true;
            delete columnsBindingClone[columnName];
            break;
          }
        }
        if (!bound) {
          boundByIndex.push('__skip__');
        }
      }
    }

    // eslint-disable-next-line react/no-did-mount-set-state
    this.setState({
      columnsBinding,
      boundByIndex,
    });
  }

  onChange(option, index) {
    const { value } = option;
    const { onColumnBindChange } = this.props;
    const { columnsBinding, boundByIndex } = this.state;
    const currentBinding = boundByIndex[index];

    if (currentBinding) {
      delete columnsBinding[currentBinding];
    }
    if (value != '__skip__') {
      columnsBinding[value] = index;
    }
    boundByIndex[index] = value;

    let isBindingValid = true;
    let i;
    for (i = 0; i < this.requiredColumns.length; i++) {
      if (typeof columnsBinding[this.requiredColumns[i]] === 'undefined') {
        isBindingValid = false;
        break;
      }
    }

    this.setState({
      columnsBinding,
      boundByIndex,
    });

    const bindingArray = [];
    for (const columnName in columnsBinding) {
      if (!Object.prototype.hasOwnProperty.call(columnsBinding, columnName)) {
        // eslint-disable-next-line no-continue
        continue;
      }
      const column = this.columnsByName[columnName];
      bindingArray.push({
        id: `${column.id}`,
        columnName: column.name,
        columnIndex: `${columnsBinding[columnName]}`,
        displayName: column.displayName,
      });
    }
    isBindingValid = isBindingValid && bindingArray.length > 0;

    if (onColumnBindChange) {
      onColumnBindChange({ bindingArray, isBindingValid, columnsBinding });
    }
  }

  get headerTable() {
    const { data } = this.props;
    if (!data.length) {
      return null;
    }

    const { columnsBinding, boundByIndex } = this.state;
    const firstRow = data[0];
    const cells = [];
    let i;
    for (i = 0; i < firstRow.length; i++) {
      const index = i;
      const value = boundByIndex[index];
      const displayValue = this.columnsNames[value];
      const options = this.columnsOptions.slice(0).filter(option => option.value == '__skip__'
                    || option.value == value
                    || typeof columnsBinding[option.value] === 'undefined');

      cells.push(<td
        className={value === '__skip__' ? 'Skipped' : ''}
        key={`col-${index}`}
      >
        <div>
          <Select
            multiple={false}
            options={options}
            value={displayValue}
            onChange={({ option }) => this.onChange(option, index)}
          />
        </div>
      </td>);
    }
    return (
      <table>
        <tbody>
          <tr>
            {cells}
          </tr>
        </tbody>
      </table>
    );
  }

  get dataTable() {
    const { data } = this.props;
    const rows = [];
    if (!data.length) {
      return rows;
    }

    const { boundByIndex } = this.state;
    let i;
    let j;
    for (i = 0; i < data.length; i++) {
      const cells = [];
      for (j = 0; j < data[i].length; j++) {
        const value = boundByIndex[j];
        cells.push(<td
          key={`col-${j}`}
          className={`cell${value === '__skip__' ? ' Skipped' : ''}`}
        >
          <div>{data[i][j]}</div>
        </td>);
      }
      rows.push(<tr
        key={`row-${i + 1}`}
      >
        {cells}
      </tr>);
    }

    return (
      <table>
        <tbody>
          {rows}
        </tbody>
      </table>
    );
  }

  handleScroll(event) {
    this.setState({
      scrollTop: 0 + event.target.scrollTop,
    });
  }

  render() {
    return (
      <Box
        id="TaggingGuideMappingPreviewContainer"
        flex
        onScroll={this.handleScroll}
      >
        <div id="TaggingGuideMappingPreviewGridContainer">
          <div
            id="TaggingGuideImportPreview"
          >
            <div
              className="header-table-container"
              style={{
                top: `${this.state.scrollTop}px`,
              }}
            >
              {this.headerTable}
            </div>
            <div
              className="data-table-container"
            >
              {this.dataTable}
            </div>
          </div>
        </div>
      </Box>
    );
  }
}

TaggingGuideImportPreview.propTypes = {
  data: PropTypes.array.isRequired,
  columns: PropTypes.array.isRequired,
  onColumnBindChange: PropTypes.func,
};
