import Constants from 'constants/Constants';

const sentenceMarker = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REPLACE,
    mappings: { },

  };
  const sentenceStart = /^/i;
  const sentenceEnd = /$/;
  regex.mappings[sentenceStart] = '_class_ss';
  regex.mappings[sentenceEnd] = '_class_se';

  return regex;
};

export default sentenceMarker;
