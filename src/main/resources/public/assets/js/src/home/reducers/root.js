var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');
var sessions = require('./sessions');
var query = require('./query');

var rootReducer = combineReducers({
	locale,
	user,
	device:combineReducers({
		query,
	})
});

module.exports = rootReducer;
