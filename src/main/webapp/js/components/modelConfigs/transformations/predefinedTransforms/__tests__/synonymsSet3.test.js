import synonymsSet3 from 'components/modelConfigs/transformations/predefinedTransforms/synonymsSet3';

describe('synonymsSet2', () => {
  const regex = synonymsSet3();

  test('type is equal to regex-replace', () => {
    expect(regex.type).toEqual('regex-replace');
  });

  test('mapping contains more than one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toBeGreaterThan(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
