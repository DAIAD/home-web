var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var history = require('./history');
var dashboard = require('./dashboard');
var message = require('./message');

var { routerReducer } = require('react-router-redux');

var rootReducer = combineReducers({
  locale,
  user,
  routing: routerReducer,
  query,
  section: combineReducers({
    history,
    dashboard
  }),
  message
});

module.exports = rootReducer;
