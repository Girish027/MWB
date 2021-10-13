import removeDupWords from 'components/modelConfigs/transformations/predefinedTransforms/removeDupWords';

describe('removeDupWords', () => {
  const regex = removeDupWords();

  test('type is equal to regex-replace', () => {
    expect(regex.type).toEqual('regex-replace');
  });

  test('mapping contains more than one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toBeGreaterThanOrEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
