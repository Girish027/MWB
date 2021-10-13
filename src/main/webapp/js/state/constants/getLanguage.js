import config from 'config';
import * as english from './english';

export const getLanguage = () => {
  switch (config.lang) {
  case 'english':
  default:
    return english;
  }
};
