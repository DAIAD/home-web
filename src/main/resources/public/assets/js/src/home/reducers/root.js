var { combineReducers } = require('redux');
const { reducer:formReducer } = require('redux-form');
var { routerReducer } = require('react-router-redux');

var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var history = require('./history');
var dashboard = require('./dashboard');

var rootReducer = combineReducers({
  routing: routerReducer,
  form: formReducer,
  locale,
  user,
  query,
  section: combineReducers({
    history,
    dashboard
  })
});

module.exports = rootReducer;
