import Constants from 'constants/Constants';

const nonBreakingSpace = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    mappings: { },

  };
  const reg = /\xao/i;
  regex.mappings[reg] = ' ';

  return regex;
};

export default nonBreakingSpace;
