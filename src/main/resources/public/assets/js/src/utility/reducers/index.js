var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var mode_management = require('./mode_management');
var admin = require('./admin');
var alerts = require('./alerts');
var query = require('./query');
var debug = require('./debug');


var rootReducer = combineReducers({
	i18n,
	session,
	mode_management,
	admin,
        alerts,
	query,
	debug,
	routing: routerReducer
});

module.exports = rootReducer;
