var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var history = require('./history');
var dashboard = require('./dashboard');
var messages = require('./messages');
var forms = require('./forms');

var rootReducer = combineReducers({
  routing: routerReducer,
  locale,
  user,
  query,
  forms,
  section: combineReducers({
    history,
    dashboard
  }),
  messages
});

module.exports = rootReducer;
