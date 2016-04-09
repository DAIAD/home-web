var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var admin = require('./admin');
var query = require('./query');

var rootReducer = combineReducers({
	i18n,
	session,
	admin,
	query,
	routing: routerReducer
});

module.exports = rootReducer;
