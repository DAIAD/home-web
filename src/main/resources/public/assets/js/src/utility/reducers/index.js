var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');

var rootReducer = combineReducers({
	i18n,
	session,
	routing: routerReducer
});

module.exports = rootReducer;
