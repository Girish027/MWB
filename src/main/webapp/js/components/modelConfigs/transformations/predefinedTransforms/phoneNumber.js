import Constants from 'constants/Constants';

const phoneNumber = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/\\(?([0-9]{3})?[-.\\s)]*[0-9]{3}[-.\\s]*[0-9]{4}\\b/': '_class_phone_number',
    '/\\b[0-9]{5}[\\s\\.-]?[0-9]{5}\\b/': '_class_phone_number',
  };
  return regex;
};

export default phoneNumber;
