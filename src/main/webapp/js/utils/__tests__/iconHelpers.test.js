import getIcon, { IconNames } from 'utils/iconHelpers';

describe('getIcon', () => {
  test('returns icon for the requested name', () => {
    const icon = getIcon(IconNames.ADD);
    expect(icon).toEqual(expect.anything());
  });
});
