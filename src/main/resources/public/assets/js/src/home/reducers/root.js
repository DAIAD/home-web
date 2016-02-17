var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var lastSession = require('./lastSession');
var sessions = require('./sessions');

var rootReducer = combineReducers({
	locale,
	user,
	device:combineReducers({
		lastSession,
		sessions
	})
});

module.exports = rootReducer;
