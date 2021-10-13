import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { HashRouter, Route } from 'react-router-dom';
import store from 'state/configureStore';
import TaggerApp from 'components/app/TaggerApp';

ReactDOM.render(
  <Provider store={store}>
    <HashRouter>
      <Route path="/" component={TaggerApp} />
    </HashRouter>
  </Provider>,
  document.getElementById('app'),
);
