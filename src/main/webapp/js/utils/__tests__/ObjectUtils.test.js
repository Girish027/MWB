import ObjectUtils from 'utils/ObjectUtils';

describe('objectUtils', () => {
  describe('isUndefinedOrNull', () => {
    test('returns false when passed in a value', () => {
      const val = ObjectUtils.isUndefinedOrNull('test');
      expect(val).toBe(false);
    });

    test('returns true when nothing is passed', () => {
      const val = ObjectUtils.isUndefinedOrNull(null);
      expect(val).toBe(true);
    });

    test('returns false when passed in a value', () => {
      const val = ObjectUtils.isUndefinedOrNull(null);
      expect(val).toBe(true);
    });
  });

  describe('isEmptyOrNull', () => {
    test('returns false when passed in a value', () => {
      const val = ObjectUtils.isEmptyOrNull('test');
      expect(val).toBe(false);
    });

    test('returns true when nothing is passed', () => {
      const val = ObjectUtils.isEmptyOrNull(null);
      expect(val).toBe(true);
    });

    test('returns false when passed in a value', () => {
      const val = ObjectUtils.isEmptyOrNull(null);
      expect(val).toBe(true);
    });
  });

  describe('get', () => {
    test('returns valued when passed in a value', () => {
      const val = ObjectUtils.get(['abc', 'cde'], { abc: 'sdfd', cde: 'dsdsa' }, 'tre');
      expect(val).toBe('tre');
    });

    test('returns undefined when no value passed', () => {
      const val = ObjectUtils.get(['abc', 'cde'], { abc: 'sdfd', cde: 'dsdsa' });
      expect(val).toBe(undefined);
    });

    test('returns object when object value passed', () => {
      const val = ObjectUtils.get([], { fgg: 'fdsfd' });
      expect(val).toStrictEqual({ fgg: 'fdsfd' });
    });

    test('returns {}} when np value passed', () => {
      const val = ObjectUtils.get([], {});
      expect(val).toStrictEqual({});
    });
  });

  describe('createNestedObject', () => {
    test('returns false when passed in a value', () => {
      const val = ObjectUtils.createNestedObject({}, ['abc', 'cde'], 'tre');
      expect(val).toBe('tre');
    });
  });

  describe('removeEmpty', () => {
    test('returns empty object when empty object is passed', () => {
      const val = ObjectUtils.removeEmpty({});
      expect(val).toStrictEqual({});
    });

    test('returns object with empty value removed', () => {
      const val = ObjectUtils.removeEmpty({ abc: 'cde', fggf: '' });
      expect(val).toStrictEqual({ abc: 'cde' });
    });
  });

  describe('removeSpecificKeys', () => {
    test('returns empty object when empty object is passed', () => {
      const val = ObjectUtils.removeSpecificKeys({}, []);
      expect(val).toStrictEqual({});
    });

    test('returns object with keys removed from the list value removed', () => {
      const val = ObjectUtils.removeSpecificKeys({ abc: 'cde', fggf: '' }, ['fggf']);
      expect(val).toStrictEqual({ abc: 'cde' });
    });
  });

  describe('jsonToObject', () => {
    test('returns empty object when empty string is passed', () => {
      const val = ObjectUtils.jsonToObject('', (err, obj) => {});
      expect(val).toMatchSnapshot();
    });

    test('returns object with keys removed from the list value removed', () => {
      const val = ObjectUtils.jsonToObject('{ abc: \'cde\', fggf: \'\' }', (err, obj) => {});
      expect(val).toMatchSnapshot();
    });
  });

  describe('cleanObject', () => {
    test('returns object with null removed from the object', () => {
      const val = ObjectUtils.cleanObject({ abc: 'cde', fggf: '', sssdd: null });
      expect(val).toStrictEqual({ abc: 'cde', fggf: '' });
    });

    test('returns object with undefined removed from the object', () => {
      const val = ObjectUtils.cleanObject({ abc: 'cde', fggf: '', sssdd: undefined });
      expect(val).toStrictEqual({ abc: 'cde', fggf: '' });
    });

    test('returns nested object from the object', () => {
      const val = ObjectUtils.cleanObject({ abc: 'cde', fggf: '', sssdd: { adsd: 'dfdsf' } });
      expect(val).toStrictEqual({ abc: 'cde', fggf: '', sssdd: { adsd: 'dfdsf' } });
    });

    test('returns empty object when empty object is passed', () => {
      const val = ObjectUtils.cleanObject({});
      expect(val).toStrictEqual({});
    });
  });
});
