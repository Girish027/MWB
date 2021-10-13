import phoneNumber from 'components/modelConfigs/transformations/predefinedTransforms/phoneNumber';

describe('phoneNumber', () => {
  const regex = phoneNumber();

  test('type is equal to wordclass-subst-regex', () => {
    expect(regex.type).toEqual('wordclass-subst-regex');
  });

  test('mapping contains more than one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toBeGreaterThan(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
