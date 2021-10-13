import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Box from 'grommet/components/Box';
import Select from 'grommet/components/Select';
import { escapeRegExp } from 'utils/StringUtils';

export default class BindColumnsPreviewGrid extends Component {
  constructor(props) {
    super(props);
    this.props = props;

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

  handleScroll(event) {
    this.setState({
      scrollTop: 0 + event.target.scrollTop,
    });
  }

  componentDidMount() {
    const { columns, data, onColumnBindChange } = this.props;
    const { isSomeColumnsPreSelected, columnsBinding } = this.tryPreSelectColumns(this.props.columnsBinding);
    const columnsBindingClone = Object.assign({}, columnsBinding);

    this.columnsNames = {
      __skip__: 'Skip',
    };
    this.columnsOptions = [{
      value: '__skip__',
      label: 'Skip',
    }];
    this.requiredColumns = [];

    columns.forEach(column => {
      this.columnsByName[column.name] = column;
      this.columnsNames[column.name] = column.displayName;
      this.columnsOptions.push({
        value: column.name,
        label: column.displayName,
      });
      if (column.required) {
        this.requiredColumns.push(column.name);
      }
    });

    const boundByIndex = [];
    if (data.length) {
      let firstRow = data[0],
        index;
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

    this.setState({
      columnsBinding,
      boundByIndex,
    });
    if (onColumnBindChange && isSomeColumnsPreSelected) {
      let isBindingValid = true;
      for (let i = 0; i < this.requiredColumns.length; i++) {
        if (typeof columnsBinding[this.requiredColumns[i]] === 'undefined') {
          isBindingValid = false;
          break;
        }
      }
      const bindingArray = [];
      for (const columnName in columnsBinding) {
        if (!columnsBinding.hasOwnProperty(columnName)) {
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
      onColumnBindChange({
        bindingArray, isBindingValid, columnsBinding, isPreSelected: true,
      });
    }
  }

  componentWillUnmount() {
  }

  tryPreSelectColumns(columnsBindingInput) {
    const { columns, data } = this.props;
    const firstRow = data.length ? data[0] : [];
    const columnsBinding = Object.assign({}, columnsBindingInput);

    if (!firstRow.length || !columns.length || (Object.keys(columnsBinding)).length != 0) {
      return { isSomeColumnsPreSelected: false, columnsBinding };
    }

    columns.forEach((c) => {
      let foundIndexes = [],
        usedIndexes = {};
      // pass 1 - exact match
      let tests = [
        new RegExp(`^${escapeRegExp(c.name)}$`, 'i'),
        new RegExp(`^${escapeRegExp(c.displayName)}$`, 'i'),
      ];

      firstRow.forEach((newData1, index) => {
        for (let i = 0; i < tests.length; i++) {
          if (tests[i].test(newData1.trim())) {
            foundIndexes.push(index);
            break;
          }
        }
      });

      if (!foundIndexes.length) {
        // pass 2 - partial match
        tests = [
          new RegExp(escapeRegExp(c.name), 'i'),
          new RegExp(escapeRegExp(c.displayName), 'i'),
        ];

        firstRow.forEach((newData2, index) => {
          for (let i = 0; i < tests.length; i++) {
            if (tests[i].test(newData2)) {
              foundIndexes.push(index);
              break;
            }
          }
        });
      }

      if (foundIndexes.length === 1 && !usedIndexes[foundIndexes[0]]) {
        columnsBinding[c.name] = foundIndexes[0];
        usedIndexes[foundIndexes[0]] = true;
      }
    });

    return { isSomeColumnsPreSelected: (Object.keys(columnsBinding)).length != 0, columnsBinding };
  }

  onSelectChange = (option, index) => {
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
    for (let i = 0; i < this.requiredColumns.length; i++) {
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
      if (!columnsBinding.hasOwnProperty(columnName)) {
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
  };

  get headerTable() {
    const { data } = this.props;
    if (!data.length) {
      return null;
    }

    const { columnsBinding, boundByIndex } = this.state;
    let firstRow = data[0],
      cells = [];
    for (let i = 0; i < firstRow.length; i++) {
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
            onChange={({ option }) => (this.onSelectChange(option, index))}
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
    for (let i = 0; i < data.length; i++) {
      const cells = [];
      for (let j = 0; j < data[i].length; j++) {
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

  render() {
    return (
      <Box
        className="MappingPreviewContainer"
        flex
        onScroll={this.handleScroll}
      >
        <div
          className="MappingPreviewGridContainer"
        >
          <div
            className="Preview"
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

BindColumnsPreviewGrid.propTypes = {
  data: PropTypes.array.isRequired,
  columns: PropTypes.array.isRequired,
  onColumnBindChange: PropTypes.func,
};
