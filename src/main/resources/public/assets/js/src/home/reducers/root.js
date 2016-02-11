var combineReducers = require('redux').combineReducers;
var locale = require('./locale');
var user = require('./user');

var rootReducer = combineReducers({
	locale,
	user
});

module.exports = rootReducer;
