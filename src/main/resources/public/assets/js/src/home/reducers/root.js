var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var sessions = require('./sessions');
var query = require('./query');
var measurements = require('./measurements');

var rootReducer = combineReducers({
	locale,
	user,
	device:combineReducers({
		query,
		sessions,
		//measurements
	})
});

module.exports = rootReducer;
