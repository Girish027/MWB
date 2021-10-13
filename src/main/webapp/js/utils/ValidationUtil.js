import Constants from 'constants/Constants';
import * as _ from 'lodash';
import he from 'he';

class ValidationUtil {
  constructor() {
    this.validateTopicGoal = this.validateTopicGoal.bind(this);
  }

  checkField = (value, regexExpression = Constants.NAME_REGEX) => {
    const regExp = new RegExp(
      `^${
        regexExpression
      }$`,
      'i',
    );
    return regExp.test(value);
  }

  // Note for future: Topic Goal is a synonym for granular intent / manual intent / manual tag.
  validateTopicGoal(intent) {
    let invalid = true;

    if (typeof intent === 'undefined') return invalid;

    if (intent === null) return invalid;

    // it have an -
    const dash = (intent.indexOf('-') > -1);

    if (!dash) return invalid;

    // does it have 2 not empty parts?
    const parts = intent.split('-');
    if (parts.length != 2 || !(parts[0].trim()).length || !(parts[1].trim()).length) {
      return invalid;
    }

    invalid = false;

    // else all good
    return invalid;
  }

  validateWorldClassFile(content) {
    let isValid = false;
    if (content) {
      let lines = content.split('\n');
      if (lines.length < 2) {
        return isValid;
      }
      if (lines[0].startsWith(Constants.WORD_CLASS_SYNTAX) && lines[0].trim().split(' ').length === 1) {
        for (let line = 0; line < lines.length; line++) {
          if (lines[line].startsWith(Constants.WORD_CLASS_SYNTAX) && lines[line].trim().split(' ').length > 1) {
            isValid = false;
            break;
          } else if (lines[line].startsWith(Constants.WORD_CLASS_SYNTAX) && lines[line].trim().split(' ').length === 1) {
            isValid = true;
          } else if (lines[line].indexOf(Constants.WORD_CLASS_SYNTAX) > 0) {
            isValid = false;
            break;
          }
        }
      }
    }
    return isValid;
  }

  decodeDescription(dataArray) {
    const modifiedDataArray = dataArray.map(data => {
      const description = data.description ? he.decode(data.description) : data.description;
      return { ...data, description };
    });
    return modifiedDataArray;
  }

  validateUrl(url) {
    if (url) {
      const regexp = /((http|https):\/\/.)(www\.)?[-a-zA-Z0-9@:%._~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_.~#?&//=]*)/g;
      return regexp.test(url);
    }
    return true;
  }

  validateComment(comment) {
    let invalid = true;

    // comments can be blank since there is no delete API

    // if (comment !== '' && comment.length < 2000) {
    if (comment.length < 2000) {
      invalid = false;
    }

    return invalid;
  }

  validateWordClassLabel(label) {
    let isValid = true;
    if (!label || !label.startsWith(Constants.WORD_CLASS_SYNTAX)
       || label.length < Constants.MIN_CLASS_LENGTH
       || label.length > Constants.MAX_CLASS_LENGTH
       || label.trim().split(' ').length > 1) {
      isValid = false;
    }
    return isValid;
  }

  isEmpty(value) {
    return value === '' || value === null || typeof value === 'undefined' || value.length === 0;
  }

  validateTaggingGuideTag(value, isRequired) {
    if (typeof value === 'undefined') {
      return false;
    }
    value = `${value}`;
    if (!value && isRequired) {
      return false;
    }

    if (!value && !isRequired) {
      return true;
    }

    if ((`${value}`).length > 50) {
      return false;
    }

    const tagWordRegExpString = Constants.TAG_WORD_REGEX;
    const regExp = new RegExp(
      `^${
        tagWordRegExpString
      }-${
        tagWordRegExpString
      }$`,
      'i',
    );
    return regExp.test(value);
  }

  validatePartialTaggingGuideTag(value, isRequired) {
    if (typeof value === 'undefined') {
      return false;
    }
    value = `${value}`;
    if (!value && isRequired) {
      return false;
    } if (!value && !isRequired) {
      return true;
    }
    if ((`${value}`).length > 1000) {
      return false;
    }

    const tagWordRegExpString = Constants.TAG_WORD_REGEX;
    const wordRegExp = new RegExp(`^${tagWordRegExpString}$`);
    if (value.indexOf('-') == -1) {
      return wordRegExp.test(value);
    }
    const parts = value.split('-');
    if (parts.length != 2) {
      return false;
    }
    return wordRegExp.test(parts[0]) && (parts[1].length == 0 || wordRegExp.test(parts[1]));
  }

  validateTranscriptionHashComment(value, isRequired) {
    if (typeof value === 'undefined') {
      return false;
    }
    value = `${value}`;
    if (!value && isRequired) {
      return false;
    } if (!value && !isRequired) {
      return true;
    }
    if ((`${value}`).length > 1000) {
      return false;
    }
    return true;
  }

  validateTaggingGuideRow({ intent, rutag }) {
    if (!_.toString(intent).length && !_.toString(rutag).length) {
      return false;
    }
    return true;
  }

  validateJSONString(value) {
    try {
      JSON.parse(value);
    } catch (e) {
      return false;
    }
    return true;
  }
}

const validationUtil = new ValidationUtil();
export default validationUtil;
