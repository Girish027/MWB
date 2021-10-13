import Constants from 'constants/Constants';

const apostrophe = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    mappings: { },

  };
  const reg = /[`|‘|’]/i;
  regex.mappings[reg] = '\'';

  return regex;
};

export default apostrophe;
