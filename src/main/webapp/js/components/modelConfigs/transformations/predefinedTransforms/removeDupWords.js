import Constants from 'constants/Constants';

const removeDupWords = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    mappings: { },

  };
  const reg = /\\b(\\w+)(\\s\\1\\b)+/i;

  regex.mappings[reg] = '$1';

  return regex;
};

export default removeDupWords;
