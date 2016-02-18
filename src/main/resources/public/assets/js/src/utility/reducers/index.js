var { combineReducers } = require('redux');
var { routeReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');

var rootReducer = combineReducers({
	i18n,
	session,
    routing: routeReducer
});

module.exports = rootReducer;
