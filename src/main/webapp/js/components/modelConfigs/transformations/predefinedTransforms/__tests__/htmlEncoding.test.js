import htmlEncoding from 'components/modelConfigs/transformations/predefinedTransforms/htmlEncoding';

describe('htmlEncoding', () => {
  const regex = htmlEncoding();

  test('type is equal to regex-removal', () => {
    expect(regex.type).toEqual('regex-removal');
  });

  test('mapping contains one key', () => {
    expect(regex.list.length).toEqual(1);
  });

  test('snapshot predefined transformation', () => {
    expect(regex.list).toMatchSnapshot();
  });
});
