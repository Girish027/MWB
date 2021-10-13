import Constants from 'constants/Constants';

const urlRegex = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/https?:\\/\\/(www\\.)?[\\-a-zA-Z0-9@:%._\\+~#=/]+/i': '_class_url',
  };

  return regex;
};

export default urlRegex;
