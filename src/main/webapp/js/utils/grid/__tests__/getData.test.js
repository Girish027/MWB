import { nameFromDataIndex } from 'utils/grid/getData';

describe('nameFromDataIndex', () => {
  const column = {
    name: 'abc',
  };
  test('It should return empty string if column is NULL', () => {
    const val1 = nameFromDataIndex(null);
    expect(val1).toEqual('');
  });

  test('It should return empty if column is empty', () => {
    const val2 = nameFromDataIndex('');
    expect(val2).toEqual('');
  });

  test('It should return column index for string type', () => {
    const mColumn = {
      ...column,
      dataIndex: 'sdds',
    };
    const val2 = nameFromDataIndex(mColumn);
    expect(val2).toEqual('sdds');
  });

  test('It should return last value of array columnIndex', () => {
    const mColumn = {
      ...column,
      dataIndex: ['dd', 'dsfdf', 'fee'],
    };
    const val2 = nameFromDataIndex(mColumn);
    expect(val2).toEqual('fee');
  });

  test('It should return camal case of column name value', () => {
    const val2 = nameFromDataIndex(column);
    expect(val2).toEqual('abc');
  });
});
