import {
  reorder,
  getItemStyle,
  getEditorListStyle,
  getPostProcessingListStyle,
  getTransformationListStyle,
  getLastItemStyle,
} from 'components/modelConfigs/modelConfigUtilities';

describe('<modelConfigUtilities />', () => {
  describe('reorder', () => {
    test('should return reordered list - 3 in the 5th position', () => {
      const result = reorder([1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 2, 5);
      expect(result[5]).toEqual(3);
    });
  });
  describe('getItemStyle', () => {
    test('should return the right color if isDragging is true', () => {
      expect(getItemStyle(true, {}).background).toEqual('#f6f7f8');
    });
    test('should return white if isDragging is false', () => {
      expect(getItemStyle(false, {}).background).toEqual('white');
    });
  });
  describe('getEditorListStyle', () => {
    test('should return the right color if isDragging is true', () => {
      expect(getEditorListStyle(true).background).toEqual('#f6f7f8');
    });
    test('should return white if isDraggingOver is false', () => {
      expect(getEditorListStyle(false).background).toEqual('white');
    });
  });
  describe('getPostProcessingListStyle', () => {
    test('should return the right color if isDragging is true', () => {
      expect(getPostProcessingListStyle(true).background).toEqual('#f6f7f8');
    });
    test('should return white if isDraggingOver is false', () => {
      expect(getPostProcessingListStyle(false).background).toEqual('white');
    });
  });
  describe('getTransformationListStyle', () => {
    test('should return the right color if isDragging is true', () => {
      expect(getTransformationListStyle(true).background).toEqual('#f6f7f8');
    });
    test('should return white if isDraggingOver is false', () => {
      expect(getTransformationListStyle(false).background).toEqual('white');
    });
  });
});
