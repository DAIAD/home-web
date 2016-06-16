// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReduxProvider = require('react-redux').Provider;
var Router = require('react-router').Router;
require('babel-polyfill');


var routes = require('./routing/routes');
var store = require('./store/configureStore');

var { syncHistoryWithStore } = require('react-router-redux');
var history = require('./routing/history');
history = syncHistoryWithStore(history, store);

//Actions
var LocaleActions = require('./actions/LocaleActions');
var UserActions = require('./actions/UserActions');


ReactDOM.render(
  <ReduxProvider store={store}>
    <Router 
      history={history}
      routes={routes}
    />
  </ReduxProvider>,
  document.getElementById('app'));

