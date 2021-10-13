import tableUtils from 'utils/TableUtils';

describe('TableUtils', () => {
  const tableData = [
    {
      name: 'm12',
      projectId: '1109',
      status: 'COMPLETED',
      userId: 'abc@247.ai',
    },
    {
      name: 'm12',
      projectId: '1109',
      status: 'COMPLETED',
      userId: 'test.123@247.ai',
    },
    {
      name: 'm12',
      projectId: '1109',
      status: 'FAILED',
      userId: 'test.123@247.ai',
    },
  ];

  describe('handleTableResizeChange', () => {
    const newResized = [
      { id: 'name', value: 565.01416015625 },
    ];

    test('returns resized width', () => {
      const val = tableUtils.handleTableResizeChange(newResized, 'abc');
      expect(val.name.resizedWidth).toBe(newResized[0].value);
    });
  });

  describe('getColumnWidth', () => {
    const columnId = 'count';
    const tableState = {
      [columnId]: {
        resizedWidth: 200,
      },
    };

    const tableStateEmpty = {
    };

    test('returns new resized width when passed modified state of column ', () => {
      const val = tableUtils.getColumnWidth(tableState, columnId, 100);
      expect(val).toBe(200);
    });

    test('returns default width when passed null object in a value', () => {
      const val = tableUtils.getColumnWidth(tableStateEmpty, columnId, 100);
      expect(val).toBe(100);
    });
  });

  describe('getColumnVisibility', () => {
    const columnId = 'count';
    const tableState = {
      [columnId]: {
        resizedWidth: 200,
        visible: false,
      },
    };

    const tableStateEmpty = {
    };

    test('returns toggled value of column visibility when passed an object', () => {
      const val = tableUtils.getColumnVisibility(tableState, columnId, true);
      expect(val).toBe(true);
    });

    test('returns default value when passed null object in a value', () => {
      const val = tableUtils.getColumnVisibility(tableStateEmpty, columnId, true);
      expect(val).toBe(true);
    });
  });

  describe('getItem List', () => {
    test('returns the itemList for "status" which does not contains duplicate entries', () => {
      const val = tableUtils.getItemList(tableData, 'status');
      expect(val).toEqual(['ALL', 'COMPLETED', 'FAILED']);
    });

    test('returns the itemList for "userId" which does not contains duplicate entries', () => {
      const val = tableUtils.getItemList(tableData, 'userId');
      expect(val).toEqual(['ALL', 'abc@247.ai', 'test.123@247.ai']);
    });
  });

  describe('handle Filter Method', () => {
    const filter = {
      id: 'userId',
      value: 'abc.test@247.ai',
    };

    const nextFilter = {
      id: 'status',
      value: 'COMPLETED',
    };

    const filter1 = {
      id: 'userId',
      value: 'dummy',
    };

    const tableRow = {
      name: 'm12',
      projectId: '1109',
      status: 'COMPLETED',
      userId: 'abc.test@247.ai',
    };

    const nextTableRow = {
      name: 'm12',
      projectId: '1109',
      status: { status: 'COMPLETED' },
      userId: 'abc.test@247.ai',
    };

    test('returns true if the selected/filtered value presents in table', () => {
      const val = tableUtils.handleFilterMethod(filter, tableRow, {});
      expect(val).toBe(true);
    });

    test('returns true if the selected/filtered value presents in table', () => {
      const val = tableUtils.handleFilterMethod(nextFilter, nextTableRow, {});
      expect(val).toBe(true);
    });

    test('returns false if the selected/filtered value is not presents in table', () => {
      const val = tableUtils.handleFilterMethod(filter1, tableRow, {});
      expect(val).toBe(false);
    });
  });

  describe('handle onFilteredChange', () => {
    const filtered = [{ id: 'status', value: 'COMPLETED' }];

    test('returns correct filtered state based on new value: COMPLETED', () => {
      const val = tableUtils.onFilteredChange(filtered, 'FAILED', 'status');
      expect(val).toEqual([{ id: 'status', value: 'FAILED' }]);
    });
  });
});
