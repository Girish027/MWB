import Constants from 'constants/Constants';

const punctutationNormalization = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    mappings: {},
  };
  regex.mappings = {
    // eslint-disable-next-line no-useless-escape
    '/(\\-|\\.|\\,|\\?|\\!|\\*|\\$|\\\\|\\%|\\&|\\#|\\@|\\:|\\||\"|\\/|\\)|\\()/i': ' ',
  };
  return regex;
};

export default punctutationNormalization;
