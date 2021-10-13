import classTime from 'components/modelConfigs/transformations/predefinedTransforms/classTime';

describe('classTime', () => {
  const regex = classTime();

  test('type is equal to wordclass-subst-regex', () => {
    expect(regex.type).toEqual('wordclass-subst-regex');
  });

  test('mapping contains more than one key', () => {
    const keys = Object.keys(regex.mappings);
    expect(keys.length).toBeGreaterThanOrEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.mappings).toMatchSnapshot();
  });
});
