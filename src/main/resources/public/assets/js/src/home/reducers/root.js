var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var history = require('./history');

var { routeReducer } = require('react-router-redux');

var rootReducer = combineReducers({
	locale,
	user,
	routing: routeReducer,
  query,
  section: combineReducers({
    history
  })
});

module.exports = rootReducer;
