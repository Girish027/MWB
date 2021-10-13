import React, { Component } from 'react';
import Request from 'react-http-request';
import _ from 'lodash';
import getUrl, { pathKey } from 'utils/apiUrls';

class LocaleSelect extends Component {
  constructor(props) {
    super(props);
    this.processResults = this.processResults.bind(this);
  }

  processResults(result) {
    const locales = !_.isNil(result) && !_.isNil(result.body) ? result.body.sort() : [];
    return (
      <select {...this.props.input}>
        <optgroup>
          {locales.map(locale => (
            <option
              value={locale}
              key={locale}
            >
              {locale}
            </option>
          ))}
        </optgroup>
      </select>
    );
  }

  render() {
    // TODO - Move fetch outside of the select control
    const fetchUrl = getUrl(pathKey.locales);
    return (
      <div>
        <label>{this.props.label}</label>
        <Request
          url={fetchUrl}
          method="get"
          accept="application/json"
          verbose
        >
          {
            ({/* error, */ result, loading }) => {
              if (loading) {
                return (<option value="">loading...</option>);
              }
              return this.processResults(result);
            }
          }
        </Request>
      </div>
    );
  }
}
export default LocaleSelect;
