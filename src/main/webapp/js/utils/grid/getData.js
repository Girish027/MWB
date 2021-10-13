import { camelize } from './camelize';

export const nameFromDataIndex = (column) => {
  if (!column) {
    return '';
  }
  if (typeof column.dataIndex === 'string') {
    return column.dataIndex;
  }
  if (Array.isArray(column.dataIndex)) {
    return column.dataIndex[column.dataIndex.length - 1];
  }
  if (!column.dataIndex) {
    return camelize(column.name);
  }
};
