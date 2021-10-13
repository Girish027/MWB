import sentenceMarker from 'components/modelConfigs/transformations/predefinedTransforms/sentenceMarker';

describe('sentenceMarker', () => {
  const regex = sentenceMarker();

  test('type is equal to regex-replace', () => {
    expect(regex.type).toEqual('regex-replace');
  });

  test('mapping contains one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toBeGreaterThanOrEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
