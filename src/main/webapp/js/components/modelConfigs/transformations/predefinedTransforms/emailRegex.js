import Constants from 'constants/Constants';

const emailRegex = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.WORDCLASS_SUBST_REGEX,
    mappings: {},

  };
  regex.mappings = {
    '/(([\\w_\\.-])+@([\\d\\w\\.-])+\\.([a-z\\.]){2,6})/i': '_class_email',
  };

  return regex;
};

export default emailRegex;
