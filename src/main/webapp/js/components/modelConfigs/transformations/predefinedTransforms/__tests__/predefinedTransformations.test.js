import predefinedTransformations, { getPredefinedDisplayList, getTransforms }
  from 'components/modelConfigs/transformations/predefinedTransforms/predefinedTransformations';

describe('predefinedTransformations', () => {
  const transforms = predefinedTransformations;

  test('expect each item to have name, id and getMethod', () => {
    const valid = true;
    transforms.forEach((item) => {
      const keys = Object.keys(item);
      expect(keys.length).toBeGreaterThanOrEqual(3);
      let index = keys.indexOf('name');
      expect(index).not.toEqual(-1);
      index = keys.indexOf('getMethod');
      expect(index).not.toEqual(-1);
      index = keys.indexOf('id');
      expect(index).not.toEqual(-1);
      expect(typeof item.getMethod).toEqual('function');
    });
    expect(valid).toBe(true);
  });

  describe('getPredefinedDisplayList', () => {
    test('should contain at least one item', () => {
      const results = getPredefinedDisplayList();
      expect(results.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('getTransforms', () => {
    test('should return two items when passed in two tranforms', () => {
      const transformList = ['htmlEncoding', 'phoneNumber'];
      const results = getTransforms(transformList);
      expect(results.length).toEqual(2);
    });
  });
});
