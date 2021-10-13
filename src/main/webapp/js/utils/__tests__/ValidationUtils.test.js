import validationUtil from 'utils/ValidationUtil';

describe('ValidationUtil', () => {
  describe('isEmpty', () => {
    test('returns false when passed in a value', () => {
      const val = validationUtil.isEmpty('test');
      expect(val).toBe(false);
    });
    test('returns false when passed in a true', () => {
      const val = validationUtil.isEmpty(true);
      expect(val).toBe(false);
    });
    test('returns false when passed in a 1', () => {
      const val = validationUtil.isEmpty(1);
      expect(val).toBe(false);
    });
    test('returns false when passed false', () => {
      const val = validationUtil.isEmpty(false);
      expect(val).toBe(false);
    });
    test('returns false when passed 0', () => {
      const val = validationUtil.isEmpty(0);
      expect(val).toBe(false);
    });
    test('returns true when passed empty string', () => {
      const val = validationUtil.isEmpty('');
      expect(val).toBe(true);
    });
    test('returns true when passed undefined', () => {
      const val = validationUtil.isEmpty(undefined);
      expect(val).toBe(true);
    });
    test('returns true when passed null', () => {
      const val = validationUtil.isEmpty(null);
      expect(val).toBe(true);
    });
  });

  describe('validateTaggingGuideTag', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateTaggingGuideTag(undefined)).toBe(false);
    });
    test('returns true when passed empty string', () => {
      expect(validationUtil.validateTaggingGuideTag('')).toBe(true);
    });
    test('returns false when passed 0', () => {
      expect(validationUtil.validateTaggingGuideTag(0)).toBe(false);
    });
    test('returns false when passed false', () => {
      expect(validationUtil.validateTaggingGuideTag(false)).toBe(false);
    });
    test('returns false when passed undefined, true', () => {
      expect(validationUtil.validateTaggingGuideTag(undefined, true)).toBe(false);
    });
    test('returns false when passed empty string, true', () => {
      expect(validationUtil.validateTaggingGuideTag('', true)).toBe(false);
    });
    test('returns false when passed 0, true', () => {
      expect(validationUtil.validateTaggingGuideTag(0, true)).toBe(false);
    });
    test('returns false when passed false, true', () => {
      expect(validationUtil.validateTaggingGuideTag(false, true)).toBe(false);
    });
    test('returns true when passed "topic-goal"', () => {
      expect(validationUtil.validateTaggingGuideTag('topic-goal')).toBe(true);
    });
    test('returns false when passed "topic-goal "', () => {
      expect(validationUtil.validateTaggingGuideTag('topic-goal ')).toBe(false);
    });
    test('returns false when passed "topicgoal "', () => {
      expect(validationUtil.validateTaggingGuideTag('topicgoal ')).toBe(false);
    });
    test('returns false when passed "-goal "', () => {
      expect(validationUtil.validateTaggingGuideTag('-goal ')).toBe(false);
    });
    test('returns false when passed "topic-goal-"', () => {
      expect(validationUtil.validateTaggingGuideTag('topic-goal-')).toBe(false);
    });
    test('returns false when passed "topic-goal%"', () => {
      expect(validationUtil.validateTaggingGuideTag('topic-goal%')).toBe(false);
    });
    test('returns true when passed "topic-goal1_goal2"', () => {
      expect(validationUtil.validateTaggingGuideTag('topic-goal1_goal2')).toBe(true);
    });
    test('returns true when passed "тема-цель"', () => {
      expect(validationUtil.validateTaggingGuideTag('тема-цель')).toBe(true);
    });
  });

  describe('validateWordClassLabel', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateWordClassLabel(undefined)).toBe(false);
    });
    test('returns false when passed only class without value', () => {
      expect(validationUtil.validateWordClassLabel('_class_')).toBe(false);
    });
    test('returns true when passed class with value', () => {
      expect(validationUtil.validateWordClassLabel('_class_xyz')).toBe(true);
    });
    test('returns false when passed random value', () => {
      expect(validationUtil.validateWordClassLabel('abc')).toBe(false);
    });
    test('returns false when passed class with space seprated value', () => {
      expect(validationUtil.validateWordClassLabel('_class_ddfd dfdf')).toBe(false);
    });
  });

  describe('validatePartialTaggingGuideTag', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(undefined)).toBe(false);
    });
    test('returns true when passed empty string', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('')).toBe(true);
    });
    test('returns true when passed 0', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(0)).toBe(true);
    });
    test('returns true when passed false', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(false)).toBe(true);
    });
    test('returns false when passed undefined, true', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(undefined, true)).toBe(false);
    });
    test('returns false when passed empty string, true', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('', true)).toBe(false);
    });
    test('returns true when passed 0, true', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(0, true)).toBe(true);
    });
    test('returns true when passed false, true', () => {
      expect(validationUtil.validatePartialTaggingGuideTag(false, true)).toBe(true);
    });
    test('returns true when passed "topic-goal"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-goal')).toBe(true);
    });
    test('returns false when passed "topic-goal "', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-goal ')).toBe(false);
    });
    test('returns false when passed "topicgoal "', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topicgoal ')).toBe(false);
    });
    test('returns false when passed "-goal "', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('-goal ')).toBe(false);
    });
    test('returns false when passed "topic-goal-"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-goal-')).toBe(false);
    });
    test('returns false when passed "topic-goal%"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-goal%')).toBe(false);
    });
    test('returns true when passed "topic-goal1_goal2"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-goal1_goal2')).toBe(true);
    });
    test('returns true when passed "тема-цель"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('тема-цель')).toBe(true);
    });
    test('returns true when passed "topic"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic')).toBe(true);
    });
    test('returns true when passed "topic-"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic-')).toBe(true);
    });
    test('returns афдыу when passed "topic--"', () => {
      expect(validationUtil.validatePartialTaggingGuideTag('topic--')).toBe(false);
    });
  });

  describe('validateUrl', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateUrl(undefined)).toBe(true);
    });
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateUrl(null)).toBe(true);
    });
    test('returns true when passed empty string', () => {
      expect(validationUtil.validateUrl('')).toBe(true);
    });
    test('returns true when passed valid http url "http://topic-goal.com"', () => {
      expect(validationUtil.validateUrl('http://topic-goal.com')).toBe(true);
    });
    test('returns true when passed valid https url "https://topic-goal.com"', () => {
      expect(validationUtil.validateUrl('https://topic-goal.com')).toBe(true);
    });
    test('returns false when passed valid ftp url "ftp://topic-goal.com"', () => {
      expect(validationUtil.validateUrl('ftp://topic-goal.com')).toBe(false);
    });
    test('returns false when passed random "topic-goal" string', () => {
      expect(validationUtil.validateUrl('topic-goal')).toBe(false);
    });
  });

  describe('validateWorldClassFile', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateWorldClassFile(undefined)).toBe(false);
    });
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateWorldClassFile(null)).toBe(false);
    });
    test('returns false when passed empty string', () => {
      expect(validationUtil.validateWorldClassFile('')).toBe(false);
    });
    test('returns false when passed invalid wordclass tag "_class_family 1"', () => {
      expect(validationUtil.validateWorldClassFile('_class_family 1')).toBe(false);
    });
    test('returns false when passed valid wordclass tag but no content"_class_family_1"', () => {
      expect(validationUtil.validateWorldClassFile('_class_family_1')).toBe(false);
    });
    test('returns false when passed invalid wordclass tag "__class_family_1"', () => {
      expect(validationUtil.validateWorldClassFile('__class_family_1')).toBe(false);
    });
    test('returns false when passed valid wordclass tag "__classfamily_1"', () => {
      expect(validationUtil.validateWorldClassFile('_classfamily_1')).toBe(false);
    });
    test('returns false when passed invalid wordclass tag and content', () => {
      expect(validationUtil.validateWorldClassFile('__class_family_1\n'
        + 'son')).toBe(false);
    });
    test('returns false when passed invalid wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family 1\n'
        + 'son')).toBe(false);
    });
    test('returns true when passed valid wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son')).toBe(true);
    });
    test('returns true when passed valid multiline wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son\n'
        + 'daughter\n'
        + 'father')).toBe(true);
    });
    test('returns true when passed valid multiline multi class wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son\n'
        + 'daughter\n'
        + 'father\n'
        + '_class_number\n'
        + 'nine\n'
        + 'ten')).toBe(true);
    });
    test('returns false when passed invalid multiline multi class wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son\n'
        + 'daughter\n'
        + 'father\n'
        + '_class_number 1\n'
        + 'nine\n'
        + 'ten')).toBe(false);
    });
    test('returns true when passed valid multiline multi class wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son\n'
        + 'daughter\n'
        + 'father\n'
        + '_class__number\n'
        + 'nine\n'
        + 'ten')).toBe(true);
    });
    test('returns false when passed invalid multiline multi class wordclass content', () => {
      expect(validationUtil.validateWorldClassFile('_class_family\n'
        + 'son\'s\n'
        + 'daughter\n'
        + 'father\n'
        + '__class_number\n'
        + 'nine\n'
        + 'ten')).toBe(false);
    });
  });

  describe('validateTranscriptionHashComment', () => {
    test('returns false when passed undefined', () => {
      expect(validationUtil.validateTranscriptionHashComment(undefined)).toBe(false);
    });
    test('returns true when passed empty string', () => {
      expect(validationUtil.validateTranscriptionHashComment('')).toBe(true);
    });
    test('returns false when passed empty string and required', () => {
      expect(validationUtil.validateTranscriptionHashComment('', true)).toBe(false);
    });
    test('returns true when passed something', () => {
      expect(validationUtil.validateTranscriptionHashComment('something')).toBe(true);
    });
  });

  describe('validateTaggingGuideRow', () => {
    test('returns false if both rutag and granular intent is not present', () => {
      expect(validationUtil.validateTaggingGuideRow({ rutag: undefined, intent: undefined })).toBe(false);
      expect(validationUtil.validateTaggingGuideRow({ rutag: undefined, intent: '' })).toBe(false);
      expect(validationUtil.validateTaggingGuideRow({ rutag: '', intent: undefined })).toBe(false);
      expect(validationUtil.validateTaggingGuideRow({ rutag: null, intent: null })).toBe(false);
    });
    test('returns true if one of rutag and granular intent is present', () => {
      expect(validationUtil.validateTaggingGuideRow({ rutag: 't-t', intent: undefined })).toBe(true);
      expect(validationUtil.validateTaggingGuideRow({ rutag: undefined, intent: 'T_T' })).toBe(true);
      expect(validationUtil.validateTaggingGuideRow({ rutag: 't-t', intent: 'T_T' })).toBe(true);
    });
  });

  describe('validateJSONString', () => {
    test('returns false if json is not valid', () => {
      const jsonStr = '_class_family';
      expect(validationUtil.validateJSONString(jsonStr)).toBe(false);
    });
    test('returns true if json is valid', () => {
      const jsonStr = '{ "rutag": "t-t", "intent": "reservation" }';
      expect(validationUtil.validateJSONString(jsonStr)).toBe(true);
    });
  });

  describe('decodeHandler', () => {
    test('returns decoded value of given string &#64;', () => {
      const data = [
        {
          clientId: '151',
          description: '&#64;',
        },
      ];
      const val = validationUtil.decodeDescription(data);
      expect(val).toEqual([{ clientId: '151', description: '@' }]);
    });
    test('returns decoded value of given string &amp', () => {
      const data = [
        {
          clientId: '151',
          description: '&amp',
        },
      ];
      const val = validationUtil.decodeDescription(data);
      expect(val).toEqual([{ clientId: '151', description: '&' }]);
    });
    test('should not fail in case of null value', () => {
      const data = [
        {
          clientId: '151',
          description: null,
        },
      ];
      const val = validationUtil.decodeDescription(data);
      expect(val).toEqual([{ clientId: '151', description: null }]);
    });
    test('should not decode the normal string value of description', () => {
      const data = [
        {
          clientId: '151',
          description: 'ABC',
        },
      ];
      const val = validationUtil.decodeDescription(data);
      expect(val).toEqual([{ clientId: '151', description: 'ABC' }]);
    });
    test('should not fail in case of empty string', () => {
      const data = [
        {
          clientId: '151',
          description: '',
        },
      ];
      const val = validationUtil.decodeDescription(data);
      expect(val).toEqual([{ clientId: '151', description: '' }]);
    });
  });
});
