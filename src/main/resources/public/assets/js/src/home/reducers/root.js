var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var query = require('./query');
var { routeReducer } = require('react-router-redux');

var rootReducer = combineReducers({
	locale,
	user,
	routing: routeReducer,
	device:combineReducers({
		query,
	})
});

module.exports = rootReducer;
