import { displayWarningRequestMessage } from 'state/actions/actions_app';
import { getLanguage } from 'state/constants/getLanguage';

const lang = getLanguage();

/*

 Confluence API & Status Code

 https://247inc.atlassian.net/wiki/display/APT/Status+and+Error+Codes
 https://247inc.atlassian.net/wiki/display/APT/APIs

 Fetch Tutorial on Error Handling

 this error handler is based off this example but instead of throwing an error, I want redux to display the error in a notification
 https://www.tjvantoll.com/2015/09/13/fetch-and-errors/

 */

class ErrorMessageUtil {
  constructor() {
    this.handleErrors = this.handleErrors.bind(this);
    this.getErrorMessage = this.getErrorMessage.bind(this);

    this.dispatchError = this.dispatchError.bind(this);
  }

  getErrorMessage(status) {
    // if ( response.statusText) return response.statusText;

    switch (Number(status)) {
    case 503:
      return lang.ERROR_SERVER_503; // elastic search not available
    case 500:
      return lang.ERROR_SERVER_500; // server_error
    case 400:
      return lang.ERROR_SERVER_400; // bad_request
    case 401:
      return lang.ERROR_SERVER_401; // unauthorized
    case 403:
      return lang.ERROR_SERVER_403; // Forbidden error
    case 404:
      return lang.ERROR_SERVER_404; // not_found
    case 405:
      return lang.ERROR_SERVER_405; // not_allowed - ie: Post instead of Get
    case 406:
      return lang.ERROR_SERVER_406; // not_acceptable - may happen on file upload
    case 409:
      return lang.ERROR_SERVER_409; // conflict - ie: deleting an intent being used
    case 429:
      return lang.ERROR_SERVER_TOO_MANY; // too_many_requests
    default:
      return lang.ERROR_SERVER_UNKNOWN;
    }
  }

  handleErrors(response) {
    if (!response.ok) {
      throw response;
    } else {
      return response;
    }
  }

  dispatchError(error, dispatch, serverErrorAction) {
    const errorNum = Number(error.status);

    if (error.message) {
      if (error.message === 'Failed to fetch') {
        dispatch(displayWarningRequestMessage(lang.ERROR_SERVER_DOWN));
      } else if (error.message === 'Transformation failed') {
        dispatch(serverErrorAction(lang.ERROR_TRANSFORM_FAIL));
      }
    } else {
      error.text().then((value) => {
        if (Number(error.status) === 409) {
          let JsonErrorObject;
          try {
            JsonErrorObject = JSON.parse(value);
          } catch (e) {
            /* do nothing */
          }
          if (JsonErrorObject && JsonErrorObject.message) {
            dispatch(displayWarningRequestMessage(JsonErrorObject.message));
          } else {
            dispatch(displayWarningRequestMessage(value));
          }
        } else {
          let JsonErrorObject;
          try {
            JsonErrorObject = JSON.parse(value);
          } catch (e) {
            /* do nothing */
          }
          if (JsonErrorObject && JsonErrorObject.message) {
            dispatch(serverErrorAction(`${JsonErrorObject.message}`));
          } else {
            dispatch(serverErrorAction(`${value}`));
          }
        }
      });
    }
  }
}

const errorMessageUtil = new ErrorMessageUtil();

export default errorMessageUtil;
