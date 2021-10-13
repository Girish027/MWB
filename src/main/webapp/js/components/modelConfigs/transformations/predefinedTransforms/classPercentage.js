import Constants from 'constants/Constants';

const classPercentage = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/[0-9]+\\.[0-9]*\\s?(?:%|percent(?:age|ile)?)/i': '_class_percentage',
    '/[0-9]+\\s?(?:%|percent(?:age|ile)?)/i': '_class_percentage',
    '/(?:one(?:\\s|-))?hundred\\s?(?:%|percent(?:age|ile)?)/i': '_class_percentage',
    '/(?:\\b(?:twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)(?:(?:\\s|-)?(?:one|two|three|four|five|six|seven|eight|nine))?)\\s?(?:%|percent(?:age|ile)?)/i': '_class_percentage',
    '/\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|hundred|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)\\s?(?:%|percent(?:age|ile)?)/i': '_class_percentage',
  };

  return regex;
};

export default classPercentage;
