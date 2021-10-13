import { camelize } from 'utils/grid/camelize';

describe('camelize', () => {
  test('It should convert capital letter string to camelize characters', () => {
    const val1 = camelize('ABCDEF');
    expect(val1).toEqual('aBCDEF');
  });

  test('It should not convert small letter string to camelize characters', () => {
    const val2 = camelize('abcdef');
    expect(val2).toEqual('abcdef');
  });

  test('It should remove space in between the string and convert first letter after space to capital letter', () => {
    const val3 = camelize('abc / . def');
    expect(val3).toEqual('abc/.Def');
  });

  test('It should remove space and should not impact to capital string given after space', () => {
    const val4 = camelize('abc        def    GHI');
    expect(val4).toEqual('abcDefGHI');
  });
});
