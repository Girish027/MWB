/**
 * 24/7 Customer, Inc. Confidential, Do Not Distribute. This is an
 * unpublished, proprietary work which is fully protected under
 * copyright law. This code may only be used pursuant to a valid
 * license from 24/7 Customer, Inc.
 */

/**
 * Object Utils
 * @module utils/object-utils
 */


const reducer = (acc, value) => ((acc === null || acc === undefined) ? undefined : acc[value]);

class ObjectUtils {
  static get(keys, object, defaultValue = undefined) {
    return keys.reduce(reducer, object) || defaultValue;
  }

  static isUndefinedOrNull(obj) {
    return obj === undefined || obj === null;
  }

  static isEmptyOrNull(obj) {
    return ObjectUtils.isUndefinedOrNull(obj) || obj === '';
  }

  /**
   * @ref https://stackoverflow.com/a/11433067
   */
  static createNestedObject(object, keysArr, value) {
    let _object = object;
    const lastKey = keysArr.pop();
    keysArr.map((key) => {
      _object[key] = _object[key] || {};
      _object = _object[key];
      return 0;
    });
    _object[lastKey] = value;
    _object = _object[lastKey];
    return _object;
  }

  static removeEmpty(object) {
    const _object = object;
    Object.keys(_object).forEach((key) => {
      if (ObjectUtils.isEmptyOrNull(_object[key])) {
        delete _object[key];
      }
    });
    return _object;
  }

  static removeSpecificKeys(obj, keys) {
    const retVal = obj;
    keys.map((key) => delete retVal[key]);
    return retVal;
  }

  /**
   * Attempts to rehydrate a JSON string into an object
   * @param  {string}   str      The JSON string
   * @param  {Function} callback The function to invoke with the parse error or the parsed object
   * @private
   */
  static jsonToObject(str, callback) {
    let err;
    let obj;

    if (str) {
      try {
        obj = JSON.parse(str);
      } catch (e) {
        err = e;
      }
    }

    callback(err, obj);
  }

  static cleanObject(obj) {
    const tempObj = Object.assign({}, obj);
    Object.keys(tempObj).forEach((key) => {
      if (tempObj[key] && typeof tempObj[key] === 'object') ObjectUtils.cleanObject(tempObj[key]);
      else if (ObjectUtils.isUndefinedOrNull(tempObj[key])) delete tempObj[key];
    });
    return tempObj;
  }

  static find(list, key, value) {
    return list.find((item) => item.get(key) === value);
  }

  static findIndex(list, key, value) {
    return list.findIndex((item) => item.get(key) === value);
  }
}

export default ObjectUtils;
