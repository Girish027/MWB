import Constants from 'constants/Constants';

const htmlEncoding = () => {
  const regex = {
    type: Constants.TRANSFORMATION_TYPES.REGEX_REMOVAL,
    mappings: {},

  };
  regex.list = [
    '/%[0-9]+/',
  ];

  return regex;
};

export default htmlEncoding;
