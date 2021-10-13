import { escapeRegExp } from 'utils/StringUtils';

describe('StringUtils', () => {
  describe('escapeRegex', () => {
    test('returns expected regex when passed in a value', () => {
      const val = escapeRegExp('/path/to/res?search=this.that');
      expect(val).toEqual('\\/path\\/to\\/res\\?search=this\\.that');
    });
  });
});
