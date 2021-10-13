import apostrophe from 'components/modelConfigs/transformations/predefinedTransforms/apostrophe';

describe('apostrophe', () => {
  const regex = apostrophe();

  test('type is equal to regex-replace', () => {
    expect(regex.type).toEqual('regex-replace');
  });

  test('mapping contains one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
