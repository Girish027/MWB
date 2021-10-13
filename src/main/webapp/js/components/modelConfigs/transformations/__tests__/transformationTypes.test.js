import transformationTypes, {
  getTransformationTypesDisplayArray,
  getCategory,
  transformationTypeHasName,
  convertTransformationTypeIfNeeded,
} from 'components/modelConfigs/transformations/transformationTypes';

describe('transformationTypes', () => {
  describe('getTransformationTypesDisplayArray', () => {
    test('should return an array that has length greater than 1', () => {
      const result = getTransformationTypesDisplayArray();
      expect(result.length).toBeGreaterThan(1);
    });
  });
  describe('getCategory', () => {
    test('should return "pairs" for "wordclass-subst-text ', () => {
      const result = getCategory('wordclass-subst-text');
      expect(result).toEqual('pairs');
    });
  });
  describe('transformationTypeHasName', () => {
    test('should return "false" for "whitespace-normalization ', () => {
      const result = transformationTypeHasName('whitespace-normalization');
      expect(result).toEqual(false);
    });
  });
  describe('convertTransformationTypeIfNeeded', () => {
    test('should return "stems" for "stems-url', () => {
      const result = convertTransformationTypeIfNeeded('stems-url');
      expect(result).toEqual('stems');
    });
    test('should return "stems-nocase" for "stems-nocase-url', () => {
      const result = convertTransformationTypeIfNeeded('stems-nocase-url');
      expect(result).toEqual('stems-nocase');
    });
    test('should return "whitespace-normalization" for "whitespace-normalization', () => {
      const result = convertTransformationTypeIfNeeded('whitespace-normalization');
      expect(result).toEqual('whitespace-normalization');
    });
  //    test('should return "url Transformation" for "url Transformation', () => {
  //      const result = convertTransformationTypeIfNeeded('url Transformation');
  //      expect(result).toEqual('url Transformation');
  //    });
  });
});
