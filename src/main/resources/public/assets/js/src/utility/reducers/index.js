var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var demographics = require('./demographics');
var mode_management = require('./mode_management');
var admin = require('./admin');
var query = require('./query');
var debug = require('./debug');


var rootReducer = combineReducers({
	i18n,
	session,
	demographics,
	mode_management,
	admin,
	query,
	debug,
	routing: routerReducer
});

module.exports = rootReducer;
