import { escapeRegExp, capitalizeFirstLetter } from 'utils/StringUtils';

describe('StringUtils', () => {
  describe('escapeRegex', () => {
    test('returns expected regex when passed in a value', () => {
      const val = escapeRegExp('/path/to/res?search=this.that');
      expect(val).toEqual('\\/path\\/to\\/res\\?search=this\\.that');
    });
  });
  describe('captializeFirstLetter', () => {
    test('returns expected string', () => {
      const val = capitalizeFirstLetter('models');
      expect(val).toEqual('Models');
    });
  });
});
