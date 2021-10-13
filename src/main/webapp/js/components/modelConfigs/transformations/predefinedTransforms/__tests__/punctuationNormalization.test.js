import punctuationNormalization from 'components/modelConfigs/transformations/predefinedTransforms/punctuationNormalization';

describe('punctuationNormalization', () => {
  const regex = punctuationNormalization();

  test('type is equal to regex-removal', () => {
    expect(regex.type).toEqual('regex-replace');
  });

  test('mapping contains one key', () => {
    expect(Object.keys(regex.mappings).length).toEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
