import Constants from 'constants/Constants';

/* eslint-disable no-useless-escape */
const classTime = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/\d{1,2}\s*(?:(?:am|pm)|(?:\s:\s\d{1,2})\s*(?:am|pm)?)/i': '_class_time',
  };

  return regex;
};

export default classTime;
