import Constants from 'constants/Constants';

const classNumber = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/\\b[0-9]+(?:\\s*[\\.,]\\s*[0-9]+)*(?:\\s*(?:nd|th|rd|st))?\\b/i': '_class_number',
    '/\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|hundred|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)\'?s\\b/i': '_class_number',
    '/#(?=[^a-zA-Z0-9])/i': '_class_number_ref',
  };

  return regex;
};

export default classNumber;
