var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var history = require('./history');
var dashboard = require('./dashboard');

var { routerReducer } = require('react-router-redux');

var rootReducer = combineReducers({
  locale,
  user,
  routing: routerReducer,
  query,
  section: combineReducers({
    history,
    dashboard
  })
});

module.exports = rootReducer;
