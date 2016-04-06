var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var mode_management = require('./mode_management');

var rootReducer = combineReducers({
	i18n,
	session,
	mode_management,
	routing: routerReducer
});

module.exports = rootReducer;
