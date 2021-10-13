import axios from 'axios';
import Constants from 'constants/Constants';

export const setDefaultsForAPI = (csrfToken) => {
  axios.defaults.headers.common['X-CSRF-TOKEN'] = csrfToken;
  axios.defaults.headers.common['Content-Type'] = 'application/json';
  axios.defaults.headers.common.Accept = 'application/json';
};

const api = {
  /**
   * Handle all error scenarios
   * @param  {[type]} error            [description]
   * @param  {[type]} handleError      [handle error for all non 2xx status and code erros]
   * @param  {[type]} handleNoResponse [request was made, but response was not recieved]
   */
  _handleError(error, onApiError = Constants.noop, onApiNoResponse = Constants.noop, dispatch) {
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      dispatch(onApiError(error.response.data));
    } else if (error.request) {
      // The request was made but no response was received
      // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
      // http.ClientRequest in node.js
      dispatch(onApiNoResponse(error));
    } else {
      // Something happened in setting up the request that triggered an Error
      dispatch(onApiError(error));
    }
  },

  _handleSuccess(response, onApiSuccess = Constants.noop, dispatch) {
    dispatch(onApiSuccess(response));
  },

  _api(apiData = {}, method = 'get') {
    const {
      url,
      onApiError = Constants.noop,
      onApiNoResponse = Constants.noop,
      onApiSuccess = Constants.noop,
      onFinalizeApi = Constants.noop,
      cancelToken = axios.CancelToken.source().token,
      data = {},
      params = {},
      headers = {},
      requestData = {},
      dispatch = () => {}, // donot pass dispatch if the handlers are not actions
    } = apiData;
    return axios[method](
      url,
      data,
      { headers, params, cancelToken },
    )
      .then((response) => {
        this._handleSuccess(response.data, onApiSuccess, dispatch);
      })
      .catch((error) => {
        this._handleError(error, onApiError, onApiNoResponse, dispatch);
      })
      .finally(onFinalizeApi);
  },

  /**
   * Abrstractions to be used.
   */

  get(apiData = {}) {
    return this._api(apiData, 'get');
  },

  post(apiData = {}) {
    return this._api(apiData, 'post');
  },

  patch(apiData = {}) {
    return this._api(apiData, 'patch');
  },

  put(apiData = {}) {
    return this._api(apiData, 'put');
  },

  delete(apiData = {}) {
    return this._api(apiData, 'delete');
  },

};

export default api;
