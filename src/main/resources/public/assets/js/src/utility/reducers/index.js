var { combineReducers } = require('redux');
var { routerReducer } = require('react-router-redux');

var i18n = require('./i18n');
var session = require('./session');
var mode_management = require('./mode_management');
var admin = require('./admin');
var query = require('./query');
var debug = require('./debug');
var reports = require('./reports');
var config = require('./config');

var rootReducer = combineReducers({
	i18n,
  config,
	session,
	mode_management,
	admin,
	query,
	debug,
	routing: routerReducer,
  reports,
});

module.exports = rootReducer;
