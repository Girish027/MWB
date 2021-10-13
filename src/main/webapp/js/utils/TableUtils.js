import Constants from '../constants/Constants';

class TableUtils {
  constructor() {
    this.handleTableResizeChange = this.handleTableResizeChange.bind(this);
    this.getColumnWidth = this.getColumnWidth.bind(this);
    this.getColumnVisibility = this.getColumnVisibility.bind(this);
    this.onFilteredChange = this.onFilteredChange.bind(this);
    this.handleFilterMethod = this.handleFilterMethod.bind(this);
    this.getItemList = this.getItemList.bind(this);
  }

  getColumnWidth(tableState, columnId, defaultWidth) {
    if (tableState && tableState.hasOwnProperty(columnId) && tableState[columnId].hasOwnProperty(Constants.TABLE_CONSTANTS.RESIZED_WIDTH)) {
      return tableState[columnId].resizedWidth;
    }

    return defaultWidth;
  }

  getColumnVisibility(tableState, columnId, defaultVisiblity) {
    if (tableState && tableState.hasOwnProperty(columnId) && tableState[columnId].hasOwnProperty(Constants.TABLE_CONSTANTS.VISBLE)) {
      return !tableState[columnId].visible;
    }
    return defaultVisiblity;
  }

  handleTableResizeChange(newResized, localStorageKey) {
    let resizedData = {},
      modifiedData = {},
      storedSettings = {};

    if (newResized && localStorageKey) {
      newResized.forEach((column) => {
        let resizedCol = column.id;
        let resizedWidth = column.value;

        resizedData = Object.assign(resizedData, {
          [resizedCol]: {
            activeCol: resizedCol,
            resizing: true,
            resizedWidth,
          },
        });

        try {
          storedSettings = JSON.parse(localStorage.getItem(localStorageKey)) || {};
        } catch (e) { // do nothing
        }

        resizedData = Object.assign({}, {
          [resizedCol]: { ...storedSettings[resizedCol], ...resizedData[resizedCol] },
        });
      });

      modifiedData = Object.assign({}, storedSettings, resizedData);
      localStorage.setItem(localStorageKey, JSON.stringify(modifiedData));
    }

    return modifiedData;
  }

  onFilteredChange = (filteredState, value, accessor, localStorageKey) => {
    let insertNewFilter = true;
    let storedSettings = {};
    let modifiedState = {};

    if (filteredState.length) {
      filteredState.forEach((filter, i) => {
        if (filter.id === accessor) {
          if (value === '' || !value.length) {
            filteredState.splice(i, 1);
          } else {
            filter.value = value;
          }

          insertNewFilter = false;
        }
      });
    }

    if (insertNewFilter) {
      filteredState.push({ id: accessor, value });
    }

    let modifiedFilteredState = {};

    // storing the newFilteredState in local storage, so that we can retain the selected filtering after page reload
    filteredState.forEach((column, i) => {
      modifiedFilteredState = Object.assign(modifiedFilteredState, {
        [i]: {
          id: column.id,
          value: column.value,
        },
      });
      try {
        storedSettings = JSON.parse(localStorage.getItem(localStorageKey)) || {};
      } catch (e) { // do nothing
      }

      modifiedState = Object.assign({}, {
        [i]: { ...storedSettings[i], ...modifiedFilteredState[i] },
      });
    });

    modifiedState = Object.assign({}, storedSettings, modifiedFilteredState);
    localStorage.setItem(localStorageKey, JSON.stringify(modifiedState));

    return filteredState;
  };

  handleFilterMethod(filter, value, column) {
    const id = filter.id;

    if (filter.value === Constants.ALL) return true;

    return typeof value[id] === 'object'
      ? String(value[id][id]).indexOf(filter.value) > -1
      : String(value[id]).indexOf(filter.value) > -1;
  }

  getItemList(tableData, filterBy) {
    const itemList = [Constants.ALL];
    tableData.map((value) => {
      if (!itemList.includes(value[filterBy])) {
        itemList.push(value[filterBy]);
      }
    });

    return itemList;
  }
}

const tableUtils = new TableUtils();
export default tableUtils;
